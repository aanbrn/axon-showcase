## ADDED Requirements

### Requirement: Review-round classes come from the reviewer's canonical list

The build SHALL verify that every finding class recorded in the review-round ledger (`docs/review-rounds.tsv`) — in the
classes-raised and the classes-that-repeated column of a row dated after the canonical boundary — is a class the
reviewer's canonical list names, so a class the list does not carry fails the build rather than splitting the
aggregation the retrospective reads. The canonical list's owner SHALL be `.opencode/agent/review-quick.md`, and the
boundary's owner SHALL be `scripts/review-rounds.py`, which records the last day whose rows may carry a pre-canonical
label; the verification SHALL read both rather than restate either. A row dated on or before that boundary SHALL pass
with the labels it was recorded with.

A label whose round's report was never reconstructed SHALL be exempt only when a comment line of the form
`# --- Acknowledged class: <unit> round <N> <label> - <reason>` names that row and that label, so an exemption covers
one label on one row rather than a unit or a label generally. The comment SHALL be accepted as the record of that fact:
the verification SHALL NOT attempt to re-derive it, as the ledger's acknowledged-gap form does not. A verification that
cannot read the ledger, the boundary, or the canonical list SHALL fail, naming the unreadable source, rather than
passing.

#### Scenario: The standard check verifies the recorded classes

- **WHEN** the standard `check` task runs
- **THEN** it includes the review-class vocabulary verification

#### Scenario: A class outside the list fails

- **WHEN** a row dated after the boundary carries, in its classes-raised column, a label the canonical list does not
  name
- **THEN** the verification fails, naming the row's line, its unit and round, the column, and the label

#### Scenario: A label in the repeated column is verified too

- **WHEN** a row dated after the boundary carries a label the canonical list does not name in its classes-that-repeated
  column, and its classes-raised column is clean
- **THEN** the verification fails, naming the repeated column and the label

#### Scenario: Canonical labels and an empty round pass

- **WHEN** every class of every post-boundary row is a class the canonical list names, or the column carries `-`
- **THEN** the verification passes

#### Scenario: A pre-boundary row keeps its recorded label

- **WHEN** a row dated on or before the boundary carries a label the canonical list does not name
- **THEN** the verification passes, since the digest maps that row's labels rather than reading them as canonical

#### Scenario: An acknowledgement exempts one label on one row

- **WHEN** a class-acknowledgement comment names a row and a label, and another row, another round of the same unit, or
  another label the list does not name on the acknowledged row carries an unknown class
- **THEN** the named label on the named row passes, and each other occurrence fails

#### Scenario: An acknowledgement's reason is not re-derived

- **WHEN** a class-acknowledgement comment names a row and a label with a reason the repository cannot evidence
- **THEN** the verification passes for that label, since the comment is the record of the fact

#### Scenario: An unreadable row fails rather than passing

- **WHEN** a post-boundary row lacks the classes column, carries a date the verification cannot read, or carries a round
  it cannot read
- **THEN** the verification fails, naming the row, rather than treating it as pre-boundary or as carrying no class

#### Scenario: An unreadable source fails rather than passing

- **WHEN** the ledger, the boundary's owner, or the canonical list cannot be read; the list's extraction finds no class,
  more than one list, or a token that is not a class; or the boundary declares no date, or a value that is not a
  written-out calendar date
- **THEN** the verification fails, naming the unreadable or ambiguous source, rather than reporting every row clean
