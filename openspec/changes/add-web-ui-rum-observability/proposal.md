# Proposal

## Why

Only server-side nginx metrics observe the web UI today: `nginx_http_requests_total` and the connection stats say the UI
is being _served_, but nothing reports what a visitor actually _experiences_. A slow page or a client-side JavaScript
error leaves no trace — there is no measurement of Core Web Vitals, and an exception thrown in the browser is invisible
in Grafana. The 2026-10-05 retrospective's recommended direction is to ship product-facing behavior, and this closes the
one observability gap its own idea list still names.

## What Changes

- **Gateway — new `showcase.api.telemetry` package**: a `TelemetryApi` interface and `TelemetryController` exposing
  `POST /telemetry`, request DTOs (`ClientTelemetryReport` with nested `ClientVital`/`ClientError`), and a recorder that
  writes the report to Micrometer — a Core Web Vitals distribution and a JavaScript-error counter, tagged only with
  bounded labels.
- **Gateway — error mapping**: an invalid report is rejected through the existing problem-detail handling (the
  `ShowcaseApiErrorResolver` 400 with `bodyErrors`), not a bespoke error shape.
- **Web UI**: add the `web-vitals` dependency; a new `src/shared/telemetry.ts` measures Core Web Vitals and captures
  `error`/`unhandledrejection`, reporting each to the gateway endpoint with the page load's `traceparent`; initialised
  once from `src/main.tsx`, and the Vite dev/preview proxy is extended to forward the report path.
- **Chart**: a new "Web UI experience" section in the bundled Grafana dashboard
  (`helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`) — Core Web Vitals percentiles and JS-error
  rate.
- **Docs**: README observability section (and the gateway's endpoint enumerations in `README.md`/`AGENTS.md`); the RUM
  idea is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

- `showcase/gateway/client-telemetry`: the gateway's client-telemetry ingestion endpoint — its request contract, its
  bounded metric recording, and its rejection of malformed or oversized reports.

### Modified Capabilities

- `showcase/clients/web-ui`: the UI SHALL measure Core Web Vitals and client-side errors and report them to the gateway.
- `showcase/gateway/rest-api`: the CORS allowance is extended to the telemetry endpoint (the requirement's endpoint
  enumeration gains `/telemetry`).

## Impact

- **Modules**: `showcase-api-gateway` (new package, tests), `showcase-web-ui` (dependency, module, tests, e2e), the
  bundled Grafana dashboard, `README.md`, `AGENTS.md`, `docs/ideas.md`.
- **APIs**: one new additive endpoint (`POST /telemetry`); no existing endpoint changes.
- **Dependencies**: `web-vitals` added to the web UI.
- **Deployment**: no new infrastructure or chart component — the gateway's existing ServiceMonitor/Prometheus scrape and
  the bundled Grafana dashboard carry the new metrics. The endpoint is a public write surface, bounded by request
  validation and bounded metric labels.
- **Non-goals**: frontend distributed tracing and session replay (Faro's richer features), and per-client rate limiting.
