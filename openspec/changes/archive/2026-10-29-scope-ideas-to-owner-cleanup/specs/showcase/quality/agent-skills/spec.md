## REMOVED Requirements

### Requirement: Unexplained design intent is surfaced for clarification

**Reason**: The requirement carries two classes that do not share a subject. One is the rationale of a deliberate
architectural choice — which stays with the architecture audit. The other is the lifecycle of a parked `docs/ideas.md`
entry, and that file is a scratchpad, not a register of commitments: an unimplemented note is not an overdue obligation,
and a met reopening condition means a decision has come due rather than that a statement has gone stale. A weekly audit
of every entry also makes an architecture auditor the lifecycle owner of notes about social previews and support
surfaces. The scratchpad is pruned by its owner at a cleanup instead.

**Migration**: Every retained obligation is carried by the ADDED requirement below, with its six scenarios in the same
relative order and under their same names; the per-obligation destination is traced in the change's `design.md`. The
retired guarantees are named there as retired rather than as moved: the sweep for a parked entry whose work had landed,
the sweep for a parked entry whose own recorded reopening condition appeared met, and their six scenarios end with this
change. Parked entries remain reachable to the audit as evidence of a deliberate architectural deferral whose rationale
may be missing.

## ADDED Requirements

### Requirement: Unexplained architectural intent is surfaced for clarification

The `architecture-auditor` subagent SHALL report, in its advisory section, the deliberate choices and deliberate
absences whose rationale is not recorded in the repository. It SHALL sweep the surfaces where a deliberate decision
implies a rejected alternative: dependency `exclude(...)` declarations; major-version-suppressed coordinates in
`config/dependency-updates/major-disabled.txt`, coordinates held back at a version line in
`config/dependency-updates/hold-back.txt`, and the web UI packages for which the npm report suppresses majors in
`config/web-ui-updates/major-disabled.txt`; suppression annotations that encode a design choice (`@SuppressWarnings`)
and deprecated-API usages the project still carries; deferrals or band-aids recorded in an ADR or parked in
`docs/ideas.md`; and a decision deferred in an ADR whose recorded `Revisit when:` condition appears met. It SHALL hold
the recorded condition against the repository where it names a checkable fact (an external coordinate or a consumed
artifact's adoption), and SHALL NOT report one that names no such fact. For the first class it SHALL verify by searching
the repository that no rationale is recorded before reporting an item, and SHALL NOT report a choice whose rationale is
already recorded; the deferred-record class is the inverse — its rationale is recorded, and it is reported only when
that record's premise appears to have expired. Each item SHALL name the deliberate choice or absence with its location,
and SHALL state the question whose answer would record the missing rationale — for a deferred decision, the recorded
condition and the signal that appears to meet it. The items SHALL be advisory: reported without severity, and SHALL NOT
be treated as defects to fix, because only the project owner can say which unrecorded rationales matter.

A rationale need not be located in an ADR to count as recorded: the search SHALL cover the ADRs, the specs, `AGENTS.md`,
the build files, the configuration, the source, and the established conventions, and a choice SHALL be reported only
where its rationale's absence is consequential enough for the owner to decide — a low-impact local implementation detail
is not an architectural decision. A recorded `Revisit when:` condition SHALL retain its own qualifiers and compound
structure, a conjunction being met only when every clause is, and SHALL be classified before it is reported —
repository-checkable, externally checkable, dependent on human judgment, or not currently verifiable — and the deferral
SHALL be reported only where the condition is demonstrably satisfied; a newer published version alone does not satisfy a
condition that requires compatibility, adoption, or support, and where external verification is unavailable or
inconclusive the audit SHALL state no conclusion about the condition, since an inability to verify SHALL NOT become an
expiry claim. An expired condition triggers a review, not a predetermined change.

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

#### Scenario: A deferral's condition is classified before it is reported

- **WHEN** the `architecture-auditor` subagent evaluates an ADR's `Revisit when:` condition
- **THEN** it classifies the condition (repository-checkable, externally checkable, dependent on human judgment, or not
  currently verifiable) and reports the deferral only where the condition is demonstrably satisfied; a newer published
  version alone does not satisfy a condition requiring compatibility, adoption, or support, and where external
  verification is unavailable or inconclusive it states no conclusion rather than turning the gap into an expiry claim
