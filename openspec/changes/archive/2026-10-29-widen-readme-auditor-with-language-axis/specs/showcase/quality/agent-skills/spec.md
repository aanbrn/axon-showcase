## REMOVED Requirements

### Requirement: The README is audited for accuracy, design intent, and experience coverage

**Reason**: The requirement's title names three axes, and the change adds a fourth — language. A `MODIFIED` block cannot
retitle a requirement (the delta is matched by its verbatim header), so the retitled requirement replaces this one.

**Migration**: Every obligation of this block is retained in the ADDED block below, with the same scenarios in the same
relative order and under the same names; the per-obligation destination is traced in the change's `design.md`. The
advisory scenario gained preference and unclear-intent conditions, the language axis gained its own finding rule (which
the requirement body states as the carve-out from the advisory class), and the design-intent axis gained its own
scenario.

## ADDED Requirements

### Requirement: The README is audited for accuracy, design intent, language, and experience coverage

The repository SHALL provide a `readme-auditor` agent subagent that audits `README.md` — the human-facing showcase and
onboarding guide — for the drift no existing auditor can see, and reports its findings without editing files. The
auditor's scope SHALL be `README.md` only; the other documents (the ADRs, the retrospectives, `docs/`, and the
`openspec/specs/` corpus) are covered by other owners. The audit SHALL cover four axes:

- **Accuracy / consistency**: every claim in the README — commands, ports, versions, image and task names, links, and
  the OpenSpec-flow diagram's semantics — matches the repository, cross-checked against `AGENTS.md` and the spec corpus
  rather than trusted from the prose.
- **Design-intent fidelity**: the README keeps the shape its convention fixes — the section order, the step-by-step
  Getting Started path, Gradle tasks in preference to raw `docker compose`/`helm install` commands, curl as the single
  API example CLI, and the prompting-exercise narrative.
- **Language correctness**: the README's prose is grammatical English. A **provable** error — wrong verb agreement or
  tense, a missing or wrong article or preposition, a duplicated or dropped word, an em-dash left unclosed where a
  paired insertion was opened, a fragment in a passage that needs a full sentence, or another punctuation defect with an
  identifiable fault — is a **finding**, reported with its location, what is wrong, and a minimal rewrite that preserves
  the meaning. A debatable wording, a diction preference, a passage whose intended meaning is unclear, and a sentence
  whose first parse misleads although it is grammatical SHALL remain **advisory**.
- **Coverage / experience surfacing**: the Cool Story and every human-visible capability the system offers (for example
  the saga auto-start, the live SSE timeline, the `setup-hosts` hostnames, and the Grafana access path) is mentioned in
  the README, cross-checked against what the system does.

A finding about what the README **asserts** SHALL be verified against the repository rather than inferred from the
README's prose. A **language** finding SHALL be evidenced by the passage and the language rule it violates, since a
grammar error is established by the text and the grammar, not by the system's configuration. Every finding SHALL be
reported with its location and a concrete suggested rewrite. A language correction that would change a technical claim
SHALL NOT be reported as a language finding alone, since wording and claim are one decision at that site: when a source
refutes the README's assertion, the site is an accuracy finding; when the assertion holds, the meaning-changing
correction is rejected; and when the intended assertion cannot be established either way, the site is advisory. Wording
preferences, structure, redundancy, jargon, flow beyond the documented shape, and an unclear intent SHALL be reported in
a separate **advisory** class for the user's judgment, never as a defect, because the README is hand-curated by design
and its intended shape is to be preserved on every edit; a provable language error is the language axis's finding, not
an advisory item. The subagent SHALL propose its findings without modifying files; the main agent verifies and applies
those the user approves, and the report SHALL follow the shared report contract.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: A README audit is produced

- **WHEN** the main agent invokes the `readme-auditor` subagent (e.g. via the `/audit-readme` command)
- **THEN** it returns findings across the accuracy/consistency, design-intent fidelity, language-correctness, and
  coverage/experience-surfacing axes, each with a location and a suggested rewrite, plus a separate advisory class for
  preference and judgment, and names the finding and advisory counts in its verdict line

#### Scenario: A claim is verified against the repository

- **WHEN** the `readme-auditor` subagent flags a claim in the README (a count, a port, a version, a task name, or a
  diagram's semantics)
- **THEN** it checks the claim against the repository — the build files, the Helm values, the spec corpus, or
  `AGENTS.md` — before reporting it, so a deliberate or still-true statement is not reported as wrong

#### Scenario: A human-visible capability missing from the README is reported

- **WHEN** the system offers a capability a person can see or experience and the README does not mention it
- **THEN** the audit reports it under coverage/experience surfacing, naming the capability and where the README should
  surface it

#### Scenario: A provable language error is a finding

- **WHEN** the `readme-auditor` subagent finds a provable language error — wrong agreement or tense, a missing article
  or preposition, a duplicated word, an unclosed paired em-dash, or a fragment in a passage that needs a full sentence
- **THEN** it reports the passage, the rule the text violates, and a minimal rewrite that preserves the meaning, as a
  finding rather than an advisory item

#### Scenario: Subjective quality is advisory, not a defect

- **WHEN** the auditor finds a phrasing it would word differently, a passage whose intended meaning it cannot establish,
  or a prose, structure, or redundancy concern beyond the README's documented design intent
- **THEN** it reports the concern in the advisory class for the user's judgment rather than as a finding to fix, since
  the README is hand-curated by design

#### Scenario: A departure from the README's design intent is reported

- **WHEN** the README departs from the shape its convention fixes — the section order, the step-by-step Getting Started
  path, Gradle tasks over raw commands, or curl as the single API example
- **THEN** the audit reports the departure under design-intent fidelity, naming the convention it breaks and the shape
  the surrounding section keeps

#### Scenario: A language correction that would change a claim is verified

- **WHEN** a language correction the auditor would otherwise propose would change what the README asserts about the
  system
- **THEN** it verifies the assertion against a source: a refuted assertion is reported as an accuracy finding, an
  assertion that holds leaves the meaning-changing correction rejected, and an assertion whose intent cannot be
  established is reported as advisory — never as a language finding alone

#### Scenario: The auditor does not edit files itself

- **WHEN** the `readme-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves
