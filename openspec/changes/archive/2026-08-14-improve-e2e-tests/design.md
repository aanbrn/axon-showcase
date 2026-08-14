## Context

See proposal.md — the E2E test (`ShowcaseApiGatewayE2E`) covers only happy paths and two negative cases, with heavy
code duplication and no resilience safeguards. The existing `showcase/api-gateway` spec already defines all the
behavior; this change is about verifying more of it end-to-end and improving test code quality.

The component test (`ShowcaseApiControllerCT`, 1420 lines) already covers validation, error mapping, circuit breakers,
and cache fallback with mocks. The E2E test's unique value is verifying the full pipeline: command → Kafka → projection
→ OpenSearch → query → gateway. New E2E scenarios should focus on what only E2E can verify — that validation errors,
state-transition conflicts, and pagination actually work across the real services.

## Goals / Non-Goals

**Goals:**

- Eliminate duplicated boilerplate via helper methods.
- Restructure into `@Nested` groups by operation with `@DisplayName`.
- Add missing negative scenarios: validation, invalid IDs, state-transition conflicts.
- Add missing pagination scenarios: `size`, `afterId`.
- Add standalone `GET /showcases/{showcaseId}` tests.
- Add `@AfterEach` cleanup safety nets and explicit Awaitility timeouts.

**Non-Goals:**

- Changing any API behavior or production code.
- Testing cache fallback, circuit breaker, or timeout/202 paths — these are component-test concerns (mocked infra
  cannot produce real transient failures reliably in E2E).
- Adding E2E tests to other modules.
- Changing the build configuration or Docker image setup.

## Decisions

### Decision 1: Helper methods as private instance methods, not a separate base class

**Choice:** Extract helpers as private methods within `ShowcaseApiGatewayE2E` itself.

**Alternatives considered:**
- Separate abstract base class (`AbstractE2E`): rejected — there is only one E2E test in the project; a base class adds
  indirection without reuse. If more E2E tests appear later, extraction can happen then.
- Static utility class: rejected — helpers need access to `webClient` instance and `await()`.

Helper methods to extract:

```
scheduleShowcase() → String showcaseId
   POST /showcases with random title/startTime/duration, assert 201, return showcaseId

scheduleShowcase(title, startTime, duration) → String showcaseId
   POST /showcases with explicit params, assert 201, return showcaseId

awaitShowcase(showcaseId, status, title, startTime, duration)
   GET /showcases/{id}, await until status matches, assert all fields

awaitShowcaseStatus(showcaseId, status)
   GET /showcases/{id}, await until status matches (lighter — for intermediate steps)

startShowcase(showcaseId)
   PUT /showcases/{id}/start, assert 200

finishShowcase(showcaseId)
   PUT /showcases/{id}/finish, assert 200

removeShowcase(showcaseId)
   DELETE /showcases/{id}, assert 200, await 404 on GET

awaitShowcaseRemoved(showcaseId)
   GET /showcases/{id}, await until 404
```

### Decision 2: `@Nested` structure by operation

**Choice:** Six nested classes: `SchedulingTests`, `StartingTests`, `FinishingTests`, `RemovingTests`,
`FetchingByIdTests`, `FetchingListTests`.

**Alternatives considered:**
- Keep flat structure: rejected — 25 tests in a flat list is hard to navigate; nested groups give clear test reports.
- Group by happy/negative: rejected — grouping by operation is more intuitive and matches the API structure.

The container setup (all `@Container` fields, `webClient`, `@BeforeEach`) stays at the top level. Nested classes
inherit the test instance lifecycle and can use the parent's `webClient` field.

### Decision 3: `@AfterEach` cleanup with tracked showcase IDs

**Choice:** Each top-level test (and each nested class) tracks created showcase IDs in a list. `@AfterEach` iterates
the list and removes any that still exist. Removal is best-effort — if `DELETE` fails or `GET` still returns 200, the
test doesn't fail in cleanup.

**Alternatives considered:**
- `@AfterAll` cleanup: rejected — too coarse; a single test failure could leave many orphans.
- Try-with-resources / AutoCloseable: rejected — Testcontainers lifecycle doesn't support this pattern for HTTP-created
  resources.
- No cleanup: rejected — orphaned showcases accumulate across test runs and pollute list-fetch tests.

### Decision 4: Explicit Awaitility timeouts at 60s

**Choice:** Configure `await().atMost(Duration.ofSeconds(60))` on all await calls, or set a default via
`Awaitility.setDefaultTimeout(Duration.ofSeconds(60))` in `@BeforeAll`.

**Alternatives considered:**
- Default 10s: rejected — too tight for Docker-based E2E on slow CI; causes flaky failures.
- 120s: rejected — excessively long; if a projection hasn't synced in 60s, something is broken.
- Per-call timeouts: rejected — inconsistent and easy to forget. A single `setDefaultTimeout` in `@BeforeAll` is
  cleaner.

### Decision 5: Remove `assertThat(showcaseIds).isEmpty()` guard in `FetchingTests.@BeforeEach`

**Choice:** Remove the assertion. The `@AfterEach` cleanup already handles orphan removal. If a previous test's
cleanup fails, the assertion causes a cascade failure that obscures the real problem.

**Alternatives considered:**
- Keep it: rejected — couples tests to execution order and amplifies failures.
- Replace with a cleanup-all-remaining-Showcases loop: rejected — would delete showcases from other test classes
  running in parallel (though `SAME_THREAD` prevents this, it's still fragile).

### Decision 6: New scenarios to add

| Nested Class | New Scenarios |
|---|---|
| `SchedulingTests` | blank title (400), too-long title (400), past start time (400), too-short duration (400), too-long duration (400) |
| `StartingTests` | finished showcase (409), already-started idempotent (200), invalid ID (400) |
| `FinishingTests` | not-started (409), already-finished idempotent (200), invalid ID (400) |
| `RemovingTests` | (existing two are sufficient) |
| `FetchingByIdTests` | existing (200), non-existing (404), invalid ID (400) |
| `FetchingListTests` | `size` parameter, `afterId` cursor pagination |

**State machine note:** the aggregate treats `start` of a `STARTED` showcase and `finish` of a `FINISHED` showcase as
idempotent no-ops (200), while `start` of a `FINISHED` showcase and `finish` of a `SCHEDULED` showcase are rejected
with `ILLEGAL_STATE` (409). The E2E scenarios above reflect this actual behavior.

Validation scenarios use parameterized tests where the pattern is identical (e.g., all schedule validation cases
assert 400 + problem detail structure).

## Risks / Trade-offs

- **[Risk] E2E test runtime increases** → Mitigation: parameterized tests share container setup (static containers,
  `@Testcontainers(parallel = true)`); the added tests are mostly fast HTTP assertions. The slow part is awaiting
  projection sync, which only applies to state-transition tests.
- **[Risk] State-transition conflict tests depend on timing** → Mitigation: use `await()` to confirm the projection
  reflects the current state before triggering the conflict (e.g., await `STARTED` before trying to start again).
- **[Risk] Validation error response format may differ from component test expectations** → Mitigation: the existing
  spec and component test define the exact problem detail format; E2E should assert the same structure (400, problem
  JSON, "Invalid request." detail, error maps).
- **[Trade-off] No E2E test for 202/timeout paths** → Accepted: these require controlling command-service processing
  time, which isn't feasible without modifying production code. Component tests cover these adequately.
