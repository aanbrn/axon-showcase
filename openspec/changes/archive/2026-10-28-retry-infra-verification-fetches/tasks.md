# Tasks

Workspace: unit=retry-infra-verification-fetches; path=/Users/afanasyev/Development/axon-showcase;
branch=retry-infra-verification-fetches; writer=ses_e832e4043ffe6ADYTEa13DvV1s

## 1. Implementation

- [x] 1.1 Wrap the `execHelmCaptureOutput("show", "values")` call in `VerifyInfraImageVersionsTask.verify()` inside
      `BoundedRetry.execute`, logging a warning per retry and letting the last failure propagate. Verify:
      `./gradlew -p build-logic build` passes.
- [x] 1.2 Remove the 2026-10-28 `docs/ideas.md` entry parking this retry (the change implements it). Verify: grep the
      file for the entry text.
- [x] 1.3 Extend the `deployment-operations` pack's install-path retry note: `verifyInfraImageVersions` also retries
      each chart's values fetch. Verify: grep the paragraph for the values-fetch sentence.

## 2. Verification

- [x] 2.1 Scratch fixture with a fake `helm` (init script overriding the real task's `executable` and `repos`) that
      fails the first `show values` call and then emits a values document matching each check's chart. Verify: the task
      retries, logs the warning, and completes; the fixture's call log shows two values calls for the failed chart.
- [x] 2.2 The same fixture failing every values call. Verify: exactly three attempts for the first chart, the last
      failure propagates, no later chart is fetched, and the task exits non-zero.
- [x] 2.3 The real task on the repository's checks: `./gradlew :verifyInfraImageVersions`. Verify: it resolves the tags
      and passes.
- [x] 2.4 Configuration-cache store and reuse over the task. Verify: the second run reuses the entry, and a cache hit
      performs no values fetch.
- [x] 2.5 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `./gradlew spotlessCheck`. Verify: both green.

## 3. Review gate

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`; re-derive it after the last edit
      to the unit's own record. Verify: every row's source reproduces.
- [x] 3.2 Run the quick-review loop to a clean round, appending the ledger rows when each report arrives and running the
      ledger checks. Verify: the closing round reports no observations and `--expect-clean` passes.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py --change retry-infra-verification-fetches`,
      decide at or above the floor, run the pass if it is `REQUIRED`, and record the outcome and its signals on this
      task. Verify: the outcome is recorded. Routing: `NOT_NEEDED`; no signal matched and no deep pass ran.
- [x] 3.4 Write `docs/process-defects/retry-infra-verification-fetches.md` as defects are observed, or state that none
      was. Verify: the file exists before the manual-pass request.
- [x] 3.5 Request the owner's manual review pass and stop. Verify: the request names the review surface; nothing is
      committed, pushed, or archived before the owner's approval.
