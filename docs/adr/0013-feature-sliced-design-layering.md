# ADR-0013: Organize the web UI by Feature-Sliced Design with a one-way import rule

Date: 2026-09-21

Status: Accepted

## Context

`showcase-web-ui` is a standalone React + Vite application (ADR-0008) that grew as a browsable showcase: a list, a
detail view with lifecycle actions, a creation form, and a live event timeline. A flat component directory would let any
component import any other, so as the UI accreted features, a change to a shared piece would ripple unpredictably and
the module graph would carry cycles that no gate could see. Nothing in the build enforces a component-import direction —
ESLint gates correctness, `tsc` gates types, and Prettier gates formatting; none of them constrains which layer may
import which.

The first architecture audit surfaced the layering as a deliberate structural choice whose rationale was recorded
nowhere: `AGENTS.md` states the convention (organize per Feature-Sliced Design, importing only downward) but not why the
direction is enforced, nor what it buys.

## Decision

Organize the web UI by **Feature-Sliced Design** — six layers with a strict one-way import direction — and treat an
upward or same-level-cross-slice import as a defect:

```
app  →  pages  →  widgets  →  features  →  entities  →  shared
```

A layer may import only from layers **below** it, never from a layer above it and never from a sibling slice in its own
layer — except through an explicitly declared `@x` cross-import API, the canonical Feature-Sliced way to make a
same-layer edge. Every cross-slice import goes through the target slice's public API (`index.ts`) rather than reaching
into its internals, and a source file that matches no layer or slice is itself a defect. All imports resolve through the
`@/` alias to `src/` (`tsconfig.json`'s `"@/*": ["src/*"]`, wired in `vite.config.ts`), so the layer is visible at the
import site rather than buried in a relative path.

The layers, as populated:

- `app` — the composition root (`App.tsx`, the Redux `store` composed from the entity-owned state slices, global
  styles); imports from every layer below.
- `pages` — route-level composition (`showcases`); assembles widgets and features.
- `widgets` — self-contained UI blocks (`showcase-list`, `showcase-detail`); compose features and entities.
- `features` — user interactions (`create-showcase`, `showcase-actions`); use entities and shared.
- `entities` — domain display units (`showcase`, `showcase-event`) owning their state slices; use `shared`, and one
  declared `@x` cross-import for the events edge (`showcase` reading the `showcase-event` type).
- `shared` — the layer with no domain knowledge (`api`, `format`, `retry`); imports nothing from the layers above.

Alternatives considered and rejected: a single flat `components/` directory (no enforceable direction, cycles likely as
the UI grows); a type-based split (`components`/`hooks`/`utils`) with no domain grouping (organizes by implementation
shape, not by feature, so a feature's pieces scatter and a change touches many directories); and trusting review alone
to hold a convention that no gate checks (the arrangement would drift silently — the same failure mode this ADR is
repairing by recording the rule at all).

## Consequences

- A change to `shared` can be reasoned about as affecting everything above it, and a change to a feature stays in that
  feature; the dependency direction makes the blast radius of an edit readable from the layer it sits in.
- The rule is **enforced by the build**: the `check` lint gate (`eslint-plugin-boundaries`) rejects an upward, an
  undeclared-sibling, or a public-API-bypassing import, and a source file that matches no layer or slice — landing the
  parked `docs/ideas.md` entry's mechanism (`enforce-web-ui-import-boundaries`, 2026-09-26, which also added the `@x`
  clause and the public-API and file-classification defect classes to the Decision; the entry's naming half remains
  parked). An upward import is now a build failure, not a review finding.
- Layers with a single slice today (`pages/showcases`) leave room for more pages without restructuring, and the layering
  is what makes the eventual route/feature split mechanical rather than a refactor.
- The `@/` alias is load-bearing for the convention: a relative `../../shared/...` import would cross the same boundary
  and be equally wrong, so the alias is the affordance that keeps the visible import path aligned with the layer
  structure.
