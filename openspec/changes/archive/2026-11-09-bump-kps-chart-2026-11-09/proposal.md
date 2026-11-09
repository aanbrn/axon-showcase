# Proposal

## Why

The Helm update check reports one row, `prometheus-community-stack: 92.17.0 -> 93.1.0`, re-resolved today (issue #40
names `93.0.0`), and the catalog pin is a major release behind. The 93.x line, however, ships Prometheus Operator
`v0.95.1`, whose operator, config-reloader and admission-webhook images are published for `linux/amd64` only, while the
local deployment runs on an arm64 cluster — so 93.x cannot be installed and smoked here. The newest 92.x (`92.22.0`)
carries the same subchart versions with the multi-arch operator `v0.94.2`. `showcase/quality/infra-image-versions` makes
an observability chart's update actionable and requires a live install + smoke test before merge, so the change takes
the reachable 92.x update and holds 93.x with a recorded return condition.

## What Changes

- **`gradle/libs.versions.toml`** — the `prometheus-community-stack` coordinate moves `92.17.0` → `92.22.0`, a concrete
  pin as the catalog's rule requires.
- **`.opencode/skills/deployment-operations/SKILL.md`** — the manual `helm install kps … --version` command in the
  Kubernetes Deployment block moves to `92.22.0`; `verifyInstallCommands` (part of `check`) asserts it against the
  catalog pin, so leaving it would fail the build.
- **`docs/ideas.md`** — a dated entry parks the 93.x hold with its verifiable return condition (an arm64 image for the
  chart's operator line).
- **No other chart coordinate moves:** `grafana-tempo` `1.24.4`, `ingress-nginx` `4.15.1`, and the Bitnami infra charts
  are untouched, because the check reports no row for them.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None (`skip_specs`): the `infra-image-versions` requirement already states exactly what this change does — a concrete
pin, surfaced as actionable rather than suppressed, verified by a live install and smoke test before merge — so the bump
complies with that scenario instead of changing it.

## Impact

- **The local observability release and the documented install path.** The verification is baseline-first — a read of
  the pre-upgrade readiness, Prometheus targets, datasources and operator state, the subcharts' migration notes, a
  render inspected for every override and every disabled key, the upgrade, and a comparison against that baseline — and
  it stops for the owner on the conditions task 2.5 names. The documented command is gated by `verifyInstallCommands`.
- **The Prometheus Operator stays `v0.94.2`**, so the CRD schemas do not move and no CRD apply is owed; 93.x's operator
  bump is what the held row would bring.
- **The chart's own dependencies move:** kube-state-metrics `8.8.0` → `9.0.0` and prometheus-node-exporter `4.60.1` →
  `5.0.0` cross a major, while grafana moves `14.3.0` → `14.4.0` and prometheus-windows-exporter `0.12.*` → `0.14.0`.
  The local values set resources on kube-state-metrics, grafana and the operator and a datasource on grafana, so the
  render is inspected for those overrides; a migration note that requires a values change is one of task 2.5's stop
  conditions.
- **93.x stays visible in the Helm update report** rather than suppressed, and the parked idea records that the pin must
  not follow it until the chart's operator line publishes an arm64 image.
- **No application change:** the chart is third-party, and the application chart's own dependency (the Bitnami `common`
  subchart) is untouched.
- **No test or build-logic change:** `verifyInfraImageVersions` gates only the Bitnami infra charts' `image.tag` drift,
  and the observability charts carry no `*-image-tag`.
