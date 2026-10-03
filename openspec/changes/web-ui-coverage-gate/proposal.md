# Proposal

## Why

The JVM modules measure coverage and gate on it (`jacocoTestCoverageVerification`, baseline
`config/jacoco/coverage-baseline.properties`), but `showcase-web-ui` has none: `npmTest` (`vitest run`) reports
pass/fail only, so a change can drop the frontend's coverage — drop a test, or add an untested module — with nothing to
notice it. The coverage gap between the JVM and web-UI halves of the same repository is the defect.

## What Changes

- Wire Vitest's V8 coverage provider into the frontend test run: add `@vitest/coverage-v8` (pinned to the `vitest` major
  in the catalog/`package.json`), a `test:coverage` npm script (`vitest run --coverage`), and a `coverage` block in
  `vite.config.ts` (provider `v8`, reporters text + `lcov`, output under `build/coverage`, and a statement `threshold`
  fed from the baseline), so a regression fails `npmTestCoverage`.
- Add an `npmTestCoverage` Gradle task in `frontend-conventions` and make it the frontend `check`'s test member
  (replacing `npmTest`, which stays as a standalone fast task), so the gate runs with the other frontend checks without
  running the Vitest suite twice.
- Declare the threshold in `config/web-ui-coverage/coverage-baseline.properties` (mirroring the JVM baseline's shape),
  so it is tuned in one committed place rather than in code.
- Refresh the docs (`AGENTS.md` coverage convention, `README.md`) and remove the implemented idea.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/code-quality`: adds a requirement that the frontend test run measures coverage and fails `check` on
  a regression below the committed baseline — the capability owns the web module's build checks (`npmTest`, `npmLint`,
  `npmFormatCheck`, `npmTypeCheck`) and their `check` membership.

## Impact

- **Files**: `showcase-web-ui/package.json` + `package-lock.json` (the coverage dependency and script),
  `showcase-web-ui/vite.config.ts` (coverage config), `build-logic/src/main/kotlin/frontend-conventions.gradle.kts` (the
  `npmTestCoverage` task and `check` wiring), a new `config/web-ui-coverage/coverage-baseline.properties`, and docs.
- **Build / tests / services**: adds one `check` member (a Vitest run with coverage — no network beyond the npm install
  already done, no Docker); the frontend dependency graph gains `@vitest/coverage-v8`.
- **Verification**: proven with a known-bad (the threshold below the measured coverage fails the gate) and a known-good
  (the current tree passes at its measured coverage), and by the task running in `check`.
