# Design

## Context

See `proposal.md` — Why. The web UI is a static nginx-served bundle (no backend of its own), the gateway is the only
component the browser already calls cross-origin, and the observability stack is Prometheus + Grafana (kps) plus Tempo —
there is no Loki and no collector. The gateway already exposes Micrometer metrics on `/actuator/prometheus`, scraped by
the existing api-gateway ServiceMonitor, and the bundle already carries a per-page-load W3C `traceparent` it sends on
API calls (`showcase-web-ui/src/shared/tracing.ts`).

## Goals / Non-Goals

**Goals:**

- Report browser-side Core Web Vitals and JavaScript errors so a visitor's experience is observable in Grafana.
- Reuse the existing gateway, ServiceMonitor, Prometheus, and Grafana dashboard — no new infrastructure.
- Keep the browser report bounded and cardinality-safe.

**Non-Goals:**

- Frontend distributed tracing (browser spans joined to the backend trace in Tempo) and session replay/breadcrumbs.
- Per-client rate limiting of the telemetry endpoint.
- Spec'ing the dashboard panel layout (only the metric contract is spec'd).

## Decisions

### Push through the gateway, not Grafana Faro

Faro is not a single library: it is a Web SDK plus a collector (`faro.receiver` / `otelcol.receiver.faro` in Grafana
Alloy) that fans out to Loki, Tempo, and Prometheus — none of which this repo runs except Prometheus/Tempo. **Chosen:**
a `POST /telemetry` endpoint on the gateway, recording Micrometer metrics. It reuses the surfaces the browser already
reaches (CORS allow-list, open server-port NetworkPolicy, ingress, ingress-adjacent trace propagation) and the
scrape/dashboard that already exist, and it is verifiable in-repo (unit/component tests, the web-UI e2e).

- _Rejected — Faro + Alloy collector:_ adds a new component (Alloy, plus Loki for useful error data), a second endpoint
  exposed to the browser, and datasource wiring; its value only shows against a live cluster, which no local gate
  exercises. Revisit if frontend distributed tracing or Grafana Cloud Frontend Observability becomes the goal.
- _Rejected — console/log only:_ no metrics and no dashboard, so nothing for a person to see.

### One batched endpoint, not one per signal

**Chosen:** a single `POST /telemetry` whose body carries an array of vitals and an array of errors. Fewer routes, one
CORS surface, and one request per flush. A per-signal split (`/telemetry/web-vitals`, `/telemetry/errors`) was rejected
as more surface for no benefit.

### Record with Micrometer using bounded labels only

**Chosen:** a Core Web Vitals distribution (`showcase.web.vitals`) and a JavaScript-error counter
(`showcase.web.errors`) — the two metric families the dashboard queries. Labels are the vital name, rating, normalized
error type, and normalized route. Free-form client strings (message, source) are logged, never used as label values —
arbitrary strings as Prometheus labels are the classic unbounded-cardinality failure. Labels are normalized: the route's
query string is dropped, an unknown route folds to `other`, and an unrecognized error type folds to `Error`. The known
route set is the SPA's single route (`/`); the known error names are the standard JavaScript error constructors
(`Error`, `TypeError`, `RangeError`, `ReferenceError`, `SyntaxError`, `URIError`, `EvalError`). An alternative —
shipping raw reports to a log store — was rejected because there is no log store, and because counters/histograms are
what a dashboard needs.

### Reuse the existing validation and error mapping

