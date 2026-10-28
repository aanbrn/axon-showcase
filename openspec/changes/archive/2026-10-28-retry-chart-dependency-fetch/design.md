# Design

## Context

See `proposal.md` for the motivation and `specs/showcase/deployment/helm-chart/spec.md` for the changed requirement.

The chart module's packaging path is the plugin's task graph: `helmCollectMainChartSources` →
`helmFilterMainChartSources` → `helmAdd*Repository` → `helmAddRepositories` → `helmUpdateRepositories` →
`helmUpdateMainChartDependencies` → `helmLintMainChart{Full,Minimal}` → `helmPackageMainChart`. Four facts shape the
approach:

- The plugin's `HelmAddRepository`, `HelmUpdateRepositories` and `HelmUpdateDependencies` actions each perform their
  network call once; none exposes a retry, and their task types are final enough that no repo-side task can wrap another
  task's action.
- `helmCollectMainChartSources` is a `Sync` into the chart's `outputDir` that preserves only `*.lock` files, so a
  collect re-run deletes a previously fetched `charts/` tree while keeping `Chart.lock`.
- The chart artifact (`helmMain`) publishes the chart dir with `builtBy(helmUpdateMainChartDependencies)`, and the
  package task and both lint tasks depend on that task by name; the root release install consumes the artifact through
  `from(chart(":helm:chart", "main"))`, and the release workflow runs the package task.
- The final `Chart.yaml` in the chart dir is produced by `helmCollectMainChartSources`: the `Sync` copies from the
  filtered-sources directory and depends on `helmFilterMainChartSources`, so the new task's dependency on the collect
  task orders filtering before the fetch.

## Goals / Non-Goals

**Goals:**

- A transient failure of the repository setup or of the dependency fetch is retried a bounded number of times and does
  not fail `check`.
- Every existing consumer of the dependency fetch keeps one entry point; the package, the lints, the chart artifact and
  the release install need no rewiring.
- The bundled `common` dependency remains required and the check still fails when the fetch stays unreachable.

**Non-Goals:**

- Changing Helm's per-download timeout or retrying a steadily slow download.
- Retrying the root project's release-install fetch (the `deployment-operations` guidance keeps that path's manual
  retry).
- Gradle build-cacheability of the fetch, or a classification of failures by message.
- Upstream support in the plugin itself, which stays reported at `build-extensions-oss/gradle-helm-plugin#238`.

## Decisions

### One task performs both stages, each under the existing `BoundedRetry`

`FetchChartDependenciesTask` (build-logic, extends `AbstractHelmRepositoriesTask`) calls `addHelmRepositories(repos)` —
`repo add` + `repo update <name>` per repository, already inside `BoundedRetry` — then runs
`helm dependency update <chartDir> --skip-refresh` inside its own `BoundedRetry.execute` with a warning per retry. The
helper is the repository's existing model (`helmUpdates`, `verifyInfraImageVersions`) and its tests cover the attempts
and the last-failure propagation; they pass a zero pause, so the duration is not asserted. Two tasks would duplicate the
repository setup; a single task keeps one retry boundary per network step.

Rejected: retrying only the repository fetch (leaves the dependency fetch unretried); a CI-level retry of the whole
`build` step (toleration-shaped, and the rule wants the step itself resilient); adding `gradleTestKit()` to build-logic
(the module deliberately has none and verifies plugin effects through consuming modules).

### The plugin's dependency task stays as a disabled bridge

`helmUpdateMainChartDependencies` is disabled and depends on the new task. Gradle runs a task's dependencies even when
the task itself is skipped, so the package task, both lints and the `helmMain` artifact keep the dependency they already
declare, and the release install's `from(chart(":helm:chart", "main"))` keeps working. The `setOnlyIf { true }`
configuration-cache workaround becomes moot and is removed.

Rejected: re-wiring `dependsOn` on the package and both lints and rebinding the artifact's `builtBy` (three consumers,
two mechanisms, and a fourth consumer would silently miss the fetch); deleting the bridge (the artifact's `builtBy`
would reference a missing task).

### The fetch's outputs are `Chart.lock` and `charts/`

The chart dir is internal; `Chart.yaml` is the input; the lock file and the `charts/` directory are the outputs. The
lock alone is insufficient: the collect task's `Sync` deletes `charts/` (preserving only `*.lock`), so a collect re-run
would leave the lock in place and the bundle gone, and an up-to-date fetch would package a chart without `common`.
Declaring `charts/` makes the missing bundle re-trigger the fetch.

