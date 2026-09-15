## Context

Fixes the report-contract requirement's verdict scenario, which the agent-tooling audit found stale: it says an
auditor's first line states "the section structure it uses", but all three auditors open with a count instead. See the
proposal.

## Goals / Non-Goals

- **Goal:** the spec states the first-line outcome that every report-producing subagent actually produces.
- **Non-Goal:** changing any agent definition — they already open with the count, so the spec is what drifted.

## Decisions

- **State the count as the shared shape, noting the one distinction.** The three auditors all open with `<n> findings`
  or `<n> findings, <n> advisory`; the per-change agents with `<n> findings` / `<n> durable proposals`. The unifying
  statement is "whether anything remains and how many items", with an audit that carries an advisory section named as
  the case that reports two counts (agent-tooling and architecture; `specs-auditor` names one).
  - _Alternative — name each agent's exact verdict:_ rejected, a roster that must be edited whenever an agent or its
    verdict changes; the contract's value is one shape.
  - _Alternative — keep "the section structure":_ rejected, inaccurate for every auditor.

## Risks / Trade-offs

- A count is less informative than a section structure for a reader who wants the report's shape up front — accepted;
  the shape is stated immediately in the sections below the verdict line, and every current agent leads with the count.