**Chosen:** the telemetry DTOs carry Jakarta constraints (`@Size` per array and field, a bounded set of vital names and
ratings, `@PositiveOrZero` values). The vitals/errors lists use `@Singular(ignoreNullCollections = true)` — the same
idiom `FetchShowcaseListQuery` uses — so the builder exposes singular adders and an omitted or `null` list becomes an
empty (immutable) list rather than NPEing the request; a `null` list is therefore tolerated, not rejected, and no
field-level `@NotNull` is needed. The vital name and rating are constrained **strings** (`@Pattern` against the bounded
set) rather than Java enums: an unknown wire value must be a bean-validation failure, because a Jackson enum
deserialization failure surfaces as an `HttpMessageNotReadableException` and yields no `bodyErrors` map. The controller
carries its own small `@ExceptionHandler(HandlerMethodValidationException.class)` delegating to the shared
`ShowcaseApiErrorResolver` — a controller's `@ExceptionHandler` is not inherited by another controller, and the existing
handler is private to `ShowcaseRestController` — so an invalid report produces the same `400 Bad Request` problem detail
with a `bodyErrors` map the `/showcases` endpoints use. That handler only sees a `HandlerMethodValidationException` when
framework method validation is active for the controller; Spring activates it for a controller that has a constraint
annotation directly on a method parameter, which is why a `@NotNull` accompanies the `@Valid` on the body parameter (the
sibling `ShowcaseRestController` gets it incidentally from the `@Min`/`@Max` on its page-size parameter). Without it the
resolver sees a `WebExchangeBindException` instead and the `bodyErrors` contract is lost. The gateway's existing
blocking-execution configurer routes **every** controller method (its predicate is `__ -> true`), so this endpoint is on
the bounded-elastic scheduler like the others and the rest-api routing requirement holds for it too. A bespoke error
shape was rejected as a second, divergent contract.

### Measure with `web-vitals` and report through the shared `request` helper

**Chosen:** the `web-vitals` package for field-accurate Core Web Vitals (its callbacks fire at the right lifecycle
points, including on `visibilitychange`/`pagehide`). Reporting goes through the existing `shared/api.ts` `request`
helper —
`request('/telemetry', { method: 'POST', keepalive: true, headers: { 'Content-Type': 'application/json' }, body })` — so
the report reuses the helper's `BASE` prefix and per-page `traceparent` rather than duplicating them.
`navigator.sendBeacon` was rejected as a fallback because it cannot set request headers, so it could not carry the trace
(and would bypass `request`); a hand-rolled `PerformanceObserver` was rejected as reimplementing a subtle,
well-maintained standard.

### Initialise once from the app entry and keep it in `shared`

**Chosen:** a `src/shared/telemetry.ts` module (framework-agnostic, exported through `shared/index.ts`) initialised once
from `src/main.tsx`. `shared` is the FSD layer every layer may import, matching the existing `tracing.ts` helper. Under
`viteDev` and the `vite preview` server the e2e drives, `BASE` is empty and requests are same-origin, so the telemetry
path SHALL be in the Vite proxy like `/showcases` and `/events` (else a report never reaches the gateway in dev or the
e2e) — `showcase-web-ui/vite.config.ts` gains `/telemetry`.

### Leave `openspec/config.yaml`'s gateway description unchanged

**Chosen:** the docs sweep widens the live **endpoint enumerations** (the `AGENTS.md` Architecture bullet, the README
tree/table/proxy lines, and the `playwright.config.ts` JSDoc), but leaves `openspec/config.yaml`'s `context` as it is:
it describes the gateway coarsely ("REST entry point (/showcases)") and already omits `/events`, so it is a service
summary, not an endpoint enumeration — widening it would add a fourth un-gated copy of facts it never carried.

## Risks / Trade-offs

- **A public write endpoint can be abused** (counter inflation) → bounded request validation, bounded metric labels, and
  no command/query dispatch; per-client rate limiting is a recorded Non-Goal for a reference app.
- **Unbounded label cardinality from route, error type, or message** → the route and error type are normalized to known
  sets with `other`/`Error` fallbacks, and message/source are never labels (logged at a capped length).
- **Reports that fire on page hide may not flush before unload** → `fetch` with `keepalive` is used precisely for the
  unload path (and it can still carry the `traceparent` header, unlike `sendBeacon`).
- **Error messages may carry sensitive data into logs** → message/source are length-capped before logging.
- **E2E timing of client metrics is inherently racy** → the browser e2e asserts an early-firing vital (TTFB) via route
  interception with a tolerant timeout; the deterministic coverage lives in the web-UI unit tests and the gateway
  component test.

## Migration Plan

Additive: deploy the gateway (new endpoint + metrics), then the web UI (reporting). No data migration, no breaking
change. Rollback is reverting the two images; the metrics simply stop.

## Open Questions

None.
