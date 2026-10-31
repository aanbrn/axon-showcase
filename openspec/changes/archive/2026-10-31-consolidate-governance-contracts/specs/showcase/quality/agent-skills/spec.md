## MODIFIED Requirements

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

A reference SHALL be reported as dead only where the referenced artifact is verified absent or obsolete — its exact name
and variants searched and its rename, relocation, replacement, and current context checked — and SHALL NOT be reported
where it merely cannot be located, where a filename differs in case or extension from an informal mention, where the
class is generated or produced by a build, where the command belongs to an external tool, where the configuration key is
defined indirectly, where the name appears only in documentation, or where the reference is conceptual rather than an
exact symbol. A stale reference SHALL be corrected to its verified successor, or removed where the referenced concept no
longer applies, without rewriting the requirement's behavior.

Two similar requirements SHALL be compared semantically — the obligation, the actors and components, the conditions, the
observable outcomes, and the scope, ownership, and exceptions — and SHALL be reported only where the obligations are
substantially identical or their overlap leaves ownership or precedence materially ambiguous; a requirement that is
related but distinct (the same concept with a different actor, boundary, condition, outcome, or responsibility) and an
intentional repetition (a shared convention repeated so each capability stays self-contained) SHALL NOT be reported as a
defect. Before recommending a consolidation the audit SHALL read both requirement bodies and their scenarios and the
surrounding Purposes, establish the owners and scopes, and establish what a merge would lose.

