## ADDED Requirements

### Requirement: Review-round ledger rows are unique

The build SHALL verify that the review-round ledger (`docs/review-rounds.tsv`) carries **at most one row per unit and
round**, failing when the same `(unit, round)` pair appears twice, so a duplicated row cannot double-count a round in
the retrospective figures aggregated from it. The verification SHALL be a `commit-hygiene.py` mode run by a `check`
task, reading the tracked ledger rather than the staged set.

#### Scenario: A duplicated round row fails the check

- **WHEN** `check` runs and the ledger carries two rows with the same unit and round
- **THEN** the check fails, naming the unit, the round, and both line numbers

#### Scenario: One row per round passes

- **WHEN** every `(unit, round)` pair in the ledger appears at most once
- **THEN** the check passes
