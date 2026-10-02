# Proposal

## Why

The README's diagrams moved to Mermaid, but the `diagrammer` subagent and its `/diagram` command are ASCII-only, and the
`agent-skills` scenario still reads "ASCII diagrams are drawn by the pro-model diagrammer". A request to draw or fix a
diagram therefore routes to a tool that cannot help with the primary medium, and the cheap flash main agent — which
cannot reliably choose Mermaid constructs or avoid its rendering pitfalls — has no pro-model delegate for it.

## What Changes

- `.opencode/agent/diagrammer.md` — widen the subagent from ASCII to **ASCII and Mermaid**: for Mermaid it chooses the
  diagram type and constructs, keeps the content clear of GitHub's right-edge control toolbar, renders the result once
  with `mermaid-cli` to check it, and returns the diagram **source**; the ASCII geometry rules stay. Its body points at
  the pitfalls recorded in the `AGENTS.md` diagram-and-content bullet.
- `.opencode/commands/diagram.md` — the `/diagram` trigger describes both media and its task step names a Mermaid
  diagram.
- `AGENTS.md` — the "Diagrammer subagent" bullet covers Mermaid as well as ASCII, and its stale ASCII example ("a README
  flow diagram") is corrected, since README flow diagrams are Mermaid now.
- `README.md` — the agent-table row and the `/diagram` slash-command row drop the ASCII-only wording.
- `docs/ideas.md` — remove the implemented `## 2026-10-02` idea section.
- `openspec/specs/showcase/quality/agent-skills/spec.md` (via a delta) — `MODIFIED` the requirement to keep the ASCII
  scenario (its example narrowed to the ASCII diagrams that remain) and add a sibling "Mermaid diagrams are drawn by the
  pro-model diagrammer" scenario; the requirement's description is unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the shared subagents requirement gains a Mermaid-diagram scenario beside the ASCII
  one.

## Impact

Agent tooling and documentation only — no build, test, runtime, or deployment effect. Touches `.opencode/agent/`,
`.opencode/commands/`, `AGENTS.md`, `README.md`, `docs/ideas.md`, and the `agent-skills` delta. The `diagrammer`
subagent's model pin (pro) and its trigger are unchanged.
