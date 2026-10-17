# Spec Delta

## ADDED Requirements

### Requirement: Agent-authored technical artifacts follow a structured writing standard

Agent-authored technical artifacts — `AGENTS.md`, OpenSpec proposals, specifications, designs, and task lists, plans,
code reviews, and other repository documentation — SHALL follow the structured, scannable shape `AGENTS.md`'s
"Structured Writing Standard" section states, applied while the artifact is drafted rather than only in a later cleanup
pass, and each SHALL keep its own required template and sections.

#### Scenario: A requirement is stated as its own scannable unit

- **WHEN** an agent authors an artifact that states several independent requirements, actions, or decisions
- **THEN** each is a separate bullet or numbered step rather than several packed into one paragraph, and an ordered
  procedure is numbered

#### Scenario: A condition is explicit

- **WHEN** the next action depends on a condition
- **THEN** the artifact states the condition and its branch (`IF`/`THEN`-style, or a decision-table row) rather than
  leaving the dependency implied

#### Scenario: The mandatory is separated from the explanatory

- **WHEN** an artifact states a normative requirement together with its rationale, background, or an example
- **THEN** the obligation is distinguishable from the explanation, so a reader finds what must be done without reading
  why

#### Scenario: The standard governs drafting, not only a later pass

- **WHEN** an agent creates or substantially edits an artifact
- **THEN** it organizes the intended content into its sections and lists before writing the prose, instead of leaving
  the structure to a formatting pass after the fact

#### Scenario: Artifact-specific conventions are preserved

- **WHEN** the standard is applied to an artifact with its own required format (an OpenSpec proposal, specification,
  design, or task list, an ADR, or a review report)
- **THEN** the artifact keeps every required section and semantic distinction (SHALL statements, `####` WHEN/THEN
  scenarios, task checkboxes), the standard shaping the content within those sections

#### Scenario: Dense prose is restructured on review

- **WHEN** an artifact is reviewed (`review-quick`, `review-thorough`) or audited (`agents-auditor`) and a passage packs
  several independently actionable statements into one paragraph or bullet, implies a condition, or merges mandatory
  text with its rationale
- **THEN** it is reported as a finding, with its location and the restructure, without changing the passage's technical
  meaning, normative strength, or required format
