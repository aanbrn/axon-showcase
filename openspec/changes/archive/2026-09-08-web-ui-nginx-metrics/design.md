## Context

The web-UI ServiceMonitor scrapes the `http-metrics` port (9090) at `/metrics`, but nginx serves plaintext
`/stub_status` there. The deployment already renders a generic `webUi.sidecars` list (line 141), and the metrics port
is a named container port (`http-metrics`). The deployable-UI change documented the exporter as "configurable via
`webUi.sidecars`" but left it empty. The kps smoke test confirmed the target is down (404).

## Goals / Non-Goals

**Goals:**
- Get the `axon-showcase-web-ui` Prometheus target **up** by wiring an NGINX Prometheus Exporter that converts
  `/stub_status` → `/metrics`.
- Source the exporter image from the catalog (concrete version, per infra-image-versions).

**Non-Goals:**
- No change to the nginx stub_status listener itself (it works).
- No change to the ServiceMonitor scrape config (it already targets `/metrics` on `http-metrics`).

## Decisions

### D1: Wire the exporter as an explicit, observability-gated sidecar

Rather than adding a default entry to the generic `webUi.sidecars` list (which would deploy the exporter even when
observability is disabled, and couples a purpose-built metrics container to an operator escape hatch), render the
exporter **explicitly in the deployment template**, gated by the same condition as the web-UI ServiceMonitor:

```yaml
{{- if and .Values.observability.prometheus.metrics.export.enabled .Values.webUi.serviceMonitor.enabled }}
- name: nginx-exporter
  image: {{ include "common.images.image" (dict "imageRoot" .Values.webUi.metricsExporter.image \
      "global" .Values.global) }}
  args:
    - --nginx.scrape-uri=http://127.0.0.1:{{ .Values.webUi.containerPorts.stubStatus }}/stub_status
  ports:
    - name: http-metrics
      containerPort: {{ .Values.webUi.metricsExporter.port }}
{{- end }}
```

The exporter and nginx share the pod (same network namespace), so it reaches nginx's stub_status listener (the
stub-status port, 9090 — `containerPorts.stubStatus`, set by `BP_NGINX_STUB_STATUS_PORT`) via `127.0.0.1`. The
exporter serves Prometheus-format `/metrics` on its own port (default 9113).

**Port mapping:** the Service's `http-metrics` port must reach the exporter's `/metrics`, not nginx's stub_status
(which is plaintext). So the Service `http-metrics` `targetPort` points at the exporter's port (9113), and the main
container's stub_status listener is renamed `stub-status` (pod-internal, scraped only by the exporter over
`127.0.0.1`). When observability is off, the exporter is absent and the `http-metrics` service port is unused (no
ServiceMonitor scrapes it anyway). The web-UI NetworkPolicy's metrics ingress opens the exporter port (9113) to
monitoring peers.

### D2: Catalog coordinate for the exporter image

Add `nginx-prometheus-exporter` (e.g. `nginx/nginx-prometheus-exporter:1.4.0`) to `gradle/libs.versions.toml` and
reference it from the chart values (`webUi.metricsExporter.image`), keeping the image single-sourced and concrete.

## Risks / Trade-offs

- **Port mapping confusion** → the stub_status listener (nginx 9090) and the exporter's `/metrics` listener must be
  distinct; the Service `http-metrics` port maps to the exporter. Verified by the smoke test (target up).
- **Exporter image availability** → must be a real, maintained image (`nginx/nginx-prometheus-exporter`). If it
  cannot be sourced, fall back to a metric-relabeling scrape of `/stub_status` (documented alternative).