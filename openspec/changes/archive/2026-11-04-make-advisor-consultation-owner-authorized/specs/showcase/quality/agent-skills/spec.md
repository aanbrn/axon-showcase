# Delta — make advisor consultation owner-authorized

## MODIFIED Requirements

### Requirement: Independent technical consultation is available on demand

The repository SHALL provide a `technical-advisor` subagent at `.opencode/agent/technical-advisor.md` and an explicit
`/consult` command, so the owner can obtain an independent recommendation on a concrete decision question without
transferring context to an external advisor. The advisor SHALL recommend a direction and SHALL NOT implement changes,
approve delivery, or gate a change. It SHALL be distinct from `review-quick`, `review-thorough`, and
`architecture-auditor`. A consultation SHALL run only when the owner authorizes it — their `/consult` invocation or an
explicit instruction to consult the advisor — and a calling agent that judges one useful SHALL propose it, naming the
question, rather than delegating on its own initiative.

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

#### Scenario: An agent proposes a consultation rather than running one

- **WHEN** a calling agent judges that an independent consultation would inform a proposal, an implementation, a
  disagreement, or a next step
- **THEN** it states the question and proposes the consultation to the owner, and no consultation runs until the owner
  authorizes one

### Requirement: A consultation receives a focused handoff

The invoking agent SHALL supply only what the decision needs: selected relevant excerpts, source references, and prior
findings with their provenance and disposition are permitted; the full conversation and an undifferentiated tool-output
dump are not. The advisor SHALL assess supplied claims independently and reread the relevant sources where available.
Direct delegation, when the owner authorizes it, SHALL use the same handoff, and the report SHALL be presented without
converting a recommendation into authorization.

#### Scenario: The handoff carries only the decision's inputs

- **WHEN** the invoking agent prepares a consultation
- **THEN** it supplies the owner's authorization for the consultation, the owner's objective and material constraints,
  the question and proposed next action, the change identity and stage, artifact references and the diff base, the
  relevant diff hunks with their changed paths and revision context, any pre-change or history excerpts, the relevant
  findings and disagreements, the applicable subject packs, the report's audience, the requested language, any explicit
  effort override, and whether the consultation is fresh or a follow-up
- **AND** it does not forward the full conversation or an undifferentiated tool-output dump, while selected relevant
  excerpts remain permitted

#### Scenario: Selected excerpts carry provenance

- **WHEN** the handoff includes an excerpt or a prior finding
- **THEN** it names the excerpt's source and, for a prior finding, its disposition

#### Scenario: The advisor verifies independently

- **WHEN** the handoff summarizes a claim that affects the decision
- **THEN** the advisor assesses it independently and rereads the referenced source where available rather than accepting
  the summary as proof

#### Scenario: Direct delegation follows the same contract

- **WHEN** another agent delegates directly to the technical advisor
- **THEN** it supplies the same focused handoff and presents the report without converting the recommendation into
  authorization

#### Scenario: A handoff naming no authorization is refused

- **WHEN** a handoff reaches the advisor without the owner's authorization for the consultation
- **THEN** the advisor refuses it as an unauthorized consultation rather than answering the question
