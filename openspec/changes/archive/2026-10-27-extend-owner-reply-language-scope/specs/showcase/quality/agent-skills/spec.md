# Delta — showcase/quality/agent-skills

## MODIFIED Requirements

### Requirement: Agent output language follows the output's audience

Every agent's output in this repository SHALL use the language its audience reads, following the output-language
convention stated once in `AGENTS.md`'s "Conventions" section.

#### Scenario: An owner-facing reply uses the owner's language

- **WHEN** an agent replies to the owner in the session
- **THEN** the reply uses the language of the owner's current request unless the owner explicitly requested another
  language

#### Scenario: Progress narration uses the owner's language

- **WHEN** an agent emits progress narration around a tool call, or any other assistant message the owner can see in the
  turn
- **THEN** that message uses the language of the owner's current request unless the owner explicitly requested another
  language

#### Scenario: A later language switch is followed

- **WHEN** the owner writes a later request in another language
- **THEN** the reply follows that language rather than the language of the session's first request

#### Scenario: An agent-facing report and a handoff are English

- **WHEN** an agent returns a report to another agent, or hands a decision to one
- **THEN** the report and the handoff are English, whatever language the owner's request used

#### Scenario: A delegation prompt is English

- **WHEN** an agent composes the prompt for a `subagent` invocation
- **THEN** that prompt is English, whatever language the owner's request used

#### Scenario: A durable artifact is English

- **WHEN** an agent writes a repository document, a specification, a commit message, a pull-request or issue body, or a
  report a workflow publishes — or composes a draft in chat for one
- **THEN** the artifact is English even when the conversation about it ran in the owner's language

#### Scenario: A relayed report keeps its contract

- **WHEN** an agent presents a subagent's English report to the owner in the owner's language
- **THEN** the presentation keeps the report's structure, evidence, and uncertainty rather than shortening it into a
  summary

#### Scenario: A presented report's prose is translated

- **WHEN** an agent presents a subagent's English report to the owner in the owner's language
- **THEN** the report's prose headings, labels, and count descriptions are translated, while its structure, evidence,
  and uncertainty are preserved

#### Scenario: Identifiers and quotations stay untranslated

- **WHEN** a reply or a relayed report quotes an identifier, a path, a command, an exact quotation, or a
  machine-readable field
- **THEN** it stays in its original form

#### Scenario: An unclear owner language is asked about

- **WHEN** the language of the owner's request cannot be established
- **THEN** the agent asks rather than guessing
