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

It SHALL also refuse a staged review-round ledger (`docs/review-rounds.tsv`) whose duplicate or non-contiguous rows the
ledger's own verification reports. It SHALL refuse a staged ledger whose session-continuity check reports a
contradiction. It SHALL refuse a staged ledger diff that removes a data row unless that `(unit, round)` remains in the
staged ledger, and a staged ledger whose file is deleted or renamed away has no row remaining. It SHALL judge the
ledger's staged content, not the working tree's copy. It SHALL report a staged ledger row whose round column cannot be
parsed as a named failure rather than raising.

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
