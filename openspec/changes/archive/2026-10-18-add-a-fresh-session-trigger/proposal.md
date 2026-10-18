# Proposal

## Why

The review loop continues the reviewer's session across rounds, starting a fresh one only when it has expired or the
reviewer's definition changed. A session can also **stop responding mid-round** — observed twice in
`sharpen-architecture-audit` and twice more in `make-the-capture-selective`, where each recovery needed a fresh session
and a re-supplied hand-over. The rule names neither case, so a stalled round is an undocumented deviation and a caller
may wait on a session that will never answer. The fix is a third trigger, in the requirement that owns the rule.

## What Changes

- `showcase/quality/agent-skills` delta — the review-loop requirement's session clause gains the third trigger: a fresh
  session also when the reviewer stops responding mid-round.
- `AGENTS.md` — the "Continue the reviewer's session" bullet's trigger list synced to the requirement, keeping its
  pointer to the spec as the rule's home.
- `docs/ideas.md` — the parked idea this change implements is removed.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the per-unit quality-gate and analysis requirement's review-loop session clause gains
  the mid-round non-response trigger.

## Impact

- `openspec/changes/add-a-fresh-session-trigger/specs/showcase/quality/agent-skills/spec.md` — one `MODIFIED`
  requirement (its delta), synced into `openspec/specs/showcase/quality/agent-skills/spec.md` at archive.
- `AGENTS.md` — one decision point's trigger list; and the "Programmatic edits to a structured file" gotcha, which the
  unit's capture extended with the anchor-placement clause.
- `docs/ideas.md` — the parked "third fresh-session trigger" entry removed in the same change.
- No code, build, test, runtime, or deployment change; no agent definition changes, so no smoke-run is owed.
