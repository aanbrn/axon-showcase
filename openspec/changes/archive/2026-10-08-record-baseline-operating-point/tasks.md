# Tasks

## 1. Record and gate on the operating point

- [x] 1.1 In `load-tests/src/main/java/showcase/loadtests/BaselineReference.java`, add a nullable `operatingPoint` field
      (workload units per second) with its Javadoc, parse the `operatingPoint` key, render it between `recordedAt` and
      `factor` when present, and name the operating point in the class Javadoc and in `HEADER`; update
      `BaselineReferenceTests`' `REFERENCE` fixture and its parse/render round-trip assertions. Verify with
      `./gradlew :load-tests:test --tests showcase.loadtests.BaselineReferenceTests`.
- [x] 1.2 In `load-tests/src/main/java/showcase/loadtests/BaselineDrift.java`, make `compare` require the recorded
      reference's target to match and the measured operating point to be within a tenth of the recorded one (both
      present), and report the mismatch reasons distinctly (an operating point beyond the band; none recorded); update
      the class and `compare` Javadoc, and give `BaselineDriftTests`' `reference(...)` helper an operating point. Verify
      with `./gradlew :load-tests:test --tests showcase.loadtests.BaselineDriftTests`.
- [x] 1.3 Prove the gate both ways in `BaselineDriftTests` — a slightly different operating point within the band (e.g.
      124 vs 127) still compares and can regress (known-good), while one beyond the band (e.g. 124 vs 300) and one with
      no recorded operating point report no regression, name the reason, and write the measurement (known-bad) — and
      read the test output to confirm every case ran.
- [x] 1.4 In `load-tests/src/gatling/java/showcase/loadtests/BaselineStats.java`, accept the measured operating point as
      the task's last argument and record it in `measure`'s built reference; update the class Javadoc and `main`'s
      `@param args`. In `load-tests/build.gradle.kts`, forward it to the `baselineStats` task via
      `providers.gradleProperty("operatingPoint")` (the property name is not `rate`, which the simulation already
      forwards). Verify with `./gradlew :load-tests:compileGatlingJava` and by reading the task's `argumentProviders`
      list.

## 2. Wire the recording pipeline

- [x] 2.1 In `scripts/load-test-baseline.sh`, pass `-PoperatingPoint="$OPERATING"` on the `baselineStats` invocation
      (the value it already reads from `knee.properties`). Verify with `bash -n scripts/load-test-baseline.sh` and by
      reading the invocation.
- [x] 2.2 In `load-tests/src/gatling/resources/baseline.properties`, add `operatingPoint=127` (the value from
      `docs/load-tests/2026-10-08.md`, the record matching the file's `recordedAt=2026-10-08T03:10:49.456839Z`) and
      reword its first three lines to match the revised `BaselineReference.HEADER`. The gatling source set's resources
      are not on the unit suite's runtime classpath, so verify by reading the file: `sed -n '1,8p'` shows the new header
      and the line, and `git diff -- load-tests/src/gatling/resources/baseline.properties` changes only those header
      lines and that one line.

## 3. Documentation

- [x] 3.1 Update the `AGENTS.md` Build & Test load-test note (the `./scripts/load-test-baseline.sh` paragraph): the run
      compares against "the reference recorded for the target and a matching operating point", and the reference records
      the operating point. Verify with `./gradlew spotlessApply` then `grep -n "more than a tenth" AGENTS.md` (the
      phrase spans a comment line break, so a token, not the whole phrase, is the check).
- [x] 3.2 Update the README load-test section (the paragraph that describes the drift comparison) to name the operating
      point the reference records and the matching-band comparison. Verify with `./gradlew spotlessApply` and a grep.
- [x] 3.3 Remove the 2026-10-08 operating-point idea from `docs/ideas.md` (it rides this change's branch); verify the
      entry is gone and the file's other sections are intact.

## 4. Verification

- [x] 4.1 Run `./gradlew :load-tests:check` (the module's unit suite; it has no integration tests) and confirm it passes
      with the new operating-point cases.
- [x] 4.2 Run `openspec validate --all` and confirm the delta validates — the `MODIFIED` requirement's header matches
      the main spec and every existing scenario is carried.
- [x] 4.3 Run `./gradlew spotlessApply` after the last edit, then `git status` and `git diff` to confirm the committed
      reference, the docs, and the `docs/ideas.md` removal are the intended set.

## 5. Lesson capture

- [x] 5.1 Run the `lesson-capture` subagent over the implementation diff, the quick-review findings, and this change
      dir; apply the durable proposals to `AGENTS.md` (or record why each is rejected) and record the applied net
      `AGENTS.md` delta on this task. Verify by re-running `./gradlew spotlessApply` and reading the applied delta.
      Applied: one merge into the auto-review bullet — "single-source a predicate the requirement owns" — net **+4**
      lines, no retirement (the load-tests bullets it touches remain accurate). It also surfaced a live defect, now
      fixed: the README had re-anchored the band to the measured rate. `spotlessCheck` and `verifyCapturedMarkers` pass.

## Workflow follow-up

- Run the change's quick review, then request the manual review pass; commit, push, and open one PR only after approval.
- Archive the change (move the change dir, sync the main spec) as an additional commit in the same PR after CI is green.
