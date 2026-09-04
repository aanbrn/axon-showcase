## Why

The CQRS/Event-Sourcing pipeline is today only demonstrable via the REST API and `curl`: there is no visual way to
create and browse showcases or, crucially, to watch domain events flow through the pipeline. The showcase's pedagogical
value is the event-sourced lifecycle (`SCHEDULED` → `STARTED` → `FINISHED`), and a web-based UI that both drives the
lifecycle and displays the live event stream makes that visible.

The design intentionally keeps the CQRS separation intact: historical timeline data comes from the existing `Showcase`
read model (`scheduledAt`, `startedAt`, `finishedAt`), and live events come from the Kafka topic the command service
already publishes to — the gateway never reads the Axon event store and no duplicated events index is projected.

## What Changes

- **New `showcase-web-ui` module**: a standalone React single-page application (Vite + TypeScript) built as a Node
  module in the Gradle monorepo via the node-gradle plugin, served independently and talking to the gateway over CORS.
- **Gateway live-events endpoint**: a new WebFlux `GET /events` (Server-Sent-Events) endpoint streaming real domain
  events, backed by a new Kafka consumer in the gateway that subscribes to the existing `axon-showcase-events` topic
  with its own consumer group and decodes messages with the Axon event serializer (mirroring the projection service).
- **Gateway CORS configuration**: permit the standalone UI origin to call the `/showcases` REST API and `/events` SSE
  endpoint (no CORS config exists today).
- **UI feature set**: browse showcases, create/start/finish/remove showcases via the existing REST endpoints, render a
  per-showcase history timeline from the read-model timestamps, and append live events to the timeline as they arrive
  over SSE.

## Capabilities

### New Capabilities
- `clients/web-ui`: a standalone web UI that browses and drives showcase lifecycle actions over the gateway REST API
  and renders a live event timeline from the gateway SSE stream.
- `gateway/live-events`: a gateway SSE endpoint that streams real domain events consumed from Kafka, so clients can
  observe the event flow live without reaching into the write side.

### Modified Capabilities
- `gateway/api-gateway`: CORS configuration allowing the standalone UI origin to access the gateway REST and SSE
  endpoints.

## Impact

- **New module**: `showcase-web-ui` (Vite + React + TypeScript) added to `settings.gradle.kts`; Node pinned in
  `gradle/libs.versions.toml`; a new `frontend-conventions` build-logic plugin wires node-gradle into the module.
- **Gateway**: `axon-kafka` + `reactor-kafka` dependencies (both already in the catalog), a Kafka consumer bean
  (mirroring `ShowcaseProjector`), the `/events` SSE endpoint, and a CORS `WebFluxConfigurer`.
- **Build**: `package-lock.json` becomes a new pinned source-of-truth surface; frontend lint/build/test wired into
  `check`.
- **No changes** to the command, projection, or query services; no event-store reads; no new projected index.
- **Specs**: new `clients/web-ui` and `gateway/live-events` capability specs, plus a delta on
  `gateway/api-gateway` for CORS.