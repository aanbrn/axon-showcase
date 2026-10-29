# showcase/quality/commit-hygiene Specification

## Purpose

Defines the mechanical guards over the commit/artifact hygiene the standard quality gates cannot see — a local
pre-commit hook that inspects the staged set and validates a staged review-round ledger's duplicate and contiguity
checks, its reviewer-session continuity, and the preservation of every recorded `(unit, round)` row — build checks that
verify `captured:` marker placement, that the tracked set excludes ignored files, that no tracked file carries a merge
conflict marker, that tracked scripts carry the executable bit, that no tracked file exceeds the configured size limit,
that no two workflow cron schedules collide, that the review-round ledger carries at most one row per unit and round,
that each unit's rounds are contiguous from `1` apart from an explicitly acknowledged gap, that a round the caller names
with `--expect` is recorded, and that each round's reviewer session and its continuity are recorded — and, asserted at
the review gate's manual-pass boundary rather than by a build check, that a unit the caller names with `--expect-clean`
closes on a round carrying no finding class, since a round in flight is unclean by design, and that the unit's own tree
is ready for the owner's pass: no unstaged new file, no stale indexed copy, and a base its `HEAD` contains, failing as
not established when a read it needs fails rather than reporting the tree clean — and a committed `.gitattributes` that
normalizes line endings (enforced by git, not the build) — so slips that were caught only by a human or a review pass
are blocked by construction.

## Requirements

### Requirement: A pre-commit guard blocks a mechanically defective staged set

The repository SHALL provide a tracked git `pre-commit` hook that inspects the staged set before a commit is created and
refuses the commit when it detects a mechanically defective state: the project's formatter check fails; a staged path is
a generated artifact that must not be committed; a path is staged and then modified again so the index no longer matches
the working tree; a staged file carries a merge conflict marker; a `captured:` marker is misplaced in a staged guidance
file (`AGENTS.md`, or a project-authored `.opencode/agent/*.md` or `.opencode/skills/*/SKILL.md`); or a change dir under
`openspec/changes/archive/**` is staged while the spec sync `openspec archive` wrote under `openspec/specs/**` is left
unstaged or untracked. The hook SHALL name each offending path and the reason and SHALL exit non-zero. The hook's path
parsing SHALL be quote-safe: every path it reads from git SHALL be the real path rather than git's C-quoted rendering,
so a path containing non-ASCII bytes is inspected exactly like any other and no check is silently skipped for it.

It SHALL also refuse a staged review-round ledger (`docs/review-rounds.tsv`) whose duplicate or non-contiguous rows the
ledger's own verification reports. It SHALL refuse a staged ledger whose session-continuity check reports a
contradiction. It SHALL refuse a staged ledger diff that removes a data row unless that `(unit, round)` remains in the
staged ledger, and a staged ledger whose file is deleted or renamed away has no row remaining. It SHALL judge the
ledger's staged content, not the working tree's copy. It SHALL report a staged ledger row whose round column cannot be
parsed as a named failure rather than raising.

It SHALL also refuse a staged change directory under `openspec/changes/archive/**` whose `tasks.md` carries an unticked
task, naming the file and the task line: the archive commit carries the file, and `openspec archive --yes` reports an
incomplete task as a warning rather than failing. The refusal SHALL select a path the staged set carries under
`openspec/changes/archive/**` whose basename is `tasks.md`, and an archived `tasks.md` absent from the staged set SHALL
NOT be judged. For each selected path it SHALL judge that file's entire content as the index holds it, and SHALL NOT
substitute the working tree's copy. A task is a list item at any indentation whose bracket holds at most one character
and is not followed by `(`, which is a Markdown link's syntax. A bracket whose content is empty or whitespace SHALL be
unticked; `x` or `X` SHALL be ticked; any other content SHALL be refused as an unsupported marker rather than counted as
ticked. The file's historical content SHALL refuse the commit too, without implying that its tasks are to be marked
completed. A selected path SHALL be exempt when the commit's merge brings it unchanged — the staged blob equals that of
the same path in a merged revision — since merged content is not the commit's own.

