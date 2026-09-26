# Design

## Context

See `proposal.md` — Why. Current state, with the module graph derived rather than recalled:

- **ADR-0013 is the recorded decision this change implements, and it is strict**: a layer imports only from layers below
  it — `app` > `pages` > `widgets` > `features` > `entities` > `shared` — "never from a layer above it and never from a
  sibling slice in its own layer". Its Consequences name the parked `docs/ideas.md` entry as "the planned mechanism to
  give it a real check", so this change lands the ADR's follow-on. It **amends** the Decision in the two ways the ADR's
  own text does not carry: it adds the canonical FSD `@x` clause (the ADR's "never from a sibling" is absolute), and it
  states the enforcement's two defect classes beyond the ADR's "upward or same-level-cross-slice" pair — an import that
  bypasses a slice's public API, and a file outside the layer graph (D8).
- **Two imports violate that rule today** (pre-existing, which the ADR calls defects):
  - _Upward_: `pages/showcases/ShowcasesPage.tsx` and `ShowcasesPage.test.tsx` import `@/app/store` (`pages` → `app`).
    `app/store.ts` holds the UI slice (`selectedId`, `liveEvents`), the store, and the typed hooks.
  - _Sibling_: `entities/showcase` imports `entities/showcase-event` — its `types` module only (`ShowcaseEvent`), at
    four sites (two source, two test).
- **Graph figures** (derived from `showcase-web-ui/src`): 48 `@/` imports (32 source / 16 test), 30 deep (20 source / 10
  test); exactly those two upward sites; exactly that one same-layer edge. Three root files sit outside a slice —
  `src/main.tsx`, `src/test-setup.ts`, `src/vite-env.d.ts` — and `eslint src` lints all of them.
- **The mechanism**: `eslint-plugin-boundaries` v7 (latest 7.2.0) enforces dependencies through the canonical
  `boundaries/dependencies` rule over `boundaries/elements`; its TypeScript guide requires
  `eslint-import-resolver-typescript` so the `@/` path mapping resolves. Entry-point (public-API) enforcement is the
  dependency rule's `fileInternalPath` selector (the dedicated `boundaries/entry-point` rule is deprecated in v7).
- **The gate exists**: `npmLint` (`eslint src --max-warnings 0`) runs in the web module's `check`.

## Goals / Non-Goals

**Goals:**

- Make the tree satisfy ADR-0013's rule, then enforce it in the lint gate.
- Give every imported slice a public API and route cross-slice imports through it.
- Prove the rule rejects violations (a positive control), not only that the tree passes.

**Non-Goals:**

- Relaxing the one-way rule: no upward import, and no sibling import beyond a declared `@x`. The ADR's Decision is
  amended only to add the canonical `@x` clause and the enforcement's added defect classes (D8).
- Any behaviour change: the state relocation keeps the same state and hooks, only their module home moves.
- Merging the two entity slices (the ADR names both), or introducing `@x` for anything but the one edge.

## Decisions

- **D1 — Public API per imported slice.** Each imported slice gets an `index.ts` (with the
  `// SPDX-License-Identifier: MIT` header) re-exporting its surface; intra-slice imports stay relative. `shared` is
  flat and gets `shared/index.ts`. `app` gets **no** barrel: the rule forbids importing it from below, so nothing needs
  one — which is also why no `app` ↔ `pages` cycle can arise. The public API is not a blanket `export *` where a slice
  exposes only part of its internals.
- **D2 — The one-way chain.** The elements implement ADR-0013 exactly: `shared` < `entities` < `features` < `widgets` <
  `pages` < `app`, a layer importing only lower layers; a cross-slice import targets the target's public API; a sibling
  import is rejected unless it goes through a declared `@x` (D4).
- **D3 — The app-level UI state moves to the slices that own it.** `entities/showcase-event` owns the received-events
  state (its slice + a read hook and a dispatch-bound append hook); `entities/showcase` owns the selection state (its
  slice + hooks). `app/store.ts` composes the store from their reducers (`app` → `entities`, downward);
  `pages/showcases` consumes the entity hooks instead of the store, so nothing below `app` imports it and the generic
  `useAppDispatch` / `useAppSelector` hooks lose their only consumer — they are removed with the move, together with the
  `RootState` / `AppDispatch` exports they alone fed and the store JSDoc that names them (the entity slices bind their
  own hooks structurally to their state shape, e.g. `{ showcaseEvents: EventsState }` rather than importing the app's
  `RootState`; the exact generic form is settled during apply). `app/store.test.ts`'s state assertions move to the
  entity tests; the app test keeps composition.
- **D4 — The one sibling edge goes through `@x`.** FSD's cross-import API is how a same-layer edge is declared: the
  consuming slice imports `@/entities/showcase-event/@x/showcase` (the target's public API for that consumer), and the
  boundaries config admits that `@x` path as an entry point while rejecting every other sibling import. The direction
  and file name follow FSD's `@x` convention, verified during apply.
- **D5 — Configuration.** `eslint-plugin-boundaries` + `eslint-import-resolver-typescript` in the flat
  `eslint.config.js`, with `boundaries/elements` classifying the layers/slices and `boundaries/dependencies` policies
  for the direction, the public-API rule, and the `@x` entry point. The `@x` allowance is general — every slice layer
  with siblings (entity, feature, widget, page) may import a sibling's declared `@x` API — though only the entity edge
  exists today, so the other layers' allowances are declarative until such an edge appears. **The `default` does not
  catch an unclassified file**: the plugin skips a source unknown on both axes (and an unknown target under the default
  `checkUnknownLocals: false`), so the three out-of-slice files (`src/main.tsx`, `src/test-setup.ts`,
  `src/vite-env.d.ts`) and the tests sit outside the rule by construction. That must not become a silent hole: exactly
  those paths and `**/*.test.ts`/`**/*.test.tsx` are named in `boundaries/ignore` (derived from what `eslint src` lints,
  so the ignore stays precise), and `boundaries/no-unknown-files` then reports any _other_ source file matching no layer
  or slice. The alias resolution and the exact `fileInternalPath`/`@x`/ignore shapes are verified against the plugin
  during apply (its docs are a contract to probe, not to assume).
- **D6 — Positive control.** A test lints snippets against the real config: a deep cross-slice import, an upward import,
  an undeclared sibling import, and an unclassified source path must error; a public-API downward import and a declared
  `@x` import must pass (a rule that only ever passes is not evidence). The test loads the config at runtime
  (`new ESLint({ overrideConfigFile: 'eslint.config.js' })`), never by static import — the module's tsconfig covers
  `src` with no `allowJs`, so importing `../eslint.config.js` would fail `npmTypeCheck`.
- **D7 — Test files are outside the rule.** Tests are not part of the layer graph, and `ShowcasesPage.test.tsx` spies on
  slice internals (`@/features/create-showcase/api`, `@/entities/showcase/api`) that the public APIs deliberately do not
  expose; a test's `vi.mock(...)` paths are string literals, not imports. They (and the composition entry) are excluded
  via `boundaries/ignore` (D5), so a test's imports are unchecked while the shipped layers are fully enforced — an
  explicit trade-off, and the ADR's rule (about layers) is unaffected.
- **D8 — Refresh ADR-0013**, derived line by line rather than from the summary. The **Decision** gains the `@x` clause
  (its "never from a sibling slice in its own layer" is absolute, while the canonical FSD rule permits a declared `@x`
  cross-import) **and the two defect classes the enforcement adds beyond its "upward or same-level-cross-slice" pair**:
  an import that bypasses a slice's public API, and a file outside the layer graph. The **population** lines change:
  `entities`' "use shared only" gains the declared `@x` edge, and `app`'s "the Redux `store`" becomes the store's
  _composition_ now that the slices live in the entities (D3). The **Consequences**' "convention, not a gate" becomes
  the enforced gate, its parked-entry sentence landing.

## Risks / Trade-offs

- **Alias resolution**: if the resolver does not resolve `@/` for the plugin, the _target_ of each aliased import
  classifies as unknown and — with the default `checkUnknownLocals: false` — the dependency is skipped, so the rule
  silently passes. Mitigation: the positive control (D6) exercises real violations, so a misconfigured resolver fails
  the test rather than hiding.
- **The state move**: relocating the slices changes where state is defined, so the entity hooks must reproduce the
  current behaviour exactly (same state shape, same transitions). The existing store tests move with the state and
  remain the guard; the page's tests verify the wiring end to end.
- **Selector typing**: binding the entity hooks structurally to their own state shape avoids an upward `RootState`
  import, but the exact generic form must compile — settled and type-checked during apply.
- **Scope**: this change both fixes the violations and adds the gate (a rule cannot land while the tree violates it), so
  it is one unit — mechanical imports plus one behaviour-preserving state move.

## Migration Plan

- Apply: relocate the state, add the `@x` file, add the barrels, rewrite the source imports, configure the rules, add
  the positive control, refresh the docs and ADR.
- Rollback: revert the branch (no data or external API surface).
