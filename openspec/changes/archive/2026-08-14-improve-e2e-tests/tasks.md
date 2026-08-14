## 1. Helper Methods & Infrastructure

- [x] 1.1 Add `@BeforeAll` static method to set `Awaitility.setDefaultTimeout(Duration.ofSeconds(60))`
- [x] 1.2 Add a `List<String> createdShowcaseIds` field and `@AfterEach` cleanup method that best-effort removes any
      remaining showcases (DELETE + ignore failures)
- [x] 1.3 Extract `scheduleShowcase()` → returns `String showcaseId`; generates random title/startTime/duration, POSTs,
      asserts 201 + Location header + JSON content type + cache-control header, returns `showcaseId`, adds to
      `createdShowcaseIds`
- [x] 1.4 Extract `scheduleShowcase(String title, Instant startTime, Duration duration)` → returns `String showcaseId`;
      POSTs with explicit params, asserts 201, returns `showcaseId`, adds to `createdShowcaseIds`
- [x] 1.5 Extract `awaitShowcase(String showcaseId, ShowcaseStatus status, String title, Instant startTime, Duration
      duration)` → awaits GET returns 200, asserts all fields including status and timestamp presence/absence
- [x] 1.6 Extract `awaitShowcaseStatus(String showcaseId, ShowcaseStatus status)` → lighter await, just polls GET
      until status matches
- [x] 1.7 Extract `startShowcase(String showcaseId)` → PUT .../start, assert 200
- [x] 1.8 Extract `finishShowcase(String showcaseId)` → PUT .../finish, assert 200
- [x] 1.9 Extract `removeShowcase(String showcaseId)` → DELETE, assert 200, await GET returns 404, remove from
      `createdShowcaseIds`
- [x] 1.10 Extract `awaitShowcaseRemoved(String showcaseId)` → await GET returns 404

## 2. Restructure Into Nested Classes

- [x] 2.1 Move container setup, `webClient`, `@BeforeEach`, `@AfterEach`, and all helpers to the top-level class
- [x] 2.2 Create `@Nested @DisplayName("Scheduling") class SchedulingTests` with `@AfterEach` cleanup
- [x] 2.3 Create `@Nested @DisplayName("Starting") class StartingTests` with `@AfterEach` cleanup
- [x] 2.4 Create `@Nested @DisplayName("Finishing") class FinishingTests` with `@AfterEach` cleanup
- [x] 2.5 Create `@Nested @DisplayName("Removing") class RemovingTests` with `@AfterEach` cleanup
- [x] 2.6 Create `@Nested @DisplayName("Fetching by ID") class FetchingByIdTests`
- [x] 2.7 Create `@Nested @DisplayName("Fetching list") class FetchingListTests` with its own `@BeforeEach`/`@AfterEach`
      for seeding showcases (reuse existing pattern, remove the `assertThat(showcaseIds).isEmpty()` guard)
- [x] 2.8 Add `@DisplayName` on the top-level class and on every test method

## 3. Scheduling Tests

- [x] 3.1 Migrate `scheduleShowcase_validRequest_exposesScheduledShowcase` → use helpers, assert 201 + Location +
      content type + cache-control, await projection shows SCHEDULED with all fields
- [x] 3.2 Migrate `scheduleShowcase_alreadyUsedTitle_failsWithTitleInUseProblem` → schedule one, then POST same title,
      assert 409 + problem detail (type, title, status, detail, instance)
- [x] 3.3 Add `scheduleShowcase_blankTitle_failsWithValidationProblem` → POST with blank title, assert 400 + problem
      detail with `bodyErrors` containing `title`
- [x] 3.4 Add `scheduleShowcase_tooLongTitle_failsWithValidationProblem` → POST with title > 255 chars, assert 400 +
      `bodyErrors` with `title`
- [x] 3.5 Add `scheduleShowcase_pastStartTime_failsWithValidationProblem` → POST with past start time, assert 400 +
      `bodyErrors` with `startTime`
- [x] 3.6 Add `scheduleShowcase_tooShortDuration_failsWithValidationProblem` → POST with duration < 1 min, assert 400
      + `bodyErrors` with `duration`
- [x] 3.7 Add `scheduleShowcase_tooLongDuration_failsWithValidationProblem` → POST with duration > 10 min, assert 400
      + `bodyErrors` with `duration`

## 4. Starting Tests

- [x] 4.1 Migrate `startShowcase_existingShowcase_exposesStartedShowcase` → use helpers, schedule, start, await
      STARTED with all fields
- [x] 4.2 Migrate `startShowcase_nonExistingShowcase_failsWithNotFoundProblem` → PUT start with random ID, assert 404
      + problem detail
- [x] 4.3 Add `startShowcase_finishedShowcase_failsWithConflictProblem` → schedule, start, finish, await FINISHED,
      start again, assert 409 + problem detail "Showcase is finished already"
