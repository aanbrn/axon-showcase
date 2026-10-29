## MODIFIED Requirements

### Requirement: Unexplained design intent is surfaced for clarification

The `architecture-auditor` subagent SHALL report, in its advisory section, the deliberate choices and deliberate
absences whose rationale is not recorded in the repository. It SHALL sweep the surfaces where a deliberate decision
implies a rejected alternative: dependency `exclude(...)` declarations; major-version-suppressed coordinates in
`config/dependency-updates/major-disabled.txt`, coordinates held back at a version line in
`config/dependency-updates/hold-back.txt`, and the web UI packages for which the npm report suppresses majors in
`config/web-ui-updates/major-disabled.txt`; suppression annotations that encode a design choice (`@SuppressWarnings`)
and deprecated-API usages the project still carries; deferrals or band-aids recorded in an ADR or parked in
`docs/ideas.md`; and a record whose premise appears to have expired — a decision deferred in an ADR whose recorded
`Revisit when:` condition appears met, a parked `docs/ideas.md` entry whose work has already landed, or a parked entry
whose own recorded reopening condition appears met. It SHALL hold a recorded condition — an ADR's or a parked entry's —
against the repository where it names a checkable fact (an external coordinate or a consumed artifact's adoption), and
the parked entry against the change, commit, workflow or mechanism the entry describes rather than an artifact it merely
cites, and SHALL NOT report one that names no such fact. For the first class it SHALL verify by searching the repository
that no rationale is recorded before reporting an item, and SHALL NOT report a choice whose rationale is already
recorded; the deferred-and-parked-record class is the inverse — its rationale is recorded, and it is reported only when
that record's premise appears to have expired. Each item SHALL name the deliberate choice or absence with its location,
and SHALL state the question whose answer would record the missing rationale — for a record whose premise appears to
have expired, the recorded condition or parked entry and the signal that appears to meet it. The items SHALL be
advisory: reported without severity, and SHALL NOT be treated as defects to fix, because only the project owner can say
which unrecorded rationales matter.

A rationale need not be located in an ADR to count as recorded: the search SHALL cover the ADRs, the specs, `AGENTS.md`,
the build files, the configuration, the source, and the established conventions, and a choice SHALL be reported only
where its rationale's absence is consequential enough for the owner to decide — a low-impact local implementation detail
is not an architectural decision. Each recorded reopening condition — an ADR's `Revisit when:` line, or a parked entry's
own condition, labelled or in prose — SHALL retain its own qualifiers and compound structure, a conjunction being met
only when every clause is, and SHALL be classified before it is reported — repository-checkable, externally checkable,
dependent on human judgment, or not currently verifiable — and the deferral or parked entry SHALL be reported only where
the condition is demonstrably satisfied; a newer published version alone does not satisfy a condition that requires
compatibility, adoption, or support, and where external verification is unavailable or inconclusive the audit SHALL
state no conclusion about the condition, since an inability to verify SHALL NOT become an expiry or a due-recurrence
claim. An expired condition triggers a review, not a predetermined change. A parked entry's met condition SHALL be
reported as a recurrence that is due, and SHALL NOT be reported as an instruction to remove the entry. A parked entry
SHALL distinguish its proposed work having landed, only part of it having landed, and a different mechanism achieving
the same objective; an artifact the entry merely cites SHALL NOT establish completion.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

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

#### Scenario: A parked idea whose own recorded condition appears met is surfaced as due

- **WHEN** the `architecture-auditor` subagent reads a parked `docs/ideas.md` entry whose own reopening condition
  appears met — stated under a `Revisit when:` label or in the entry's prose, such as a fix to make once a third
  vendored skill set exists and a third set has been vendored since
- **THEN** it reports the entry, its recorded condition, and the signal that appears to meet it, in the advisory
  section, as a recurrence that is due rather than as an instruction to remove the entry, so the owner can decide
  whether to reopen the decision, do the work, or narrow the entry

#### Scenario: A parked idea whose condition is not met is not reported

- **WHEN** a parked entry records a reopening condition the repository's own state does not satisfy — a named
  prerequisite that still stands, or a conjunction one of whose clauses is still open
- **THEN** the auditor does not report that entry, so the advisory section stays a list of due recurrences

#### Scenario: A parked idea's condition the repository cannot settle is not reported

- **WHEN** a parked entry's condition depends on human judgment or on an external fact the auditor cannot establish — an
  upstream release it cannot verify, or an internal capacity trigger
- **THEN** the auditor classifies it as dependent on human judgment or not currently verifiable, states no conclusion
  about it, and does not report it, since an inability to verify SHALL NOT become a due-recurrence claim
