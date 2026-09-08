## 1. Add the exporter image and wire the gated sidecar

- [x] 1.1 Add the `nginx-prometheus-exporter` image coordinate to `gradle/libs.versions.toml` (a concrete version,
      `nginx-prometheus-exporter = "1.4.0"`), and reference it from the chart values (`webUi.metricsExporter`).
- [x] 1.2 Add a `webUi.metricsExporter` values block (`image`, `port` default 9113) and render the exporter sidecar
      **explicitly in the deployment template**, gated by `and .Values.observability.prometheus.metrics.export.enabled
      .Values.webUi.serviceMonitor.enabled` — it scrapes nginx stub_status
      (`--nginx.scrape-uri=http://127.0.0.1:9090/stub_status`) and serves `/metrics` on the exporter port.
- [x] 1.3 Point the web-UI Service's `http-metrics` `targetPort` at the exporter's port (9113), so the ServiceMonitor's
      `/metrics` scrape reaches the exporter; keep nginx's stub_status listener on 9090 (pod-internal).
- [x] 1.4 Update the lint value files (`helm-lint-full.yaml`, `helm-lint-minimal.yaml`) to cover the new
      `metricsExporter` branch.

## 2. Verify

- [x] 2.1 Run `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal` and confirm lint
      passes.
- [x] 2.2 Deploy the stack (`./gradlew helmInstallToLocal`) and confirm the `axon-showcase-web-ui` Prometheus target
      is **up** and nginx stub_status metrics (e.g. `nginx_connections_active`) appear in Prometheus.
- [x] 2.3 Run `openspec validate --all` and confirm the change passes.