The hook SHALL also refuse a staged set that carries another change's directory. Before it reads the staged set it SHALL
establish the commit's **assigned change**: the branch's name when that name is a change name, and otherwise the single
line of the claim at the worktree's git path `axon-showcase-change`, which holds a change name or the literal `none`. A
branch name SHALL be resolved by identity: it is a change name when `openspec/changes/<name>/` exists, or when an
archived `openspec/changes/archive/<YYYY-MM-DD>-<name>/` carries that same name after exactly one leading date segment
is stripped. A missing source — no claim, and a branch whose name is not a change name — and a claim and a branch that
name two different changes SHALL each fail, naming what is wrong and how the assignment is recorded, rather than passing
or choosing the single directory found; a `none` claim contradicted by a change-named branch names no change, so the
branch's assignment stands. A path under `openspec/changes/<name>/` or `openspec/changes/archive/<date>-<name>/` SHALL
pass only when `<name>` is the assigned change: an added or modified path under the assigned change's live directory
passes, and so does a deletion under it — whether it removes a file the change no longer needs or carries the change
into `openspec/changes/archive/`; and a foreign change's path, added, modified, or deleted, SHALL fail. When the
assignment is `none`, any path under `openspec/changes/` SHALL fail, apart from a path the commit's merge accounts for
as stated below. Except under a `none` assignment, a path that names no change directory — one outside
`openspec/changes/`, a file directly under it, or an `archive/` entry without the `YYYY-MM-DD-` prefix — SHALL NOT be
judged by this check.

A merge commit SHALL be judged against the revisions it merges as well as against the commit's own staged set: a staged
change path SHALL be exempt from the ownership check when the index holds it and a merged revision holds it too, or when
the index lacks it while a fork of that revision held it and the revision removed it — the path is then the merged
revision's content rather than the commit's. An added or modified foreign change path no merged revision holds, and a
deletion the merge did not make, SHALL still fail. A squash merge SHALL NOT be treated as a merge commit: it records no
merged revision, so the exemption has nothing to name.

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

- **WHEN** a staged guidance file — `AGENTS.md`, or a project-authored `.opencode/agent/*.md` or
  `.opencode/skills/*/SKILL.md` — carries a `captured:` marker that is not inside a rule block (for example one sitting
  on a plain `- Text` bullet)
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

#### Scenario: An unpaired archive move refuses the commit

- **WHEN** a change dir under `openspec/changes/archive/**` that carries delta specs is staged while the spec sync
  `openspec archive` wrote under `openspec/specs/**` is left unstaged or untracked
- **THEN** the guard refuses the commit and names the archived change and the un-staged spec paths

#### Scenario: A no-delta archive move requires no spec sync

- **WHEN** a change dir under `openspec/changes/archive/**` with no delta specs (a `skip_specs` change) is staged
- **THEN** the guard does not require a spec sync and does not report the archive move

#### Scenario: The assigned change's own directory is committed

- **WHEN** the assignment names a change and the staged set adds or modifies paths under that change's
  `openspec/changes/<name>/`
- **THEN** the guard accepts them and the commit proceeds

#### Scenario: Another change's directory is refused

- **WHEN** the assignment names one change and the staged set carries paths under a different change's
  `openspec/changes/<name>/` that the commit's merge does not account for, including when that is the only change
  directory staged
- **THEN** the guard refuses the commit and names both the staged directory and the assigned change

#### Scenario: The assigned change's archive move is accepted

- **WHEN** the assignment names a change and the staged set deletes `openspec/changes/<name>/` while adding
  `openspec/changes/archive/<date>-<name>/`
- **THEN** the guard accepts the transition, whether or not git reports it as a rename

#### Scenario: Another change's archive move is refused

