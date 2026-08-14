## Why

Consumers reach test fixtures (e.g. `RandomCommandTestUtils`) only through hidden transitive edges — either an empty
`testFixtures` bridge in `showcase-projection-model` or `testFixturesApi` re-exporting `showcase-command-api` fixtures
through `showcase-query-api`. Three consumers also depend on `showcase-query-proto` testFixtures, which ships no fixture
sources at all. This makes the build dependency graph misleading: a reader of any consumer's `build.gradle.kts`
cannot tell where imported test utilities come from, and dead edges are impossible to notice.

## What Changes

- Remove the empty testFixtures bridge from `showcase-projection-model`: drop `testFixturesApi(testFixtures(...))` and
  `testFixturesImplementation(...)` (the module ships no testFixtures sources; the jar contains only a manifest).
- Demote `showcase-query-api`'s fixture exposure from `testFixturesApi` to `testFixturesImplementation` so command-api
  fixtures are no longer transitively re-exported to consumers.
- Add direct `testFixtures(project(":showcase-command-api"))` to `showcase-query-service` and `showcase-query-client`,
  which import `RandomCommandTestUtils` but currently reach it only transitively.
- Remove the dead `testFixtures(project(":showcase-projection-model"))` and
  `testFixtures(project(":showcase-query-proto"))` dependencies from `showcase-query-service` and
  `showcase-query-client`, and the dead `testFixtures(project(":showcase-projection-model"))` from
  `showcase-projection-service`.
- Fix `showcase-query-proto`'s test-suite dependency on `testFixtures(project(":showcase-test"))`: `RandomTestUtils`
  lives in `showcase-test`'s main source set, so the dependency becomes `implementation(project(":showcase-test"))`.
- Keep the unaffected direct declarations (`showcase-command-service`, `showcase-command-client`, `load-tests`,
  `showcase-api-gateway`, and `showcase-projection-service`'s command-api fixture) as they already target the right
  module.
- **BREAKING** (build-internal only): `showcase-query-api` testFixtures no longer expose `showcase-command-api`
  fixtures transitively; any consumer relying on that must declare the dependency directly.

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

_(none — pure build-configuration refactor; no runtime behavior changes)_

## Impact

- **Code**: `build.gradle.kts` files only — `showcase-projection-model`, `showcase-query-api`, `showcase-query-service`,
  `showcase-query-client`, `showcase-query-proto`, `showcase-projection-service`, `showcase-api-gateway`.
- **Dependencies**: testFixtures graph simplified to explicit, single-hop edges; the phantom projection-model
  test-fixtures jar is no longer produced.
- **Behavior**: unchanged at runtime; test-compilation continues to resolve every import via a direct dependency.
- **Docs**: none.