- [x] 4.4 Add `startShowcase_alreadyStarted_doesNotFail` → schedule, start, await STARTED, start again, assert 200 and
      unchanged `startedAt` (idempotent retry semantics)
- [x] 4.5 Add `startShowcase_invalidShowcaseId_failsWithValidationProblem` → PUT start with non-KSUID, assert 400 +
      problem detail with `pathErrors` containing `showcaseId`

## 5. Finishing Tests

- [x] 5.1 Migrate `finishShowcase_existingShowcase_exposesFinishedShowcase` → use helpers, schedule, start, finish,
      await FINISHED with all fields
- [x] 5.2 Migrate `finishShowcase_nonExistingShowcase_failsWithNotFoundProblem` → PUT finish with random ID, assert
      404 + problem detail
- [x] 5.3 Add `finishShowcase_notStarted_failsWithConflictProblem` → schedule, finish without starting, assert 409 +
      problem detail "Showcase must be started first"
- [x] 5.4 Add `finishShowcase_alreadyFinished_doesNotFail` → schedule, start, finish, await FINISHED, finish again,
      assert 200 and unchanged `finishedAt` (idempotent retry semantics)
- [x] 5.5 Add `finishShowcase_invalidShowcaseId_failsWithValidationProblem` → PUT finish with non-KSUID, assert 400 +
      problem detail with `pathErrors` containing `showcaseId`

## 6. Removing Tests

- [x] 6.1 Migrate `removeShowcase_existingShowcase_doesNotExposeRemovedShowcase` → use helpers, schedule, await
      SCHEDULED, delete, assert 200 + cache-control, await 404
- [x] 6.2 Migrate `removeShowcase_nonExistingShowcase_doesNotFail` → GET random ID (404), DELETE same ID, assert 200

## 7. Fetching by ID Tests

- [x] 7.1 Add `fetchById_existingShowcase_returnsShowcase` → schedule, await SCHEDULED, GET by ID, assert 200 + all
      fields + cache-control header
- [x] 7.2 Add `fetchById_nonExistingShowcase_returnsNotFound` → GET random ID, assert 404 + problem detail
- [x] 7.3 Add `fetchById_invalidShowcaseId_returnsBadRequest` → GET with non-KSUID, assert 400 + problem detail with
      `pathErrors` containing `showcaseId`

## 8. Fetching List Tests

- [x] 8.1 Migrate `fetchList_noFiltering_exposesExistingShowcases` → use the existing `@BeforeEach` seeding pattern,
      assert all seeded IDs present + cache-control header
- [x] 8.2 Migrate `fetchList_titleToFilterBy_exposesFilteredShowcases` → fetch all, pick one, filter by title, assert
      single result
- [x] 8.3 Migrate `fetchList_singleStatusToFilterBy_exposesFilteredShowcases` → filter by one status, assert all
      match + non-empty
- [x] 8.4 Migrate `fetchList_multipleStatusesToFilterBy_exposesFilteredShowcases` → filter by two statuses, assert all
      match either + non-empty
- [x] 8.5 Add `fetchList_withSizeParameter_returnsLimitedResults` → seed showcases, GET with `?size=1`, assert exactly
      1 result
- [x] 8.6 Add `fetchList_withAfterIdCursor_returnsPaginatedResults` → seed showcases, fetch first page with `?size=1`,
      use last ID as `afterId` for second page, assert different result

## 9. Verification

- [x] 9.1 Run `./gradlew :showcase-api-gateway:compileE2eTestJava` to verify compilation
- [x] 9.2 Run `./gradlew :showcase-api-gateway:e2eTest` to verify all tests pass (requires Docker + pre-built images)
- [x] 9.3 Run `./gradlew :showcase-api-gateway:check` to verify no regressions in other test tiers

## Implementation Notes

- Tasks 3.3-3.7 were implemented as a single `@ParameterizedTest` (`scheduleShowcase_invalidRequest_failsWithValidationProblem`)
  with five `argumentSet` cases (blank title, too-long title, past start time, too-short duration, too-long duration) —
  identical assertion shape, consolidated for readability.
- Aggregate behavior discovered during implementation: starting an already-started showcase and finishing an
  already-finished showcase are **idempotent no-ops (200)**, not conflicts. Tasks 4.3/5.4 were corrected accordingly,
  and idempotency is asserted by verifying `startedAt`/`finishedAt` remain unchanged.
- `FetchingListTests.@BeforeEach` requires a final sync point that awaits all seeded showcases visible in the list query
  before running list tests — the by-ID get is real-time but the list search is subject to the OpenSearch refresh
  interval. Without it, status-filtered list tests can flake with an empty result.
- Cleanup is centralized in the outer class `@AfterEach` (shared `createdShowcaseIds`), which JUnit 5 runs after nested
  test methods too; the per-test best-effort DELETE removes orphaned showcases even on test failure.
- E2E result: 28 tests, all passing. Full `:showcase-api-gateway:check` green (test + componentTest + e2eTest + spotbugs
  + errorprone).
