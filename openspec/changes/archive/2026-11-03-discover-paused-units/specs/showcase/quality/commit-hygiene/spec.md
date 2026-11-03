## REMOVED Requirements

### Requirement: A change unit records its workspace

**Reason**: A unit's `Workspace:` record now lives in the unit's own record at `docs/units/<unit>/unit.md`, for every
kind of unit, and a change unit's `tasks.md` references it (ADR-0029). The requirement's subject widens from a change
unit to any unit, and the file carrying the record moves.

**Migration**: The behavior carries into "A unit records its workspace in its unit record": the verification, its
archived-change boundary and acknowledgements, the pre-commit guard's judgments, and the recorded forms are retained,
and the record's location and the `tasks.md` reference replace the fields inside `tasks.md`. An archive dated before the
new record boundary keeps the old contract, so its own `tasks.md` still carries the record; a unit recorded before the
change keeps that record as legacy until its own writer moves it.

## ADDED Requirements

### Requirement: A unit records its workspace in its unit record

A unit's `Workspace:` record SHALL live in `docs/units/<unit>/unit.md`, and a change unit's `tasks.md` SHALL reference
that record rather than carry its fields.

The record boundary is this change's effective date.

The standard `check` task SHALL verify every live unit's record and every live change directory's reference to it.

It SHALL enumerate the assignment and assert that the record and the reference exist, so a deleted record fails rather
than passing an empty enumeration.

An archived change directory dated before the gate's introduction SHALL NOT be judged.

One dated on or after the gate's introduction and before the record boundary SHALL be judged by the record inside its
own `tasks.md`.

One dated on or after the record boundary SHALL be judged by its reference to the unit record.

The verification SHALL pass an archive it acknowledges by name, because it landed before the contract it is judged by.

The pre-commit guard SHALL refuse a staged record, and a staged reference, that fails the same judgment, apart from one
a merge brings unchanged.

The `writer` SHALL be shape-checked: a reviewer's identifier has the same form.

#### Scenario: A well-formed record passes

- **WHEN** a record carries a `unit` naming its directory, an absolute `path`, a `branch`, and a `writer` shaped `ses_`
  plus alphanumerics
- **THEN** the verification passes

#### Scenario: The standard check verifies the unit records

- **WHEN** the standard `check` task runs
- **THEN** it includes the workspace-record verification

#### Scenario: A unit without a record fails the build

- **WHEN** a live unit's `docs/units/<unit>/unit.md` is absent
- **THEN** the verification fails and names the unit

#### Scenario: A change directory without a reference fails the build

- **WHEN** a live change directory's `tasks.md` carries no reference to its unit record
- **THEN** the verification fails and names the file

#### Scenario: A reference to a missing or differently named unit fails the build

- **WHEN** a `tasks.md` references a record that does not exist, or one naming another unit
- **THEN** the verification fails, naming the file, the referenced unit, and the directory's name

#### Scenario: A change directory carrying no tasks.md is not judged for a reference

- **WHEN** a live change directory carries no `tasks.md`
- **THEN** the verification does not judge that directory for a reference, and still judges its archived counterpart
  under the boundary that applies to it

#### Scenario: A record naming another unit fails the build

- **WHEN** a live unit's record names a unit other than its directory's name
- **THEN** the verification fails, naming the file, the recorded unit, and the directory's name

#### Scenario: A malformed field fails the build

- **WHEN** a record's `path` is relative, its `branch` is empty, its `writer` does not match the shape, or it carries a
  field the format does not name
- **THEN** the verification fails and names the field

#### Scenario: Each recorded form parses

- **WHEN** a record is written as one logical line wrapped across source lines, as `Workspace:` followed by a backticked
  value line, or inside one backticked line that begins with the marker, and its writer value may end with a sentence
  period
- **THEN** the verification reads the same four fields from each form and passes

#### Scenario: A tree with no unit passes

- **WHEN** `docs/units/` carries no record, no live change directory exists, and no archive dated on or after the gate's
  introduction date does
- **THEN** the verification passes, because none is the record's subject

#### Scenario: A unit without a change directory is judged

- **WHEN** a live unit has no change directory and its record exists
- **THEN** the verification judges the record and requires no `tasks.md` reference

#### Scenario: An archive before the record boundary keeps the inline record

- **WHEN** an archived change directory is dated on or after the gate's introduction date and before the record boundary
- **THEN** the verification requires the record inside its own `tasks.md`, and does not require a reference

#### Scenario: An archive on or after the record boundary is judged by its reference

- **WHEN** an archived change directory is dated on or after the record boundary and carries no reference, or one that
  fails the judgment
- **THEN** the verification fails and names the file

#### Scenario: An acknowledged pre-boundary archive is exempt

- **WHEN** an archive dated on or after the gate's introduction date and before the record boundary landed before the
  gate, and the verification acknowledges that directory by name
- **THEN** it passes without a record, while a second directory sharing its date and carrying none fails

#### Scenario: A boundary-dated archive created before the change is acknowledged

- **WHEN** an archive is dated on or after the record boundary, landed before this change, and the verification
  acknowledges it by name
- **THEN** it passes without a reference, while the record inside its own `tasks.md` is still required

#### Scenario: A deleted record fails rather than passing

- **WHEN** a unit's work is staged and its record has been deleted, so the files the verification finds are a strict
  subset of the assignments it enumerates
- **THEN** the verification fails, because it asserts the assignment rather than only the records it finds

#### Scenario: The guard refuses a staged record that fails the judgment

- **WHEN** a record or a reference within the verification's scope is staged, and its record or reference is absent or
  malformed
- **THEN** the pre-commit guard refuses the commit, naming the file and the defect, while an archive outside that scope
  is not judged

#### Scenario: A merged record is exempt

- **WHEN** a record a merge brings unchanged is staged, so it names the tree of the unit that recorded it
- **THEN** the guard does not judge it, because merged content is not the committing unit's own

#### Scenario: A detached head skips the branch comparison

- **WHEN** a record is staged while `HEAD` is detached, so no branch is being committed
- **THEN** the guard still judges the record's shape and its `path`, and does not fail on the branch it cannot read

#### Scenario: The guard requires the record to name the committing tree

- **WHEN** a staged record's `path` is other than the worktree's top level, or its `branch` is other than the branch
  being committed
- **THEN** the pre-commit guard refuses the commit, naming the recorded and the actual value
