# Proposal: Show the showcase lifecycle's removal as a span

## Why

The README's showcase lifecycle diagram annotates the `SCHEDULED → REMOVED` edge with "REMOVED (any time)", which
mis-scopes the annotation to a single state: in `ShowcaseAggregate`, a remove command is accepted from **any** state
(`SCHEDULED → REMOVED`, `FINISHED → REMOVED`, and from `STARTED` a `FINISHED` event is emitted first). "Any time"
belongs to the whole lifecycle — a span, which the diagram's plain-text fallback already draws as one bracket. Drawing
it as a span in Mermaid (a labelled subgraph) is exactly what the `diagrammer` subagent's own rule forbids, and an
`AGENTS.md` note adds that a subgraph's `direction` is ignored when it has an outside edge. The fix contradicts the
first claim outright and the second in part: a subgraph-level outside edge honours the `direction`, but a member node's
edge to outside the subgraph ignores it.

## What Changes

- `README.md` — the "Cool Story" lifecycle diagram groups the three lifecycle states under a single labelled subgraph
  (`REMOVED (any time)`) with one dashed edge to the `REMOVED` state, replacing the `SCHEDULED → REMOVED` edge label and
  the redundant `FINISHED → REMOVED` edge. The plain-text fallback already draws the span; its right bracket bar is
  realigned from `FINISHED`'s left edge to its center.
- `.opencode/agent/diagrammer.md` — relax the Mermaid rule: a **single** range over a run is drawn as one non-nested
  labelled subgraph (its border is the bracket, its title the label), and its `direction` is honoured when the outside
  edge leaves the subgraph itself; **overlapping or nested ranges** over the same run are stated inline as edge labels.
- `AGENTS.md` — the "a diagram's geometry and content encode semantics" bullet gains the same carve-out and corrects the
  falsified "Mermaid ignores a subgraph's `direction` when it has outside edges" note; the "Diagrammer subagent"
  bullet's Mermaid-pitfall parenthetical is updated to match (a span has no native `flowchart` construct).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. The `agent-skills` spec describes the `diagrammer` subagent generically ("chooses the diagram
type and constructs"); it does not state this rendering rule, so no requirement's outcome changes.

## Impact

- **Docs and agent tooling only** — `README.md`, `.opencode/agent/diagrammer.md`, `AGENTS.md`. No build, test, runtime,
  or deployment effect; no code, API, dependency, chart, or workflow change.
- **`AGENTS.md` grows by a net 10 lines** (27 insertions / 17 deletions): the relaxed rule, the qualified `direction`
  correction, and the lesson-capture addition for the recurring-class root cause this unit took four review rounds to
  find. The capture convention requires such growth to be a stated, justified decision — it is, and nothing is retired.
