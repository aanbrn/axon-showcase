# Proposal

## Why

Axon Framework has no distributed query bus without Axon Server: `DistributedQueryBus` ships in the excluded
`axon-server-connector`, and the JGroups extension distributes commands only. The gateway's query transport —
`ShowcaseQueryController` plus `ShowcaseQueryClient` — is a hand-rolled stand-in: a generic Axon `StreamingQueryMessage`
wrapped in a protobuf envelope over HTTP, with the response returned as JSON the gateway decodes and re-encodes. Moving
that generic dispatch onto a gRPC service gives the query side a standard service-to-service transport (HTTP/2
streaming, status codes) without Axon Server and without losing the generic, name-routed dispatch.

## What Changes

- `showcase-query-proto/src/main/proto/showcase-query.proto` — add a `ShowcaseQueryTransport` gRPC service with a single
  generic `rpc Dispatch(QueryRequest) returns (stream QueryResponse)`, and a `QueryResponse` message mirroring
  `QueryRequest` (payload type/revision, serialized payload, serialized metadata).
- `showcase-query-proto/build.gradle.kts` — generate gRPC stubs (the `grpc` plugin of the existing
  `protobuf-gradle-plugin`).
- `showcase-query-service` — replace the `@RestController` (`ShowcaseQueryController`) with a gRPC service
  implementation that dispatches the reconstructed Axon query on the `QueryBus`, serializes each response into
  `QueryResponse`, and maps failures to gRPC statuses; add the gRPC server (port, lifecycle) and its properties.
- `showcase-query-client` — replace the `WebClient` transport in `ShowcaseQueryClient` with a gRPC channel + stub
  (generic `Dispatch`), decode `QueryResponse` payloads, keep the Resilience4j time limiter / circuit breaker / retry
  over the calls, remap gRPC statuses to `ShowcaseQueryException`, and change `showcase.query.api-url` from an HTTP URL
  to a gRPC target.
- `showcase-query-client` / `showcase-query-service` build files + `platform`/`libs.versions.toml` — add the gRPC
  library (and the grpc-ecosystem Spring Boot starter if the compatibility spike passes; otherwise raw grpc-java).
- `helm/chart` — expose the query-service gRPC container port and Service port, and admit it in the query-service
  NetworkPolicy.
- Specs: `showcase/clients/query-client`, `showcase/read-side/query-service`, `showcase/deployment/helm-chart` (below).
- `docs/adr/` — a new ADR recording the transport decision and the generic-vs-typed trade.
- `AGENTS.md` / `README.md` — the transport description and the command/query distribution story.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/clients/query-client`: the transport, error translation, retry-status set, and configuration change from the
  HTTP + protobuf envelope to the generic gRPC RPC.
- `showcase/read-side/query-service`: the transport requirement changes from two HTTP endpoints to one generic gRPC RPC,
  and the error/validation requirements change from HTTP problem details to gRPC statuses (dropping the WebFlux
  bounded-elastic routing rationale, which is HTTP-specific).
- `showcase/deployment/helm-chart`: the query-service Deployment/Service expose a gRPC port, and its NetworkPolicy
  admits it.

## Impact

- **Code:** `showcase-query-proto`, `showcase-query-service`, `showcase-query-client`, the `platform`/catalog, the
  chart, and the query-client/query-service tests (the client CT's WireMock HTTP mock is replaced by an in-process gRPC
  server). No change to the gateway's REST/JSON/SSE surface, the web UI, or the write side.
- **Dependencies:** a gRPC library family is added (grpc-java and/or the grpc-ecosystem Spring Boot starter); it must be
  pinned in the catalog, added to the platform BOM where applicable, and cleared by `dependencySecurityCheck`.
- **Deployment:** the query service gains a gRPC port (container port, Service, NetworkPolicy); the gateway's
  `SHOWCASE_QUERY_SERVICE_URL` becomes a gRPC target, and the chart renders it.
- **Risk:** the community starter's latest release (`3.1.0.RELEASE`) targets Spring Boot 3.2, so its fit on 3.5.16 is
  unverified — the change opens with a timeboxed compatibility spike and falls back to raw grpc-java if it fails.
