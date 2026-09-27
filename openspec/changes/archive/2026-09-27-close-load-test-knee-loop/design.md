# Design

## Context

See `proposal.md` — Why. Current state: `scripts/load-test-baseline.sh` runs `calibrate` at `CALIBRATE_RATE` (default
`200`), derives the knee with the `kneeFinder` task into `knee.properties` (knee, operatingPoint, `measured`,
baselineP95Ms), then runs `baseline` at the operating point. Both committed records and the local `knee.properties`
report `measured=false` — the ramp never sustained a departure at `200`, so the derived "knee" is the ceiling and the
operating point is 60 % of it. The simulation's `kneeRate` property (added by `realistic-load-test-profiles`) is the
profiles' reference rate, but the wrapper never passes it.

An unsupported `profile` currently falls back to `smoke`: the `load-tests` spec states the fallback in two requirements,
and `smoke` is itself the simulation's `default` branch (there is no `case "smoke"`). `smoke` is also the property
default, so an unset `-Pprofile` resolves to it.

## Goals / Non-Goals

**Goals:**

- Measure a real knee where the stack departs, by raising the calibration ceiling until it does.
- Run a performance profile at the derived knee from the wrapper, closing the loop.
- Fail loudly on an unsupported profile instead of silently substituting `smoke`.

**Non-Goals:**

- No CI/scheduled run (a separate parked idea).
- No adaptive control during a single ramp — the loop re-runs `calibrate` at a higher ceiling, keeping the profile and
  the knee-finder code unchanged.
- No change to the supported profiles' injection curves or assertions.

## Decisions

- **D1 — Iterate the ceiling, doubling, capped.** Start at `CALIBRATE_RATE`, and while the derived knee is
  `measured=false` and the ceiling is below `CALIBRATE_MAX_RATE` (default `1600`), double it (clamping a doubled ceiling
  to the cap, so no step overshoots it) and re-run `calibrate` + `kneeFinder`. Stop on `measured=true` or at the cap.
  Doubling reaches a departure in a few steps for a stack that never departed at `200`; the cap bounds the extra time
  (each step is a `CALIBRATE_DURATION` ramp).
- **D2 — Report the ceiling reached and keep the labelled fallback.** The report's calibration line states the final
  ceiling (and how many steps it took), not the starting default, and when the cap is reached unmeasured the fallback
  label says so without recommending a knob the cap has superseded. The `measured` flag and its labels otherwise stay,
  so a run that never departs records honestly rather than presenting the ceiling as a knee.
- **D3 — `PROFILE` runs a performance profile at the derived knee, and its failure exits non-zero.** An optional
  `PROFILE` variable (e.g. `average`) runs `gatlingRun -Pprofile=$PROFILE -PkneeRate=$KNEE`, forwarding the same
  properties the baseline run forwards (`baseUrl`, `ratio`, `sseConnections`; other mix/pacing properties use their
  defaults). It runs after the baseline, so the baseline report stays the run's durable output. A profile whose
  assertions fail sets the script's exit status, like the baseline. The alternative — replacing the baseline with the
  profile — loses the resource-usage record the baseline produces.
- **D4 — `-PkneeRate` is the knee line, not the operating point.** The value is `knee.properties`' `knee` (the measured
  knee, or the ceiling when unmeasured); the profiles apply their own multiples (e.g. `average` `0.6×`), so passing the
  `operatingPoint` would apply the `0.6` factor twice.
- **D5 — An unsupported `profile` fails the run, and the two requirements stating the fallback are modified.** The
  simulation keeps an explicit `case "smoke"` (its current curve, and the property default, so an unset `-Pprofile`
  still works) and both profile-dispatch switches' (`injectionSteps` and `assertions`) `default` throws, naming the
  profile. The third `switch (PROFILE)` — the SSE hold — keeps its `default -> SSE_QUIET_PERIOD` (the smoke hold) on
  purpose: it is a static initializer that runs before dispatch, so a throw there would surface as an
  `ExceptionInInitializerError` rather than the named `IllegalArgumentException`. This deliberately reverses a recorded
  decision rather than fixing a defect: the spec's `Pass assertions` and `Knee-relative injection profiles` both state
  that an unknown value falls back to `smoke`, so each is modified — no requirement is added, and no other requirement
  changes. The payoff is twofold: a mistyped profile can no longer pass as a run, and the wrapper's profile-failure path
  (D3) becomes verifiable in seconds, where a failing supported profile would need a 30–40 minute asserting profile to
  reproduce.
- **D6 — `A baseline run reports its measurements` is untouched.** The report still states the knee, the operating
  point, the plateau duration and response times, and the per-service usage, and still labels an unmeasured knee as the
  calibration ceiling; the loop only changes which ceiling it was (the last one tried). No spec'd outcome moves, so the
  requirement takes no delta.

## Risks / Trade-offs

- **A longer run when the ceiling must be raised.** → Doubling plus the cap bound it; `CALIBRATE_RATE` and
  `CALIBRATE_MAX_RATE` are properties, so a faster or slower target is tunable.
- **A higher calibration load stresses the target.** → That is the point of a breakpoint-style ramp, and it is bounded
  by `CALIBRATE_MAX_RATE`; the calibration carries no assertions, so a degraded run is the finding.
- **`PROFILE` output lands only in the Gatling report.** → The baseline report remains the durable record; the profile's
  report path is named in the docs.
- **The fail-fast is a behavior reversal.** → A caller passing an unsupported profile name now fails instead of running
  `smoke`; that is the intent, and the modified requirements record it.
