# Process defects — retry-chart-dependency-fetch

### retry-chart-dependency-fetch#1 — Two review rounds' ledger rows were appended only after the second round

- **Recorded** — 2026-10-28, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the review stage.
- **Stage** — review.
- **Expected** — each round's row is appended when its report arrives, and both ledger checks run before the next
  reviewer invocation.
- **Observed** — round 1's report arrived; its findings were fixed and round 2 invoked without appending the row or
  running the checks; round 2's report then arrived.
- **Evidence** — `docs/review-rounds.tsv` carried no row for this unit until both were appended together after round 2;
  `--unique-rounds --expect retry-chart-dependency-fetch:1` would have failed before that.
- **Consequence** — both rows were reconstructed from the reports and appended in order; the pre-commit checks run
  before shipping, so the gap would have blocked the commit rather than reaching the repository.
- **Knowledge boundary** — observed for this unit. `docs/process-defects/scope-chart-repository-fetch.md#3` records the
  same class for the previous unit, but this session did not check whether other units repeat it.

- **Decision** — no-change: already parked — the `docs/ideas.md` 2026-10-22 "reviewer wrapper" entry, whose
  `Revisit when:` condition is not met.

### retry-chart-dependency-fetch#2 — The new Gradle task initially lacked a description

- **Recorded** — 2026-10-28, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the review stage.
- **Stage** — review: the omission was made in implementation, and the owner's check ran inside the quick-review loop.
- **Expected** — a repository-registered Gradle task carries a description of its purpose, following the existing
  practice of the build's own tasks. The `gradle-build` pack states no explicit mandatory rule for it.
- **Observed** — `fetchMainChartDependencies` was registered without a `description`; it inherited the `helm` group from
  `AbstractHelmCommandTask`. The owner asked for a check, and `help --task` printed `Description -`.
- **Evidence** — `./gradlew help --task :helm:chart:fetchMainChartDependencies` printed `Description -` before the fix;
  the task listing now shows
  `fetchMainChartDependencies - Registers the chart's repository and fetches its external dependencies, retrying each step`.
  The root build registers 26 tasks; all 26 set a `description`, and 25 also set a `group` (`jacocoRootReport` sets
  none). The local OpenCode session store, measured on 2026-10-28 rather than read from a repository artifact, places
  the check inside the review loop: round 1's quick-review invocation at `2026-10-27T23:38:00Z`, its report at
  `23:44:31Z` (nine findings, none of them this omission), the owner's request at `23:45:25Z` — "Проверь, что у все
  новых gradle-тасков есть description." — `help --task` at `23:45:25Z`, the description added by `23:46:05Z`, and round
  2's invocation at `23:48:45Z`; the manual-pass request followed at `2026-10-28T00:05:37Z`.
- **Consequence** — the description was added before the commit, so no shipped artifact carried the omission, and it was
  added before round 2 and before the manual-pass request. The check that found it was the owner's, run inside the
  review loop: round 1's report did not name it. The record first stated that the check ran after the manual-pass
  request; the session-store measurement above supersedes that sentence.
- **Knowledge boundary** — one case observed; the group was inherited, so its absence is not part of this record, and
  why round 1 did not report the omission is not established.

- **Decision** — no-change: one occurrence; the owner's check inside the review loop is material but not severe, and
  corpus uniformity is not a second occurrence. The record is corrected to place the check between quick-review rounds 1
  and 2, and the owner keeps the case under observation for a second one.
