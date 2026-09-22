## Why

The boundary rule added by `extract-specd-rationale-from-agents-md` routes a rule whose rationale is normative in a spec
to a pointer, and covers the case where the specs describe a rule's subject only as an outcome by moving the mechanism
into the capability spec. It states the _move_ without a causal precondition, so it licenses moving a mechanism that
changes no scenario's outcome — internal control flow — into a spec. That gap cost a full propose→apply→review cycle: a
change (`spec-cache-fallback-failed-fetch-contract`, since abandoned) moved the "failed cached fetch counts as a miss"
contract into `gateway/rest-api`, then the review read the code and found both fallback branches emit `onError`, so the
contract decides which branch runs but changes no observable outcome. `resolve-recorded-intent-questions` had already
reached the same conclusion and declared `skip_specs` on that same contract.

## What Changes

- Add the precondition to the outcome-only clause in the `AGENTS.md` promotion-gate bullet: the mechanism belongs in the
  capability spec **only when changing it would change a scenario's outcome**, verified against the code before moving
  it.
- Mirror the same precondition into the clause's verbatim copy in `specs/showcase/quality/agent-skills/spec.md`, so the
  two do not drift.

## Capabilities

### New Capabilities

None — the precondition refines an existing clause.

### Modified Capabilities

- `showcase/quality/agent-skills`: the promotion-gate requirement's outcome-only clause gains the precondition, keeping
  the `AGENTS.md` copy and the spec copy coherent.

## Impact

- **Specs**: one `MODIFIED` delta on `agent-skills`.
- **Docs**: `AGENTS.md` — the outcome-only clause gains the precondition and drops a redundant restatement of the
  pointer rule; the net change is nearly size-neutral.
- **Build / tests / deployment**: none.
