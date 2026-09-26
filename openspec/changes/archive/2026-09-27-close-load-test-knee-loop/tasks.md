# Tasks

## 1. Wrapper

- [x] 1.1 Iterate the calibration ceiling in `scripts/load-test-baseline.sh`: after each `calibrate` + `kneeFinder`,
      read `measured`, and while it is not `true` and the ceiling is below `CALIBRATE_MAX_RATE` (default `1600`), double
      the ceiling and repeat; stop on `measured=true` or at the cap. Verify with short durations
      (`CALIBRATE_DURATION=PT30S`) that the loop stops on a departure or at the cap.
- [x] 1.2 Make the report state the ceiling actually reached (and the step count) in its calibration line, and adjust
      the fallback label so it does not recommend raising a knob the cap has superseded. Verify by reading `report.md`
      after a looped run.
- [x] 1.3 Add an optional `PROFILE` variable that runs `gatlingRun -Pprofile=$PROFILE -PkneeRate=$KNEE` (forwarding the
      baseline's `baseUrl`, `ratio`, and `sseConnections`) after the baseline, setting the script's exit status on a
      failed profile run. Verify a short `PROFILE=spike` run starts at the derived knee.

## 2. Simulation

- [x] 2.1 In `ShowcaseSimulation`, add an explicit `case "smoke"` to the injection and assertion switches and make each
      `default` fail the run, naming the unsupported profile. Verify `-Pprofile=smoke` still runs its fixed users, and
      an unsupported name fails before injecting load.

## 3. Docs

- [x] 3.1 Document the calibration loop (`CALIBRATE_MAX_RATE`) and the `PROFILE` run in `AGENTS.md`'s load-test command
      block and `README.md`'s load-testing section, and note that an unsupported profile name fails the run. Verify each
      documented statement matches the script and the simulation.
- [x] 3.2 Remove the implemented "Close the loop between the measured knee and the performance profiles" entry from
      `docs/ideas.md`. Verify no other entry references it.

## 4. Verification

- [x] 4.1 `./gradlew spotlessApply` then `spotlessCheck` pass; `openspec validate --changes` passes.
- [x] 4.2 Run the wrapper with short durations against the local cluster and confirm: the calibration loop reports each
      step's outcome, the baseline plateau still measures, and a `PROFILE` run uses the derived knee.
- [x] 4.3 Run the wrapper with `PROFILE=bogus` and confirm the profile run fails (the wrapper reports the profile
      failure and exits non-zero), exercising the D3 failure path.
