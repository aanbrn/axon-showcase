# Design: Restore UnstableApiUsage suppressions for the test-suites DSL and settings scripts

## Context

See proposal.md — Why. The prior suppression-removal change judged redundancy solely by whether the Gradle compiler
errored. That test is insufficient: Gradle enforces `@UnstableApi` (error) but not `@Incubating`, while the IntelliJ
`UnstableApiUsage` inspection flags both. The 9 test-suites-DSL scripts (5–15 warnings each) and the 2 settings
scripts (1 warning each) still trigger the inspection.

## Goals / Non-Goals

**Goals:**
- Restore the suppression to exactly the files that still carry IDE `UnstableApiUsage` warnings.
- Use the same suppression form as before the over-removal (file-level for build scripts, inline for settings).
- Leave the genuinely-redundant case (`showcase-command-api`) untouched.

**Non-Goals:**
- Changing any build behavior or dependency wiring (the directives are compiler/IDE-only).
- Changing the IDE inspection configuration project-wide (too blunt; would hide real future warnings).
- Re-opening the already-restored `showcase-query-client` file.

## Decisions

- **Restore the exact pre-removal form.** `@file:Suppress("UnstableApiUsage")` on the nine test-suites build scripts;
  the scoped inline `@Suppress("UnstableApiUsage")` inside `dependencyResolutionManagement { }` on the two settings
  scripts (matching the original state before `remove-redundant-unstable-api-suppressions`).
- **The build remains the authority for compilation.** These are optional IDE-hygiene directives; the Gradle compiler
  never errored on them, so `spotlessApply`/`check` are unaffected. This is consistent with the repo's stance that IDE
  inspections are non-gating but should be kept clean.
- **Scope by measured warnings, not by DSL usage.** `showcase-command-api` uses neither the test-suites DSL nor
  `dependencyResolutionManagement`, shows zero warnings, and is excluded — preserving the one genuinely-redundant
  removal from the prior change.

## Risks / Trade-offs

- [The suppression hides a future genuinely-`@UnstableApi` usage in these files] → Mitigation: same trade-off as
  before the removal; the test-suites DSL is the file's only unstable surface and is explicitly `@Incubating`, so the
  suppression is not hiding a compile error.
- [A future Gradle bump re-annotates the DSL from `@Incubating` to `@UnstableApi`] → Mitigation: that would turn the
  usage into a compiler error; the suppression would then be load-bearing rather than redundant, which is acceptable
  and matches the pre-removal state.

## Migration Plan

1. Add `@file:Suppress("UnstableApiUsage")` to the nine test-suites-DSL build scripts.
2. Add the inline `@Suppress("UnstableApiUsage")` inside `dependencyResolutionManagement { }` in the two settings
   scripts.
3. Verify via the IntelliJ inspection that each file reports zero `UnstableApiUsage` warnings.
4. Run `./gradlew spotlessApply` and `./gradlew build -x e2eTest -PskipITs`.
5. Rollback: `git checkout` the touched build scripts.

## Open Questions

None.