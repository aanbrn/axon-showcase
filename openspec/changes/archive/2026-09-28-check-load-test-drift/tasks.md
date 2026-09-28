# Tasks

## 1. The reference and the comparison

- [x] 1.1 Extract `BaselineReference` from `BaselineStats` (`load-tests/src/gatling/java/showcase/loadtests/`): the
      reference's figures (target, `recordedAt`, factor, the three floors, and each read/write request's mean/p95/p99),
      a parse from its properties text, and a render — proving the render reproduces the current file for the current
      inputs.
- [x] 1.2 Add `BaselineDrift`: compare a measured reference against a recorded one per read/write request at mean, 95th,
      and 99th percentile, yielding each figure's delta and whether it regresses beyond
      `max(floor, round(recorded x (1 + tolerance)))` (tolerance default 0.5; floors 5/10/20 ms), with a property
      override. The verdict SHALL also yield the write decision (`!regressed || refreshIntended`, true when there is
      nothing to compare, and false when the measurement recorded no figures), so the policy is testable rather than
      living in the writer.
- [x] 1.3 Add `load-tests/src/test/java/showcase/loadtests/BaselineDriftTests` and `BaselineReferenceTests` covering:
      within tolerance, beyond tolerance, an improvement (never a failure), a missing figure, a reference recording
      another target, a round-trip parse/render of a reference text, and the write decision: within tolerance writes,
      beyond tolerance withholds, a forced refresh writes, nothing to compare writes, and an empty measurement withholds
      even when refreshed.
- [x] 1.4 Wire `BaselineStats`: read the reference at the output path, print the delta report, write the reference
      unless a figure is beyond tolerance with no refresh intended (a run with nothing to compare records), withhold a
      measurement that recorded no figures even when refreshed, and exit non-zero naming the reason. Wire the refresh
      and tolerance properties through the `baselineStats` task's arguments in `load-tests/build.gradle.kts` — not the
      `loadTestProperties` whitelist, which only the simulation reads.

## 2. The measurement wrapper

- [x] 2.1 Add `REFRESH_BASELINE` and `TOLERANCE` to `scripts/load-test-baseline.sh`, passing them through as
      `-PrefreshBaseline`/`-PtolerancePercent`, and print whether the reference was written or left unchanged.
- [x] 2.2 Reorder the wrapper so the dated record is written above the `baselineStats` invocation, capturing that step's
      status instead of exiting on it, and propagate it at the end (still non-zero when the comparison failed).
- [x] 2.3 Verify with a shortened run against the local cluster
      (`CALIBRATE_DURATION=PT1M BASELINE_DURATION=PT1M ./scripts/load-test-baseline.sh`): a within-tolerance run
      rewrites the reference and writes a record; a doctored reference that trips the tolerance leaves the reference
      byte-identical, exits non-zero, and still writes the record; with `REFRESH_BASELINE=1` the doctored case is
      accepted and rewritten. Back the reference up first and restore it afterwards, and remove the transient records,
      since the shortened plateau is evidence rather than a reportable baseline.

## 3. The trend

- [x] 3.1 Add `BaselineTrend` (parse each `docs/load-tests/*.md`: date, target, knee, operating point, plateau duration,
      and the fenced summary's mean/p95/p99/throughput) and a `baselineTrend` Gradle task in
      `load-tests/build.gradle.kts` taking the records' directory as an argument, defaulting to
      `rootProject.layout.projectDirectory.dir("docs/load-tests")` (the module directory is not the repository root) and
      overridable so the test can point it at a fixture.
- [x] 3.2 Unit-test the record parsing (`BaselineTrendTests`): a generated record, an annotated one, one with no
      readable figures, an empty directory, and two readable records with different targets (the
      distinguishable-environments scenario).
- [x] 3.3 Run `./gradlew :load-tests:baselineTrend` against the committed records and read its output: the generated
      2026-09-27 record reported with its target and operating point, and the two hand-authored 2026-09-26 records
      reported as unreadable rather than silently missing.

## 4. Verification and docs

- [x] 4.1 Prove the comparison on known inputs (the wrapper's own contract is task 2.3's): the unit tests'
      beyond-tolerance case must withhold the write, and the within-tolerance and nothing-to-compare cases must write,
      and a `baselineStats` invocation over an existing run log with a doctored recorded reference must exit non-zero,
      name the figure, and leave the reference unchanged — read the command's output, do not infer it.
- [x] 4.2 Check the tolerance against reality: compare `0.5` and the 5/10/20 ms floors against the committed reference's
      per-request figures (means 4-7 ms, p95 7-10 ms, p99 10-15 ms) and the drift the idea recorded (a p95 of 17-23 ms
      against the 2026-09-26 record's 8 ms); state the outcome and adjust the default if it would flag the records' own
      noise.
- [x] 4.3 Run `./gradlew spotlessApply`, then `./gradlew spotlessCheck :load-tests:check` and
      `openspec validate --changes` — all pass.
- [x] 4.4 Remove the idea from `docs/ideas.md`, add the drift comparison, `REFRESH_BASELINE`, `TOLERANCE`, and
      `baselineTrend` to `AGENTS.md`'s load-test block, and surface the trend as a human-visible capability in
      `README.md` (or state why not).
- [x] 4.5 Record the capability's `## Purpose` refresh for the archive commit (`showcase/quality/load-tests` now also
      covers the drift comparison and the trend) — a delta cannot carry a Purpose.
