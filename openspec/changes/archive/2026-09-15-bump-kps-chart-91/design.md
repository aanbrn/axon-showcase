## Context

`gradle/libs.versions.toml` pins `prometheus-community-stack = "90.0.0"`; `build.gradle.kts` reads it into the `kps`
Helm release (`HelmChartUpdateCheck(name = "prometheus-community-stack", ...)`). The Helm update check reports the
coordinate as stale. The observability charts are not in `config/helm-updates/major-disabled.properties` (only the
Bitnami infra charts are), so an observability bump surfaces as actionable.

The open "Helm updates" issue names `90.0.0 -> 91.2.3`, but
`helm search repo prometheus-community/kube-prometheus-stack` now returns `91.4.0` as the latest chart version. `91.2.3`
and `91.4.0` both ship app version `v0.94.0`, so the pin moves to the current latest rather than the issue's
now-superseded snapshot.

## Goals / Non-Goals

**Goals:**

- Move the `prometheus-community-stack` catalog coordinate from `90.0.0` to `91.4.0`.
- Verify the bumped chart renders with the local values and deploys; the observability wiring still works.

**Non-Goals:**

- No chart/values changes to `helm/chart` unless the `91.4.0` chart requires them (the smoke test decides).
- No change to the `*-image-tag` pins or the Bitnami infra charts (unaffected by this bump).

## Decisions

### D1: Take the current latest (91.4.0), not the issue's 91.2.3

The issue is a point-in-time snapshot; the `helmUpdates` check resolves the chart's current latest. `91.2.3` and
`91.4.0` are the same major line and the same app version, so bumping to `91.4.0` is the same class of change and leaves
the update check clean instead of re-reporting a fresh update. Alternative rejected: pinning `91.2.3` exactly would
satisfy the issue text but would immediately surface `91.4.0` as another update.

### D2: Pin bump only, then live smoke test

The change is a single version bump in the catalog. The `kps` release consumes it via `helm.releases`, so no build logic
changes. Verify by rendering the chart at `91.4.0` with `helm/values/kps/values-local.yaml`, then smoke-test the
deployed stack:

- the `kps` release installs cleanly (pods ready: prometheus, grafana, operator);
- the chart's own ServiceMonitors reconcile into **up** Prometheus scrape targets (a chart-only smoke test does not
  deploy the app services, so their ServiceMonitors are not exercised);
- the Grafana datasources (Prometheus, Tempo) are wired.

If `91.4.0` drops or renames a value the local values use, adjust the kps values accordingly — verified by the smoke
test, not assumed.

### D3: Docs update

`AGENTS.md` quotes the `kps` chart version (`--version 90.0.0`); update it to `91.4.0`. `README.md` does not quote the
version, so it needs no edit.

## Risks / Trade-offs

- **Chart API drift** → `91.4.0` may change default values or remove deprecated ones. Mitigation: the smoke test
  exercises the real deployment; any needed values adjustments are captured in this change.
- **Unrelated dashboard/metrics churn** → the new chart may alter bundled dashboards or alert rules. Mitigation: out of
  scope unless the smoke test shows a break; note any visible change in the change report.
