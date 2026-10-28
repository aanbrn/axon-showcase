# Proposal

## Why

The chart-packaging gate fetches the chart's Helm repository and its `common` dependency once: a transient network
failure fails `check` with no retry, and the plugin's own task types cannot be wrapped from the build.

## What Changes

- `build-logic/src/main/kotlin/FetchChartDependenciesTask.kt` (new): adds and refreshes the chart's repository and runs
  `helm dependency update --skip-refresh`, each step inside the existing `BoundedRetry` (three attempts, a two-second
  pause, the last failure propagating); its outputs are `Chart.lock` and the chart's `charts/` directory.
- `helm/chart/build.gradle.kts`:
  - register the new task for the `main` chart, with the Bitnami repository and the chart's `Chart.yaml`, `Chart.lock`
    and `charts/` paths;
  - disable the plugin's `helmAddBitnamiRepository`, `helmAddRepositories`, `helmUpdateRepositories` and
    `helmUpdateMainChartDependencies`;
  - keep the disabled dependency task as a bridge that depends on the new task, so the package task, the two lint tasks
    and the chart artifact keep one entry point;
  - remove the now-moot `setOnlyIf { true }` workaround.
- `openspec/specs/showcase/deployment/helm-chart/spec.md` (delta): the packaging requirement gains the bounded retry of
  the repository setup and the dependency fetch, and a scenario for a transient failure that clears on retry.
- `docs/ideas.md`: the 2026-10-17 entry drops the implemented retry part and keeps the upstream remainder
  (`build-extensions-oss/gradle-helm-plugin#238`) with its retirement condition; the 2026-10-16 cache-skip entry notes
  that the build no longer uses the plugin's dependency task.
- `.opencode/skills/deployment-operations/SKILL.md`: the chart-validation note states that the packaging path retries
  its fetches a bounded number of times.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/deployment/helm-chart`: the standard `check` packaging requirement states the bounded retry of its
  repository setup and dependency fetch.

## Impact

- `build-logic`: one new task type; its retry is the existing `BoundedRetry`, whose tests are unchanged.
- `helm/chart`: the packaging task graph — four plugin tasks disabled, one new task and a disabled bridge in their
  place.
- No runtime, API, deployment or dependency change; the gate still fails when the fetch stays unreachable.
- `deployment-operations` guidance and the two `docs/ideas.md` entries.
