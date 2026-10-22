# Proposal

## Why

The review-round ledger lost rows in two consecutive units (rounds 15, 18, 19 and then 3, 8): the append is deferred
inside the fix loop, and the next reviewer then reports the missing row as a finding, which costs extra review work. The
existing check (`commit-hygiene.py --unique-rounds`) detects only duplicate `(unit, round)` pairs, so a missing row is
invisible to every gate, and the contiguity idea has been parked since `separate-agents-md-core-and-subject-packs`
(2026-10-21) hit the same gap.

## What Changes

- Extend `scripts/commit-hygiene.py`'s `--unique-rounds` mode: keep the duplicate check, add per-unit contiguity
  (`1..max` with no gap), and add an expected-round assertion (`--expect <unit>:<round>`, repeatable) so the checkpoint
  can demand the row for a round whose report has just arrived — the case contiguity alone cannot see, because a missing
  _last_ row only lowers `max`.
- Acknowledge the one historical gap explicitly in the ledger (a comment line naming the unit and round, whose report is
  not recoverable), so the check is strict for every new row without inventing a record.
- Run the checkpoint inside the review gate: after the append and before the fixes, as a preflight before the next
  reviewer invocation, and after a clean result before the manual pass — one mechanism at three points.
- Record the verification in the two owning requirements, and remove the implemented parked idea from `docs/ideas.md`.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/commit-hygiene`: the requirement that promises "at most one row per unit and round" now also
  requires per-unit contiguity, an expected-round assertion for the round whose report has arrived, and an explicit
  acknowledgement for a gap whose record is unrecoverable.
- `showcase/quality/agent-skills`: the review gate's per-round record requirement gains the checkpoint that runs the
  verification at the three points, so a deferred append is caught before the next reviewer invocation rather than by
  it.

## Impact

- `scripts/commit-hygiene.py` and `scripts/test-commit-hygiene.py` — the mode, its CLI, and its committed fixtures.
- `build.gradle.kts` — the `verifyUniqueReviewRounds` description.
- `AGENTS.md` — the review gate's step 3 (the checkpoint and its three placements).
- `docs/review-rounds.tsv` — one acknowledgement comment line.
- `docs/ideas.md` — the implemented idea removed, the wrapper idea parked with its feasibility findings.
- No reviewer definition, mode, or model pin changes; the wrapper stays parked.