Rejected: declaring the whole chart dir as the output (it is the collect task's output, and the change would claim it);
an `outputs.upToDateWhen` predicate instead of the output (the predicate re-derives what Gradle already tracks from a
declared output).

### The attempt count is an input, defaulting to the helper's three

`FetchChartDependenciesTask` exposes an `attempts` input (default 3) and passes it to `BoundedRetry`, so the control run
can set it to 1 and demonstrate that the fixture detects a step without retries. The build sets nothing.

### No timeout, no failure classification, no cacheability

The retry covers transient failures only; a steadily slow index still fails within Helm's two-minute per-download
default, and the owner's earlier scope left the timeout at its default. A persistent failure is not classified by
message: three attempts and the last failure is enough. The task stays non-cacheable, as the plugin's fetch tasks are.

### The requirement's existing clauses are retained; one clause and one scenario are added

`MODIFIED` replaces the whole requirement, so the delta carries the current description and its three scenarios
verbatim. The added clause is: "The repository setup and the dependency fetch SHALL each be retried a bounded number of
times before the packaging fails, so a transient repository or registry failure does not fail the check; the retry SHALL
NOT weaken the failure, since a fetch that stays unreachable SHALL still fail the build." The added scenario is "A
transient fetch failure is retried". No existing clause is amended or removed.

## Risks / Trade-offs

- **Overlapping outputs** between the collect task's `outputDir` and the fetch's `charts/` → both tasks are
  non-cacheable and the fetch depends on collect, so the overlap is ordering-controlled; the verification runs the
  packaging and reads Gradle's output for an overlapping-outputs warning.
- **The disabled bridge might not execute its dependency** if Gradle's skip semantics differ → the cold run asserts the
  fetch's commands execute and `charts/common` is bundled; a failure here invalidates the bridge and requires the
  explicit rewiring the plan rejected.
- **The scratch fixture proves the retry's wiring, not the real Helm's failure modes** → the fixture's fake Helm
  exercises the task's retry, its arguments and exhaustion; the cold packaging with the real CLI proves the bundling. It
  remains a one-time check, not a committed regression test.
- **A retried step must stay synchronous** → the action uses `execHelmCaptureOutput` inside the retry, the helper's
  documented synchronous variant, so a retry waits for the failing call rather than racing the worker queue.
- **The install path stays unretried** → recorded as a non-goal; the `deployment-operations` guidance retains its manual
  retry note, and the upstream report stays open.

## Verification plan

| Check                                                                                                                                                      | What it establishes                                                                                                                                                                   |
| ---------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Scratch fixture with a fake Helm executable driving the real task, one failure mode per stage (`add`, `update`, `dependency`) with successful predecessors | The affected step retries, warns per retry, and the run succeeds; the preceding stages ran, and a `repo update` failure re-runs its add+update pair                                   |
| Exhaustion, one stage at a time, with a fake that always fails that stage                                                                                  | Exactly three attempts for that operation, the last failure propagates, no later stage runs, and the task exits non-zero                                                              |
| Control: the same fixture with the task's `attempts` set to 1                                                                                              | The check goes red without the retry and green with it, so the fixture detects the retry rather than the fake's behaviour                                                             |
| Cold packaging with the real Helm                                                                                                                          | The real `common` is bundled, the package, both lint tasks and `helmMain` resolution (without a cluster) still work, and no overlapping-outputs warning names the collect/fetch paths |
| Configuration-cache store and reuse over the packaging and both lints                                                                                      | The new task and the bridge keep every consumer's path serializable                                                                                                                   |
| Incrementality: a chart source change, and deleting `charts/` while `Chart.lock` survives                                                                  | The fetch re-runs in both cases and the package keeps `common`                                                                                                                        |
| Docker-free `check`                                                                                                                                        | The change's own gate                                                                                                                                                                 |

## Claims table

| Claim                                                                                                               | Source                                                                                                         | Result   |
| ------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- | -------- |
| The plugin's three fetch tasks have no retry                                                                        | plugin 3.2.0 sources (`HelmAddRepository`, `HelmUpdateRepositories`, `HelmUpdateDependencies`)                 | verified |
| `helmCollectMainChartSources` is a `Sync` preserving only `*.lock`                                                  | plugin `CollectChartSourcesTaskRule.kt`                                                                        | verified |
| The package and both lints depend on `helmUpdateMainChartDependencies`; the chart artifact's `builtBy` is that task | plugin `PackageTaskRule`, `LintWithConfigurationTaskRule`, `ChartDirArtifactRule`                              | verified |
| The chart has exactly one dependency, Bitnami's `common`                                                            | `helm/chart/src/main/helm/Chart.yaml`                                                                          | verified |
| `build.gradle.kts` installs the app from `chart(":helm:chart", "main")`                                             | `build.gradle.kts` application release                                                                         | verified |
| `BoundedRetry` makes three attempts with a two-second pause and propagates the last failure                         | `build-logic/src/main/kotlin/BoundedRetry.kt`                                                                  | verified |
| The packaging requirement's current description and three scenarios                                                 | `openspec/specs/showcase/deployment/helm-chart/spec.md` (packaging requirement)                                | verified |
| Gradle executes a skipped task's dependencies                                                                       | the cold packaging run (`helmUpdateMainChartDependencies SKIPPED` while `fetchMainChartDependencies` executed) | verified |
| The collected `Chart.yaml` is the final filtered one                                                                | plugin `CollectChartSourcesTaskRule` (a `Sync` from the filtered sources, depending on the filter task)        | verified |
| No repo-side task can wrap another task's action                                                                    | each plugin fetch task performs its work in its own `@TaskAction`                                              | verified |
| The install path remains unretried                                                                                  | `deployment-operations` guidance                                                                               | verified |
| The fixture's per-stage retries recover from one failure, and exhaustion stops after three attempts                 | the fixture runs under `$TMPDIR/opencode/retry-fixture`                                                        | verified |
| The control with `attempts = 1` fails on the same first-failure fixture                                             | the fixture control run                                                                                        | verified |
| The cold packaging fetches only Bitnami and bundles `common`                                                        | the cold packaging run                                                                                         | verified |
| The packaging and both lints store and reuse the configuration cache with the fetch up-to-date                      | the two cache runs                                                                                             | verified |
| A chart source change and a deleted `charts/` each re-run the fetch and keep `common`                               | the incrementality runs                                                                                        | verified |
| The Docker-free `check` is green                                                                                    | the check run                                                                                                  | verified |
