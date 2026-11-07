# showcase/quality/commit-hygiene Specification

## MODIFIED Requirements

### Requirement: The review-round record format is stated once

Each unit's review-round record SHALL be the file `docs/units/<unit>/rounds.tsv`, holding that unit's data rows in the
order they were recorded, as tab-separated fields: the date (`YYYY-MM-DD`); the unit, which SHALL name the directory the
record is filed under; the round, continuous per unit across its proposal and implementation reviews; the classes
raised, comma-separated or `-`; the classes that repeated a prior round, comma-separated or `-`; the compliance-miss
count; the reviewer's session identifier; the session transition (`created`, `continued`, or `replaced`); and a
replacement's reason (`expired`, `definition-changed`, `stopped-responding`, `parent-session-changed`, or
`caller-restarted`; `-` otherwise).

The boundary between the rows that predate the session fields and those that carry them SHALL be marked by a comment
line naming it. A record SHALL carry that marker only where it holds rows on both sides, and a record without one SHALL
be read as having all its rows on the side its own rows show. A row carrying the pre-fields width SHALL be historical
only when it is dated on or before the last day the fields were absent (2026-10-18); a later row lacks the fields and
SHALL fail, so the exemption cannot be claimed by writing the old width today.

A gap whose report was never reconstructed SHALL be acknowledged by a comment line of the form
`# --- Acknowledged gap: <unit> round <N> - <reason>`, and a class recorded before the canonical list landed, whose
round's report was never reconstructed, by `# --- Acknowledged class: <unit> round <N> <label> - <reason>`, which covers
that label on that row only.

This requirement SHALL own the record format, so the `review-ledger` binding's owner cell SHALL name it rather than the
retired header, and no per-unit file SHALL carry a normative header of its own.

#### Scenario: A record's fields parse in the stated order

- **WHEN** a unit's `rounds.tsv` carries a data row of the nine fields
- **THEN** the record's readers parse the date, unit, round, classes raised, classes repeated, compliance misses,
  session identifier, transition, and replacement reason from it

#### Scenario: A record's unit column names its directory

- **WHEN** a record is filed at `docs/units/<unit>/rounds.tsv`
- **THEN** every row's unit column names that same `<unit>`

#### Scenario: A record holding rows on both sides carries the marker

- **WHEN** a record holds rows that predate the session fields beside rows that carry them
- **THEN** the record carries the boundary comment at the position dividing them

#### Scenario: A session a new main session cannot continue carries its own reason

- **WHEN** a unit resumes in a main session other than the one that created its reviewer's session
- **THEN** the row recording the reviewer's new session carries `parent-session-changed` as its reason, rather than
  `expired`, which would name a cause the evidence does not establish

#### Scenario: A six-field row dated after the fields fails

- **WHEN** a record row dated after 2026-10-18 carries the pre-fields width
- **THEN** the continuity verification fails, naming the row, its date, and the fields it lacks

### Requirement: Review-round rows record the reviewer session's continuity

The build SHALL verify that a unit's round record (`docs/units/<unit>/rounds.tsv`) records, for every row below the
boundary the record format marks, the reviewer session the round ran in — its identifier, its transition, and, for a
replacement, a reason the record format allows or `caller-restarted` for a restart already performed without an
evidenced recovery case (a violation record, not a recovery case) — and that no row above it carries a field count other
than the format's pre-fields width. A record carrying no boundary comment SHALL be read as having all its rows on the
side its own rows show, so the comment SHALL be required only where a record holds rows on both sides. The verification
SHALL be a `commit-hygiene.py` mode run by a `check` task over the tracked records.

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

`caller-restarted` SHALL record a continuity violation, not an additional permitted recovery case; the verification
SHALL validate the recorded fields, and passing it SHALL NOT attest that the stated cause occurred or authorize the
review gate to continue.

### Requirement: A unit's directory holds only the record kinds the layout names

