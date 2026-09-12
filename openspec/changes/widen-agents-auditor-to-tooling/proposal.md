## Why

The `agents-auditor` subagent (added in `add-agents-auditor-agent`) audits `AGENTS.md` — the agent's persistent memory —
because it accretes an entry per change and nothing reconciled it. The repo's project-owned agent tooling under
`.opencode/` (the subagent definitions, the project commands, and the two project skills) has the same property: it is
loaded on every invocation and drives every workflow, yet nothing audits it as a set. Drift there is real and un-gated —
a command can reference a renamed subagent, a skill can describe a step a script no longer has, the inventory can drift
from what `AGENTS.md`/`README.md` claim, a definition can carry a stale enumeration (the `add-agents-auditor-agent`
change itself fixed one: the `agent-skills` spec's subagent list had been outgrown). The `AGENTS.md` audit and this
tooling are tightly coupled — the same agents appear in `AGENTS.md` bullets, the README table, the spec, and their own
definitions — so one auditor with both artifacts in view catches the cross-artifact drift two separate auditors would
each half-see.

The widened audit must **skip the generated and vendored files**: the OpenSpec instruction files written by
`openspec update` (via `/opsx-tool-update`) and the `axon4to5-*` skills vendored verbatim from the upstream
`AxonIQ/agent-skills` repository (refreshed by re-copying, not editing). Auditing those would be wrong, not just noisy:
their correct state is "matches their upstream/generator", and a local "fix" is overwritten or breaks the provenance
contract. The skip rule is by provenance, not by name — the `opsx-tool-update` command is project-authored and stays in
scope.

## What Changes

- **Widen the `agents-auditor` subagent** (`.opencode/agent/agents-auditor.md`) from `AGENTS.md` alone to the
  project-owned agent tooling: `AGENTS.md` plus the project-authored files under `.opencode/agent/`,
  `.opencode/commands/`, and `.opencode/skills/`. The scope is a **provenance partition**, not a file list or a name
  glob: audit what the repo authors, and skip what a generator owns (the files `openspec update` writes) or the repo
  vendors (`axon4to5-*` skills) — those have their own refresh path. The existing two axes (consistency, conciseness)
  and the verify-against-the-repository and report-don't-edit contract carry over.
- **Retitle the `showcase/quality/agent-skills` requirement** from "AGENTS.md is audited for consistency and
  conciseness" to cover the widened scope. A delta cannot rename a MODIFIED header, so this is a delete-and-add
  (REMOVED + ADDED) with the full scenario set, not a MODIFIED block.
- Update the `/audit-agents` command description, the `AGENTS.md` subagent-convention bullet, and the README's
  agent-table row, slash-command row, and Self-Learning Loop bullet to describe the widened scope.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the "AGENTS.md is audited for consistency and conciseness" requirement is replaced by
  "Project-owned agent tooling is audited for consistency and conciseness", covering `AGENTS.md` and the
  project-authored `.opencode/` files, excluding generated and vendored ones.

## Impact

- `.opencode/agent/agents-auditor.md` — widened scope, provenance partition, and skip rule.
- `.opencode/commands/audit-agents.md` — description updated.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — the requirement replaced (delta spec in this change).
- `AGENTS.md` (subagent convention bullet) and `README.md` (agent-table row, slash-command row, Self-Learning Loop
  bullet) — docs that are the change, shipped in this change's PR.
