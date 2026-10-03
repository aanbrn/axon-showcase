# Tasks

## 1. Dashboard section

- [x] 1.1 In `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`, append a `Web UI` row (id `74`,
      `y=133`) and four `timeseries` panels (ids `75`–`78`) at `y=134`/`y=141`, every series filtered by
      `service="axon-showcase-web-ui"`: Requests `sum(rate(nginx_http_requests_total[1m]))`; Active Connections
      `sum(nginx_connections_active)`; Connection States `sum(nginx_connections_reading)`,
      `sum(nginx_connections_writing)`, `sum(nginx_connections_waiting)`; Accepted/Handled
      `sum(rate(nginx_connections_accepted[1m]))` and `sum(rate(nginx_connections_handled[1m]))`. Copy an existing panel
      (e.g. `id` 60) for the shape, keep legends at the bottom, and leave every existing `gridPos` untouched. Verify
      with `python3 -m json.tool helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json > /dev/null`
      (exit 0) and the derivation command in 2.1 printing `top 41 rows 6 panels 35`.
- [x] 1.2 Run `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal` and confirm both pass —
      the chart still renders the dashboard ConfigMap with the edited JSON.

## 2. Documentation

- [x] 2.1 In `README.md`, replace the `31 panels across 5 sections covering every service …` sentence in the
      Observability section with the counts **derived from the JSON** —
      `python3 -c "import json;d=json.load(open('helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json'));p=d['panels'];print('top',len(p),'rows',sum(x.get('type')=='row' for x in p),'panels',sum(x.get('type')!='row' for x in p))"`
      prints `top 41 rows 6 panels 35` — and keep the coverage wording truthful about the web UI now being on the
      dashboard. Verify the README's numbers equal the command's output.
- [x] 2.2 In `AGENTS.md` (the "Verify documented infrastructure/deployment numbers against the config files" gotcha),
      replace the `(36 is the raw top-level count — 5 are empty row separators, 31 are real panels)` parenthetical with
      wording that keeps the incident and the raw-vs-real distinction without pinning totals. Verify with
      `grep -n "real panel count" AGENTS.md` showing the genericized text and
      `grep -n "36 is the raw\|5 are empty\|31 are real" AGENTS.md` returning nothing.
- [x] 2.3 Remove the implemented idea from `docs/ideas.md` (the 2026-10-02 "Surface the web UI on the Axon Showcase
      Grafana dashboard" entry). Verify `grep -n "Surface the web UI on the" docs/ideas.md` returns nothing.
- [x] 2.4 Run `./gradlew spotlessApply` after the last edit to any Spotless-owned file, then `./gradlew spotlessCheck`
      and confirm both pass (markdown in `README.md`, `AGENTS.md`, `docs/ideas.md`, and the active change dir is gated).

## 3. Verification

- [x] 3.1 On a live local cluster, upgrade the app release and confirm the Grafana sidecar reloads the dashboard — ran
      `helmInstallAxonShowcaseToLocal` with the five image-build tasks excluded via `-x` (they were already current for
      this JSON-only change), and confirmed the `axon-showcase-axon-showcase` ConfigMap reloaded with the Web UI row.
- [x] 3.2 Confirm the selector and every panel query against the deployed signal: port-forward Prometheus and verify the
      nginx series carry `service="axon-showcase-web-ui"` (fall back to `namespace`/`pod` and adjust D1 if not), then
      run each panel's PromQL and confirm it returns data.
- [x] 3.3 Confirm the Web UI row renders with data in Grafana — query the provisioned dashboard via the Grafana API
      (`GET /api/dashboards/uid/d5ebc0e9-ba9c-40d5-92c2-e1f7263290c6`) and/or screenshot it through the `vision`
      subagent. Note: an agent shell has no TTY for the `/etc/hosts` write, so reach Grafana with a `Host:`-header
      request against the ingress address and hand the hostname write to the owner if needed.
- [x] 3.4 Run the per-unit `lesson-capture` subagent over the change and apply its durable proposals; record the applied
      net `AGENTS.md` delta (expected: none, or a small merge) on this task. Applied: one merge into the
      "doc-consistency sweep" bullet's sweep-scope clause (a live artifact's historical citation also stays; +4 rendered
      lines), `captured: add-web-ui-dashboard-panels`; the subagent's dashboard-JSON-validity check was routed to
      `docs/ideas.md` (2026-10-03) rather than `AGENTS.md`.
