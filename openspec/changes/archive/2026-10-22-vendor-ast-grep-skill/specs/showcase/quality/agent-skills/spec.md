## MODIFIED Requirements

### Requirement: Vendored agent skills are available to agents

The repository SHALL vendor two upstream skill sets under `.opencode/skills/`, so that opencode discovers them as
project skills, and each set's provenance SHALL name it separately (see "Vendored skills carry version provenance"). The
AxonIQ set SHALL be the `axoniq-migration` plugin skills (version 0.2.2) from the `AxonIQ/agent-skills` repository, and
SHALL include `axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`. The structural-search set
SHALL be the `ast-grep` skill (version 1.0.0) from the `ast-grep/agent-skill` repository, whose `SKILL.md` entry point
names its `references/rule_reference.md`. The structural-search skill SHALL remain discoverable without its CLI being
installed — discovery is file-based, and the missing CLI is reported as an unsatisfied optional prerequisite rather than
an unavailable skill. Vendoring SHALL NOT add the skill files to any Gradle module's classpath or to any built Docker
image.

#### Scenario: Migration skills are discoverable by agents

- **WHEN** an agent lists the project's skills in `.opencode/skills/`
- **THEN** it finds `axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`, each with a `SKILL.md`
  entry point and its referenced assets

#### Scenario: Vendored skills do not affect the application build

- **WHEN** the project build runs any Gradle task (compile, test, package)
- **THEN** the vendored skill files under `.opencode/skills/` are not part of any module's source set, classpath, or
  Docker image

#### Scenario: The structural-search skill is discoverable by agents

- **WHEN** an agent lists the project's skills in `.opencode/skills/`
- **THEN** it finds `ast-grep`, with its `SKILL.md` entry point and the `references/rule_reference.md` its entry point
  names, and the reference is readable at that relative path from the skill's directory

#### Scenario: The structural-search skill is available while its CLI is missing

- **WHEN** the `ast-grep` CLI is not installed on the machine
- **THEN** the skill is still discovered, and the toolchain diagnostic reports the missing CLI as an unsatisfied
  optional prerequisite with its install command, rather than the skill being unavailable

### Requirement: Vendored skills carry version provenance

The vendored skills SHALL record, per set, their upstream source repository, the exact plugin version taken from the
upstream plugin manifest, the upstream path the copy came from, the license, and the upstream commit whose tree the
vendored files correspond to. An entry MAY also record the vendored files' git blob hashes. Checking the current copy
SHALL compare it with the recorded commit — or with those hashes where the entry records them — and an update SHALL
compare the new copy with the upstream snapshot it is taken from and refresh the recorded provenance; neither check
constrains how the upstream content is obtained. A vendored tree SHALL be copied verbatim: it SHALL NOT be reformatted
by the build, and no project-owned gate SHALL require its own markers in it.

#### Scenario: Provenance is recorded

- **WHEN** a maintainer inspects the vendored skills or the `agent-skills` capability spec
- **THEN** each set's upstream source, plugin, version, upstream path, license, and commit are documented —
  `AxonIQ/agent-skills` `axoniq-migration` 0.2.2 at `518acd865a`, and `ast-grep/agent-skill` `ast-grep` 1.0.0 at
  `f2175af` — and the `ast-grep` entry also records its files' blob hashes, so that copy is checkable with
  `git hash-object` alone

#### Scenario: Updating to a new plugin version is an explicit change

- **WHEN** a new plugin version is published upstream for either vendored set
- **THEN** refreshing that set is performed as a reviewed change that updates the recorded version, commit, and every
  recorded blob hash, rather than applied silently

#### Scenario: A vendored tree stays byte-identical to upstream

- **WHEN** the build's formatter or a project-owned gate runs over the repository
- **THEN** the vendored trees are excluded from both, so a vendored file differs from its upstream copy only when a
  refresh change replaces it

### Requirement: Project-owned agent tooling is audited for consistency and conciseness

