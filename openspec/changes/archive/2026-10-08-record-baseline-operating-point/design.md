# Design

## Context

See `proposal.md` — Why. The baseline reference (`load-tests/src/gatling/resources/baseline.properties`, parsed and
rendered by `BaselineReference`) records the target, the derivation policy (factor and floors), and each read/write
request's mean, 95th, and 99th percentile response time, but not the workload rate the plateau ran at. `BaselineStats`
builds the measured reference from a simulation log and `BaselineDrift.compare` compares it against the recorded one
when the targets match, regardless of rate, so a plateau at a different operating point is read as a regression.

The wrapper `scripts/load-test-baseline.sh` already derives the operating point it runs the plateau at (the `$OPERATING`
value it reads from `knee.properties`, written by `KneeFinder`) and passes the run's other inputs to the `baselineStats`
task. The below-knee profiles (`ShowcaseSimulation`) derive their thresholds from the reference's target, factor,
floors, and figures only — none of which this change alters. The operating point is derived as 60% of the observed
maximum workload rate, so it varies by a few percent run to run even at a fixed calibration ceiling: the 2026-09-28 and
2026-10-08 runs both used `CALIBRATE_MAX_RATE=200` and came out at 124 and 127 units/s.

## Goals / Non-Goals

**Goals:**

- Record the operating point the plateau ran at in the baseline reference, and compare a new plateau against the
  recorded one only when the target matches and the measured operating point is within the matching band.

**Non-Goals:**

- Rescaling or otherwise normalizing response times across operating points — comparing different loads is not made
  valid by arithmetic.
- Changing the calibration or the knee/operating-point derivation (`KneeFinder`), or the below-knee profiles' threshold
  derivation.
- A per-operating-point reference file, or scheduling the drift comparison in CI.

## Decisions

- **Record the plateau's workload rate as the operating point, and treat operating points as matching within a tenth of
  the recorded rate.** The comparison is valid only at the same applied load, and the rate the plateau ran at is the
  quantity that identifies it; a relative band admits the calibration's own run-to-run variation (124 vs 127 units/s is
  2.4% and was relied on by the 2026-10-08 record as an apples-to-apples comparison) while rejecting a materially
  different load (124 vs 300 is 142%). Alternatives: exact equality (refuses the 124 vs 127 comparison and, because a
  mismatch writes the reference, would silently accept that run's measurement without checking it for a regression);
  recording the calibration ceiling instead of the rate (a proxy — the ceiling can change without the load changing, and
  two runs at the same ceiling can still differ in the derived operating point; the plateau's rate is what the response
  times reflect); rescaling (invalid — the response-time curve is not linear in rate around the knee).
- **A non-matching operating point is "nothing to compare", so the run records its measurement** — the same write policy
  as a target mismatch. Alternative: withhold the reference and require `REFRESH_BASELINE=1` to move it, which would
  keep the committed reference pinned to one rate but overload the refresh flag's "accept a regression" meaning and
  leave no path to adopt a genuinely repositioned baseline. The report names both rates and each run's dated record is
  annotated and the reference diff reviewed before commit, so the move is visible.
- **A recorded reference with no operating point is not comparable, with its own reported reason.** A hand-written or
  pre-change reference carries no rate to match, so comparing it would reintroduce the defect. The committed reference
  is given its operating point (`127`, from the 2026-10-08 record that produced it) in this change.
- **`operatingPoint` is nullable in the reference model and a required argument to the `baselineStats` task.** Nullable
  keeps a reference that omits it parseable (and round-trippable) rather than defaulting to a fake rate; required on the
  measured side keeps a run from silently recording an unmeasurable zero.
- **The Gradle property is named `operatingPoint`, not `rate`.** `rate` is already the simulation's forwarded property
  in `load-tests/build.gradle.kts`; reusing it would forward a rate to a task whose meaning is "the rate this plateau
  ran at", and the knee's own output already spells the value `operatingPoint`.

## Risks / Trade-offs

- **A run whose operating point lands outside the band skips the comparison.** This is a false negative (a comparison
  not made), not a false positive, and the report names both rates so the shift is visible; a wider band admits load
  differences that distort the figures, so the band stays narrow.
- **A reference lacking the operating point is never compared until refreshed.** Mitigation: set the committed
  reference's operating point from the record that produced it in this change.
- **Each run that measures an operating point outside the band moves the committed baseline.** Mitigation: the report
  names both rates, the dated record is annotated, and the reference diff is committed under review — the move is never
  silent.

## Migration Plan

No deployment or data migration. The committed reference gains `operatingPoint=127`; a reference without it is treated
as not comparable, so nothing breaks — the comparison simply waits for a reference that records its rate. Rollback is
reverting the change.
