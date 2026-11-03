# Proposal

## Why

A unit's `Workspace:` record, its pause state, and its `Resume checkpoint` are unit state, but they live in the change's
`tasks.md` — and a unit with no change directory has no fixed place at all. A session that opens on the primary checkout
therefore cannot find the paused unit: a real one continued only after the owner named it, because the unit sat in a
sibling worktree. `/resume-unit` also takes no unit, so the pause report cannot hand over a command the owner can paste.

## What Changes

- Every unit's state moves to one record at a fixed path, `docs/units/<unit>/unit.md` — a directory per unit, so a later
  per-unit artifact can sit beside the record without moving it: its `Workspace:` line, its state (`active`, `paused`,
  `closed`), and its `Resume checkpoint`. The change's `tasks.md` keeps the plan and references that record instead of
  carrying its fields. ADR-0029 records the decision.
- The workspace-record verification and the pre-commit guard are retargeted at the record and at the `tasks.md`
  reference, so a missing record or a broken reference fails where a missing record in `tasks.md` failed before.
- `/resume-unit` gains an optional unit name: a named unit is resolved among the repository's registered trees and the
  primary checkout, its membership is verified from the owning records, the session moves to its tree when it is
  elsewhere, and the existing resume procedure runs.
- Without a name, the command discovers the units in the `paused` state through one resolver over the registered trees,
  continues the only one, presents the set for the owner's choice when several exist, and reports when none does. A
  legacy record — one still in a `tasks.md` — is reported as unknown and is not selected automatically.
- `scripts/paused-units.py` enumerates the assignments read-only, with `scripts/test-paused-units.py` carrying the
  controls and a `check` test task running them.
- The pause report names the unit and the tree and hands over `/resume-unit <unit>`.
- The unit record carries its state through the unit's life: the pause writes `paused`, the resume writes `active` after
  its verification, and delivery writes `closed` in the commit before the merge — or in the unit's own tree, for a unit
  that does not ship.
- A legacy unit migrates on a named resume, in its own tree, with its checkpoint carried across and its `tasks.md`
  reduced to the reference.
- `README.md` gains the named form and the discovery behavior; `AGENTS.md`, the bindings, the skill, and the command
  describe the single record; the checkpoint-lifecycle idea this change implements is removed from `docs/ideas.md`, and
  the `Stage` idea it does not implement stays parked.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/agent-skills`: the pause writes into the unit record and the report names the unit and its resume
  command; the continuation establishes which unit continues — by name or by discovery — before it verifies the state,
  with the one-of-many outcomes and the legacy rule specified.
- `showcase/quality/commit-hygiene`: a unit's workspace record lives in its unit record, and the verification, the
  reference from `tasks.md`, and the pre-commit guard follow that record; the requirement's subject widens from a change
  unit to any unit.

## Impact

- New: `docs/units/<unit>/unit.md` records, `scripts/paused-units.py`, `scripts/test-paused-units.py`, ADR-0029.
- Changed: `.opencode/skills/pause-resume-unit/SKILL.md`, `.opencode/commands/resume-unit.md`,
  `.opencode/skills/ship-change/SKILL.md`, `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`,
  `build.gradle.kts`, `AGENTS.md`, `docs/contracts/process-bindings.md`, `README.md`, `docs/ideas.md`, and the two delta
  specs.
- Migration: this change's own record moves first; another unit migrates on its own named resume, with its checkpoint
  carried across, and a `tasks.md` record reads as legacy until then.
- No runtime, API, or deployment change: the resolver reads `git worktree list`, each tree's branch and claim, and each
  tree's unit records.
