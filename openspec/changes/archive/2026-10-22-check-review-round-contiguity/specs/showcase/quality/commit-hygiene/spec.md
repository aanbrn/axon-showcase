# Delta

## MODIFIED Requirements

### Requirement: Review-round ledger rows are unique

The build SHALL verify that the review-round ledger (`docs/review-rounds.tsv`) carries **at most one row per unit and
round**, failing when the same `(unit, round)` pair appears twice, so a duplicated row cannot double-count a round in
the retrospective figures aggregated from it. The same verification SHALL also fail when a unit's rounds are not
contiguous from `1` to its highest round, and — when the caller names a round whose report has just arrived
(`--expect <unit>:<round>`, repeatable) — when that row is absent, so a deferred append is caught in the same turn
rather than by the next reviewer; a gap whose record is unrecoverable SHALL be acknowledged by a comment line in the
ledger naming the unit and the round, and only an acknowledged gap SHALL pass. An expectation the mode cannot parse, an
expectation given without the mode, and a ledger row whose round column is not a number SHALL each fail rather than
skip. The verification SHALL be a `commit-hygiene.py` mode run by a `check` task, reading the tracked ledger rather than
the staged set.

#### Scenario: A duplicated round row fails the check

- **WHEN** `check` runs and the ledger carries two rows with the same unit and round
- **THEN** the check fails, naming the unit, the round, and both line numbers

#### Scenario: One row per round passes

- **WHEN** every `(unit, round)` pair appears at most once, each unit's rounds are contiguous from `1` apart from
  acknowledged gaps, and every round the caller passed with `--expect` is recorded
- **THEN** the check passes

#### Scenario: A gap in a unit's rounds fails the check

- **WHEN** `check` runs and a unit's recorded rounds are `1, 2, 4`
- **THEN** the check fails, naming the unit and the missing round `3`

#### Scenario: The round whose report arrived is not recorded

- **WHEN** the caller runs the verification expecting `<unit>:3` and the ledger records that unit only through round `2`
- **THEN** the check fails, naming the unit and the expected round, even though its recorded rounds are contiguous

#### Scenario: An acknowledged gap passes

- **WHEN** a gap's record is unrecoverable and the ledger carries an acknowledgement comment naming the unit and the
  round
- **THEN** the check passes, and a gap without such an acknowledgement still fails

#### Scenario: An unparseable or mode-less expectation fails rather than skips

- **WHEN** the verification is run with an expectation that is not `<unit>:<round>`, or with an expectation and no
  `--unique-rounds`
- **THEN** it fails with a message naming what was wrong, rather than skipping the assertion

#### Scenario: A non-numeric round fails rather than being skipped

- **WHEN** a ledger row's round column is present but not a number
- **THEN** the check fails, naming the unit and the value, rather than ignoring the row
