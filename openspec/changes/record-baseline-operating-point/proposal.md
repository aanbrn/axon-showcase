# Proposal

## Why

A baseline run compares its plateau's response times against the committed reference without knowing what workload rate
each side measured, so a re-measure at a different operating point reports every read _and_ write request as regressed.
A re-measure whose calibration found a higher knee ran its plateau at 300 workload units/s against a reference at 124
and reported a spurious regression across the board (including writes, which the change under test had not touched),
while the 2026-10-08 re-measure at 127 units/s against the 2026-09-28 reference at 124 showed the list read had actually
improved. The reference records the response times but not the rate they were measured at, so nothing can tell a real
regression from a different load point.

## What Changes

- Record the operating point (the plateau's workload rate in units per second) in the baseline reference — parse,
  render, and header comment in `BaselineReference.java`, populated by `BaselineStats.measure` from a new argument.
- Gate the drift comparison in `BaselineDrift.compare` on a matching target **and** an operating point within a tenth of
  the recorded operating point: a reference recorded beyond that band, or with no operating point, is not compared, and
  the run reports it and records its measurement rather than reporting regressions.
- Pass the measured operating point through: `load-tests/build.gradle.kts` (`-PoperatingPoint` on the `baselineStats`
  task) and `scripts/load-test-baseline.sh` (the `$OPERATING` value it already reads from `knee.properties`).
- Record `operatingPoint=127` in the committed `load-tests/src/gatling/resources/baseline.properties`, the value from
  the 2026-10-08 record that produced it, so the committed reference is comparable again.
- Update `BaselineReferenceTests` and `BaselineDriftTests` for the new field and the operating-point-mismatch case.
- Refresh the load-tests docs: the `AGENTS.md` Build & Test note, the README load-test section, and the load-tests spec
  delta; remove this change's idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/load-tests`: the "A baseline run reports its measurements" requirement — the reference records the
  operating point it measured, and the drift comparison is made only against a reference recorded for the same target
  and a matching operating point.

## Impact

- **Code**: `BaselineReference`, `BaselineStats`, `BaselineDrift` (load-tests module), `load-tests/build.gradle.kts`,
  and `scripts/load-test-baseline.sh`.
- **Reference data**: the committed `baseline.properties` gains `operatingPoint`; a reference lacking it is treated as
  not comparable until it is refreshed.
- **Tests**: the load-tests unit suite (`BaselineReferenceTests`, `BaselineDriftTests`) gains the operating-point field
  and the mismatch case.
- **Build / deployment**: no dependency, image, chart, or CI-gate change. A live measurement is not required — the
  committed reference's operating point is taken from the dated record that produced it.
