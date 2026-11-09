# Design

## Context

The change is one catalog coordinate whose real check is a live install: the chart is a third-party observability stack
whose templates no local test exercises, and `verifyInfraImageVersions` covers only the Bitnami infra charts.

The target is not the check's newest row. Chart `93.1.0` moves the Prometheus Operator to `v0.95.1`, and the
`prometheus-operator`, `prometheus-config-reloader` and `admission-webhook` `v0.95.1` images each publish a
`linux/amd64`-only index, while the local colima/k3s cluster is `linux/arm64`. The live upgrade of `93.1.0` therefore
fails at the operator pod (`ErrImagePull`, then `ImagePullBackOff`: "no matching manifest for linux/arm64/v8") after
installing the rest of the chart. The `v0.94.2` images are multi-arch (`amd64`, `arm`, `arm64`, `ppc64le`, `s390x`), and
the newest `92.x` (`92.22.0`) carries the same subchart versions as `93.1.0` with that operator, so the reachable update
is `92.22.0` and the `93.x` line is held.

The `93.1.0` attempt is the record for that failure. Its upgrade reached revision 3 on `kube-prometheus-stack-93.1.0`
with the Grafana, kube-state-metrics and Prometheus pods Ready, the operator pod in `ErrImagePull` and then
`ImagePullBackOff`, and the kubelet reporting, verbatim:

```text
Failed to pull image "quay.io/prometheus-operator/prometheus-operator:v0.95.1": Error response from daemon: no matching manifest for linux/arm64/v8 in the manifest list entries
```

The rollback to revision 4 on `92.17.0`, with the `v0.94.2` CRDs re-applied server-side, restored the stack.

A previous bump recorded that a hand-run `helm search repo` named an older version than the check's fresh run, because
`helm search repo` reads the local index. The check's run (`helmUpdates`) drives the Gradle plugin's Helm client, whose
repository file lives under `build/helm/`, so it refreshes that client's index and reports the newest release (the held
`93.1.0` row) without touching the host CLI's index. The `92.x` target is therefore read from the host's `--versions`
listing after `helm repo update prometheus-community` refreshes it (task 1.1, as task 2.3 also does).

The local cluster carried the release `bump-kps-chart-2026-11-01` installed, chart `92.17.0` (app `v0.94.2`), so this
unit's install is an upgrade of a running release rather than a fresh install. That state is read as a baseline before
acting (task 2.4): the chart and revision, pod readiness, the active Prometheus targets, Grafana's datasources, and the
operator's state. If the release is absent, the same install task creates it; the conditions that stop the plan for the
owner's decision are task 2.5's.

The chart's dependencies move: kube-state-metrics `8.8.0` → `9.0.0` and prometheus-node-exporter `4.60.1` → `5.0.0`
cross a major, while grafana moves `14.3.0` → `14.4.0` and prometheus-windows-exporter `0.12.*` → `0.14.0`. The local
values enable kube-state-metrics and grafana (node-exporter, alertmanager and windows monitoring are disabled or
absent), and set resources on both plus the operator and Prometheus. The operator version itself does not move, so no
CRD apply is owed.

## Goals / Non-Goals

**Goals:**

- Move the coordinate to the newest reachable `92.x` and keep the documented install command in step with it.
- Verify the bumped chart by upgrading the running local release and reading the deployed signal against a baseline.
- Record the `93.x` hold and its return condition where a later unit will read it, and keep the row visible in the
  update report.

**Non-Goals:**

- Any other chart or coordinate: a row another update check reports is its own unit.
- Suppressing the `93.x` row: the spec requires an observability chart's update to stay actionable.
- Pre-emptively rewriting the values: one is changed only where the render or the smoke shows the new chart does not
  read it.
- Freezing the run image, or changing the application chart's own dependency.

## Decisions

1. **Resolve the held row and the `92.x` target from refreshed indexes, not a stale one.** The two reads use different
   Helm clients, so each refreshes its own:
   - the check's own run refreshes the Gradle plugin client's index (under `build/helm/`) and reports the newest row,
     `93.1.0`;
   - the `92.x` target is read from the host CLI's `--versions` listing after `helm repo update prometheus-community`.
   - Alternative rejected: a hand search without the refresh — the prior bump recorded it naming an older release than
     the check did.
   - Evidence: the check's run reported `92.17.0 -> 93.1.0` on 2026-11-09, and the refreshed host listing lists
     `92.22.0` first among the `92.x` line.
