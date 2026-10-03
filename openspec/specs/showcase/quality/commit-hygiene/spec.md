# showcase/quality/commit-hygiene Specification

## Purpose

Defines the mechanical guards over the commit/artifact hygiene the standard quality gates cannot see — a local
pre-commit hook that inspects the staged set, and build checks that verify `captured:` marker placement, that the
tracked set excludes ignored files, that no tracked file carries a merge conflict marker, that tracked scripts carry the
executable bit, that no tracked file exceeds the configured size limit, and that no two workflow cron schedules collide
— so slips that were caught only by a human or a review pass are blocked by construction.

## Requirements

### Requirement: A pre-commit guard blocks a mechanically defective staged set

The repository SHALL provide a tracked git `pre-commit` hook that inspects the staged set before a commit is created and
refuses the commit when it detects a mechanically defective state: the project's formatter check fails; a staged path is
a generated artifact that must not be committed; a path is staged and then modified again so the index no longer matches
the working tree; a staged file carries a merge conflict marker; or a `captured:` marker is misplaced in a staged
`AGENTS.md`. The hook SHALL name each offending path and the reason and SHALL exit non-zero. The hook's path parsing
SHALL be quote-safe: every path it reads from git SHALL be the real path rather than git's C-quoted rendering, so a path
containing non-ASCII bytes is inspected exactly like any other and no check is silently skipped for it.

#### Scenario: A failing formatter check refuses the commit

- **WHEN** the project's formatter check fails while a formatter-owned file is staged
- **THEN** the guard refuses the commit and reports the formatter check's failure

#### Scenario: A force-staged generated artifact refuses the commit

- **WHEN** a generated artifact that the repository excludes (for example a Python bytecode file or a build output) is
  staged
- **THEN** the guard refuses the commit and names the offending path

#### Scenario: A path staged and then edited refuses the commit

- **WHEN** a path is staged and then modified again so the index and the working tree differ for that path
- **THEN** the guard refuses the commit and names the path

#### Scenario: A clean staged rename is not reported

- **WHEN** a path is renamed and staged with no further modification (`git mv`, nothing edited afterwards)
- **THEN** the guard does not report the rename as a path staged and then edited, and names no spurious path from the
  rename's old name

#### Scenario: A staged conflict marker refuses the commit

- **WHEN** a staged file carries a merge conflict branch marker
- **THEN** the guard refuses the commit and names the offending file and line

#### Scenario: A misplaced captured marker refuses the commit

- **WHEN** a staged `AGENTS.md` carries a `captured:` marker that is not inside a rule block (for example one sitting on
  a plain `- Text` bullet)
- **THEN** the guard refuses the commit and names the offending marker's location

#### Scenario: A clean staged set proceeds

- **WHEN** the staged set carries none of the detected defects
- **THEN** the guard passes and the commit proceeds

#### Scenario: A non-ASCII path is inspected rather than skipped

- **WHEN** a staged path contains non-ASCII bytes
- **THEN** the guard reads the real path rather than git's quoted rendering, so every check treats that path exactly as
  it would an ASCII path instead of skipping it

#### Scenario: A non-ASCII formatter-owned file reaches the formatter check

- **WHEN** a staged formatter-owned file has a non-ASCII name (e.g. `café.md`)
- **THEN** it is selected for the formatter check, so its formatting is verified rather than silently passed

#### Scenario: A non-ASCII generated artifact is refused

- **WHEN** a staged path that the repository excludes (a generated artifact) has a non-ASCII name
- **THEN** the guard reports it as a force-staged artifact and refuses the commit, so the escape check completes for a
  non-ASCII path as it would for an ASCII one

#### Scenario: A non-ASCII path staged and then edited is refused

- **WHEN** a non-ASCII path is staged and then modified again so the index and the working tree differ for it
- **THEN** the guard names that path and refuses the commit

### Requirement: The guard is activated per clone and does not alter state

The repository SHALL document how a clone activates the guard by pointing `core.hooksPath` at the tracked hooks
directory, and SHALL provide an install step that applies it. The guard SHALL be bypassable with
`git commit --no-verify` for a deliberate exception and SHALL NOT modify the working tree or the index.

#### Scenario: A clone activates the guard

- **WHEN** a contributor runs the documented install step
- **THEN** `core.hooksPath` points at the tracked hooks directory and the guard runs on subsequent commits

