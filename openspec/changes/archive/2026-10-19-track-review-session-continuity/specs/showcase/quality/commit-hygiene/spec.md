# Spec Delta

## ADDED Requirements

### Requirement: Review-round rows record the reviewer session's continuity

The build SHALL verify that the review-round ledger records, for every row below the boundary its own contract marks,
the reviewer session the round ran in — its identifier, its transition, and, for a replacement, a reason the review
loop's recovery cases allow — and that no row above it carries a field count other than the ledger's pre-fields width.
The verification SHALL be a `commit-hygiene.py` mode run by a `check` task over the tracked ledger.

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
  and one of the three allowed reasons — `expired`, `definition-changed`, or `stopped-responding`
- **THEN** the continuity verification passes for that unit

#### Scenario: An unexplained session change fails the check

- **WHEN** a unit's row records a different session identifier from its preceding row without the `replaced` transition
- **THEN** the continuity verification fails, naming the unit, the round, and both identifiers

#### Scenario: A replacement without an allowed reason fails the check

- **WHEN** a row records the `replaced` transition whose reason is absent or is not one of the three allowed reasons
- **THEN** the continuity verification fails, naming the unit, the round, and the reason

#### Scenario: A reason on a non-replacement row fails the check

- **WHEN** a row whose transition is `created` or `continued` carries a replacement reason
- **THEN** the continuity verification fails, naming the unit, the round, and the reason

#### Scenario: A row whose field count is not the ledger's width fails the check

- **WHEN** a row below the ledger's boundary comment carries more or fewer fields than the contract declares
- **THEN** the continuity verification fails, naming the row and its field count

#### Scenario: A transition outside the three fails the check

- **WHEN** a row below the boundary carries a transition that is not `created`, `continued`, or `replaced`
- **THEN** the continuity verification fails, naming the unit, the round, and the value

#### Scenario: A row with no session identifier fails the check

- **WHEN** a row below the ledger's boundary comment carries no session identifier
- **THEN** the continuity verification fails, naming the row

#### Scenario: A first row that is not a creation fails the check

- **WHEN** a unit's first row below the boundary comment carries a transition other than `created`
- **THEN** the continuity verification fails, naming the unit and its round

#### Scenario: A row above the boundary whose field count is not the pre-fields width fails the check

- **WHEN** a row above the ledger's boundary comment carries a field count other than the one the ledger's rows carried
  before the session fields existed
- **THEN** the continuity verification fails, stating that the boundary was moved past the row

#### Scenario: Rows above the boundary are exempt

- **WHEN** the ledger carries rows above its boundary comment, which predate the session fields
- **THEN** the continuity verification does not require the session fields on those rows

#### Scenario: A missing boundary comment fails the check

- **WHEN** the ledger carries no boundary comment marking the first row to carry the session fields
- **THEN** the continuity verification fails, stating that the boundary is absent

#### Scenario: Distinct units may start with distinct sessions

- **WHEN** two units' first rows record different session identifiers, each with the `created` transition
- **THEN** the continuity verification passes for both