The audit SHALL NOT audit the corpus's capability decomposition (whether the role groups still match the module and
service structure), which the `architecture-auditor` owns; SHALL NOT re-report an issue an existing gate already
enforces (the `openspec validate` well-formedness, the review loop's behavior check); and SHALL NOT audit the internal
quality of a proposed change or change delta, which the review loop owns.

A requirement heading, a Purpose, or a requirement body SHALL be reported only for a violation of a stated convention: a
heading containing a verb, a technical term, or an established expression is not malformed, a valid normative body is
not reported for carrying explanatory prose, an incidental occurrence of `SHALL` that expresses no obligation is not
sufficient, and a scenario is checked by its Markdown structure rather than by a pattern match alone.

Confidence SHALL be an internal filter, not a report category: the audit SHALL NOT attach confidence or severity scores,
and SHALL omit a candidate whose evidence shows an intentional, coherent choice.

Symptoms sharing a root cause and a correction SHALL be reported as one finding, and independent defects SHALL NOT be
merged, repeated across severity groups, or padded with a placeholder.

The audit SHALL proceed in stages — inventory, local structure, cross-spec consistency, reference verification, report
validation — reusing the evidence an earlier stage gathered.

Every item the report carries SHALL also carry the shared report contract's per-item budget, owned by
`docs/contracts/shared-report.md`, not this one.

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

#### Scenario: A dead reference is verified before it is reported

- **WHEN** the `specs-auditor` subagent finds a class, file, configuration key, or command a spec names that it cannot
  locate
- **THEN** it searches the exact name and its variants and checks renames, relocations, generated artifacts,
  replacements, and the reference's current context before reporting it, and it does not report a case/extension
  difference, a generated class, an external tool's command, an indirectly-defined configuration key, a
  documentation-only name, or a conceptual reference as dead

#### Scenario: A candidate duplication is classified before it is reported

- **WHEN** two requirements across specifications appear similar
- **THEN** the audit compares the obligation, actors, conditions, outcomes, scope, and exceptions, and reports the
  candidate only where the obligations are substantially identical or their overlap is materially ambiguous — not a
  related-but-distinct requirement or an intentional repetition

#### Scenario: The out-of-scope surfaces are not audited

- **WHEN** the `specs-auditor` subagent audits the corpus
- **THEN** it does not audit whether the role groups still match the module or service structure (the
  `architecture-auditor` owns that), does not re-report an issue an existing gate already enforces, and does not audit
  the internal quality of a proposed change or change delta (the review loop owns that)

#### Scenario: A correction preserves the intended behavior

- **WHEN** the `specs-auditor` subagent proposes a rewrite, merge, split, or reference update
- **THEN** the recommendation names the exact text to change and the smallest correction that repairs the problem,
  preserving the requirements' conditions, outcomes, exceptions, and the distinctions between capabilities, and stating
  for a merge why the requirements are semantically equivalent

#### Scenario: Confidence is a filter, not a report category

- **WHEN** the `specs-auditor` subagent weighs a candidate whose evidence shows an intentional, coherent choice, or
  several symptoms of one root cause
- **THEN** it omits the coherent choice, reports the shared symptoms as one finding, and attaches no confidence or
  severity score — repeating no item across groups and adding no placeholder for a category with no findings

#### Scenario: The audit proceeds in bounded stages

- **WHEN** the `specs-auditor` subagent audits the corpus
- **THEN** it inventories the corpus, checks local structure, compares related requirements across specs, verifies the
  questionable references, and validates the report against the scope — reusing the evidence an earlier stage gathered

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

Every item the report carries SHALL also carry the shared report contract's per-item budget, owned by
`docs/contracts/shared-report.md`, not this one.

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

Every item the report carries SHALL also carry the shared report contract's per-item budget, owned by
`docs/contracts/shared-report.md`, not this one.

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

### Requirement: Report-producing subagents follow a shared report contract

Every subagent that reports findings or a recommendation to the main agent — the per-unit review agent and the
`lesson-capture` agent, the on-demand auditors of the agent tooling, the spec corpus, the architecture, and the README,
and the independent technical advisor — SHALL follow one shared output contract owned by
`docs/contracts/shared-report.md`, so a report is skimmable and its verdict is read first. The contract SHALL bound the
report's shape; the analysis a subagent performs is bounded by that subagent's own stated scope, not by this contract —
an auditor or the deep reviewer verifies its claims as thoroughly as its scope requires, while a bounded screen such as
the quick review performs only the pass its scope defines and says so.

#### Scenario: A report opens with its verdict

- **WHEN** a report-producing subagent returns its report
- **THEN** the first line states the outcome — for a findings report, whether anything remains and how many items,
  naming the count of every class it carries (findings, and any advisory or accretion class); for the technical advisor,
  its recommendation and the counts of its observation classes
- **AND** when the report is a findings report and the caller's request named a prior round's findings, that first line
  also names which of the report's classes repeat a class the prior round already raised
- **AND** a report with nothing to report says so in that first line rather than in a closing sentence

#### Scenario: Each item is budgeted

- **WHEN** the subagent reports a proposed addition, a finding, or an observation
- **THEN** the item names itself, its anchor (`file:line` where applicable), and one line of evidence
- **AND** the budget is per item, not a cap on the report's total, so a report with many genuine findings is longer than
  one with few

#### Scenario: Verified-and-set-aside candidates are summarized

- **WHEN** the subagent has verified a candidate and concluded it is already covered, rejected, or not a defect
- **THEN** it reports that in one line, or collapses several into one summary line, rather than a paragraph each

#### Scenario: A bounded pass does not present itself as an unbounded analysis

- **WHEN** a report-producing subagent's scope bounds the analysis it performs — the quick review's fixed scope and stop
  condition, for example
- **THEN** its report states that bound rather than implying the analysis was unlimited, so a reader does not read a
  bounded screen as a thorough pass

#### Scenario: No alternatives are offered

- **WHEN** the subagent proposes a change or a correction
- **THEN** it states the recommendation, not a menu of alternatives — the calling agent makes the choice

### Requirement: AGENTS.md entries follow a stated authoring standard

`AGENTS.md` SHALL be maintained as an operational guide for future agents — not a narrative log of development activity
or a general knowledge repository — so every entry states reusable knowledge that constrains behavior, guides a
decision, defines a reusable procedure, prevents a known failure, or supplies the context such a decision needs. The
representation choices, the procedure and gotcha shapes, the evidence rules, and the validation pass are owned by
`docs/contracts/knowledge-authoring.md`; this requirement states the capability outcome and the scenarios below.

The standard SHALL be applied when `AGENTS.md` knowledge is authored and when it is audited: the `lesson-capture`
subagent (and the main agent's defect-generalization workflow) applies it to each proposed entry, and the
`agents-auditor` subagent applies it to each `AGENTS.md` entry it reads.

Every item the report carries SHALL also carry the shared report contract's per-item budget, owned by
`docs/contracts/shared-report.md`, not this requirement.

#### Scenario: An entry is recorded in the right representation, once

- **WHEN** an author adds knowledge to `AGENTS.md`
- **THEN** it chooses the representation — a rule or invariant, a procedure, a decision rule, a gotcha, or reference
  knowledge — keeps one authoritative definition for the subject and references it rather than duplicating, and updates
  an existing entry before adding a new one

#### Scenario: A knowledge item that changes no future action is not recorded

- **WHEN** a candidate entry would record transient debugging detail, generic advice, or a fact that does not change
  what a future agent does
- **THEN** it is not added to `AGENTS.md`

#### Scenario: A procedure is executable without inventing steps

- **WHEN** an entry states a procedure
- **THEN** it carries, where applicable, its Trigger, Prerequisites, Procedure, Decision points, Failure handling,
  Completion criteria, and Verification, so a reader can execute it without reconstructing the intended order or
  inventing a missing decision

#### Scenario: A gotcha separates the observed from the proven

- **WHEN** an entry states a gotcha
- **THEN** it takes the smallest actionable structure and distinguishes an observation, a hypothesis, a confirmed cause,
  and a verified resolution, presenting only the last as established

#### Scenario: An unknown cause is preserved, not invented

- **WHEN** a failure's cause is unknown
- **THEN** the entry preserves the observation and a diagnostic approach rather than asserting an invented fix, and does
  not generalize a workaround past the conditions under which it was shown to work

#### Scenario: A claim is derived from repository evidence

- **WHEN** an author records a project-specific claim, or a lesson sourced from untrusted content
- **THEN** the claim is derived from repository evidence (or the untrusted lesson verified against the repository)
  before it is recorded

#### Scenario: A lesson is folded into the workflow it belongs to

- **WHEN** an author records a lesson that can be incorporated into an existing workflow — as a preventive rule, a
  diagnostic step, a verification criterion, or a targeted review check
- **THEN** it is incorporated there rather than left as an isolated warning, and a procedure is not duplicated across
  sections

#### Scenario: Existing guidance is revised or removed only once obsolete

- **WHEN** an author considers removing an existing instruction
- **THEN** it removes or revises the instruction only once its obsolescence is established, not because it appears
  unusual or has not been encountered recently

#### Scenario: An edit is validated before it is finished

- **WHEN** an author finishes adding, modifying, consolidating, or removing instructions
- **THEN** it re-reads the affected section and its context and checks for duplication, contradiction, unclear scope,
  surviving constraints, evidence-backed claims, and an executable procedure

#### Scenario: A capture proposal follows the standard

- **WHEN** the `lesson-capture` subagent (or the main agent's defect-generalization workflow) proposes an `AGENTS.md`
  entry
- **THEN** the proposal follows this standard — its representation, its shape, its evidence, and its validation — in
  addition to the capture requirement's promotion gate

#### Scenario: The audit reports an entry that violates the standard

- **WHEN** the `agents-auditor` subagent reads an `AGENTS.md` entry that violates this standard — a procedure or gotcha
  leaving a required slot unfilled, a claim stated as fact without evidence, or a subject whose representation is
  duplicated
- **THEN** it reports the entry as a finding with its location and a concrete rewrite that conforms it

### Requirement: Agent-authored technical artifacts follow a structured writing standard

Agent-authored technical artifacts — `AGENTS.md`, OpenSpec proposals, specifications, designs, and task lists, plans,
code reviews, and other repository documentation — SHALL follow the structured, scannable shape `AGENTS.md`'s
"Structured Writing Standard" section states, applied while the artifact is drafted rather than only in a later cleanup
pass, and each SHALL keep its own required template and sections.

Every item the report carries SHALL also carry the shared report contract's per-item budget, owned by
`docs/contracts/shared-report.md`, not this one.

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

Every item the report carries SHALL also carry the shared report contract's per-item budget, owned by
`docs/contracts/shared-report.md`, not this one.

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

Every item the report carries SHALL also carry the shared report contract's per-item budget, owned by
`docs/contracts/shared-report.md`, not this one.

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
