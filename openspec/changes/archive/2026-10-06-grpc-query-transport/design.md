# Design

## Context

See `proposal.md` — Why. Today the gateway's `ShowcaseQueryClient` (`showcase-query-client`) serializes an Axon
`StreamingQueryMessage` into a protobuf `QueryRequest` and POSTs it to the query service's `ShowcaseQueryController`
(`showcase-query-service`), which reconstructs the Axon message, dispatches it on the local `QueryBus`, and returns the
responses. This hand-rolled transport exists because Axon Framework has no distributed query bus without Axon Server:
`DistributedQueryBus` lives in the excluded `axon-server-connector`, and the JGroups extension distributes commands
only, so the command side scales via JGroups while the query side has this stand-in.

The transport's shape is deliberate: the envelope carries any Axon query message generically (payload type + revision +
metadata + response type), and the receiver dispatches by handler name. Only the response is not generic — it is the
concrete `Showcase` DTO as JSON.

## Goals / Non-Goals

**Goals:**

- Move the generic query dispatch onto gRPC, keeping the generic envelope and the `QueryBus` dispatch on the receiver.
- Preserve the resilience protection (time limiter, circuit breaker, retry) and the business-error contract across the
  transport.
- Keep the bootRun/container/Helm surfaces coherent (a gRPC port and target).

**Non-Goals:**

- A **typed** gRPC API (one RPC per query) — that is a client-server API, not a bus, and would drop the name-routed
  generic dispatch (see D1).
- Handler routing, discovery, or scatter-gather across query-service nodes — the query side is homogeneous, so a
  Kubernetes Service already load-balances; a true `DistributedQueryBus` remains future (see D6).
- Changing the gateway's REST/JSON/SSE surface, the web UI, or the write side.

## Decisions

**D1 — Generic `Dispatch`, not typed RPCs.** The RPC is `Dispatch(QueryRequest) returns (stream QueryResponse)`: one
method carrying any query, so the client still sends the same Axon query object the handler receives and the receiver
still routes by name. _Alternatives:_ a typed `service` (one method per query) — rejected: it is a client-server API,
not a bus, and abandons the generic dispatch this transport exists to provide.

**D2 — Integration: the grpc-ecosystem starter, behind a spike, with raw grpc-java as the fallback.** The first task
proves `net.devh:grpc-spring-boot-starter:3.1.0.RELEASE` (grpc-ecosystem, Apache-2.0) boots on Spring Boot 3.5.16 and
runs a `Dispatch` round-trip; its latest release targets Boot 3.2, so that is unverified. If it does not fit, fall back
to raw grpc-java (`io.grpc:grpc-bom`, latest) with a hand-wired server lifecycle bean and channel. _Alternatives:_
Spring gRPC (`org.springframework.grpc`) — rejected: its 1.0 line targets Spring Boot 4, which ADR-0004 defers.

**D3 — One server-streaming RPC.** Both operations go through `Dispatch`; `fetchById` takes the first response (its
`NOT_FOUND` arrives as a gRPC status). A `QueryResponse` message carries the response type, revision, serialized
payload, and serialized metadata, mirroring `QueryRequest`, so responses are generic rather than typed.

**D4 — Keep the `QueryBus` behind the RPC.** The gRPC handler reconstructs the Axon message and dispatches it on the
receiver's `QueryBus`; it does not call the handler directly. This preserves the CQRS/Axon query dispatch the app
demonstrates while the transport changes.

**D5 — Errors as gRPC status + a `field-errors-bin` trailer.** Business errors map to `INVALID_ARGUMENT` (carrying the
field errors) and `NOT_FOUND`; infrastructure failures to `UNAVAILABLE`, timeouts to `DEADLINE_EXCEEDED`, unknown to
`INTERNAL`. The client remaps back to `ShowcaseQueryException` / the retry filter, which retries only a retryable
failure: a retryable gRPC status (`UNAVAILABLE`, `DEADLINE_EXCEEDED`, `RESOURCE_EXHAUSTED`, `ABORTED`) or an operation
timeout.

**D6 — Config: `showcase.query.api-url` becomes `showcase.query.target` (a gRPC target).** An HTTP-URL name and `@URL`
constraint no longer fit; the property, its yml placeholder, the image default, and the chart value move to a gRPC
target. A homogeneous query side is reached through the Kubernetes Service, so no client-side routing is needed yet.

**D7 — A dedicated gRPC port (default 9090).** The query-service Deployment, Service, and NetworkPolicy expose it, under
the same ingress rule as the server port (the gRPC port is internal like the server port).

## Risks / Trade-offs

- **The starter may not fit Boot 3.5** → D2's spike decides it as task 1; raw grpc-java is the fallback, and the ADR
  records which held.
- **A second Netty (gRPC's) alongside Reactor Netty** → prefer `grpc-netty-shaded`; align versions via the catalog/BOM
  and clear `dependencySecurityCheck`.
- **Server-streaming backpressure is not automatic in grpc-java** (`StreamObserver` has no pull-based flow control
  without `ServerCallStreamObserver.isReady`) → acceptable: the bounded list (≤ the page size) is small; noted rather
  than engineered.
- **Spec churn** — the transport, validation, and error requirements change subject, so the delta uses REMOVED/ADDED
  rather than MODIFIED where a scenario (a WebFlux-routing clause, a 404 problem detail) no longer applies → reviewed in
  the change's spec deltas.
- **The bus is still point-to-point** — this modernizes the transport, not the distribution; handler routing/scatter-
  gather remain future work (Non-Goals, D6).

## Migration Plan

None at the data layer — a transport swap with no persisted state; both services ship together. Rollback is the revert.

## Open Questions

None that change the plan: the one open point (which integration holds) is resolved by the task-1 spike, not assumed.
