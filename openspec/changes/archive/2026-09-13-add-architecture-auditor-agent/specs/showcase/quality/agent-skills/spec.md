## ADDED Requirements

### Requirement: The architecture is audited for drift from its recorded decisions

The repository SHALL provide an `architecture-auditor` agent subagent that audits the project's architecture — the ADRs
under `docs/adr/` together with the architectural surface (the service boundaries, the module dependency graph, and the
spec corpus's capability decomposition) — for **drift**, and reports its findings without editing files. The audit SHALL
report in two clearly separated sections: verified findings and advisory observations. Verified findings SHALL cover, at
least: an ADR's Decision contradicted by the implementation; an ADR `Status` that is stale, or whose supersession is not
recorded as `Superseded by ADR-NNNN`; a cross-reference to an `ADR-NNNN` that does not exist; a cross-cutting structural
decision made in the code or configuration with no ADR; a dependency or service-boundary direction the architecture does
not sanction; and a spec role-group decomposition that no longer matches the module or service structure. Each finding
SHALL be verified against the repository rather than inferred from the documents alone, and SHALL be reported with its
location and a concrete suggested correction. Advisory observations SHALL carry design judgment for which the
architecture provides no reference to check against — cohesion and coupling, decomposition, and apparent gaps in the ADR
set — and SHALL be reported separately from findings, without severity, and SHALL NOT be treated as defects to fix. The
audit SHALL NOT verify behavior against the implementation (the change workflow's review loop and the archive-time spec
sync own that), SHALL NOT audit the spec corpus's internal structure (the `specs-auditor` owns that), and SHALL NOT
re-check a property an existing gate already enforces. The subagent SHALL propose its findings without modifying files;
the main agent verifies and applies those the user approves.

#### Scenario: An architecture audit is produced

- **WHEN** the main agent invokes the `architecture-auditor` subagent (e.g. via the `/audit-architecture` command)
- **THEN** it returns findings and advisory observations in two separated sections, the findings covering ADR drift, ADR
  `Status` integrity, unrecorded decisions, dependency/service-boundary direction, and spec-decomposition fit, each with
  a location and a suggested correction

#### Scenario: Findings are verified against the repository

- **WHEN** the `architecture-auditor` subagent flags an ADR, a cross-reference, or a boundary
- **THEN** it checks the claim against the repository (the ADR file, the module and service configuration, the
  referenced code or spec) before reporting it, so a still-true decision or a deliberate structure is not reported as
  drift

#### Scenario: Advisory observations are reported separately from findings

- **WHEN** the `architecture-auditor` subagent forms a design judgment (cohesion, decomposition, ADR-set gaps)
- **THEN** it reports it in the advisory section, without severity and not as a defect, so the main agent does not treat
  it as actionable without the user's decision

#### Scenario: Behavior and intra-corpus checks are out of scope

- **WHEN** the `architecture-auditor` subagent audits the architecture
- **THEN** it does not verify behavior against the implementation, does not audit the spec corpus's internal structure,
  and does not re-check a property an existing gate already enforces

#### Scenario: The auditor does not edit files itself

- **WHEN** the `architecture-auditor` subagent runs
- **THEN** it returns proposed findings and corrections without modifying files; the main agent verifies and applies
  those the user approves
