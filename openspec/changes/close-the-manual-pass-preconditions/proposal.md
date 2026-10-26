# Proposal

## Why

A unit's review gate can look closed while its loop never converged. `docs/review-rounds.tsv` records one row per round,
and both ledger checks pass on a unit whose highest row still carries a finding class: neither reads the `classes`
column. Eight units in the ledger were in that state at this change's base revision. One unit that merged in this
session ran six finding-bearing rounds before its first clean one. The same step asks for the owner's manual pass
without naming the unit's capture record, which the capture workflow requires before that ask.

## What Changes

- `scripts/commit-hygiene.py` gains `--expect-clean <unit>` (repeatable, and only valid with `--unique-rounds`): it
  fails when the unit names no row, when its numerically highest round's `classes` field is not `-`, or when that field
  is unreadable, naming the unit, the round, and the classes it found.
- The mode is unit-scoped and called by the review gate's manual-pass step, so no existing unit's record fails `check`;
  the `check` task keeps its current `--unique-rounds` call unchanged.
- `scripts/test-commit-hygiene.py` covers the mode: a clean highest row passes, an unclean one fails naming its unit and
  round, a unit with no rows fails, and an earlier clean round does not mask a later unclean one.
- `.opencode/skills/review-gate/SKILL.md` §2 names the invocation at the manual-pass boundary, beside — and outside the
  repeat-before-the-next-reviewer rule that governs — `--expect` and the session-continuity check, which it does not
  replace.
- `.opencode/skills/review-gate/SKILL.md` §5 names the ask's conditions at the step that makes it — the recorded
  pre-write pass and its claims table, a closing round carrying no finding class, and the unit's capture record at the
  implementation stage — beside the clean quick review and any required deep review it already names.
- `docs/ideas.md` loses the parked precondition of 2026-10-26, which this change delivers.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/commit-hygiene`: the review-round ledger requirement gains the clean-round verification, its failure
  conditions, and its exclusion from the `check` task.

## Impact

`scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, `.opencode/skills/review-gate/SKILL.md`, and
`docs/ideas.md`. No Gradle task wiring changes, since the mode is invoked by the gate rather than by `check`; no runtime
code, no service behaviour, no deployment surface.
