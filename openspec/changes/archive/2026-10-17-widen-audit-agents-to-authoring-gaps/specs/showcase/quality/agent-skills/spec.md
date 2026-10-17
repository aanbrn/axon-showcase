## MODIFIED Requirements

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
that is the user's decision on the report. The class's count SHALL use one fixed metric, so successive reports are
comparable: the number of distinct meta rule units carrying at least one `captured:` marker, each counted once however
many markers it carries — never the raw marker count, and never the number of items the report lists — and a rule
written before the marker convention carries none and falls outside the metric. The audit SHALL also state how many of
those rule units are new since the newest prior report under `docs/audits/`, reporting `none recorded` when no prior
report exists and that the figure is unavailable when it cannot be determined.

The audit SHALL additionally report, as an **authoring-system gap** class kept separate from its fix findings and
reported without severity, a recurring defect whose cause is a mechanism that produces or maintains the artifact rather
than the artifact's content. Each item SHALL name the mechanism, the gap in how that mechanism communicates or enforces
the applicable standard, the smallest correction at the mechanism, and how that correction would prevent or reduce the
recurrence. The class SHALL be bounded by evidence: a defect reported from a single artifact with no established link to
the mechanism that produced it is a content defect under the consistency axis, not an authoring-system gap, and the
class SHALL NOT become a route for speculative rules. The correction's handling SHALL follow the mechanism's scope: for
an in-scope mechanism (an agent definition, command, or skill under `.opencode/`) the audit proposes the correction
directly; for an editable project-owned mechanism outside the audit's scope (the `openspec/config.yaml` per-artifact
rules) it reports the gap with the correction routed to the change workflow, never applied as a local edit; and for an
excluded mechanism (a generated OpenSpec instruction file, or a vendored skill) it reports the item under the advisory
class and never proposes a local edit to it. A correction SHALL reference the authoritative standard rather than copy it
into the mechanism.

#### Scenario: An agent-tooling audit is produced

- **WHEN** the main agent invokes the `agents-auditor` subagent (e.g. via the `/audit-agents` command)
- **THEN** it returns findings grouped by severity, covering consistency (contradictions, stale claims, dead
  cross-references, drift) and conciseness (duplication, trivia, length, placement) across `AGENTS.md` and the
  project-authored `.opencode/` files, each with a location and a suggested rewrite — plus, separately, any advisory
  third-party inconsistency, the accreted meta rules with their origins, and any authoring-system gap, with the merge,
  removal, and route candidate counts named in the verdict line

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

#### Scenario: The accretion count uses one fixed metric

- **WHEN** the `agents-auditor` subagent reports its accretion class
- **THEN** its verdict states the count as the number of distinct meta rule units carrying at least one `captured:`
  marker, each counted once regardless of how many markers it carries, together with the number of those rule units new
  since the newest prior report under `docs/audits/` (`none recorded` when no prior report exists), so the count is
  comparable across successive reports

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

#### Scenario: A recurring defect is traced to the authoring mechanism

- **WHEN** the `agents-auditor` subagent finds a defect that recurs across the artifacts one mechanism produces — a
  skill that keeps emitting dense procedural prose, or a command that omits a step the workflow needs
- **THEN** it reports an authoring-system gap naming the mechanism, the gap in how that mechanism communicates or
  enforces the standard, the smallest correction at the mechanism, and how the correction would prevent or reduce the
  recurrence — separate from its fix findings and without severity

#### Scenario: A one-off defect is not an authoring-system gap

- **WHEN** a defect appears in a single artifact and no link to the mechanism that produced it is established
- **THEN** it is reported as a content defect under the consistency axis, not as an authoring-system gap

#### Scenario: An authoring-system gap in an excluded mechanism is advisory

- **WHEN** the mechanism responsible for a recurring defect is excluded (a generated OpenSpec instruction file, or a
  vendored skill)
- **THEN** the audit reports the gap under the advisory class, naming the harm and the decision it invites, and never
  proposes a local edit to the excluded mechanism

### Requirement: Agent-authored technical artifacts follow a structured writing standard

Agent-authored technical artifacts — `AGENTS.md`, OpenSpec proposals, specifications, designs, and task lists, plans,
code reviews, and other repository documentation — SHALL follow the structured, scannable shape `AGENTS.md`'s
"Structured Writing Standard" section states, applied while the artifact is drafted rather than only in a later cleanup
pass, and each SHALL keep its own required template and sections.

#### Scenario: A requirement is stated as its own scannable unit

- **WHEN** an agent authors an artifact that states several independent requirements, actions, or decisions
- **THEN** each is a separate bullet or numbered step rather than several packed into one paragraph, and an ordered
  procedure is numbered

#### Scenario: A condition is explicit

- **WHEN** the next action depends on a condition
- **THEN** the artifact states the condition and its branch (`IF`/`THEN`-style, or a decision-table row) rather than
  leaving the dependency implied

#### Scenario: The mandatory is separated from the explanatory

- **WHEN** an artifact states a normative requirement together with its rationale, background, or an example
- **THEN** the obligation is distinguishable from the explanation, so a reader finds what must be done without reading
  why

#### Scenario: The standard governs drafting, not only a later pass

- **WHEN** an agent creates or substantially edits an artifact
- **THEN** it organizes the intended content into its sections and lists before writing the prose, instead of leaving
  the structure to a formatting pass after the fact

#### Scenario: Artifact-specific conventions are preserved

- **WHEN** the standard is applied to an artifact with its own required format (an OpenSpec proposal, specification,
  design, or task list, an ADR, or a review report)
- **THEN** the artifact keeps every required section and semantic distinction (SHALL statements, `####` WHEN/THEN
  scenarios, task checkboxes), the standard shaping the content within those sections

#### Scenario: Dense prose is restructured on review

- **WHEN** an artifact is reviewed (`review-quick`, `review-thorough`) or audited (`agents-auditor`) and a passage packs
  several independently actionable statements into one paragraph or bullet, implies a condition, or merges mandatory
  text with its rationale
- **THEN** it is reported as a finding, with its location, the practical impact of the current structure, and a
  minimally invasive restructure, without changing the passage's technical meaning, normative strength, or required
  format, and without introducing a requirement the passage did not state
