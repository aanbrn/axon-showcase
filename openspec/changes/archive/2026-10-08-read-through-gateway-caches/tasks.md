# Tasks

## 1. Shared read-through query cache in `showcase.api`

- [x] 1.1 Declare the two `AsyncCache` beans (moved from `ShowcaseRestConfiguration`) and the `CaffeineCacheManager`
      registration as `@Bean` methods on `showcase.api.ShowcaseApiApplication`, and add a `ShowcaseQueryCache` component
      in `showcase.api.shared` that takes them (with `ShowcaseQueryOperations` and the receiver) by constructor and
      exposes read-through `Mono<Showcase> fetchById(FetchShowcaseByIdQuery)` /
      `Flux<Showcase> fetchList(FetchShowcaseListQuery)` via `AsyncCache.get(key, (k, executor) -> …)` (the two-argument
      overload), capturing the request's Reactor context onto the downstream subscription (design D1/D7/D8); constructor
      injection, not `@Bean` methods on the component, so a slice test can supply its own caches; verify with
      `./gradlew :showcase-api-gateway:compileJava`.
- [x] 1.2 Change the list cache value type to `List<Showcase>` (dropping the ID→by-ID resolution) in
      `ShowcaseApiApplication` and `ShowcaseQueryCache` (design D2/D8); verify with
      `./gradlew :showcase-api-gateway:compileJava`.
- [x] 1.3 Point `showcase.api.rest.ShowcaseRestController` at `ShowcaseQueryCache`, dropping its inline cache logic and
      the `onErrorResume` fallback (design D6/D8), and remove the cache beans and customizer from
      `ShowcaseRestConfiguration` (later folded into `ShowcaseApiApplication` and deleted; see 2.5); verify with
      `./gradlew :showcase-api-gateway:compileJava`.
- [x] 1.4 Confirm a failed mapping future is not cached (Caffeine removes it), so a `NOT_FOUND` during projection lag
      keeps reaching the query service; verify with the not-yet-projected scenario in 1.5.
- [x] 1.5 Rework `showcase-api-gateway/src/componentTest/java/showcase/api/rest/ShowcaseRestControllerCT.java` into
      read-through scenarios: by-ID/list hit skips downstream (`verify(…, never())`), miss fetches and caches, entry
      expiry refetches, concurrent same-key reads invoke downstream once (`times(1)`), a failed miss propagates, and a
      not-yet-projected showcase is not cached; declare the shared component as a `@Bean` in the slice with short-TTL,
      `Ticker`-backed caches (the CT builds `maximumSize`-only caches and types the list cache `List<String>` today) and
      a stub `Flux<ShowcaseEvent>` bean (the component subscribes to the receiver, so the slice must not load the Kafka
      one); run `./gradlew :showcase-api-gateway:componentTest --tests '*ShowcaseRestControllerCT'`.
- [x] 1.6 Add a focused test that the read-through subscription carries the request's Reactor context (a subscription
      created inside the plain mapping callback does not inherit it, which is why `contextWrite` is needed; design D7);
      run the same component-test task.
- [x] 1.7 Sweep the docs and Javadoc describing current gateway cache behavior: the read-through method Javadoc on
      `ShowcaseQueryCache`; the `ShowcaseRestController` Javadoc that calls the caches a fallback and
      `showcase.api.ShowcaseApiApplication`'s Javadoc (it said the caches "live in `ShowcaseRestConfiguration`") — the
      Javadoc convention covers every declaration in a touched file; `AGENTS.md` for gateway cache-fallback references
      (replace the `ShowcaseRestControllerCT` "gateway fallback logging" example, ≈line 947, since that logging is
      removed; classify the Caffeine gotcha's "`ShowcaseRestController`'s fallback paths were refactored this way"
      example at ≈line 2801, whose code this change removes — reword it to the read-through code or mark it
      deliberately-retained history; and leave the historical `captured:` markers and the historical "cache-fallback
      refactor" testing example at ≈line 905 as recorded); and `README.md:122` ("the gateway falls back to a cache when
      the query side is unavailable"), which this change falsifies. Verify by grepping `README.md` for `falls back` (its
      wording is not matched by `grep fallback`), `AGENTS.md` for `fallback`, and the gateway sources for
      `ShowcaseRestConfiguration` (to confirm no stale reference to the deleted class remains).

## 2. Shared domain-event receiver and cache invalidation

- [x] 2.1 Extract the gateway's Kafka subscription and `kafkaMessageConverter` from the SSE configuration into
      `showcaseEventReceiver` and `kafkaMessageConverter` beans on `showcase.api.ShowcaseApiApplication` (alongside the
      query caches), emitting `Flux<ShowcaseEvent>` (the domain events, keeping the `Sinks.many().replay().limit(100)`
      hot buffer and instrumenting it with the `ObservationRegistry`), and delete the now-empty
      `ShowcaseEventStreamConfiguration` — the SSE controller maps the receiver to `ShowcaseEventDto` directly (design
      D4); verify with `./gradlew :showcase-api-gateway:compileJava` plus 2.2.
