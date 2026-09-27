# Tasks

## 1. The baseline reference

- [x] 1.1 Add a `baselineStats` task (a Java class in the module's `gatling` source set, registered as a `JavaExec` in
      `load-tests/build.gradle.kts` — not in `load-testing-conventions`, which must not name a module's class) that
      reads a baseline run's Gatling log over `LoadTestRequests.READ_WRITE` (the names the simulation asserts on, shared
      so the two cannot drift) and writes `load-tests/src/gatling/resources/baseline.properties`: the mean, 95th, and
      99th percentile per name, the policy (`factor`, the floors), the recording instant, and the target it is given
      (`-Ptarget="$BASE_URL"`, since the Gatling log carries no base URL). Verify its per-name values against the run's
      Gatling report (`load-tests/build/reports/gatling/<simulation>/`), since `report.md` is aggregate-only.
- [x] 1.2 Invoke the task from `scripts/load-test-baseline.sh` after the baseline plateau, so a run refreshes the
      reference under `load-tests/src/gatling/resources/`: the committed `baseline.properties` for the wrapper's default
      (local) target, and `baseline-<slug>.properties` for any other, where `<slug>` is the target host with every
      non-alphanumeric character replaced by `-`. Pass the matching name as `-PbaselineFile` to the profile run — the
      reference is a classpath resource name, as the default is. Verify the right file is written for each target.
- [x] 1.3 Move `loadTestProperties` from `load-testing-conventions.gradle.kts` into `load-tests/build.gradle.kts` (the
      convention must not name the simulation's own property interface) and add `baselineFile` to it, so the property
      reaches the simulation's JVM; the convention keeps applying `java-conventions` and the Gatling plugin. Verify a
      normal run still receives its forwarded properties and a run that overrides `baselineFile` sees the override.
- [x] 1.4 Move `kneeFinder`'s registration out of `load-testing-conventions.gradle.kts` into
      `load-tests/build.gradle.kts` — the same correction: the convention must not name a module's class. Its task path
      `:load-tests:kneeFinder`, its classpath, and its arguments are unchanged, and no build-logic test or consumer
      depends on the declaration site. Remove what the moves leave dead in the convention. Verify the wrapper's
      `kneeFinder` step still derives the knee, and that the convention is left with only the plugins it applies.

## 2. Derived thresholds

- [x] 2.1 Read the reference in `ShowcaseSimulation` from the classpath and derive the below-knee thresholds as
      `max(floor, factor × baseline)` per read/write request and percentile, with the guard: a reference whose recorded
      target is not the configured one is ignored, not applied. Log the derived values and which set applied. Verify a
      short below-knee run logs the derived thresholds and passes.
- [x] 2.2 Verify the control: temporarily lower the reference (a tiny baseline and floors), confirm a run that passed
      now fails its derived assertion and names the derived threshold, then restore the reference.
- [x] 2.3 Verify the target guard: point `-PbaselineFile` at a reference recording a different target and confirm the
      run falls back to the absolute thresholds and logs the mismatch.

## 3. The record

- [x] 3.1 Write the record from the wrapper with the run's measured numbers and its method sections, marked as
      generated: `docs/load-tests/<date>.md` for the wrapper's default (local) target, `<date>-<slug>.md` otherwise, and
      the report's target line states the URL it measured rather than asserting the local cluster. Verify the file
      appears with the run's numbers and that `spotlessCheck` passes after `spotlessApply`.
- [x] 3.2 Update `docs/ideas.md`: remove the assertions-from-a-baseline idea (implemented here) and rewrite the
      observational-run idea — its record half is implemented here, and its scheduled half is restated as the reshaped
      availability smoke.

## 4. Docs

- [x] 4.1 Sweep the repository for the identifiers this change relocates — `kneeFinder`, `load-testing-conventions`, and
      the whitelist's property names — and update every non-historical claim about where they live (notably
      `AGENTS.md`'s build-property bullet, which names the convention as the property-forwarding surface); the archived
      changes and the dated records stay as recorded. Then document the reference, the derivation, the record, and the
      per-environment use — the wrapper's slugged reference and record, `-PbaselineFile` for another environment, the
      caveat that the calibration deliberately ramps past the knee, so it belongs to an environment you own, and the
      caveat that its resource sampling follows the ambient kube context (`kubectl top pods -A`, the first node's
      allocatable), so a non-local run's sample data may describe another cluster — in `AGENTS.md`'s load-test block and
      `README.md`'s load-testing section. Verify each documented statement against the code.

## 5. The write stream's poll conditions

- [x] 5.1 Replace the three Gatling EL poll conditions with the session predicate (`differs(...)`), so the loop exits on
      the attribute rather than crashing. Verify against the pre-fix run: a sustained run reports zero `PollShowcase`
      KOs and no `LoopBlock` condition crash, where the pre-fix run reported 194 KOs of 22,463 with the EL crash (and
      the first fix attempt, which picked `String.valueOf(char[])`, crashed with a `ClassCastException`).

## 6. Verification

- [x] 6.1 `./gradlew spotlessApply` then `spotlessCheck` pass; `openspec validate --changes` passes.
- [x] 6.2 Run the verification runs above and record their outcomes in the change's report to the owner (the session
      summary the manual review reads), since they are live-cluster checks and none may be left unticked at archive.
- [ ] 6.3 In the archive commit, refresh the capability's `## Purpose` to name the recorded baseline reference, the
      thresholds derived from it, and the record a baseline run produces — a delta cannot carry a Purpose.
