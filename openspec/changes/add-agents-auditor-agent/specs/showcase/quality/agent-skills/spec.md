## ADDED Requirements

### Requirement: AGENTS.md is audited for consistency and conciseness

The repository SHALL provide an `agents-auditor` agent subagent that audits `AGENTS.md` — the agent's persistent
guidance — for **consistency** and **conciseness**, and reports its findings without editing files. The audit SHALL
cover, at least: contradictions between entries, stale claims and enumerations the repository has outgrown, dead
cross-references, and drift from the code/workflows/specs the prose describes (consistency); and duplicated or
near-duplicate entries, one-off trivia, over-long or over-specific entries, and misplaced entries (conciseness). Each
finding SHALL be verified against the repository rather than inferred from the prose alone, and SHALL be reported with
its location and a concrete suggested rewrite. The subagent SHALL propose its findings without modifying files; the main
agent verifies and applies those the user approves.

#### Scenario: An AGENTS.md audit is produced

- **WHEN** the main agent invokes the `agents-auditor` subagent (e.g. via the `/audit-agents` command)
- **THEN** it returns findings grouped by severity, covering consistency (contradictions, stale claims, dead
  cross-references, drift) and conciseness (duplication, trivia, length, placement), each with a location and a
  suggested rewrite

#### Scenario: Findings are verified against the repository

- **WHEN** the `agents-auditor` subagent flags a claim, enumeration, or cross-reference
- **THEN** it checks the claim against the repository (the referenced files, workflows, or specs) before reporting it,
  so a deliberate or still-true entry is not reported as stale

#### Scenario: The auditor does not edit files itself

- **WHEN** the `agents-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves
