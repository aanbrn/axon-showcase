# Design

## Context

See `proposal.md` — Why. Today `ShowcaseRestController` is downstream-first: `fetchList` and `fetchById` always call
`queryOperations`, populate the two Caffeine `AsyncCache` beans as a side effect, and consult them only inside
`onErrorResume` for transient failures. The list cache holds `List<String>` IDs and resolves each through the by-ID
cache, coupling the two. The cache beans live in `showcase.api.rest` (`ShowcaseRestConfiguration`) and their fallback
logic in `ShowcaseRestController`, although the cache settings already live in the shared
`showcase.api.ShowcaseApiProperties`. The gateway also consumes the full domain-event Kafka stream for SSE (a hot
`replay().limit(100)` sink, eagerly subscribed at startup) and maps it to `ShowcaseEventDto` (type + `showcaseId` +
timestamp). `ShowcaseEvent` is a sealed interface in `showcase-command-api` exposing `showcaseId()`, with
`ShowcaseRemovedEvent` a permitted subtype. The read consumers are: the web UI (list only, reconcile settle budget ≈ 2.5
s), the gateway e2e (`Awaitility`, 60 s), and the load tests (by-ID write-lifecycle poll every 500 ms, timeout 5 min;
by-ID detail fetch for 15% of read iterations).

## Goals / Non-Goals

**Goals:**

- Turn both gateway query caches into read-through caches so downstream reads are capped at one call per key per TTL.
- Coalesce concurrent identical reads so a TTL boundary does not stampede the query service.
- Keep staleness within every consumer's convergence budget via short, per-cache write TTLs.
- Refresh a showcase's by-ID state on its events, without a negative cache.
- Share the gateway's read-side infrastructure in the `showcase.api` package: one domain-event receiver, and one
  read-through query cache any inbound adapter (REST today, another tomorrow) reuses, without the packages depending on
  each other.

**Non-Goals:**

- Adding a GraphQL (or other) inbound adapter; this change only makes the cache shareable.
- Any web-UI change; the reconcile loop is unchanged and must still converge under the short list TTL.
- A shared/distributed cache or cross-replica coherence (the gateway defaults to one replica on every target; a second
  replica would share the consumer group, so each event would invalidate only the replica that received it).
- A resilience demo: the transient-failure fallback is retired, not replaced.
- A global rate limiter (the per-key cache TTL is the rate cap).

## Decisions

