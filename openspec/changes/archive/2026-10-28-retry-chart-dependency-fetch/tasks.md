# Tasks

Workspace: unit=retry-chart-dependency-fetch; path=/Users/afanasyev/Development/axon-showcase;
branch=retry-chart-dependency-fetch; writer=ses_e832e4043ffe6ADYTEa13DvV1s

## 1. Implementation

- [x] 1.1 Add `FetchChartDependenciesTask` to `build-logic/src/main/kotlin/` — extends `AbstractHelmRepositoriesTask`,
      with `repos`, the `Chart.yaml` descriptor and an `attempts` input (default 3) as inputs, the chart dir internal,
      `Chart.lock` and `charts/` as outputs; its action calls `addHelmRepositories(repos)` then
      `execHelmCaptureOutput("dependency", "update")` inside `BoundedRetry`, warning per retry. Verify:
      `./gradlew -p build-logic build` passes and the task type is on the build-logic classpath.
- [x] 1.2 Wire `helm/chart/build.gradle.kts`:
  - register the task for the `main` chart (Bitnami URL from the helm extension, chart dir `outputDir`, descriptor
    `Chart.yaml`, outputs `Chart.lock` and `charts/`), depending on `helmCollectMainChartSources` — which depends on
    `helmFilterMainChartSources`, so the descriptor is the final filtered one;
  - disable `helmAddBitnamiRepository`, `helmAddRepositories`, `helmUpdateRepositories` and
    `helmUpdateMainChartDependencies`;
  - make the disabled dependency task depend on the new task as the bridge;
  - remove `setOnlyIf { true }`.

  Verify: `./gradlew :helm:chart:helmPackageMainChart --info` shows `repo add`, `repo update` and `dependency update`
  from the new task, and the packaged chart contains `charts/common`.

- [x] 1.3 Update `docs/ideas.md`: the 2026-10-17 entry drops the implemented retry part and keeps the upstream remainder
      at `build-extensions-oss/gradle-helm-plugin#238` with its retirement condition; the 2026-10-16 cache-skip entry
      states that the build no longer uses the plugin's dependency task. Update the `deployment-operations` chart
      validation note (the packaging path retries; the install path keeps its manual retry). Verify: grep the three
      files for the entry text and the retry statement.

## 2. Verification

- [x] 2.1 Scratch fixture: a fake `helm` executable driven through the real task (via an init script or a scratch build)
      that fails the first call of one stage at a time — `repo add`, `repo update`, `dependency update`, each with
      successful predecessors — then succeeds, creates the outputs only after a successful dependency call, and checks
      its arguments (the chart dir, `--skip-refresh`). Verify: the task's log shows the retry and warning, the run
      succeeds, and the call log shows the preceding stages ran (a `repo update` failure re-runs the add+update pair).
- [x] 2.2 Exhaustion, one stage at a time: the same fixture with a fake that always fails that stage. Verify: exactly
      three attempts for that operation, the last failure propagates, no later stage runs, and the task exits non-zero.
- [x] 2.3 Control: the same fixture with the task's `attempts` set to 1 in an isolated run. Verify: the check goes red
      on the first-failure fake; with the default attempts restored it goes green, so the fixture detects the retry.
- [x] 2.4 Cold packaging with the real Helm (caches cleared): `./gradlew :helm:chart:helmPackageMainChart`. Verify: only
      the Bitnami fetches run, the package contains `charts/common`, and Gradle's output carries no overlapping-outputs
      warning naming the collect/fetch paths (the cold run showed none).
- [x] 2.5 Direct consumers: run `:helm:chart:helmLintMainChartFull`, `:helm:chart:helmLintMainChartMinimal` and resolve
      `helmMain` without a cluster. Verify: the lints and the resolution run the new task's fetch and succeed.
- [x] 2.6 Configuration cache: run the packaging and both lints twice (store, then reuse). Verify: the second run reuses
      the entry and the fetch's execution/up-to-date state matches the first run's outputs.
- [x] 2.7 Incrementality: change a chart source, then separately delete `charts/` while `Chart.lock` survives. Verify:
      the fetch re-runs in both cases and the package keeps `common`.
- [x] 2.8 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `./gradlew spotlessCheck`. Verify: both green.

## 3. Review gate

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`; re-derive it after the last edit
      to the unit's own record. Verify: every row's source reproduces.
- [x] 3.2 Run the quick-review loop to a clean round, appending the ledger rows and running the ledger checks. Verify:
      the closing round reports no observations and `--expect-clean` passes.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py`, decide at or above the floor, and run the
      pass if it is `REQUIRED`. Verify: the outcome and its signals are recorded on this task. Routing: `REQUIRED`
      (`infrastructure-or-deployment`, `helm/chart/build.gradle.kts`); the deep pass ran and found one nitpick (N1 — the
      2026-10-16 `docs/ideas.md` entry said the dependency task was removed rather than kept as a disabled bridge),
      fixed and re-verified in quick round 5.
- [x] 3.4 Write `docs/process-defects/retry-chart-dependency-fetch.md` as defects are observed, or state that none was.
      Verify: the file exists before the manual-pass request.
- [x] 3.5 Request the owner's manual review pass and stop. Verify: the request names the review surface; nothing is
      committed, pushed, or archived before the owner's approval.
