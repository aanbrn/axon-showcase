# Proposal

## Why

The defect-accumulation phase delayed product work: the unit that recorded two process-defect files took nine quick
rounds and one deep pass, and the deep pass was unnecessary because the routing floor's repeated-class signal fired on a
diff that ships no behavior. `agent-skills` already requires a record artifact's late round that raises only new
instances of an already-raised class to stop and escalate to the owner, but the caller's procedure names only "fix
everything and re-run", so the branch is invisible at the point it applies.

## What Changes

- `scripts/review-routing.py` stops letting the repeated-class signal raise the floor of a **record-only** diff, whose
  changed paths are all record artifacts or the review-round ledger; the signal is still reported, and a mandatory
  signal, a failed derivation, or the agent's own evidence still decides.
- `scripts/test-review-routing.py` gains controls for the exemption and for its boundaries.
- `.opencode/skills/review-gate/SKILL.md` names the record-artifact escalation branch in the loop's fix-and-re-run step.
- `AGENTS.md`'s recording workflow states the drafting preference the churn exposed: a short account of the observed
  event, a quotation only where it reproduces, a figure only from a run of its command, session-only text described
  rather than quoted.
- The `agent-skills` requirement "The deep review is routed and bounded" gains the exemption and a scenario, and "The
  quick review loop runs on every unit" counts a process-defect record in its record-artifact class and states the
  escalation's pending state.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/agent-skills`: the routing requirement's floor exempts a record-only diff from the repeated-class
  signal, and the quick-review requirement's record-artifact escalation names the process-defect record and the stop's
  pending state.

## Impact

- `scripts/review-routing.py`, `scripts/test-review-routing.py`, `.opencode/skills/review-gate/SKILL.md`, `AGENTS.md`,
  and the delta for `showcase/quality/agent-skills`.
- No JVM code, build script, deployment, or API change; the routing tool stays stdlib-only and its failed-derivation
  outcome stays `REQUIRED`.
