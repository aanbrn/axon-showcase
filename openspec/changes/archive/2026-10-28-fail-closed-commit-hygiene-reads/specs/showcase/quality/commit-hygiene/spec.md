## ADDED Requirements

### Requirement: A failed read is never reported as a clean result

Every `commit-hygiene.py` mode SHALL report a read it needs that fails as that failure, naming it, and exit non-zero,
rather than reading the failure as an empty offender list — a git invocation, and a tracked file the size check cannot
measure. The exits that are themselves answers — `git grep`'s 1, the revision questions' 1, and the branch read's 1 for
a detached HEAD — SHALL stay answers. The manual-pass readiness mode's requirement states the same rule for its
boundary.

#### Scenario: The staged set cannot be read

- **WHEN** a `--staged` check runs against a repository whose index cannot be read
- **THEN** it exits non-zero naming the failing command, rather than reporting a clean staged set

#### Scenario: The tracked set cannot be read

- **WHEN** a tracked-set check (`--tracked-ignored`, `--executable-bits`, or `--large-files`) runs against a repository
  whose index cannot be read
- **THEN** it exits non-zero naming the failing command, rather than reporting a clean tracked set

#### Scenario: The change assignment cannot be resolved

- **WHEN** the `--staged` guard runs where git cannot report the worktree's claim path
- **THEN** the commit is refused with the failing command named, rather than the assignment check passing

#### Scenario: A grep with no match stays an answer

- **WHEN** `--conflict-markers` runs over a repository carrying no conflict marker
- **THEN** the check passes, since `git grep`'s exit 1 reports no match rather than a failure

#### Scenario: A detached HEAD stays an answer

- **WHEN** the guard runs in a repository whose HEAD is detached
- **THEN** the branch names no change and the claim alone assigns the commit, rather than the guard reporting a failed
  read

#### Scenario: An unborn HEAD names its branch

- **WHEN** the guard runs in a repository whose HEAD has no commit yet
- **THEN** the branch is read by its name rather than raising, and the assignment then follows the claim or that name

#### Scenario: An unreadable HEAD fails the check

- **WHEN** the branch cannot be read at all
- **THEN** the check exits non-zero naming the failing command, rather than reporting no branch

#### Scenario: A tracked file absent from the worktree is measured from the index

- **WHEN** the size check meets a tracked path that is deleted from the worktree but present in the index
- **THEN** it measures the index copy rather than skipping the path

#### Scenario: A tracked file that cannot be measured fails the check

- **WHEN** a tracked path's size cannot be read
- **THEN** the mode exits non-zero naming that path, rather than printing its checked-count line without it
