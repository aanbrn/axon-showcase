# Proposal

## Why

The **Axon Showcase** Grafana dashboard covers the four backend services and the command bus, but not the web UI — even
though its nginx metrics are already exposed and scraped into Prometheus (the `webUi.serviceMonitor` is enabled in the
local values and the `nginx-exporter` sidecar is wired). The README's "31 panels across 5 sections covering every
service" therefore over-reads: the standalone UI is the one deployed component with no presence on the dashboard.

## What Changes

- Add a **Web UI** row and a small set of nginx panels (request throughput, active connections, connection states, and
  accepted/handled connections) to `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`, selecting the
  metrics by the labels the web UI's ServiceMonitor scrape carries.
- Correct the README's dashboard claim in `README.md` (the `31 panels across 5 sections covering every service …`
  sentence) to the derived section/panel counts and the now-accurate coverage wording.
- Update the `AGENTS.md` worked example that cites the dashboard's raw and real panel counts (`36 panels` / 5 rows /
  `31 panels`) so it stays true to the file.
- Remove the implemented idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. The chart's dashboard requirement (`showcase/deployment/helm-chart`, "Extra deployments and dashboards")
  specifies only that a ConfigMap is rendered per bundled dashboard carrying the `grafana_dashboard: "1"` label with the
  JSON as data — this change adds panels to that same single dashboard file, so the requirement's outcome is unchanged.
  `showcase/deployment/web-ui` owns the nginx `stub_status` → exporter sidecar → ServiceMonitor chain, which already
  exists and is only _consumed_ here. No spec-level behavior changes, hence `skip_specs: true`.

## Impact

- **Files**: `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`, `README.md`, `AGENTS.md`,
  `docs/ideas.md`. Content/docs only. `.opencode/agent/readme-auditor.md` also cites the panel count, as the historical
  README defect the auditor was created to catch, and needs no edit (see design.md D4).
- **Build / tests / services**: no Java, API, dependency, or service change; the chart's templates and values are
  untouched (the metrics and the scrape already exist).
- **Verification**: deployment-only behavior, so it is verified against a live `helmInstallToLocal` install — the new
  panel queries resolve against the web UI's scraped nginx metrics and render in Grafana.
