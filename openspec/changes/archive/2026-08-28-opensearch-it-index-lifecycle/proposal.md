# Deterministic OpenSearch Index Lifecycle in Integration Tests

## Why

The two OpenSearch-backed integration tests (`ShowcaseProjectorIT` and `ShowcaseQueryControllerIT`) manage the
`showcases` index differently and rely on hidden assumptions. The query IT depended on the app's startup index
initializer to have created the index before the first test, and its `@AfterEach` recreated the index so the next
test's `@BeforeEach` could assert it exists — a cross-test ordering dependency. The projection IT asserted
`createWithMapping()` returns true without first ensuring the index is absent. Both suites now share an explicit,
self-healing lifecycle: each test starts from a guaranteed-absent index, creates it with the entity's mapping, and
deletes it in teardown.

## What Changes

- `showcase-query-service/.../ShowcaseQueryControllerIT.java`:
  - `@SpringBootTest(properties = "showcase.query.index-initialization-enabled=false")` — disable the app's startup
    index initializer so the test fully owns the index lifecycle.
  - `@BeforeEach`: assert the index is absent, `createWithMapping()`, assert present.
  - `@AfterEach`: assert present, `delete()`, assert absent (drop the old delete+recreate).
- `showcase-projection-service/.../ShowcaseProjectorIT.java`:
  - `@BeforeEach`: assert the index is absent before `createWithMapping()`, assert present after.
  - `@AfterEach`: assert present before `delete()`, assert absent after.

## Capabilities

### New Capabilities

(none — test-only; no spec-level behavior change, so `skip_specs: true` is set in `.openspec.yaml`)

### Modified Capabilities

(none)

## Impact

- **Affected code**: the two integration-test classes listed above; no production code, build, or dependency changes.
- **Behavior**: every IT now starts from a fresh `showcases` index with the entity's mapping, independent of app
  startup order or prior test outcomes.
- **Tests**: verified as part of the `fix-date-nanos-precision` CI spike (the same workflow ran both suites on GitHub
  Actions).