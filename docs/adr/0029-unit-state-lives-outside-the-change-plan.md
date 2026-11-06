# ADR-0029: A unit's state lives in its own record, outside the change plan

Date: 2026-11-03

Status: Accepted

## Context

A unit of work is one branch in one working tree, and it may or may not carry an OpenSpec change directory. Three facts
about it are unit state rather than plan content: the `Workspace:` record that binds a tree, branch, and writer; whether
the unit is active, paused, or closed; and, while paused, a `Resume checkpoint`.

Today all three live in the change's `tasks.md` — for a unit that has one. A unit without a change directory is told to
write them into "its hand-off", and no path names that hand-off: every hand-off file in the repository sits inside a
change directory, so that unit's record has no home. Nothing verifies it either, because the workspace-record check
enumerates change directories alone.

The coupling has three costs. A unit's identity depends on having an OpenSpec change, though the lifecycle allows units
without one. A checkpoint cannot be found by anyone who does not already know which tree to look in, so a session opened
on the primary checkout has nothing to resume from. And the record cannot outlive the change directory: the archive
moves that directory before the unit is merged, so an archived-but-unfinished unit's state travels with a directory
whose location the discovery would have to special-case.

## Decision

Every unit writes one record, at a fixed path: `docs/units/<unit>/unit.md`. It holds the unit's single `Workspace:`
line, its state (`active`, `paused`, or `closed`), and its `Resume checkpoint` when one exists. A change directory's
`tasks.md` holds the implementation plan and **references** that record; it does not carry a copy of its fields.

- The record is written when the unit's workspace is established, before its first content write, not first at a pause.
- The directory is the unit's name, which is also its branch name for a new unit, and the record sits at `unit.md`
  inside it. A later per-unit artifact can then sit beside the record without moving it; this decision adds no such
  artifact and moves none.
- The record is never archived with the change; it stays at its path after the unit closes, and its `closed` state is
  what marks it historical. A merged unit therefore leaves one small file behind.
- The pre-commit claim is unchanged: it still names the change a commit is assigned to, or `none` for a unit with no
  change directory. The unit's name comes from its record, not from the claim.
- The old location — `tasks.md` for units that predate this decision — is a **migration input**, not a second supported
  location. A unit is migrated when its writer moves the fields; until then its record is read as legacy with an unknown
  state, and discovery does not select it automatically.

Alternatives considered and rejected:

- **Two locations** (`tasks.md` for a change unit, `docs/units/` otherwise): it keeps the record coupled to having an
  OpenSpec change, branches every reader on the unit's kind, and leaves the archive case special-cased.
- **Extending the claim** to carry the unit's name and a pointer: the claim answers "which change may this commit
  carry?", and a second identity source would need its own guard and its own migration.
- **A registry file** listing units: it duplicates `git worktree list` and the change directories, and drifts.

## Consequences

- The workspace-record verification and the pre-commit guard are retargeted at the record and at the `tasks.md`
  reference; a missing record or a broken reference fails the same way a missing record in `tasks.md` did.
- Discovery reads one path per tree instead of branching on the unit's kind, and it can see a unit whose change is
  already archived.
- Existing units need a migration, which is a move rather than a copy: two live records for one unit are a conflict.
- `tasks.md` no longer carries unit state, so a reader of the plan does not see stale pause fields beside the tasks.
- `docs/units/` grows by one directory per unit and is never pruned; the `closed` state is what keeps the growth
  harmless, and a directory with one file is a shape a later per-unit artifact can join.

## Amendment — 2026-11-06

This amendment records the layering the Decision implements and the alternative it declines. It moves no record, adds no
field, and changes no check.

A unit is the unit of work; an OpenSpec change is one form it may take. The Context's "the record cannot outlive the
change directory" is superseded by the Decision: the record lives at a fixed path outside the change and stays there
when the change is archived, and what that sentence names is the address a record kept among the change's files would
lose, not the record's survival, which the Decision provides. The layering is why the unit's records live outside the
change: the `unit.md` record exists before the unit's first content write (it is written when the workspace is
established) and persists through archive and merge, while the sibling process-defect record is appended to after merge.

**Considered and declined: every unit is an OpenSpec change** — using `skip_specs` wherever no capability delta is owed,
with the record carried inside the change directory. It co-locates the files without removing the responsibilities: the
unit outlives its archive, so post-merge recording would write into the archive, and a paused unit's state would sit in
an artifact the workflow relocates.
