# Design

## Context

See `proposal.md` — Why. The current state that shapes the approach:

- `check` composes its verification members in the root `build.gradle.kts`, and none of them reaches `:helm:chart`:
  `./gradlew check --dry-run` reports no `helmPackage*`, `helmLint*`, or `helmUpdate*` task, and `:helm:chart` has no
  `check` task.
- The chart's packaging runs only outside the pull-request gate: the release workflow packages it, while
  `helmInstallToLocal` and the deployment smoke collect the chart and install it, fetching its dependency and failing at
  the install when it is missing.
- `:helm:chart:helmPackageMainChart` resolves the chart's packaging graph before packaging (the claims table's
  packaging-graph row lists its fetch and lint tasks).
- Helm validates the declared dependencies at package time: with `charts/common-*.tgz` absent, packaging fails with
  "found in Chart.yaml, but missing in charts/ directory: common", and the strict lint fails with "chart directory is
  missing these dependencies: common". The chart carries no `.helmignore`, so nothing strips the fetched subchart from
  the packaging input.
- The dependency fetch is the fragile step: `helmUpdateMainChartDependencies` skips when the configuration cache stores
  its entry before the chart sources are collected, which is why `helm/chart/build.gradle.kts` replaces its gate with
  `setOnlyIf { true }` and why the configuration cache's introduction (#581) produced a chart with no `charts/`.

## Goals / Non-Goals

**Goals:**

- Run the chart's packaging in the standard `check` task, so the packaging regression `AGENTS.md` records fails the pull
  request that caused it.
- Keep the gate free of a Kubernetes cluster.

**Non-Goals:**

- A repository-authored assertion over the packaged directory. Helm's own dependency validation rejects the state such
  an assertion would check, so it could never fire in the real graph — a check that cannot be shown to fail is not
  evidence. This change's first design carried one, and the review's reproduction of `helm package` failing on the
  missing dependency retired it.
- Verifying the deployed chart's runtime behavior — probes, resource wiring, values, and the install itself are the
  deployment smoke's subject, not the pull-request gate's.
- Changing the chart-linting requirement: `check` now runs the strict lint transitively, but the linting outcome that
  requirement states is unchanged.
- Retiring the plugin's `setOnlyIf { true }` workaround or filing the upstream report: that is the parked companion
  idea, and this gate makes the workaround's failure visible rather than removing it.

## Decisions

**Add the chart's packaging task to `check`, rather than the lint or the fetch alone.**

- `:helm:chart:helmPackageMainChart` carries the chart's whole packaging path — the members the `helm-chart` requirement
  names — so depending on it runs them in the gate.
- Alternative rejected — the strict lint tasks alone: they reject an empty `charts/`, but they do not exercise the
  package step the release workflow runs.
- Alternative rejected — the fetch task alone: it proves the dependency was fetched, not that the chart packages.
- Alternative rejected — a repository-authored assertion over the packaged chart directory: `helm package` fails on the
  same state first, so the assertion cannot fire.
- Alternative rejected — the deployment smoke alone: it is nightly and observational, so the regression surfaces days
  later rather than in the pull request that caused it.

**The wiring names the existing task; no new task, rules object, or fixture.**

- The decision lives in the plugin and in helm, so a wrapper would add surface without a reachable decision of its own.
- Alternative rejected — a `*Rules` object with its `*RulesTests`, this repository's shape for a gate: that shape exists
  to make a repository-authored decision testable, and there is no such decision here.

**No committed control is owed.**

- `code-quality`'s "A check's control is a committed test" governs a gate whose decision the repository authors — its
  scenarios name a rules object or a gate working inline; this member's decision is helm's dependency validation,
  exercised by the run itself and by the smoke.

## Risks / Trade-offs

- **A registry fetch runs on every pull request** → it is the same plugin-managed fetch the release, smoke, and local
  paths perform; a fetch failure is a real failure (the chart cannot be packaged). The gate-worthy step's bounded retry
  cannot wrap the plugin's own task types, so the resilience follow-up is parked in `docs/ideas.md` (2026-10-17).
- **The plugin's helm tasks are not cacheable (its issue #208)** → a changed-input or fresh-workspace run re-executes
  the fetch, both lints, and packaging rather than restoring them from the Gradle build cache, so the pull-request gate
  pays that cost.
- **The failure is reported by helm, not by a named check of ours** → the check fails with helm's own message, which
  names the missing dependency; the requirement is stated at that observable level.
- **The wiring couples to the plugin's task name** → a plugin bump that renames `helmPackageMainChart` breaks the
  `dependsOn`; the wiring is what such a bump must re-verify.

## Migration Plan

None: one `check` dependency on an existing task, effective on the next run.

## Claims table

| Claim                                                                                                           | Source                                                                                                                                                                                                                                                                                                                                     |
| --------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Nothing in `check` packages, lints, or fetches the chart                                                        | `./gradlew check --dry-run` reports no `helmPackage*`/`helmLint*`/`helmUpdate*` task; `./gradlew :helm:chart:tasks --all` lists packaging but no `check`, and `:helm:chart:check` does not exist                                                                                                                                           |
| The release workflow packages the chart; the local install and the deployment smoke install the collected chart | `.github/workflows/release.yml:209` (`:helm:chart:helmPackageMainChart`); `./gradlew helmInstallToLocal --dry-run` and `./gradlew helmInstallToCi --dry-run` list the collection and fetch tasks and no `helmPackageMainChart`; `AGENTS.md`'s chart-validation bullet records the smoke failing at the install with the missing dependency |
| Packaging resolves the repository setup, the dependency fetch, and both strict lint configurations              | `./gradlew :helm:chart:helmPackageMainChart --dry-run` lists `helmAdd*Repository`, `helmUpdateRepositories`, `helmUpdateMainChartDependencies`, `helmLintMainChart`, `helmLintMainChartFull`, and `helmLintMainChartMinimal`                                                                                                               |
| Helm fails packaging when a declared dependency is absent                                                       | with the dependency moved aside and the fetch and lint tasks excluded, `./gradlew :helm:chart:helmPackageMainChart` fails (the plugin's helm client exits 1); `AGENTS.md`'s chart-validation bullet records the message                                                                                                                    |
| The plugin's fetch tasks cannot be retried by a repo-side wrapper                                               | the plugin's tasks are its own task types, so a repo-side wrapper cannot re-run another task's action; the repository's retry runs inside a repo-authored task action (`build-logic/src/main/kotlin/AbstractHelmRepositoriesTask.kt`)                                                                                                      |
| The strict lint rejects a chart whose dependency directory is empty                                             | `AGENTS.md`'s chart-validation bullet: "the strict lint fails with 'chart directory is missing these dependencies: common'"                                                                                                                                                                                                                |
| The chart carries no `.helmignore`                                                                              | `find helm/chart -name .helmignore` returns nothing, so nothing strips `charts/` from the packaging input                                                                                                                                                                                                                                  |
| The plugin's helm tasks are not cacheable                                                                       | `./gradlew :helm:chart:helmPackageMainChart --info` reports caching disabled for the helm tasks (the plugin's issue #208)                                                                                                                                                                                                                  |
| The dependency fetch is skipped under the configuration cache                                                   | `helm/chart/build.gradle.kts`'s `setOnlyIf { true }` workaround; `AGENTS.md`'s chart-validation bullet; the configuration cache landed in #581 (`gh pr view 581 --json title`)                                                                                                                                                             |
| The chart declares the Bitnami `common` dependency                                                              | `helm/chart/src/main/helm/Chart.yaml`; the `showcase/deployment/helm-chart` spec's "Chart metadata" scenario                                                                                                                                                                                                                               |
| `code-quality`'s control requirement is scoped to a gate whose decision the repository authors                  | `showcase/quality/code-quality`'s "A check's control is a committed test", whose scenarios name a rules object or a gate working inline                                                                                                                                                                                                    |
| The smoke is nightly and covers the install, not the pull-request gate                                          | `.github/workflows/deployment-smoke.yml`'s `schedule` (`cron: '45 20 * * *'`); `AGENTS.md`'s deployment-smoke note                                                                                                                                                                                                                         |
| The `helm-chart` Purpose enumerates the build-side content the capability covers                                | `openspec/specs/showcase/deployment/helm-chart/spec.md`'s `## Purpose` ("the Gradle Helm-release configuration the chart is deployed through (each release's namespace and the per-target kube context)")                                                                                                                                  |
| The parked idea is removed by this change                                                                       | `docs/ideas.md`'s "Give the `build` gate a chart-packaging path" entry at the change's base (`git show origin/main:docs/ideas.md`)                                                                                                                                                                                                         |

### Dismissed scan candidates

`scripts/claim-scan.py --base origin/main` over this change's own diff lists candidates for all four kinds. Each
candidate not given a row above is dismissed as part of its kind, listed here with the reason. The run is deliberately
not quoted as a count: the diff grows while the change is in flight, so a frozen figure would be falsified by the next
edit.

- **`path`** — the change's own artifact paths and the repository paths its claims-table rows cite, the commands the
  tasks run, the probe's build-directory paths, and the parked `docs/ideas.md` entry's plugin-task and helper names.
- **`count`** — the task and section ids in `tasks.md`, the ticked tasks' evidence figures (the check's result and
  duration, the requirement-description length, the validation totals, the over-120 count, and the capture's applied
  `AGENTS.md` numstat delta), the requirement's scenario numbering, and the round numbers in `docs/review-rounds.tsv`'s
  added rows.
- **`absolute`** — the quantifiers sit in the change's own normative text, its implementation prose, its decisions, and
  the parked idea's entry ("never runs on a pull request", "runs on every pull request", "packages the chart"), whose
  facts are rows above (the `check` dry-run row and the packaging-paths row).
- **`cross-ref`** — `#581` and the plugin's `#208`, each named in a claims-table row's source.