The repository SHALL provide an `agents-auditor` agent subagent that audits the project-owned agent tooling —
`AGENTS.md`, the project-authored files under `.opencode/agent/`, `.opencode/commands/`, and `.opencode/skills/`, and
the guidance evidence records under `docs/guidance-evidence/` — for **consistency** and **conciseness**, and reports its
findings without editing files. The audit SHALL exclude files the repository does not author: the generator-owned
OpenSpec instruction files (the commands and skills written by `openspec update`) and the vendored skill trees
(`axon4to5-*` and `ast-grep`) (each copied verbatim from its upstream repository); a project-authored file that shares a
generated file's name prefix (e.g. the `opsx-tool-update` command) SHALL remain in scope. The audit SHALL cover, at
least: contradictions between entries or files, stale claims and enumerations the repository has outgrown, dead
cross-references, and drift from the code/workflows/specs the prose describes (consistency); and duplicated or
near-duplicate entries, one-off trivia, over-long or over-specific entries, and misplaced entries (conciseness). Within
conciseness the audit SHALL report these analyses as standing findings rather than only under a scoped run: **merge
candidates**, each naming the overlapping or complementary entries and the merged text that preserves every anchor and
piece of evidence the originals carried — and never blending two distinct lessons into one — and where the merged text
would only restate a rule the file already carries, the candidate is a deletion of the duplicate rather than a merge,
reported as a `remove` so the verdict's count includes it, and the audit SHALL say which it is; **removal candidates**,
each naming a rule that governs no decision (trivia, not a rule — the same test the capture's filter applies), what
would be lost, and whether git preserves it; and **route candidates**, each naming a rule whose subject a deterministic
mechanism already enforces — a build or CI gate, a lint or test, a CLI validation, or a deterministic workflow step such
as the archive-time spec sync, but never a review, whether a human's or a review subagent's — and proposing to reduce
the rule to a pointer at that mechanism, or move its content to the spec or ADR that owns it, naming the mechanism, what
would be lost, and confirming the mechanism exists and covers the rule's subject; and **extraction candidates**, each a
verified proposal to partition one `AGENTS.md` unit into a minimal retained root entry point, an authoritative
destination that loads or is discoverable when its trigger occurs, and an explicit retrieval path from that trigger to
that destination, as the conditions below define. All are candidates for the owner, not actions. Each finding SHALL be
verified against the repository rather than inferred from the prose alone, and SHALL be reported with its location and a
concrete suggested rewrite. A reduction candidate — a merge, removal, route, or extraction — SHALL carry seven fields:
its location; the decision its rule changes; the task or condition that requires it; the unique content the change would
otherwise lose (every imperative, warning, exception, identifier, rationale, provenance marker, and verification); its
current loading cost; its disposition; and the repository evidence that the surviving guidance preserves the same
obligation. A merge candidate whose merged text delegates content to a target — a spec or an ADR it points the reader to
— SHALL have that delegation verified against the target: its behavior **and** the identifiers, declarations, and gate
conditions the delegated content names, not the behavior alone. A delegated item the target does not carry SHALL stay in
the merged text, and the candidate SHALL say which items it kept for that reason. The subagent SHALL propose its
findings without modifying files; the main agent verifies and applies those the user approves.

The audit SHALL additionally report an **instruction-budget ledger** for `AGENTS.md`, measuring the always-loaded
instruction surface rather than judging it: the file's total size in bytes and lines; its size by top-level section; the
effective bound on that surface together with the source establishing it, stating whether the loading mechanism applies
a limit of its own; the first rule that falls beyond a derived effective threshold, expressed as an estimate with its
derivation rather than as a position at which content is dropped; the growth since the newest prior report under
`docs/audits/`; and the sections and `captured:`-marked rule units responsible for that growth. The ledger SHALL
estimate the bytes safely removable from the always-loaded surface, SHALL list the rules that must remain loaded with
the risk their unavailability would carry, SHALL list the rules safe to load on demand with their trigger and retained
pointer, SHALL tabulate the extraction candidates with their destination type and retrieval-path result, SHALL carry a
separate **extraction opportunities** subsection after the ready candidates — excluded from the verdict counts, stating
that its items require the owner's selection and a dedicated design or implementation change, distinguishing an existing
destination that failed retrieval from a destination that does not yet exist, and collapsing "no opportunity" to one
line — and SHALL state the post-change verification: re-measurement, pointer-target verification, retrieval-path tests,
a search for lost unique terms, and the review gate. The ledger SHALL NOT invent a limit the loading mechanism does not
apply, and a bound it reports as configured SHALL cite the configuration that sets it.

The audit SHALL keep root `AGENTS.md` guidance scoped to what an agent must see before safely starting work: a rule a
narrower task scope governs, and whose destination is durable, project-authored, owned, and loaded or deterministically
discoverable when that scope's trigger occurs, is a candidate to move out of the always-loaded surface. The audit SHALL
NOT propose a move that would leave the reader to infer a warning, an imperative, an exception, or a target-less fact
the retained root text does not state, and SHALL NOT report a rule as a removal candidate merely for being old,
uncommon, or lengthy — the accretion class's rule for a meta rule governs that case. Where the passage's intended
structure or meaning cannot be recovered confidently, the audit SHALL report the ambiguity rather than invent a rewrite.

The audit SHALL propose an **extraction candidate** only where every one of these holds: the candidate governs a
narrower task scope than root `AGENTS.md`; its destination is durable, project-authored, and owned; that destination is
loaded or deterministically discoverable when the trigger occurs; the retained root text lets an agent identify the
trigger and the required next action without reconstructing them; the move preserves every unique imperative, warning,
exception, identifier, verification, and provenance marker; the destination already carries, or the proposed change to
it will carry, the complete transferred contract; the move reduces the always-loaded surface by a material, measured net
amount — the removed span less the retained root text and any always-loaded destination metadata, with the destination's
on-demand load and the frequency of its trigger stated, and the total maintained content before and after; and it does
not split a procedure across files such that a reader must guess an order, condition, or failure path. The audit SHALL
derive the preferred destination type from the content's own class before selecting one: a global safety, branch,
review, or repository-wide workflow **invariant** — its authorization boundary, its entry trigger, any ordering that
binds across the stage, and the missing-destination handling — stays at root `AGENTS.md`, while that workflow's
stage-specific execution mechanics (its ordered steps, commands, decision branches, and recovery handling) MAY move to a
project-owned destination only where the retained root entry point requires reading that destination completely before
the governed decision. This permission is limited to a stage whose trigger is that decision: it does not extend to
guidance needed across more than one stage or before a stage can be identified, to a mechanism whose change would change
a capability's specified behavior, or to a warning, imperative, or exception the retained root text leaves to be
inferred; guidance needed only while working below one unambiguous directory goes to a nested `AGENTS.md`; behavior that
changes a capability outcome to an existing or proposed capability spec; a durable architecture decision, rationale, or
trade-off to an existing or proposed ADR; a procedure invoked through a named task entry point to an existing command or
skill; incident history, diagnostic evidence, or a large reference inventory used by several future tasks to an existing
durable reference document; and a subject a deterministic mechanism enforces to that mechanism's authoritative
configuration or test, with a concise root pointer. The audit SHALL NOT propose a generic "documentation" destination
and SHALL NOT create a catch-all extracted-guidance directory, and a proposed new destination SHALL have a clear owner,
a stable scope, and evidence that more than one future task can discover and use it. Internal control flow whose
alternatives yield the same result SHALL stay in `AGENTS.md`, except where it is a workflow's stage-specific execution
mechanics that satisfy the permission above, in which case it MAY move to that workflow's project-owned procedure
destination; it SHALL NOT move into a capability spec even where the spec describes the same outcome, and only a
mechanism whose change would change the capability's specified behavior is a candidate for a capability-spec
destination.

A technical subject whose decisions recur across the proposal, implementation, review, and diagnosis stages — a
language's production conventions, its test conventions, a build system — MAY move to a project-owned **subject pack**.
The permission holds only where all of these conditions do:

- the subject is narrower than the repository;
- the root `AGENTS.md` carries a classification table naming the subject's decision trigger, the pack's path, and the
  missing-pack stop;
- the pack loads in full before the first governed decision in the subject;
- a task outside the subject can complete without it;
- every extraction condition above holds;
- the moved content is not a cross-cutting safety, branch, review, git, evidence, or documentation rule.

The subject-scope permission is the only exception to the multi-stage limit; the capability-mechanism limit and the
warning-inference limit apply to it unchanged. It does not create a catch-all guidance directory, does not permit a
nested file for a subject spanning several directories, and does not permit an `instructions` entry. The classification
table's row is the retained root entry point, and the pack's `Source:` line is the source↔destination association.
Before reporting a candidate the audit SHALL test the retrieval path — starting from the root instruction set, stating
the task trigger, following only the retained root text and normal project discovery, and confirming the destination is
reached before the governed decision must be made and carries every transferred requirement and verification step. A
failed retrieval path to an **existing** destination remains a reason to retain the source content, and is not an
extraction opportunity. For an opportunity the audit SHALL NOT claim that a destination which does not exist passed the
test: instead it SHALL verify whether the proposed retained root entry point could name the trigger and the destination
deterministically, record the missing target or discovery mechanism as the blocking precondition, and require the
dedicated change to create the target, add the retained root pointer, and run the full test before the extraction
becomes applicable.

The audit SHALL additionally report, as an **extraction opportunity** kept separate from its findings, a source unit
that is likely too narrow or too costly for root `AGENTS.md` but whose destination does not yet exist — and which
therefore cannot yet pass the retrieval-path test. An opportunity is not a finding: it SHALL NOT be counted in the
verdict line, SHALL NOT be reported as a removal or a route candidate, and SHALL NOT be applied by the standing
reduction unit — it is a design recommendation for the owner, who selects it and carries it out in a dedicated change.
The audit SHALL report an opportunity only where the source is materially costly in root guidance and the destination
type can be justified from the content's class, and it SHALL NOT report one merely to reduce the file's size: where the
source is global or cross-cutting — guidance needed across more than one stage, or before a stage can be identified — or
cannot be retrieved before its decision is needed, the audit SHALL retain it at root. Each opportunity SHALL state that
its proposed destination is a design hypothesis rather than an existing source of truth, and SHALL carry the source unit
and its bounded content, the decision it governs and when it is needed, its current loading cost and why that cost is
material, its content classification, its recommended destination type, the exact existing target or a proposed path
with its owning artifact type, the missing precondition that keeps it from being a candidate, the content that must
remain at root until a destination is created and verified, the intended root-to-destination retrieval path once a
future change creates the target, and one recommended next action — retain, create the destination in a dedicated
change, or reject the extraction.

The audit SHALL additionally report, as an **advisory** class kept separate from its fix findings and reported without
severity, third-party inconsistency: a file the audit excludes (a generated `openspec-*` instruction file, or a vendored
skill) that contradicts how this repository uses it. The class SHALL be bounded by a harm test — reported only where the
contradiction would mislead a workflow driven by the file, or instruct a pattern the repository's code or conventions
contradict, never a mere textual difference from the repository's own prose. Each item SHALL name the harm and the
decision it invites (report it upstream, re-vendor at a newer version, or change the repository's usage) and SHALL NOT
propose a local edit to the excluded file, which the exclusion rule forbids.

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

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: An agent-tooling audit is produced

- **WHEN** the main agent invokes the `agents-auditor` subagent (e.g. via the `/audit-agents` command)
- **THEN** it returns findings grouped by severity, covering consistency (contradictions, stale claims, dead
  cross-references, drift) and conciseness (duplication, trivia, length, placement) across the audited project-owned
  tooling — `AGENTS.md`, the project-authored `.opencode/` files, and the `docs/guidance-evidence/` records, each with a
  location and a suggested rewrite — plus, separately, any advisory third-party inconsistency, the accreted meta rules
  with their origins, and any authoring-system gap, with the merge, removal, and route candidate counts named in the
  verdict line

#### Scenario: Generated and vendored files are excluded

- **WHEN** the `agents-auditor` subagent selects what to audit
- **THEN** it excludes the OpenSpec instruction files written by `openspec update` and the vendored skill trees
  (`axon4to5-*` and `ast-grep`), whose correct state is defined by their generator or upstream rather than by this
  repository — while a project-authored file with a similar name (the `opsx-tool-update` command) stays in scope

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

#### Scenario: An instruction-budget ledger is reported

- **WHEN** the `agents-auditor` subagent audits `AGENTS.md`
- **THEN** it reports an instruction-budget ledger naming the file's total size, its size by top-level section, the
  effective bound on the always-loaded surface and its source, the first rule beyond the derived effective threshold,
  the growth since the newest prior report under `docs/audits/`, and the sections and `captured:`-marked rule units
  responsible for that growth

#### Scenario: The ledger states the bound's source and invents no limit

- **WHEN** the loading mechanism applies no size limit of its own
- **THEN** the ledger says so and reports the effective bound with the source that establishes it, rather than reporting
  a configured limit that does not exist

#### Scenario: A reduction candidate carries its decision and its cost

- **WHEN** the auditor reports a merge, removal, route, or extraction candidate
- **THEN** the candidate names its location, the decision its rule changes, the task or condition that requires it, the
  unique content the change would otherwise lose, its current loading cost, its disposition, and the evidence that the
  surviving guidance preserves the same obligation

#### Scenario: An extraction candidate meets every condition

- **WHEN** the auditor proposes an extraction candidate
- **THEN** every condition the extraction analysis states holds, and the candidate names its destination type, the
  retained root text, and the authoritative destination

#### Scenario: A subject pack is a valid extraction destination

- **WHEN** the auditor considers a source unit whose decisions all belong to one technical subject named by the root
  classification table
- **THEN** it reports an extraction candidate with the subject pack as the destination only where the table names the
  trigger and the pack path, the missing-pack stop is stated, and every preservation and retrieval condition holds

#### Scenario: A failed retrieval path is a reason to retain the content

- **WHEN** the retrieval-path test cannot reach the destination before the governed decision must be made, or the
  destination omits a transferred requirement or verification step
- **THEN** the audit reports the failure and retains the content rather than proposing the extraction

#### Scenario: Internal control flow is not extracted

- **WHEN** a spec describes the same outcome but changing the rule's mechanism would not change the capability's
  specified behavior
- **THEN** the audit keeps that control flow out of the capability spec; it stays in `AGENTS.md` unless it is
  stage-specific execution mechanics that satisfy the complete-read permission

#### Scenario: A rule is not a removal candidate for being lengthy

- **WHEN** a rule is old, uncommon, or lengthy but governs a decision
- **THEN** it is not reported as a removal candidate, and the reason it stays is stated

#### Scenario: Guidance is not moved where the reader would infer it

- **WHEN** the retained root text would leave a warning, an imperative, an exception, or a target-less fact to be
  inferred
- **THEN** the audit keeps the rule at root rather than proposing the move

#### Scenario: An extraction opportunity is reported where no destination exists

- **WHEN** a source unit is materially costly in root guidance but no existing project-authored destination owns its
  subject
- **THEN** the audit reports an extraction opportunity naming its content classification, its recommended destination
  type, the proposed destination with its owning artifact type, and the missing precondition, and states that the
  proposed destination is a design hypothesis rather than an existing source of truth

#### Scenario: An extraction opportunity is not a finding and is never applied

- **WHEN** the audit reports an extraction opportunity
- **THEN** it is excluded from the verdict line, is not reported as a removal or a route candidate, and the standing
  reduction unit never applies it

#### Scenario: A global rule is retained rather than extracted

- **WHEN** the source is needed across more than one stage, before a stage can be identified, or cannot be retrieved
  before its decision is needed
- **THEN** the audit retains it at root and reports no extraction opportunity for it

#### Scenario: A failed path to an existing destination is not an opportunity

- **WHEN** a destination exists but the retrieval path to it fails
- **THEN** the audit reports the failure as a reason to retain the source content, not as an extraction opportunity

#### Scenario: A proposed new destination needs an owner and a scope

- **WHEN** the audit proposes a destination that does not exist yet
- **THEN** it names the owning artifact type, a stable scope, and evidence that more than one future task can discover
  and use it, and it proposes no generic documentation destination and no catch-all extracted-guidance directory

#### Scenario: The ledger carries the opportunities subsection

- **WHEN** the audit reports its instruction-budget ledger
- **THEN** a separate extraction-opportunities subsection follows the ready candidates, is excluded from the verdict
  counts, states that its items need the owner's selection and a dedicated design or implementation change,
  distinguishes an existing destination that failed retrieval from a destination that does not yet exist, and collapses
  "no opportunity" to one line

#### Scenario: An evidence section whose rule is gone is a pairing finding

- **WHEN** a section of a `docs/guidance-evidence/` record carries a `Rule:` paragraph that matches no live `AGENTS.md`
  rule
- **THEN** the audit reports it as a pairing finding, naming the record's path and the section's heading

#### Scenario: A converted rule whose evidence section is missing is a pairing finding

- **WHEN** a rule carries an `evidence:` token naming a record that holds no section whose `Rule:` paragraph matches
  that rule
- **THEN** the audit reports it as a pairing finding, naming the rule and the record

#### Scenario: A section matching a rule that carries no token is a pairing finding

- **WHEN** a record section's `Rule:` paragraph matches a live `AGENTS.md` rule that carries no `evidence:` token
- **THEN** the audit reports it as a pairing finding, naming the section and the rule

#### Scenario: A stage procedure's mechanics move behind a named entry point

- **WHEN** the auditor weighs an extraction whose source is a workflow stage triggered by a request
- **THEN** the candidate keeps the stage trigger, the authorization boundary, the cross-stage ordering, and the
  missing-destination handling at root, names the project-owned destination the entry point requires reading completely
  before the governed decision, and reports the removed span, the retained root text, the added always-loaded metadata,
  the on-demand load, the trigger's frequency, and the total maintained content
