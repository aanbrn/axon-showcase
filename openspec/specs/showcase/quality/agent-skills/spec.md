# showcase/quality/agent-skills Specification

## Purpose

Provides the repository's agent capabilities: curated, vendored skill sets under `.opencode/skills/` (so agents can run
the AxonIQ Axon 4→5 migration recipes against this codebase) and the locally-defined quality-gate and analysis subagents
under `.opencode/agent/` — the per-change review and lesson-capture agents, the experience-analyzer, the visual and
diagram agents, and the on-demand auditors of the project-owned agent tooling (the guidance and project-authored
`.opencode/` files, plus any generated or vendored file that contradicts how the repository uses it, the meta rules it
reports with their origins, and the merge, removal, and route candidates it reports), of the `openspec/specs/` corpus,
of the architecture (the ADRs, the service, module, and spec-decomposition surface, and where a deliberate decision's
rationale is not recorded), of the human-facing `README.md` (its claims against the repository, its shape against the
README convention, and its coverage of the human-visible capabilities), and the scheduled unattended run that performs
the audits without a human asking.

## Requirements

### Requirement: Vendored agent skills are available to agents

The repository SHALL vendor the AxonIQ `axoniq-migration` plugin skills (version 0.2.2) from the `AxonIQ/agent-skills`
repository under `.opencode/skills/`, so that opencode discovers them as project skills. The vendored set SHALL include
`axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`. Vendoring SHALL NOT add the skill files to
any Gradle module's classpath or to any built Docker image.

#### Scenario: Migration skills are discoverable by agents

- **WHEN** an agent lists the project's skills in `.opencode/skills/`
- **THEN** it finds `axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`, each with a `SKILL.md`
  entry point and its referenced assets

#### Scenario: Vendored skills do not affect the application build

- **WHEN** the project build runs any Gradle task (compile, test, package)
- **THEN** the vendored skill files under `.opencode/skills/` are not part of any module's source set, classpath, or
  Docker image

### Requirement: Vendored skills carry version provenance

The vendored skills SHALL record their upstream source repository and the exact plugin version they were copied from, so
that an upstream update is a deliberate, reviewable change rather than silent drift.

#### Scenario: Provenance is recorded

- **WHEN** a maintainer inspects the vendored skills or the `agent-skills` capability spec
- **THEN** the upstream source (`AxonIQ/agent-skills`), plugin (`axoniq-migration`), and version (0.2.2) are documented

#### Scenario: Updating to a new plugin version is an explicit change

- **WHEN** a new `axoniq-migration` plugin version is published upstream
- **THEN** refreshing the vendored skills is performed as a reviewed change that updates the recorded version, rather
  than applied silently

### Requirement: Experience analysis is available to agents

The repository SHALL provide an `experience-analyzer` agent subagent that, given a gathered digest of recent experience
(merged pull requests, archived changes, `AGENTS.md` gotchas, and `docs/ideas.md`), produces a retrospective and
forward-looking improvement suggestions. The retrospective SHALL group shipped PRs by theme, summarize lessons learned,
and record went-well/went-wrong observations. Each improvement suggestion SHALL be classified as a system suggestion
(addressed via `docs/ideas.md` or an OpenSpec proposal) or a process suggestion (addressed via `AGENTS.md` or a subagent
definition). The agent SHALL propose the retrospective and suggestions without editing files; the main agent verifies
and applies them.

#### Scenario: A retrospective is produced from gathered experience

- **WHEN** the main agent gathers a digest of recent experience and invokes the `experience-analyzer` subagent
- **THEN** it returns a retrospective grouping shipped PRs by theme, summarizing lessons, and recording went-well and
  went-wrong observations

#### Scenario: Improvement suggestions are classified

- **WHEN** the `experience-analyzer` subagent proposes improvements
- **THEN** each suggestion is labeled as a system change (→ `docs/ideas.md` or a proposal) or a process change (→
  `AGENTS.md` or a subagent definition), so the main agent can route it

#### Scenario: The agent does not edit files itself

- **WHEN** the `experience-analyzer` subagent runs
- **THEN** it returns proposed text and suggestions without modifying files; the main agent verifies and applies them

### Requirement: Per-change quality-gate and analysis subagents are available

The repository SHALL provide locally-defined agent subagents under `.opencode/agent/` for the per-change quality gates
and analysis workflows — the per-change review and lesson-capture agents, and the visual and diagram agents — each
described by the scenarios below or by its own requirement in this capability. Each SHALL be invocable by the main
agent, with its purpose described in its agent definition and (where relevant) in `AGENTS.md`.

