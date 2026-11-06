## ADDED Requirements

### Requirement: A unit's directory holds only the record kinds the layout names

A unit's directory, `docs/units/<unit>/`, admits exactly: `unit.md`, `rounds.tsv`, `review-handover.md` and
`process-defects.md` at its first level, and `findings/round-<N>.md` at its second. Presence is not required; any other
tracked path below it SHALL fail. The verification SHALL be a `commit-hygiene.py` mode run by a `check` task over the
tracked records, naming the unit, the path and that the layout does not admit it.

#### Scenario: The standard check verifies the layout

- **WHEN** the standard `check` task runs
- **THEN** it includes the unit-layout verification

#### Scenario: A new kind is added with its verification

- **WHEN** a change first writes a record kind the admitted set does not name
- **THEN** that change amends this requirement and its verification, rather than placing the file alone

#### Scenario: A unit holding only admitted kinds passes

- **WHEN** every tracked path under a unit's directory is `unit.md`, `rounds.tsv`, `review-handover.md`,
  `process-defects.md`, or `findings/round-<N>.md`
- **THEN** the verification passes for that unit

#### Scenario: A file at the unit's root that the layout does not admit fails

- **WHEN** a tracked file sits directly in `docs/units/<unit>/` and its name is not one of the four single-file kinds
- **THEN** the verification fails, naming the unit, the path and that the layout does not admit it

#### Scenario: A path the two levels do not admit fails

- **WHEN** a tracked path below `docs/units/<unit>/` is neither one of the four first-level filenames nor
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

## MODIFIED Requirements

### Requirement: Every review round recorded after the boundary keeps its finding instances

Each unit's finding instances SHALL be stored at `docs/units/<unit>/findings/round-<N>.md`, one file per recorded round,
and the build SHALL verify that every round recorded after the store's boundary has one. The store exists beside the
unit's permanent hand-over rather than inside it, because the hand-over is written before a round runs while the round's
findings arrive after it, and because the build cross-checks each round's classes against its file. A consultation, or
any other pass that is not a review round, SHALL record its findings in the unit's `review-handover.md`, and SHALL NOT
record them in a round file. The kinds a unit's directory holds are the layout requirement's subject; the store admits a
round's instances file and nothing else, which follows from that requirement rather than restating it. A file SHALL
carry one entry per finding of that round — its class, its location (`file:line`), and its claim — or state that the
round raised none, so a clean round is recorded as clean rather than left absent. An entry MAY be wrapped across source
lines, because the file sits under `docs/` and the formatter reflows it; its fields SHALL be read from the joined text.
A round dated on or before the boundary SHALL be exempt, because it was recorded before the store existed, and the
exemption SHALL be stated as that date rather than inferred from a missing file.

The verification SHALL be a `commit-hygiene.py` mode run by a `check` task over the tracked records, and it SHALL fail
when a round has no instances file, when a file names a round its record does not carry, when an entry lacks one of its
three fields, when the file states no entry and no `- none`, when it states `- none` beside entries, when the classes
the entries name differ from the round's `classes raised` field — so a file stating none beside a round that raised
classes fails instead of misleading a reader. A file the store does not hold is the layout requirement's subject, and
that requirement's mode names it. The pre-commit guard SHALL refuse a commit that changes either a record or its
instances files and leaves an affected post-boundary round without its instances: it SHALL read the index's contents for
the affected unit rather than the working tree's copy, so a file an earlier commit recorded is seen and a staged
deletion is judged.

#### Scenario: The standard check verifies the rounds' instances

- **WHEN** the standard `check` task runs
- **THEN** it includes the finding-instances verification

#### Scenario: A post-boundary round without its instances fails

- **WHEN** a record carries a round dated after the boundary and no `findings/round-<N>.md` exists for it
- **THEN** the verification fails, naming the unit and the round

#### Scenario: A clean round carries an explicit statement

- **WHEN** a round raised no findings
- **THEN** its instances file states that it raised none, and the verification passes for that round

#### Scenario: A file for a round the record does not carry fails

- **WHEN** `docs/units/<unit>/findings/round-<N>.md` exists and the unit's record carries no round `<N>`
- **THEN** the verification fails, naming the file and the round

#### Scenario: An entry missing one of its fields fails

- **WHEN** an instance entry carries no class, no location, or no claim
- **THEN** the verification fails, naming the file and the entry

#### Scenario: A reflowed entry is read from its joined text

- **WHEN** the formatter wraps an entry across source lines
- **THEN** its class, location, and claim are read from the joined text, and the round passes as it would unwrapped

#### Scenario: An empty instances file fails

- **WHEN** a post-boundary round's instances file carries no entry and no `- none`
- **THEN** the verification fails, naming the file, since a clean round must state that it raised none

#### Scenario: A file stating none beside an entry fails

- **WHEN** an instances file states `- none` and also carries an entry
- **THEN** the verification fails, naming the file, since the two statements contradict each other

#### Scenario: A round before the boundary is exempt

- **WHEN** a round is dated on or before the store's boundary
- **THEN** the verification requires no instances file for it, because it was recorded before the store existed

#### Scenario: A file disagreeing with the record's classes fails

- **WHEN** a round's `classes raised` field names one or more classes and its instances file states that the round
  raised none, or names a different set of classes
- **THEN** the verification fails, naming the file, the round, and both class sets

#### Scenario: A staged instances deletion is refused

- **WHEN** a commit stages the deletion of a post-boundary round's instances file and leaves the record unchanged
- **THEN** the guard refuses the commit, naming the unit and the round

#### Scenario: The guard refuses a staged round without its instances

- **WHEN** a commit changes a record or its instances files and the index then holds a post-boundary round without its
  instances file
- **THEN** the guard refuses the commit, naming the unit and the round, whether the file was never staged or a staged
  deletion removed it

#### Scenario: The guard judges the staged copy

- **WHEN** the staged instances file differs from the working tree's copy
- **THEN** the guard judges the content the commit records

#### Scenario: An unreadable instances file fails rather than passing

- **WHEN** an instances file cannot be read, or its entries cannot be parsed
- **THEN** the verification fails, naming the file, rather than reporting the round clean

#### Scenario: A file the store does not hold fails

- **WHEN** `docs/units/<unit>/findings/` holds a tracked file whose name is not `round-<N>.md`
- **THEN** it fails the layout requirement's verification, which names the file, since that requirement admits only a
  round's instances file there

#### Scenario: A consultation records its findings in the hand-over

- **WHEN** a pass that is not a review round raises findings for a unit
- **THEN** they are recorded in that unit's `review-handover.md`, and no file under its `findings/` directory carries
  them
