# Proposal

## Why

A unit with no change dir keeps its claims table "on the pull-request checklist line" (its process-defect record has its
own line and its own file), but that line exists only once the body is drafted, so the pre-write pass runs before the
table has a home and the quick review reports the missing record. Its seed reads two input sets — the diff's added lines
and the untracked, non-ignored files — and the saved body is neither, so the claims the body states never reach it;
where the unit also has no untracked file and its diff adds no line, the seed prints nothing and that list reads like
"nothing to resolve".

## What Changes

- The review gate's pre-write step and the `/seed-claims` command state that a unit whose claims-table home is the
  pull-request body drafts and saves it from the template before the pass, so the checklist line exists when the table
  is written.
- `scripts/claim-scan.py` accepts supplementary text (`--include <path>`), scanning the saved body's lines beside the
  ordinary diff scan, so a deletion-only diff does not hide the claims the body states.
- `scripts/test-claim-scan.py` gains controls for the included text and its coexistence with the diff scan.
- The `agent-skills` requirement the pass belongs to states the ordering and that the seed scans the saved body.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/agent-skills`: the quick-review requirement's pre-write scenario gains the destination's ordering
  and that the seed scans the saved body for a unit whose claims-table home is the body.

## Impact

- `.opencode/skills/review-gate/SKILL.md`, `.opencode/commands/seed-claims.md`, `scripts/claim-scan.py`,
  `scripts/test-claim-scan.py`, `README.md`'s `/seed-claims` row, and the delta for `showcase/quality/agent-skills`.
- No JVM code, build script, deployment, or API change; the scan stays a lead list and never fails on a candidate.