2. **Take `92.22.0`, not `93.1.0`, because `93.1.0`'s operator images are amd64-only.**
   - Evidence: the quay image indexes for the operator, config-reloader and admission-webhook `v0.95.1` carry only
     `linux/amd64`; `v0.94.2` carries `arm64`, and the local cluster is arm64.
   - Alternative rejected: keep `93.1.0` and smoke it on an amd64 environment — the spec's obligation would be met, but
     the deployment this repository's owner actually runs would stay uninstallable, and the local path would remain
     unverified.
   - Alternative rejected: hold the whole bump at `92.17.0` — the newest `92.x` carries the subchart updates now, with
     an operator that runs on both architectures.
3. **Apply no CRDs.** The chart's operator stays `v0.94.2`, so the CRD schemas do not move.
   - The `93.x` return will need the `v0.95.1` CRDs applied server-side when the arm64 images land; the parked idea
     records it.
4. **Keep the values files as they are, and prove each override still lands.** The values set resources on
   kube-state-metrics, grafana, grafana's sidecar, the operator, the operator's config-reloader and Prometheus, a Tempo
   datasource and an ingress host on grafana, and disable several features — its `false` values on the enable and
   self-monitoring keys are that set; kube-state-metrics crosses a major and grafana a minor, so those paths are exactly
   what the bump can break.
   - Alternative rejected: pre-emptively rewriting values for the new subchart versions would change behavior the smoke
     is meant to observe.
   - The evidence is the migration notes of the exact new subchart versions, the rendered resources carrying the file's
     values and rendering nothing for every key it disables (task 2.3), and the deployed signal compared against the
     pre-upgrade baseline (task 2.8) — not the render's exit status.
   - A migration note that requires a values change is one of task 2.5's stop conditions, since applying it would change
     behavior the bump is not authorized to change.
5. **Keep the `93.x` row visible and park the hold in `docs/ideas.md` with a verifiable return condition.** The report
   will read `92.22.0 -> 93.1.0` after the bump, which is the truth: a newer release exists and cannot be taken here.
   - Alternative rejected: adding `prometheus-community-stack` to `config/helm-updates/major-disabled.txt` — that list
     suppresses every major of the named chart, so it would hide future majors too, and the spec requires an
     observability chart's update to stay actionable.
   - The return condition is checkable: the operator line's image index carries `linux/arm64`.

## Risks / Trade-offs

- **The result is a partial update**: `92.22.0` is not the newest release, and the report keeps one row. That is a
  recorded hold, not a cleared finding, and the idea names the condition that reopens it.
- **A chart bump can change templates the local values rely on, and `helm template` does not fail on a key the new chart
  no longer reads.** The chart ships no `values.schema.json`, and Grafana's own schema sets no
  `additionalProperties: false`, so Helm merges an unknown key silently. The check is therefore a comparison of the
  rendered resources against the values the file sets, plus the deployed signal; a render that merely exits zero proves
  nothing about the overrides.
- **The smoke is not hermetic**: it upgrades a release the local cluster already carries, so the recorded revision and
  chart version are what make the run attributable. A pre-existing fault on that cluster is indistinguishable from a
  regression unless the state before the bump is read, which is why the baseline includes readiness, the active target
  set and the datasources rather than the release version alone.
- **Any condition task 2.5 names stops the plan** for the owner's decision, rather than being absorbed into the bump.
- **Grafana's API needs authentication**: the chart generates an admin password into the `kps-grafana` secret and
  enables no anonymous access, so the datasource check reads the credentials from that secret.

## Verification

- `./gradlew helmUpdates` after the bump — the report names `92.22.0 -> 93.1.0`, whose left side is the new pin, and the
  old `92.17.0` no longer appears.
- The migration notes of the exact new subchart versions, read before the render.
- `helm template` of the pinned chart with `helm/values/kps/values-local.yaml`, inspected for the overrides the file
  sets and for every key it disables rendering nothing.
- The pre-upgrade baseline (chart and revision, pod readiness, active targets with their names, datasources, operator
  state); then `./gradlew helmInstallKpsToLocal` — the release upgrades or installs; no CRD step is owed.
- The deployed signal compared against that baseline: `helm list -n monitoring`, `kubectl get pods -n monitoring`
  (readiness), Prometheus's `/api/v1/targets` (non-empty, loses none of the baseline's named targets), and Grafana's
  `/api/datasources` and `/api/search` with the admin credentials from the `kps-grafana` secret.
- `./gradlew verifyInstallCommands` — the documented command matches the catalog pin, with a known-bad control.
- `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` — the pull-request gate.
- `./gradlew spotlessApply`/`spotlessCheck` and `openspec validate --all`.