#### Scenario: Quick review runs after proposal and implementation

- **WHEN** a change's proposal (planning artifacts) or its implementation is finished
- **THEN** the `review-quick` subagent reviews it against the change's planning artifacts, and the loop repeats until it
  reports no new observations before a manual review is requested
- **AND** for a change whose diff adds `AGENTS.md` rules (a capture), it also challenges each new rule's durability —
  the decision the rule governs, and whether a future change would plausibly hit it — so trivia and restatements are
  caught at review time rather than by a later consolidation audit
- **AND** each finding is classified, and when the caller's request names a prior round's findings, a finding whose
  class a prior round already raised is reported as a repeat of that class, naming it — so the loop's convergence signal
  comes from the review's output rather than the caller's recollection, and the caller re-derives the root cause on that
  signal rather than patching the instance
- **AND** with no prior-round list supplied, the review still classifies each finding and states that a repetition is
  unknown rather than inferring one, since the classification cannot be compared against what it has not seen

#### Scenario: Thorough review is available on demand

- **WHEN** a deep review pass is wanted (drift, correctness, architecture, conventions)
- **THEN** the `review-thorough` subagent reviews the change against its delta specs, tasks, and the surrounding code

#### Scenario: Lessons are captured after implementation

- **WHEN** a change's implementation quick review is clean
- **THEN** the `lesson-capture` subagent proposes `AGENTS.md` gotchas/conventions from the change's lessons, which the
  main agent verifies and applies
- **AND** each proposed addition names the existing bullet it extends, or states that no bullet covers it — a new rule
  merges into or replaces one rather than accreting
- **AND** alongside its additions, it reports the rules this change makes obsolete or redundant — a rule whose mechanism
  the change removed, or one the change's new enforcement subsumes — as retirement or replacement candidates, each
  naming the rule, why the change makes it so, and the retirement or replacement it proposes, as candidates for the main
  agent rather than actions, reported separately from the additions since an addition grows the file and a retirement
  shrinks it
- **AND** each proposed addition passes a promotion gate before it is proposed — true (supported by a check, an
  authoritative source, or repeated observation), actionable, not automatable as a lint/test/CI check at reasonable
  cost, material (it prevents real breakage, risk, wasted work, or review churn), general enough for a class of future
  tasks rather than one file or incident, and high-confidence with a known scope — and a rule that fails the gate is
  routed to a check, a spec, an ADR, or the change dir instead of the always-loaded file
- **AND** each proposed addition names the decision its rule governs, and answers whether a future change would
  plausibly hit it and whether the cost of not knowing it is material — a proposal that governs no decision is trivia,
  not a rule, and is not proposed
- **AND** the gate's evidence threshold is two independent occurrences or one severe verified incident with a clear
  preventive action
- **AND** the gate treats the claim's source as part of it — a lesson sourced from untrusted content (a web page, an
  issue or PR comment, tool output, or a file the change did not author) is verified against the repository before it is
  proposed, never promoted on the source's word
- **AND** applying a capture should leave `AGENTS.md` no larger than it was, preferring a merge or a replacement over an
  addition, and any net growth is a justified decision stated with the proposal — not a side effect of accumulating
  prose
- **AND** when a rule's rationale is normative in a spec, the bullet keeps only what a reader needs to act and points at
  the spec — a pointer, not a condensed copy, since `AGENTS.md` is loaded on every invocation while a spec is loaded
  only when its capability is worked on
- **AND** when the specs describe a rule's subject only as an outcome, that mechanism belongs in the capability spec
  only when changing it would change a scenario's outcome — verified against the code before moving it; a mechanism
  whose alternatives yield the same result stays in `AGENTS.md` as internal control flow
- **AND** the control on that growth is the periodic `/audit-agents` pass, whose verdict already reports the
  accreted-rule count, rather than mass deletion to hit a number
- **AND** each proposed addition carries its origin in a greppable `captured: <change>` marker — the change, and its PR
  when a merge-time detection found the lesson rather than the implementation capture, since a capture runs once at
  implementation and a merge only detects and asks — so a reader can tell where the rule came from without consulting
  git, and a markdown reflow cannot take it

#### Scenario: A change that obsoletes a rule yields a retirement candidate

