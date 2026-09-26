# Proposal: Realistic, knee-relative load-test profiles

## Why

The six performance profiles are absolute templates unrelated to the target: they ramp to `200`/`400`/`4000`/`40000`
users per second and hold for `8h`/`2h`, numbers borrowed from the load-testing canon. On this stack the calibration
reached a `200`-unit/s ceiling without the response time departing, so the knee was never measured and `average` sits at
that ceiling while `stress`, `spike`, and `breakpoint` are far beyond what the local cluster serves. The workload mix is
fixed too: a read iteration always fetches a showcase detail (a 1:1 list:detail ratio), a write iteration always runs
the whole schedule→start→finish→remove lifecycle, and nothing models a client's pacing. Make the profiles relative to a
configured knee-rate, weight the branches like real use, and make the pacing and mix configurable.

## What Changes

- `load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java`: express each performance profile as a
  multiple of a configured **knee-rate** (`kneeRate`) — `average` `0.6×`, `soak` `0.6×` for a configurable hold,
  `stress` `0.9×`, `spike`/`breakpoint` a `1.5×` burst/ramp — so every profile is runnable against whatever target it
  points at.
- The same file: **weight the branches** — the read stream fetches a detail for a configurable share, and the write
  stream always schedules and removes but starts and finishes for configurable shares, so a lifecycle still terminates
  (bounded data).
- The same file: a configurable **think time** between a user's actions, and the SSE connections hold for the profile's
  run length, so a run models client pacing and a full profile rather than a back-to-back loop.
- The same file: `spike` and `breakpoint` sit **above** the knee, so they probe the ceiling and carry no pass assertions
  (like `calibrate`); the below/at-knee profiles keep the percentiles.
- New configuration properties: `kneeRate`, `thinkTime`, `detailShare`, `startShare`, `finishShare`, and `hold`,
  alongside the existing `baseUrl`, `profile`, `rate`, `ratio`, `duration`, and `sseConnections`.
- `build-logic/src/main/kotlin/load-testing-conventions.gradle.kts`: forward the new properties into the Gatling task's
  system properties (the `kneeFinder` task keeps its own `-Pknee` output path).
- `openspec/specs/showcase/quality/load-tests/spec.md` (delta): the knee-relative profiles, the weighted mix, the new
  configuration, and the split assertion rule.
- `AGENTS.md` and `README.md`: name the new properties and correct the profiles' description.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/load-tests`: the injection profiles become knee-relative, the read/write mix is weighted, the pacing
  and mix are configurable, and the above-knee profiles drop their pass assertions.

## Impact

- **Build**: the `load-tests` simulation, its convention wiring, and its spec only; no new dependency, no gate change.
  The run stays opt-in.
- **Tests**: verified by running `smoke` and a short `spike` against the local cluster, and by the wrapper's
  `calibrate`+`baseline`; `average` and `soak` reuse the same weighted streams and knee scaling but were not run to
  their full holds.
- **Deployment**: none. The wrapper (`scripts/load-test-baseline.sh`) runs only `calibrate` and `baseline` today; a
  follow-up would let it (or the docs) supply the measured knee when a performance profile is run, and the profiles fall
  back to the `kneeRate` default until then.
