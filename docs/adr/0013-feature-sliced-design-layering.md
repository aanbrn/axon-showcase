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
layer. All imports resolve through the `@/` alias to `src/` (`tsconfig.json`'s `"@/*": ["src/*"]`, wired in
`vite.config.ts`), so the layer is visible at the import site rather than buried in a relative path.

The layers, as populated:

- `app` — the composition root (`App.tsx`, the Redux `store`, global styles); imports from every layer below.
- `pages` — route-level composition (`showcases`); assembles widgets and features.
- `widgets` — self-contained UI blocks (`showcase-list`, `showcase-detail`); compose features and entities.
- `features` — user interactions (`create-showcase`, `showcase-actions`); use entities and shared.
- `entities` — domain display units (`showcase`, `showcase-event`); use shared only.
- `shared` — the layer with no domain knowledge (`api`, `format`, `retry`); imports nothing from the layers above.

Alternatives considered and rejected: a single flat `components/` directory (no enforceable direction, cycles likely as
the UI grows); a type-based split (`components`/`hooks`/`utils`) with no domain grouping (organizes by implementation
shape, not by feature, so a feature's pieces scatter and a change touches many directories); and trusting review alone
to hold a convention that no gate checks (the arrangement would drift silently — the same failure mode this ADR is
repairing by recording the rule at all).

## Consequences

- A change to `shared` can be reasoned about as affecting everything above it, and a change to a feature stays in that
  feature; the dependency direction makes the blast radius of an edit readable from the layer it sits in.
- The rule is **convention, not a gate**: no lint rule enforces the import direction today. It holds because it is
  followed, and the parked `docs/ideas.md` entry to enforce FSD import boundaries with `eslint-plugin-boundaries` (plus
  naming conventions) is the planned mechanism to give it a real check. Until then, an upward import is a review
  finding, not a build failure.
- Layers with a single slice today (`pages/showcases`) leave room for more pages without restructuring, and the layering
  is what makes the eventual route/feature split mechanical rather than a refactor.
- The `@/` alias is load-bearing for the convention: a relative `../../shared/...` import would cross the same boundary
  and be equally wrong, so the alias is the affordance that keeps the visible import path aligned with the layer
  structure.
