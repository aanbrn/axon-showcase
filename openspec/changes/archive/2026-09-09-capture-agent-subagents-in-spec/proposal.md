# Proposal: Capture the agent subagents in the agent-skills spec

## Why

The repository has accumulated several locally-defined agent subagents under `.opencode/agent/` — `review-quick`,
`review-thorough`, `lesson-capture`, `vision`, and `experience-analyzer` — but the `agent-skills` spec only captures
them inconsistently: `experience-analyzer` has a dedicated requirement, while the other four appear only as an "e.g."
list inside the vendored-skills requirement, with no requirements or scenarios of their own. The spec is the behavioral
source of truth for the repo's agent tooling, so the per-change quality-gate agents deserve the same treatment as
`experience-analyzer`: a dedicated requirement with scenarios.

## What Changes

- Add a dedicated requirement to the `agent-skills` spec covering the per-change quality-gate subagents (`review-quick`,
  `review-thorough`, `lesson-capture`, `vision`) with scenarios for each:
  - `review-quick` — the quick review run after a change's proposal and implementation, repeated until no new
    observations before a manual review.
  - `review-thorough` — the deep review pass used manually for drift/correctness/architecture/conventions.
  - `lesson-capture` — captures lessons learned from a change into `AGENTS.md` after the implementation quick review, so
    mistakes and conventions are recorded instead of relying on memory.
  - `vision` — reads screenshots/visual UI state captured by Playwright, so the main agent (on a text-only model) can
    delegate visual review.
- Keep the existing `experience-analyzer` requirement (it is already captured).
- Trim the redundant "e.g." subagent list from the vendored-skills requirement (the new requirement owns the local
  subagents; the vendored-skills requirement covers the vendored `.opencode/skills/` only).

## Capabilities

### New Capabilities

- None (this is documentation of existing agent tooling, not new behavior).

### Modified Capabilities

- `showcase/quality/agent-skills` — a new requirement documents the per-change quality-gate subagents; the
  vendored-skills requirement is narrowed to vendored skills only.

## Impact

- **Spec**: the `agent-skills` capability gains a requirement covering `review-quick`, `review-thorough`,
  `lesson-capture`, and `vision`, with scenarios; the vendored-skills requirement drops the local-subagents clause.
- **Docs**: `docs/ideas.md` — no idea to remove (none was parked for this).
- **Behavior**: none — the subagents already exist; this change only records them in the spec.
