## ADDED Requirements

### Requirement: A change unit records its workspace

The standard `check` task SHALL verify that every live change directory's `tasks.md` carries its `Workspace:` record,
and SHALL judge an archived change directory by the same rule from the gate's introduction date, apart from one it
acknowledges by name because it landed before the gate. The pre-commit guard SHALL refuse a staged `tasks.md` whose
record fails the same judgment, apart from one a merge brings unchanged. The `writer` is shape-checked: a reviewer's
identifier has the same form.

#### Scenario: A well-formed record passes

- **WHEN** a record carries a `unit` naming its directory, an absolute `path`, a `branch`, and a `writer` shaped `ses_`
  plus alphanumerics
- **THEN** the verification passes

#### Scenario: The standard check verifies the live change records

- **WHEN** the standard `check` task runs
- **THEN** it includes the workspace-record verification

#### Scenario: A change directory without a record fails the build

- **WHEN** a live `openspec/changes/<name>/tasks.md` carries no `Workspace:` record
- **THEN** the verification fails and names the file

#### Scenario: A record naming another unit fails the build

- **WHEN** a live change directory's record names a unit other than its own directory name
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

#### Scenario: A tree with no live or post-introduction archived change passes

- **WHEN** `openspec/changes/` carries no live change directory and no archive dated on or after the gate's introduction
  date
- **THEN** the verification passes, because neither is the record's subject

#### Scenario: A change directory carrying no tasks.md is not judged

- **WHEN** a live change directory carries no `tasks.md`
- **THEN** the verification does not judge that directory, and still judges an archive dated on or after the gate's
  introduction date

#### Scenario: An archived change is judged from the gate's introduction date

- **WHEN** an archived change directory dated on or after the gate's introduction date carries no record, or one that
  fails the judgment
- **THEN** the verification fails and names the file, while an archive dated before that date passes as recorded

#### Scenario: An acknowledged pre-introduction archive is exempt

- **WHEN** an archive dated at or after the gate's introduction date landed before the gate, and the verification
  acknowledges that directory by name
- **THEN** it passes without a record, while a second directory sharing its date and carrying none fails

#### Scenario: The guard refuses a staged record that fails the judgment

- **WHEN** a `tasks.md` within the verification's scope is staged — a live one, or an archive dated on or after the
  gate's introduction date that the verification has not acknowledged — and its record is absent or malformed
- **THEN** the pre-commit guard refuses the commit, naming the file and the defect, while an archive outside that scope
  is not judged

#### Scenario: A merged record is exempt

- **WHEN** a `tasks.md` a merge brings unchanged is staged, so its record names the tree of the unit that recorded it
- **THEN** the guard does not judge it, because merged content is not the committing unit's own

#### Scenario: A detached head skips the branch comparison

- **WHEN** a `tasks.md` is staged while `HEAD` is detached, so no branch is being committed
- **THEN** the guard still judges the record's shape and its `path`, and does not fail on the branch it cannot read

#### Scenario: The guard requires the record to name the committing tree

- **WHEN** a staged `tasks.md` records a `path` other than the worktree's top level, or a `branch` other than the branch
  being committed
- **THEN** the pre-commit guard refuses the commit, naming the recorded and the actual value