## Claims

The base revision for the rows about the state before the change is `7900ec42` (`git merge-base HEAD origin/main`, after
the branch refresh that brought in #877 and #878).

| Claim                                    | Source                                                                                                                                                                                                                                 |
| ---------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The finding's row                        | `gh issue view 40` — body `prometheus-community-stack: 92.17.0 -> 93.0.0`, updated 2026-11-07                                                                                                                                          |
| The chart's resolved target              | task 1.1's `./gradlew helmUpdates` run — the row reads `92.17.0 -> 93.1.0` — and the host CLI's refreshed `helm search repo … --versions` listing, whose newest `92.x` is `92.22.0`                                                    |
| One row stays reported after the bump    | task 2.1's report reads `92.22.0 -> 93.1.0` — the held row, whose left side is the new pin, with `92.17.0` gone                                                                                                                        |
| The `93.x` blocker                       | the quay image indexes of `prometheus-operator/prometheus-operator:v0.95.1`, `prometheus-config-reloader:v0.95.1` and `admission-webhook:v0.95.1` list only `linux/amd64`; `v0.94.2` lists `amd64`, `arm`, `arm64`, `ppc64le`, `s390x` |
| The `93.x` chart carries that operator   | `kube-prometheus-stack` `93.0.0` and `93.1.0` `Chart.yaml` `appVersion: v0.95.1`                                                                                                                                                       |
| The `93.1.0` arm64 upgrade fails         | the Context's `93.1.0` attempt paragraph and task 2.7's `92.22.0` success around it — revision 3, both pod states, and the verbatim kubelet line                                                                                       |
| The operator is unchanged                | `92.17.0` and `92.22.0` both carry `appVersion: v0.94.2`                                                                                                                                                                               |
| The CRD manifests do not move            | the `charts/kube-prometheus-stack/charts/crds` tree of the `92.17.0` and `92.22.0` tags is identical (every entry's blob SHA matches)                                                                                                  |
| The subcharts move                       | the `dependencies` blocks of the `92.17.0` and `92.22.0` `Chart.yaml` tags (kube-state-metrics 8.8.0 → 9.0.0, node-exporter 4.60.1 → 5.0.0, grafana 14.3.0 → 14.4.0, windows-exporter 0.12.* → 0.14.0)                                 |
| The Helm CLI pin is current              | `gh api repos/helm/helm/releases/latest` → `v4.3.1`, equal to the catalog's `helm` pin                                                                                                                                                 |
| A render does not reject an unknown key  | the pulled chart tree has no `values.schema.json`, and `grafana-14.4.0`'s schema sets no `additionalProperties: false`                                                                                                                 |
| The values' overrides land in the render | `helm template … --version 92.22.0 -f helm/values/kps/values-local.yaml` → the rendered requests, limits, Tempo datasource, and Grafana ingress host, with every key the file disables absent                                          |
| Grafana's credentials are generated      | the rendered Grafana Deployment's `GF_SECURITY_ADMIN_PASSWORD` reads the `kps-grafana` secret                                                                                                                                          |
| The pre-upgrade baseline                 | task 2.4's reads on the shared context — chart and revision, pod readiness, active targets with their names, datasources, operator state                                                                                               |
| The documented command matches           | `./gradlew verifyInstallCommands` → BUILD SUCCESSFUL                                                                                                                                                                                   |
| The gate reads the edited file           | the same task fails on `--version 92.17.0`, naming the catalog's `92.22.0`                                                                                                                                                             |
| The release upgraded                     | `helm list -n monitoring` → `kps` at `kube-prometheus-stack-92.22.0`                                                                                                                                                                   |
| The monitoring pods are ready            | `kubectl get pods -n monitoring` → every pod Running and Ready                                                                                                                                                                         |
| Prometheus targets are preserved         | `/api/v1/targets?state=active` → the set is non-empty and loses none of the baseline's named targets                                                                                                                                   |
| Grafana reads its datasources            | `/api/datasources` → Prometheus and Tempo; `/api/search` → `Axon Showcase`                                                                                                                                                             |
| The old pin is in no live doc            | `git grep -n '92\.17\.0' -- . ':!openspec/changes' ':!docs/units'` exits 1                                                                                                                                                             |

**Dismissals.** `scripts/claim-scan.py --base origin/main` emits candidate shapes — `count`, `absolute`, `cross-ref` and
`path` — and each is a lead rather than a claim: a candidate is a claim only where a row states it, and the rest are
pointers a reader resolves against the repository.
