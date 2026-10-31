## Why

The first consolidation moved the knowledge-authoring standard and the shared report contract into `docs/contracts/`,
but no audit covers them: the `agents-auditor`'s scope is `AGENTS.md`, the project-authored `.opencode/` files, and
`docs/guidance-evidence/`. A contract/spec drift has no reconciliation mechanism; the consolidation's design recorded
the risk as unmitigated and parked the widening in `docs/ideas.md`.

## What Changes

- `.opencode/agent/agents-auditor.md` — its description, intro, scope list, and Method read-list add `docs/contracts/`.
- `AGENTS.md` — the agents-auditor bullet's scope adds `docs/contracts/`.
- `README.md` (the agent table row, the prose bullet, the slash-command table row) and
  `.opencode/commands/audit-agents.md` (the description) — the four summaries stop enumerating the scope; the README
  prose bullet and the command's procedure point at the definition.
- `openspec/specs/showcase/quality/agent-skills/spec.md` gains a `MODIFIED` delta for "Project-owned agent tooling is
  audited for consistency and conciseness": the scope adds `docs/contracts/`; the 34 scenarios stay.
- `docs/ideas.md` — the parked entry is removed by this change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the auditor's scope.

## Impact

- Docs: the definition, `AGENTS.md`, `README.md`, the `/audit-agents` command, the ideas entry, and the
  `knowledge-authoring.md` qualifier the smoke-run restored.
- Specs: a `MODIFIED` delta on `showcase/quality/agent-skills` only; the main spec changes at archive.
- No runtime code, build, API, schema, image, chart, or deployment change.
