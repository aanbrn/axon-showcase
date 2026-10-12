# Proposal

## Why

The per-unit quick review is taking longer and longer — the proposal stage repeatedly takes several rounds, so a small
change now costs as much review as a large one. The rounds scale with the number of _unverified claims_ in the planning
artifacts — the four claim classes the auto-review bullet names — not with the diff, and each is already covered by a
durable rule — the rules simply fire reactively, after the claim is written, so the review spends rounds doing
derivation the author should have done at authoring time. This change moves that derivation upstream; a second change
covers the reviewer's own contract.

## What Changes

- `AGENTS.md`: a pre-write derivation pass on the auto-review bullet — before the first review round, derive the known
  claim classes from their sources — and the requirement to pass the prior round's class list on every re-run, so the
  reviewer's repeated-class flag fires rather than the loop grinding.
- `openspec/config.yaml`: per-artifact derivation rules for `proposal` and `design` (`tasks` already carries the
  task-check rule) — the authoring-time surface OpenSpec feeds the model, so the derivation is stated where each
  artifact is written.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. The change edits always-loaded guidance (`AGENTS.md`) and OpenSpec's per-artifact authoring
rules; it alters no subagent's specified behavior — the reviewer-contract change (intra-round class clustering) is a
separate follow-up, since it owes that capability's delta.

## Impact

- Files: `AGENTS.md`, `openspec/config.yaml`, `docs/ideas.md` (parking the reviewer-contract follow-up).
- No code, build, test, or deployment impact.
