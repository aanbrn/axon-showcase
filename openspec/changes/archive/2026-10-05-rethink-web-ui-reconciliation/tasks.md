# Tasks

## 1. Reconciliation controller in the showcase entity

- [x] 1.1 Add `showcase-web-ui/src/entities/showcase/lib/reconciliation.ts`: a controller that records pending expected
      states (latest-wins per showcase), schedules one debounced flush, refetches the `SHOWCASES_QUERY_KEY` query, and
      repeats on the settle interval until all pending states are visible or the budget is exhausted (design D1, D3,
      D4). Make the clock/sleep and the fetch injectable so it is unit-testable. Verify with new
      `showcase-web-ui/src/entities/showcase/lib/reconciliation.test.ts` covering: one flush for a burst, latest-wins
      for repeated events on one showcase, and budget exhaustion resolving without throwing.
- [x] 1.2 Add `showcase-web-ui/src/entities/showcase/useShowcaseReconciliation.ts`: a hook that consumes newly received
      events from the `showcase-event` slice and drives the controller, suppressing events replayed when the stream
      opens (their timestamp precedes it) and processing each appended event once (design D2, D5). Verify with a hook
      test covering debounced coalescing of a multi-event burst and replay suppression.
- [x] 1.3 Remove the per-event helpers (`waitForEvent`, `waitForReadModel`, `reconcileShowcase`, and `query-hooks.ts`)
      and their test, and update `showcase-web-ui/src/entities/showcase/index.ts` to export the new hook instead. Verify
      `./gradlew :showcase-web-ui:check` type-checks and no import of the removed symbols remains
      (`grep -rn "waitForEvent\|waitForReadModel" showcase-web-ui/src` returns nothing).
- [x] 1.4 Extend the sibling cross-import API in `showcase-web-ui/src/entities/showcase-event/@x/showcase.ts` to export
      `useLiveEvents` and update its JSDoc, and update the JSDoc in
      `showcase-web-ui/src/entities/showcase-event/index.ts` that describes the API; verify the FSD boundary lint passes
      (`./gradlew :showcase-web-ui:check`).

## 2. Page integration

- [x] 2.1 Update `showcase-web-ui/src/pages/showcases/ShowcasesPage.tsx` to drop the per-event `waitForEvent` call and
      use `useShowcaseReconciliation`, keeping the single `connectEventStream` subscription for the timeline, and update
      its JSDoc (it no longer reconciles). Verify `showcase-web-ui/src/pages/showcases/ShowcasesPage.test.tsx` passes
      (the page no longer imports a reconciliation helper).
- [x] 2.2 Add a page test asserting a replayed event on connect does not trigger a list refetch, and that a live event
      does, so the event-driven path is exercised end to end at the page level.

## 3. Documentation

- [x] 3.1 Update the JSDoc/JSDoc-example reference in `AGENTS.md` that names `waitForReadModel` so it no longer cites a
      removed symbol, and run `./gradlew spotlessApply`.
- [x] 3.2 Remove the "Rethink reconciliation in the web UI" idea from `docs/ideas.md` (the change implements it), and
      confirm no other doc or idea entry still describes the old per-showcase polling.

## 4. Verification

- [x] 4.1 Run the frontend gate `./gradlew :showcase-web-ui:check` and confirm lint, format-check, type-check, and
      Vitest pass.
- [x] 4.2 Run `./gradlew :showcase-web-ui:e2eTest` (requires Docker; builds the images and boots the pipeline via
      compose) and confirm create→appears, start→STARTED, the saga auto-start, and live timeline events still work
      against the redesigned reconciliation.
- [x] 4.3 Run the `lesson-capture` subagent over the implementation diff and review findings, apply its durable
      proposals, record the applied net `AGENTS.md` delta on this task, and re-run `./gradlew spotlessApply` +
      `spotlessCheck` after the final edit. Applied: merged the
      refactor-can-drop-a-test's-coverage/vacuous-negative-test lesson into the **Test display names** bullet, and the
      single-live-event-subscription invariant into the **Frontend** bullet; applied net `AGENTS.md` delta **+7 lines**;
      `spotlessApply`/`spotlessCheck`/`verifyCapturedMarkers` green.
