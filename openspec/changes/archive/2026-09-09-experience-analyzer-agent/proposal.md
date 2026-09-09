# Proposal: Experience-analyzer agent for retrospectives and improvements

## Why

The repo's agent tooling has three layers per-change: `review-quick`/`review-thorough` (correctness), and
`lesson-capture` (per-change lessons into AGENTS.md). What is missing is the **top layer**: an agent that periodically
reviews recent experience across many changes and produces (1) a sprint retrospective and (2) forward-looking
improvement suggestions for both the system and the process. Today that analysis is done by the maintainer manually (or
not at all). This adds an `experience-analyzer` subagent that synthesizes the deterministic inputs (merged PRs, archived
changes, AGENTS.md gotchas, docs/ideas.md) into a retrospective + actionable improvement suggestions.

## What Changes

- Add an `experience-analyzer` subagent (`.opencode/agent/experience-analyzer.md`), modeled on `lesson-capture` but
  sprint-scoped and forward-looking: it takes the gathered inputs and produces (a) a retrospective summary — shipped PRs
  grouped by theme, lessons learned, went-well/went-wrong — and (b) improvement suggestions, each classified as a
  **system** suggestion (→ `docs/ideas.md` or an OpenSpec proposal) or a **process** suggestion (→ `AGENTS.md`). The
  main agent verifies and applies suggestions; the subagent never edits files itself.
- A deterministic **gather step** — a `scripts/experience-analysis.sh` script (or documented shell/gh commands) that
  assembles the input digest: `gh pr list --state merged --search "merged:>=<window>"`, `git log`, archived changes,
  AGENTS.md gotchas, docs/ideas.md.
- A **user-facing trigger** — the `/retrospective` opencode command (`.opencode/commands/retrospective.md`) that gathers
  the digest and invokes the subagent, so users run the analysis without knowing the internals.
- Retrospectives are stored as docs changes under `docs/retrospectives/<date>.md`.
- `AGENTS.md`: document the agent and its `/retrospective` trigger; the `agent-skills` spec covers it.

## Capabilities

### New Capabilities

- None (this is agent tooling, not a runtime/deployment behavior).

### Modified Capabilities

- `showcase/quality/agent-skills` — the capability grows from vendored skills (`.opencode/skills/`) to also cover the
  locally-defined agent subagents (`.opencode/agent/`), with `experience-analyzer` as a specified requirement.

## Impact

- **Agent tooling**: a new `.opencode/agent/experience-analyzer.md` subagent; optionally a
  `scripts/experience-analysis.sh` gatherer.
- **Docs**: `AGENTS.md` — document the agent + when to run it; `docs/retrospectives/` — where retrospective docs land.
- **Behavior**: no runtime change; the agent is an optional periodic analysis tool, invoked by the main agent on request
  or schedule.