- **WHEN** the staged set archives a change the assignment does not name and the commit's merge does not account for
- **THEN** the guard refuses the commit and names the archived change and the assignment

#### Scenario: An unestablished assignment stops the commit

- **WHEN** the guard finds no claim and a branch whose name is not a change name, or finds a claim and a branch naming
  different changes
- **THEN** it refuses the commit, reports which source was missing or contradictory, and states how the assignment is
  recorded

#### Scenario: A unit that declares no change directory stages none

- **WHEN** the assignment declares that the unit works without a change dir and the staged set carries a path under
  `openspec/changes/` that the commit's merge does not account for
- **THEN** the guard refuses the commit and names the path and the declaration

#### Scenario: Another change's directory deleted without archiving is refused

- **WHEN** the staged set deletes paths under another change's `openspec/changes/<name>/` that the commit's merge does
  not account for — or renames them away from it — and adds nothing under `openspec/changes/archive/`
- **THEN** the guard refuses the commit and names the deleted change and the assignment

#### Scenario: The assigned change's own deletion passes

- **WHEN** the staged set deletes a file under the assigned change's `openspec/changes/<name>/`, whether that file is
  one the change no longer needs or the change is moving into `openspec/changes/archive/`
- **THEN** the guard accepts the deletion, since the assignment check judges whose directory the path belongs to rather
  than whether the change's lifecycle keeps the artifact

#### Scenario: Non-change paths are not judged by the assignment

- **WHEN** the staged set carries paths outside `openspec/changes/`, alongside the assigned change's own directory
- **THEN** the ownership check reports nothing for them, and the guard's other checks apply as before

#### Scenario: A merge commit may carry the change directories it merges

- **WHEN** a merge commit's staged set carries another change's directory or archive move that a merged revision holds,
  or deletes such a path that a fork of a merged revision held and that revision removed
- **THEN** the guard accepts those paths and the commit proceeds, since they are the merged revision's content rather
  than the commit's own

#### Scenario: A merge commit still refuses a foreign change path it does not merge

- **WHEN** a merge is in progress and the staged set adds or modifies a path under another change's directory that no
  merged revision holds, or deletes a path no merged revision removed
- **THEN** the guard refuses the commit and names the path and the assignment, as it does for an ordinary commit

#### Scenario: A squash merge is judged as an ordinary commit

- **WHEN** another revision's content is staged with `git merge --squash`, so the staged set carries that revision's
  foreign change directory and no merged revision is recorded
- **THEN** the guard refuses the commit, since a squash merge leaves no merged revision for the exemption to match

#### Scenario: A duplicated ledger row refuses the commit

- **WHEN** `docs/review-rounds.tsv` is staged and two of its rows share a unit and round
- **THEN** the guard refuses the commit and names the unit, the round and both rows

#### Scenario: A ledger gap refuses the commit

- **WHEN** `docs/review-rounds.tsv` is staged and a unit's rows are not contiguous from 1
- **THEN** the guard refuses the commit and names the unit and the missing round

#### Scenario: A ledger row contradicting the session's continuity refuses the commit

- **WHEN** `docs/review-rounds.tsv` is staged and a row's session transition contradicts the continuity rule
- **THEN** the guard refuses the commit and names the row

#### Scenario: A removed ledger row refuses the commit

- **WHEN** `docs/review-rounds.tsv` is staged and its diff removes a data row whose unit and round are absent from the
  staged ledger, or the staged ledger is deleted or renamed away
- **THEN** the guard refuses the commit and names the removal

#### Scenario: A duplicated row's repair proceeds

- **WHEN** `docs/review-rounds.tsv` is staged and its diff removes a row whose unit and round remain in the staged
  ledger
- **THEN** the guard does not report the removal, so a duplicated row can be repaired

#### Scenario: An unparseable ledger row refuses the commit by name

- **WHEN** `docs/review-rounds.tsv` is staged and a row's round column cannot be parsed
- **THEN** the guard refuses the commit and names the file and the failure, rather than raising

