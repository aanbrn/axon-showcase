# Proposal: Wire nginx stub_status metrics for the web UI

## Why

The web-UI container's nginx exposes `stub_status` on a dedicated stub-status port (`9090`), and the chart ships a
ServiceMonitor that scrapes the `http-metrics` port at `/metrics`. But no converter is wired: nginx serves plaintext
`/stub_status` there, not Prometheus-format `/metrics`, so the Prometheus target is **down** (404). This surfaced in
the kps bump smoke test — every service target is up except `axon-showcase-web-ui`. The `webUi.sidecars` value is
empty, so the intended NGINX Prometheus Exporter sidecar (documented in the deployable-UI change as configurable) is
never deployed.

## What Changes

- Add a **NGINX Prometheus Exporter sidecar** to the web-UI deployment, rendered **explicitly in the deployment
  template** and gated by the same condition as the web-UI ServiceMonitor
  (`observability.prometheus.metrics.export.enabled` + `webUi.serviceMonitor.enabled`) — so the exporter is only
  deployed when metrics are actually scraped, and the generic `webUi.sidecars` escape hatch stays operator-owned.
  The exporter scrapes nginx stub_status (`--nginx.scrape-uri=http://127.0.0.1:9090/stub_status`) and serves
  Prometheus-format metrics on its own port (9113), matching what the ServiceMonitor scrapes.
- Point the web-UI Service's `http-metrics` `targetPort` at the exporter's port (9113), so the ServiceMonitor reaches
  the exporter rather than nginx's plaintext stub_status.
- Source the exporter image as a concrete catalog coordinate (per the infra-image-versions spec: every image is
  single-sourced, no hard-coded version in values).
- Verify the fix: deploy the stack, confirm the `axon-showcase-web-ui` Prometheus target is **up** and nginx
  stub_status metrics appear.

## Capabilities

### New Capabilities

- None (the web-UI deployable capability already covers the ServiceMonitor; this change hardens it).

### Modified Capabilities

- `showcase/deployment/web-ui` — the web-UI Deployment gains the exporter sidecar (gated by the observability
  flags, wired through a new `webUi.metricsExporter` value), so the nginx metrics target is actually scrapable.
- `showcase/quality/infra-image-versions` — the exporter image gets a catalog coordinate.

## Impact

- **Build**: `gradle/libs.versions.toml` — add the `nginx-prometheus-exporter` image coordinate (a concrete version).
- **Helm**: `helm/chart` — add a `webUi.metricsExporter` value (image + port) and render the exporter sidecar
  explicitly in the deployment template, gated by the observability flags; point the Service `http-metrics`
  `targetPort` at the exporter port; `values-local.yaml` keeps it enabled; lint value files cover the new branch.
- **Docs**: `AGENTS.md` / `README.md` — note the web-UI metrics target is now up (remove/soften any "excluded" note).
- **Behavior**: the web-UI nginx metrics appear in Prometheus/Grafana, matching the other services.