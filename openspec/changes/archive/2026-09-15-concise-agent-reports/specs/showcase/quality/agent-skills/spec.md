## ADDED Requirements

### Requirement: Report-producing subagents follow a shared report contract

Every subagent that reports findings to the main agent — the per-change review and lesson-capture agents and the
on-demand auditors of the agent tooling, the spec corpus, and the architecture — SHALL follow one shared output
contract, so a report is skimmable and its verdict is read first. The contract SHALL bound the report, not the analysis:
a subagent SHALL verify its claims as thoroughly as before and report them in the contract's shape.

#### Scenario: A report opens with its verdict

- **WHEN** a report-producing subagent returns its findings
- **THEN** the first line states the outcome — for a capture or review, whether anything remains (and how many items);
  for an auditor, the section structure it uses
- **AND** a report with nothing to report says so in that first line rather than in a closing sentence

#### Scenario: Each item is budgeted

- **WHEN** the subagent reports a proposed addition, a finding, or an observation
- **THEN** the item names itself, its anchor (`file:line` where applicable), and one line of evidence
- **AND** the budget is per item, not a cap on the report's total, so a report with many genuine findings is longer than
  one with few

#### Scenario: Verified-and-set-aside candidates are summarized

- **WHEN** the subagent has verified a candidate and concluded it is already covered, rejected, or not a defect
- **THEN** it reports that in one line, or collapses several into one summary line, rather than a paragraph each

#### Scenario: No alternatives are offered

- **WHEN** the subagent proposes a change or a correction
- **THEN** it states the recommendation, not a menu of alternatives — the calling agent makes the choice
