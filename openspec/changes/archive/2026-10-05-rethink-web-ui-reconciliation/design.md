# Design

## Context

See `proposal.md` — Why. Today reconciliation lives in `showcase-web-ui/src/entities/showcase/query-hooks.ts`:
`waitForEvent` maps an event to a per-showcase predicate, `reconcileShowcase` keeps a latest-wins pending map, and
`waitForReadModel` polls the list up to `5 × 500 ms` through `queryClient.query`. `ShowcasesPage` owns the SSE
subscription (`connectEventStream` → `onEventReceived`) and calls `waitForEvent` for every post-connect event.

Two existing facts shape the approach: the `showcase-event` slice already records every received event in Redux
(`useLiveEvents`), so a single, owned event feed already exists; and Feature-Sliced Design forbids a direct
sibling-slice import, so `entities/showcase` may read `showcase-event` only through the declared `@x/showcase`
cross-import API (`showcase-web-ui/src/entities/showcase-event/@x/showcase.ts`).

## Goals / Non-Goals

**Goals:**

- One coalesced reconciliation per burst of events, rather than one poll loop per showcase.
- Reconciliation owned by the showcase entity and exposed as a single event-driven entry point; the page renders rather
  than reconciles.
- Preserve today's observable guarantees: stream-driven only, no brief stale state, replay on connect suppressed.

**Non-Goals:**

- Server-pushed read-model state or a richer SSE payload (the parked idea's other route).
- Any change to the gateway, the query side, the event contract, or the timeline merge behavior.
- Changing the worst-case settle budget (currently ~2.5 s).

## Decisions

**D1 — Debounce, then a bounded settle loop, rather than a bare invalidation.** A burst of events schedules one flush on
a short trailing debounce (~200 ms); the flush refetches the list and re-checks every pending expected state, repeating
on the settle interval (~500 ms) until all are satisfied or the budget (5 attempts) is exhausted. _Alternatives:_ the
current per-showcase poll loop (the thing being replaced); a single debounced invalidation with no settle (rejected —
one refetch can race the projection and then no further refetch fires, so the list stays stale longer than today,
regressing a spec'd guarantee); server push (rejected — Non-Goals).

**D2 — The entity owns reconciliation; the page stops reconciling.** A hook in `entities/showcase` consumes newly
received events from the `showcase-event` slice (`useLiveEvents`, added to the `@x/showcase` cross-import API) and runs
the coalescing controller; `ShowcasesPage` keeps only its stream subscription for the timeline. _Alternatives:_ the page
hands each event to a `reconcile(event)` entry point (rejected — re-couples the page and keeps per-event dispatch
there); the hook opening its own `EventSource` (rejected — a second subscription, and the browser's replay buffer would
double-deliver).

**D3 — Refresh through the shared list query.** The flush refetches the `SHOWCASES_QUERY_KEY` query so the existing
`useShowcases` cache and observers update and the page re-renders — one fetch path, no parallel fetch. _Alternatives:_
an `invalidateQueries`-plus-cache-read variant (equivalent; rejected for having two code paths where the settle loop
needs the resolved value anyway).

**D4 — One pending set, latest-wins per showcase.** A burst spanning several showcases shares a single flush, and
multiple events for one showcase keep only the newest expected state. This preserves the existing per-showcase
coalescing and extends it across showcases.

**D5 — Keep the connect-time filter, scoped to the initial connection.** The hook records the time the stream is first
opened and reconciles only events whose timestamp is later, so history the gateway replays when the UI opens does not
refetch the list (which is already fresh after an initial load). A within-session cursor advances past each appended
event so the same element is processed once; a frame the browser re-delivers on a reconnect is appended anew and may
reconcile again, which is idempotent and costs at most one redundant flush.

## Risks / Trade-offs

- Budget exhaustion still leaves the list stale until the next event → keep the `5 × 500 ms` budget (parity with today)
  and rely on the existing "could not confirm" notices for the write path.
- The debounce delays the first refetch by up to ~200 ms → keep it small; the round-trips it saves on a saga burst are
  worth more than the delay, and the stale window it avoids is longer than the delay itself.
- Consuming `useLiveEvents` accumulates a cursor over a slice that grows for the session → reset the cursor on mount;
  the slice is not persisted and the app is a demo session.
- Moving the connect-time filter could regress replay suppression → a dedicated test asserts that history replayed when
  the stream opens does not trigger a refetch.

## Migration Plan

None — frontend-only, no persisted state and no API change. Rollback is the code revert.

## Open Questions

The exact debounce and settle-interval constants can be tuned during implementation without changing the specs, the
approach, or the task breakdown; only the debounce-and-settle behavior itself is spec'd.
