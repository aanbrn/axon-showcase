# Make the unit lifecycle explicit in the agent process

## Why

The process defines a unit of work as its base unit, but no artifact states the lifecycle that unit follows: the
governing procedure is titled for the change form, mixes unit-generic steps with change-specific ones, and leaves the
OpenSpec change's propose, apply and archive implicit apart from the archive's placement inside shipping. Workspace
establishment is split between a sibling-tree command and prose, and no entry point declares the lifecycle point it
serves.

## What Changes

- Add ADR-0030 recording the unit lifecycle as the enclosing process and the OpenSpec change as an optional workflow
  contained within it.
- Replace `AGENTS.md`'s mixed lifecycle procedure with an explicit model: seven stages, their transitions, exits, and
  approval boundaries, with change operations marked as conditional.
- Add the `begin-unit` skill and the `/begin-unit` command; retire `/new-worktree` as the Begin entry while keeping
  `scripts/new-worktree.sh`.
- Make the archive conditional on a change directory in the root completion criteria and in the `ship-change` skill, and
  reconcile the publication prohibition with its existing narrow exceptions.
- Declare each project-authored command's lifecycle entry, and verify the projection with a `verifyCommandLifecycle`
  check.
- Align `review-gate`, `pause-resume-unit`, and `ship-change` with the model's vocabulary, and resolve the scrutinized
  records: `unit.md` initialization at Begin, and `closed` no longer reading as proof of merge or settlement.
- Restate the README's process narrative around the unit lifecycle, naming the existing OpenSpec diagram as its nested
  change path.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

`showcase/quality/agent-skills` — a new enclosing-lifecycle requirement, a new Begin-skill-ownership requirement, a new
command-declaration requirement, and modified requirements for the unit record's state and the mutating command's
lifecycle preflight.

`showcase/quality/commit-hygiene` — a new requirement that the standard `check` verifies the command lifecycle
projection.

## Impact

- `AGENTS.md`: the lifecycle section is replaced; the review-gate and ship sections are aligned; the tooling-placement
  example changes to `new-worktree.sh ↔ /begin-unit`.
- New: `docs/adr/0030-*.md`, `.opencode/skills/begin-unit/SKILL.md`, `.opencode/commands/begin-unit.md`, a
  `verifyCommandLifecycle` task with its rules and tests, and `docs/units/` records for this unit.
- Removed: `.opencode/commands/new-worktree.md` (replaced), and the replaced lifecycle prose.
- `docs/contracts/process-bindings.md`: the `lifecycle`, `workspace-record`, and `branch-refresh` values, and the
  consumer trace.
- `README.md`: the process narrative and one slash-command row.
- The build gate gains the `verifyCommandLifecycle` task and its tests, so `check` runs one more verification. No
  dependency, API, deployment, or service behavior change.
