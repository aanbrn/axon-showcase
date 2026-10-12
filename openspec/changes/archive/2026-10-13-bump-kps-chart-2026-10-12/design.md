# Design

## Context

See `proposal.md` — Why. The chart is pinned in the version catalog (`prometheus-community-stack`) and read by the `kps`
release in `build.gradle.kts` (`from("prometheus-community/kube-prometheus-stack")`,
`version = libs.versions.prometheus.community.stack`, namespace `monitoring`, `mustUninstallAfter` `tempo` and the app
and infra releases — `axon-showcase-db-events`, `axon-showcase-kafka`, `axon-showcase-os-views`, `axon-showcase`). The
local values (`helm/values/kps/values-local.yaml`) disable Alertmanager and node-exporter, keep kube-state-metrics, give
Grafana an ingress plus a Tempo datasource, and size Prometheus/Grafana for a laptop.

Two facts drive the verification shape:

- **The observability majors are actionable by design.** The Bitnami infra majors are suppressed because a major chart
  ships a new preconfigured `image.tag` that diverges from the test-surface pins; the observability charts carry no such
  tag, so nothing suppresses them and nothing else checks they deploy — `verifyInfraImageVersions` covers only the
  Bitnami charts.
- **`verifyInstallCommands` couples the catalog to `AGENTS.md`.** The manual `helm install … --version` commands in the
  Kubernetes Deployment section are asserted against the catalog pins, so a catalog-only bump fails `check`; the two
  move in one edit.

## Goals / Non-Goals

**Goals:**

- Move the chart and its documented manual command together, keeping `check` green.
- Prove the bumped chart deploys: install `kps` from it and smoke the signal the observability stack exists for (pods
  ready, Prometheus scraping its targets, Grafana's datasources wired).

**Non-Goals:**

- Touching `config/helm-updates/major-disabled.txt` — this row is not suppressed, and adding it would hide an actionable
  upgrade.
- Upgrading Tempo (`grafana-tempo`) — a separate chart, with its own row if it has one.
- Editing the chart or values for the new major: the upgrade is a version change unless the install shows otherwise.

## Decisions

**Verify by live install + smoke, not by `helm lint` or a template render.** A chart major can change CRDs, default
values, and subchart wiring in ways a render does not execute — the smoke checks the deployed signal (targets up,
datasources wired), which is what the capability promises.

**Take the major rather than pinning back.** Nothing in this repo depends on the old chart's internals; the chart's
values surface this repo sets (Grafana ingress/datasources, Prometheus resources, disabled components) is small and
version-agnostic, so the risk is the upgrade itself — verified by the install.

## Risks / Trade-offs

- A chart major can rename or restructure values → the install with the existing values is the test; a failure here is a
  values migration, not a reason to pin back.
- The live install is on the local cluster (colima), which is host state → the smoke checks the deployed resources it
  installs, and the cluster is disposable.
- The upgrade replaces the running `monitoring` release → acceptable locally; the deployed cluster is disposable too.

## Migration Plan

Install the new chart over the existing `kps` release (or uninstall/reinstall); no data migration — the release carries
configuration, not state this repo persists.
