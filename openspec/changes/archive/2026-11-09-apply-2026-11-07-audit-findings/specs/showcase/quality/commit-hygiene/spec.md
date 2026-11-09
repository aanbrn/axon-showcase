# Spec Delta

## MODIFIED Requirements

### Requirement: Review-round ledger rows are unique

The build SHALL verify that the unit round records (`docs/units/<unit>/rounds.tsv`) carry **at most one row per unit and
round**, failing when the same `(unit, round)` pair appears twice, so a duplicated row cannot double-count a round in
the retrospective figures aggregated from it. A record filed under a unit's directory SHALL carry only that unit's rows:
a row whose unit column names another unit SHALL fail the verification, so a misfiled record fails rather than passing
uniqueness and contiguity on its own rows. The same verification SHALL also fail when a unit's rounds are not contiguous
from `1` to its highest round, and — when the caller names a round whose report has just arrived
(`--expect <unit>:<round>`, repeatable) — when that row is absent, so a deferred append is caught in the same turn
rather than by the next reviewer; a gap whose record is unrecoverable SHALL be acknowledged by a comment line in the
unit's record naming the unit and the round, and only an acknowledged gap SHALL pass. An expectation the mode cannot
parse, an expectation given without the mode, and a record row whose round column is not a number SHALL each fail rather
than skip. The verification SHALL be a `commit-hygiene.py` mode run by a `check` task, reading the tracked records
rather than the staged set.

When the caller also names the units whose loops must have converged (`--expect-clean <unit>`, repeatable, and valid
only with `--unique-rounds`), the same verification SHALL fail unless each named unit's numerically highest recorded
round carries no finding class, and SHALL fail when a named unit has no rows or its `classes` field is unreadable,
naming the unit, the round, and the classes found. The assertion SHALL be invoked by the review gate's manual-pass step,
and SHALL evaluate only the units the caller names, so the `check` task's invocation — which names none — never fails on
a unit's history, where a round in flight is unclean by design.

#### Scenario: A duplicated round row fails the check

- **WHEN** `check` runs and the records carry two rows with the same unit and round
- **THEN** the check fails, naming the unit, the round, and both line numbers

#### Scenario: One row per round passes

- **WHEN** every `(unit, round)` pair appears at most once, each unit's rounds are contiguous from `1` apart from
  acknowledged gaps, and every round the caller passed with `--expect` is recorded
- **THEN** the check passes

#### Scenario: A gap in a unit's rounds fails the check

- **WHEN** `check` runs and a unit's recorded rounds are `1, 2, 4`
- **THEN** the check fails, naming the unit and the missing round `3`

#### Scenario: The round whose report arrived is not recorded

- **WHEN** the caller runs the verification expecting `<unit>:3` and the records hold that unit only through round `2`
- **THEN** the check fails, naming the unit and the expected round, even though its recorded rounds are contiguous

#### Scenario: An acknowledged gap passes

- **WHEN** a gap's record is unrecoverable and the records carry an acknowledgement comment naming the unit and the
  round
- **THEN** the check passes, and a gap without such an acknowledgement still fails

#### Scenario: An unparseable or mode-less expectation fails rather than skips

- **WHEN** the verification is run with an expectation that is not `<unit>:<round>`, or with an expectation and no
  `--unique-rounds`
- **THEN** it fails with a message naming what was wrong, rather than skipping the assertion

#### Scenario: A non-numeric round fails rather than being skipped

- **WHEN** a record row's round column is present but not a number
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

- **WHEN** the caller names a unit the records do not include
- **THEN** the verification fails, naming the unit, rather than passing because no row contradicts it

#### Scenario: An unreadable classes field fails rather than reading as clean

- **WHEN** the caller names a unit whose highest row carries no classes field at all
- **THEN** the verification fails, naming the unit and the round, rather than treating the truncated row as clean

#### Scenario: A unit the caller does not name is not evaluated

- **WHEN** the records hold units whose highest round carries a finding class and the caller names none of them
- **THEN** the verification passes, so `check` stays green on the records' history

#### Scenario: A row filed under another unit's directory fails the check

- **WHEN** a unit's `rounds.tsv` carries a row whose unit column names a different unit
- **THEN** the check fails, naming the file, the row, and both unit names

### Requirement: Review-round rows record the reviewer session's continuity

