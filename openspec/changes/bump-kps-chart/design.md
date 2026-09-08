## Context

`gradle/libs.versions.toml` pins `prometheus-community-stack = "88.6.2"`; `build.gradle.kts` reads it into the
`kps` Helm release (`HelmChartUpdateCheck(name = "prometheus-community-stack", ...)`). The Helm update check reports
`90.0.0` available. The observability charts are not in `config/helm-updates/major-disabled.properties` (only the
bitnami infra charts are), so a major observability bump surfaces as actionable.

## Goals / Non-Goals

**Goals:**
- Move the `prometheus-community-stack` catalog coordinate from `88.6.2` to `90.0.0`.
- Verify the bumped chart deploys and the observability wiring still works on the local cluster.

**Non-Goals:**
- No chart/values changes to `helm/chart` unless the `90.0.0` chart requires them (the smoke test decides).
- No change to the `*-image-tag` pins or the bitnami infra charts (unaffected by this bump).

## Decisions

### D1: Pin bump only, then live smoke test

The change is a single version bump in the catalog. The `kps` release consumes it via `helm.releases`, so no build
logic changes. After bumping, run `./gradlew helmInstallKpsToLocal` (or a full `helmInstallToLocal`) and smoke-test:

- the `kps` release installs cleanly (pods ready: prometheus, alertmanager, grafana, operator);
- the services' ServiceMonitors (including the web-UI `http-metrics`) show as **up** Prometheus targets;
- the Grafana datasources (Prometheus, Tempo) are wired.

If the `90.0.0` chart drops or renames a value the local values use (e.g. a serviceMonitor/dashboard key), adjust
`helm/values/axon-showcase/values-local.yaml` accordingly — verified by the smoke test, not assumed.

### D2: Docs update

If `AGENTS.md` / `README.md` quote the `kps` chart version (`--version 88.6.2`), update them to `90.0.0`; otherwise
no doc change.

## Risks / Trade-offs

- **Major chart API drift** → `90.0.0` may change default values or remove deprecated ones. Mitigation: the smoke
  test exercises the real deployment; any needed values adjustments are captured in this change.
- **Unrelated dashboard/metrics churn** → the new chart may alter bundled dashboards or alert rules. Mitigation:
  out of scope unless the smoke test shows a break; note any visible changes in the change report.