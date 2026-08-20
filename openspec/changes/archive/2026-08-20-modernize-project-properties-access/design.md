## Context

See proposal.md - Why. `code-coverage-conventions.gradle.kts` reads two module-scoped flags via
`project.extra.properties["key"]` (the `ExtraPropertiesExtension.getProperties()` map). Although this accessor is not
deprecated, it is an untyped, eager map read. The related `Project.getProperties()` deprecation that Gradle reports is
emitted by the third-party `io.github.build-extensions-oss.helm` plugin (3.1.2) during root configuration, not by this
repo's build logic.

## Goals / Non-Goals

- **Goals**: Read the two extra properties through Gradle's null-safe direct accessors so the build logic uses the
  recommended idiom; preserve exact behavior (absent property → `null` fallback); record the upstream helm-plugin
  deprecation as a tracked risk.
- **Non-Goals**: Not patching the third-party plugin's `getProperties()` call (impossible in-repo); not introducing a
  new flag-reading abstraction; not changing any coverage-gate behavior.

## Decisions

- **Use `project.findProperty("key")` instead of `project.extra.properties["key"]`.**
  - `findProperty` returns `null` for an unset extra property, exactly like the current map access, so the existing
    fallbacks (`as? List<*>` and `!= false`) behave identically. It is the idiomatic null-safe direct accessor for
    extra properties on `Project`.
  - The originally planned `project.extra.findByName("key")` does not exist: `ExtraPropertiesExtension` (Gradle
    8.14.5) exposes only `has`/`get`/`set`/`getProperties`, while `findByName` belongs to `ExtensionContainer` and
    looks up *extensions* by name, not extra properties.
  - Alternatives rejected: `project.extra["key"]` / `project.extra.get("key")` throws
    `UnknownPropertyException`/`GradleException` on an absent key, which would change behavior for the default (unset)
    case; `extra.has(name)` + `extra.get(name)` is more verbose with no benefit.
- **Track the upstream `io.github.build-extensions-oss.helm` `getProperties()` deprecation, do not fix it here.**
  - The call lives inside the plugin (3.1.2); the only in-repo lever is the plugin version, and no fixed version
    exists yet. Action: file an upstream issue and bump the plugin when a fixed release lands. No AGENTS.md warning is
    added because the warning is already emitted by Gradle and is self-documenting.

## Risks / Trade-offs

- [The fork plugin's `getProperties()` call will become a hard error in Gradle 10] → Mitigation: tracked upstream via a
  GitHub issue; revisit on the next plugin version bump. Not an immediate blocker on Gradle 9.7.1.
- [Changing the accessor could subtly alter behavior if an extra property is set to `null` vs unset] → Mitigation:
  `findProperty` and the map access both yield `null` for absent keys; `as? List<*>` / `!= false` fallbacks are
  unchanged.

## Migration Plan

1. Replace the two `project.extra.properties["key"]` reads in `code-coverage-conventions.gradle.kts` with
   `project.findProperty("key")`.
2. Verify a full configuration (`./gradlew help --warning-mode all`) reports no new warnings and the coverage
   convention still evaluates (the existing `Project.getProperties()` warning from the helm plugin may remain, since it
   is not from this repo).
3. Re-run the `check`/coverage tasks on a module that applies `code-coverage-conventions` to confirm the gate and
   generated-class excludes are unchanged.

## Open Questions

- None.