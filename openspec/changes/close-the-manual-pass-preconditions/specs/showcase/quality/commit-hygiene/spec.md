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

When the caller also names the units whose loops must have converged (`--expect-clean <unit>`, repeatable, and valid
only with `--unique-rounds`), the same verification SHALL fail unless each named unit's numerically highest recorded
round carries no finding class, and SHALL fail when a named unit has no rows or its `classes` field is unreadable,
naming the unit, the round, and the classes found. The assertion SHALL be invoked by the review gate's manual-pass step,
and SHALL evaluate only the units the caller names, so the `check` task's invocation — which names none — never fails on
a unit's history, where a round in flight is unclean by design.

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

#### Scenario: A misfiled deep pass is acknowledged without a data row

- **WHEN** a historical number was assigned incorrectly to a deep-review pass whose authoritative record moved to the
  change's review task
- **THEN** an `# --- Acknowledged gap:` comment names the unit, the original number, and the destination
- **AND** no quick-review data row is created for that number

#### Scenario: A named unit's unclean highest round fails the verification

- **WHEN** the caller names a unit with `--expect-clean` and that unit's numerically highest recorded round carries one
  or more finding classes
- **THEN** the verification fails, naming the unit, the round, and the classes it found

#### Scenario: A named unit's clean highest round passes over earlier unclean rounds

- **WHEN** the named unit's highest recorded round carries `-` and earlier rounds carry classes
- **THEN** the verification passes, since the loop's convergence is recorded by the highest round

#### Scenario: A named unit with no rows fails rather than passing vacuously

- **WHEN** the caller names a unit the ledger does not record
- **THEN** the verification fails, naming the unit, rather than passing because no row contradicts it

#### Scenario: An unreadable classes field fails rather than reading as clean

- **WHEN** the caller names a unit whose highest row carries no classes field at all
- **THEN** the verification fails, naming the unit and the round, rather than treating the truncated row as clean

#### Scenario: A unit the caller does not name is not evaluated

- **WHEN** the ledger holds units whose highest round carries a finding class and the caller names none of them
- **THEN** the verification passes, so `check` stays green on the ledger's history
