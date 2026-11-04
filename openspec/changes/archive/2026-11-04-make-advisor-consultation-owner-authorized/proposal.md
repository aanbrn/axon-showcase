# Make advisor consultation owner-authorized

## Why

The guidance lets a calling agent consult `technical-advisor` on its own initiative: `AGENTS.md` permits direct
delegation, `/consult` describes itself as "not the only path", and the `agent-skills` requirement's direct-delegation
clause carries only a handoff obligation. Using the advisor is the owner's prerogative — a consultation spends their
attention and an expensive model — so an agent SHALL propose one and SHALL NOT run one without the owner's
authorization.

## What Changes

- The advisor is invoked only on the owner's authorization: their `/consult` invocation or an explicit instruction.
- An agent that judges a consultation useful states the question and proposes it, and does not delegate on its own
  initiative.
- The sites that currently permit direct delegation are corrected together: `AGENTS.md`'s technical-advisor bullet, the
  `/consult` command, and the `technical-advisor` definition.
- The parked `docs/ideas.md` entry that proposed a mandatory consultation before a corrected unit's artifacts is
  withdrawn: it misdiagnosed the skipped step, which was the owner's manual review of the unit's shape, and its vehicle
  is the automatic consultation this change forbids.

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — "Independent technical consultation is available on demand" gains the owner-
  authorization precondition and a scenario for an agent that proposes rather than runs a consultation; "A consultation
  receives a focused handoff" narrows its direct-delegation clause to an owner-authorized delegation.
