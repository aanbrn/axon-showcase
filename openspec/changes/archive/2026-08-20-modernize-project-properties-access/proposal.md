## Why

The `code-coverage-conventions` build convention reads two module-scoped flags through the map returned by
`ExtraPropertiesExtension.getProperties()` (`project.extra.properties[...]`). While that specific accessor is not
deprecated (unlike `Project.getProperties()`), it is an untyped, eager, map-style access that is harder to reason about
and less configuration-cache friendly than the direct extra-property accessors. Modernizing it removes ambiguity and
keeps our build logic aligned with Gradle's recommended way to read extra properties.

This change also records the related, separately-observed deprecation: the `io.github.build-extensions-oss.helm`
plugin (3.1.2) calls `Project.getProperties()` during root configuration, which is deprecated and becomes an error in
Gradle 10. That call lives in the third-party plugin and cannot be fixed in this repo; it is tracked upstream (see
Impact) rather than patched here.

## What Changes

- In `build-logic/src/main/kotlin/code-coverage-conventions.gradle.kts`, replace the two
  `project.extra.properties["key"]` reads with the null-safe direct accessor `project.findProperty("key")`:
  - `coverage.generatedClassExcludes` (read in `generatedClassExcludes()`)
  - `coverage.gate.enabled` (read in the `coverageGateEnabled` provider)
- Preserve exact behavior: `findProperty` returns `null` when the extra property is unset, matching the current
  `["key"]` access that returns `null` for absent keys, so the `as? List<*>` and `!= false` fallbacks behave the same.
- Note: the originally planned `project.extra.findByName("key")` does not exist — `ExtraPropertiesExtension`
  (Gradle 8.14.5) exposes only `has`/`get`/`set`/`getProperties`, and `findByName` is an `ExtensionContainer` API for
  looking up extensions. `Project.findProperty(String)` is the idiomatic null-safe direct accessor for extra properties
  and returns `null` for an unset property, exactly matching the previous map read.
- Document the upstream `io.github.build-extensions-oss.helm` `getProperties()` deprecation as a tracked risk (no
  in-repo fix; addressed by an upstream issue and a future plugin bump).

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. This is a pure build-tooling cleanup with no behavioral change; the change sets `skip_specs: true`.

## Impact

- `build-logic/src/main/kotlin/code-coverage-conventions.gradle.kts` — two `project.extra.properties[...]` reads become
  `project.findProperty(...)`.
- Upstream (not edited here): `io.github.build-extensions-oss.helm` 3.1.2 emits a `Project.getProperties()`
  deprecation warning (error in Gradle 10). Tracked via a GitHub issue against `build-extensions-oss/gradle-helm-plugin`
  and a future plugin version bump; no in-repo change is possible or appropriate for that call.