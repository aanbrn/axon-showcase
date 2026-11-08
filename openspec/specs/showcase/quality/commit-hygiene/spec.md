# showcase/quality/commit-hygiene Specification

## Purpose

Defines the mechanical guards over the commit/artifact hygiene the standard quality gates cannot see — a local
pre-commit hook that inspects the staged set and validates staged review-round records' duplicate and contiguity checks,
its reviewer-session continuity, the preservation of every recorded `(unit, round)` row, and each recorded round's
finding instances — build checks that verify `captured:` marker placement, that the tracked set excludes ignored files,
that no tracked file carries a merge conflict marker, that tracked scripts carry the executable bit, that no tracked
file exceeds the configured size limit, that recognized tables in formatter-owned Markdown have consistent cell counts,
that every project-authored command's lifecycle declaration is present, well-formed, resolvable, and mapped, when it
starts a unit carrying the preflight, that the Gradle wrapper pins its distribution with a checksum, that every dated
ideas section carries an entry, that no two workflow cron schedules collide, that no two archived changes carry the same
name apart from one recorded historical pair, that every unit records its workspace at `docs/units/<unit>/unit.md` and
every live change's `tasks.md` references that record with each archive judged by its inline record or its reference
according to its date, that the review-round ledger carries at most one row per unit and round, that each unit's rounds
are contiguous from `1` apart from an explicitly acknowledged gap, that a round the caller names with `--expect` is
recorded, that every class a row recorded after the canonical boundary carries is one the reviewer's definition names
apart from an acknowledged label, that each round's reviewer session and its continuity are recorded, that each unit's
agent-session register carries its review-event sequence, its invocations' continuity, and its hand-over links, that a
post-retirement unit's quick rows correspond one-to-one with its recorded rounds, that a quick-review round's hand-over
section is present before the delegation, that every round recorded after the findings store's boundary carries its
instances file, and that a unit's directory holds only the record kinds the layout admits, the deep review's own record
included — and, asserted at the review gate's manual-pass boundary rather than by a build check, that a unit the caller
names with `--expect-clean` closes on a round carrying no finding class, since a round in flight is unclean by design,
and that the unit's own tree is ready for the owner's pass: no unstaged new file, no stale indexed copy, and a base its
`HEAD` contains, failing as not established when a read it needs fails rather than reporting the tree clean — and a
committed `.gitattributes` that normalizes line endings (enforced by git, not the build) — so slips that were caught
only by a human or a review pass are blocked by construction.

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

It SHALL also refuse a staged unit round record (`docs/units/<unit>/rounds.tsv`) whose duplicate or non-contiguous rows
the record's own verification reports. It SHALL refuse staged records whose session-continuity check reports a
contradiction. It SHALL refuse a staged record's diff that removes a data row unless that `(unit, round)` remains in the
staged records, and a record deleted or renamed away has no row remaining. It SHALL judge the staged content, not the
working tree's copy. It SHALL report a staged row whose round column cannot be parsed as a named failure rather than
raising.

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

The hook SHALL also refuse a staged `docs/ideas.md` that the ideas-section verification rejects — a dated section
carrying no entry — naming the file, the section's line, and its heading, since that verification runs at `check` time
rather than at the commit. It SHALL judge the index's content rather than the working tree's copy, and SHALL refuse a
staged deletion of the file as a missing ideas file.

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

- **WHEN** a unit's `rounds.tsv` is staged and two of its rows share a unit and round
- **THEN** the guard refuses the commit and names the unit, the round and both rows

#### Scenario: A ledger gap refuses the commit

- **WHEN** a unit's `rounds.tsv` is staged and its rows are not contiguous from 1
- **THEN** the guard refuses the commit and names the unit and the missing round

#### Scenario: A ledger row contradicting the session's continuity refuses the commit

- **WHEN** a unit's `rounds.tsv` is staged and a row's session transition contradicts the continuity rule
- **THEN** the guard refuses the commit and names the row

#### Scenario: A removed ledger row refuses the commit

- **WHEN** a unit's `rounds.tsv` is staged and its diff removes a data row whose unit and round are absent from the
  staged records, or the record is deleted or renamed away
- **THEN** the guard refuses the commit and names the removal

#### Scenario: A duplicated row's repair proceeds

- **WHEN** a unit's `rounds.tsv` is staged and its diff removes a row whose unit and round remain in the staged records
- **THEN** the guard does not report the removal, so a duplicated row can be repaired

#### Scenario: An unparseable ledger row refuses the commit by name

- **WHEN** a unit's `rounds.tsv` is staged and a row's round column cannot be parsed
- **THEN** the guard refuses the commit and names the file and the failure, rather than raising

#### Scenario: A clean ledger append proceeds

- **WHEN** a unit's `rounds.tsv` is staged with an appended row that passes the checks
- **THEN** the guard reports no round-record failure

#### Scenario: A commit that does not stage the ledger runs no ledger check

- **WHEN** no `docs/units/<unit>/rounds.tsv` is in the staged set, whatever its working-tree content
- **THEN** the guard reports no round-record failure

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

