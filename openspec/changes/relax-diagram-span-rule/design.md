# Design: Show the showcase lifecycle's removal as a span

## Context

The README's "Cool Story" diagram depicts the showcase lifecycle. Its Mermaid form labelled the removal transition
`SCHEDULED -.->|"REMOVED (any time)"| REMOVED`, mis-scoping the annotation to `SCHEDULED` even though
`ShowcaseAggregate.handle(RemoveShowcaseCommand…)` accepts removal from any state. The plain-text fallback already draws
the correct shape — a bracket spanning `SCHEDULED → STARTED → FINISHED` labelled `REMOVED (any time)`. The proposal
(`../proposal.md`) fixes the Mermaid to match.

## Goals / Non-Goals

- **Goal**: the Mermaid shows removal as a span over the whole lifecycle, not a labelled edge from one state.
- **Goal**: the `diagrammer` rule and the `AGENTS.md` note agree with the diagram they ship.
- **Non-Goal**: changing the domain or the aggregate; the lifecycle transitions are unchanged.
- **Non-Goal**: introducing a `STARTED → REMOVED` edge (a started showcase is finished before removal).

## Decisions

- **Represent the span as a single, non-nested labelled subgraph.** Mermaid has no dedicated bracket-span syntax; for a
  single range, one non-nested labelled subgraph is the analogue — the border is the bracket and the title its label,
  and one dashed edge leaves the border for `REMOVED`. Rejected: an inline edge label (the user's original complaint —
  it re-scopes "any time" to one edge); a nested-subgraph span (collides, per the `AGENTS.md` note — and unnecessary
  here, since there is one range).
- **Keep `direction LR` on the subgraph.** Without it Mermaid stacks the three states vertically (verified against
  `@mermaid-js/mermaid-cli@11.17.0`: the three nodes sit at y=65/203/341 without the directive and at y=62.5 across with
  it). The fix also qualifies the `AGENTS.md` claim that a subgraph's `direction` is ignored when it has an outside
  edge: that holds for a **member node's** edge to outside (verified: under a `TD` parent, `B -.-> D` stacks the
  subgraph's nodes), but a subgraph-level edge (`lifecycle -.-> REMOVED`) honours it.
- **Relax the `diagrammer` rule rather than keep it.** The recorded prohibition targets **overlapping or nested ranges
  over the same run** faked by a nested subgraph; a single range carries no collision. Keeping the rule would leave the
  README contradicting its own tooling.

## Risks / Trade-offs

- **A future diagram could over-use subgraph spans.** Mitigation: the rule allows a subgraph only for a single range
  over a run and still forbids nested subgraphs for overlapping ranges.
- **Mermaid could change subgraph-direction behaviour across versions.** Mitigation: rendering with `mermaid-cli` is
  already a required step in the diagrammer's Mermaid rule, so a regression is caught at draw time.