#### Scenario: A deliberate exception can bypass the guard

- **WHEN** a contributor commits with `--no-verify`
- **THEN** the guard does not run and the commit proceeds

#### Scenario: The guard does not change repository state

- **WHEN** the guard runs
- **THEN** the working tree and the index are unchanged by it

### Requirement: Captured marker placement is verified by the build

The standard `check` task SHALL verify that each `captured:` provenance marker in `AGENTS.md` lies within a rule block —
a bold-lead bullet or a bold-lead paragraph — and never on a plain `- Text` bullet, so a misplaced marker fails the CI
`build` gate even when no local hook is installed or the hook was bypassed. A backticked mention of the token in prose
(`` `captured:` ``) is not a marker and SHALL NOT be treated as one. The marker's existence rule (each captured rule
carries a greppable `captured: <unit>` origin) is specified in `showcase/quality/agent-skills`.

#### Scenario: The standard check verifies marker placement

- **WHEN** the standard `check` task runs
- **THEN** it includes the `captured:` marker-placement verification for `AGENTS.md`

#### Scenario: A marker outside a rule block fails the build

- **WHEN** a `captured:` marker in `AGENTS.md` sits on a plain `- Text` bullet rather than inside a rule block
- **THEN** the marker-placement verification fails and reports the offending location

#### Scenario: A backticked prose mention is not a marker

- **WHEN** `AGENTS.md` mentions the token in prose as a backticked `` `captured:` ``
- **THEN** the marker-placement verification does not treat that mention as a marker

#### Scenario: Correctly placed markers pass

- **WHEN** every `captured:` marker in `AGENTS.md` lies within a rule block
- **THEN** the marker-placement verification passes

### Requirement: The guard's checks are covered by tests run in the build

Each defect class the guard detects SHALL be covered by an automated test that exercises the checker, and the tests
SHALL run in the standard `check` task, so a regression that disables or weakens a check fails the build.

#### Scenario: The checker's tests run in the standard check

- **WHEN** the standard `check` task runs
- **THEN** it runs the checker's automated tests

#### Scenario: A disabled check fails a test

- **WHEN** a check is disabled or its logic regresses
- **THEN** at least one test that exercises that defect class fails

### Requirement: Tracked-file hygiene is verified by the build

The standard `check` task SHALL verify that no tracked file is excluded by the repository's ignore rules — the CI-gated
counterpart of the pre-commit guard's force-staged-artifact check — so a generated artifact force-added at any point
fails the CI `build` gate even when no local hook is installed. The verification SHALL name each offending path.

#### Scenario: The standard check verifies tracked-file hygiene

- **WHEN** the standard `check` task runs
- **THEN** it includes the tracked-file hygiene verification

#### Scenario: A tracked ignored file fails the build

- **WHEN** a file matching the repository's ignore rules is tracked (for example a bytecode or build-output file
  force-added to the index)
- **THEN** the tracked-file hygiene verification fails and names the offending path

#### Scenario: A clean repository passes

- **WHEN** no tracked file matches the repository's ignore rules
- **THEN** the tracked-file hygiene verification passes

### Requirement: The tracked set carries no merge conflict marker

The standard `check` task SHALL verify that no tracked file carries a merge conflict marker, so a conflicted markdown or
docs file — whose branch marker the formatter rewrites into a blockquote rather than flagging — fails the CI `build`
gate. The verification SHALL name each offending file and line.

#### Scenario: The standard check verifies the tracked set

- **WHEN** the standard `check` task runs
- **THEN** it includes the conflict-marker verification

#### Scenario: A tracked conflict marker fails the build

- **WHEN** a tracked file carries a merge conflict branch marker
- **THEN** the conflict-marker verification fails and names the offending file and line

#### Scenario: A clean repository passes

- **WHEN** no tracked file carries a merge conflict branch marker
- **THEN** the conflict-marker verification passes

### Requirement: The tracked hook, shell scripts, and Gradle wrapper carry the executable bit

The standard `check` task SHALL verify that every tracked file git runs directly is mode `100755`: the `pre-commit` hook
(which git silently skips when non-executable), the shell scripts (`*.sh`, excluding the vendored `axon4to5-*` and
generated `openspec-*` skill subtrees), and the Gradle wrapper. A lost executable bit SHALL fail the CI `build` gate,
naming each offending path.

#### Scenario: The standard check verifies the executable bits

