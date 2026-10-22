# Design

## Context

The pin is one coordinate in the version catalog, but the verification is a live install: the chart is a third-party
observability stack whose templates are not exercised by any local test, and `verifyInfraImageVersions` deliberately
covers only the bitnami infra charts (it gates their preconfigured `image.tag` against the test-surface `*-image-tag`
pins, which the observability charts do not have). So the catalog pin, the `helmUpdates` report and the documented
command are the static gates, and the release itself is the real check.

A previous bump recorded that a hand-run `helm search repo` named an older version than the check's fresh run, because
`helm search repo` reads the local index. That is why the target is taken from `./gradlew helmUpdates` here.

## Goals / Non-Goals

**Goals:**

- Move the coordinate to the resolved version and keep the documented install command in step with it.
- Verify the bumped chart by installing it and reading the deployed signal: pods ready, Prometheus targets up, Grafana
  datasources wired.

**Non-Goals:**

- Any other chart or coordinate: a row another update check reports is its own unit.
- Freezing or changing the run image, or the application chart's own dependency.

## Decisions

1. **Re-resolve with the check, not a hand search.** `helmUpdates` refreshes the repositories before it searches, so its
   row is the current one; a hand-run `helm search repo` reads whatever index is on disk.
2. **Install the bumped chart into the running local release.** It is an upgrade rather than a fresh install, which is
   what a real deployment does and what makes the smoke meaningful; the release's revision and chart version are
   recorded as the evidence.
3. **Read the deployed signal, not the config.** A green `check` and a correct catalog value prove the configuration;
   the verification reads the pods' readiness, Prometheus's own target list, and Grafana's datasources.

## Risks / Trade-offs

- **A minor chart bump can still change templates.** The live install is what covers that; the static gates cannot.
- **The smoke runs against the local cluster**, so it is not a hermetic check: the release it upgraded was already
  installed there. The recorded revision and chart version are what make the run attributable.

## Verification

- `./gradlew helmUpdates` after the bump — "No Helm updates available."
- `./gradlew verifyInstallCommands` — the documented command matches the catalog pin.
- `./gradlew helmInstallKpsToLocal` — the release upgrades to the new chart version.
- The deployed signal: `helm list -n monitoring` (revision and chart), `kubectl get pods -n monitoring` (readiness),
  Prometheus's `/api/v1/targets` (up/down counts), and Grafana's `/api/datasources`.
- `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`.

## Claims

| Claim                                          | Source                                                                                                                         |
| ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| The chart's resolved target                    | `./gradlew helmUpdates` (refreshes the repositories, then searches) — `92.9.0 -> 92.11.0`                                      |
| The check reports nothing after the bump       | `./gradlew helmUpdates` → "No Helm updates available."                                                                         |
| The documented command matches the catalog pin | `./gradlew verifyInstallCommands`                                                                                              |
| The release upgraded to the new chart version  | `helm list -n monitoring` → `kps` revision 3, `kube-prometheus-stack-92.11.0`                                                  |
| The monitoring pods are ready                  | `kubectl get pods -n monitoring` → Grafana 3/3, Prometheus 2/2, operator, kube-state-metrics and Tempo 1/1, all Running        |
| Prometheus reports its targets up              | `kubectl get --raw /api/v1/namespaces/monitoring/services/kps-prometheus:9090/proxy/api/v1/targets` → 14 active, 14 up, 0 down |
| Grafana lists its datasources                  | `kubectl port-forward -n monitoring svc/kps-grafana 3001:80` then `/api/datasources` → Prometheus and Tempo                    |
| The old pin survives only in dated records     | `git grep -n 92\.9\.0 -- ':!openspec/changes'` → no live document names it; the tracked sites are dated records                |

**Dismissals.** `scripts/claim-scan.py --base origin/main` fires on shape rather than on claimhood — paths and command
invocations, numbers, words like `only`/`every`/`never`, and repository-path/`#NNN` references. A candidate is a claim
only where a claims row states it; the rest are pointers a reader resolves against the repository.
