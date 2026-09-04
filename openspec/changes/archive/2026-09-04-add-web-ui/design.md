## Context

See proposal.md — Why. Current state relevant to the design: the gateway is a WebFlux app that already participates in
the distributed command bus (JGroups) and already depends on Axon; the projection service already demonstrates the exact
Kafka consumer + Axon-event-serializer pattern this change mirrors (`ShowcaseProjector`, `DefaultKafkaMessageConverter`
wired with the `eventSerializer` bean). The repo is a pure-JVM Gradle monorepo with convention plugins in `build-logic`;
no Node tooling, no static resources, no CORS config exist today.

## Goals / Non-Goals

**Goals:**
- Standalone React UI as a first-class Gradle module (`showcase-web-ui`), built reproducibly and wired into `check`.
- Gateway SSE endpoint streaming real domain events from Kafka, coexisting with the projection consumer.
- CORS for the UI origin only.

**Non-Goals:**
- No gateway reads of the Axon event store (write-side boundary respected).
- No projected events index (no duplicate of history the read model already carries).
- No authentication for the UI (consistent with the rest of the gateway).
- No embedding of the UI in the gateway jar — the UI is served independently.

## Decisions

### 1. Node-in-Gradle via the node-gradle plugin

Use the `com.github.node-gradle.node` plugin in a new `frontend-conventions` build-logic plugin. It downloads a pinned
Node distribution (version added to `gradle/libs.versions.toml`), manages npm, and exposes `npm ci` / `npm run build`
/ `npm run lint` / `npm run test` as Gradle tasks wired into the module's `check`.

- **Alternative (rejected):** shelling out to a system `npm` — non-reproducible, depends on the developer's Node.
- **Alternative (rejected):** a separate non-Gradle repo — breaks the monorepo single-build model this repo values.

### 2. React + Vite + TypeScript, full modern stack

Vite as the dev server and build tool (fast, standard for React SPAs), React 18 + TypeScript for the UI. `dist/` is the
produced static bundle, served by any static host in prod and by Vite in dev.

Server state is managed with **TanStack Query** (`useQuery` for the list, `useMutation` for create/start/finish/remove),
which provides caching, invalidation, and the loading/error lifecycle for free. Client-only state (selected showcase,
live events) lives in a **Redux Toolkit** slice with typed `useAppDispatch`/`useAppSelector` hooks. Forms use **React
Hook Form + Zod** for schema-driven, typed validation. Code style is enforced by **Prettier** (`format:check` in
`check`) plus ESLint (which also enforces the SPDX header via `eslint-plugin-header`); exports carry JSDoc describing
their purpose, mirroring the Java modules' Javadoc convention.

- **Alternative (rejected):** hand-rolled hooks + `useReducer` — works but reinvents caching/retry/invalidation that
  TanStack Query provides, and diverges from the mainstream stack a reference app should demonstrate.
- **Alternative (rejected):** Thymeleaf/HTMX server-rendered from the gateway — clashes with WebFlux's reactive model
  and with the "beautiful SPA" goal.

### 3. Feature-Sliced Design for the frontend structure

The UI is organized per **Feature-Sliced Design** (FSD): `app/` (composition root + store), `pages/` (route-level
composition), `widgets/` (reusable compositions of entities/features), `features/` (user interactions such as
create-showcase and showcase-actions), `entities/` (showcase and showcase-event domain logic + data access), and
`shared/` (cross-slice primitives). Slices import only downward through this graph; the `@/` path alias maps to `src/`.

- **Alternative (rejected):** a flat `components/` + hooks layout — less instructive for a reference app and does not
  make the layering explicit.

### 4. Live events: gateway Kafka consumer + SSE, mirroring the projection service

Add `axon-kafka` + `reactor-kafka` to the gateway (both already in the catalog). A `KafkaReceiver` bean consumes the
`axon-showcase-events` topic with a consumer group distinct from the projection's, decodes messages with
`DefaultKafkaMessageConverter` + the Axon `eventSerializer` (same wiring as `ShowcaseProjector`), and a `@GetMapping(
path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)` endpoint publishes the decoded events as a Flux.

- **Alternative (rejected):** a dedicated new events service — adds a fifth service for what the gateway already hosts
  well (it is the only external-facing service and is already reactive).
- **Alternative (rejected):** exposing the Axon event store or a projected events index — both rejected on CQRS
  grounds in the proposal.

### 5. History from the read model, live events appended, list reconciled

The UI renders a showcase's history timeline from `scheduledAt`/`startedAt`/`finishedAt` (already in `GET /showcases`
responses) and appends live events from SSE to the same timeline. This keeps history and live in one view with no
deduplication concern: history is derived from timestamps, live events are the actual event stream.

Because the command and query sides are eventually consistent, the UI reconciles the list against the read model after
writes. Reconciliation is driven **solely by the live event stream** — mutations only confirm the write, and the
matching SSE event triggers a `waitForReadModel` poll (a forced `queryClient.query`) until the event's effect is
visible. Reconciliations are coalesced per showcase (latest-wins), so a saga burst (SCHEDULED → STARTED → FINISHED)
runs one poll tracking the newest expected state rather than one per event; events replayed from history on a new SSE
connection are skipped so an initial connect does not poll for already-projected state. The poll interval (500ms) is
tuned to the projection's latency, which bounds how many requests each reconciliation issues.

- **Alternative (rejected):** have mutations reconcile in `onSuccess` in addition to the SSE event — duplicates the
  same poll, and since the write can confirm after the event arrives, the two loops often run sequentially rather than
  coalescing.
- **Alternative (rejected):** invalidate the list and show whatever the immediate refetch returns — races the
  projection and can leave the list stale until the next manual refresh.
- **Alternative (rejected):** delay events on the gateway so the projection catches up before forwarding — hard-codes
  projection latency as a magic constant, penalizes fast projections, and undermines the "live" nature of the stream.

### 6. CORS via a WebFlux configurer

A `WebFluxConfigurer` (or `CorsWebFilter`) with an allowed-origins allow-list sourced from configuration, defaulting to
the Vite dev origin. This is additive and applies to both REST and SSE.

## Risks / Trade-offs

- **Frontend toolchain is a second dependency universe** → `package-lock.json` is committed as the npm source of
  truth; Node version pinned in the catalog; the node-gradle plugin keeps the Node binary pinned. npm dependency bumps
  are not covered by the existing dependency-update machinery.
- **Gateway gains Kafka responsibilities** → the gateway already participates in the command bus and Axon; a consumer
  is consistent with its role. The consumer group is distinct so it cannot interfere with the projection.
- **SSE scalability** → a broadcast model (all connected clients receive all events) is fine for a demo/ref app;
  per-client filtering is out of scope.
- **CI cost** → frontend build/lint/test add time to `check`; keep the frontend test tier (Vitest unit) fast and
  defer any browser E2E.

## Migration Plan

Additive only: new module, new gateway endpoint, new gateway config. No existing behavior changes. Deployable
independently — the UI can be served without the gateway changes and vice versa. Rollback is removing the new module
and endpoint.

## Open Questions

- Exact UI origin default for CORS (Vite dev port) and whether prod serves the UI from a fixed origin. Deferrable to
  implementation without changing specs.
- Frontend test depth: unit-only (Vitest) vs adding a browser test (Playwright) tier. Deferrable.