**D1 — Read-through via `AsyncCache.get(key, (k, executor) -> …)` (the two-argument overload, D7).** Each read serves a
cache hit and otherwise computes the value from `queryOperations`, caching it atomically with in-flight coalescing.
Caffeine removes an entry whose async computation completes exceptionally (`AsyncCache.get` javadoc: "If the
asynchronous computation fails, the entry will be automatically removed"), so a `NOT_FOUND` during projection lag is not
cached and the creation-visibility poll loop is not delayed. _Alternatives:_ the current failure-triggered fallback
(rejected — absorbs nothing under normal load); `getIfPresent` + `put` (rejected — every concurrent request misses at a
TTL boundary and stampedes downstream); Spring `@Cacheable` (rejected — no async coalescing, and awkward for reactive
return types and object keys).

**D2 — Decouple the caches; the list cache stores full showcases.** Change the list cache's value type from
`List<String>` to `List<Showcase>` and drop the ID→by-ID resolution, so a list hit is self-contained and never fans out
downstream. _Alternatives:_ keep IDs and resolve through by-ID (rejected — a list hit can miss on by-ID entries,
reintroducing downstream calls and the partial-miss `503` path the current code has).

**D3 — Per-cache short write TTLs.** `expiresAfterWrite` is the binding freshness bound; `expiresAfterAccess` alone
never bounds a hot entry (every hit resets it), so both are set to the same value per cache for clarity. Proposed: list
`PT1S` (under the UI's ≈2.5 s settle budget), by-ID `PT5S` (well inside the e2e 60 s and load 5 min budgets). The
existing `Cache` shape (maximum size + two expiries) is unchanged, so no new configuration surface. _Alternatives:_ one
shared TTL (rejected — the list is freshness-bound by the UI, the by-ID by slower pollers); long TTLs (rejected — breaks
the UI reconcile); `refreshAfterWrite` (stale-while-revalidate; rejected — the fallback's loss was accepted, so the
extra config surface and refresh semantics aren't needed).

**D4 — Share one domain-event source: `showcaseEventReceiver` in `showcase.api`.** The gateway's single Kafka
subscription moves out of `events` into `showcaseEventReceiver` and `kafkaMessageConverter` beans declared on
`showcase.api.ShowcaseApiApplication` (alongside the query caches), emitting the original `Flux<ShowcaseEvent>` (domain
events, not the SSE DTO); the generic `kafkaMessageConverter` bean also carries the Axon configuration's upcaster chain
(mirroring the projection service's converter, so events are upcast as they are consumed). It keeps the
`Sinks.many().replay().limit(100)` hot buffer, which now serves two purposes: SSE history for late clients, and a
subscription window for an in-process consumer that attaches during startup — the buffer replays the last 100 events,
and since the by-ID cache is empty before context refresh completes, a startup-race replay is a no-op. The receiver is
instrumented with the application's `ObservationRegistry` (mirroring the projector), so consumed events are traced. The
SSE controller maps the receiver to `ShowcaseEventDto` directly (there is no separate `showcaseEventStream` bean), and
the read-through query cache (D8) subscribes to the receiver, evicting the by-ID entry on any showcase event. Because
the shared seam lives in `showcase.api` (the receiver) and `showcase.api.shared` (the query cache), `events` and `rest`
do not import each other — `events` depends only on the parent, `rest` on the parent and the shared cache package, and
the domain-event type already lives in `showcase-command-api`. _Alternatives:_ an inline removal-listener invoked by the
`events` pipeline (the earlier draft — rejected, the event pipeline would depend on the cache package and the source
would not be reusable); a Spring `ApplicationEvent` (framework-mediated — an extra type and indirection for a source
that is already reactive); injecting the cache into `events` (moves cache strategy into the event package).

**D5 — Invalidate only the by-ID cache, not the list.** An event also makes cached list entries stale, but the list TTL
(`PT1S`) bounds that, and evicting every list entry per event would thrash the list cache under the load tests' constant
writes (every write lifecycle emits several events). _Alternatives:_ invalidate all list entries on every event
(rejected — thrash for no observable gain at a 1 s list TTL).

**D6 — Retire the fallback; a failed miss propagates.** Under read-through a hit cannot fail, and a miss has no entry to
fall back to, so the `onErrorResume` arms become unreachable. Removing them means every failed miss propagates to the
existing error translation — a `ShowcaseQueryException` to `400`/`404`, any other failure to `503` — exactly as a read
with no cached entry did before. This is the behavior change the spec delta records.

**D7 — Carry the request's Reactor context into the read-through subscription.** `AsyncCache.get`'s future-returning
overload takes a two-argument function (`(key, executor) -> CompletableFuture<V>`) and invokes it on the calling thread
— only the future it returns completes asynchronously — but a subscription created inside a plain Java callback (the
`.toFuture()` on `queryOperations.fetch…`) does not inherit the caller's Reactor context, so the query-service call
would lose the trace context. Read the context with `Mono.deferContextual(…)` and write it onto the inner subscription,
in the two-argument form:
`Mono.deferContextual(ctx -> Mono.fromFuture(cache.get(key, (k, executor) -> fetch(k).contextWrite(ctx).toFuture())))`,
where `fetch(k)` is the value-producing source — `queryOperations.fetchById(…)` for the by-ID cache and
`queryOperations.fetchList(…).collectList()` for the list cache — so the future's type matches each cache's value type
(`Showcase` / `List<Showcase>`; only `Mono` declares `toFuture()`, so the list's `Flux` is collected first). The
read-through methods on the shared component (D8) own this. _Alternatives:_ rely on
`spring.reactor.context-propagation: auto`'s ThreadLocal restoration (rejected — implicit and thread-dependent); leave
it and accept a broken trace link (rejected — the gateway e2e and the observability path assert trace continuity); a
hand-rolled single-flight map (rejected — Caffeine already coalesces).

**D8 — The query caches are shared read-side infrastructure in `showcase.api.shared`, not REST.** The two `AsyncCache`
beans and their `CaffeineCacheManager` registration move out of `showcase.api.rest`; the read-through logic and the
event-driven invalidation are added in the shared `showcase.api.shared` package (neither exists in `rest` today). The
cache beans and the customizer become `@Bean` methods on `ShowcaseApiApplication` (the gateway's bean hub, alongside the
command-bus, Jackson, and security beans), and a new `showcase.api.shared.ShowcaseQueryCache` component takes them (with
`ShowcaseQueryOperations` and the receiver) by constructor, exposing `Mono<Showcase> fetchById(...)` /
`Flux<Showcase> fetchList(...)`; the constructor injection is what lets a slice test supply `Ticker`-backed caches.
`ShowcaseRestController` becomes a thin adapter delegating to it, and any future inbound adapter (e.g. a GraphQL
resolver) reuses the same component and one cache instance rather than importing `rest` or re-implementing the
read-through semantics. Only the beans move — the cache settings already live in `showcase.api.ShowcaseApiProperties`.
_Alternatives:_ leave the caches in `rest` (rejected — fuses the reusable read-through behavior to the REST adapter, so
a second adapter duplicates it or imports `rest`; the event-driven invalidation is not a REST concern either); declare
the caches as `@Bean` methods on `ShowcaseQueryCache` itself (rejected — a component's `@Bean` methods run in lite mode,
so a direct call from `fetchById`/`fetchList` would build a fresh cache rather than the registered singleton, and a
slice test could not supply its own); implement `ShowcaseQueryCache` as a `@Primary` decorator of
`ShowcaseQueryOperations` so adapters get caching transparently (deferred — the gateway's adapters stay explicit about
their reads, and a transparent decorator complicates the query-client/resilience bean wiring; revisit if many adapters
appear).

**D9 — Consolidate the gateway's infrastructure beans on `ShowcaseApiApplication`.** The Kafka receiver/converter (D4),
the query caches (D8), and the OpenAPI definition/version customizer all become `@Bean`/class-level declarations on
`ShowcaseApiApplication`, and the now-empty per-surface configuration classes (`ShowcaseEventStreamConfiguration`,
`ShowcaseRestConfiguration`) are deleted. The OpenAPI customizer is app-wide rather than REST-specific: the served
document spans both `ShowcaseRestApi` and `ShowcaseEventStreamApi`, so its `@OpenAPIDefinition` moves off the REST
interface and is retitled for the shared document. _Alternatives:_ keep the per-surface configuration classes (rejected
— each held a single bean once the caches and receiver moved, so they were indirection); leave the OpenAPI definition on
`ShowcaseRestApi` (rejected — it titles a document that also carries the live-events API).

## Risks / Trade-offs

- **UI reconcile stalls under projection lag** if the list TTL is not below the settle budget → keep `PT1S` (well under
  ≈2.5 s) and verify with the web-UI e2e and the reconciliation component tests.
- **Event-driven refresh** depends on the event arriving; a concurrent in-flight by-ID read could re-cache the
  pre-change showcase just after eviction → bounded by the by-ID TTL (`PT5S`) and idempotent; acceptable.
- **Load-test timing shifts**: read latency drops (cache hits) while the write-lifecycle poll observes each transition
  up to `PT5S` later → re-measure the committed baseline (`./scripts/load-test-baseline.sh`) and compare.
- **Trace/context loss** if the Reactor context is not carried onto the read-through subscription → apply D7 and confirm
  via the gateway e2e / trace check.
- **One shared cache across adapters** means every adapter inherits the same TTL and eviction semantics → intended (one
  consistent read path); a future adapter that needs different freshness would wrap its own policy, not fork the cache.
- **Concurrency correctness**: many polls hit one ID at 500 ms; coalescing must collapse them → assert the mapping
  function is invoked once (spy `verify(..., times(1))`).
- **No negative caching**: repeated reads of a genuinely missing showcase always reach the query service → intended; the
  poll loop depends on it to observe creation promptly.

## Migration Plan

Behavioral, in-place; no data migration. Deploy is a normal image/chart update. Rollback is the code revert (the caches
still exist; reverting restores downstream-first reads). TTLs are tunable per environment through the existing cache
settings, so a deployment can widen them without a code change.

## Open Questions

The exact TTLs (`PT1S` list / `PT5S` by-ID) can be tuned during implementation against the UI e2e and the load baseline
without changing the specs, the approach, or the task breakdown; only the read-through-with-short-TTL behavior itself is
spec'd.
