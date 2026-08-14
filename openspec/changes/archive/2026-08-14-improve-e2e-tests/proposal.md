## Why

The E2E test (`ShowcaseApiGatewayE2E`) is the only test that exercises the full command → Kafka → projection → query
pipeline end-to-end, yet it covers only happy paths and two negative cases. The test code is heavily duplicated
(scheduling boilerplate repeated 7 times), lacks cleanup safety nets, has no explicit Awaitility timeouts, and misses
entire categories of scenarios that the component test covers only with mocks — validation errors, state-transition
conflicts, invalid IDs, and pagination.

## What Changes

- Extract helper methods for common operations: schedule a showcase, await projection sync, assert showcase state,
  remove and verify removal — eliminating ~200 lines of duplicated boilerplate.
- Restructure the test class into `@Nested` groups by operation: `SchedulingTests`, `StartingTests`, `FinishingTests`,
  `RemovingTests`, `FetchingByIdTests`, `FetchingListTests`.
- Add `@DisplayName` annotations on all test classes and methods for readable test reports.
- Add missing negative scenarios: validation errors (blank title, too-long title, past start time, out-of-range
  duration), invalid KSUID on all endpoints, state-transition conflicts (start already-started, finish not-started,
  finish already-finished).
- Add missing pagination scenarios: `size` parameter, `afterId` cursor, default size behavior.
- Add standalone `GET /showcases/{showcaseId}` tests (existing, non-existing, invalid ID).
- Add `@AfterEach` cleanup safety nets to top-level tests so orphaned showcases are removed even on test failure.
- Configure explicit Awaitility timeouts (at least 60s) tuned for Docker-based E2E on slow CI.
- Remove the `assertThat(showcaseIds).isEmpty()` guard in `FetchingTests.@BeforeEach` that couples tests to execution
  order and causes cascade failures.

## Capabilities

### New Capabilities

_(none — this change improves test coverage and quality only; no API behavior changes.)_

### Modified Capabilities

_(none — existing `showcase/api-gateway` spec requirements are unchanged; the E2E test merely verifies more of them.)_

## Impact

- **Files**: `showcase-api-gateway/src/e2eTest/java/showcase/api/ShowcaseApiGatewayE2E.java` (full rewrite).
- **Build**: no new dependencies; e2eTest task configuration unchanged.
- **Tests**: E2E test count roughly doubles (from 10 to ~25). E2E test runtime increases proportionally but remains
  within the existing Docker-based E2E timeout envelope.
- **Deployment**: no impact.
