# Proposal

## Why

`gradle-helm-plugin` 3.3.1, published 2026-11-08, carries the bounded retry this repository reported (`#240`, part of
`#238`) and the `failIfExists` fix (`#241`), but the catalog pins 3.3.0, so the build still replaces the plugin's fetch
tasks with two workarounds. The plugin's tasks can be scoped through `extraArgs` — the route `#718` used to scope the
chart fetch, and the route the upstream report records for `repo update` — so both workarounds can retire: the plugin
then owns the retry and the declared-repository scope.

## What Changes

- **`gradle/libs.versions.toml`** — `helm-plugin` `3.3.0` → `3.3.1`; `helm-releases-plugin` shares the version ref and
  moves with it.
- **`build.gradle.kts`** — enable the plugin's repository tasks; set `attempts` (3) and `retryDelay` (2 s) on the
  `HelmAddRepository` tasks, and `attempts`, `retryDelay`, `repositoryCacheTtl` (`Duration.ZERO`), `repositoryNames` and
  the declared names in `extraArgs` on `helmUpdateRepositories`; remove the `updateHelmRepositories` registration and
  its bridge.
- **`build-logic/src/main/kotlin/UpdateHelmRepositoriesTask.kt`** — deleted; the install path uses the plugin's tasks.
- **`helm/chart/build.gradle.kts`** — enable `helmAddBitnamiRepository`, `helmUpdateRepositories` and
  `helmUpdateMainChartDependencies`; set `attempts` (3) and `retryDelay` (2 s) on the add, update and dependency tasks,
  `repositoryCacheTtl` (`Duration.ZERO`) on the update, and scope the update to `bitnami` through `repositoryNames` and
  `extraArgs`; delete the `fetchMainChartDependencies` registration and its bridge.
- **`build-logic/src/main/kotlin/FetchChartDependenciesTask.kt`** — deleted; the chart packaging uses the plugin's
  tasks.
- **`.opencode/skills/gradle-build/SKILL.md`** — the deprecation note names plugin 3.3.1, which still calls
  `Project.getProperties()` and lists no `#145` fix.
- **`.opencode/skills/deployment-operations/SKILL.md`** — the install-path bullet and the chart-validation paragraph
  describe the plugin's tasks, their retry and their repository scope.
- **`docs/ideas.md`** — the 2026-10-27 `failIfExists` entry is removed (released, and the build does not set the
  option); the 2026-10-17 entry is removed (this change implements it); the 2026-10-16 cache-skip entry is removed
  (fixed in 3.3.0); the 2026-11-09 entry is rewritten to park a per-download timeout on every fetch path.
- **Upstream** — a comment on `#238` names the two remaining API papercuts: `repositoryNames` does not scope the
  executed command, and `HelmAddRepository` has no timeout property.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. The only requirement touching this behavior is `showcase/deployment/helm-chart`'s "The chart is packaged by the
standard check", and it is outcome-level: the repository setup and the dependency fetch stay retried a bounded number of
times and a persistent failure still fails the build. The release install path is not specified. `skip_specs: true`
records that decision.

## Impact

- **Build path**: the version catalog, the root build script, `build-logic` and the chart module move; every Helm
  release install task and the chart packaging resolve through the plugin's tasks.
- **Configuration cache**: the plugin's fetch tasks run in the root project and the chart module for the first time
  since the workarounds, so their store-and-reuse behavior is verified rather than assumed.
- **Deployment**: the install path and the chart packaging keep their declared-repository scope and gain the plugin's
  retry; Helm's two-minute per-download default is unchanged.
- **Verification tasks**: the chart-strategy render tasks depend on the plugin's dependency task, so
  `verifyChartStrategies` still renders the collected chart after the workaround's task is gone.
- **No runtime or API change**: no service, chart value, or image is touched.
