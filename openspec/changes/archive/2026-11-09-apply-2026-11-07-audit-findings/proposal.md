# Correct the commit-hygiene spec's structural defects

## Why

The 2026-11-07 repository audit's spec-corpus section (`docs/audits/2026-11-07.md`) reports two structural defects in
`showcase/quality/commit-hygiene`'s spec: an orphaned sentence stands between two SHALL paragraphs of "Review-round
ledger rows are unique", and a normative SHALL paragraph sits after the scenarios of "Review-round rows record the
reviewer session's continuity". The report's other findings are separate units — the config-guard guidance correction is
`apply-2026-11-07-audit-doc-findings`, and the `handlebars` advisory is `remove-dead-handlebars-constraint` — because
`showcase/quality/agent-skills` requires findings of different routes or scopes to be split rather than batched.

## What Changes

- `openspec/changes/apply-2026-11-07-audit-findings/specs/showcase/quality/commit-hygiene/spec.md` — two `MODIFIED`
  requirement blocks: the orphaned sentence is deleted, and the detached paragraph's obligations move into its
  requirement's body, dropping the clause the body already states.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene` — two requirements' bodies are restructured; no obligation is added, removed, or
  weakened.

## Impact

- **Documents and specs**: the `commit-hygiene` delta; no `AGENTS.md`, code, build, or workflow change.
- **Behavior**: none — the restructure preserves every SHALL and every scenario.
- **Split**: the config-guard guidance correction ships as `apply-2026-11-07-audit-doc-findings`, and the `handlebars`
  advisory as `remove-dead-handlebars-constraint`.
