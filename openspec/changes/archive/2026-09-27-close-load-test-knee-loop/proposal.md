# Proposal: Close the load-test knee loop

## Why

The reworked load tests compute a knee, but nothing uses it. `scripts/load-test-baseline.sh` derives the knee into
`knee.properties` and runs only `calibrate` and `baseline`, so running a performance profile at the measured knee is a
manual `-PkneeRate=<knee>` step — the profiles exist but are never anchored to the measurement. Worse, the calibration's
`CALIBRATE_RATE` default (`200`) has never produced a sustained departure: every recorded run reports `measured=false`,
so the "knee" is a ceiling and the baseline operating point is 60 % of an unmeasured number.

The loop is also untrustworthy at its edges: a mistyped `profile` silently falls back to `smoke`, so a run that never
exercised the intended profile reads as a pass. Make a real knee measurable, anchor the profiles to it, and make a wrong
profile name fail.

## What Changes

- `scripts/load-test-baseline.sh`: iterate the calibration ceiling upward (from `CALIBRATE_RATE`, doubling, capped at
  `CALIBRATE_MAX_RATE`, default `1600`) until the run reports a sustained departure (`measured=true`) or the cap is
  reached; report the ceiling actually reached rather than the single starting value.
- The same script: an optional `PROFILE` variable runs a named performance profile at the derived knee
  (`-Pprofile=$PROFILE -PkneeRate=$KNEE`) after the baseline, so the loop is closed end to end instead of by hand.
- `ShowcaseSimulation`: an unsupported `profile` fails the run before injecting load instead of falling back to `smoke`,
  with `smoke` becoming an explicit case; a mistyped profile can then no longer pass as a run.
- `AGENTS.md` and `README.md`: document the calibration loop (`CALIBRATE_MAX_RATE`), the `PROFILE` run, and the
  unsupported-profile failure.
- `docs/ideas.md`: remove the implemented "Close the loop between the measured knee and the performance profiles" entry.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/load-tests`:
  - `Pass assertions` — the smoke scenario no longer covers an unknown value; an unsupported profile fails before
    asserting.
  - `Knee-relative injection profiles` — an unsupported profile value fails the run instead of falling back to the smoke
    profile, carried by a new scenario.

## Impact

- **Build**: the load-test wrapper (a shell script, not gated) and `ShowcaseSimulation` (Spotless/ErrorProne-gated); no
  new dependency, no gate change.
- **Tests**: verified by running the wrapper with short durations and a `PROFILE`, confirming the loop reports each
  step's outcome, the baseline still measures, and the profile runs at the derived knee; and by a `PROFILE=bogus` run
  that fails before injecting load.
- **Docs**: `AGENTS.md`, `README.md`, and the implemented `docs/ideas.md` entry.
- **Deployment**: none. The loop lengthens a full run when the ceiling must be raised (each step is a
  `CALIBRATE_DURATION` ramp); the cap bounds it.
