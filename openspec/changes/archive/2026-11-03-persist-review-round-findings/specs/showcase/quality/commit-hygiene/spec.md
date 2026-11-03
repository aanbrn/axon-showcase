## ADDED Requirements

### Requirement: Every review round recorded after the boundary keeps its finding instances

Each unit's finding instances SHALL be stored at `docs/units/<unit>/findings/round-<N>.md`, one file per recorded round,
and the build SHALL verify that every round recorded after the store's boundary has one. A file SHALL carry one entry
per finding of that round — its class, its location (`file:line`), and its claim — or state that the round raised none,
so a clean round is recorded as clean rather than left absent. An entry MAY be wrapped across source lines, because the
file sits under `docs/` and the formatter reflows it; its fields SHALL be read from the joined text. A round dated on or
before the boundary SHALL be exempt, because it was recorded before the store existed, and the exemption SHALL be stated
as that date rather than inferred from a missing file.

The verification SHALL be a `commit-hygiene.py` mode run by a `check` task over the tracked records, and it SHALL fail
when a round has no instances file, when a file names a round its record does not carry, when an entry lacks one of its
three fields, when the file states no entry and no `- none`, when it states `- none` beside entries, and when the
classes the entries name differ from the round's `classes raised` field — so a file stating none beside a round that
raised classes fails instead of misleading a reader. The pre-commit guard SHALL refuse a commit that changes either a
record or its instances files and leaves an affected post-boundary round without its instances: it SHALL read the
index's contents for the affected unit rather than the working tree's copy, so a file an earlier commit recorded is seen
and a staged deletion is judged.

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
