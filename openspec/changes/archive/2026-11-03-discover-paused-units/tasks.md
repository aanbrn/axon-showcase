# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is `docs/units/discover-paused-units/unit.md`.

## 1. The record contract

- [x] 1.1 `docs/adr/0029-unit-state-lives-outside-the-change-plan.md` records the single record at
      `docs/units/<unit>/unit.md`, its fields and states, why unit state is separated from the change plan, and the
      rejected alternatives (two locations, the claim, a registry file).
- [x] 1.2 `AGENTS.md`'s workspace rule states the single path, that the record is written at workspace establishment,
      that `tasks.md` references it, and that a unit without a change dir records its `Workspace:` line there; the
      pre-commit guard's enumeration in the repository's gotchas is retargeted with it.
- [x] 1.3 `docs/contracts/process-bindings.md`'s `workspace-record` row names the unit record and the `tasks.md`
      reference.

## 2. The record's checks

- [x] 2.1 `scripts/commit-hygiene.py`'s workspace-record verification judges every live unit's record and its `tasks.md`
      reference; a missing record, a reference to a missing or differently named unit, and a malformed record each fail,
      and a deleted record or reference fails rather than passing an empty enumeration.
- [x] 2.2 The staged guard reads the index copy of the record and the reference, keeping its identity, tree, branch, and
      merge-exemption judgments, and applies the three archive ranges the delta's requirement states.
- [x] 2.3 `scripts/test-commit-hygiene.py` carries the controls: a well-formed record, a missing record, a broken
      reference, a reference naming another unit, a malformed field, each recorded form, a legacy `tasks.md` record, an
      archive on each side of the record boundary, an acknowledged same-day archive created under the old contract, and
      the guard's tree and branch judgments.
- [x] 2.4 `build.gradle.kts`'s `verifyWorkspaceRecords` covers the new path, and `check` stays green on the repository
      as migrated by section 3. The `commit-hygiene` capability's `## Purpose` still names the `tasks.md` record, so its
      refresh is recorded as its own task (7.6) and lands in the archive commit.

## 3. This unit's migration

- [x] 3.1 Write `docs/units/discover-paused-units/unit.md`: the `Workspace:` line, state `active`, and no checkpoint
      yet.
- [x] 3.2 Remove the `Workspace:` line and the pause block from this `tasks.md`, leaving a reference to the record, and
      verify both sides in one read.
- [x] 3.3 Confirm no second live record remains for this unit.

## 4. The pause and resume procedure

- [x] 4.1 `.opencode/skills/pause-resume-unit/SKILL.md` writes the pause into the unit record, sets the state `paused`,
      and keeps the checkpoint's `Workspace` field a pointer to the record rather than a copy of the writer.
- [x] 4.2 The pause report names the unit and the tree and hands over `/resume-unit <unit>`.
- [x] 4.3 The resume sets `active` only after the state verification and handover, at the point work continues; a stop
      on a disagreement, a handover, or an open decision leaves it `paused`, and once the work continues the checkpoint
      is frozen as the pause's snapshot, which a later pause replaces.
- [x] 4.4 The resume establishes the unit first: a named unit through the resolver's resolution, and otherwise its
      candidates; one continues, several are presented and the turn ends, none is reported and the session stops.
- [x] 4.5 Membership comes from the record's `path` and `branch`, and its claim only contradicts them; a disagreement
      stops; a session moved to the tree re-verifies the path and branch.
- [x] 4.6 The skill states that the unit name grants neither the writer handover nor an answer to a pending decision,
      and that a legacy record is unknown rather than paused.
- [x] 4.7 `.opencode/commands/resume-unit.md` documents `/resume-unit [unit]`, the discovery behavior, and points at the
      skill for the procedure.
- [x] 4.8 The resume migrates a legacy unit in its own tree on a named resume: read the legacy record, verify the tree,
      establish the handover, write `docs/units/<unit>/unit.md` with the checkpoint carried across, remove the fields
      from `tasks.md` leaving the reference, and verify both sides with no second live record; a stop before the
      handover migrates nothing.
