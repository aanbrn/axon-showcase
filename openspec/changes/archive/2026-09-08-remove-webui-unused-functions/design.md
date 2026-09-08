## Context

`entities/showcase/query-hooks.ts` exports five reconciliation helpers built on a shared `reconcileShowcase` /
`waitForReadModel` core. Production (`ShowcasesPage`) only calls `waitForEvent`. The other three exports are dead:

- `waitForShowcasePresence` — no production callers; only referenced by `useCreateShowcase.test.tsx`, which mocks it and
  asserts it is NOT called.
- `waitForShowcaseStatus` — no production callers; `useShowcaseActions.test.tsx` mocks it (asserting NOT called), and
  `query-hooks.test.ts`'s dedup test calls it directly.
- `waitForShowcaseRemoval` — zero references anywhere (production or tests).

## Goals / Non-Goals

**Goals:**

- Remove the three dead exports and the stale test mocks/assertions that reference them.
- Keep the shared reconciliation core (`reconcileShowcase`, `waitForReadModel`, `predicateForEvent`) and `waitForEvent`
  untouched — they back the live SSE-driven flow.
- Preserve dedup test coverage using only the supported `waitForEvent` entry point.

**Non-Goals:**

- No new lint/tsc rules (unused locals/parameters are already gated by `noUnusedLocals`/`noUnusedParameters`).
- No behavioral change to reconciliation.

## Decisions

### D1: Remove the three dead helpers, keep the shared core

Delete `waitForShowcasePresence`, `waitForShowcaseStatus`, and `waitForShowcaseRemoval` and their Javadoc from
`query-hooks.ts`. `waitForReadModel` stays — it is the shared poll loop used by `reconcileShowcase`. `waitForEvent`
stays — it is the only production entry point.

### D2: Clean the stale test mocks

- `useCreateShowcase.test.tsx` and `useShowcaseActions.test.tsx` mock the removed helpers and assert they are NOT
  called. Drop the `vi.mock('@/entities/showcase/query-hooks', ...)` blocks (or the specific mock keys) and the
  `expect(...).not.toHaveBeenCalled()` assertions — they documented that the old reconciliation path was superseded, and
  are now dead assertions on removed symbols.
- `query-hooks.test.ts`'s first dedup test pairs `waitForEvent` with `waitForShowcaseStatus` to exercise coalescing from
  two entry points for the same outcome. Rework it to use `waitForEvent` for both legs with the **same** event
  (preserving the pure-coalescing intent — `attempts === 1` for an identical expected state); the "supersede with a
  newer event" case is already covered by the second dedup test.

## Risks / Trade-offs

- **Dedup coverage** → the reworked dedup test uses `waitForEvent` for both legs, so the coalescing behavior is still
  verified; the "two different entry points" variant is intentionally dropped since one entry point is now the rule.
- **Mock removal** → the `not.toHaveBeenCalled()` assertions were guarding against regression to the old helpers; since
  the helpers no longer exist, the assertions are unrepresentable and their intent is captured by the spec's
  "event-stream reconciliation is the only supported entry point" scenario.
