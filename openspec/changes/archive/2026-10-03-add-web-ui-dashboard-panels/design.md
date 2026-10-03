# Design

## Context

See `proposal.md` — Why.

The **Axon Showcase** dashboard is one Grafana JSON file,
`helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`, rendered by
`templates/grafana-dashboards/configmaps.yaml` into a single ConfigMap (labelled `grafana_dashboard: "1"`). It currently
holds five row sections (API Gateway, Command Bus, Command Service, Projection Service, Query Service) plus three
ungrouped top panels (Availability, CPU Usage, Memory Usage): 36 top-level array entries, of which 5 are the empty row
separators — i.e. 31 real panels. None of them queries the web UI.

The web UI is already observable at the Prometheus layer: the chart's `webUi.metricsExporter` runs an
`nginx/nginx-prometheus-exporter` sidecar that converts nginx `stub_status` to Prometheus `/metrics` on the Service's
`http-metrics` port (`webUi.metricsExporter.port`, `9113`), and the `webUi.serviceMonitor` scrapes it — enabled in
`helm/values/axon-showcase/values-local.yaml` alongside `observability.prometheus.metrics.export.enabled`. The dashboard
simply never queries those series, which is why the README's "covering every service" reads loosely.

## Goals / Non-Goals

**Goals:**

- Add a **Web UI** dashboard section built from the nginx series already scraped, so the dashboard covers the standalone
  UI as it covers the backend services.
- Match the existing dashboard's conventions: `timeseries` panels, `datasource` `${DS_PROMETHEUS}`, bottom legends,
  service-name filtering, and grid positions appended after the last section.

**Non-Goals:**

- Adding the web UI to the top CPU/Memory/Availability panels. Those select Spring metrics or
  `pod=~"$application-…-.+"`; extending them is a separate change and does not serve the section's purpose.
- Any chart template, value, or sidecar change — the scrape path already exists and is left untouched.
- Client-side (RUM) web-vitals/JS-error observability (a separate parked idea).

## Decisions

### D1: Select the nginx series by `service="axon-showcase-web-ui"`

Prometheus-operator's ServiceMonitor scrape relabelings attach `namespace`, `service` (the Service name), and `pod` to
scraped series; the dashboard's existing panels filter Spring series by the service name
(`service="axon-showcase-query-service"`, etc.), so `service="axon-showcase-web-ui"` is both correct and consistent.

Alternatives considered: filtering by `namespace` (skips the service-name convention and is no more precise here);
`pod=~"$application-web-ui-.+"` (brittle to the pod-name prefix); no filter (would sum every nginx exporter in the
cluster). The chosen selector is verified against a live install (see Risks) with `pod`/`namespace` as the fallback if
the relabelings differ.

### D2: Four panels — Requests, Active Connections, Connection States, Accepted/Handled

`stub_status` exposes connection counters and a request total, not request latency, so the per-service pattern
(Throughput / Latency / Failure Rate) cannot be filled. The section uses four timeseries panels in a 2×2 grid, mirroring
the API Gateway and Query Service sections:

| Panel              | Series (all filtered by `service="axon-showcase-web-ui"`)                                              |
| ------------------ | ------------------------------------------------------------------------------------------------------ |
| Requests           | `sum(rate(nginx_http_requests_total[1m]))`                                                             |
| Active Connections | `sum(nginx_connections_active)`                                                                        |
| Connection States  | `sum(nginx_connections_reading)` / `sum(nginx_connections_writing)` / `sum(nginx_connections_waiting)` |
| Accepted / Handled | `sum(rate(nginx_connections_accepted[1m]))` / `sum(rate(nginx_connections_handled[1m]))`               |

Alternatives considered: a single "Connections" panel folding active and the state breakdown (rejected — cramming
distinct units into one legend); an `nginx_up` availability panel (rejected as redundant with the section's purpose); an
`nginxexporter_build_info` panel (rejected — exporter metadata, not UI behavior).

### D3: Append the Web UI row after Query Service

The row and its four panels go at the end of the `panels` array with `gridPos.y` starting at 133 (the current maximum
bottom edge is 133): the row at `y=133`, the first two panels at `y=134`, the second two at `y=141`. Appending keeps
every existing `gridPos` untouched, so the diff is purely additive and the layout cannot shift.

### D4: Genericize the `AGENTS.md` worked-example totals rather than renumber them

The gotcha "Verify documented infrastructure/deployment numbers against the config files" cites the dashboard's raw vs
real panel counts as its worked example. Renumbering that example to the new totals would put a count back into a
durable, always-loaded artifact that the next panel change stales again — against the file's own guidance not to pin
such totals. Keep the incident and the raw-vs-real distinction; drop the specific figures.
`.opencode/agent/readme-auditor.md` cites the same figure ("a '36 panels' that counted row separators"), but there it
names the historical README defect the auditor was created to catch, not the dashboard's current structure, so it stays
accurate and is deliberately left unchanged.

### D5: Derive the README count from the JSON, never hand-write it

The README's `31 panels across 5 sections …` sentence becomes the new totals, extracted by the implementing step from
the dashboard file (count the row separators and the non-row panels) rather than computed from the design's arithmetic.

## Risks / Trade-offs

- [The `service` label may be absent on the nginx series if a Prometheus installation's relabelings differ] → Confirmed
  on a live `helmInstallToLocal` install before the change is reported done; fall back to `namespace`/`pod` if absent.
- [Malformed JSON silently breaks dashboard provisioning — `helm lint` renders the file as an opaque string, so it does
  not validate it] → Validate with `python3 -m json.tool` after editing, and confirm the dashboard renders in Grafana.
- [A PromQL typo yields empty panels, which a chart lint cannot see] → Each panel is checked to return data on the live
  install; the section is not reported done on a green build alone.
- [The README count drifts on the next dashboard change] → The count is derived from the JSON in the implementing task,
  and the `readme-auditor` owns README accuracy thereafter.

## Migration Plan

Content-only and auto-provisioned: the Grafana sidecar reloads the dashboard ConfigMap on the next `helmInstallToLocal`
(the config reloads without a restart). Rollback is reverting the commit and re-running the install. No data migration
or API compatibility concern.

## Open Questions

None.
