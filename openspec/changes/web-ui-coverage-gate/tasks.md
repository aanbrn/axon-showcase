# Tasks

## 1. The coverage tooling

- [x] 1.1 Add `@vitest/coverage-v8` to `showcase-web-ui/package.json`'s devDependencies with the same caret range as
      `vitest` (`^5.0.x`), and a `test:coverage` script (`vitest run --coverage`); update `package-lock.json`. Verify
      `npm ls @vitest/coverage-v8` reports a version satisfying the range.
- [x] 1.2 Declare the minimum in `config/web-ui-coverage/coverage-baseline.properties` (e.g.
      `coverage.statements.minimum = 0.80`, with a comment naming the measured value and rationale). Verify the file
      parses and the value sits below the measured statement coverage.
- [x] 1.3 Configure coverage in `showcase-web-ui/vite.config.ts`: provider `v8`, reports `text` and `lcov`,
      `reportsDirectory: 'build/coverage'` (not Vitest's default `<module>/coverage`), and a statement `threshold` read
      from `process.env.VITEST_COVERAGE_MIN` (defaulting to the committed minimum when unset, so `npm run test:coverage`
      works standalone) **scaled from the baseline's fraction to Vitest's percentage** (a fraction compared against a
      percentage never trips the gate — the failure the control caught). Verified: `0.80` passes, `0.99` fails with
      "Coverage for statements (91.6%) does not meet global threshold (99%)", and no `coverage/` directory appears
      outside `build/`.

## 2. The Gradle gate

- [x] 2.1 Add an `npmTestCoverage` `NpmTask` in `build-logic/src/main/kotlin/frontend-conventions.gradle.kts` (wrapping
      `test:coverage`, reading `coverage.statements.minimum` from the baseline file and passing it as the
      `VITEST_COVERAGE_MIN` environment variable, with the baseline file and the `src` tree + configs as inputs,
      mirroring `npmTest`) and make it the frontend `check`'s test member **in place of** `npmTest` (a strict superset;
      keeping both would run the Vitest suite twice). Verified with `./gradlew :showcase-web-ui:npmTestCoverage` and by
      confirming the task is scheduled in `./gradlew :showcase-web-ui:check --dry-run`.
- [x] 2.2 Refresh the docs the change falsifies: `AGENTS.md`'s coverage convention (which currently describes only the
      JVM JaCoCo gate) and its `check` note if it enumerates the frontend checks, and `README.md` if it describes
      coverage or the frontend checks. Remove the implemented idea from `docs/ideas.md`. Verify by reading the edited
      passages and the idea grep returning nothing.
- [x] 2.3 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then `./gradlew spotlessCheck` and
      `openspec validate --changes`, and confirm all pass.

## 3. Verification

- [x] 3.1 Prove the gate with a known-bad and a known-good input: temporarily raise the baseline above the measured
      statement coverage and confirm `./gradlew :showcase-web-ui:npmTestCoverage` fails with the measured-vs-minimum
      message; restore it and confirm the task passes. Record both runs' output.
- [x] 3.2 Confirm the gate runs inside `./gradlew :showcase-web-ui:check` and that the standard `check` is green.
- [x] 3.3 Run the per-unit `lesson-capture` subagent over the change and apply its durable proposals; record the applied
      net `AGENTS.md` delta on this task.
