# Record why the README audit is not scheduled

## Why

The `readme-auditor` is the one repository audit absent from the scheduled `audit` workflow, which runs the
agent-tooling, spec-corpus, and architecture audits. The exclusion is correct — the scheduled cadence exists to counter
the accretion the capture loop produces, and the README audit checks a different subject — but no artifact records
_why_. The agent-tooling audit checked the three-vs-four count and found it **consistent**, noting only that
`readme-auditor` is on-demand; a count that agrees is not a rationale, so the reason is inferable from the requirement's
purpose clause but nowhere stated. This change records it where a reader or an auditor would look for it.

## What Changes

- **`showcase/quality/agent-skills`** — the `The on-demand audits are also available on a schedule` requirement states
  that the README audit is deliberately on-demand rather than scheduled, and why: the scheduled three are the audits of
  the agent's own machinery (tooling, spec corpus, architecture) whose cadence counters the accretion the capture loop
  produces, while the README audit verifies the human-facing document against the repository — an accuracy-and-coverage
  check that is not part of that reconciliation loop.
- **`AGENTS.md`** — the readme-auditor bullet records that it is on-demand only, so the on-demand status and its reason
  sit beside the subagent's description as well as in the spec.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the scheduled-audits requirement gains the README audit's deliberate on-demand scope
  and its reason.

## Impact

- **Specs**: one `MODIFIED` delta carrying all scenarios of the requirement.
- **Docs**: one `AGENTS.md` sentence in the readme-auditor bullet.
- **Code**: none. This records an existing design decision; it changes no behaviour.