#### Scenario: A clean ledger append proceeds

- **WHEN** `docs/review-rounds.tsv` is staged with an appended row that passes the checks
- **THEN** the guard reports no ledger failure

#### Scenario: A commit that does not stage the ledger runs no ledger check

- **WHEN** `docs/review-rounds.tsv` is not in the staged set, whatever its working-tree content
- **THEN** the guard reports no ledger failure

#### Scenario: A staged archived tasks file with an unticked task refuses the commit

- **WHEN** `openspec/changes/archive/<date>-<name>/tasks.md` is staged and a task line's checkbox is unticked
- **THEN** the guard refuses the commit and names the file and the task

#### Scenario: A fully ticked archived tasks file proceeds

- **WHEN** a staged archived `tasks.md` has every task line ticked
- **THEN** the guard reports no archived-task failure

#### Scenario: An archived tasks file the commit does not stage is not judged

- **WHEN** an archived `tasks.md` carries an unticked task but is not in the staged set
- **THEN** the guard reports no archived-task failure

#### Scenario: An unsupported checkbox marker refuses the commit

- **WHEN** a staged archived `tasks.md` carries a task whose checkbox content is neither empty nor whitespace nor `x`
  nor `X`, such as `~`
- **THEN** the guard refuses the commit and names the file and the task as an unsupported marker

#### Scenario: A staged archived tasks file is judged by its indexed content

- **WHEN** the staged blob of an archived `tasks.md` carries an unticked task and the working tree's copy has it ticked
- **THEN** the guard refuses the commit and names the task

#### Scenario: A link bullet is not a task

- **WHEN** a staged archived `tasks.md` carries a list item whose one-character bracket is followed by `(`, as the link
  `- [ ](url)`
- **THEN** the guard reports no archived-task failure for it

#### Scenario: A merge commit's archived tasks file is exempt

- **WHEN** a merge commit stages an archived `tasks.md` whose staged blob equals a merged revision's blob for that path,
  and it carries an unticked task
- **THEN** the guard reports no archived-task failure for it

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

The standard `check` task SHALL verify that each `captured:` provenance marker in `AGENTS.md` and in the
project-authored guidance files (`.opencode/agent/*.md` and `.opencode/skills/*/SKILL.md`) lies within a rule block — a
bold-lead bullet or a bold-lead paragraph — and never on a plain `- Text` bullet, so a misplaced marker fails the CI
`build` gate even when no local hook is installed or the hook was bypassed. A backticked mention of the token in prose
(`` `captured:` ``) is not a marker and SHALL NOT be treated as one. The marker's existence rule (each captured rule
carries a greppable `captured: <unit>` origin) is specified in `showcase/quality/agent-skills`.

#### Scenario: The standard check verifies marker placement

- **WHEN** the standard `check` task runs
- **THEN** it includes the `captured:` marker-placement verification for `AGENTS.md` and each project-authored guidance
  file

#### Scenario: A marker outside a rule block fails the build

- **WHEN** a `captured:` marker in `AGENTS.md` or a project-authored guidance file sits on a plain `- Text` bullet
  rather than inside a rule block
- **THEN** the marker-placement verification fails and reports the offending location

#### Scenario: A backticked prose mention is not a marker

- **WHEN** a guidance file mentions the token in prose as a backticked `` `captured:` ``
- **THEN** the marker-placement verification does not treat that mention as a marker

#### Scenario: Correctly placed markers pass

- **WHEN** every `captured:` marker in `AGENTS.md` and the project-authored guidance files lies within a rule block
- **THEN** the marker-placement verification passes

#### Scenario: A misplaced marker in a project guidance file fails the build

- **WHEN** a `captured:` marker in a project-authored `.opencode/agent/*.md` or `.opencode/skills/*/SKILL.md` file sits
  on a plain `- Text` bullet rather than inside a rule block
