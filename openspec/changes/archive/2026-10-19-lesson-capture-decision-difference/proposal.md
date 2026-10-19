# A capture addition states its decision difference

## Why

The capture loop is `AGENTS.md`'s growth engine, and its promotion gate tests a proposal in isolation — is this rule
true, actionable, material, general, high-confidence? The workflow already carries one instance of an increment test — a
Decision point asking whether an addition "restates a remedy the target bullet already carries" — but its remedy is to
**reword the rule**, never to refuse the addition, and it tests one shape of restatement rather than every addition.
Nothing tests the proposal's **increment** generally: whether the existing text already determines the same action. The
`agents-auditor` already owns that test, as a `Future decision` field on every candidate and a merge rule for text that
"would only restate a rule the file already carries" — but it runs weekly and after the fact, while the subagent that
writes the rules has no equivalent. So an extension can add incident narrative, examples, or a restatement of an
obligation the bullet already carries, and the file grows without changing the guidance a future agent receives.

## What Changes

- The `lesson-capture` subagent's promotion gate gains a **decision-difference test**, generalizing the workflow's
  existing restatement branch from "reword the rule" to a gate on every addition: every proposed addition or extension
  states the action a future agent takes under the existing guidance and the action the proposed text requires or
  prohibits under an explicit condition, with a repository-backed example of why the difference matters.
- An addition whose action the existing guidance already determines is reported as **already covered** and does not
  expand `AGENTS.md`; an additional incident, example, citation, or provenance marker alone is not a decision
  difference.
- An extension prefers an **exact replacement** of the existing rule over appending qualifications, preserving the
  established scope, exceptions, and normative strength.
- The incident evidence a rule rests on stays recoverable behind its `captured:` marker and in git history, rather than
  being carried in the rule's text.
- The `showcase/quality/agent-skills` capture requirement states the test, so the subagent's instruction and the spec
  agree.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the capture requirement's promotion gate gains the decision-difference test and the
  replacement preference; no new requirement.

## Impact

- **Agent tooling**: `.opencode/agent/lesson-capture.md` (the gate, and the report contract's proposal budget and
  collapsed line — its verdict-line format is unchanged), and the `AGENTS.md` capture workflow's pointer to the gate.
- **Spec**: one `MODIFIED` requirement in `showcase/quality/agent-skills` (a 391-line block, copied whole).
- **No journal, no size cap, no `AGENTS.md` cleanup**: the change adds a test, not a mechanism or a deletion.