A unit's directory, `docs/units/<unit>/`, admits exactly: `unit.md`, `rounds.tsv`, `review-handover.md`,
`process-defects.md` and `sessions.tsv` at its first level, and `findings/round-<N>.md` at its second. Presence is not
required; any other tracked path below it SHALL fail. The verification SHALL be a `commit-hygiene.py` mode run by a
`check` task over the tracked records, naming the unit, the path and that the layout does not admit it.

#### Scenario: The standard check verifies the layout

- **WHEN** the standard `check` task runs
- **THEN** it includes the unit-layout verification

#### Scenario: A new kind is added with its verification

- **WHEN** a change first writes a record kind the admitted set does not name
- **THEN** that change amends this requirement and its verification, rather than placing the file alone

#### Scenario: A unit holding only admitted kinds passes

- **WHEN** every tracked path under a unit's directory is `unit.md`, `rounds.tsv`, `review-handover.md`,
  `process-defects.md`, `sessions.tsv`, or `findings/round-<N>.md`
- **THEN** the verification passes for that unit

#### Scenario: A file at the unit's root that the layout does not admit fails

- **WHEN** a tracked file sits directly in `docs/units/<unit>/` and its name is not one of the five single-file kinds
- **THEN** the verification fails, naming the unit, the path and that the layout does not admit it

#### Scenario: A path the two levels do not admit fails

- **WHEN** a tracked path below `docs/units/<unit>/` is neither one of the five first-level filenames nor
  `findings/round-<N>.md` — a third level such as `findings/consultations/advisor-1.md`, or another location such as
  `other/round-1.md`
- **THEN** the verification fails, naming the unit, the path and that the layout does not admit it, so neither a new
  kind nor a new location can be smuggled in

#### Scenario: Presence is not required

- **WHEN** a unit carries only `rounds.tsv`, or only `rounds.tsv` and its instances files
- **THEN** the verification passes, since the layout admits kinds and each kind's presence is a separate requirement's
  subject

#### Scenario: An untracked file is outside the verification

- **WHEN** a file under a unit's directory is not tracked
- **THEN** the verification does not judge it, because it reads the tracked records

## ADDED Requirements

### Requirement: The agent-session record is stated once

Each unit's agent-session record SHALL be the file `docs/units/<unit>/sessions.tsv`, holding that unit's rows of
registered invocations in the order they were recorded, as tab-separated fields: the date (`YYYY-MM-DD`); the unit,
which SHALL name the directory the record is filed under; the invoking main session's identifier; the agent, which SHALL
name a registered agent by its own name; the scope, which SHALL be the unit for a reviewer and a kebab-case decision
slug for a consultation; the opinion (`primary` or `independent` for a consultation, `-` for a reviewer); the pass,
continuous per `(unit, agent, scope, opinion)`; the requested session identifier (`-` when the invocation was asked for
none); the returned session identifier (`-` when the invocation reported none); the transition (`created`, `continued`,
`replaced`, or `-`); a replacement's reason, from the set the review-round record's format requirement names, where
`caller-restarted` records a violation rather than a justified case (`-` otherwise); the hand-over section's slug; and
the outcome (`report`, `stalled`, `failed`, or `mismatch`).

The hand-over section's slug SHALL name a second-level heading in the unit's `review-handover.md`, which SHALL begin
with that slug in backticks, so the row and the context it points at resolve to each other.

The quick reviewer SHALL NOT be recorded here: its session's continuity is the review-round record's. A consultation
that starts no unit SHALL NOT be recorded here either.

This requirement SHALL own the agent-session record's format, and no per-unit file SHALL carry a normative header of its
own. The set of registered agents is the requirement recording their sessions, not this one.

#### Scenario: A record's fields parse in the stated order

- **WHEN** a unit's `sessions.tsv` carries a data row of the thirteen fields
- **THEN** the record's readers parse the date, unit, main session, agent, scope, opinion, pass, requested identifier,
  returned identifier, transition, reason, hand-over slug, and outcome from it

#### Scenario: A record's unit column names its directory

- **WHEN** a record is filed at `docs/units/<unit>/sessions.tsv`
- **THEN** every row's unit column names that same `<unit>`

#### Scenario: A hand-over slug resolves to its section

