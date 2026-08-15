## Why

`showcase-test` — a shared test-utilities module consumed by nearly every other module's test suites — has no tests of its
own. Its two classes (`RandomTestUtils`, `KafkaTestPublisher`) carry real logic (random-element selection, per-aggregate
sequence tracking, domain-message construction) that other modules depend on but never directly verify. A regression in
either class would surface only as a confusing downstream failure.

## What Changes

- Add `RandomTestUtilsTests` unit tests covering: `anAlphabeticString` length/content, `anElementOf` for array and list
  (membership, empty-input `IllegalArgumentException`, single-element determinism), and `anEnum` (valid constant,
  empty-enum rejection).
- Add `KafkaTestPublisherTests` unit tests with a mocked `KafkaPublisher`, covering: domain-message construction
  (aggregate type, extracted identifier, sequence number 0), per-aggregate sequence increments, `publishEventTwice`,
  `publishEvents`, and `NullPointerException` on a null extracted identifier.
- Add `testImplementation(libs.mockito.core)` to `showcase-test/build.gradle.kts` (matching
  `showcase-resilience4j-extension`); the Mockito agent is already wired globally in `java-conventions`.
- No production code changes and no behavior changes — tests only.

## Capabilities

### New Capabilities

None. This is a testing-only change; no new capability is introduced.

### Modified Capabilities

None. No requirement behavior changes.

This change sets `skip_specs: true` in `.openspec.yaml` because it adds tests without any spec-level behavior delta.

## Impact

- **Code**: `showcase-test/src/test/java/` (two new test classes) and `showcase-test/build.gradle.kts` (one dependency).
- **Build/tests**: `:showcase-test:test` gains the new unit tests; no effect on other modules' builds.
- **APIs/dependencies**: no production API or dependency changes.