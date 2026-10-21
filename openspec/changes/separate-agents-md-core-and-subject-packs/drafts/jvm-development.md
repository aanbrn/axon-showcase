---
name: jvm-development
description:
  JVM production-code conventions and runtime diagnosis for this repository's Java services and libraries — value types,
  nullness, MapStruct, the gateway's blocking-execution routing, and the library semantics that bite at runtime.
---

# JVM development

Load this pack before you choose, write, or review JVM production code, or before you form the first hypothesis or run
the first diagnostic experiment in a JVM runtime diagnosis. It carries the JVM content the root `AGENTS.md` used to
hold; the root's classification table owns when it loads.

Source: `AGENTS.md` `## Conventions` (Lombok, MapStruct, ErrorProne, the WebFlux blocking-execution routing) and
`## Gotchas` → `### Runtime code, libraries & Spring`.

Root guidance this pack does not restate: the no-comments rule, the Javadoc/JSDoc convention, the redundancy rule, and
the formatting convention stay in `AGENTS.md` and apply here.

## Conventions

- **Lombok**: use Lombok where possible (e.g., `@RequiredArgsConstructor`, `@Data`, `@Builder`, `@Value`) instead of
  writing boilerplate manually; `addNullAnnotations = jspecify`; copyable annotations include `@Qualifier` and `@Value`.
  The command/event/query/DTO value types are Lombok `@Value` classes carrying `@Builder` (`toBuilder = true` on the
  three types whose variants tests derive — `ScheduleShowcaseCommand`, `Showcase`, `ShowcaseEntity`) rather than Java
  records, and the builder is the reason: the aggregate and saga construct events and commands through `builder()`
  (`ShowcaseScheduledEvent.builder()`, `StartShowcaseCommand.builder()`), and tests derive variants via `toBuilder()` —
  a record's canonical constructor cannot express that. The value classes carry `@SuppressWarnings("ClassCanBeRecord")`
  to encode the choice; delete one only if the type genuinely needs no builder, and omit it where the class is already
  not record-convertible (an initialized `@Builder.Default` field is one such case — IntelliJ flags the suppression as
  redundant there). A `@Builder.Default` on a `@Jacksonized` value class guards only an omitted JSON key: an explicit
  `null` sets the field to `null` (`@Size` ignores a `null`), so a builder-defaulted collection also carries `@NotNull`
  or the request NPEs to a 500; a collection that should instead _tolerate_ an omitted or `null` list uses
  `@Singular(ignoreNullCollections = true)`, which makes both empty (as `FetchShowcaseListQuery` does). Whether records
  would be faster is **untested** — the suppression records intent, not a measured result, so do not read it as a
  performance claim. Declare local variables with Lombok's `val` when the initializer infers the type and the local is
  not reassigned, and with the language's `var` when it is reassigned (Lombok's `var` cannot be imported on Java 10+ —
  the compiler rejects `import lombok.var;` as a restricted type); keep an explicit type where inference is impossible
  or would change the type — fields, method parameters and returns, a diamond initializer with no target type
  (`new TreeMap<>()`), a declaration with no initializer or more than one declarator, a
  `null`/lambda/method-reference/array initializer, a declared boxed type the initializer would unbox, or a declared
  primitive wider than the initializer. captured: add-web-ui-rum-observability