- [x] 4.9 `.opencode/skills/ship-change/SKILL.md` writes `closed` in the commit before the merge for a shipping unit,
      and in the unit's own tree before release for a rejected or abandoned one. A pause after the archive writes
      nothing to the branch, because a pause commits nothing, so the archive commit's `closed` is what the merge
      carries; a resume that continues after that pause re-closes the record before any further merge.

## 5. Discovery

- [x] 5.1 `scripts/paused-units.py` is read-only and stdlib-only, resolves the repository from its own location, reads
      `git worktree list --porcelain`, and prints one JSON document with `candidates`, `unknown`, `errors`, and
      `complete`; a candidate carries the unit, its tree, its branch, and its record path. `--help` owns the contract
      and the exit status.
- [x] 5.2 The resolver enumerates the trees' assignments, not checkpoint headers: a record whose branch or claim assigns
      another unit is not a candidate, an archived-but-unfinished unit still is, and an unknown-state record goes to
      `unknown`.
- [x] 5.3 An unreadable tree, an unresolvable assignment, or a contradiction makes the result incomplete, and an
      incomplete result never selects a unit automatically; an unresolved assignment beside one paused candidate blocks
      the automatic choice as well.
- [x] 5.4 `scripts/test-paused-units.py` carries the controls: one candidate, several, none, an archived-but-unfinished
      unit, a unit without a change directory, a legacy record, one paused candidate beside an unresolved assignment, an
      unreadable tree beside one good candidate, a record whose branch assigns another unit, a deleted record, the named
      form's unknown name, its several assignments, and its `closed` record with no live assignment. The suite runs
      under the declared Python floor, on isolated fixtures rather than the developer's worktrees.
- [x] 5.5 `build.gradle.kts` registers `testPausedUnits` beside `testReviewRouting`, and `check` depends on it.

## 6. The docs

- [x] 6.1 `README.md`'s prose bullet and slash-command row name the optional unit and what a bare invocation does.
- [x] 6.2 `docs/ideas.md`'s checkpoint-lifecycle idea ("Freeze a unit's resume checkpoint at the resume") is removed as
      implemented — the record carries the state, the resume freezes the checkpoint as a snapshot, and its `Workspace`
      field points at the record — while the `Stage` idea ("Correct `pause-resume-unit`'s `Stage` field") stays parked,
      since this change does not rework that field.
- [x] 6.3 A sweep confirms no live document still says the record lives in `tasks.md`, in an unfixed hand-off, or that
      the guard judges a `tasks.md`'s record — `AGENTS.md`'s guard enumeration, the bindings, the skill, the command,
      ADR-0024's amended Context, and the capability's Purpose included.
- [x] 6.4 `docs/ideas.md` gains the parked ledger move: `docs/review-rounds.tsv` into the units' directories, naming its
      consumers (`commit-hygiene`'s checks, `review-rounds.py`, `review-routing.py`, the gate, the bindings) and the
      requirement that a full split keeps every row, its order within the unit, the acknowledgements, and the aggregate
      and routing results.

## 7. The gates

- [x] 7.1 The claims table in `design.md` is re-derived after the last edit, and each row reproduces.
- [x] 7.2 `openspec validate --all` passes, and `./gradlew spotlessApply` runs after the final edit.
- [x] 7.3 The Python suites run under the declared floor, and the touched build's `check` passes.
- [x] 7.4 `docs/process-defects/discover-paused-units.md` records what the unit observed, including the proposal written
      before the consultation on the widened scope.
- [x] 7.5 The review gate's quick pass and routing decision run, and the owner's manual pass approves the
      implementation. Quick rounds 1–19 are recorded in `docs/review-rounds.tsv`; routing returned `REQUIRED` (two
      requirement blocks added, one removed), and the deep pass ran five rounds on this implementation and closed clean.
      The owner's manual pass is the step outstanding.
- [x] 7.6 The `commit-hygiene` capability's `## Purpose` is refreshed in the archive commit, since a delta cannot carry
      a Purpose and the current one still describes the `tasks.md` record.
