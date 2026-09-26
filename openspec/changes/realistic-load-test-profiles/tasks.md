# Tasks

## 1. Configuration

- [x] 1.1 Add the `kneeRate`, `thinkTime`, `detailShare`, `startShare`, `finishShare`, and `hold` properties to
      `ShowcaseSimulation.java` (defaults per design D4) and forward them through
      `build-logic/src/main/kotlin/load-testing-conventions.gradle.kts` — keeping the name `kneeRate` distinct from the
      `kneeFinder` task's `-Pknee` output path. Verify a `smoke` run accepts them.

## 2. Profiles and mix

- [x] 2.1 Rewrite `injectionSteps` so the performance profiles are multiples of `kneeRate` per design D1
      (smoke/calibrate/baseline unchanged; `spike` bursts to `1.5×` users via `stressPeakUsers`). Verified by a `spike`
      run (`-PkneeRate=50`) bursting at the scaled rate; `average`/`soak` reuse the same scaling but were not run to
      their full holds.
- [x] 2.2 Weight the read stream's detail fetch by `detailShare` and the write stream's start/finish by `startShare`/
      `finishShare`, keeping every lifecycle terminating. Verified by a `smoke` run and the wrapper's `baseline` (0
      failed), whose statistics show fewer starts than schedules.
- [x] 2.3 Add the `thinkTime` pauses between actions in both streams, and make the SSE hold follow the profile's run
      length (the soak's ramps plus `hold`) rather than the `duration` property. Verified by a `smoke` run's durations
      and the wrapper's `baseline` holding its SSE connections for the plateau; the soak's hold derives from the shared
      ramp/hold constants.
- [x] 2.4 Split the assertions at the knee: `average`/`stress`/`soak` keep the percentiles, and `spike`/`breakpoint`
      carry none. Verify a short `spike` run completes without a percentile assertion.

## 3. Spec and docs

- [x] 3.1 Add the `showcase/quality/load-tests` delta: `REMOVED`+`ADDED` for `Injection profiles` (knee-relative curves,
      carrying calibrate/baseline), and `MODIFIED` for the read/write streams (weighted mix, think time), the
      configuration (the new properties), and the pass assertions (the above-knee split). Verify with
      `openspec validate --changes`.
- [x] 3.2 Refresh `AGENTS.md`'s load-test section (the forwarded-property list) and `README.md`'s load-testing
      description (the profiles and the new properties). Verify each documented command/property matches the code.

## 4. Verification

- [x] 4.1 `./gradlew :load-tests:check` passes; `./gradlew spotlessApply` then `spotlessCheck` pass.
- [x] 4.2 Verified against the local cluster: `smoke` passes (weighted streams, think time), a `spike` run
      (`-PkneeRate=50`) completes with no assertion (the above-knee split), and the wrapper's `calibrate`+`baseline`
      measures a plateau with 0 failed (the mixed streams under the per-name assertions). `average` and `soak` were not
      run to completion (40 min / 2 h holds) and reuse the same weighted streams and knee scaling.
