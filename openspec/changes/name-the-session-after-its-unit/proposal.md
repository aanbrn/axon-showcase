# Proposal

## Why

A session's title is generated from its first prompt, so a session that resumes a unit is named after the action — a
bare `/resume-unit` produced "Resuming a paused unit" — rather than after the unit, and the owner reports that a session
which begins a further unit keeps the previous unit's title. The owner cannot tell from the session list which unit a
session is working on.

## What Changes

- `AGENTS.md` — the lifecycle's Workspace rules carry one imperative sentence stating the rule and pointing at the
  `showcase/quality/agent-skills` requirement that owns its conditions.
- `.opencode/skills/begin-unit/SKILL.md` — a step referencing that policy: after the workspace and record are
  established, the applicable claim step has run, and before the concluding report, set the session's title to the
  established unit's name, including when the session previously worked on another unit.
- `.opencode/skills/pause-resume-unit/SKILL.md` — a step referencing that policy: after the unit is verified and any
  writer handover or legacy migration is done, and before continuing the work, set the session's title to the unit's
  name.
- `showcase/quality/agent-skills` — a delta requirement stating the owner-visible outcome, the points it synchronizes
  at, and that a failed rename is reported rather than blocking the work.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — a session's title names the unit it works on, synchronized when a unit becomes the
  session's current unit.

## Impact

- **Skills and guidance**: `AGENTS.md`'s lifecycle Workspace rules, and a reference-bearing step in the `begin-unit` and
  `pause-resume-unit` skills.
- **Specs**: one added requirement in `showcase/quality/agent-skills`.
- **Code and build**: none — no source file changes, no new dependency, no build or deployment change.
- **Docs**: `docs/ideas.md` gains the parked IntelliJ-configuration entry this unit's Begin surfaced — the IDE upgrade
  left a stale configuration directory beside the new one, so the tree's preparation reported `Tree NOT ready` until it
  was moved away.
- **Verification**: the mechanism relies on the harness's session rename, verified on 2026-11-08 on a disposable main
  session (an explicit title applied, read back, and survived a further prompt turn). No repository gate reads session
  titles.
