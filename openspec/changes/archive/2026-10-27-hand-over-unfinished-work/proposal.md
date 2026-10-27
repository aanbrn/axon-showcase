# Proposal

## Why

The pause path already exists, but its trigger names only an owner-announced pause. In the reported incident an agent
concluded its session budget was exhausted, kept the unit moving one step per owner reply — seven replies — and only the
owner's own `/pause-unit` recovered it; the owner had to guess the procedure. The gap is the agent's own handover
decision, not a missing mechanism.

## What Changes

- **`AGENTS.md`** — the existing pause entry-point bullet names the agent's own decision that it cannot reliably
  continue the unit in the current session as a trigger, and keeps the procedure itself in the skill.
- **`.opencode/skills/pause-resume-unit/SKILL.md`** — the trigger gains that decision; the skill states that the agent
  pauses instead of continuing through requests to confirm each next work step, that an unmeasured session limitation is
  described as an assessment, and that the pause report names the saved checkpoint and `/resume-unit`.
- **`README.md`** — the existing pausing bullet names a session that judges it cannot reliably continue its work as a
  reason to pause.
- **`docs/process-defects/hand-over-unfinished-work.md`** — the defect this unit exists for, recorded under the new
  workflow.
- **`openspec/specs/showcase/quality/agent-skills/spec.md`** — via a delta: `MODIFIED` the pause entry-point requirement
  and the checkpoint requirement, adding the self-decided trigger, the no-confirmation-step rule, the assessment
  wording, and the report's `/resume-unit` clause.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the pause-and-resume procedure's trigger SHALL include the agent's own decision that
  it cannot reliably continue the unit in the current session, and a pause SHALL be recorded instead of continuing the
  unit through confirmation requests; the pause report SHALL name the saved checkpoint and the new session's
  `/resume-unit`, and an unmeasured session limitation SHALL be described as an assessment.

## Impact

- **Process and tooling**: one `AGENTS.md` bullet extension, one skill's trigger and contract, one README bullet, one
  process-defect record, and the `agent-skills` spec delta. No new command, agent, script, or gate.
- **Gates**: `spotlessApply`/`spotlessCheck` format the markdown, `openspec validate --all` checks the delta, the marker
  and hygiene checks cover the edited guidance, and the `build` job's OpenCode probe loads the edited skill.
- **The capability's `## Purpose`**: unchanged — it names the project-authored skills by member and no skill is added.
- **Code, builds, dependencies, deployments, tests**: unchanged.