- **THEN** the marker-placement verification fails and names that file and the offending location

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
(which git silently skips when non-executable), the shell scripts (`*.sh`, excluding the vendored skill subtrees and the
generated `openspec-*` ones), and the Gradle wrapper. A lost executable bit SHALL fail the CI `build` gate, naming each
offending path.

#### Scenario: The standard check verifies the executable bits

- **WHEN** the standard `check` task runs
- **THEN** it includes the executable-bit verification

#### Scenario: A non-executable hook fails the build

- **WHEN** a tracked file under `scripts/git-hooks/` is not mode `100755`
- **THEN** the executable-bit verification fails and names the offending path

#### Scenario: A non-executable shell script fails the build

- **WHEN** a tracked shell script (`*.sh`, outside the vendored and generated `openspec-*` skill subtrees) is not mode
  `100755`
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

### Requirement: Line endings are normalized by a committed `.gitattributes`

The repository SHALL commit a `.gitattributes` that stores every `text`-detected file as LF in the index
(`* text=auto eol=lf`) and gives the Windows batch file an `eol=crlf` checkout (`*.bat text eol=crlf`), so a file's line
endings in the index are repo-declared and enforced by git at `add`/`checkout` rather than depending on a contributor's
`core.autocrlf`. `eol=crlf` governs the working-tree checkout only; a batch file is still stored as LF in the index,
like every other text file, and `text=auto` leaves a detected binary unnormalized.

#### Scenario: A text file is stored as LF regardless of its working-tree endings

- **WHEN** a text file with CRLF line endings is added to the index
- **THEN** git normalizes it to LF in the index, per the committed `.gitattributes`

#### Scenario: The Windows batch file is checked out as CRLF

- **WHEN** the repository's `gradlew.bat` is checked out
- **THEN** the committed `.gitattributes` gives it `eol=crlf`, so the working-tree file is CRLF, while its index blob is
  LF like every other text file

#### Scenario: A binary file is left unnormalized

- **WHEN** a tracked binary file (for example the Gradle wrapper JAR) is added
- **THEN** git detects it as binary and leaves it unnormalized, so its bytes are unchanged

### Requirement: Review-round ledger rows are unique

The build SHALL verify that the review-round ledger (`docs/review-rounds.tsv`) carries **at most one row per unit and
round**, failing when the same `(unit, round)` pair appears twice, so a duplicated row cannot double-count a round in
the retrospective figures aggregated from it. The same verification SHALL also fail when a unit's rounds are not
contiguous from `1` to its highest round, and — when the caller names a round whose report has just arrived
(`--expect <unit>:<round>`, repeatable) — when that row is absent, so a deferred append is caught in the same turn
rather than by the next reviewer; a gap whose record is unrecoverable SHALL be acknowledged by a comment line in the
ledger naming the unit and the round, and only an acknowledged gap SHALL pass. An expectation the mode cannot parse, an
expectation given without the mode, and a ledger row whose round column is not a number SHALL each fail rather than
skip. The verification SHALL be a `commit-hygiene.py` mode run by a `check` task, reading the tracked ledger rather than
the staged set.

That input clause scopes the build's verification.

When the caller also names the units whose loops must have converged (`--expect-clean <unit>`, repeatable, and valid
only with `--unique-rounds`), the same verification SHALL fail unless each named unit's numerically highest recorded
round carries no finding class, and SHALL fail when a named unit has no rows or its `classes` field is unreadable,
naming the unit, the round, and the classes found. The assertion SHALL be invoked by the review gate's manual-pass step,
and SHALL evaluate only the units the caller names, so the `check` task's invocation — which names none — never fails on
a unit's history, where a round in flight is unclean by design.

#### Scenario: A duplicated round row fails the check

- **WHEN** `check` runs and the ledger carries two rows with the same unit and round
- **THEN** the check fails, naming the unit, the round, and both line numbers

#### Scenario: One row per round passes