- [x] 2.2 Cover the receiver-to-DTO mapping through the SSE controller: rework `ShowcaseEventStreamControllerCT` to
      supply a test `Flux<ShowcaseEvent>` plus a `ShowcaseEventMapper` and `ShowcaseEventStreamControllerTests` to
      assert the mapped DTO; run `./gradlew :showcase-api-gateway:componentTest`.
- [x] 2.3 Make `ShowcaseQueryCache` subscribe to the receiver's `Flux<ShowcaseEvent>` and evict the by-ID entry on any
      showcase event (design D4/D5); verify with 2.4.
- [x] 2.4 Add a component test (`ShowcaseQueryCacheCT` in `showcase.api.shared`) that supplies a non-replay
      `Sinks.many().multicast().directBestEffort()` `Flux<ShowcaseEvent>` as the receiver and loads the cache, emits a
      removal event and a status-change event and asserts the cached by-ID entry is evicted and the next read re-queries
      the query service for each; run `./gradlew :showcase-api-gateway:componentTest`.
- [x] 2.5 Move the `@OpenAPIDefinition` (retitled "Showcase API Gateway", describing both the management REST API and
      the live event stream) and the version customizer onto `showcase.api.ShowcaseApiApplication`, delete the now-empty
      `ShowcaseRestConfiguration`, and rename the REST `@Tag` to `Showcase Management`; assert the served document's
      title in `ShowcaseApiApplicationIT`; verify with `./gradlew :showcase-api-gateway:check`.

## 3. Configuration defaults

- [x] 3.1 Align the cache TTL defaults on every surface: `ShowcaseApiProperties.java` (list `PT1S`, by-ID `PT5S`, both
      expiries), `showcase-api-gateway/src/main/resources/application.yml` (`FETCH_SHOWCASE_LIST_QUERY_CACHE_*`,
      `FETCH_SHOWCASE_BY_ID_QUERY_CACHE_*`), and `helm/chart/src/main/helm/values.yaml` (`apiGateway.caches.*`); verify
      each surface's values match with a grep.
- [x] 3.2 Update `showcase-api-gateway/src/componentTest/java/showcase/api/ShowcaseApiPropertiesCT.java`
      (`allProperties_whenNothingSet_useDocumentedDefaults`,
      `applicationYml_placeholders_bindDefaultsAndEnvVarOverrides`) to the new values; run
      `./gradlew :showcase-api-gateway:componentTest --tests '*ShowcaseApiPropertiesCT'`.

## 4. Spec delta and validation

- [x] 4.1 Confirm the delta `openspec/changes/read-through-gateway-caches/specs/showcase/gateway/rest-api/spec.md`
      matches the implementation and run `openspec validate --all`; it passes.
- [ ] 4.2 Record the archive-time Purpose refresh of `openspec/specs/showcase/gateway/rest-api/spec.md` (the line naming
      "cache fallback on query failures"), applied in the archive commit of the same PR.

## 5. Integration verification

- [x] 5.1 Run `./gradlew :showcase-api-gateway:check` with integration tests and the coverage gate enabled (the module
      gained production code; the PR gate's `-PskipITs -Pcoverage.gate.enabled=false` would not catch a coverage drop);
      it passes.
- [x] 5.2 Run `./gradlew :showcase-api-gateway:e2eTest`; it passes — by-ID changes are visible promptly and state
      transitions are observed within their budgets.
- [x] 5.3 Run `./gradlew :showcase-web-ui:e2eTest`; it passes — the UI reconcile converges under the `PT1S` list TTL.
- [x] 5.4 Re-measure the committed baseline: `./scripts/load-test-baseline.sh` at the reference's operating point
      (`CALIBRATE_MAX_RATE=200`, 127 vs 124 workload units/s, 0 failed requests) wrote
      `load-tests/src/gatling/resources/baseline.properties` (list read improved: `FetchShowcases` mean 8→3 ms, p95
      11→6, p99 24→14) and `docs/load-tests/2026-10-08.md` (annotated); two write-lifecycle p99 tails (`PollShowcase`
      24→56, `StartShowcase` 18→45) were accepted with `REFRESH_BASELINE=1`.

## 6. Lesson capture

- [x] 6.1 Run the `lesson-capture` subagent over the diff and the review findings, apply its durable proposals, and
      record the applied net `AGENTS.md` delta here: **+10 lines** (`git diff --numstat origin/main -- AGENTS.md` =
      `27 17`) — replaced the Caffeine `AsyncCache`/Reactor-bridge bullet, dropping its stale `ShowcaseRestController`
      fallback clause (+3), and added the sibling-extraction-mirrors-full-wiring bullet (+6), the remainder reflow.
