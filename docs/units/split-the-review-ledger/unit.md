# Unit: split-the-review-ledger

Workspace:
`unit=split-the-review-ledger; path=/Users/afanasyev/Development/axon-showcase; branch=split-the-review-ledger; writer=ses_e5fa0fb2dffe0pwcgRMG3WCl2N`

State: active

The unit's plan and tasks live in `openspec/changes/split-the-review-ledger/`.

## Resume checkpoint

- **Time and revision** — 2026-11-03, `git rev-parse HEAD` = `952934b5` (the branch's base; nothing committed).
- **Workspace** — the `Workspace:` record above: path `/Users/afanasyev/Development/axon-showcase`, branch
  `split-the-review-ledger`, writer `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`.
- **Stage** — implementation. The planning artifacts are revised and validate; the data split is complete and
  independently verified; `scripts/commit-hygiene.py` is refactored to read the set of records; its test suite is not
  yet migrated, so `check` is red.
- **Next step** — migrate `scripts/test-commit-hygiene.py` to per-unit fixtures: `ledger_repo` (~line 1856),
  `UniqueReviewRoundTests._write_ledger` (~line 989), the guard's staged-ledger tests (~lines 2916–3126), and every
  message assertion. Then tasks 2.2–2.3, 3.1–3.2, 4.1–4.3 and 5.1–5.5.
- **Change set** — staged: the change dir, `docs/units/split-the-review-ledger/unit.md`, and the review-rounds row.
  Working tree: `docs/review-rounds.tsv` deleted; 257 `docs/units/<unit>/rounds.tsv` untracked;
  `scripts/commit-hygiene.py` modified and unstaged; the change artifacts edited after they were staged (five `AM`
  entries). Nothing else.
- **Blockers and open decisions** — `python3 scripts/test-commit-hygiene.py` reports 330 tests with 61 failures and 6
  errors, all in the ledger/round tests, whose fixtures still write the deleted global file. The refactor changed three
  contracts those tests assert: a message names `docs/units/<unit>/rounds.tsv:<line>` rather than a global line number,
  a duplicate message includes the record path, and the guard selects staged record paths instead of one ledger path.
  The split's measured result: 257 records, 2,327 data rows, 14 non-header comment lines, 24 header lines retired to the
  format requirement, and no boundary marker (no unit holds rows on both sides).
- **Pending decision** — none from the owner; the direction stands (split the ledger only; process defects stay).
- **Findings no record holds** — round 1's findings are now carried by the artifacts: the record format is an `ADDED`
  requirement that names itself the `review-ledger` binding's owner; the session-continuity requirement has a `MODIFIED`
  block with the marker optional per file and its missing-marker scenario narrowed, plus a one-side sibling scenario;
  the unit-column assertion is a clause and a scenario; the two missing controls (a deleted record, a marker-less
  one-side record, and now a marker-less two-side record) are in task 2.3; the singular-verb wording is fixed.
  `openspec validate --all` passes (27 items).
- **Operations in flight** — none. The one-off split and verification scripts are in `$TMPDIR/opencode`
  (`split-review-ledger.py`, `verify-split.py`) and are not committed.
