# Proposal: Remove unused functions from the web UI

## Why

The web UI has accumulated exported helper functions that production code no longer references, notably the
`waitForShowcase*` reconciliation helpers in `entities/showcase/query-hooks.ts`. Only `waitForEvent` is used by
production (`ShowcasesPage`); `waitForShowcasePresence`, `waitForShowcaseStatus`, and `waitForShowcaseRemoval` are dead
code — the first two are referenced only by tests that mock them and assert they are NOT called, and the last has zero
usages anywhere. Keeping them grows the surface the build agents and maintainers must reason about, and invites reusing
a stale reconciliation path instead of the current `waitForEvent` flow.

The "catch them going forward" half of the idea is already satisfied: `noUnusedLocals`/`noUnusedParameters` are enabled
in `showcase-web-ui/tsconfig.json` and `tsc` runs in the `build` script, so unused locals/parameters fail the build.
What remains is removing the existing dead exports.

## What Changes

- Remove `waitForShowcasePresence`, `waitForShowcaseStatus`, and `waitForShowcaseRemoval` from
  `entities/showcase/query-hooks.ts` (and their JSDoc). The reconciliation machinery they share (`reconcileShowcase`,
  `waitForReadModel`, `predicateForEvent`, `waitForEvent`) stays — it backs the live `waitForEvent` flow.
- Update the tests that reference the removed helpers:
  - `features/create-showcase/useCreateShowcase.test.tsx` — drop the `waitForShowcasePresence` mock (it asserted the
    helper is NOT called) and its `expect(...).not.toHaveBeenCalled()` assertions.
  - `features/showcase-actions/useShowcaseActions.test.tsx` — drop the `waitForShowcaseStatus` mock and its not-called
    assertions.
  - `entities/showcase/query-hooks.test.ts` — the dedup test that pairs `waitForEvent` with `waitForShowcaseStatus`
    exercises dedup via a non-event reconcile; rework it to use `waitForEvent` for both legs (or drop the
    `waitForShowcaseStatus` leg) so dedup coverage is preserved with only the supported entry point.
- No new lint rule is added: unused locals/parameters are already gated by `tsc`
  (`noUnusedLocals`/`noUnusedParameters`).

## Capabilities

### New Capabilities

- None (this is dead-code removal, not new behavior).

### Modified Capabilities

- `showcase/clients/web-ui` — a new requirement documents that the UI's reconciliation helpers are exercised only
  through the supported `waitForEvent` flow, so dead helpers are not reintroduced.

## Impact

- **Code**: `entities/showcase/query-hooks.ts` shrinks by three dead exports; three test files lose their stale
  mocks/assertions.
- **Docs**: `docs/ideas.md` — remove the now-implemented "Remove unused functions from the web UI" idea.
- **Behavior**: none — the removed helpers are unreachable from production, and the tests that referenced them only
  mocked/asserted non-invocation. `tsc`/`lint`/`test` all still pass.
