# Design

## Context

A unit of work is one branch in one working tree, and it may or may not carry an OpenSpec change directory. Its
`Workspace:` record binds a tree, branch, and writer; its state says whether it may be continued; a `Resume checkpoint`
records what a pause left behind. None of the three is plan content, yet all three live in the change's `tasks.md`.

The costs, and the state they meet today:

- A unit without a change directory has no fixed home for them. The procedure sends it to "its hand-off"
  (`.opencode/skills/pause-resume-unit/SKILL.md:27-28`; `AGENTS.md:313-315`; `docs/contracts/process-bindings.md:19`),
  and no hand-off path exists outside a change directory: every path whose name carries hand-off or handover at the base
  revision is under `openspec/changes/` (claims table row 1).
- Nothing verifies that unit's record: the workspace-record verification enumerates live and archived `tasks.md` paths
  alone (`scripts/commit-hygiene.py:1759`).
- The claim cannot stand in for the name: it holds a change name or `none` for a unit with no change dir
  (`scripts/commit-hygiene.py`, `CLAIM_NONE` in `resolve_assignment`).
- A pause is not self-limiting. The procedure never removes the checkpoint at a resume, and six `tasks.md` carry the
  header today, all archived (claims table row 2) — so "has a checkpoint" is not "is paused".
- A paused unit can be archived and still unfinished, because the archive follows CI and precedes the merge
  (`.opencode/skills/ship-change/SKILL.md:25`), so excluding archived changes would drop a real pause.
- A real session, opened on the primary checkout, could not establish which unit to continue; it continued only after
  the owner supplied the unit's **name**, not its tree path. That unit has since been archived and its worktree removed
  (claims table row 5), so the incident is the owner's account while the archive is the record the shape leaves behind.
  `/resume-unit` takes no unit today (`.opencode/commands/resume-unit.md:5`), so no pasteable command carried the name.
- A checkpoint is a snapshot of one pause. The archived unit shows the shape: its checkpoint's `Workspace` field names
  the writer its record names and records the prior writer as handed over from (claims table row 6). A copy can be
  right, and it is still a copy — a snapshot cannot carry a later handover, so the record owns the state.
- `session_move` takes no permission action, so only the procedure bounds it
  (`docs/decisions/2026-10-31-session-move-permission.md`).

## Goals / Non-Goals

**Goals.**

- One home for a unit's workspace record, state, and checkpoint, for every kind of unit (ADR-0029).
- A session on the primary continues a paused unit by name, with no hint from the owner.
- A bare `/resume-unit` continues the only paused unit, offers the set when several exist, reports when none does, and
  never treats an unknown record as a pause.
- A missing record, a broken `tasks.md` reference, or an unreadable tree fails loudly instead of reading as clean.

**Non-Goals.**

- Creating, deleting, or re-branching a working tree, or moving a unit's work between trees.
- Taking over a unit whose writer has not handed over; the existing handover rules are unchanged.
- Choosing automatically among several paused units.
- Migrating a unit other than this change's own.
- Distributed locking, or discovery across other clones.
- Reworking every checkpoint field (the parked `Stage` correction stays separate).

## Decisions

**1. One record per unit, at `docs/units/<unit>/unit.md`.**

- Chosen: the directory is the unit's name — the branch name for a new unit — and the file inside it is `unit.md`; the
  file carries the single `Workspace:` line, the unit state (`active`, `paused`, `closed`), and the `Resume checkpoint`
  while one exists. A later per-unit artifact can sit beside it without moving the record; this change adds none. It is
  written when the workspace is established, before the first content write. It is never archived; `closed` marks it
  historical. ADR-0029 records the decision and the rejected alternatives.
- Rejected — two locations (`tasks.md` for a change unit): it keeps unit state coupled to having an OpenSpec change and
  branches every reader on the unit's kind.
- Rejected — extending the claim: a second identity source, with its own guard and migration.
- Rejected — a registry file: it duplicates `git worktree list` and the change directories.

**2. The move is a move, not a copy.**

- Chosen: a change's `tasks.md` keeps a reference to the unit record; it carries no copy of the record's fields. A unit
  with two live records is a conflict to report, never a preference to pick.
- Rejected — leaving both in place for a transition: the record is then written in two places, and one of them is the
  snapshot the resume must not treat as authority.

**3. The unit state is `active`, `paused`, or `closed`; a record without one is unknown.**

- `paused` is written when the pause records its checkpoint.
- `active` is written after the resume's state verification and handover, at the point work actually continues; a resume
  that stops on a disagreement, a handover, or an open decision leaves the state `paused`.
- `closed` is written when the unit is delivered, rejected, or abandoned. For a unit that ships it is written in the
  commit before its merge — the archive commit for a change unit, its final commit otherwise — because a write after the
  merge cannot reach the branch it merges. For a unit that does not ship it is written in its own tree before the branch
  is released, and that write is local: no merge carries it. Archive alone never closes a unit, because the merge and
  the tree's release follow it.
