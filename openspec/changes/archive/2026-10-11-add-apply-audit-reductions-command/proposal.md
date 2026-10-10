# Proposal

## Why

The scheduled `audit` workflow reports the `agents-auditor`'s merge/removal/route **reduction candidates** but never
applies them, and no standing unit applies an existing report's candidates — `/audit-agents` re-derives them live rather
than draining a stored report. So applying them is an ad-hoc, easily-skipped chore: the 2026-10-04 candidates sat parked
in `docs/ideas.md` until a hand-run unit cleared them (#540), and the last retrospective recorded that the reduction
half of the loop "has no engine". This gives that half a standing, owner-gated application slot.

## What Changes

- Add `.opencode/commands/apply-audit-reductions.md` — an owner-gated unit that reads an audit report's reduction
  candidates (the newest `docs/audits/*.md`, or one named), presents them, and applies the owner-approved ones following
  each candidate's suggested edit — a merge's merged text or duplicate-deletion, a route's pointer (a content-move
  routed through the change workflow), a removal's deletion — then removes the corresponding parked entries.
- Name the unit and its per-report cadence in `AGENTS.md`'s agents-auditor bullet.
- Spec the unit in `openspec/specs/showcase/quality/agent-skills/spec.md` (a new requirement + a `## Purpose` refresh
  task, since the Purpose enumerates the capability's parts).
- Add the `/apply-audit-reductions` row and prose to `README.md`, and remove the implemented S3 idea from
  `docs/ideas.md`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the agent-tooling maintenance capability gains a standing, owner-gated unit that
  applies an audit report's reduction candidates.

## Impact

- Files: `.opencode/commands/apply-audit-reductions.md`, `AGENTS.md`,
  `openspec/specs/showcase/quality/agent-skills/spec.md`, `README.md`, `docs/ideas.md`.
- No code, build, test, or deployment impact, and no change to the scheduled `audit` workflow
  (`.github/workflows/audit.yml` and the `merge-governance` spec are untouched): the unit is a new owner-invoked
  command, not a scheduled or automatic application.
