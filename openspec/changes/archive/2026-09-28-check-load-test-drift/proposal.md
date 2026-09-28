# Proposal

## Why

Nothing compares one load-test run to another. The profiles gate only a catastrophic change —
`max(floor, factor x baseline)` with a fivefold factor — the dated `docs/load-tests/` records are written but no tool
reads them, and a baseline run overwrites the reference unconditionally, so a slower-but-passing plateau silently
becomes the new normal. The drift the records already hold (a p95 of 17-23 ms at a comparable load against the 8 ms the
2026-09-26 record measured) has no reader.

## What Changes

- A baseline run compares its plateau's per read and write request mean, 95th, and 99th percentile response times
  against the reference recorded for the target it measured, and reports each figure alongside the recorded value and
  their delta.
- A figure beyond the configured tolerance fails the run and withholds the reference write, so a regression cannot be
  normalized into the artifact the profiles assert against by the run that observed it, while a within-tolerance run
  replaces the reference so a routine re-measurement still records itself; `REFRESH_BASELINE=1` accepts a regression
  deliberately.
- The run's dated record is still written when the comparison fails, so the trend can see the movement.
- A `baselineTrend` task reports the dated records as a chronological series — date, target, operating point, and the
  plateau's mean, 95th, 99th percentile, and throughput — so comparable loads can be read over time.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/load-tests`: the requirement covering a baseline run's report and recording gains the drift
  comparison and the tolerance-gated write policy, and a new requirement covers the trend over the recorded runs.

## Impact

- `load-tests/src/gatling/java/showcase/loadtests/`: `BaselineStats` (the comparison and the conditional write);
  `load-tests/src/main/java/showcase/loadtests/`: new `BaselineReference`, `BaselineDrift`, and `BaselineTrend` types
  (the `main` source set, so the `test` suite can see them and the `gatling` suite still does), with a new
  `src/test/java` unit test.
- `load-tests/build.gradle.kts`: the `baselineTrend` task and the drift properties.
- `scripts/load-test-baseline.sh`: `REFRESH_BASELINE`, the tolerance override, and the record written before the
  comparison's verdict is propagated.
- `AGENTS.md` (the load-test command block) and `docs/ideas.md` (the idea's removal).
- No service, API, or deployment change; nothing new is required to run a load test, only to record it.