- **WHEN** a change's implementation makes an existing `AGENTS.md` rule obsolete (the mechanism it describes is removed)
  or redundant (the change's new enforcement subsumes it)
- **THEN** the `lesson-capture` subagent reports a retirement or replacement candidate naming the rule, why the change
  makes it so, and the retirement or replacement it proposes — a candidate for the main agent, not an action
- **AND** the capture's verdict names the retirement count alongside its durable-proposal count

#### Scenario: Screenshots are reviewed visually

- **WHEN** the main agent needs to inspect a screenshot, image, or visual UI state (e.g. web-UI styling)
- **THEN** the `vision` subagent reads the image and returns a description, so the text-only main agent can delegate
  visual review

#### Scenario: ASCII diagrams are drawn by the pro-model diagrammer

- **WHEN** a diagram needs to be created, aligned, or fixed (e.g. a README flow diagram)
- **THEN** the `diagrammer` subagent renders it with the pro model: it establishes the semantic mapping (which span
  starts and ends where), aligns by character width, and preserves deliberate asymmetry — so the cheap flash main agent
  does not spend effort on ASCII geometry

### Requirement: The spec corpus is audited for structure and consistency

The repository SHALL provide a `specs-auditor` agent subagent that audits the `openspec/specs/` corpus for **structure
and consistency**, and reports its findings without editing files. The audit SHALL cover, at least: each spec's `#`
title matching its capability path; each Purpose describing the requirements the spec holds; requirement-header and
requirement-body conventions (a declarative header, a SHALL body, WHEN/THEN scenarios); requirements duplicated or
near-duplicated across specs; and dead cross-references to renamed classes, files, or configuration the specs cite. The
audit SHALL NOT verify behavior against the implementation — that remains with the change workflow's review loop and the
archive-time spec sync. Each finding SHALL be verified against the repository, and SHALL be reported grouped by severity
with its location and a concrete suggested rewrite. A requirement header reused across two genuinely different
capabilities SHALL be reported for judgment rather than treated as a defect. The subagent SHALL propose its findings
without modifying files; the main agent verifies and applies those the user approves.

#### Scenario: A spec-corpus audit is produced

- **WHEN** the main agent invokes the `specs-auditor` subagent (e.g. via the `/audit-specs` command)
- **THEN** it returns findings grouped by severity, covering title/path match, Purpose fit, requirement conventions,
  cross-spec duplication, and dead cross-references, each with a location and a suggested rewrite

#### Scenario: Findings are verified against the repository

- **WHEN** the `specs-auditor` subagent flags a title, cross-reference, or apparent duplicate
- **THEN** it checks the claim against the repository (the capability path, the referenced file or symbol, the bodies of
  the requirements involved) before reporting it, so a still-true title or a legitimately shared header is not reported
  as a defect

#### Scenario: Behavior drift is out of scope

- **WHEN** the `specs-auditor` subagent audits the corpus
- **THEN** it does not verify requirement scenarios against the implementation, leaving behavior-vs-code drift to the
  change workflow's review loop and the archive-time spec sync

#### Scenario: The auditor does not edit files itself

- **WHEN** the `specs-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves

### Requirement: Project-owned agent tooling is audited for consistency and conciseness

The repository SHALL provide an `agents-auditor` agent subagent that audits the project-owned agent tooling —
`AGENTS.md` and the project-authored files under `.opencode/agent/`, `.opencode/commands/`, and `.opencode/skills/` —
for **consistency** and **conciseness**, and reports its findings without editing files. The audit SHALL exclude files
the repository does not author: the generator-owned OpenSpec instruction files (the commands and skills written by
`openspec update`) and the vendored `axon4to5-*` skills (copied verbatim from the upstream AxonIQ repository); a
project-authored file that shares a generated file's name prefix (e.g. the `opsx-tool-update` command) SHALL remain in
scope. The audit SHALL cover, at least: contradictions between entries or files, stale claims and enumerations the
repository has outgrown, dead cross-references, and drift from the code/workflows/specs the prose describes
(consistency); and duplicated or near-duplicate entries, one-off trivia, over-long or over-specific entries, and
misplaced entries (conciseness). Within conciseness the audit SHALL report three analyses as standing findings rather
than only under a scoped run: **merge candidates**, each naming the overlapping or complementary entries and the merged
text that preserves every anchor and piece of evidence the originals carried — and never blending two distinct lessons
into one — and where the merged text would only restate a rule the file already carries, the candidate is a deletion of
the duplicate rather than a merge, reported as a `remove` so the verdict's count includes it, and the audit SHALL say
which it is; **removal candidates**, each naming a rule that governs no decision (trivia, not a rule — the same test the
capture's filter applies), what would be lost, and whether git preserves it; and **route candidates**, each naming a
rule whose subject a deterministic mechanism already enforces — a build or CI gate, a lint or test, a CLI validation, or
a deterministic workflow step such as the archive-time spec sync, but never a review, whether a human's or a review
subagent's — and proposing to reduce the rule to a pointer at that mechanism, or move its content to the spec or ADR
that owns it, naming the mechanism, what would be lost, and confirming the mechanism exists and covers the rule's
subject. All are candidates for the owner, not actions. Each finding SHALL be verified against the repository rather
than inferred from the prose alone, and SHALL be reported with its location and a concrete suggested rewrite. A merge
candidate whose merged text delegates content to a target — a spec or an ADR it points the reader to — SHALL have that
delegation verified against the target: its behavior **and** the identifiers, declarations, and gate conditions the
delegated content names, not the behavior alone. A delegated item the target does not carry SHALL stay in the merged
text, and the candidate SHALL say which items it kept for that reason. The subagent SHALL propose its findings without
modifying files; the main agent verifies and applies those the user approves.

The audit SHALL additionally report, as an **advisory** class kept separate from its fix findings and reported without
severity, third-party inconsistency: a file the audit excludes (a generated `openspec-*` instruction file, or a vendored
`axon4to5-*` skill) that contradicts how this repository uses it. The class SHALL be bounded by a harm test — reported
only where the contradiction would mislead a workflow driven by the file, or instruct a pattern the repository's code or
conventions contradict, never a mere textual difference from the repository's own prose. Each item SHALL name the harm
and the decision it invites (report it upstream, re-vendor at a newer version, or change the repository's usage) and
SHALL NOT propose a local edit to the excluded file, which the exclusion rule forbids.

The audit SHALL additionally report, as an **accretion** class kept separate from both its fix findings and its advisory
class, the in-scope rules that are meta rather than product — a rule about the agent, its tooling, the per-change
workflow, or the documentation, as opposed to a fact about the product. Each item SHALL name the rule, the origin that
introduced it (established from the in-prose `captured:` marker where present, and from `git blame` / `git log -S`
otherwise), and the source used to establish it. The class SHALL be reported without severity and is not a defect: an
accreted rule may be correct and load-bearing, so the audit SHALL NOT propose removing or merging it for being meta — a
meta rule that governs no decision is a removal candidate under the conciseness analysis, not a victim of its class —
that is the user's decision on the report.

#### Scenario: An agent-tooling audit is produced

- **WHEN** the main agent invokes the `agents-auditor` subagent (e.g. via the `/audit-agents` command)
- **THEN** it returns findings grouped by severity, covering consistency (contradictions, stale claims, dead
  cross-references, drift) and conciseness (duplication, trivia, length, placement) across `AGENTS.md` and the
  project-authored `.opencode/` files, each with a location and a suggested rewrite — plus, separately, any advisory
  third-party inconsistency, the accreted meta rules with their origins, with the merge, removal, and route candidate
  counts named in the verdict line

#### Scenario: Generated and vendored files are excluded

- **WHEN** the `agents-auditor` subagent selects what to audit
- **THEN** it excludes the OpenSpec instruction files written by `openspec update` and the vendored `axon4to5-*` skills,
  whose correct state is defined by their generator or upstream rather than by this repository — while a
  project-authored file with a similar name (the `opsx-tool-update` command) stays in scope

#### Scenario: Findings are verified against the repository

- **WHEN** the `agents-auditor` subagent flags a claim, enumeration, or cross-reference
- **THEN** it checks the claim against the repository (the referenced files, workflows, or specs) before reporting it,
  so a deliberate or still-true entry is not reported as stale

#### Scenario: The auditor does not edit files itself

- **WHEN** the `agents-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves

#### Scenario: An excluded file inconsistent with our usage is reported as advisory

- **WHEN** an excluded file (a generated instruction file or a vendored skill) contradicts how this repository uses it —
  for example a vendored migration skill instructing a pattern the codebase no longer follows
- **THEN** the audit reports it in a separate advisory class, naming the harm and the decision it invites (report
  upstream, re-vendor, or change our usage), and never proposes a local edit to that file

#### Scenario: A mere textual difference is not reported

- **WHEN** an excluded file differs from the repository's own prose but would not mislead a workflow or instruct a
  contradicted pattern
- **THEN** the audit does not report it, so the advisory class stays limited to contradictions that would change what a
  reader does

#### Scenario: Accreted meta rules are reported with their origin

- **WHEN** the `agents-auditor` subagent audits `AGENTS.md`
- **THEN** it reports the in-scope rules that are meta rather than product as a separate accretion class, each with the
  origin that introduced it (the `captured:` marker where present, `git blame` / `git log -S` otherwise) and the source
  used, without proposing that the rule be removed or merged for being meta

#### Scenario: Merge and removal candidates are standing findings

- **WHEN** the `agents-auditor` subagent audits `AGENTS.md`
- **THEN** it reports merge candidates (with a merged text that preserves every anchor and piece of evidence the
  originals carried, and never blending two distinct lessons — or, where that text would only restate a rule the file
  already carries, a deletion of the duplicate, still counted as a `remove`, saying which) and removal candidates (rules
  that govern no decision — trivia, not a rule — with what would be lost and whether git preserves it) — as findings for
  the owner, not actions

#### Scenario: A merge candidate's pointer target is verified

- **WHEN** a merge candidate's merged text points at a spec or an ADR for content it removes
- **THEN** the audit verifies the target carries the delegated content — its behavior and the identifiers, declarations,
  and gate conditions the delegated text names — and keeps in the merged text any delegated item the target does not
  carry, saying which it kept for that reason

#### Scenario: A rule enforced by a deterministic mechanism is a route candidate

- **WHEN** the `agents-auditor` subagent finds a rule whose subject an in-place deterministic mechanism already enforces
  (a build or CI gate, a lint or test, a CLI validation, or a deterministic workflow step such as the archive-time spec
  sync)
- **THEN** it reports a route candidate naming the mechanism and proposing to reduce the rule to a pointer at it, or to
  move the rule's content to the spec or ADR that owns it, with what would be lost — as a candidate for the owner, not
  an action

#### Scenario: A rule detectable only by review is not a candidate

- **WHEN** the only detector of a rule's violation is a review — a human's or a review subagent's — and no deterministic
  mechanism enforces it
- **THEN** the audit does not report the rule as a removal or route candidate, because review is not a gate and the rule
  may itself be what makes the violation visible to the reviewer

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

### Requirement: Report-producing subagents follow a shared report contract

Every subagent that reports findings to the main agent — the per-change review and lesson-capture agents and the
on-demand auditors of the agent tooling, the spec corpus, the architecture, and the README — SHALL follow one shared
output contract, so a report is skimmable and its verdict is read first. The contract SHALL bound the report, not the
analysis: a subagent SHALL verify its claims as thoroughly as before and report them in the contract's shape.

#### Scenario: A report opens with its verdict

- **WHEN** a report-producing subagent returns its findings
- **THEN** the first line states the outcome — whether anything remains and how many items — and names the count of
  every class the report carries (findings, and any advisory or accretion class it reports)
- **AND** when the caller's request named a prior round's findings, that first line also names which of the report's
  classes repeat a class the prior round already raised
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

### Requirement: The on-demand audits are also available on a schedule

The repository's audits of its agent tooling, its spec corpus, and its architecture SHALL be runnable unattended on a
schedule, in addition to their on-demand `/audit-*` triggers, so the reconciliation they perform does not depend on a
human asking. The scheduled run SHALL perform the audits through the OpenCode GitHub action's scheduled path — which
requires a `prompt` input, authenticates by OIDC (`id-token: write`), and produces a branch or pull request rather than
an issue — and SHALL batch them into one run whose findings land in a single committed report the action carries into a
pull request, so the pro-model cost is one run per period rather than one per audit. Its cadence SHALL serve the
reconciliation the audits exist for (the consolidation that counters the accretion the capture loop produces) rather
than being a bare reminder: a run with nothing to report SHALL commit nothing and so produce no artifact. The
`readme-auditor` SHALL remain on-demand only rather than joining the scheduled set: it verifies the human-facing
`README.md` against the repository, an accuracy-and-coverage check distinct from the reconciliation of the agent's own
machinery the schedule exists for. The `experience-analyzer` SHALL likewise remain on-demand only: its retrospective is
a narrative judgment about a period rather than a reconciliation verifiable against the repository, and its richest
input — what went wrong that no diff captures — is available only in the session that lived it, so a scheduled run could
not supply it. A scheduled run that _produces_ the retrospective SHALL NOT be used in its place, because it automates
the decision rather than the trigger and still lacks the session-only context; a scheduled check that only _reports_
what has accumulated since the newest retrospective, and otherwise stays silent, is not such a run — it surfaces the
trigger while leaving the decision and the analysis to the on-demand invocation.

#### Scenario: The audits run unattended on their schedule

- **WHEN** the scheduled audit workflow fires
- **THEN** it runs the agents-auditor, specs-auditor, and architecture-auditor audits in one unattended agent run and
  reports their findings together

#### Scenario: The scheduled audits do not replace the on-demand triggers

- **WHEN** a maintainer invokes an audit on demand (e.g. `/audit-agents`)
- **THEN** the on-demand trigger still runs that single audit, and the scheduled workflow's existence does not change it
