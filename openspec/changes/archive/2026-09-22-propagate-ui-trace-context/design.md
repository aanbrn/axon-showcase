# Design

## Context

See `proposal.md` — Why. The constraints that shape the approach:

- The tracing pipeline is already wired: `micrometer-tracing-bridge-otel` and `otel-exporter-otlp` on the gateway,
  Axon's `axon-tracing-opentelemetry`, and the chart's `observability.tracing.sampling.probability` (default `0.1`),
  with OTLP tracing export gated per deployment. Nothing server-side needs to change to _continue_ an incoming trace —
  Micrometer's OTel instrumentation extracts a W3C `traceparent` from an incoming request by default.
- Every UI gateway call funnels through one function, `request()` in `showcase-web-ui/src/shared/api.ts`; the SSE stream
  is a separate `EventSource`, which cannot set headers.
- The gateway's CORS registration sets an explicit `allowedHeaders` list (the `SHOWCASE_CORS_ALLOWED_HEADERS` change,
  #359) across four surfaces per ADR-0002: the Java `Cors` field, the `application.yml` placeholder, the image's
  `BPE_DEFAULT_*` map, and the chart value.
- Observability is Kubernetes-deployment-only: the local compose stack exports no traces, so end-to-end confirmation
  needs a live Helm install with OTLP tracing export enabled.

## Goals / Non-Goals

**Goals:**

- One trace per page load, from the browser's click through the gateway into the command/query pipeline, visible in
  Tempo.
- A single place in the UI that builds the trace context, and a single place that sends it.
- The header admitted by the gateway's CORS allow-list on every surface a deployment reads.

**Non-Goals:**

- Web-vitals and JS-error reporting (the other half of the parked RUM idea) — a separate change.
- Tracing the SSE connection: `EventSource` cannot set request headers, so the live-event stream stays outside the
  browser-originated trace.
- Changing the gateway's server-side sampling of its own root spans (sagas, projections).
- Any new gateway endpoint or service logic.

## Decisions

**1. Build the trace context in a new `shared/tracing.ts` and inject it in `request()`.** `traceparent()` returns the
header value; `request()` merges it into `init.headers`. Alternatives: adding the header at each call site (duplicated
and easy to miss — the create flow is one of several callers); wrapping `fetch` globally (implicit, and it would also
tag non-gateway fetches). One module plus the existing choke point keeps the change to two files and makes the behaviour
testable in isolation.

**2. One trace id per page load, a fresh parent id per request.** The trace id is generated once at module load; each
`request()` call mints a new parent id. Alternatives: a trace per request (each call becomes its own trace, so a page
load's requests are unrelated in Tempo — defeats the point); a trace per session persisted in `localStorage` (spans a
page reload, but a reload is a new user action and the extra plumbing buys little for a single-page app).

**3. Always sampled (flags `01`).** The UI sets the sampled bit, so every page load is recorded. Per W3C Trace Context a
downstream service must not re-sample a trace whose parent set the flag, so the UI's choice governs browser-originated
traces; the chart's `0.1` probability keeps governing the services' own root spans. Alternative: a client-side rate
mirroring `0.1` — rejected for a showcase, where a demo page load should reliably show its trace. The flag is one
constant if the volume ever matters.

**4. `traceparent` joins the CORS allow-list on all four surfaces, mirroring #359.** The Java `Cors.allowedHeaders`
default, the yml placeholder, the image's `BPE_DEFAULT_SHOWCASE_CORS_ALLOWED_HEADERS`, and the chart's
`apiGateway.cors.allowedHeaders` each gain the third header, and the gateway's CORS tests gain it. A cross-origin
deployment whose UI sends `traceparent` would otherwise have its preflight rejected — the exact defect #359 fixed for
`Content-Type`/`Idempotency-Key`.

**5. No gateway code change.** The existing OTel instrumentation extracts the incoming context; adding a filter or a
manual context extraction would duplicate it. If the live verification shows the gateway roots a new trace instead of
continuing the browser's, the fix is a configuration/propagator question, not a new component.

## Risks / Trade-offs

- **A sampled parent must not be re-sampled downstream** — if the gateway's sampler drops a browser trace despite the
  flag, the feature silently does nothing. → Mitigation: the verification task queries Tempo for a browser-originated
  trace on a live Helm install with tracing export enabled, and the evidence is recorded; if it fails, decision 5's
  fallback applies.
- **Trace volume** — every page load produces a trace, at a higher rate than the server's `0.1`. → Mitigation: the
  sampled flag is a single constant; a future change can make it a rate or a config value without touching the specs.
- **The header is invisible to the existing e2e suite** — the web UI e2e serves the UI same-origin through a Vite proxy,
  so no preflight runs and the CORS allowance is not exercised there. → Mitigation: the gateway's
  `ShowcaseApiApplicationIT` preflight test asserts the header is granted, exactly as #359's does.
- **Cross-origin preflight growth** — the UI's state-changing preflight now requests a third header. → Mitigation: the
  same CORS change covers it; a same-origin deployment sends no preflight at all.

## Migration Plan

Deploy the new web UI image and the gateway with the updated `SHOWCASE_CORS_ALLOWED_HEADERS`; the header is additive, so
older UIs and gateways keep working (a request without `traceparent` simply roots its own trace). Rollback is reverting
the images and the chart value — no data or contract migration.
