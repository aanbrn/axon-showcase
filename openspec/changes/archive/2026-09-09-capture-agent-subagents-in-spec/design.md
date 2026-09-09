## Context

The repo's `.opencode/agent/` directory holds five locally-defined subagents. The `agent-skills` spec (under
`openspec/specs/showcase/quality/agent-skills/spec.md`) already has a dedicated requirement for `experience-analyzer`
(captured in the `experience-analyzer-agent` change), but the four per-change quality-gate subagents (`review-quick`,
`review-thorough`, `lesson-capture`, `vision`) are only named in an "e.g." list inside the vendored-skills requirement.
This change gives them a dedicated requirement with scenarios, matching how `experience-analyzer` is captured.

## Goals / Non-Goals

**Goals:**

- Add a dedicated `agent-skills` requirement covering `review-quick`, `review-thorough`, `lesson-capture`, and `vision`,
  each with a scenario reflecting its actual behavior (from the agent files and AGENTS.md).
- Narrow the vendored-skills requirement to vendored skills only (it currently also mentions the local subagents, which
  the new requirement owns).

**Non-Goals:**

- No new agent behavior or files — the subagents already exist.
- No change to the existing `experience-analyzer` requirement.

## Decisions

### D1: Dedicated requirement for the per-change subagents

Add a "Per-change quality-gate and analysis subagents are available" requirement with four scenarios, one per agent,
phrased as WHEN/THEN and matching the agents' actual descriptions:

- `review-quick` — run after proposal and implementation; loop until no new observations before manual review.
- `review-thorough` — deep, on-demand pass.
- `lesson-capture` — after a clean implementation quick review, proposes AGENTS.md gotchas/conventions.
- `vision` — reads screenshots for the text-only main agent.

### D2: Narrow the vendored-skills requirement

Remove the trailing "In addition to the vendored skills, the repository SHALL provide locally-defined agent subagents
under `.opencode/agent/` …" sentence from the vendored-skills requirement — the new requirement owns the local
subagents, keeping the two requirements' scopes clean (vendored `.opencode/skills/` vs local `.opencode/agent/`).

## Risks / Trade-offs

- **Spec drift from reality** → the scenarios must match the actual agent files; the change's task includes reading each
  agent file to phrase its scenario. If an agent's behavior changes later, the scenario needs updating (same as any
  spec).
- **Delta header matching** → the MODIFIED vendored-skills requirement keeps its existing header name (a delta cannot
  rename a main-spec requirement header); only its description is trimmed.
