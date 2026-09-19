## MODIFIED Requirements

### Requirement: Report-producing subagents follow a shared report contract

Every subagent that reports findings to the main agent — the per-change review and lesson-capture agents and the
on-demand auditors of the agent tooling, the spec corpus, the architecture, and the README — SHALL follow one shared
output contract, so a report is skimmable and its verdict is read first. The contract SHALL bound the report, not the
analysis: a subagent SHALL verify its claims as thoroughly as before and report them in the contract's shape.

#### Scenario: A report opens with its verdict

- **WHEN** a report-producing subagent returns its findings
- **THEN** the first line states the outcome — whether anything remains and how many items — and names the count of
  every class the report carries (findings, and any advisory or accretion class it reports)
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

## ADDED Requirements

### Requirement: The README is audited for accuracy, design intent, and experience coverage

The repository SHALL provide a `readme-auditor` agent subagent that audits `README.md` — the human-facing showcase and
onboarding guide — for the drift no existing auditor can see, and reports its findings without editing files. The
auditor's scope SHALL be `README.md` only; the other documents (the ADRs, the retrospectives, `docs/`, and the
`openspec/specs/` corpus) are covered by other owners. The audit SHALL cover three axes:

- **Accuracy / consistency**: every claim in the README — commands, ports, versions, image and task names, links, and
  the OpenSpec-flow diagram's semantics — matches the repository, cross-checked against `AGENTS.md` and the spec corpus
  rather than trusted from the prose.
- **Design-intent fidelity**: the README keeps the shape its convention fixes — the section order, the step-by-step
  Getting Started path, Gradle tasks in preference to raw `docker compose`/`helm install` commands, curl as the single
  API example CLI, and the prompting-exercise narrative.
- **Coverage / experience surfacing**: the Cool Story and every human-visible capability the system offers (for example
  the saga auto-start, the live SSE timeline, the `setup-hosts` hostnames, and the Grafana access path) is mentioned in
  the README, cross-checked against what the system does.

Each finding SHALL be verified against the repository rather than inferred from the README's prose, and SHALL be
reported with its location and a concrete suggested rewrite. The audit SHALL report subjective quality — prose,
structure, redundancy, jargon, and flow beyond the documented shape — in a separate **advisory** class for the user's
judgment, never as a defect, because the README is hand-curated by design and its intended shape is to be preserved on
every edit. The subagent SHALL propose its findings without modifying files; the main agent verifies and applies those
the user approves, and the report SHALL follow the shared report contract.

#### Scenario: A README audit is produced

- **WHEN** the main agent invokes the `readme-auditor` subagent (e.g. via the `/audit-readme` command)
- **THEN** it returns findings across the accuracy/consistency, design-intent fidelity, and
  coverage/experience-surfacing axes, each with a location and a suggested rewrite, plus a separate advisory class for
  subjective quality, and names the finding and advisory counts in its verdict line

#### Scenario: A claim is verified against the repository

- **WHEN** the `readme-auditor` subagent flags a claim in the README (a count, a port, a version, a task name, or a
  diagram's semantics)
- **THEN** it checks the claim against the repository — the build files, the Helm values, the spec corpus, or
  `AGENTS.md` — before reporting it, so a deliberate or still-true statement is not reported as wrong

#### Scenario: A human-visible capability missing from the README is reported

- **WHEN** the system offers a capability a person can see or experience and the README does not mention it
- **THEN** the audit reports it under coverage/experience surfacing, naming the capability and where the README should
  surface it

#### Scenario: Subjective quality is advisory, not a defect

- **WHEN** the auditor finds a prose, structure, or redundancy concern beyond the README's documented design intent
- **THEN** it reports the concern in the advisory class for the user's judgment rather than as a finding to fix, since
  the README is hand-curated by design

#### Scenario: The auditor does not edit files itself

- **WHEN** the `readme-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves
