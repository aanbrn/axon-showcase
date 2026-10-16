# Design

## Context

See `proposal.md` — Why. Two rows from the `helmUpdates` check, both re-resolved before bumping:

- `helm 4.3.0 -> 4.3.1` — `gh api repos/helm/helm/releases/latest` reports `v4.3.1`. The pin is the Gradle Helm plugin's
  client version (`helm-conventions.gradle.kts:12`, `version = libs.versions.helm.asProvider().get()`), so the plugin
  downloads that client under `.gradle/helm/client`. No workflow pins the CLI separately (grep), so the catalog is its
  only surface.
- `prometheus-community-stack 92.3.0 -> 92.9.0` — the run's chart search reported `92.3.0` as newest before
  `helm repo update prometheus-community` and `92.9.0` after it, matching the check's report. The stale local index is
  the case the 2026-10-13 kps bump recorded; the index has since advanced, so that "before" reading is the run's record
  rather than a current state.

Both land in one unit because one live install exercises both: the plugin uses the new client to install the new chart.

## Goals / Non-Goals

**Goals:**

- Take both check rows to their re-resolved latest, and keep the documented install command in step with the chart.
- Verify with what actually proves a chart bump — a live install and a smoke — rather than the config alone.

**Non-Goals:**

- The Paketo findings, which are a separate unit.
- `grafana-tempo` (`1.24.4`): the report names no newer version, so it stays.

## Decisions

**Both rows in one unit.** They are the same check's rows, and the live install that verifies the chart also exercises
the client, so a second unit would re-run one smoke for nothing.

**`skip_specs: true`.** `showcase/quality/infra-image-versions` already names the observability charts among the
coordinates that must be concrete and carries the "actionable and smoke-tested" scenario, and states no version this
change makes stale — so this change satisfies that wording rather than altering it, and no delta is owed. The argument
is stated in the proposal rather than left as "no capability covers it".

**Verify by the live install, since no CI job installs the observability stack.** The `ci` release target excludes the
observability tag, so the local smoke's record is this change's evidence (the runner-hosted `kind` route `AGENTS.md`
documents stays available if a CI check is wanted).

## Risks / Trade-offs

- **A stale index makes the "latest" wrong** → both targets were re-resolved from the tools' own lookups, and the chart
  row was confirmed only after `helm repo update`.
- **A chart bump can deploy but miswire the stack** → the smoke checks pods ready, Prometheus targets up, and Grafana's
  datasources wired, which the pin alone cannot show.
- **The documented command drifts** → `verifyInstallCommands` (in `check`) fails on exactly that, so the catalog and the
  `AGENTS.md` command move together.

## Migration Plan

None: version values only, taking effect on the next install.

## Claims table

| Claim                                                         | Source                                                                                                                                                                                                                                                                                                            |
| ------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The check reports these two rows                              | this change's own `./gradlew helmUpdates` run before the bump (`helm: 4.3.0 -> 4.3.1`, `prometheus-community-stack: 92.3.0 -> 92.9.0`) and after it (`No Helm updates available.`) — the run's output, not the tracker issue, which still carries the previous unit's rows until the weekly workflow refreshes it |
| The CLI's latest is `4.3.1`                                   | `gh api repos/helm/helm/releases/latest --jq .tag_name` → `v4.3.1`                                                                                                                                                                                                                                                |
| The chart's latest is `92.9.0`, and the run's index was stale | the run's own `helm search repo prometheus-community/kube-prometheus-stack --versions` output before the repo update (top row `92.3.0`) and after (`92.9.0`)                                                                                                                                                      |
| The stale-index case is the 2026-10-13 kps bump's             | `openspec/changes/archive/2026-10-13-bump-kps-chart-2026-10-12/proposal.md` (the same chart and the same stale-index correction)                                                                                                                                                                                  |
| The CLI pin is the plugin's client version                    | `build-logic/src/main/kotlin/helm-conventions.gradle.kts:12` reads `libs.versions.helm`                                                                                                                                                                                                                           |
| No other surface pins the CLI                                 | `grep -rn helm .github/workflows/*.yml` finds no CLI version pin (only `helm/kind-action` and the plugin's own client path)                                                                                                                                                                                       |
| The documented command is asserted against the catalog        | `infra-image-versions`'s scenario "The AGENTS.md manual install commands match the catalog", and `./gradlew verifyInstallCommands`                                                                                                                                                                                |
| No CI job installs the observability stack                    | the `ci` release target's tag selection (the deployment-smoke note in `AGENTS.md`)                                                                                                                                                                                                                                |
