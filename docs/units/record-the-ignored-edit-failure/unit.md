# Unit: record-the-ignored-edit-failure

Workspace:
`unit=record-the-ignored-edit-failure; path=/Users/afanasyev/Development/axon-showcase; branch=record-the-ignored-edit-failure; writer=ses_e4add21daffe0vW0OISMmtQS3i`

State: closed

A docs-fix unit with no change directory. The owner's authorization of 2026-11-08 ("Записывай") names the one direct
edit: append a process-defect record to the merged unit `generalize-process-defects-2026-11-08` for the incident where a
failed edit script's exit status was not read and the commit proceeded. The unit therefore runs the single review pass
that the lifecycle's named-direct-edit route allows, at the implementation boundary.

`Packs: none` — one record appended under `docs/units/`, no code, build, or deployment surface.

## Why the edit is a separate unit

The incident was observed during delivery but not recorded before that unit's merge, so it cannot ride the unit's own
pull request. `AGENTS.md`'s docs-refresh convention makes a docs edit the change did not cause — or one surfaced only
after the change is done — its own unit, forked from `main`. The record is the artifact; no other edit is owed.

## The recorded defect, in short

An edit script applying the two pull-request links to the `proposed` `Decision` lines raised on the second replacement
(its anchor had been reflowed by the formatter), and the shell chain still committed and pushed with only the first link
written. The verification the unit had specified — a `grep` for the link — showed `0` matches for one file; the defect
was fixed by a follow-up commit on the same pull request before the merge. The class is `AGENTS.md`'s "never chain an
edit to a commit without reading the edit's result"; this record is its instance.

The edit appends a `process-defects.md`, which the guard's branch check does not judge — that check reads a staged
`unit.md` — so `--staged` passes on this unit's staged set and no `--no-verify` exception is needed. The parked guard
limitation (`link-generalization-decision-records#1`) is the adjacent case, and this unit does not exercise it.
