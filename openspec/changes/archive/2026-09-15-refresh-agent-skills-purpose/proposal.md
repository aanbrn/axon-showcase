## Why

The `agent-skills` capability's Purpose enumerates the subagents it provides as "the per-change review and
lesson-capture agents, the experience-analyzer, and the on-demand auditors", but the capability's own requirement
"Per-change quality-gate and analysis subagents are available" also covers **the visual and diagram agents**. A reader
of the Purpose does not learn the capability provides them.

## What Changes

- The `agent-skills` Purpose gains "the visual and diagram agents" in its enumeration of the locally-defined subagents,
  matching the wording its requirement already uses.
- No requirement, scenario, or behavior text changes — a Purpose-only refresh, declared via `skip_specs: true` (a
  `## Purpose` cannot ride a delta).

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — this corrects the capability's `## Purpose` prose, which is not a requirement and so cannot ride a delta; the
  edit is made directly as a `skip_specs` change, as the analogous title fixes were.

## Impact

- `openspec/specs/showcase/quality/agent-skills/spec.md` — the `## Purpose` paragraph's subagent enumeration only.
- `docs/ideas.md` — the parked idea this implements is removed (it rides this change's branch).
- No agent definition, `AGENTS.md`, `README.md`, behavior, code, or deployment change.
