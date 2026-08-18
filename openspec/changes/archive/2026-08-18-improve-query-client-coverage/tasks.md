## 1. Error-handling tests

- [x] 1.1 Integration tests for the `handleError` branches: bad-request with field errors -> `INVALID_QUERY`,
      bad-request without field errors -> response exception, not-found without detail -> response exception,
      problem-json unmapped status -> response exception

## 2. Additional client behavior tests

- [x] 2.1 Reactive-context metadata propagates into the query request
- [x] 2.2 Circuit-breaker profile scenario: repeated failures open the circuit, subsequent calls fail fast
- [x] 2.3 `ShowcaseQueryRetryFilterTests`: retryable/non-retryable status codes, timeouts, request failures, unrelated
      exceptions

## 3. Tier relabels

- [x] 3.1 `ShowcaseQueryClientCT` -> `ShowcaseQueryClientIT` (integration), moved with its `application*.yml` resources
- [x] 3.2 former `ShowcaseQueryClientIT` -> `ShowcaseQueryClientE2E` (e2e)
- [x] 3.3 Register the `integrationTest` suite (deps for spring/axon/opensearch/wiremock/resilience4j), drop
      component-test resources, order suites `test` -> `componentTest` -> `integrationTest` -> `e2eTest`

## 4. Gate and verification

- [x] 4.1 Re-enable the coverage gate (remove `extra["coverage.gate.enabled"] = false`)
- [x] 4.2 `jacocoTestReport` shows >= 80% (96%) and the gate passes