#### Scenario: A staged emptied ideas section refuses the commit

- **WHEN** `docs/ideas.md` is staged and one of its dated sections carries no `- ` entry
- **THEN** the guard refuses the commit and names the file, the section's line, and its heading

#### Scenario: A staged deletion of the ideas file refuses the commit

- **WHEN** `docs/ideas.md` is staged as deleted
- **THEN** the guard refuses the commit and names the missing file

#### Scenario: A commit that does not stage the ideas file runs no ideas-section check

- **WHEN** a commit's staged set does not carry `docs/ideas.md`
- **THEN** the guard reports no ideas-section failure

#### Scenario: The guard judges the staged copy, not the working tree's

- **WHEN** the staged `docs/ideas.md` differs from the working tree's copy
- **THEN** the guard judges the content the commit records

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
bold-lead bullet or a bold-lead paragraph — and ends it, so no rule text of that block follows the marker; the block
ends at the next bullet, numbered step, bold-lead paragraph, heading, table row, fence, or blank line. The rest of the
marker's chain, a name wrapped onto the next line, a citation after a name, and an `evidence:` tag line are its own
text, not rule text that follows it. A marker that ends the first paragraph of a rule whose second paragraph continues
it passes, because the check judges the block and not the whole rule. A marker inside a fenced block is not rule prose
and SHALL NOT be judged, whether it annotates a command example or records the origin of a command block's comment
guidance. A marker SHALL never sit on a plain `- Text` bullet, so a misplaced marker fails the CI `build` gate even when
no local hook is installed or the hook was bypassed. A backticked mention of the token in prose (`` `captured:` ``) is
not a marker and SHALL NOT be treated as one. The marker's existence rule (each captured rule carries a greppable
`captured: <unit>` origin) is specified in `showcase/quality/agent-skills`.

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

#### Scenario: A marker inside a fenced block is not judged

- **WHEN** a guidance file carries a `captured:` marker inside a fenced code block, as a command example or as the
  origin of a command block's comment guidance
- **THEN** the marker-placement verification does not judge that marker's placement

#### Scenario: Correctly placed markers pass

- **WHEN** every `captured:` marker in `AGENTS.md` and the project-authored guidance files lies within a rule block and
  nothing but its chain follows it
- **THEN** the marker-placement verification passes

#### Scenario: A marker followed by rule text fails the build

- **WHEN** a `captured:` marker sits inside a rule block with rule text of that block after it, so the marker reads as
  part of the sentence around it rather than as the rule's trailing provenance
- **THEN** the marker-placement verification fails and reports the offending location

#### Scenario: A misplaced marker in a project guidance file fails the build

- **WHEN** a `captured:` marker in a project-authored `.opencode/agent/*.md` or `.opencode/skills/*/SKILL.md` file sits
  on a plain `- Text` bullet rather than inside a rule block
- **THEN** the marker-placement verification fails and names that file and the offending location

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

The build SHALL verify that the unit round records (`docs/units/<unit>/rounds.tsv`) carry **at most one row per unit and
round**, failing when the same `(unit, round)` pair appears twice, so a duplicated row cannot double-count a round in
the retrospective figures aggregated from it. A record filed under a unit's directory SHALL carry only that unit's rows:
a row whose unit column names another unit SHALL fail the verification, so a misfiled record fails rather than passing
uniqueness and contiguity on its own rows. The same verification SHALL also fail when a unit's rounds are not contiguous
from `1` to its highest round, and — when the caller names a round whose report has just arrived
(`--expect <unit>:<round>`, repeatable) — when that row is absent, so a deferred append is caught in the same turn
rather than by the next reviewer; a gap whose record is unrecoverable SHALL be acknowledged by a comment line in the
unit's record naming the unit and the round, and only an acknowledged gap SHALL pass. An expectation the mode cannot
parse, an expectation given without the mode, and a record row whose round column is not a number SHALL each fail rather
than skip. The verification SHALL be a `commit-hygiene.py` mode run by a `check` task, reading the tracked records
rather than the staged set.

That input clause scopes the build's verification.

When the caller also names the units whose loops must have converged (`--expect-clean <unit>`, repeatable, and valid
only with `--unique-rounds`), the same verification SHALL fail unless each named unit's numerically highest recorded
round carries no finding class, and SHALL fail when a named unit has no rows or its `classes` field is unreadable,
naming the unit, the round, and the classes found. The assertion SHALL be invoked by the review gate's manual-pass step,
and SHALL evaluate only the units the caller names, so the `check` task's invocation — which names none — never fails on
a unit's history, where a round in flight is unclean by design.

#### Scenario: A duplicated round row fails the check

- **WHEN** `check` runs and the records carry two rows with the same unit and round
- **THEN** the check fails, naming the unit, the round, and both line numbers

#### Scenario: One row per round passes

- **WHEN** every `(unit, round)` pair appears at most once, each unit's rounds are contiguous from `1` apart from
  acknowledged gaps, and every round the caller passed with `--expect` is recorded
- **THEN** the check passes

#### Scenario: A gap in a unit's rounds fails the check

