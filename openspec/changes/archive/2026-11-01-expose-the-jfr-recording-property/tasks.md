# Tasks

Workspace: unit=expose-the-jfr-recording-property; path=/Users/afanasyev/Development/axon-showcase;
branch=expose-the-jfr-recording-property; writer=ses_e6bb3d98effeNURMqTdoGwAu9f

## 1. The hook

- [x] 1.1 `showcase-api-gateway/build.gradle.kts`: add `-Dshowcase.diagnostics.jfr=true` to the component-test suite's
      `jvmArgs` unless `-Pdiagnostics.jfr` is the `false` value, read through `providers` so the configuration cache
      records it as an input — the shape `skipITs` already uses.
- [x] 1.2 `JfrSummary` and `DiagnosticsControlCT`: the failure-time hint and the class Javadoc name
      `-Pdiagnostics.jfr=true` (the route a reader can use, which the build forwards), and the control's disabled-render
      assertion matches the new text.

## 2. Verification

- [x] 2.1 Three states, each green, each identified by which property-gated control skips, measured on
      `--tests 'showcase.api.diagnostics.*'`: with no property the JFR control skips, so the recording is off and the
      default unchanged; with `-Pdiagnostics.jfr=true` the disabled-render control skips, so the property reached the
      test JVM; with `-Pdiagnostics.jfr=false` the JFR control skips, so the value-aware read leaves it off as `skipITs`
      does. The suite is 135 declared tests, 0 failures, one skipped in each state.
- [x] 2.2 `./gradlew spotlessApply` after the last edit and `spotlessCheck`; `openspec validate --all`; the manual
      120-character check over the changed set.
- [x] 2.3 Review gate: the `review-quick` rounds and their findings are this unit's rows in `docs/review-rounds.tsv`.
      Deep-review routing is `NOT_NEEDED` (`scripts/review-routing.py`), so no deep review runs.
- [x] 2.4 Ask the owner for the manual review pass.

## 3. The change's own records

- [x] 3.1 Record the pre-write derivation pass as a task. It ran: the claims table is in `design.md`, and its rows come
      from the three-run measurement, the recording's own gate, and the two runs that show the property reaching the
      JVM.
- [x] 3.2 Write `docs/process-defects/expose-the-jfr-recording-property.md` — it records that no process defect was
      observed, and why the first measurement was re-run in isolation.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