- **MapStruct**: default component model is `spring` (`-Amapstruct.defaultComponentModel=spring`)
- **ErrorProne**: NullAway on `showcase.*` packages; disabled in `TestJava` tasks. Every package in a NullAway-compiled
  source set (`main`, `testFixtures`, and `load-tests`' `gatling`) carries a `package-info.java` with `@NullMarked`
  (`org.jspecify.annotations.NullMarked`), so the nullness contract is declared for the whole package.
  `UnrecognisedJavadocTag` is suppressed where it fires (`@SuppressWarnings("UnrecognisedJavadocTag")` on the
  Lombok-processed class): it is a false positive from Lombok's copy of a field Javadoc carrying an inline tag, so the
  Javadoc's wording is not the defect — only removing the tag or suppressing the check clears it. Tracked upstream as
  `google/error-prone#5855` (open, the pinned 2.50.0); retires when an ErrorProne bump past the fix lands. captured:
  suppress-lombok-javadoc-false-positive

- **The WebFlux blocking-execution routing in the gateway is load-bearing — do not remove it as a BlockHound band-aid.**
  Every controller method routes to the bounded-elastic scheduler; removing it reddens the error and validation paths
  (the gateway's invalid payloads become `500` instead of `400`) with zero BlockHound hits either way. The rules and the
  measured evidence live in the spec that owns the outcome — `gateway/rest-api` (`Command error translation`,
  `Query error translation`) — and in `design.md` of the change that moved them there
  (`extract-specd-rationale-from-agents-md`). Do not read the `@WebFluxTest` component-scan the routing forces as
  removable complexity. The query service carried the same routing until its HTTP controller was replaced by the generic
  gRPC transport (`grpc-query-transport`), which is why its error and validation outcomes are gRPC statuses now rather
  than HTTP problem details.

## Runtime code, libraries & Spring

- **A controller-local `@ExceptionHandler(HandlerMethodValidationException.class)` fires only when method validation is
  active for that controller — put a constraint (`@NotNull`) directly on the method parameter, not only `@Valid`.**
  Without it a body failure arrives as a `WebExchangeBindException`, so the shared `ShowcaseApiErrorResolver`'s
  `bodyErrors` contract is silently lost (`ShowcaseRestController` gets activation incidentally from the `@Min`/`@Max`
  on its page-size parameter). A component test that posts a malformed body and asserts `bodyErrors` covers it.
  captured: add-web-ui-rum-observability

- **A new gateway controller package or browser-called endpoint is silent until registered in two un-gated lists.** Add
  the package to `springdoc.packages-to-scan` (else the endpoint is absent from `/v3/api-docs`) and, for a path the
  browser calls, add it to the Vite proxy in `showcase-web-ui/vite.config.ts` (else it never reaches `viteDev` or the
  e2e `vite preview`); widen the live endpoint enumerations per the docs-refresh convention too. captured:
  add-web-ui-rum-observability

- **NullAway is strict on `showcase.*` packages — ensure proper `@Nullable`/`@NonNull` annotations from `jspecify`.** A
  missing or wrong annotation fails the module's `compileJava`.

- **Jackson 3 artifacts (`tools.jackson.core:*`) on the query-service and projection-service runtime classpaths are
  dependency hygiene, not the deferred Jackson 3 backend migration.** IF you see `tools.jackson.core:*` on those
  classpaths, THEN do not treat it as a migration to complete and do not switch application code to Jackson 3 — Jackson
  2 remains the serialization backend. The why (including the `jackson3-bom` constraint) lives in ADR-0003.

- **Spring Data Elasticsearch's `DateFormat.strict_date_optional_time_nanos` truncates to microseconds despite its
  name.** The nanosecond pattern and the reason live in the `read-side/projection-model` spec. `ShowcaseEntity` declares
  the custom `NANOS_DATE_PATTERN` (`yyyy-MM-dd['T'HH:mm:ss.SSSSSSSSSXXX]`) with `format = {}`; do not "simplify" it back
  to the built-in enum until the fix (PR #3337, 6.2.0-M2) reaches us through `spring-data-opensearch`; the truncation is
  invisible on macOS (microsecond clocks) and surfaces only on nanosecond clocks (Linux CI).

- **Extracting a component that exists in a sibling service means mirroring the sibling's full wiring, not the shape you
  copied.** The gateway's Kafka converter was lifted from the projection service without its Axon `upcasterChain`, and
  its event receiver from `ShowcaseProjector` without the `ObservationRegistry` — both silent until review, and no gate
  catches either. Grep the sibling for every configuration call it makes on the same component and carry each across: a
  missing upcaster chain deserializes events un-upcast and a missing registry drops their traces. captured:
  read-through-gateway-caches

- **A generic getter's inferred type can bind an overloaded call to the wrong overload — cast to the erased type, and
  verify the binding, not the source.** `Session.get` is generic (`<T> T get(String)`), so
  `String.valueOf(session.get(attr))` inferred `T = char[]` (the most specific applicable overload) and called
  `String.valueOf(char[])`; the source reads correct and only the run failed
  (`ClassCastException: class java.lang.Integer cannot be cast to class [C`). Write
  `String.valueOf((Object) session.get(attr))` — `javap` then shows `String.valueOf:(Ljava/lang/Object;)` — or bind the
  value to an `Object` local. A compile-valid call proves only that some overload matched; confirm the binding with
  `javap -c` or a run. captured: anchor-the-load-test-baseline

- **Caffeine's `AsyncCache` trips a Reactor bridge on two contracts — a non-null value type and an empty source — and
  its access expiry never bounds a hot entry.** `getIfPresent` returns `null` for a future that completed exceptionally,
  so a failed cached future is indistinguishable from a miss: the `if (present) … else …` shape takes the miss branch, a
  `future.thenAccept(…)` callback only runs on success, and the future ErrorProne's `FutureReturnValueIgnored` flags can
  never leave a sink hanging — so a `@SuppressWarnings("FutureReturnValueIgnored")` on such a method is **redundant**,
  not a hidden bug (verified against Caffeine 3.3.0). Prefer `Mono.fromFuture(…)` over a raw sink-plus-`thenAccept`:
  Reactor observes the future and the empty-vs-failed distinction stays with the cache's own contract. The reverse
  bridge — a `Mono` fed into `AsyncCache.get`'s mapping callback — needs a helper: `Mono.toFuture()` is typed
  `CompletableFuture<@Nullable T>`, and a fresh future completed from
  `source.subscribe(future::complete, future::completeExceptionally)` never completes on an empty source, so guard it
  with `switchIfEmpty(Mono.error(…))` or that entry wedges forever. The callback runs on the calling thread, so a
  subscription created inside it does not inherit the caller's Reactor context — carry it with
  `Mono.deferContextual(ctx -> … contextWrite(ctx) …)` or the trace link breaks. A cache's freshness bound is
  `expiresAfterWrite`: every read resets an access expiry, so `expiresAfterAccess` never bounds a hot entry. captured:
  read-through-gateway-caches