- **WHEN** `check` runs and a unit's recorded rounds are `1, 2, 4`
- **THEN** the check fails, naming the unit and the missing round `3`

#### Scenario: The round whose report arrived is not recorded

- **WHEN** the caller runs the verification expecting `<unit>:3` and the records hold that unit only through round `2`
- **THEN** the check fails, naming the unit and the expected round, even though its recorded rounds are contiguous

#### Scenario: An acknowledged gap passes

- **WHEN** a gap's record is unrecoverable and the records carry an acknowledgement comment naming the unit and the
  round
- **THEN** the check passes, and a gap without such an acknowledgement still fails

#### Scenario: An unparseable or mode-less expectation fails rather than skips

- **WHEN** the verification is run with an expectation that is not `<unit>:<round>`, or with an expectation and no
  `--unique-rounds`
- **THEN** it fails with a message naming what was wrong, rather than skipping the assertion

#### Scenario: A non-numeric round fails rather than being skipped

- **WHEN** a record row's round column is present but not a number
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

- **WHEN** the caller names a unit the records do not include
- **THEN** the verification fails, naming the unit, rather than passing because no row contradicts it

#### Scenario: An unreadable classes field fails rather than reading as clean

- **WHEN** the caller names a unit whose highest row carries no classes field at all
- **THEN** the verification fails, naming the unit and the round, rather than treating the truncated row as clean

#### Scenario: A unit the caller does not name is not evaluated

- **WHEN** the records hold units whose highest round carries a finding class and the caller names none of them
- **THEN** the verification passes, so `check` stays green on the records' history

#### Scenario: A row filed under another unit's directory fails the check

- **WHEN** a unit's `rounds.tsv` carries a row whose unit column names a different unit
- **THEN** the check fails, naming the file, the row, and both unit names

### Requirement: Review-round rows record the reviewer session's continuity

The build SHALL verify that a unit's round record (`docs/units/<unit>/rounds.tsv`) whose first round predates
`2026-11-09` records, for every row below the boundary the record format marks, the reviewer session the round ran in —
its identifier, its transition, and, for a replacement, a reason the agent-session register's format requirement names
or `caller-restarted` for a restart already performed without an evidenced recovery case (a violation record, not a
recovery case) — and that no row above it carries a field count other than the format's pre-fields width. A unit whose
first round is dated on or after that date carries six-field rows: this mode SHALL judge its width, so a nine-field row
in it fails, but SHALL NOT judge its session fields, whose continuity is the agent-session register's. A record carrying
no boundary comment SHALL be read as having all its rows on the side its own rows show. The verification SHALL be a
`commit-hygiene.py` mode run by a `check` task over the tracked records.

#### Scenario: A post-retirement unit's width is judged, its session fields are not

- **WHEN** a unit's first round is dated on or after `2026-11-09`
- **THEN** the mode accepts its six-field rows and rejects a nine-field one, and it requires no session fields on them

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

### Requirement: A markdown table's rows carry the delimiter row's column count

The standard `check` task SHALL verify the tables in the markdown the Spotless format owns. A table SHALL be a row
carrying an unescaped pipe whose next line is a delimiter row that itself carries an unescaped pipe, running until a
blank line or a line that begins another block — a heading, a fence, a thematic break, a list item, a blockquote, or an
HTML block — and each of its lines SHALL carry the delimiter row's count of cells, where a cell is what splitting on the
unescaped pipes yields after one optional leading and one optional trailing pipe are removed. The verification SHALL
read the file set the build passes it, SHALL fail when it is given no path or a path it cannot read, and SHALL name each
offending file and line with the line's count and the delimiter row's.

A line inside a table carrying no unescaped pipe SHALL fail as a wrapped row. A pipe SHALL separate cells unless it
follows an odd number of backslashes. A fenced block SHALL NOT be read as a table. A table whose opening row begins with
a blockquote or list prefix is outside this requirement's coverage, and a line beginning with `<` SHALL be read as an
HTML block, so a row that begins with one ends the recognized table and the rows after it are not examined. This
requirement covers the recognized tables, never every line a renderer might lay out as a row.

#### Scenario: The standard check verifies table rows

- **WHEN** the standard `check` task runs
- **THEN** it includes the markdown table-row verification, over the same file set the markdown format owns

#### Scenario: A wrapped table row fails the build

- **WHEN** a table row is wrapped onto a second line, which carries no unescaped pipe
- **THEN** the verification fails and names the file, the line, and that it carries no separator

#### Scenario: A bare pipe inside a cell fails the build

- **WHEN** a table row carries more unescaped pipes than its delimiter row because a cell holds a bare `|`
- **THEN** the verification fails and names the file, the line, its count, and the delimiter row's count

#### Scenario: An escaped pipe is content

- **WHEN** a cell carries `\|` in place of a bare pipe
- **THEN** the verification passes that row

#### Scenario: A fenced block is not a table

- **WHEN** a fenced block contains pipe characters, as a diagram or a command does
- **THEN** the verification does not read it as a table

#### Scenario: A clean table passes

- **WHEN** every row of a table carries the delimiter row's count of unescaped pipes
- **THEN** the verification reports no failure for that table

