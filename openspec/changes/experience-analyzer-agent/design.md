## Context

The repo's agent tooling under `.opencode/agent/` is layered per-change: `review-quick`/`review-thorough` (correctness,
before merge) and `lesson-capture` (per-change lessons → AGENTS.md). There is no agent that aggregates across many
changes to produce a retrospective and improvement suggestions; the `experience-analyzer` agent fills that gap.

## Goals / Non-Goals

**Goals:**

- An `experience-analyzer` subagent that synthesizes a retrospective + improvement suggestions from deterministic
  inputs, following the `lesson-capture` pattern (proposes; main agent verifies and applies).
- A documented, repeatable gather step (shell/gh commands, optionally a `scripts/experience-analysis.sh`).
- Retrospectives stored under `docs/retrospectives/<date>.md` as docs changes.

**Non-Goals:**

- No automatic scheduling/triggering of the agent — it runs on the maintainer's request (or an explicit schedule), not
  wired into the build or CI.
- No changes to the per-change review/lesson-capture agents.
- No runtime/deployment behavior.

## Decisions

### D1: The `experience-analyzer` subagent (proposal-only, like `lesson-capture`)

Add `.opencode/agent/experience-analyzer.md` with the same shape as `lesson-capture` (frontmatter: `mode: subagent`,
cheap model, `temperature: 0`). Its prompt takes the gathered input digest (merged PRs, archived changes, AGENTS.md
gotchas, docs/ideas.md, and a short context note from the main agent) and produces:

- A **retrospective** section: shipped PRs grouped by theme, lessons learned (from gotchas + per-change captures),
  went-well/went-wrong observations.
- An **improvement suggestions** section: each suggestion classified as `system` (→ `docs/ideas.md` or an OpenSpec
  proposal) or `process` (→ `AGENTS.md`), with the reasoning.

It never edits files — it returns the proposed retrospective text and suggestion list for the main agent to verify and
apply.

### D2: Deterministic gather step

The inputs are assembled by the main agent via documented shell/gh commands (or a thin `scripts/experience-analysis.sh`
wrapper):

- `gh pr list --state merged --search "merged:>=<window>" --json number,title,mergedAt`
- `git log --since <window> --oneline`
- `ls openspec/changes/archive/`
- AGENTS.md gotchas (grep the Gotchas section) + `docs/ideas.md`

The script (if built) prints a structured digest to stdout; the main agent feeds it to the subagent. Whether to build
the script or just document the commands is an open question left to the gather-step task.

### D3: User-facing trigger (the `/retrospective` command)

The analysis is invoked by the user through a slash-command, not by knowing the internals: a
`.opencode/commands/retrospective.md` command file that (1) determines the window from an optional `since` argument
(default 7 days), (2) gathers the digest via `scripts/experience-analysis.sh`, (3) invokes the `experience-analyzer`
subagent with the digest + a context note, and (4) presents the retrospective + suggestions and asks which to apply.
This matches the repo's command convention (`/ideas`, `/dependency-updates`, `/opsx-*`).

### D4: Retrospective storage + docs

Retrospectives land under `docs/retrospectives/<date>.md` (new directory), committed as a docs change. `AGENTS.md`
documents the agent and the `/retrospective` trigger. The `agent-skills` spec gains a requirement covering the local
agent subagents, with `experience-analyzer` specified.

## Risks / Trade-offs

- **Agent-value dependence on input quality** → the subagent is only as good as the digest; the deterministic gather
  step keeps input complete (PRs + changes + gotchas + ideas), and the main agent's context note fills the gaps that no
  diff captures (process mistakes).
- **Overlap with `lesson-capture`** → the two are complementary: `lesson-capture` records per-change gotchas into
  AGENTS.md; `experience-analyzer` aggregates those (plus PR history) into a periodic view and forward-looking
  suggestions. Scope the retrospective section to reuse AGENTS.md gotchas rather than re-deriving them.
- **Scope creep** → the change is agent tooling + docs + a gather script; no build/CI/runtime wiring. Keep it that way.
