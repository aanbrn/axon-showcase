# Require the owner's authorization to start a unit and to implement its proposal

## Why

The lifecycle's procedure begins at "Propose the change", and the only approval stop it names is the review gate's
implementation-stage manual pass, so a unit can begin on the agent's own judgement and reach implementation before the
owner has seen its shape. The owner's rule: a discussion of what to do is not authorization to do it, a unit starts only
on the owner's explicit instruction, and a proposal passes manual review before its implementation.

## What Changes

- The lifecycle gains a start gate: a unit begins only on the owner's explicit instruction or authorization, and an
  agent that believes it has understood what is needed states the proposal and waits rather than beginning.
- The proposal gains its own manual-review stop: implementation begins only after the owner has reviewed and approved
  the proposal artifacts, so the review gate asks at both boundaries — the proposal and the implementation.
- The existing shipping stop is unchanged: no commit, push, pull request, archive, or merge without the owner's approval
  and their delivery request.
- The parked `docs/ideas.md` entry for this rule is removed by the change that implements it.

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — a new requirement states the start gate and the proposal-approval stop; the
  mutating-command requirement gains those two stops among the lifecycle it requires a command to state.