- **WHEN** every `(unit, round)` pair appears at most once, each unit's rounds are contiguous from `1` apart from
  acknowledged gaps, and every round the caller passed with `--expect` is recorded
- **THEN** the check passes

#### Scenario: A gap in a unit's rounds fails the check

- **WHEN** `check` runs and a unit's recorded rounds are `1, 2, 4`
- **THEN** the check fails, naming the unit and the missing round `3`

#### Scenario: The round whose report arrived is not recorded

- **WHEN** the caller runs the verification expecting `<unit>:3` and the ledger records that unit only through round `2`
- **THEN** the check fails, naming the unit and the expected round, even though its recorded rounds are contiguous

#### Scenario: An acknowledged gap passes

- **WHEN** a gap's record is unrecoverable and the ledger carries an acknowledgement comment naming the unit and the
  round
- **THEN** the check passes, and a gap without such an acknowledgement still fails

#### Scenario: An unparseable or mode-less expectation fails rather than skips

- **WHEN** the verification is run with an expectation that is not `<unit>:<round>`, or with an expectation and no
  `--unique-rounds`
- **THEN** it fails with a message naming what was wrong, rather than skipping the assertion

#### Scenario: A non-numeric round fails rather than being skipped

- **WHEN** a ledger row's round column is present but not a number
- **THEN** the check fails, naming the unit and the value, rather than ignoring the row

#### Scenario: A misfiled deep pass is acknowledged without a data row

- **WHEN** a historical number was assigned incorrectly to a deep-review pass whose authoritative record moved to the
  change's review task
- **THEN** an `# --- Acknowledged gap:` comment names the unit, the original number, and the destination
- **AND** no quick-review data row is created for that number

#### Scenario: A named unit's unclean highest round fails the verification

- **WHEN** the caller names a unit with `--expect-clean` and that unit's numerically highest recorded round carries one
  or more finding classes
- **THEN** the verification fails, naming the unit, the round, and the classes it found

#### Scenario: A named unit's clean highest round passes over earlier unclean rounds

- **WHEN** the named unit's highest recorded round carries `-` and earlier rounds carry classes
- **THEN** the verification passes, since the loop's convergence is recorded by the highest round

#### Scenario: A named unit with no rows fails rather than passing vacuously

- **WHEN** the caller names a unit the ledger does not record
- **THEN** the verification fails, naming the unit, rather than passing because no row contradicts it

#### Scenario: An unreadable classes field fails rather than reading as clean

- **WHEN** the caller names a unit whose highest row carries no classes field at all
- **THEN** the verification fails, naming the unit and the round, rather than treating the truncated row as clean

#### Scenario: A unit the caller does not name is not evaluated

- **WHEN** the ledger holds units whose highest round carries a finding class and the caller names none of them
- **THEN** the verification passes, so `check` stays green on the ledger's history

### Requirement: Review-round rows record the reviewer session's continuity

The build SHALL verify that the review-round ledger records, for every row below the boundary its own contract marks,
the reviewer session the round ran in — its identifier, its transition, and, for a replacement, a reason the review
loop's recovery cases allow or `caller-restarted` for a restart already performed without an evidenced recovery case (a
violation record, not a recovery case) — and that no row above it carries a field count other than the ledger's
pre-fields width. The verification SHALL be a `commit-hygiene.py` mode run by a `check` task over the tracked ledger.

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
  and one of the three recovery reasons — `expired`, `definition-changed`, or `stopped-responding`
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

#### Scenario: An already-performed caller restart is recorded without inventing a recovery cause

- **WHEN** a different reviewer session was already started without an evidenced recovery case
- **THEN** its row records `replaced` with `caller-restarted`
- **AND** the record identifies the continuity violation and its correction
- **AND** accepting that record does not authorize another restart

`caller-restarted` SHALL record a continuity violation, not an additional permitted recovery case; the verification
SHALL validate the recorded fields, and passing it SHALL NOT attest that the stated cause occurred or authorize the
review gate to continue.

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