- **WHEN** the standard `check` task runs
- **THEN** it includes the executable-bit verification

#### Scenario: A non-executable hook fails the build

- **WHEN** a tracked file under `scripts/git-hooks/` is not mode `100755`
- **THEN** the executable-bit verification fails and names the offending path

#### Scenario: A non-executable shell script fails the build

- **WHEN** a tracked shell script (`*.sh`, outside the vendored `axon4to5-*` and generated `openspec-*` skill subtrees)
  is not mode `100755`
- **THEN** the executable-bit verification fails and names the offending path

#### Scenario: A non-executable Gradle wrapper fails the build

- **WHEN** the tracked `gradlew` is not mode `100755`
- **THEN** the executable-bit verification fails and names the offending path

#### Scenario: A clean repository passes

- **WHEN** every tracked hook, shell script, and the Gradle wrapper is mode `100755`
- **THEN** the executable-bit verification passes

### Requirement: Workflow cron schedules do not collide

The standard `check` task SHALL verify that no two `cron` schedules among the workflow files under `.github/workflows/`
collide — two jobs firing in the same minute undo a deliberate stagger — so a collision fails the CI `build` gate,
whether the two schedules live in different workflow files or in the same one. A pair of schedules collide, as the
verification determines it, when their minute and hour fields are equal and their day-of-week fields overlap (equal, or
either field is `*`, since a `*` day-of-week runs daily and so runs on the other schedule's day); two schedules naming
different days of the week do not collide, because they never run on the same day. A day-of-week field of `7` is treated
as `0`, since both name Sunday. A schedule that restricts day-of-month or month is compared with another schedule only
by exact cron-string equality, because the verification does not compute whether two such day constraints intersect. The
verification SHALL name each colliding pair of schedules, the workflow file or files carrying them, and the shared cron,
and SHALL NOT treat a commented-out `cron` as a schedule.

#### Scenario: The standard check verifies the workflow schedules

- **WHEN** the standard `check` task runs
- **THEN** it includes the workflow-schedule verification

#### Scenario: Two workflows sharing a cron string fail the build

- **WHEN** two workflow files carry the same `cron` string
- **THEN** the workflow-schedule verification fails and names both files and the shared cron

#### Scenario: Two schedules in one workflow file sharing a cron string fail the build

- **WHEN** one workflow file carries two `cron` schedules with the same `cron` string
- **THEN** the workflow-schedule verification fails and names the file and the shared cron

#### Scenario: A daily and a weekly workflow sharing a minute fail the build

- **WHEN** a workflow whose day-of-week field is `*` and a workflow naming a weekday carry the same minute and hour
- **THEN** the workflow-schedule verification fails and names both files and the shared minute

#### Scenario: Weekly workflows on different days sharing a minute pass

- **WHEN** two workflows naming different days of the week carry the same minute and hour
- **THEN** the workflow-schedule verification passes for that pair, because the two schedules never run on the same day

#### Scenario: A restricted day constraint is matched by exact cron string

- **WHEN** two workflow files carry different `cron` schedules that both restrict day-of-month or month and share the
  same minute and hour
- **THEN** the workflow-schedule verification does not report them as colliding unless their cron strings are identical

#### Scenario: A commented-out cron is not a schedule

- **WHEN** a workflow file contains a commented-out `cron` line
- **THEN** the workflow-schedule verification does not treat that line as a schedule

#### Scenario: A clean set of workflow schedules passes

- **WHEN** no two workflow schedules collide under the verification
- **THEN** the workflow-schedule verification passes

### Requirement: No tracked file exceeds the configured size limit

The standard `check` task SHALL verify that no tracked file exceeds a maximum size declared in
`config/commit-hygiene/large-files.properties`, so a large blob — a binary, a vendored archive, or an accidentally
committed build output — fails the CI `build` gate before it enters history. The verification SHALL name each oversized
file with its size and the configured limit.

#### Scenario: The standard check verifies file sizes

- **WHEN** the standard `check` task runs
- **THEN** it includes the tracked-file size verification

#### Scenario: A tracked file over the limit fails the build

- **WHEN** a tracked file's size exceeds the configured maximum
- **THEN** the size verification fails and names the file, its size, and the limit

#### Scenario: Files at or under the limit pass

- **WHEN** every tracked file is at or under the configured maximum
- **THEN** the size verification passes
