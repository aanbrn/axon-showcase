# Proposal

## Why

A `/consult` report's closing prompt was executed by the invoking agent before the owner decided on it: the report
arrived as a finding-shaped to-do list, the review-gate habit applied it, and only the agent's own disclosure caught the
violation. The command's step 7 already forbids applying a recommendation without the owner's decision, but it does not
require the turn to end after the report is presented, so nothing marks the boundary the moment a prompt arrives.

## What Changes

- Add a terminal handover to `/consult`'s step 7: after presenting the report, end the turn and ask for the owner's
  decision; a closing prompt, a named correction, or a next action is a proposition, and applying any of it needs a
  subsequent owner decision about that report.
- Add the matching decision point to the command: IF the report returns a recommendation, THEN present it, ask for the
  owner's decision, and end the turn.
- State in `technical-advisor.md`'s report contract that a requested prompt is delivered as a proposal to the owner,
  never as an instruction the invoking agent may run on the spot.
- Record the behavior in the `agent-skills` capability: the requirement "Independent technical consultation is available
  on demand" gains the terminal handover in its existing scenario "A recommendation is not authorization", and the
  requirement "A consultation report leads with its recommendation" gains the proposal-form prompt in its scenario "One
  next action is recommended"; every scenario name and the other scenarios stay unchanged.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the requirement "Independent technical consultation is available on demand" requires
  the invoking agent to end its turn after presenting the report and to start proposed work only after a subsequent
  owner decision (scenario "A recommendation is not authorization"); the requirement "A consultation report leads with
  its recommendation" requires a requested prompt to be delivered as a proposal (scenario "One next action is
  recommended").

## Impact

- `.opencode/commands/consult.md` — step 7, one decision point, and the direct-delegation paragraph.
- `.opencode/agent/technical-advisor.md` — one sentence in the report contract.
- `AGENTS.md` — the capture's general rule merged into the Structured Writing Standard, plus the stopping step at its
  two approval hand-backs (the change lifecycle's step 4 and the review gate's step 6).
- `openspec/specs/showcase/quality/agent-skills/spec.md` — via the delta at archive.
- No code, build, gate, or runtime behavior changes; the change is a procedure the agent follows.