The build SHALL verify that a unit's round record (`docs/units/<unit>/rounds.tsv`) whose first round predates
`2026-11-09` records, for every row below the boundary the record format marks, the reviewer session the round ran in —
its identifier, its transition, and, for a replacement, a reason the agent-session register's format requirement names
or `caller-restarted` for a restart already performed without an evidenced recovery case (a violation record, not a
recovery case) — and that no row above it carries a field count other than the format's pre-fields width. A unit whose
first round is dated on or after that date carries six-field rows: this mode SHALL judge its width, so a nine-field row
in it fails, but SHALL NOT judge its session fields, whose continuity is the agent-session register's. A record carrying
no boundary comment SHALL be read as having all its rows on the side its own rows show. The verification SHALL be a
`commit-hygiene.py` mode run by a `check` task over the tracked records. The verification SHALL validate the recorded
fields, and passing it SHALL NOT attest that the stated cause occurred or authorize the review gate to continue.

#### Scenario: A post-retirement unit's width is judged, its session fields are not

- **WHEN** a unit's first round is dated on or after `2026-11-09`
- **THEN** the mode accepts its six-field rows and rejects a nine-field one, and it requires no session fields on them

#### Scenario: The standard check verifies reviewer-session continuity

- **WHEN** the standard `check` task runs
- **THEN** it includes the reviewer-session continuity verification

#### Scenario: A unit's first row records a created session

- **WHEN** a unit's first row below the boundary carries a session identifier with the `created` transition
- **THEN** the continuity verification passes for that unit

#### Scenario: Consecutive rounds sharing one session pass

- **WHEN** a unit's later row records the same session identifier as its preceding row, with the `continued` transition
- **THEN** the continuity verification passes for that unit

#### Scenario: A repeated identifier with a transition other than continued fails the check

- **WHEN** a row records the same session identifier as its preceding row, with a transition other than `continued`
- **THEN** the continuity verification fails, naming the unit, the round, and the identifier

#### Scenario: A padded identifier names the same session as its trimmed form

- **WHEN** a row's identifier carries surrounding whitespace and its preceding row's identifier is the same value
  trimmed
- **THEN** the continuity verification treats them as one session, so the row continues it

#### Scenario: An allowed replacement passes with its reason

- **WHEN** a unit's row records a different session identifier from its preceding row, with the `replaced` transition
  and a reason the record format names
- **THEN** the continuity verification passes for that unit

#### Scenario: An unexplained session change fails the check

- **WHEN** a unit's row records a different session identifier from its preceding row without the `replaced` transition
- **THEN** the continuity verification fails, naming the unit, the round, and both identifiers

#### Scenario: A replacement without an allowed reason fails the check

- **WHEN** a row records the `replaced` transition whose reason is absent or is not one of the recorded reasons
- **THEN** the continuity verification fails, naming the unit, the round, and the reason

#### Scenario: A reason on a non-replacement row fails the check

- **WHEN** a row whose transition is `created` or `continued` carries a replacement reason
- **THEN** the continuity verification fails, naming the unit, the round, and the reason

#### Scenario: A row whose field count is not the ledger's width fails the check

- **WHEN** a row below the record's boundary comment carries more or fewer fields than the contract declares
- **THEN** the continuity verification fails, naming the row and its field count

#### Scenario: A transition outside the three fails the check

- **WHEN** a row below the boundary carries a transition that is not `created`, `continued`, or `replaced`
- **THEN** the continuity verification fails, naming the unit, the round, and the value

#### Scenario: A row with no session identifier fails the check

- **WHEN** a row below the record's boundary comment carries no session identifier
- **THEN** the continuity verification fails, naming the row

#### Scenario: A first row that is not a creation fails the check

- **WHEN** a unit's first row below the boundary comment carries a transition other than `created`
- **THEN** the continuity verification fails, naming the unit and its round

#### Scenario: A row above the boundary whose field count is not the pre-fields width fails the check

- **WHEN** a unit's record carries a row above its boundary comment with a field count other than the one the rows
  carried before the session fields existed
- **THEN** the continuity verification fails, stating that the boundary was moved past the row

#### Scenario: Rows above the boundary are exempt

- **WHEN** a unit's record carries rows above its boundary comment, which predate the session fields
- **THEN** the continuity verification does not require the session fields on those rows

#### Scenario: A missing boundary comment fails the check

- **WHEN** a unit's record holds rows on both sides of the session-fields boundary and carries no boundary comment
- **THEN** the continuity verification fails, stating that the boundary is absent

#### Scenario: A record whose rows all lie on one side carries no marker

- **WHEN** a unit's record's rows all predate the session fields, or all carry them
- **THEN** the continuity verification requires no boundary comment and judges every row on that one side

#### Scenario: Distinct units may start with distinct sessions

- **WHEN** two units' first rows record different session identifiers, each with the `created` transition
- **THEN** the continuity verification passes for both

#### Scenario: An already-performed caller restart is recorded without inventing a recovery cause

- **WHEN** a different reviewer session was already started without an evidenced recovery case
- **THEN** its row records `replaced` with `caller-restarted`
- **AND** the record identifies the continuity violation and its correction
- **AND** accepting that record does not authorize another restart
