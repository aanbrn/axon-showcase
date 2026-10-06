# ADR-0017: Route queries over a generic gRPC transport, not Axon Server

Date: 2026-10-06

Status: Accepted

Revisit when: `org.springframework.grpc` publishes a release supporting a Spring Boot line this project runs (the
project tracks Boot 3.x; ADR-0004 defers Boot 4, which its 1.0 line targets) — the signal is a first-party Spring gRPC
release targeting Boot 3.x, at which point the community starter this decision depends on can be replaced.

## Context

Axon Framework has no distributed query bus without Axon Server: `DistributedQueryBus` ships in the
`axon-server-connector` excluded everywhere (ADR-0009), and the JGroups extension distributes commands only. The query
side therefore needed its own transport for the gateway to read from the query service — the only reader of OpenSearch.

That transport was hand-rolled: the gateway's `ShowcaseQueryClient` serialized an Axon `StreamingQueryMessage` into a
protobuf envelope and POSTed it to the query service's `ShowcaseQueryController`, which reconstructed the Axon message,
dispatched it on the local `QueryBus`, and returned the responses as JSON that the gateway decoded and re-encoded. The
envelope is **generic** — it carries any Axon query by name, not one field shape per query — because the receiver
dispatches by handler name, exactly as an in-process `QueryBus` does.

HTTP + a protobuf body re-invents what a service-to-service RPC already provides (streaming, status codes). The generic
envelope is worth keeping; the transport around it was not.

## Decision

Move the generic query dispatch onto **gRPC**, with a single method carrying any query:

- `service ShowcaseQueryTransport { rpc Dispatch(QueryRequest) returns (stream QueryResponse); }` — one method for every
  query. `QueryRequest` carries the query name, identifier, payload type + revision, serialized payload, serialized
  metadata, and expected response type; `QueryResponse` mirrors it. The gRPC handler reconstructs the Axon message and
  dispatches it on the receiver's `QueryBus`, so the CQRS/Axon query dispatch the app demonstrates is unchanged.
- **Integration:** the grpc-ecosystem starter (`net.devh:grpc-spring-boot-starter:3.1.0.RELEASE`, Apache-2.0) with
  grpc-java 1.84 and protobuf 4.36, proven by a timeboxed compatibility spike to boot on Spring Boot 3.5.16 before
  commit. Raw grpc-java with a hand-wired server lifecycle and channel was the fallback, and was **not** needed.
- **Errors:** business errors as gRPC status — `INVALID_ARGUMENT` (carrying the field errors in a `field-errors-bin`
  trailer), `NOT_FOUND`; infrastructure failures as `UNAVAILABLE`, timeouts as `DEADLINE_EXCEEDED`, unknown as
  `INTERNAL`. The client remaps `INVALID_ARGUMENT`/`NOT_FOUND` back to `ShowcaseQueryException`, and retries only a
  retryable failure: a retryable gRPC status (`UNAVAILABLE`, `DEADLINE_EXCEEDED`, `RESOURCE_EXHAUSTED`, `ABORTED`) or an
  operation timeout.
- **Configuration:** `showcase.query.api-url` (an HTTP URL with a `@URL` constraint) becomes `showcase.query.target`, a
  gRPC target (`SHOWCASE_QUERY_SERVICE_TARGET` in the chart). The query service gains a dedicated gRPC port (default
  `9090`) exposed by its Deployment, Service, and NetworkPolicy alongside its management HTTP port.

_Alternatives considered:_ **Typed RPCs** (one method per query) — rejected: that is a client-server API, not a bus, and
abandons the generic name-routed dispatch the transport exists to provide. **Spring gRPC** (`org.springframework.grpc`)
— rejected: its 1.0 line targets Spring Boot 4, which ADR-0004 defers. **Axon Server** — rejected for the same reason as
ADR-0009: its commercial licensing is a cost the project deliberately avoids.

## Consequences

- The query side gets a standard service-to-service transport: HTTP/2 streaming and status codes, with the generic
  envelope and `QueryBus` dispatch preserved. This **reimplements a slice of the excluded Axon Server connector**:
  `DistributedQueryBus` is what Axon Server's connector would otherwise provide, so this transport is that role's
  stand-in — and, like the JGroups command bus, it stays **point-to-point**. The query side is homogeneous, so a
  Kubernetes Service load-balances across its pods; handler routing, discovery, and scatter-gather across query-service
  nodes remain future work.
- A second Netty (gRPC's, via `grpc-netty-shaded`) runs alongside Reactor Netty. The query service's HTTP server has no
  API left to serve, so its `server.port` is vestigial: the reactive server must still bind a port, and its management
  server shares it (the chart's query-service ports are the management and gRPC ports — no separate `server` port).
- The dependency-update and dependency-security checks now cover the gRPC family; the community starter lags the Boot
  line it runs on (its latest release targets Boot 3.2 while this project runs 3.5.16), which is the `Revisit when:`
  above.
- gRPC server-streaming carries no pull-based backpressure without `ServerCallStreamObserver.isReady`; acceptable here —
  a list response is bounded by the page size — and noted rather than engineered.
- The client module drops its WebFlux dependency (it no longer builds a `WebClient`); its Jackson dependencies are
  declared explicitly (`jackson-databind`, `jackson-datatype-jsr310`) rather than arriving through the unused starter.
