# Design

## Context

See `proposal.md` for motivation and `specs/showcase/quality/infra-image-versions/spec.md` for the changed requirement.

`VerifyInfraImageVersionsTask` is a `@CacheableTask`: its inputs are the configured infra checks, the chart repositories
and the infra values files; its output is a result file. Its action calls `addHelmRepositories(repos)` — which already
retries each repository's add and update through `BoundedRetry` — then, for every check, runs
`execHelmCaptureOutput("show", "values")` once and compares the chart's preconfigured tag with the component's official
tag. That per-chart values fetch was the only read in this task's action without a retry.

## Goals / Non-Goals

**Goals:**

- A transient failure of a chart's values fetch that clears within the retry budget is retried and does not fail
  `check`.
- A parse or drift failure, and a fetch that stays failing, still fail the build.
- The task's cache key and a cache hit's behaviour are unchanged.

**Non-Goals:**

- Classifying failures by message, or changing the attempt count through a new input.
- Retrying the parse or comparison steps, which are local and deterministic.
- The install path's or the packaging path's fetches, already covered or parked separately.

## Decisions

### Each values fetch runs inside the shared `BoundedRetry`

The action wraps the `execHelmCaptureOutput("show", "values")` call in `BoundedRetry.execute` with the helper's default
attempts, logging a warning per retry and letting the last failure propagate. The helper is the repository's existing
model for a transient fetch (`addHelmRepositories` uses it), and `BoundedRetryTests` covers the attempt count, the pause
parameter and the last-failure propagation.

Rejected: a new `attempts` input (no production need; the fixture proves the wiring by the call counts); a CI-level
retry of the whole check (toleration-shaped); retrying the local parse/comparison (deterministic, not a fetch).

### The task stays cacheable with the same inputs

The retry is action-internal: no property is added, so the cache key is unchanged. A cache hit restores the result and
performs no fetch, so it never enters the retry; a changed coordinate re-runs the action and gains the retry.

### A persistent values failure still fails the build

`BoundedRetry` rethrows the last failure after its attempts, so the task fails with the Helm error, exactly as before
apart from the retry budget.

## Risks / Trade-offs

- **A failing values fetch adds up to two pauses per check** → the budget is three attempts with a two-second pause, so
  three failing checks add up to six pauses; the failure is still reported, and the success path is unchanged.
- **A transient outage outlasting the budget still fails `check`** → the budget is about four seconds; the previous
  unit's `fetchMainChartDependencies` failed the gate transiently despite the same budget and passed alone minutes
  later, so the CI re-run remains the remedy for a longer outage.
- **No classification of failure kinds** → a persistent error is retried twice needlessly; the helper's design accepts
  that, and a misconfiguration still fails.
- **The fixture is one-time evidence, not a regression test** (`build-logic` has no Gradle TestKit) →
  `BoundedRetryTests` covers the retry decision, and the fixture's call log evidences the wiring.

## Verification plan

| Check                                                              | What it establishes                                                          |
| ------------------------------------------------------------------ | ---------------------------------------------------------------------------- |
| Scratch fixture with a fake Helm that fails the first values fetch | The fetch is retried, a warning is logged, and the verification completes    |
| The same fixture failing every values fetch                        | Exactly three attempts, the last failure propagates, and no later check runs |
| The real task on the repository's checks                           | The gate still resolves the tags and passes                                  |
| Configuration-cache store and reuse over `check`                   | The task keeps its cache behaviour and a hit performs no fetch               |
| `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`          | The change's own gate                                                        |

## Claims table

| Claim                                                                                     | Source                                                                                                                               | Result   |
| ----------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ | -------- |
| Before this change, the task's action ran `helm show values` once per check               | `git show 749e4ec2:build-logic/src/main/kotlin/VerifyInfraImageVersionsTask.kt`                                                      | verified |
| Before this change, the repository add and update retried and the values fetch did not    | `git show 749e4ec2:build-logic/src/main/kotlin/VerifyInfraImageVersionsTask.kt` (the `addHelmRepositories` call and the action loop) | verified |
| The current requirement enumerates only the repository add and update                     | `openspec/specs/showcase/quality/infra-image-versions/spec.md`                                                                       | verified |
| `BoundedRetry` makes three attempts with a two-second pause and rethrows the last failure | `build-logic/src/main/kotlin/BoundedRetry.kt`                                                                                        | verified |
| The task is cacheable on the checks, repositories and values files                        | `VerifyInfraImageVersionsTask.kt` annotations                                                                                        | verified |
| A transient values failure can fail the gate                                              | `git show 749e4ec2:docs/ideas.md` (the 2026-10-28 entry this change removes)                                                         | verified |
