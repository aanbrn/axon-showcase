## Why

The `showcase-command-client` and `showcase-query-client` are behavior-carrying libraries — they own conditional retry
filters, circuit breaker/time-limiter wiring, error translation, and auto-configuration — but their behavior is only
pinned by tests, never captured in an OpenSpec spec. With the spec tree now grouped by architectural role, the "clients"
concern has no home, so these two resilient-service-consumer patterns are undocumented and changes to them are not
reviewable against a stated contract.

## What Changes

- Add two new capability specs under the `clients` group:
    - `showcase/clients/command-client`: the reactive Axon command gateway wrapper — the four
      schedule/start/finish/remove operations, business-error translation, and the resilience configuration (retry
      filter, circuit breaker ignoring business errors).
    - `showcase/clients/query-client`: the protobuf WebClient query consumer — list/by-ID fetch operations,
      problem-detail error translation, validated properties, and the resilience configuration (retry filter over
      retryable HTTP statuses, time limiter, circuit breaker ignoring business errors).
- Both specs document current behavior derived from the implementations and their component/integration tests. No
  behavioral or code changes.

## Capabilities

### New Capabilities

- `showcase/clients/command-client`: The command client's contract — dispatch of the four showcase commands through the
  reactor command gateway, mapping of `CommandExecutionException`s carrying showcase error details to
  `ShowcaseCommandException`, and the resilience4j wiring: retry only for retryable (non-business, non-transient)
  errors, circuit breaker ignoring business errors.
- `showcase/clients/query-client`: The query client's contract — protobuf query dispatch to the query service's
  `/streaming-query` and `/query` endpoints, mapping of problem-detail responses to `ShowcaseQueryException` with
  `INVALID_QUERY`/`NOT_FOUND` codes, validated `apiUrl` configuration, and the resilience4j wiring: retry for a fixed
  set of retryable HTTP status codes plus timeouts and request failures, time limiter, circuit breaker ignoring business
  errors.

### Modified Capabilities

- None.

## Impact

- New specs only:
  `openspec/changes/capture-client-behavior/specs/showcase/clients/{command-client,query-client}/spec.md`.
- No production code, APIs, dependencies, or build configuration are affected.
