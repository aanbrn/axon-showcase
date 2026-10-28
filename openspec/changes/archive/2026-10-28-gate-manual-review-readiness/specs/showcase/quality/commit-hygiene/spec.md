## ADDED Requirements

### Requirement: A unit's readiness for the manual review pass is verified at the boundary

`scripts/commit-hygiene.py --review-ready` SHALL verify, at the review gate's manual-pass boundary, that the unit's
uncommitted tree is ready for the owner's pass: no non-ignored untracked path, no staged path whose index copy is stale,
and a base revision (`origin/main` by default) whose history `HEAD` contains. It SHALL report every failure it finds,
and a git invocation it needs that fails SHALL fail the check as not established rather than as a clean result.

#### Scenario: A ready tree passes and reports the revisions it checked

- **WHEN** the unit's index is current, it holds no untracked path the repository does not ignore, and `HEAD` contains
  the base
- **THEN** the mode exits zero and prints the base and `HEAD` revisions it compared

#### Scenario: A unit with its own commits on a current base passes

- **WHEN** the unit's work is committed and the base is an ancestor of `HEAD`
- **THEN** the mode exits zero, since containing the base is the condition and not equal revisions

#### Scenario: An unstaged new file fails the check

- **WHEN** the unit's new file is untracked and the repository does not ignore it
- **THEN** the mode exits non-zero naming that path
- **AND** the path is still untracked afterward, since the checker reports rather than stages

#### Scenario: An ignored untracked path is not reported

- **WHEN** an untracked path matches the repository's ignore rules
- **THEN** the mode does not report it

#### Scenario: A path staged and then edited fails the check

- **WHEN** a path is staged and then edited again, so the index holds an older copy
- **THEN** the mode exits non-zero naming that path

#### Scenario: A base that advanced fails the check

- **WHEN** the base carries commits `HEAD` does not
- **THEN** the mode exits non-zero naming the base's commits and the paths they changed

#### Scenario: A base that dropped a line the tree still carries fails the check

- **WHEN** the base's advance removed a line the working tree still has, so the tree changes nothing the base gained
- **THEN** the mode exits non-zero, since the base's history is not contained in `HEAD`

#### Scenario: A refreshed tree passes

- **WHEN** the unit has been fast-forwarded or rebased onto the base
- **THEN** the mode exits zero

#### Scenario: An unresolvable base revision fails cleanly

- **WHEN** the base revision the caller names does not resolve in the repository
- **THEN** the mode exits non-zero with a message naming that revision, rather than passing or raising a traceback

#### Scenario: A base with no shared history fails

- **WHEN** the base revision and `HEAD` are unrelated roots, so neither contains the other
- **THEN** the mode exits non-zero stating that the unit cannot be shown to sit on that base

#### Scenario: A base revision given without the mode fails rather than being ignored

- **WHEN** the checker is run with `--base` and no `--review-ready`
- **THEN** it exits non-zero stating that the revision needs the mode, rather than ignoring the revision

#### Scenario: A failing git invocation fails as not established

- **WHEN** a git invocation the check needs fails, such as a status read against an unreadable index
- **THEN** the mode exits non-zero reporting that readiness is not established, with the failing command, rather than
  reporting the tree ready on an empty result
