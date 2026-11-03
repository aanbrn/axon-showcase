## MODIFIED Requirements

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

The build SHALL verify that a unit's round record (`docs/units/<unit>/rounds.tsv`) records, for every row below the
boundary the record format marks, the reviewer session the round ran in — its identifier, its transition, and, for a
replacement, a reason the review loop's recovery cases allow or `caller-restarted` for a restart already performed
without an evidenced recovery case (a violation record, not a recovery case) — and that no row above it carries a field
count other than the format's pre-fields width. A record carrying no boundary comment SHALL be read as having all its
rows on the side its own rows show, so the comment SHALL be required only where a record holds rows on both sides. The
verification SHALL be a `commit-hygiene.py` mode run by a `check` task over the tracked records.

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

## ADDED Requirements

### Requirement: The review-round record format is stated once

Each unit's review-round record SHALL be the file `docs/units/<unit>/rounds.tsv`, holding that unit's data rows in the
order they were recorded, as tab-separated fields: the date (`YYYY-MM-DD`); the unit, which SHALL name the directory the
record is filed under; the round, continuous per unit across its proposal and implementation reviews; the classes
raised, comma-separated or `-`; the classes that repeated a prior round, comma-separated or `-`; the compliance-miss
count; the reviewer's session identifier; the session transition (`created`, `continued`, or `replaced`); and a
replacement's reason (`expired`, `definition-changed`, `stopped-responding`, or `caller-restarted`; `-` otherwise).

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

#### Scenario: A six-field row dated after the fields fails

- **WHEN** a record row dated after 2026-10-18 carries the pre-fields width
- **THEN** the continuity verification fails, naming the row, its date, and the fields it lacks