- A record without a state — every record written before this decision — reads as **unknown**: it is reported, it is not
  a candidate for automatic selection, and it does not prove a pause never existed.
- The checkpoint keeps pause facts only; its `Workspace` field points at the record rather than restating the writer.
- Once the resume continues, the checkpoint is frozen as the snapshot the pause wrote; only the record's state changes
  afterwards.

**4. Discovery enumerates the trees' assignments, not checkpoint headers.**

- Source of truth for the trees: `git worktree list --porcelain`, the registry `AGENTS.md` already names.
- For each registered tree: its branch, its claim (read through that tree's own git path), and the unit record the
  branch or claim assigns.
- A record is assigned to a tree when its `path` names that worktree's top level and its `branch` names the branch the
  tree has checked out; the claim is consulted only to contradict that, never to establish the name. This is the rule
  that covers a unit without a change directory, whose claim is `none`.
- A record found in a tree whose branch and claim assign a different unit, or a historical record carried into another
  tree, is not a candidate.
- An archived change directory does not exclude the unit: the record is outside it, and an archived-but-unmerged unit
  can still be paused.
- Rejected — scanning every `## Resume checkpoint` occurrence: an archived change's frozen checkpoint is history, and a
  header alone does not prove an open pause.

**5. The resolver is a read-only script with a machine-readable result.**

- `scripts/paused-units.py` prints one JSON document: `candidates` (unit, tree, branch, record path), `unknown` (records
  whose state is legacy or unreadable), `errors`, and `complete`.
- A complete enumeration exits zero whatever the number of candidates; an unreadable tree, an unresolvable assignment,
  or a contradiction makes the result incomplete or fails the run, and an incomplete result never selects a unit
  automatically.
- Rejected — the skill performing the reads itself: a missed tree or a wrapped header reads exactly like "no paused
  unit", and nothing would fail. `scripts/test-paused-units.py` carries the controls, registered as `testPausedUnits`
  beside `testReviewRouting`.
- Rejected — adding a lock or a liveness proof: the resume re-verifies state before the move and before any write, and
  the handover stays a discipline.

**6. One, several, none — with no hidden priority for the current tree.**

- Exactly one candidate: the session reports it and runs the ordinary resume procedure for it.
- More than one: the session presents each candidate with its tree and state and ends the turn for the owner's choice;
  it continues none until that choice arrives.
- None: the session reports that no paused unit was found and stops. A unit left waiting with no checkpoint is still
  continuable through a named or an explicitly contextual request, not through a bare command.
- An assignment whose state cannot be read blocks the automatic choice as an incomplete set does: one paused candidate
  beside one unresolved assignment selects neither, because the paused set is not established.
- The current tree's unit is a candidate like any other.

**7. A named unit is resolved, not assumed.**

- `/resume-unit <unit>` resolves the name through the same resolver: exactly one assignment with its record; then the
  membership check, the optional `session_move`, and a re-verification of the path and branch.
- An unknown name, several assignments of the same name, an unreadable tree that prevents uniqueness, or a `closed`
  record with no live assignment stops with what was established.
- An open pause is **not** required: the resume procedure already supports an unfinished unit with no checkpoint. The
  named form does not reopen a closed unit.

**8. The session moves only to a verified tree.**

- The move follows resolution and membership verification, and the resume re-verifies the absolute path and branch in
  the new tree before reading or writing anything.
- The move is target selection, not writer handover and not an answer to a pending decision.

**9. The checks follow the record, and they check the link.**

- `verifyWorkspaceRecords` judges each live unit's record and the `tasks.md` reference: a missing record, a reference to
  a missing or differently named unit, or a malformed record fails.
- The pre-commit guard reads the index copy of the record and the reference, keeping its identity, tree, branch, and
  merge-exemption judgments.
- The archive checks stay on the change directory and its tasks, and the three ranges the delta's requirement states
  govern them. The ranges are stated once, in that requirement, so this decision does not restate them.
- An archive dated on or after the record boundary that landed before this change is acknowledged by name, the mechanism
  the existing gate already uses; `2026-11-03-require-marker-ends-rule` is one (claims table row 6), so a same-day
  archive created under the old contract keeps its inline record.
- A deletion must not pass: the verification enumerates the assignment and asserts the record and the reference exist,
  rather than only validating the records it happens to find.

**10. Migration is bounded and does not invent a tool.**

- This change migrates its own record first, then removes the fields from its `tasks.md`.
- A legacy unit migrates on a named resume, in its own tree: read the legacy record, verify the tree, establish the
  writer handover, write the record at `docs/units/<unit>/unit.md`, carry its checkpoint across unchanged, remove the
  fields from `tasks.md` leaving the reference, and verify both sides with no second live record. A resume that stops
  before the handover migrates nothing.
- No mass migration script: the old location is an input to a unit's own move, not a second supported location.

**11. Resolution gets its own requirement.**

- The verification requirement's title covers the state verification, not which unit is continued. Discovery, the named
  form, the one/several/none outcomes, the move, and the legacy rule therefore take a new `agent-skills` requirement.
- The state vocabulary and its three producers take a second, because pause, resume, and delivery each write one state
  and the verification requirement's title covers none of them.

## Risks / Trade-offs

- `docs/units/` grows by one directory per unit and is never pruned; the `closed` state keeps that harmless.
- A unit whose record still sits in its `tasks.md` — a tree resumed before it migrates — reads as unknown and is not
  selected automatically; that is the intended loud failure, and it resolves when the unit is resumed or re-paused.
- Discovery costs one pass over the registered trees per bare invocation, bounded by their number.
- The archive carries a frozen copy of its `tasks.md`; after this change that copy references a record that lives
  outside it, so a reader of an archived change follows one pointer rather than reading the fields in place.

## Migration

The change moves its own record, then removes the fields from its `tasks.md`, and verifies both sides. Every other unit
keeps its record where it is until its own named resume moves it, under Decision 10; until then a `tasks.md` record is
read as legacy with an unknown state, never as a pause, and it blocks the automatic choice while that assignment is
unresolved. The record verification's boundary is this change's introduction date for the reference contract, and the
existing `2026-11-02` boundary keeps governing the archives written to the old one; an archive of the boundary date that
landed before this change is acknowledged by name.

## Claims table

Base revision: `87e9f565` (`git merge-base HEAD origin/main`).

| #   | Claim                                                                        | Source                                                                                                                              | Result                                                                                                                     |
| --- | ---------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| 1   | Every path named for a hand-off lives inside a change directory              | `git ls-tree -r --name-only $BASE \| rg -i "handover\|hand-off\|handoff" \| rg -v "^openspec/changes/"`                             | no output; the unfiltered count is 16, all under `openspec/changes/`                                                       |
| 2   | Six `tasks.md` carry a checkpoint header, and all are archived               | `git grep -l "^## Resume checkpoint" $BASE -- "*/tasks.md"`                                                                         | 6 paths, each under `openspec/changes/archive/`                                                                            |
| 3   | The workspace-record verification enumerates `tasks.md` paths only           | `rg -n "def workspace_record_paths" -A 18 scripts/commit-hygiene.py`                                                                | live change dirs plus archives dated on or after `WORKSPACE_RECORD_ARCHIVE_SINCE`                                          |
| 4   | The claim holds a change name or `none`                                      | `rg -n "CLAIM_NONE" scripts/commit-hygiene.py`                                                                                      | `resolve_assignment` treats `none` as no change                                                                            |
| 5   | One tree is registered, and the sample unit's change is archived at the base | `git worktree list --porcelain`; `git ls-tree -d --name-only $BASE openspec/changes/archive/`                                       | one worktree, the primary on `discover-paused-units`; `2026-11-03-require-marker-ends-rule` archived at the base           |
| 6   | The archived unit's checkpoint names the writer its record names             | `git show $BASE:openspec/changes/archive/2026-11-03-require-marker-ends-rule/tasks.md`                                              | line 3 and the checkpoint's `Workspace` field both name `ses_e612eefc…`, which records `ses_e653ab5c…` as handed over from |
| 7   | The archive precedes the merge                                               | `rg -n "archive" .opencode/skills/ship-change/SKILL.md`                                                                             | "The archive … always follows CI, never precedes it" and "CI green, the change archived …, the pull request merged"        |
| 8   | `session_move` takes no permission action                                    | `rg -n "session_move" docs/decisions/2026-10-31-session-move-permission.md`                                                         | the tool carries `options` with no `permission` assertion, unlike `worktree_remove`                                        |
| 9   | `/resume-unit` takes no unit today                                           | `sed -n "5p" .opencode/commands/resume-unit.md`                                                                                     | "Continue the unit this tree holds."                                                                                       |
| 10  | The delta validates                                                          | `openspec validate --all`                                                                                                           | 27 passed, 0 failed                                                                                                        |
| 11  | Fourteen archives are dated on or after the existing record boundary         | `git ls-tree -d --name-only $BASE openspec/changes/archive/ \| sed "s#.*/##" \| rg "^2026-11" \| awk '$0 >= "2026-11-02"' \| wc -l` | 14, each carrying the record inside its `tasks.md`                                                                         |

**Dismissals**, from `python3 scripts/claim-scan.py --base origin/main`, one reason per kind:

- **absolute** — each candidate is either the repository fact a row states, reproduced by that row's command, or a
  quantifier over this change's own rules; no candidate stands as a repository claim without a row behind it.
- **count** — each is a task or section identifier, a field of the unit's recorded review-ledger row, or the figure a
  row states and its command reproduces.
- **path** — a cited source is reproduced only where a row cites it with a result, and a planned path names a file or a
  directory this change creates.
- **cross-ref** — each candidate is a phrase of this change's own rules the scanner reads as a reference, or a citation
  a row already carries.
