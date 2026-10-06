# Tasks

## 1. Compatibility spike (decides the integration)

- [x] 1.1 Add the gRPC pins to `gradle/libs.versions.toml` and the `platform` BOM (start with
      `net.devh:grpc-spring-boot-starter:3.1.0.RELEASE` and `io.grpc:grpc-bom`), wire them into `showcase-query-service`
      and `showcase-query-client`, and verify the query-service Spring context boots on Spring Boot 3.5.16
      (`./gradlew :showcase-query-service:test` plus a context test). If the starter does not fit, switch to raw
      grpc-java and record the outcome. **Outcome: the grpc-ecosystem starter (with grpc-java 1.84 + protobuf 4.36)
      boots on Boot 3.5.16 — no fallback needed; the only issue was a gRPC port bind across test contexts, fixed with an
      ephemeral test port.**
- [x] 1.2 Prove a minimal generic `Dispatch(QueryRequest) returns (stream QueryResponse)` round-trip (a proto stub plus
      an in-process gRPC server) with an Axon-serialized payload and a gRPC non-OK status carrying a `field-errors-bin`
      trailer; record the outcome and the integration chosen. **Outcome: the round-trip holds — an Axon-serialized
      `Showcase` payload streams back and an `INVALID_ARGUMENT` status carrying a `field-errors-bin` trailer is
      translated, proven end to end by the query-service `ShowcaseQueryTransportServiceCT` (the handler) and the query
      client `ShowcaseQueryClientCT` (a Netty gRPC server on an ephemeral port); the integration is the grpc-ecosystem
      starter (D2).**

## 2. Query transport

- [x] 2.1 In `showcase-query-proto` add the `ShowcaseQueryTransport` service and a `QueryResponse` message (response
      type, revision, serialized payload, serialized metadata) to `showcase-query.proto`, generate the stubs (the `grpc`
      plugin of `protobuf-conventions` / the module's `build.gradle.kts`), extend the module's
      `coverage.generatedClassExcludes` to the new generated classes, and verify
      `./gradlew :showcase-query-proto:compileJava` compiles the generated sources.
- [x] 2.2 In `showcase-query-service` implement the `Dispatch` service (reconstruct the Axon message, dispatch on the
      `QueryBus`, serialize each `QueryResponseMessage` payload into a `QueryResponse`, map failures to gRPC statuses)
      and configure the gRPC server (port, lifecycle) — baking `BPE_DEFAULT_GRPC_SERVER_PORT=9090` into the image's
      `bootBuildImage` map, mirroring `BPE_DEFAULT_SERVER_PORT`; remove `ShowcaseQueryController` and its HTTP
      problem-detail handling, and remove or rewrite `ShowcaseQueryControllerIT`; verify
      `./gradlew :showcase-query-service:test`.
- [x] 2.3 In `showcase-query-client` replace the `WebClient` transport in `ShowcaseQueryClient` with the gRPC channel +
      stub, decode `QueryResponse` payloads, keep the Resilience4j time limiter / circuit breaker / retry, and remap
      gRPC statuses to `ShowcaseQueryException` / the retry filter; rename `showcase.query.api-url` to
      `showcase.query.target` (the Java field) as a gRPC target; verify `./gradlew :showcase-query-client:test`.
- [x] 2.4 In `showcase-api-gateway` update the renamed property's surfaces — `application.yml`
      (`showcase.query.api-url`), the `bootBuildImage` `BPE_DEFAULT_SHOWCASE_QUERY_SERVICE_URL` map value, and the
      `ShowcaseApiPropertiesCT` assertions — to the gRPC target; verify `./gradlew :showcase-api-gateway:componentTest`.

## 3. Deployment and dependencies

- [x] 3.1 In `helm/chart` expose the query-service gRPC port (container port, Service port, NetworkPolicy rule) and the
      gRPC target env; verify `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal`.
- [x] 3.2 Confirm the gRPC coordinates are pinned in `gradle/libs.versions.toml` / the platform, run
      `./gradlew dependencyUpdates` and `./gradlew dependencySecurityCheck`, and — if a transitive version constraint or
      a `.snyk` suppression is added (the `zstd-jni` precedent) — add the matching
      `showcase/quality/dependency-security` delta and note it in the report. **Outcome: both run green — the gRPC
      family is catalog/BOM-pinned, `dependencySecurityCheck` passes with no new finding and no `.snyk` suppression
      added, and `dependencyUpdates` reports only pre-existing rows unrelated to this change (the known `log4j-core`
      build-environment constraint row and the `jetty-bom`/`jetty-ee10-bom` minors), so no delta is owed.**

## 4. Documentation

- [x] 4.1 Write the ADR under `docs/adr/` recording the transport decision: generic dispatch over gRPC without Axon
      Server, the generic-vs-typed trade, the integration choice and its fallback, and the note that this reimplements a
      slice of the excluded Axon Server connector.
- [x] 4.2 Refresh `AGENTS.md` and `README.md` for the new transport and the distribution story (commands via JGroups,
      queries via gRPC), and remove any `docs/ideas.md` entry this implements.
- [x] 4.3 Refresh the `showcase/clients/query-client` and `showcase/read-side/query-service` `## Purpose` sections in
      the archive commit (a delta cannot carry a `Purpose`), since both Purposes describe the HTTP/protobuf endpoints
      and problem-detail errors this change removes — including the query-client `Contract source:` line that names the
      removed `/streaming-query` and `/query` endpoints. **Outcome: both Purposes refreshed in the archive commit — the
      query client's now names the generic gRPC `Dispatch` RPC and drops the `/streaming-query`/`/query` endpoints from
      its `Contract source:` line; the query service's now names the generic gRPC query transport.**

## 5. Verification

- [x] 5.1 Run
      `./gradlew :showcase-query-proto:check :showcase-query-service:check :showcase-query-client:check :showcase-api-gateway:check -PskipITs -Pcoverage.gate.enabled=false`
      and confirm the unit, component, and static-analysis gates pass. **Outcome: all four modules pass (also
      `spotlessCheck`, the chart's `helmLintMainChartFull`/`Minimal`, and `openspec validate --changes`).**
- [x] 5.2 Run `./gradlew :showcase-query-service:integrationTest` (requires Docker; boots a real OpenSearch) and confirm
      it passes. (The query client has no `integrationTest` suite — its transport is exercised by the client
      `componentTest` against an in-process gRPC server and by the gateway `e2eTest`.) **Outcome: green — the
      OpenSearch-backed suite passed (Docker).**
- [x] 5.3 Run `./gradlew :showcase-api-gateway:e2eTest` (requires Docker; boots the full four-service pipeline) and
      confirm the read path works over the new transport end to end. **Outcome: green — the gateway e2e passed, driving
      the full command → Kafka → projection → query pipeline over the new gRPC transport.**
- [x] 5.4 Run the `lesson-capture` subagent over the implementation diff and review findings, apply its durable
      proposals, record the applied net `AGENTS.md` delta on this task, and re-run `./gradlew spotlessApply` +
      `spotlessCheck` after the final edit. **Outcome: all five proposals applied — four merges into existing bullets
      (the dependency-load-bearing bullet, the test-display-names bullet, the IDE-inspections bullet, and the
      rename/removal-sweep bullet) plus R1's replacement of the now-obsolete query-service evidence point — each
      carrying `captured: grpc-query-transport`; no new bullet was added. `AGENTS.md` grows by 18 lines vs `main` for
      the whole change (this change's docs-refresh edits included); `spotlessApply`/`spotlessCheck` and
      `verifyCapturedMarkers` pass after the final edit.**
