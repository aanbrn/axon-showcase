## MODIFIED Requirements

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

A finding SHALL establish which artifact is authoritative for the disputed claim under the project's documented
conventions before it asserts a violation: the implementation does not automatically override an accepted decision, and
an accepted decision does not automatically make the implementation correct. Each finding SHALL be established by
inspecting both sides — the recorded decision or convention, and the implementation or configuration it names — and
SHALL NOT be inferred from naming, file placement, or superficial similarity; a dependency edge is a finding only where
a recorded rule forbids it, so the applicable boundary rule is established first. Where authority cannot be established
— two records contradict each other, a later decision supersedes an earlier one but the record is stale, or a deliberate
exception explains the current state — the item SHALL be reported as an advisory observation, not as a verified
violation, and the audit SHALL NOT invent a precedence rule the repository does not state. Symptoms of one underlying
mismatch SHALL be reported as one item when they share a root cause and a correction, and independent violations SHALL
NOT be merged; where one root cause yields both a contradiction and a missing rationale, it SHALL be one finding with a
short note on the documentation gap rather than the same issue repeated in the advisory section. A cohesion, coupling,
or decomposition observation SHALL be bounded: it is reported only where it identifies a concrete inconsistency or a
meaningful missing decision within the audit's scope, never as a general review. The audit SHALL proceed in stages,
reusing what an earlier stage collected rather than re-reading the same files: orientation (the ADR conventions, the
ADRs, the architectural guidance, the module build files, and the spec role groups); structural verification (the
dependency edges, the service boundaries, the UI layer constraints, the cross-references, and the spec-decomposition
fit); targeted verification (only the discrepancies and missing-rationale candidates the earlier stages identified,
searching for exceptions, consumers, implementation evidence, and relevant external facts); and report validation
(re-checking every item against the evidence and the exclusions).

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

#### Scenario: A disagreement between records is resolved by authority

- **WHEN** the implementation and a recorded decision disagree, or two records contradict each other
- **THEN** the audit establishes which artifact is authoritative for the disputed claim under the project's documented
  conventions before asserting a violation, and where authority cannot be established — a stale supersession, a
  deliberate exception, or a genuine contradiction between records — it reports the item as an advisory observation
  rather than a verified violation

#### Scenario: A dependency edge is a finding only where a recorded rule forbids it

- **WHEN** the `architecture-auditor` subagent finds a dependency edge that looks undesirable
- **THEN** it establishes the applicable boundary rule before reporting, and reports the edge as a finding only where a
  recorded decision, convention, or module-graph rule forbids it — never from the edge's shape alone

#### Scenario: Symptoms of one mismatch are reported as one item

- **WHEN** several symptoms share one underlying architectural mismatch and one correction
- **THEN** the audit reports them as one item, and it does not merge independent violations that merely concern the same
  ADR or module; where one root cause yields both a contradiction and a missing rationale, it is one finding with a
  short note on the documentation gap, not the same issue repeated in the advisory section

#### Scenario: The audit proceeds in bounded stages

- **WHEN** the `architecture-auditor` subagent audits the architecture
- **THEN** it orients first (the ADR conventions, the ADRs, the architectural guidance, the module build files, the spec
  role groups), verifies the structure next (the dependency edges, the boundaries, the UI layers, the cross-references,
  the spec-decomposition fit), investigates only the discrepancies and candidates those stages identified, and validates
  the report against the evidence and the exclusions — reusing what earlier stages collected

#### Scenario: A general cohesion or coupling observation is not reported

- **WHEN** a cohesion, coupling, or decomposition observation identifies no concrete inconsistency and no meaningful
  missing decision within the audit's scope
- **THEN** the audit does not report it, so the advisory section stays a list of concrete items rather than a general
  design review

### Requirement: Unexplained design intent is surfaced for clarification

