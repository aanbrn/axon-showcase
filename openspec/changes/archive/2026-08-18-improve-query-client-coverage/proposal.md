# Proposal: Improve query-client coverage

## Why

`showcase-query-client` reports 79% coverage, just below the 0.80 gate baseline, so its coverage gate is disabled. The
uncovered code is the reactive query client's request/response mapping and error paths.

## What Changes

- Add tests covering the query client's uncovered paths: error handling (timeouts, retries, failure mapping, circuit
  breaker), reactive-context metadata propagation, and the retry filter.
- Relabel the query-client test suites to match their tiers: `ShowcaseQueryClientCT` -> `ShowcaseQueryClientIT`
  (integration, `@SpringBootTest` + WireMock), `ShowcaseQueryClientIT` -> `ShowcaseQueryClientE2E` (e2e, real
  query-service container). Register the `integrationTest` suite and move test resources accordingly.
- Re-enable the module's coverage gate (remove `extra["coverage.gate.enabled"] = false`) once above 0.80.
- Verify `./gradlew :showcase-query-client:jacocoTestReport` shows >= 80% (96%) and the gate passes.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — test-only; no spec-level behavior change, so `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- New unit/integration/e2e tests under `showcase-query-client/src/`; the gate opt-out removed from
  `showcase-query-client/build.gradle.kts`.
- No application code or runtime behavior changes.
