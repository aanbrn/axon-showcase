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

### retry-chart-dependency-fetch#2 — The new Gradle task shipped without a description

- **Recorded** — 2026-10-28, by the writing session (`ses_e832e4043ffe6ADYTEa13DvV1s`), during the review stage.
- **Stage** — implementation.
- **Expected** — a repository-registered Gradle task carries a description of its purpose, following the existing
  practice of the build's own tasks. The `gradle-build` pack states no explicit mandatory rule for it.
- **Observed** — `fetchMainChartDependencies` was registered without a `description`; it inherited the `helm` group from
  `AbstractHelmCommandTask`. The owner asked for a check, and `help --task` printed `Description -`.
- **Evidence** — `./gradlew help --task :helm:chart:fetchMainChartDependencies` printed `Description -` before the fix;
  the task listing now shows
  `fetchMainChartDependencies - Registers the chart's repository and fetches its external dependencies, retrying each step`.
  The root build registers 26 tasks; all 26 set a `description`, and 25 also set a `group` (`jacocoRootReport` sets
  none).
- **Consequence** — the description was added before the commit; no shipped artifact carried the omission. The check
  that found it was the owner's, run after the manual-pass request.
- **Knowledge boundary** — one case observed; the group was inherited, so its absence is not part of this record, and
  why the omission was not noticed earlier is not established.
