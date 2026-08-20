## Context

See proposal.md - Why. Gradle 9.7.1 deprecated the Kotlin DSL test-suite delegate idioms that the module build files
use to bind `JvmTestSuite` objects inside `testing { suites { ... } }`:
- `val test by getting(JvmTestSuite::class)` — the `getting`/`provideDelegate` extension is deprecated.
- `val suite by register<JvmTestSuite>("...") { ... }` — the `NamedDomainObjectProvider.getValue` extension used by
  the `by` delegate is deprecated.

The replacement APIs (`getByName` / `register` on the suites container) are non-deprecated. The refactor is confined to
the eight module build files that use these delegates plus the AGENTS.md convention text that references the deprecated
form. No suite behavior, ordering, dependencies, or test-tier placement changes.

## Goals / Non-Goals

- **Goals**: Remove every Gradle 9.6+ test-suite delegate deprecation warning from the module build files; keep the
  suites bound as `val`s so they remain referenceable in `shouldRunAfter` / `mustRunAfter`; update the AGENTS.md
  convention text to the non-deprecated idiom.
- **Non-Goals**: Not changing suite behavior, ordering, dependencies, or test tiers; not addressing other Gradle-10
  deprecation warnings unrelated to test suites; not altering the `withType<JvmTestSuite>` shared-dependency block.

## Decisions

- **`by getting(JvmTestSuite::class)` → `getByName<JvmTestSuite>("test")`.**
  - This follows the deprecation message's own guidance ("Use 'val element = getByName<Type>(name)' instead") and
    preserves the exact type (`JvmTestSuite`), so `shouldRunAfter(test)` call sites are unchanged.
- **`by register<JvmTestSuite>(name) { ... }` → `register<JvmTestSuite>(name) { ... }` bound to a val.**
  - Keeping the `val` binding preserves the lazy `NamedDomainObjectProvider<JvmTestSuite>` and the property
    referenced in `shouldRunAfter` / `mustRunAfter`. The ordering APIs accept both a concrete task and a
    `Provider<Task>`, so passing the provider (instead of the previously auto-unwrapped suite) needs no `.get()` and
    keeps registration lazy.
  - Alternative rejected: eagerly resolving with `register(...).get()` would defeat laziness and force the suite to
    be realized during configuration, which is unnecessary.
- **Update the AGENTS.md convention note** to cite the non-deprecated binding form (e.g.
  `val integrationTest = suites.register<JvmTestSuite>("integrationTest")`).

## Risks / Trade-offs

- [A `NamedDomainObjectProvider` passed to `shouldRunAfter`/`mustRunAfter` may behave differently from the
  auto-unwrapped suite] → Mitigation: these APIs accept task providers natively; behavior is identical. Verified by a
  full configure (`--warning-mode all`) and, if needed, by running the affected test tasks.
- [Missed occurrences elsewhere (e.g. build-logic convention plugins)] → Mitigation: build-logic uses
  `withType<JvmTestSuite>` (not the deprecated delegates) and needs no change; confirmed during the initial scan.
- [The refactor could subtly reorder suite resolution] → Mitigation: `getByName` and `register` preserve the existing
  eager/lazy semantics of `getting` and `register`; no ordering change.

## Migration Plan

1. In each of the eight module build files, replace the `by getting` and `by register` delegates with the container
   `getByName` / `register` forms; adjust the `shouldRunAfter` / `mustRunAfter` call sites to pass the provider where
   needed.
2. Update the AGENTS.md convention note to the non-deprecated binding form.
3. Verify with a full configuration (`./gradlew help --warning-mode all`) that the test-suite deprecation warnings are
   gone and no new warnings appear.
4. Run a build sanity check (e.g. `./gradlew compileTestJava` or the relevant module `check` subset) to confirm the
   suites still wire up.

## Open Questions

- None. The replacement patterns are unambiguous and verified against the Gradle 9.6 deprecation messages.