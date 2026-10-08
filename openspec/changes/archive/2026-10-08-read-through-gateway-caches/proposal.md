# Proposal

## Why

The API gateway forwards every `GET /showcases` and `GET /showcases/{id}` to the query service, and through it to
OpenSearch, even when the same query was just served. It already holds two Caffeine caches, but they are consulted only
after a query _fails_ (`onErrorResume`), so under normal load they absorb nothing. The web UI's reconciliation loop and
the load tests' write-lifecycle poll loops issue the same reads repeatedly, and each one is a fresh round trip. A
read-through cache with a short write TTL caps downstream calls per key without exposing stale state longer than its
consumers' convergence budgets tolerate.

## What Changes

- Move the two query-cache `AsyncCache` beans and their `CaffeineCacheManager` registration out of `showcase.api.rest`,
  and add the read-through logic and event-driven eviction in a shared `showcase.api.shared.ShowcaseQueryCache`
  component (`fetchById`/`fetchList`), so the REST controller and any future inbound adapter (e.g. a GraphQL resolver)
  reuse one cache instance; the list cache holds `List<Showcase>` instead of `List<String>` (a list hit becomes
  self-contained and the two caches are decoupled), and both caches keep per-cache size and expiry settings.
- `showcase-api-gateway/.../ShowcaseRestController.java` — delegates the two reads to `ShowcaseQueryCache` and drops its
  inline cache logic and the `onErrorResume` fallback arms.
- Short write-TTL defaults aligned across every configured surface (`ShowcaseApiProperties` Java field,
  `showcase-api-gateway/src/main/resources/application.yml`, `helm/chart/src/main/helm/values.yaml`): list `PT1S`, by-ID
  `PT5S`.
- Extract the gateway's single Kafka domain-event subscription into a `showcaseEventReceiver` bean in `showcase.api`
  emitting `Flux<ShowcaseEvent>` (the domain events); the SSE controller maps it to the SSE DTO, and
  `ShowcaseQueryCache` subscribes to it and evicts its by-ID entry on any showcase event, so a changed showcase is not
  served from a stale entry.
- Consolidate the gateway's infrastructure beans onto `showcase.api.ShowcaseApiApplication` — the query caches, the
  Kafka receiver/converter, and the OpenAPI definition/version customizer — deleting the now-empty per-surface
  configuration classes (`ShowcaseEventStreamConfiguration`, `ShowcaseRestConfiguration`); the served OpenAPI document
  is retitled for the surfaces it spans and its REST tag renamed `Showcase Management`.
- Replace the `showcase/gateway/rest-api` "Cache fallback on transient query failures" requirement with two requirements
  — "Read-through caching of showcase queries" and "Showcase events invalidate the by-ID cache" (a retitling, so the
  delta uses a `REMOVED` block plus two `ADDED` blocks).
- Refresh docs that name the gateway's cache-fallback behavior (`AGENTS.md`, `README.md`, the rest-api Purpose) and
  re-measure the committed load-test baseline.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/gateway/rest-api`: the cache requirement changes from failure-triggered fallback to read-through caching
  with a short write TTL and event-driven eviction; the fallback scenarios are retired.

## Impact

- **Code**: the shared read-side cache (`showcase/api/shared`), the app-wide infrastructure and domain-event receiver
  (`showcase/api`), the SSE controller (`showcase/api/events`), the REST adapter (`showcase/api/rest`), and gateway
  tests — the reworked `ShowcaseRestControllerCT`, `ShowcaseEventStreamControllerCT`, and `ShowcaseApiPropertiesCT` plus
  the new `ShowcaseQueryCacheCT`.
- **Behavior**: downstream query-service/OpenSearch read volume drops to at most one call per cache key per TTL, with
  concurrent identical reads coalesced; the transient-failure fallback (serve cached on failure) is retired, so a failed
  read now propagates to the existing error translation (a `400`/`404` query error, a `503` availability failure).
- **Deployment**: cache TTL defaults change on all configured surfaces; no chart template or environment-variable shape
  change.
- **Load tests**: the committed response-time baseline is re-measured, since read-through lowers read latency while the
  write-lifecycle poll loop observes each transition up to the TTL later.
- **No REST API contract change**; the web UI is unchanged.
