# Design

## Context

See `proposal.md` — Why.

- The root project registers `updateHelmRepositories` (`UpdateHelmRepositoriesTask`, built on
  `AbstractHelmRepositoriesTask`) and disables the plugin's `helmAdd*Repository`, `helmAddRepositories` and
  `helmUpdateRepositories` tasks; the plugin's task is a disabled bridge the release install tasks already depend on.
- The chart module registers `fetchMainChartDependencies` (`FetchChartDependenciesTask`) with `repos = bitnami`, and
  keeps the plugin's chart tasks disabled behind a disabled `helmUpdateMainChartDependencies` bridge.
- The plugin's release tasks in 3.3.1: `HelmAddRepository`, `HelmUpdateRepositories` and `HelmUpdateDependencies` each
  expose `attempts` (default 1) and `retryDelay` (default 10 s); `HelmUpdateRepositories` also exposes
  `repoUpdateTimeout` (unset, so Helm's 2-minute per-index default applies) and `repositoryCacheTtl` (default 1 hour).
- `HelmUpdateRepositories.updateRepositories()` runs `helm repo update` with **no** repository names; `repositoryNames`
  feeds only the up-to-date check and the `onlyIf`. The command can still be scoped: `extraArgs` is applied to the
  invocation before the action's own arguments, and `#718` scoped the chart packaging's update with
  `extraArgs.addAll("bitnami")`. The upstream report records the same route, plus
  `extraArgs.addAll("--timeout", "15m")`, and states both were checked against the executed command line.
- The configuration-cache skip that motivated the chart workaround was fixed by the plugin's own 3.3.0 change: 3.2.0's
  `onlyIf` resolved `modelDependencies.get()` (a provider serialized with the cache entry), 3.3.0 reads the chart model
  at execution (`readModelDependencies()`).

## Goals / Non-Goals

**Goals:**

- The plugin's tasks own the retry and the declared-repository scope on both fetch paths.
- The chart packaging keeps its Bitnami-only scope, so `check` does not refresh every declared repository.
- The records state the two remaining API papercuts upstream, and the parked timeout entry covers every fetch path.

**Non-Goals:**

- Changing the per-download timeout on any path: Helm's two-minute default stays, and a budget needs a measured run.
- Changing the chart, values, images, or any spec'd outcome.
- Taking the other two `buildLogicUpdates` rows (`protobuf-plugin`, `spotless-plugin`); they are separate units.

## Decisions

### Both workarounds retire; the plugin's tasks are scoped through `extraArgs`

The plugin's `helm repo update` accepts positional repository names, and `extraArgs` is applied to the invocation before
the action's arguments, so the declared-repository scope `#718` established is preserved by
`extraArgs.addAll(<declared names>)` on the update task. The install path uses the same route for the root's four
repositories; the chart module uses `extraArgs.addAll("bitnami")`. The plugin's retry then replaces `BoundedRetry` on
both paths. Alternatives rejected: keeping the workarounds (the duplicated retry and the stale plugin pin stay); an
unscoped plugin update (refreshes every repository in the user's Helm configuration, contradicting `#718`).

### `repositoryCacheTtl` is `Duration.ZERO`

The plugin's up-to-date check would otherwise skip `repo update` for up to an hour. A chart bumped inside that window
would be missing from the cached index, and `helm install --version <pinned>` would fail to resolve it. The workarounds
refreshed on every run, so `ZERO` preserves that. Alternative rejected: the one-hour default (faster, but a stale-index
install failure the repository has no way to attribute).

### No per-download timeout budget is introduced

The plugin's update and add run at Helm's two-minute default, and so do the shared helper's two remaining callers. The
upstream report's reproduction records a failure on a slow index, but a budget needs a measured run and a justified
value; neither exists here, and the same objection applies to the helper and the plugin alike. The parked entry names
the evidence that reopens it. Alternatives rejected: five minutes on the plugin's tasks only (unjustified, and it would
also need the add's `extraArgs` route, whose flag reaches the OCI login path); a larger value (no evidence for one).

### The shared helper keeps its two remaining callers and its default

`AbstractHelmRepositoriesTask` and `BoundedRetry` stay for `VerifyInfraImageVersionsTask` and `HelmUpdatesTask`; both
keep Helm's default timeout, covered by the parked entry.

### The upstream report is a comment on `#238`

The issue that asked for the retry and the timeout is still open, so the two remaining papercuts — `repositoryNames`
does not scope the executed command, and `HelmAddRepository` has no timeout property — go on that thread, each with its
`extraArgs` workaround, rather than a new issue.

## Risks / Trade-offs

- **The plugin's tasks may not store under the configuration cache** → run the repository setup and the packaging twice
  on a clean checkout and require a stored entry, then a reused one; if they do not store, restore the workaround and
  record why.
- **`extraArgs` is a positional list, so a renamed repository makes the update fail loudly** → that is the intended
  failure: `helm repo update` names the unknown repository, and the build's repository names are declared in one place.
- **The retry's bound is not covered by a committed gate** → `build-logic` has no TestKit, and the plugin's own
  functional tests cover its retry; the repository verifies its configuration by a run.
- **The fetch paths keep Helm's two-minute default** → the parked entry names the reproduction and the measurement a
  budget would need.

## Migration Plan

A build-configuration change with no deployment step: rollback is reverting the commit, which restores the two
workaround tasks and the disabled plugin tasks.

## Claims table

| Claim                                                                                                                                                   | Source                                                                                                                                                   | Result   |
| ------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| 3.3.1 was published on 2026-11-08 and lists `#240` (retry) and `#241` (`failIfExists`)                                                                  | `gh api repos/build-extensions-oss/gradle-helm-plugin/releases/tags/3.3.1 --jq '.published_at, .body'`                                                   | verified |
| `#238` is still open                                                                                                                                    | `gh api repos/build-extensions-oss/gradle-helm-plugin/issues/238 --jq .state`                                                                            | verified |
| The catalog pins `helm-plugin` 3.3.0, and `helm-releases-plugin` shares the version ref                                                                 | `grep -n '^helm-plugin' gradle/libs.versions.toml`; `gradle/libs.versions.toml:244-245`                                                                  | verified |
| `buildLogicUpdates` reports `helm-plugin`/`helm-releases-plugin` 3.3.0 → 3.3.1                                                                          | `cat build/build-logic-updates/report.txt`                                                                                                               | verified |
| 3.3.1 resolves from the Gradle Plugin Portal                                                                                                            | `curl -sL -o /dev/null -w '%{http_code}' https://plugins.gradle.org/m2/io/github/build-extensions-oss/helm/helm-plugin/3.3.1/helm-plugin-3.3.1.pom`      | `200`    |
| The plugin's `attempts` defaults to one attempt, `retryDelay` to ten seconds, and `repositoryCacheTtl` to one hour                                      | 3.3.1 `HelmAddRepository.kt` and `HelmUpdateRepositories.kt` (`HelmCommandRetry.DEFAULT_ATTEMPTS`, `convention(Duration.ofHours(1))`)                    | verified |
| `HelmUpdateRepositories` runs `helm repo update` with no repository names; `repositoryNames` feeds its `onlyIf` and cache check                         | 3.3.1 `HelmUpdateRepositories.kt` (`execHelmWithRetry("repo", "update", …)`; `repositoryNames` in `onlyIf` and `checkUpToDate`)                          | verified |
| `extraArgs` is applied to the invocation before the action's own arguments                                                                              | 3.3.1 `HelmExecProvider.kt` (`applyOptions` before `action?.execute`)                                                                                    | verified |
| `#718` scoped the chart packaging's update with `extraArgs.addAll("bitnami")` and `repositoryNames`                                                     | `git show bf0dbd61 -- helm/chart/build.gradle.kts`                                                                                                       | verified |
| `#238`'s body records `extraArgs.addAll("bitnami")` and `extraArgs.addAll("--timeout", "15m")`, checked against the executed command line               | `gh api repos/build-extensions-oss/gradle-helm-plugin/issues/238 --jq .body`                                                                             | verified |
| `HelmAddRepository` exposes no timeout property                                                                                                         | 3.3.1 `HelmAddRepository.kt`; `AbstractHelmCommandTask.kt` (no timeout property)                                                                         | verified |
| 3.2.0's dependency `onlyIf` resolved `modelDependencies.get()`; 3.3.0 reads `readModelDependencies()`                                                   | `HelmUpdateDependencies.kt` at tags 3.2.0 and 3.3.0                                                                                                      | verified |
| Helm 4.3.1's `repo add` and `repo update` accept `--timeout`, defaulting to two minutes; `repo update` accepts repository names                         | `helm repo add --help`; `helm repo update --help`                                                                                                        | verified |
| `BoundedRetry` makes three attempts with a two-second pause and propagates the last failure                                                             | `build-logic/src/main/kotlin/BoundedRetry.kt`                                                                                                            | verified |
| `AbstractHelmRepositoriesTask` has two callers once both workarounds are deleted                                                                        | `grep -rln AbstractHelmRepositoriesTask build-logic/src/main/kotlin` (the class, `VerifyInfraImageVersionsTask`, `HelmUpdatesTask`)                      | verified |
| Both fetch paths resolve charts from the Bitnami repository                                                                                             | `build.gradle.kts` (`helmRepoUrls`, `infraChecks`); `build-logic/src/main/kotlin/helm-conventions.gradle.kts` (`bitnami()`)                              | verified |
| The chart module's comment claims the plugin's tasks cannot retry                                                                                       | `helm/chart/build.gradle.kts` (the fetch registration's comment)                                                                                         | verified |
| The `helm-chart` packaging requirement is the only spec text naming the repository setup, and the release install path is not specified                 | `grep -rn "repository setup" openspec/specs/`; `grep -rn 'helmInstallToCi\|helmInstallToLocal' openspec/specs/` (only `deployment/web-ui`'s image build) | verified |
| 3.3.1 still calls `Project.getProperties()` and its release body lists no `#145` fix                                                                    | 3.3.1 `HelmPlugin.kt` (`createRepositoriesFromProjectProperties`); `gh api …/releases/tags/3.3.1 --jq .body`                                             | verified |
| The build sets no `failIfExists`                                                                                                                        | `grep -rn "failIfExists" --include=*.kts .`                                                                                                              | no match |
| The other two `buildLogicUpdates` rows are separate coordinates                                                                                         | `build/build-logic-updates/report.txt`                                                                                                                   | verified |
| The install path's update executes `helm repo update` with exactly the four declared repository names                                                   | the `helmUpdateRepositories --info` run's `Executing:` line                                                                                              | verified |
| The chart packaging executes `repo add bitnami`, `repo update bitnami` and `dependency update … --skip-refresh`, and bundles `charts/common-2.41.0.tgz` | the `:helm:chart:helmPackageMainChart --info` run; the packaged directory                                                                                | verified |
| The root's and the chart's fetch tasks store and reuse the configuration cache                                                                          | two `helmUpdateRepositories` runs and two `helmPackageMainChart` runs (`Configuration cache entry stored`/`reused`)                                      | verified |
| The Docker-free check is green                                                                                                                          | `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`                                                                                                | verified |
| `renderChartStrategies` depended on the deleted task and now depends on `helmUpdateMainChartDependencies`                                               | `build.gradle.kts` (the `dependsOn` line)                                                                                                                | verified |

**Scan dismissals** (`scripts/claim-scan.py`, one reason per kind):

- `path` — the paths are the files this change names or edits, or the unit's own record paths; each is a target, not an
  evidence claim.
- `count` — the matches are task-number prefixes (`1.1`, `2.3`) and figures quoted from the rows above; no figure is
  stated without a row.
- `absolute` — the scan matches the qualifier word itself; the load-bearing universal claims are the spec-corpus row,
  the `failIfExists` absence row and the caller rows, the rest are prose qualifiers, and the unit's own findings and
  hand-over are its log rather than claims of the change.
- `cross-ref` — `#718` and `#238` are rows; the remaining matches are ordinary phrases, not references to another
  artifact.
