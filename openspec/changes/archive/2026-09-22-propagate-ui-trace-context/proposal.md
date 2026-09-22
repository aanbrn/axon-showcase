## Why

A browser's API calls reach the gateway as the root of a fresh trace, so Tempo shows the server-side trace (gateway →
command/query service) with no link to the page load that caused it: the UI's own experience is unobserved, and an
operator investigating a slow or failed user action cannot follow it from the click into the pipeline. The tracing
plumbing is already in place end to end — Micrometer's OTel bridge, the OTLP exporter, and Axon's tracing
instrumentation — and the gateway already extracts an incoming W3C Trace Context, so the missing pieces are a
`traceparent` the browser sends and a CORS allow-list that admits it.

## What Changes

- `showcase-web-ui/src/shared/tracing.ts` (new) — builds a W3C `traceparent` (`00-<trace-id>-<parent-id>-01`): one trace
  id per page load, a fresh parent id per request, always sampled.
- `showcase-web-ui/src/shared/api.ts` — `request()` adds the `traceparent` header to every call.
- `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiProperties.java` — the `Cors.allowedHeaders` default gains
  `traceparent`.
- `showcase-api-gateway/src/main/resources/application.yml` — the `allowed-headers` placeholder default gains
  `traceparent`.
- `showcase-api-gateway/build.gradle.kts` — `BPE_DEFAULT_SHOWCASE_CORS_ALLOWED_HEADERS` gains `traceparent`.
- `helm/chart/src/main/helm/values.yaml` — the `apiGateway.cors.allowedHeaders` default gains `traceparent` (the gateway
  template already joins that value, so it needs no edit).
- `AGENTS.md` — the `SHOWCASE_CORS_ALLOWED_HEADERS` line names the third header.
- `docs/ideas.md` — the parked RUM idea's trace-propagation piece is removed (implemented here), and the product-phase
  entry that names it as the highest-value parked candidate is fixed; `README.md`'s observability section is updated if
  it needs it.
- Tests — Vitest unit tests for the traceparent builder and for `request()`, with the existing UI test files' exact
  `fetch`-argument assertions updated to expect the header; the gateway's `ShowcaseApiPropertiesCT` default assertion
  and the `ShowcaseApiApplicationIT` preflight test gain the header.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/clients/web-ui`: a new requirement — the UI propagates W3C Trace Context on its API calls, so one page
  load's requests join a single trace the gateway continues.
- `showcase/gateway/rest-api`: the `CORS allows the standalone UI origin` requirement's allowed-header list gains
  `traceparent`, so the UI's cross-origin preflight for it is granted.

## Impact

- **Code**: the web UI's `shared/` API layer; the gateway's CORS defaults on all four surfaces. No service logic changes
  — the gateway's existing OTel instrumentation does the extraction.
- **Build/tests**: Vitest unit tests in `showcase-web-ui`; the gateway's `ShowcaseApiPropertiesCT` and
  `ShowcaseApiApplicationIT` gain the header; `check` otherwise unchanged.
- **Deployment**: `SHOWCASE_CORS_ALLOWED_HEADERS` gains a third value across the Java field, the yml placeholder, the
  image's `BPE_DEFAULT_*` map, and the chart value. Confirming a browser-originated trace reaches Tempo is
  deployment-only — a live Helm install with OTLP tracing export enabled.
- **Docs**: `AGENTS.md`'s `SHOWCASE_CORS_ALLOWED_HEADERS` line; `docs/ideas.md`'s RUM and product-phase entries;
  `README.md` if its observability section needs it; and the `showcase/clients/web-ui` Purpose refresh in the archive
  commit if the change falsifies it.
- **Specs**: one `ADDED` requirement on `showcase/clients/web-ui`; one `MODIFIED` on `showcase/gateway/rest-api`.