- **WHEN** a row carries a hand-over slug
- **THEN** the unit's `review-handover.md` carries a second-level heading that begins with that slug in backticks

#### Scenario: An opinion belongs to a consultation

- **WHEN** a reviewer row carries a non-`-` opinion, or a consultation row carries `-`
- **THEN** the verification fails, naming the row

#### Scenario: A scope names its agent's subject

- **WHEN** a reviewer row's scope differs from the unit, or a consultation row's scope is not a kebab-case slug
- **THEN** the verification fails, naming the row

#### Scenario: A quick-reviewer row is not admitted

- **WHEN** a row names `review-quick` as its agent
- **THEN** the verification fails, naming the row, because the quick reviewer's continuity is the review-round record's

#### Scenario: An outside-unit consultation is not recorded

- **WHEN** a consultation starts no unit
- **THEN** it writes no agent-session row, and its report is not a review round

### Requirement: Agent-session rows record the session's continuity

The build SHALL verify each agent-session row's identity and continuity, per `(unit, agent, scope, opinion)`:

- a `report` row SHALL carry a returned identifier, and a transition of `created` when it is the key's first resolved
  row, `continued` when its returned identifier equals the last resolved row's, or `replaced` with an allowed reason
  otherwise;
- a `created` row SHALL carry `-` as its requested identifier;
- a `continued` row SHALL carry the last resolved row's identifier as its requested one, and return that same one;
- a `report` row whose requested identifier is present SHALL return that same identifier;
- a `replaced` row SHALL carry `-` as its requested identifier;
- a `stalled` or `failed` row SHALL carry no transition, no reason, and `-` as its returned identifier, and SHALL NOT
  advance the key's resolved session;
- a `mismatch` row SHALL carry no transition and no reason, a requested identifier that is present, and a returned
  identifier that differs from it, and SHALL NOT advance the key's resolved session, recording a continuity failure
  rather than a replacement;
- a `continued` row's main session SHALL equal the last resolved row's, and a `parent-session-changed` row's SHALL
  differ from it, since a child session cannot be continued from another main session;
- the pass SHALL number every row continuously from `1` per key, whether or not the row resolves a session;
- a `replaced` row SHALL carry a reason the review-round record's format requirement names, and a row whose transition
  is not `replaced` SHALL carry none;
- a row naming an agent the registering requirement does not register SHALL fail;
- a row's hand-over slug SHALL resolve to a section, as the format requirement states.

The record SHALL be append-only: a row SHALL NOT be removed or rewritten, so a staged record's committed prefix SHALL
equal the committed record's rows.

The verification SHALL be a `commit-hygiene.py` mode run by a `check` task over the tracked records, and the pre-commit
guard SHALL refuse a staged record whose verification reports a violation, judging the staged content rather than the
working tree's copy, and a staged record that removes or rewrites a committed row.

Passing the verification SHALL validate the recorded fields only: it SHALL NOT attest that a replacement's stated cause
occurred, that a forced replacement was reported to the owner, or that a discretionary one was approved.

#### Scenario: The standard check verifies agent-session continuity

- **WHEN** the standard `check` task runs
- **THEN** it includes the agent-session continuity verification

#### Scenario: A key's first resolved row creates its session

- **WHEN** a key's first row with the `report` outcome carries a returned identifier and the `created` transition
- **THEN** the verification passes for that key

#### Scenario: A later row continuing the same session passes

- **WHEN** a later `report` row of the same key returns the identifier the last resolved row returned, with `continued`
  and the same main session
- **THEN** the verification passes for that key

#### Scenario: A continuation from another main session fails

- **WHEN** a `continued` row's main session differs from the last resolved row's
- **THEN** the verification fails, naming the row and that a child session cannot be continued from another main session

#### Scenario: A report row returning another requested identifier fails

- **WHEN** a `report` row carries a requested identifier and returns a different one
- **THEN** the verification fails, naming the row, because a continuation returns the session it was sent to and a
  creation is recorded with no requested identifier

#### Scenario: A created row naming a requested identifier fails

- **WHEN** a row with the `created` transition carries a requested identifier other than `-`
- **THEN** the verification fails, naming the row, because a creation asks for no session

