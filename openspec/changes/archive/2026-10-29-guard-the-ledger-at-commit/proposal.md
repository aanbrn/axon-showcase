# Proposal

## Why

The pre-commit guard runs every staged-set check except the review-round ledger's own: `commit-hygiene.py --staged` runs
no duplicate/contiguity or session-continuity verification, so those defects reach CI and are caught there, if at all.
The ledger is also treated as append-only by convention — the 2026-10-24 idea records two mis-transitioned rows reaching
CI and the gap this leaves — but no rule states it, so a row that disappears with a wrong-base re-apply passes review:
contiguity catches a removed middle row as a gap, but not a removed highest row, not a unit's whole set, and not a
ledger deleted or renamed away.

## What Changes

- `scripts/commit-hygiene.py`'s staged check runs the ledger's duplicate/contiguity verification and its
  session-continuity check when `docs/review-rounds.tsv` is staged, reading the staged blob through the guard's existing
  `staged_file_text`, and reports each failure as a named guard reason.
- A staged ledger row whose round column cannot be parsed is a named failure, never a traceback.
- The staged check refuses a removal of a data row unless that `(unit, round)` remains in the staged ledger, so a
  duplicated row's repair passes and a row's loss fails.
- `scripts/git-hooks/pre-commit`'s header, `README.md`'s guard enumeration and the `AGENTS.md` gotcha's enumeration name
  the ledger check they gain.
- `docs/ideas.md` loses the 2026-10-24 entry this change implements.
- The `commit-hygiene` spec's pre-commit requirement gains the ledger checks and the append-only rule as separate
  SHALLs, each with its own scenario; the ledger requirement's input clause is scoped to the build's verification, since
  the guard judges the staged content. The capability's Purpose names the commit-time checks, so its refresh is a task
  for the archive commit (a delta cannot carry a Purpose).

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/commit-hygiene`: the pre-commit guard's enumerated checks gain the review-round ledger's
  duplicate/contiguity and session-continuity verification and the append-only rule.

## Impact

- `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, `scripts/git-hooks/pre-commit`, `README.md`,
  `AGENTS.md`, `docs/ideas.md`, and the delta for `showcase/quality/commit-hygiene`.
- No JVM code, build script, deployment, or API change; the `check` task's two ledger verifications and the
  caller-driven `--expect`/`--expect-clean` modes are unchanged.
