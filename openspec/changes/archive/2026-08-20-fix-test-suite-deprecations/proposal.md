## Why

The Gradle 9.7.1 upgrade surfaced deprecation warnings (incompatible with Gradle 10) in eight module build files. The
Kotlin DSL test-suite delegate idioms `val test by getting(JvmTestSuite::class)` and
`val suite by register<JvmTestSuite>(...)` rely on `provideDelegate`/`getValue` extensions that Gradle 9.6 deprecated in
favor of the container's `getByName`/`register` APIs. The build still succeeds, but these warnings will become errors on
Gradle 10.

## What Changes

- Replace `val test by getting(JvmTestSuite::class)` with `val test = suites.getByName<JvmTestSuite>("test")` in each
  module build file that declares a `test` suite.
- Replace `val suite by register<JvmTestSuite>("...") { ... }` with `val suite = suites.register<JvmTestSuite>("...")
  { ... }` in each module build file, keeping the suites bound as `val`s so they stay referenceable in
  `shouldRunAfter` / `mustRunAfter`.
- Update the usage sites accordingly: `shouldRunAfter(test)` / `shouldRunAfter(suite)` now receive a `JvmTestSuite`
  or a `NamedDomainObjectProvider<JvmTestSuite>` instead of the auto-unwrapped delegate value; both are accepted by
  the task-ordering APIs, so no behavioral change.
- No change to suite behavior, ordering, dependencies, or test tier placement.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. This is a pure build-tooling refactor that removes deprecated Gradle API usage without changing any
  observable behavior. The change therefore sets `skip_specs: true`.

## Impact

- The eight module build files that declare `JvmTestSuite` delegates: `showcase-command-service`,
  `showcase-command-client`, `showcase-query-service`, `showcase-query-client`, `showcase-query-proto`,
  `showcase-projection-service`, `showcase-projection-model`, `showcase-api-gateway`.
- `AGENTS.md` — the convention note "A suite can be referenced in `shouldRunAfter(...)` only when bound as a val
  (e.g. `val integrationTest by register<JvmTestSuite>(\"integrationTest\")`)" references the deprecated idiom and
  must be updated to the non-deprecated form.
- No runtime, dependency, or application code changes.