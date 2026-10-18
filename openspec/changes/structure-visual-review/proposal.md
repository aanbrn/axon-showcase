# Proposal

## Why

The `diagrammer` subagent verifies a Mermaid diagram only by rendering it with `mermaid-cli`, which proves the source
parses and lays out but cannot show node shapes, arrow attribution, duplicate labels, clipping, or a collision with
GitHub's right-edge control toolbar. The repository already requires a Mermaid render to be read by the `vision`
subagent for exactly that reason, but nothing connects the two: `vision` returns only a general description, so its
output is not decision-ready, and no step hands a render to it. A vision observation is also not a measurement — an
apparent clipping must be marked for measurement, not reported as fact.

## What Changes

- `.opencode/agent/vision.md` — strengthen the screenshot reader into a **structured visual review** (the delta and the
  definition state its shape and its checks). Read-only and concise. Its model pin is repinned to
  `opencode-go/claude-haiku-5-5` — the only provider that serves it, so as pinned the agent could not run at all.
- `.opencode/agent/diagrammer.md` — its Mermaid path returns a **visual-review handoff** beside the source (whose fields
  the delta and the definition state), renders the artifact the review reads as well as the SVG a measurement runs
  against, and stops claiming that a local render proves toolbar clearance or browser layout.
- `.opencode/commands/diagram.md` — `/diagram` orchestrates the complete Mermaid review path: it serves the render,
  delegates it to `vision` with the handoff, inspects a live GitHub render where one is reachable, reports the local
  check and the visual review separately, and states an unavailable live render as a limitation. The ASCII path is
  unchanged.
- `AGENTS.md` — the vision and diagrammer bullets agree with the above, and the model-pin bullet's parenthetical about
  what `README.md` names is corrected.
- `README.md` — the `vision` agent-table row, the `diagrammer` row, the `/diagram` slash-command row, and the model-pin
  paragraph the provider correction falsifies.
- `docs/ideas.md` — park the review-loop follow-up this change surfaced: add the reviewer's session id to the
  review-round record and check its continuity.
- `openspec/specs/showcase/quality/agent-skills/spec.md` (via a delta) — `MODIFIED` the subagents requirement: its
  "Screenshots are reviewed visually" scenario carries the structured review, the Mermaid-diagram scenario carries the
  handoff, and a new sibling scenario states the review path the caller completes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the visual and diagram subagents' behavior is refined — a structured visual review,
  the diagrammer's visual-review handoff, and the review path the caller completes.

## Impact

Agent tooling and documentation only — no build, test, runtime, or deployment effect. Touches `.opencode/agent/`,
`.opencode/commands/`, `AGENTS.md`, `README.md`, `docs/ideas.md` (a newly parked idea), `docs/review-rounds.tsv` (the
review records), and the `agent-skills` delta. Neither subagent's role changes and neither may edit repository files.
