# Design

## Context

See proposal.md — Why. The `diagrammer` subagent is the repository's single diagram delegate, currently ASCII-only. Two
facts changed: the README's lifecycle, Event Flow, and OpenSpec-loop diagrams are now Mermaid, and ASCII diagrams remain
(the README project tree, and each diagram's plain-text fallback — the last change had the `diagrammer` draw one of
those fallbacks). Mermaid authoring is not geometry: the renderer lays the diagram out, so the ASCII rationale (the
flash model cannot align characters) does not transfer. What Mermaid authoring needs is the right diagram type and
constructs, and avoidance of the host renderer's pitfalls — several of which this repository has already hit and
recorded in its own guidance.

## Goals / Non-Goals

**Goals:**

- One diagram delegate covers both media the repository uses, described consistently in `.opencode/agent/diagrammer.md`,
  `.opencode/commands/diagram.md`, `AGENTS.md`, `README.md`, and the `agent-skills` spec.
- Its Mermaid path chooses the diagram type and constructs and applies the pitfalls recorded in the `AGENTS.md`
  diagram-and-content bullet ("**A diagram's geometry and content encode semantics…**") in one pass.

**Non-Goals:**

- Changing the subagent's model pin (`opencode-go/deepseek-v4-pro`) or its trigger mechanism (a subagent, invoked
  through `/diagram`).
- Letting the subagent publish or apply its output; it returns the diagram source and the calling agent applies it.
- Verifying the live GitHub render. This change does not claim the pro model is required to author Mermaid — the main
  agent can author it — only that a dedicated pass over both media is cheaper than the flash agent iterating; the
  host-toolbar clause is verified by the caller's Playwright + `vision` step, not here.

## Decisions

**Widen the one `diagrammer` rather than add a second subagent or clarify the docs only.** One diagram tool with one
trigger is simpler, and the `agent-skills` requirement already names "the visual and diagram agents", not one per
medium. Alternatives considered: a separate `mermaid-diagrammer` subagent — rejected (two near-duplicate definitions and
two triggers for one job); leave the tool ASCII-only and reword the docs to say the main agent authors Mermaid itself —
rejected, because the scenario's `WHEN` ("a README flow diagram") then points at a medium the tool cannot serve. The
subagent is named `diagrammer`, not `ascii-diagrammer`, so the name does not change.

**Express the Mermaid behavior as a new scenario sibling of the ASCII one, inside the same `MODIFIED` requirement.** The
ASCII scenario's name cannot be renamed — `openspec validate` matches scenarios by name and rejects a `MODIFIED` block
that omits one — so the ASCII scenario is kept (its example narrowed to the ASCII diagrams that remain) and a new
`Mermaid diagrams are drawn by the pro-model diagrammer` scenario is added beside it. Alternatives considered: rename
the ASCII scenario to a medium-agnostic one — rejected, it fails `openspec validate`; a brand-new requirement —
rejected, it would split one subagent's behavior across two requirements.

**Describe the Mermaid work as construct choice and toolbar clearance, not "geometry".** The ASCII rationale is geometry
(the flash model cannot align characters); the Mermaid rationale is picking the right constructs and avoiding the host
renderer's pitfalls, since the renderer lays the diagram out. The scenario's `THEN` says so, so the spec does not imply
the pro model is needed for Mermaid geometry.

## Risks / Trade-offs

- **The delegate may still miss a Mermaid pitfall** (e.g. that `stateDiagram-v2` has no `diagramPadding`) → the subagent
  definition names the pitfall class (content colliding with the host renderer's controls) and points at the `AGENTS.md`
  "geometry and content encode semantics" bullet, where the specific pitfalls are recorded and kept current; and the
  caller's live-render step (a Non-Goal here) is where the toolbar clause is actually verified — a local `mermaid-cli`
  render only proves the source parses and lays out.
- **Scope creep into "the diagrammer fixes everything about diagrams"** → Non-Goals pin the boundary (no publish/apply,
  no live-render verification).

## Migration Plan

Documentation and agent-tooling only; nothing to deploy or roll back.
