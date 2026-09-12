## Why

`AGENTS.md` is the agent's persistent memory — on the order of a thousand lines of conventions and gotchas, loaded into
every session. It accretes monotonically: each change's `lesson-capture` pass appends entries, and removals are rare. No
agent audits it as a whole artifact — `review-quick` and `review-thorough` are scoped to a change, `experience-analyzer`
to a time window — so contradictions between entries, stale claims and enumerations the repository has outgrown (e.g. a
"the only X" claim after a second X appeared), dead cross-references, duplication, and over-long entries accumulate
unnoticed. That is exactly the drift the self-learning loop exists to prevent. The repository already routes
specialized, model-sensitive work to pinned subagents (`vision`, `diagrammer`, `experience-analyzer`); auditing the
guidance document deserves the same treatment, and the pro model reads a long document far more thoroughly than the
cheap flash main agent.

## What Changes

- Add an `agents-auditor` subagent (`.opencode/agent/agents-auditor.md`) pinned to `opencode-go/deepseek-v4-pro` that
  audits `AGENTS.md` along two axes: **consistency** (contradictions between entries, stale claims and enumerations,
  dead cross-references, drift from the code/workflows/specs the prose describes) and **conciseness** (duplicated or
  near-duplicate entries to merge, one-off trivia, over-long or over-specific entries, misplaced entries). It verifies
  each claim against the repository rather than trusting the prose, reports findings by severity with line references
  and concrete suggested rewrites, and proposes edits without applying them.
- Add an `/audit-agents` command (`.opencode/commands/audit-agents.md`) as the subagent's trigger.
- Capture the new subagent in the `showcase/quality/agent-skills` spec.
- Record the convention in `AGENTS.md` (subagent section), add the `agents-auditor` row to the README agent table plus
  the `/audit-agents` row to the slash-command table, and add a bullet to the README's "The Self-Learning Loop" section
  presenting the audit as the memory's maintenance alongside its accretion.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: adds a requirement that an `agents-auditor` subagent audits `AGENTS.md` for
  consistency and conciseness, verifies its findings against the repository, and reports them without editing files.

## Impact

- `.opencode/agent/agents-auditor.md` — new subagent definition.
- `.opencode/commands/audit-agents.md` — new command trigger.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — added requirement (delta spec in this change).
- `AGENTS.md` (subagent convention bullet) and `README.md` (agent-table row, slash-command row, and a Self-Learning Loop
  bullet) — docs that are the change, shipped in this change's PR.
- `docs/ideas.md` — an entry parking the scheduled-workflow variant this change defers (the entry ships as its own docs
  PR, not in this change's PR).