The `architecture-auditor` subagent SHALL report, in its advisory section, the deliberate choices and deliberate
absences whose rationale is not recorded in the repository. It SHALL sweep the surfaces where a deliberate decision
implies a rejected alternative: dependency `exclude(...)` declarations; major-version-suppressed coordinates in
`config/dependency-updates/major-disabled.txt`, coordinates held back at a version line in
`config/dependency-updates/hold-back.txt`, and the web UI packages for which the npm report suppresses majors in
`config/web-ui-updates/major-disabled.txt`; suppression annotations that encode a design choice (`@SuppressWarnings`)
and deprecated-API usages the project still carries; deferrals or band-aids recorded in an ADR or parked in
`docs/ideas.md`; and a record whose premise appears to have expired — a decision deferred in an ADR whose recorded
`Revisit when:` condition appears met, or a parked `docs/ideas.md` entry whose work has already landed. It SHALL hold
the condition against the repository where it names a checkable fact (an external coordinate or a consumed artifact's
adoption), and the parked entry against the change, commit, workflow or mechanism the entry describes rather than an
artifact it merely cites, and SHALL NOT report one that names no such fact. For the first class it SHALL verify by
searching the repository that no rationale is recorded before reporting an item, and SHALL NOT report a choice whose
rationale is already recorded; the deferred-and-parked-record class is the inverse — its rationale is recorded, and it
is reported only when that record's premise appears to have expired. Each item SHALL name the deliberate choice or
absence with its location, and SHALL state the question whose answer would record the missing rationale — for a record
whose premise appears to have expired, the recorded condition or parked entry and the signal that appears to meet it.
The items SHALL be advisory: reported without severity, and SHALL NOT be treated as defects to fix, because only the
project owner can say which unrecorded rationales matter.

A rationale need not be located in an ADR to count as recorded: the search SHALL cover the ADRs, the specs, `AGENTS.md`,
the build files, the configuration, the source, and the established conventions, and a choice SHALL be reported only
where its rationale's absence is consequential enough for the owner to decide — a low-impact local implementation detail
is not an architectural decision. Each `Revisit when:` condition SHALL be classified before it is reported —
repository-checkable, externally checkable, dependent on human judgment, or not currently verifiable — and the deferral
SHALL be reported only where the condition is demonstrably satisfied; a newer published version alone does not satisfy a
condition that requires compatibility, adoption, or support, and where external verification is unavailable or
inconclusive the audit SHALL state no conclusion about the condition, since an inability to verify SHALL NOT become an
expiry claim. An expired condition triggers a review, not a predetermined change. A parked entry SHALL distinguish its
proposed work having landed, only part of it having landed, and a different mechanism achieving the same objective; an
artifact the entry merely cites SHALL NOT establish completion.

#### Scenario: An unexplained deliberate choice is surfaced as a question

- **WHEN** the `architecture-auditor` subagent sweeps a deliberate choice (a dependency exclusion, a suppressed
  major-version coordinate, a held-back coordinate, a suppression annotation, a retained deprecated API, or a recorded
  deferral) and finds no rationale recorded for it
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

#### Scenario: A deferred decision whose condition appears met is surfaced

- **WHEN** the `architecture-auditor` subagent finds an ADR carrying a `Revisit when:` condition that appears met and
  the deferral's recorded decision still stands — an external coordinate gate whose pinned coordinate has since
  published a newer major, or a third-party-adoption gate the consumed artifacts now satisfy
- **THEN** it reports the ADR, the recorded condition, and the signal that appears to meet it, in the advisory section,
  so the owner can decide whether to act

#### Scenario: A deferral whose condition is not repository-decidable is not reported

- **WHEN** an ADR's `Revisit when:` condition names no repository fact the auditor can check (for example a capacity
  trigger) and no such fact has changed
- **THEN** the auditor does not report that ADR, since the condition is not one the repository's own state can meet or
  refute

#### Scenario: A parked idea whose work has landed is surfaced

- **WHEN** the `architecture-auditor` subagent sweeps `docs/ideas.md` and finds a parked entry whose own work exists in
  the repository (its change, commit, workflow or mechanism is there)
- **THEN** it reports the entry and its location in the advisory section together with the question whose answer would
  retire it, so the owner can confirm and remove it

#### Scenario: A parked idea that merely cites an artifact is not reported

- **WHEN** a parked entry only names an artifact as context (a companion change, a workflow, or a file it refers to) and
  the work the entry itself describes does not exist
- **THEN** it is not reported, so the audit's output stays a list of open questions

#### Scenario: A deferral's condition is classified before it is reported

- **WHEN** the `architecture-auditor` subagent evaluates an ADR's `Revisit when:` condition
- **THEN** it classifies the condition (repository-checkable, externally checkable, dependent on human judgment, or not
  currently verifiable) and reports the deferral only where the condition is demonstrably satisfied; a newer published
  version alone does not satisfy a condition requiring compatibility, adoption, or support, and where external
  verification is unavailable or inconclusive it states no conclusion rather than turning the gap into an expiry claim

#### Scenario: A parked idea whose work only partly landed is reported as partial

- **WHEN** a parked entry's proposed work has only partly landed, or a different mechanism achieves the same objective
- **THEN** the audit says which part appears to have landed (or that a different mechanism does), and does not report
  the entry as fully implemented or silently unpark it