#### Scenario: A table without outer pipes passes

- **WHEN** a table's rows carry no leading or trailing pipe, and each row's separators leave the delimiter row's cell
  count
- **THEN** the verification passes that table

#### Scenario: A supplied file that cannot be read fails

- **WHEN** a path the build passes the verification cannot be read
- **THEN** the verification fails and names that path

#### Scenario: A verification given no path fails

- **WHEN** the table-row verification runs with no path to read
- **THEN** it fails rather than reporting a clean scan

### Requirement: A dated ideas section carries an entry

The standard `check` task SHALL verify that every dated section (`## YYYY-MM-DD`) in `docs/ideas.md` carries at least
one idea entry, so removing a section's last entry fails the build rather than leaving an empty heading in the file. The
verification SHALL name each offending section's line and heading. A file that carries no dated section SHALL pass: an
emptied ideas file is a legitimate state, not a defect. A missing `docs/ideas.md` SHALL fail.

#### Scenario: The standard check verifies the ideas sections

- **WHEN** the standard `check` task runs
- **THEN** it includes the ideas-section verification

#### Scenario: An emptied section fails the build

- **WHEN** a dated section in `docs/ideas.md` carries no `- ` entry
- **THEN** the verification fails and names the section's line and heading

#### Scenario: A section carrying entries passes

- **WHEN** every dated section in `docs/ideas.md` carries at least one `- ` entry
- **THEN** the verification passes

#### Scenario: A file with no dated section passes

- **WHEN** `docs/ideas.md` carries no dated section
- **THEN** the verification passes

#### Scenario: A missing ideas file fails

- **WHEN** `docs/ideas.md` is absent
- **THEN** the verification fails and names the missing file

### Requirement: Every check the capability ships is covered by tests run in the build

Each check this capability ships SHALL be covered by an automated test that exercises it — the pre-commit guard's defect
classes, the build checks it owns, and the manual-pass readiness check — and those tests SHALL run in the standard
`check` task, so a disabled or weakened check fails the build. The test suite SHALL map every mode the checker exposes
to the test class whose source names the mode and its checker function's call form.

#### Scenario: The checker's tests run in the standard check

- **WHEN** the standard `check` task runs
- **THEN** it runs the checker's automated tests

#### Scenario: A disabled check fails a test

- **WHEN** a check is disabled or its logic regresses
- **THEN** at least one test that exercises that defect class fails

#### Scenario: A mode added without a test fails the build

- **WHEN** the checker exposes a mode that the test module's mapping does not name, or names a class that has no test
- **THEN** the mapping control fails and names the mode

#### Scenario: A mode mapped to a class that never names it fails the build

- **WHEN** the mapping names a class that exists and carries tests, but its source does not name the mode, or does not
  name its checker function's call form
- **THEN** the mapping control fails and names the mode

### Requirement: Review-round classes come from the reviewer's canonical list

The build SHALL verify that every finding class recorded in the unit round records (`docs/units/<unit>/rounds.tsv`) — in
the classes-raised and the classes-that-repeated column of a row dated after the canonical boundary — is a class the
reviewer's canonical list names, so a class the list does not carry fails the build rather than splitting the
aggregation the retrospective reads. The canonical list's owner SHALL be `.opencode/agent/review-quick.md`, and the
boundary's owner SHALL be `scripts/review-rounds.py`, which records the last day whose rows may carry a pre-canonical
label; the verification SHALL read both rather than restate either. A row dated on or before that boundary SHALL pass
with the labels it was recorded with.

A label whose round's report was never reconstructed SHALL be exempt only when a comment line of the form
`# --- Acknowledged class: <unit> round <N> <label> - <reason>` names that row and that label, so an exemption covers
one label on one row rather than a unit or a label generally. The comment SHALL be accepted as the record of that fact:
the verification SHALL NOT attempt to re-derive it, as the records' acknowledged-gap form does not. A verification that
cannot read the records, the boundary, or the canonical list SHALL fail, naming the unreadable source, rather than
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

- **WHEN** the records, the boundary's owner, or the canonical list cannot be read; the list's extraction finds no
  class, more than one list, or a token that is not a class; or the boundary declares no date, or a value that is not a
  written-out calendar date
- **THEN** the verification fails, naming the unreadable or ambiguous source, rather than reporting every row clean

### Requirement: Archived change names are unique

The standard `check` task SHALL verify that no two directories under `openspec/changes/archive/` carry the same change
name, its own name with one leading `YYYY-MM-DD-` segment removed, so a reused archived name fails the CI `build` gate.
It SHALL name the shared name and every directory carrying it. One historical collision SHALL be exempt only as the
exact directory set the verification records beside its reason; that exemption SHALL NOT cover the name generally.

#### Scenario: The standard check verifies the archived change names

- **WHEN** the standard `check` task runs
- **THEN** it includes the archived-change-name verification

#### Scenario: Two archived directories carrying one name fail the build

- **WHEN** two directories under `openspec/changes/archive/` carry the same change name after their date prefixes are
  removed
- **THEN** the verification fails and names the shared name and both directories

#### Scenario: A name shared across two archive dates fails the build

- **WHEN** two archived directories were archived on different dates and carry the same change name
- **THEN** the verification fails, because the date prefix is stripped rather than compared

#### Scenario: Distinct archived names pass

- **WHEN** no two archived directories carry the same change name after their date prefixes are removed
- **THEN** the verification passes

#### Scenario: An archive entry without a date prefix is not judged

- **WHEN** an `openspec/changes/archive/` entry's name carries no `YYYY-MM-DD-` prefix
- **THEN** the verification does not judge it and it joins no name group

#### Scenario: The acknowledged historical pair passes

- **WHEN** the only directories carrying a name are the exact pair the verification records as acknowledged
- **THEN** the verification passes

#### Scenario: A third directory carrying the acknowledged name fails

- **WHEN** a third directory carries a name whose recorded pair is acknowledged
- **THEN** the verification fails and names that directory

#### Scenario: A missing archive directory fails

- **WHEN** `openspec/changes/archive/` is absent
- **THEN** the verification fails and names the missing directory rather than reporting an empty archive clean

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

### Requirement: The review-round record format is stated once

Each unit's review-round record SHALL be the file `docs/units/<unit>/rounds.tsv`, holding that unit's data rows in the
order they were recorded. A unit whose first round is dated on or after the retirement date (`2026-11-09`) carries six
tab-separated fields: the date (`YYYY-MM-DD`); the unit, which SHALL name the directory the record is filed under; the
round, continuous per unit across its proposal and implementation reviews; the classes raised, comma-separated or `-`;
the classes that repeated a prior round, comma-separated or `-`; and the compliance-miss count. A unit whose first round
predates that date keeps the nine-field form: those six fields, then the reviewer's session identifier, the session
transition (`created`, `continued`, or `replaced`), and a replacement's reason the agent-session register's format
requirement names. In a unit whose first round predates that date, a row dated on or before 2026-10-18 carries the
six-field pre-fields width and a later row carries the nine-field form. A unit SHALL NOT mix the retirement's six-field
form with the nine-field form; its pre-2026-10-18 rows are the pre-fields form, not the retirement form.

In a unit whose first round predates `2026-11-09`, the boundary between the rows that predate the session fields and
those that carry them SHALL be marked by a comment line naming it. A record SHALL carry that marker only where it holds
rows on both sides, and a record without one SHALL be read as having all its rows on the side its own rows show. A row
carrying the six-field width in a unit whose first round predates `2026-11-09` SHALL fail when it is dated after
2026-10-18, because that unit's rows carry the session fields from that day; a row dated on or before 2026-10-18 keeps
the pre-fields width, and a unit whose first round is dated on or after `2026-11-09` carries the six-field form by
design and is not this paragraph's case.

A gap whose report was never reconstructed SHALL be acknowledged by a comment line of the form
`# --- Acknowledged gap: <unit> round <N> - <reason>`, and a class recorded before the canonical list landed, whose
round's report was never reconstructed, by `# --- Acknowledged class: <unit> round <N> <label> - <reason>`, which covers
that label on that row only.

This requirement SHALL own the record format, so the `review-ledger` binding's owner cell SHALL name it rather than the
retired header, and no per-unit file SHALL carry a normative header of its own. The session fields' values are the
agent-session register's format requirement's, and the reasons a replacement may carry are stated there.

#### Scenario: A record's fields parse in the stated order

- **WHEN** a unit's `rounds.tsv` carries a data row of the nine fields, or a post-retirement unit's carries six
- **THEN** the record's readers parse the date, unit, round, classes raised, classes repeated, and compliance misses
  from it, and the nine-field rows' session identifier, transition, and replacement reason as well

#### Scenario: A unit's first round decides its width

- **WHEN** a unit's first round is dated on or after `2026-11-09`
- **THEN** its rows carry the six-field form for its life; a unit first rounded before that date keeps the historical
  row-level form, six fields for a row dated on or before 2026-10-18 and nine after it

#### Scenario: A nine-field row in a post-retirement unit fails

- **WHEN** a unit whose first round is dated on or after `2026-11-09` carries a nine-field row
- **THEN** `--session-continuity` fails, naming the unit and the row

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

- **WHEN** a unit whose first round predates `2026-11-09` carries a six-field row dated after 2026-10-18
- **THEN** `--session-continuity` fails, naming the row, its date, and the fields it lacks

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

### Requirement: The wrapper's distribution checksum is verified by the build

The standard `check` task SHALL verify that `gradle/wrapper/gradle-wrapper.properties` pins its distribution with a
`distributionSha256Sum` of 64 hexadecimal characters and a `distributionUrl` whose archive name ends `-all.zip`, so a
bump that drops or mangles the pin fails the build rather than leaving the wrapper accepting an unverified download.

#### Scenario: The standard check verifies the wrapper's checksum

- **WHEN** the standard `check` task runs
- **THEN** it includes the wrapper-checksum verification

#### Scenario: A properties file without a checksum fails the build

- **WHEN** `gradle/wrapper/gradle-wrapper.properties` carries no `distributionSha256Sum`
- **THEN** the verification fails and names the missing pin

#### Scenario: A malformed checksum fails the build

- **WHEN** the pinned value is not 64 hexadecimal characters, or the `distributionUrl` does not name a `-all.zip`
  archive
- **THEN** the verification fails and names the offending value

#### Scenario: A clean wrapper passes

- **WHEN** the properties pin a 64-hex checksum and an `-all.zip` distribution
- **THEN** the wrapper-checksum verification passes

### Requirement: A unit's directory holds only the record kinds the layout names

A unit's directory, `docs/units/<unit>/`, admits exactly: `unit.md`, `rounds.tsv`, `review-handover.md`,
`process-defects.md`, `sessions.tsv` and `deep-review.md` at its first level, and `findings/round-<N>.md` at its second.
Presence is not required; any other tracked path below it SHALL fail. The verification SHALL be a `commit-hygiene.py`
mode run by a `check` task over the tracked records, naming the unit, the path and that the layout does not admit it.

#### Scenario: The standard check verifies the layout

- **WHEN** the standard `check` task runs
- **THEN** it includes the unit-layout verification

#### Scenario: A new kind is added with its verification

- **WHEN** a change first writes a record kind the admitted set does not name
- **THEN** that change amends this requirement and its verification, rather than placing the file alone

#### Scenario: A unit holding only admitted kinds passes

- **WHEN** every tracked path under a unit's directory is `unit.md`, `rounds.tsv`, `review-handover.md`,
  `process-defects.md`, `sessions.tsv`, `deep-review.md`, or `findings/round-<N>.md`
- **THEN** the verification passes for that unit

#### Scenario: A file at the unit's root that the layout does not admit fails

- **WHEN** a tracked file sits directly in `docs/units/<unit>/` and its name is not one of the six single-file kinds
- **THEN** the verification fails, naming the unit, the path and that the layout does not admit it

#### Scenario: A path the two levels do not admit fails

- **WHEN** a tracked path below `docs/units/<unit>/` is neither one of the six first-level filenames nor
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

### Requirement: The command lifecycle projection is verified

The standard `check` task SHALL verify every project-authored command's lifecycle declaration: its presence, its
recognized stage, invocation, and effect values, that every named owning procedure and bound heading resolves, that a
unit-starting branch carries the preflight its owning requirement states, and that the generated entry mapping covers
every generated command.

#### Scenario: An undeclared command fails

- **WHEN** a project-authored command carries no lifecycle declaration
- **THEN** the verification fails and names the command and the missing declaration

#### Scenario: An unrecognized value fails

- **WHEN** a declaration names a stage, invocation, or effect outside the published vocabulary
- **THEN** the verification fails and names the value

#### Scenario: A broken owner reference fails

- **WHEN** a declaration names an owning procedure or binding heading that does not resolve
- **THEN** the verification fails and names the unresolved reference

#### Scenario: A unit-starting branch lacks its preflight anchors

- **WHEN** a command declared as unit-starting omits the anchors its owning requirement names
- **THEN** the verification fails and names the missing anchor

#### Scenario: A generated command is unmapped

- **WHEN** a generated command has no entry in the lifecycle section's mapping
- **THEN** the verification fails and names the command

### Requirement: The projection check derives its coverage from the tracked set

The verification SHALL derive its coverage from the tracked command set rather than from a hand-maintained list, so an
added undeclared command fails rather than passing as an empty extraction.

#### Scenario: An empty extraction fails

- **WHEN** the verification's derived command set is empty, or its declared model is absent
- **THEN** the verification fails rather than reporting a clean empty set

### Requirement: The projection check states what it proves

The verification SHALL prove declaration completeness, permitted values, reference resolution, the required anchors, and
mapping coverage, and SHALL NOT be read as proof of a command's behaviour or of any owner decision.

#### Scenario: The controls prove the check can fail

- **WHEN** the verification's tests run
- **THEN** each predicate carries a known-bad case and a known-good one

### Requirement: The agent-session record is stated once

Each unit's agent-session record SHALL be the file `docs/units/<unit>/sessions.tsv`, holding that unit's rows of
registered invocations in the order they were recorded, as tab-separated fields: the date (`YYYY-MM-DD`); the unit,
which SHALL name the directory the record is filed under; the invoking main session's identifier; the agent, which SHALL
name a registered agent by its own name; the scope, which SHALL be the unit for a reviewer and a kebab-case decision
slug for a consultation; the opinion (`primary` or `independent` for a consultation, `-` for a reviewer); the pass,
continuous per `(unit, agent, scope, opinion)`; the requested session identifier (`-` when the invocation was asked for
none); the returned session identifier (`-` when the invocation reported none); the transition (`created`, `continued`,
`replaced`, or `-`); a replacement's reason (`expired`, `definition-changed`, `stopped-responding`,
`parent-session-changed`, or `caller-restarted`, which records a violation rather than a justified case; `-` otherwise);
the hand-over section's slug; the outcome (`report`, `stalled`, `failed`, or `mismatch`); and, on a row dated on or
after the event boundary (`2026-11-09`), the event — the unit's sequence number, contiguous from `1` across its rows
from that date — then the round a quick row attempts or reports (`-` for another agent). A row before that date carries
the thirteen-field width and neither field; a row on or after it carries fifteen fields.

