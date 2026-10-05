# Proposal

## Why

Live updates in the web UI are reconciled by a poll loop per showcase, and the policy lives in the page: `ShowcasesPage`
calls `waitForEvent` for every post-connect SSE event, and `reconcileShowcase` runs one latest-wins loop per showcase,
each refetching the whole list up to five times. A burst spanning several showcases fans the work out across concurrent
loops, and the page — not the entity that owns the list — owns reconciliation. Why now: the 2026-10-05 retrospective's
recommended direction is to ship a parked product idea, and this reconciliation is the web UI's flagship live-timeline
behavior.

## What Changes

- Replace `showcase-web-ui/src/entities/showcase/query-hooks.ts` — the per-event `waitForEvent`/`waitForReadModel`/
  `reconcileShowcase` helpers — with a debounced, coalesced reconciliation controller under
  `showcase-web-ui/src/entities/showcase/lib/reconciliation.ts`: a burst of events triggers one list refetch, repeated
  only until every pending event's effect is visible.
- Add `showcase-web-ui/src/entities/showcase/useShowcaseReconciliation.ts` — the hook the page consumes, so the showcase
  entity owns reconciliation rather than the page.
- Update `showcase-web-ui/src/pages/showcases/ShowcasesPage.tsx` to drop the per-event call and use the hook, and update
  its JSDoc.
- Extend `showcase-web-ui/src/entities/showcase-event/@x/showcase.ts` with the event feed the hook reads, and update the
  JSDoc in `showcase-web-ui/src/entities/showcase-event/index.ts` that describes the cross-import API.
- Update `showcase-web-ui/src/entities/showcase/index.ts` (public API), the affected tests
  (`showcase-web-ui/src/entities/showcase/**` and `showcase-web-ui/src/pages/showcases/ShowcasesPage.test.tsx`), the
  web-ui spec delta, the `AGENTS.md` reference to `waitForReadModel`, and remove the idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/clients/web-ui`: the "Read-model reconciliation of the showcase list" requirement changes — reconciliation
  becomes a single debounced, coalesced refetch owned by the showcase entity, replacing the per-showcase poll loops,
  while preserving the event-stream-only entry point and the no-stale-state guarantee.

## Impact

- **Code (web UI only):** `showcase-web-ui/src/entities/showcase/query-hooks.ts` (removed),
  `showcase-web-ui/src/entities/showcase/lib/reconciliation.ts` and
  `showcase-web-ui/src/entities/showcase/useShowcaseReconciliation.ts` (new),
  `showcase-web-ui/src/entities/showcase/index.ts`, `showcase-web-ui/src/pages/showcases/ShowcasesPage.tsx`,
  `showcase-web-ui/src/entities/showcase-event/@x/showcase.ts`, and
  `showcase-web-ui/src/entities/showcase-event/index.ts`, plus the affected tests.
- **Specs:** `openspec/specs/showcase/clients/web-ui` (via the change's delta).
- **Docs:** `AGENTS.md` (a `waitForReadModel` reference), `docs/ideas.md` (idea removal).
- **No service, API, dependency, or deployment change.** Verification runs `./gradlew :showcase-web-ui:check`; the
  opt-in web-UI e2e suite (`./gradlew :showcase-web-ui:e2eTest`, requires Docker) exercises the live reconciliation end
  to end.
