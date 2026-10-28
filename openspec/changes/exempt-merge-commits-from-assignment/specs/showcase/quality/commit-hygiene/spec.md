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
