## ADDED Requirements

### Requirement: Unexplained design intent is surfaced for clarification

The `architecture-auditor` subagent SHALL report, in its advisory section, the deliberate choices and deliberate
absences whose rationale is not recorded in the repository. It SHALL sweep the surfaces where a deliberate decision
implies a rejected alternative: dependency `exclude(...)` declarations; major-version-suppressed coordinates in
`config/dependency-updates/major-disabled.properties`; suppression annotations that encode a design choice
(`@SuppressWarnings`) and deprecated-API usages the project still carries; and deferrals or band-aids recorded in an ADR
or parked in `docs/ideas.md`. It SHALL verify by searching the repository that no rationale is recorded before reporting
an item, and SHALL NOT report a choice whose rationale is already recorded. Each item SHALL name the deliberate choice
or absence with its location, and SHALL state the question whose answer would record the missing rationale. The items
SHALL be advisory: reported without severity, and SHALL NOT be treated as defects to fix, because only the project owner
can say which unrecorded rationales matter.

#### Scenario: An unexplained deliberate choice is surfaced as a question

- **WHEN** the `architecture-auditor` subagent sweeps a deliberate choice (a dependency exclusion, a suppressed
  major-version coordinate, a suppression annotation, a retained deprecated API, or a recorded deferral) and finds no
  rationale recorded for it
- **THEN** it reports the choice and its location in the advisory section together with the question whose answer would
  record the rationale, so the owner can triage it

#### Scenario: A recorded rationale is not reported

- **WHEN** the `architecture-auditor` subagent sweeps a deliberate choice whose rationale is already recorded (a
  suppressed major-version coordinate with a requirement in a spec, a deferral with an ADR, or a suppression convention
  in `AGENTS.md`)
- **THEN** it does not report that choice, so the audit's output stays a list of open questions

#### Scenario: Intent-clarification items are advisory

- **WHEN** the `architecture-auditor` subagent reports where clarification of intent is missing
- **THEN** it reports those items in the advisory section, without severity and not as defects, so the main agent does
  not "fix" them without the user's decision