A non-quick row's hand-over slug SHALL name a second-level heading in the unit's `review-handover.md`, which SHALL begin
with that slug in backticks. A quick row's slug is `round-<round>` and resolves to a second-level heading beginning
`Round <round>`, the form the hand-over check uses, so the row and the context it points at resolve to each other. A
non-quick row's round SHALL be `-`.

The quick reviewer SHALL be recorded here for a unit whose first round is dated on or after `2026-11-09`: its scope is
the unit, its opinion `-`, its round the round it attempts or reports, its pass the invocation's number for that key,
and its hand-over slug `round-<round>`. A round may carry several invocations — a stalled attempt, another, and the one
that reports — each its own row with the round and the next pass, so the round's ledger row stays one and the hand-over
section keeps each attempt's context. A stalled invocation's row is appended when its stall is detected, before the
replacement starts; the replacement's row is appended when its own outcome is known. For a unit whose first round
predates `2026-11-09`, the quick reviewer's continuity is the round record's and SHALL NOT be recorded here. A
consultation that starts no unit SHALL NOT be recorded here either.

This requirement SHALL own the agent-session record's format, and no per-unit file SHALL carry a normative header of its
own. The set of registered agents is the requirement recording their sessions, not this one.

#### Scenario: A record's fields parse in the stated order

- **WHEN** a unit's `sessions.tsv` carries a data row dated before `2026-11-09`, and a row on or after it
- **THEN** the earlier row parses thirteen fields and the later one fifteen, the last two being the event and the round

#### Scenario: A record's unit column names its directory

- **WHEN** a record is filed at `docs/units/<unit>/sessions.tsv`
- **THEN** every row's unit column names that same `<unit>`

#### Scenario: A hand-over slug resolves to its section

- **WHEN** a row other than a quick row carries a hand-over slug
- **THEN** the unit's `review-handover.md` carries a second-level heading that begins with that slug in backticks

#### Scenario: An opinion belongs to a consultation

- **WHEN** a reviewer row carries a non-`-` opinion, or a consultation row carries `-`
- **THEN** the verification fails, naming the row

#### Scenario: A scope names its agent's subject

- **WHEN** a reviewer row's scope differs from the unit, or a consultation row's scope is not a kebab-case slug
- **THEN** the verification fails, naming the row

#### Scenario: A quick-reviewer row is not admitted

- **WHEN** a row names `review-quick` as its agent for a unit whose first round predates `2026-11-09`
- **THEN** the verification fails, naming the row, because that unit's quick continuity is the review-round record's

#### Scenario: A quick-reviewer row is admitted for a post-retirement unit

- **WHEN** a row names `review-quick` as its agent for a unit whose first round is dated on or after `2026-11-09`
- **THEN** the verification accepts it

#### Scenario: An event is present from its boundary

- **WHEN** a row is dated on or after `2026-11-09`
- **THEN** it carries the event field, contiguous from `1` per unit, and a row before it carries none

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
- the pass SHALL number every row continuously from `1` per key, whether or not the row resolves a session; a
  `review-quick` row's pass is its invocation's number and its round SHALL be present, and every quick row of one round
  (the stalled attempts and the one that reports) SHALL share that round;
- the event SHALL be contiguous from `1` per unit across the rows dated on or after `2026-11-09`, and such a row SHALL
  carry the fifteen-field width while a row before it carries the thirteen-field width;
- a `replaced` row SHALL carry a reason this record's format requirement names, and a row whose transition is not
  `replaced` SHALL carry none;
- a row naming an agent the registering requirement does not register SHALL fail, and a `review-quick` row SHALL fail
  when the unit's first round predates `2026-11-09`;
- a row's hand-over slug SHALL resolve to a section, as the format requirement states, and a `review-quick` row's
  `round-<round>` slug SHALL resolve to a second-level heading beginning `Round <round>`.

The record SHALL be append-only: a row SHALL NOT be removed or rewritten, so a staged record's committed prefix SHALL
equal the committed record's rows.

The verification SHALL be a `commit-hygiene.py` mode run by a `check` task over the tracked records, and the pre-commit
guard SHALL refuse a staged record whose verification reports a violation, judging the staged content rather than the
working tree's copy, and a staged record that removes or rewrites a committed row.

Passing the verification SHALL validate the recorded fields only: it SHALL NOT attest that a replacement's stated cause
occurred, that a forced replacement was reported to the owner, or that a discretionary one was approved.

#### Scenario: An event gap or duplicate fails

- **WHEN** a unit's events are not contiguous from `1` on its rows dated on or after `2026-11-09`
- **THEN** the verification fails, naming the unit and the event

#### Scenario: A quick row on a post-retirement unit passes

- **WHEN** a unit whose first round is dated on or after `2026-11-09` records `review-quick` with its invocation as the
  pass, the round it reports, and `round-<round>` as its slug
- **THEN** the verification passes for that row