#### Scenario: A stalled or failed row carrying a returned identifier fails

- **WHEN** a `stalled` or `failed` row carries a returned identifier other than `-`
- **THEN** the verification fails, naming the row, because neither outcome reports a session

#### Scenario: A continued row without its requested identifier fails

- **WHEN** a `continued` row carries `-` as its requested identifier, or a requested identifier other than the last
  resolved row's
- **THEN** the verification fails, naming the row, because a continuation asks for the session it continues

#### Scenario: A replaced row carrying a requested identifier fails

- **WHEN** a `replaced` row carries a requested identifier other than `-`
- **THEN** the verification fails, naming the row, because a replacement starts a session and asks for none

#### Scenario: A mismatch row without a returned identifier fails

- **WHEN** a `mismatch` row carries `-` as its returned identifier
- **THEN** the verification fails, naming the row, because the failure is a returned identifier differing from the
  requested one

#### Scenario: A mismatch row without a requested identifier fails

- **WHEN** a `mismatch` row carries `-` as its requested identifier
- **THEN** the verification fails, naming the row, because a mismatch names the identifier it asked for and the one it
  got

#### Scenario: A different identifier without a replacement fails

- **WHEN** a `report` row returns an identifier different from the last resolved row's without the `replaced` transition
- **THEN** the verification fails, naming the unit, the agent, the scope, the opinion, and both identifiers

#### Scenario: A parent-session replacement with the same main session fails

- **WHEN** a `replaced` row records `parent-session-changed` while its main session equals the last resolved row's
- **THEN** the verification fails, naming the row, because the recorded cause contradicts the recorded parentage

#### Scenario: An unresolved row does not advance the session

- **WHEN** a `stalled` or `failed` row carries no transition, and the key's next `report` row continues the last
  resolved session
- **THEN** the verification passes, the unresolved row advances no session, and its pass still counts in the key's
  sequence

#### Scenario: A transition on an unresolved row fails

- **WHEN** a `stalled`, `failed`, or `mismatch` row carries a transition
- **THEN** the verification fails, naming the row, because only a `report` row establishes a session

#### Scenario: A report row without a returned identifier fails

- **WHEN** a row with the `report` outcome carries no returned identifier
- **THEN** the verification fails, naming the row, because a report establishes the session it ran in

#### Scenario: A mismatched identifier is a recorded failure, not a replacement

- **WHEN** a row returns an identifier different from the one it requested, with the `mismatch` outcome and no
  transition
- **THEN** the verification passes for the row's shape, and the key's resolved session is unchanged
- **AND** a `mismatch` row whose identifiers agree fails the verification

#### Scenario: A replacement without an allowed reason fails

- **WHEN** a `replaced` row carries no reason or one the format requirement does not name
- **THEN** the verification fails, naming the row and the reason

#### Scenario: A reason on a non-replacement row fails

- **WHEN** a row whose transition is not `replaced` carries a reason
- **THEN** the verification fails, naming the row

#### Scenario: A non-contiguous pass fails

- **WHEN** a key's passes are not contiguous from `1`
- **THEN** the verification fails, naming the key and the missing pass

#### Scenario: An unregistered agent fails

- **WHEN** a row names an agent the registering requirement does not register
- **THEN** the verification fails, naming the row and the agent

#### Scenario: A missing hand-over section fails

- **WHEN** a row's hand-over slug has no matching second-level heading in the unit's `review-handover.md`
- **THEN** the verification fails, naming the row and the slug

#### Scenario: A removed row refuses the commit

- **WHEN** a staged record's diff removes a row the committed record carries
- **THEN** the guard refuses the commit and names the removal, because the record is append-only

#### Scenario: A rewritten row refuses the commit

- **WHEN** a staged record's committed prefix differs from the committed record's rows
- **THEN** the guard refuses the commit and names the rewritten row, because an existing row is never rewritten

#### Scenario: A staged violation refuses the commit

- **WHEN** a staged record carries a row the verification rejects
- **THEN** the guard refuses the commit and names the row
