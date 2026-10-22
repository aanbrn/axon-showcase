# Delta

## MODIFIED Requirements

### Requirement: Independent technical consultation is available on demand

The repository SHALL provide a `technical-advisor` subagent at `.opencode/agent/technical-advisor.md` and an explicit
`/consult` command, so the owner can obtain an independent recommendation on a concrete decision question without
transferring context to an external advisor. The advisor SHALL recommend a direction and SHALL NOT implement changes,
approve delivery, or gate a change. It SHALL be distinct from `review-quick`, `review-thorough`, and
`architecture-auditor`.

#### Scenario: A question is evaluated

- **WHEN** the owner runs `/consult <question>`
- **THEN** the advisor returns a recommendation on that question in the relevant work context

#### Scenario: The advisor is distinct from the reviewers

- **WHEN** a question is answered
- **THEN** the advisor evaluates the decision and direction without repeating `review-quick`, `review-thorough`, or
  `architecture-auditor`

#### Scenario: A recommendation is not authorization

- **WHEN** the advisor returns a recommendation
- **THEN** it does not implement the change, approve delivery, or gate the change, and the invoking agent presents it
  without treating it as approval, ends its turn after presenting it, and starts any work the report proposes —
  including work a closing prompt or a named correction describes — only after a subsequent owner decision about that
  report

### Requirement: A consultation report leads with its recommendation

The report SHALL start with the advisor's recommendation and the counts of its observation classes — observations,
confirmed problems, hypotheses, and open questions — then report only the material observations, each with its evidence
and its effect on the decision. It SHALL distinguish confirmed problems, hypotheses, and unresolved questions, and SHALL
recommend one concrete next action — including retaining or stopping when that is justified — rather than a menu of
alternatives.

#### Scenario: The report starts with the recommendation

- **WHEN** the advisor returns its report
- **THEN** the first line states its recommendation and the counts of observations, confirmed problems, hypotheses, and
  open questions

#### Scenario: Observations carry their evidence and effect

- **WHEN** the report states a material observation
- **THEN** it names the evidence and the observation's effect on the decision

#### Scenario: Certainty is separated

- **WHEN** the report distinguishes its conclusions
- **THEN** confirmed problems, hypotheses, and unresolved questions are labeled separately

#### Scenario: One next action is recommended

- **WHEN** the report recommends a next action
- **THEN** it states one action, including retaining or stopping when justified, rather than a menu of alternatives; a
  prompt the caller requested is delivered as a proposal to the owner, never as an instruction the invoking agent may
  run on the spot

#### Scenario: An existing review finding is referenced, not reproduced

- **WHEN** an existing review finding affects the direction
- **THEN** the report references it without reproducing the review