#### Scenario: A quick row on a pre-retirement unit fails

- **WHEN** a unit whose first round predates `2026-11-09` records `review-quick`
- **THEN** the verification fails, naming the row

#### Scenario: A quick row without its round fails

- **WHEN** a `review-quick` row carries `-` as its round
- **THEN** the verification fails, naming the row

#### Scenario: A round's several quick rows share the round

- **WHEN** a round's reviewer stalled twice and the third invocation reported
- **THEN** the three rows carry that round and consecutive passes

#### Scenario: A row's width follows the boundary

- **WHEN** a row dated before `2026-11-09` carries a width other than thirteen, or one on or after it a width other than
  fifteen
- **THEN** the verification fails, naming the row and its width

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

### Requirement: A quick reviewer's hand-over section is checked before the delegation

`scripts/commit-hygiene.py --handover <unit>:<round>` SHALL fail when the unit's `docs/units/<unit>/review-handover.md`
carries no second-level heading beginning with `Round` and the named round's exact token, when the file is missing or
unreadable, or when the argument is malformed.

`<unit>` SHALL be one directory name under `docs/units/`: non-empty, with no path separator, no `.` or `..` component,
no whitespace, and no extra colon. `<round>` SHALL be a canonical positive decimal integer: no sign, no leading zero,
and not zero. The heading's text SHALL begin with `Round`, then the named round's exact decimal token, and that token
SHALL be followed by the end of the text or by whitespace separating a descriptive suffix, so neither `Round 30` nor
`Round 3x` SHALL match round 3. The round token SHALL NOT be wrapped in backticks. The check SHALL read the working
tree's file: it SHALL NOT require the file to be staged, a review-ledger row, or an agent-session row.

The check SHALL be run after the section is written and before the delegation, and its failure SHALL stop the call.

The check SHALL establish presence at inspection only. It SHALL NOT establish the section's completeness, its
attribution to the invocation, its delivery, or that it was written before the delegation: copied content beneath a
matching current-round heading SHALL pass, and a heading written after the call SHALL pass, while a heading naming a
different round SHALL fail. Passing the check SHALL NOT attest that the delegation happened or that its hand-over was
adequate.

#### Scenario: A present heading passes

- **WHEN** the unit's file carries `## Round 3` and the check names round 3
- **THEN** the check passes

#### Scenario: A descriptive suffix passes

- **WHEN** the heading is `## Round 3 — proposal screen` and the check names round 3
- **THEN** the check passes

#### Scenario: A round token is matched exactly

- **WHEN** the file carries `## Round 30` and the check names round 3
- **THEN** the check fails, naming the unit and the round

#### Scenario: A different round fails

- **WHEN** the file carries only `## Round 2` and the check names round 3
- **THEN** the check fails, naming the unit and the round

#### Scenario: A missing file fails

- **WHEN** the unit's `review-handover.md` does not exist
- **THEN** the check fails, naming the missing file

#### Scenario: An unreadable file fails

- **WHEN** the file exists but cannot be decoded as UTF-8
- **THEN** the check fails, naming the file

#### Scenario: A malformed argument fails

- **WHEN** the argument is not `<unit>:<round>` with a canonical positive round number
- **THEN** the check fails, naming what it expected

#### Scenario: A unit that is not one directory name fails

- **WHEN** the unit part is empty, contains a path separator, is `.` or `..`, contains whitespace, or adds a second
  colon
- **THEN** the check fails, naming the unit it received

#### Scenario: A third-level heading is not a section

- **WHEN** the file carries `### Round 3` rather than a second-level heading
- **THEN** the check fails

#### Scenario: Copied content passes beneath a matching heading

- **WHEN** the round's heading matches and its body was copied from an earlier round
- **THEN** the check passes, reporting only that a matching heading was present when it ran

#### Scenario: The check does not establish delivery or ordering

- **WHEN** the check passes for a section written after the delegation
- **THEN** it reports only that a matching heading was present when it ran

### Requirement: A post-retirement round's session evidence is complete

For a unit whose first round is dated on or after `2026-11-09`, each recorded round SHALL have exactly one
`review-quick` row with the `report` outcome and that round. A recorded round without one, a second `report` row for the
same round, or a quick `report` row naming no recorded round SHALL fail. The verification SHALL read the unit's ledger
and register as they are committed or staged, and it SHALL be a `commit-hygiene.py` mode. The digest's permissive read
is not this requirement's subject.

#### Scenario: A round with its report row passes

- **WHEN** a post-retirement unit's round 3 has one `review-quick` `report` row carrying round 3
- **THEN** the verification passes for that round

#### Scenario: A round without its report row fails

- **WHEN** a post-retirement unit records round 3 in its ledger and no quick `report` row carries that round
- **THEN** the verification fails, naming the unit and the round

#### Scenario: A duplicate report row fails

- **WHEN** two quick `report` rows carry the same round
- **THEN** the verification fails, naming the unit and the round

#### Scenario: A quick report row naming no recorded round fails

- **WHEN** a quick `report` row carries a round the ledger does not record
- **THEN** the verification fails, naming the unit and the round
