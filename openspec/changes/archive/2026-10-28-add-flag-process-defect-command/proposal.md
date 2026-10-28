# Proposal

## Why

The owner sometimes notices a process defect the agent's own session did not. Today the recording workflow starts when
the agent notices (`docs/process-defects/<unit>.md`), so an owner's observation has no path from signal to established
fact: `/ideas` stores a note unverified, `/generalize-process-defects` analyzes records that already exist, and a plain
prose remark leaves the agent to transcribe the owner's interpretation as if it were evidence. The verification step —
the one that turns "you missed something here" into a record carrying a reproduced fragment — is the missing mechanism.

## What Changes

- Add `.opencode/commands/flag-process-defect.md`: the owner-facing entry point that resolves the episode, compares the
  expected action against the observed sequence within that episode, and reports exactly one of three outcomes —
  confirmed, refuted by the available evidence, or not establishable from it.
- Add the `/flag-process-defect` row to `README.md`'s slash-command table.
- Add the owner-reported path to both `README.md` statements of the recording loop: the Self-Learning Loop bullet, which
  already carries the merge-append path, and the "Recording, then generalizing" bullet under _What the Agent Automates_.
- Extend `AGENTS.md`'s "Record process defects" trigger with the owner-reported path, pointing at the command rather
  than restating its procedure.
- Add one scenario to the process-defect requirement of `showcase/quality/agent-skills` (a `MODIFIED` delta), stating
  the three verification outcomes and what a confirmed record carries.
- Write the unit's own record, `docs/process-defects/add-flag-process-defect-command.md`.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the requirement "Process defects are captured during a unit and generalized
  separately" gains the owner-reported signal and the verification outcome that decides whether a record is written.

## Impact

- New: `.opencode/commands/flag-process-defect.md`.
- `README.md` (one table row, and a clause in each of the recording loop's two statements), `AGENTS.md` (the recording
  workflow's trigger), `openspec/specs/showcase/quality/agent-skills` (via the delta, at archive),
  `docs/process-defects/add-flag-process-defect-command.md` (the unit's record).
- No production code, API, dependency, build, test, or deployment change: the command is a definition the agent
  executes, and the only consumer of the new file is the OpenCode definition loader that the build's
  definition-inventory probe already reads.
- The command writes a record only, so a confirmed outcome enters the existing generalization queue as an unanalyzed
  record and needs no new analysis path.
