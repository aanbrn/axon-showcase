# Proposal: Enforce the web UI's Feature-Sliced import boundaries

## Why

The web UI follows Feature-Sliced Design, and ADR-0013 records the rule strictly: a layer imports only from layers below
it, and never from a sibling slice in its own layer. Nothing enforced it — ADR-0013's own Consequences say so — and two
imports violate it today: `pages/showcases` (and its test) read the store from `app` upward, and `entities/showcase`
imports its sibling `entities/showcase-event`. The frontend's other gates (lint, format, type-check, test) landed this
month; this change gives the layering a real check and fixes the two violations the ADR already calls defects: the
app-level UI state moves to the entity slices that own it (the store _setup_ stays in `app`), the sibling edge is
expressed through FSD's `@x` cross-import API, every slice gets a public API, and `eslint-plugin-boundaries` enforces
the direction in the existing lint gate.

## What Changes

- **Fix the two ADR-0013 violations**:
  - _Upward_: split the UI state out of `app/store.ts` into its owning slices — `entities/showcase-event` (the received
    events) and `entities/showcase` (the selection) — each exposing a hook; `app` composes the store from their reducers
    (a downward `app` → `entities` import), and `pages/showcases` consumes the entity hooks instead of the store.
  - _Sibling_: express the one `entities/showcase` → `entities/showcase-event` edge (`ShowcaseEvent`) through an
    explicitly declared `@x` cross-import API.
- **Public API per slice** — an `index.ts` for each imported slice (`entities/showcase`, `entities/showcase-event`,
  `features/create-showcase`, `features/showcase-actions`, `widgets/showcase-detail`, `widgets/showcase-list`,
  `pages/showcases`) and a `shared/index.ts` for the flat shared layer, re-exporting each slice's surface. `app` gets no
  barrel: the rule forbids importing it from below, so nothing needs one.
- **Rewrite the source imports** to the slices' public APIs, keeping intra-slice imports relative; test files are
  exempt.
- **Configure `eslint.config.js`** with `eslint-plugin-boundaries` (v7 `boundaries/dependencies`) plus
  `eslint-import-resolver-typescript`: the ADR's one-way chain, cross-slice imports through public APIs, and the `@x`
  edge as the only declared sibling exception.
- **A positive control** — a test that lints violating snippets against the real config and asserts errors (and
  conforming ones clean), so the rule is shown to fail where it should.
- **Docs and spec** — refresh ADR-0013 (its Decision gains the `@x` clause its absolute "never a sibling slice" omitted,
  its `entities` and `app` population lines reflect the sibling edge and the store's new home, and its Consequences
  record the gate), correct `AGENTS.md`'s frontend convention (the `@x` exception and the split state home), reword the
  parked `docs/ideas.md` entry to the naming half that remains, and add the requirement to
  `showcase/quality/code-quality`.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/code-quality` — a new requirement that the web module's import boundaries are enforced by the build.

## Impact

- **Web UI**: new barrels, rewritten imports, the state relocation (same state, same hooks — a new home, not new
  behaviour), the `@x` file, the ESLint config, one new test, and two devDependencies with the lockfile.
- **Build / CI**: no new gate — the rules run in the existing `npmLint` step of `check`; the bundle is verified by the
  module's `build`.
- **Docs**: ADR-0013 (Decision, population, Consequences), `AGENTS.md`, and `docs/ideas.md`.
