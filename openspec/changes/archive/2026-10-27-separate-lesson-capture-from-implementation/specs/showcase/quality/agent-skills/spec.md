# Spec Delta

## REMOVED Requirements

### Requirement: Lessons are captured after implementation

**Reason**: Superseded by "Process defects are captured during a unit and generalized separately". The inline analysis
put a judgment-heavy pass on every product unit's critical path and assumed the session that observed a defect is the
one that analyzes it — a property the spec states of the experience-analyzer's input and that holds for this one by
analogy. Recording is now a fact the unit produces and analysis is a unit of its own.

**Migration**: The analysis this requirement owned — the promotion gate, the evidence threshold, the dispositions, the
owning destinations, the growth bound, and the `captured:` provenance — carries over unchanged into the added
requirement's "The generalizing unit keeps the analysis contract" scenario. A unit's capture record becomes its
`docs/process-defects/<unit>.md`; a record without a `Decision` line is what a capture record's "unrun" reading becomes.
The four-clause skip condition, the merge-time capture, and the capture record itself retire with the inline analysis.

## ADDED Requirements

### Requirement: Process defects are captured during a unit and generalized separately

The repository SHALL separate recording a unit's process defects from generalizing them: the unit's session records what
it observes, and analysis, improvement design, and implementation happen in a separate unit that the owner triggers. The
unit's own review SHALL gate the record's currency, not the analysis, and the generalizing unit SHALL keep the analysis
contract this requirement carries in full.

#### Scenario: A unit records the process defects it observes

- **WHEN** a unit's session observes a process defect — a rule it missed, an action that failed, wasted work, a misread
  artifact
- **THEN** it appends a record to `docs/process-defects/<unit>.md` when it observes it, before the context is lost
- **AND** the record is self-contained for a reader outside that session: it carries the provenance, the expected action
  and the observed deviation, the evidence, the observed consequence and the recovery made, and the boundary of the
  author's knowledge, whose field set `AGENTS.md`'s recording workflow owns and states once
- **AND** the record does not require a cause diagnosis, a proposed improvement, or an assessment of the defect's cost
- **AND** a unit that observed no process defect states that in its record, and that statement is not a claim that none
  occurred
- **AND** immediate damage is repaired in the unit regardless — a lost file, a broken result, or a wrongly closed
  tracker is not left to a later analysis
- **AND** the record ships with the unit's pull request rather than in the unit's change directory, so it survives the
  change's archive and a later unit can read it
- **AND** a merge appends to that record any defect the merge alone reveals — a non-diff effect such as an agent pull
  request closing its own tracker — since the record outlives the change it describes

#### Scenario: The product unit's gate is the hand-off, not the analysis

- **WHEN** a unit is prepared for the manual review pass at the implementation stage
- **THEN** the review gate requires the unit's record to exist and to reflect what the unit observed, or to state that
  it observed none
- **AND** it requires no analysis of that record, no proposed improvement, and no applied guidance change
- **AND** generalization does not block the unit's review, its archive, or its merge

#### Scenario: Generalization runs as its own unit on merged records

- **WHEN** the owner decides the accumulated records are due for generalization
- **THEN** the on-demand command `/generalize-process-defects` selects the records that carry no `Decision` line and
  hands them, with the existing guidance a candidate would extend, to the `lesson-capture` subagent
- **AND** the calling unit designs, reviews, and implements the approved improvements as a unit of its own, on its own
  branch and working tree from a freshly fetched `origin/main`
- **AND** it consumes records that are already merged, never an unfinished unit's branch or working tree
- **AND** it appends one `Decision` line per record — `no-change` with its reason, `proposed` with a link, or `shipped`
  with a merged pull request's link — so a reader can tell an unanalyzed record from a decided one
- **AND** a link to an open pull request is recorded as `proposed`, never as `shipped`
- **AND** the moment of generalization is the owner's decision, so no schedule, queue, drain guarantee, or automatic
  assignment is implied
- **AND** the generalizing unit goes through the same review gate and the owner's approval as any other unit, and being
  assigned the work grants no self-review or merge authority

#### Scenario: The generalizing unit keeps the analysis contract

- **WHEN** the generalizing unit analyzes the selected records
- **THEN** the owning destinations and their routing are `AGENTS.md`'s generalization workflow's
- **AND** the generalization stage's own consolidation is a later unit
- **AND** it invokes the `lesson-capture` subagent, which proposes each durable lesson as an addition to the artifact
  that owns it — an `AGENTS.md` gotcha or convention, a check/test authoring mode in the `check-and-test-authoring`
  skill, a shipping-procedure correction in the `ship-change` skill, a stage-procedure correction in the `review-gate`
  skill, or a section of the subject pack (`jvm-development`, `jvm-testing`, `gradle-build`, or `deployment-operations`)
  whose subject owns the lesson's decision — or a pair of additions when the lesson has both halves, which the calling
  agent verifies and applies
- **AND** each proposed addition names the existing rule it extends — an `AGENTS.md` bullet, a check/test authoring mode
  already in the skill, a `ship-change` or `review-gate` procedure, or a subject-pack section — or states that none
  covers it, so a new rule merges into or replaces one rather than accreting
- **AND** each proposed addition passes a promotion gate before it is proposed — true (supported by a check, an
  authoritative source, or repeated observation), actionable, not automatable as a lint/test/CI check at reasonable
  cost, material (it prevents real breakage, risk, wasted work, or review churn), general enough for a class of future
  tasks rather than one file or incident, and high-confidence with a known scope — and a rule that fails the gate is
  routed to a check, a spec, an ADR, or the change dir instead of the always-loaded file
- **AND** the gate separates a repeated observation from its cause — a repeated review-class label establishes that
  label's recurrence, not a shared drafting defect, and a remedy resting on a class's recurrence (a section rewrite, or
  a rule generalized from that class) requires the shared cause confirmed from the evidence, with a class whose
  instances have different causes taking the cause-specific shape instead
- **AND** the caller supplies the existing guidance a candidate would extend and the evidence for a claimed recurrence —
  the records, and the review evidence they cite — and where either is unavailable the analysis states the limitation
  and withholds the unsupported candidate
- **AND** before proposing an entry, it traces the lesson to its origin — where the cause is a mechanism that produces
  the artifact (an OpenSpec template or schema, an `openspec/config.yaml` rule, a skill or command that shapes drafting,
  or the authoring standard itself), it proposes the smallest correction at that mechanism and reports it as an
  authoring-system recommendation, separate from its additions and retirements, since it changes how a future artifact
  is produced rather than what an agent reads
- **AND** each proposed addition names the decision its rule governs, and answers whether a future change would
  plausibly hit it and whether the cost of not knowing it is material — a proposal that governs no decision is trivia,
  not a rule, and is not proposed
- **AND** the gate's evidence threshold is two independent occurrences or one severe verified incident with a clear
  preventive action
- **AND** the gate treats the claim's source as part of it — a lesson sourced from untrusted content (a web page, an
  issue or PR comment, tool output, or a file the unit did not author) is verified against the repository before it is
  proposed, never promoted on the source's word
- **AND** applying the analysis should leave `AGENTS.md` no larger than it was, preferring a merge or a replacement over
  an addition, and any net growth is a justified decision stated with the proposal — not a side effect of accumulating
  prose
- **AND** each proposed addition demonstrates the **decision difference** it makes — the action a future agent takes
  under the existing guidance, and the action the proposed text requires or prohibits under an explicit condition
- **AND** that comparison names the existing obligation, its explicit condition, and the different action, so different
  phrasing does not establish a difference
- **AND** that difference carries a repository-backed example of why it matters
- **AND** an addition whose action the existing guidance already determines is reported as **already covered** and does
  not expand `AGENTS.md`
- **AND** an additional incident, example, citation, or provenance marker alone is `already covered`, and the analysis
  records that evidence in the generalization unit's change record rather than expanding the rule's text
- **AND** a partial restatement takes the **split** disposition — the new clause is kept, the restatement dropped
- **AND** it prefers an exact replacement of the existing rule over appending qualifications
- **AND** a replacement or rewrite preserves every valid obligation, warning, exception, and identifier, the established
  scope, and the normative strength, and may correct verified errors or remove established duplication and obsolete
  content
- **AND** the proposed action and its scope are derived from the verified evidence, so a remedy that worked in one place
  is not stated as a universal instruction
- **AND** the incident evidence a rule rests on stays recoverable behind its `captured:` marker rather than in the
  rule's text
- **AND** the analysis's verdict states the proposed net guidance delta in lines (`AGENTS.md` and the packs; additions
  minus retirements) and its authoring-system recommendation count, and the applying agent records the applied delta, so
  net growth is stated when it is decided, alongside the periodic audit's accreted-rule count
- **AND** when a rule's rationale is normative in a spec, the bullet keeps only what a reader needs to act and points at
  the spec — a pointer, not a condensed copy, since `AGENTS.md` is loaded on every invocation while a spec is loaded
  only when its capability is worked on
- **AND** when the specs describe a rule's subject only as an outcome, that mechanism belongs in the capability spec
  only when changing it would change a scenario's outcome — verified against the code before moving it; a mechanism
  whose alternatives yield the same result stays in `AGENTS.md` as internal control flow
- **AND** the control on that growth is the periodic `/audit-agents` pass, whose verdict already reports the
  accreted-rule count, rather than mass deletion to hit a number
- **AND** each proposed addition carries its origin in a greppable `captured: <unit>` marker naming the record's unit,
  so a reader can tell where the rule came from without consulting git, and a markdown reflow cannot take it
- **AND** every analyzed record carries its `Decision` line, so an unanalyzed record is visible as one rather than
  reading as an analysis that found nothing

#### Scenario: A change that obsoletes a rule yields a retirement candidate

- **WHEN** the records the generalizing unit analyzes show that a rule is obsolete (the mechanism it describes is
  removed) or redundant (a new enforcement subsumes it)
- **THEN** the `lesson-capture` subagent reports a retirement or replacement candidate naming the rule, why it is so,
  and the retirement or replacement it proposes — a candidate for the calling agent, not an action
- **AND** it reports those candidates separately from the additions, since an addition grows the file and a retirement
  shrinks it
- **AND** the analysis's verdict names the retirement count alongside its durable-proposal count

#### Scenario: A lesson whose cause is an authoring mechanism is routed to it

- **WHEN** a record's lesson traces to the mechanism that produces a technical artifact rather than to the artifact's
  content — a template or schema that encourages the wrong structure, a `config.yaml` rule that is silently dropped, a
  skill that prompts a shape the standard forbids
- **THEN** the `lesson-capture` subagent proposes the smallest correction at that mechanism and reports it as an
  authoring-system recommendation, naming the mechanism, the gap, the proposed change, and why an `AGENTS.md` entry
  would not suffice — separate from its additions and retirements, since it changes how a future artifact is produced
  rather than what an agent reads

#### Scenario: The process work may be performed by a different person

- **WHEN** the generalizing unit is performed by someone other than the session that recorded the defects
- **THEN** the record's fields, the product unit's gate, and the analysis contract are unchanged — only the assignment
  varies, so one participant and several follow the same obligations
- **AND** the assignment grants no approval, self-review, or merge authority, and the owner remains the approver
- **AND** `AGENTS.md` names the owner as the default generalizer, and a unit's tasking names any other assignee rather
  than a standing registry of roles

## MODIFIED Requirements

### Requirement: Report-producing subagents follow a shared report contract

Every subagent that reports findings or a recommendation to the main agent — the per-unit review agent and the
`lesson-capture` agent, the on-demand auditors of the agent tooling, the spec corpus, the architecture, and the README,
and the independent technical advisor — SHALL follow one shared output contract, so a report is skimmable and its
verdict is read first. The contract SHALL bound the report's shape; the analysis a subagent performs is bounded by that
subagent's own stated scope, not by this contract — an auditor or the deep reviewer verifies its claims as thoroughly as
its scope requires, while a bounded screen such as the quick review performs only the pass its scope defines and says
so.

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

### Requirement: Audit reduction candidates are applied by a standing owner-gated unit

The repository SHALL provide a standing, owner-gated unit, invoked by an `/apply-audit-reductions` command, that applies
an audit report's reduction candidates (its merge, removal, route, and extraction findings), routing a content-move to
the change workflow, so they do not age. It SHALL never apply an **extraction opportunity**, which is not a candidate:
an opportunity is a design recommendation the owner carries out in a dedicated change. It SHALL present the candidates
for the owner's selection, apply only the approved ones, and pass the per-unit review gate before the change ships; it
SHALL NOT run automatically.

#### Scenario: A report's reduction candidates are presented and applied

- **WHEN** the owner invokes the unit for the newest report under `docs/audits/` (a date carrying more than one report
  is offered for the owner to choose), or for one they name
- **THEN** it presents that report's reduction candidates and applies only the ones the owner approves

#### Scenario: An applied merge applies the candidate's merged text

- **WHEN** the unit applies an approved merge candidate
- **THEN** it applies the candidate's merged text, or deletes the duplicate the candidate flags

#### Scenario: An applied route reduces the rule to a verified pointer

- **WHEN** the unit applies an approved route candidate that reduces the rule to a pointer
- **THEN** it reduces the rule to a pointer at the target the candidate names, following the capture bullet's
  pointer/trim rule

#### Scenario: A route candidate that moves content into a spec or ADR is routed to a change

- **WHEN** an approved route candidate proposes moving the rule's content into the spec or ADR that owns it
- **THEN** the unit routes that edit through the normal change workflow rather than editing the spec or ADR inline

#### Scenario: An applied removal deletes a rule that governs no decision

- **WHEN** the unit applies an approved removal candidate
- **THEN** it deletes the rule the candidate names, recording what the deletion loses and whether git preserves it

#### Scenario: An applied reduction clears its parked entry

- **WHEN** the report's candidate was parked in `docs/ideas.md`
- **THEN** the unit removes the applied entry

#### Scenario: Application is owner-gated and reviewed

- **WHEN** the unit applies reductions
- **THEN** it does so only on the owner's selection, and it runs the full per-unit review gate over the result — the
  pre-write pass and its claims table, the formatter and mechanical checks, the `review-quick` loop with the
  session-continuity preflight, the deep-review routing decision, a required deep review, the unit's defect-record
  check, and the owner's manual review pass — before the change ships

#### Scenario: An extraction candidate's application is routed to a change

- **WHEN** the owner approves an extraction candidate
- **THEN** the unit routes the move to the normal change workflow rather than editing the destination inline, since the
  move creates or changes a destination that the change owns

#### Scenario: An extraction opportunity is never applied

- **WHEN** the report carries an extraction opportunity
- **THEN** the unit does not present or apply it, because an opportunity is a design recommendation rather than a
  candidate

### Requirement: AGENTS.md entries follow a stated authoring standard

`AGENTS.md` SHALL be maintained as an operational guide for future agents — not a narrative log of development activity
or a general knowledge repository — so every entry states reusable knowledge that constrains behavior, guides a
decision, defines a reusable procedure, prevents a known failure, or supplies the context such a decision needs. Before
adding knowledge, an author SHALL choose the entry's representation — a rule or invariant (behavior to preserve), a
procedure (a repeatable, ordered sequence), a decision rule (observable conditions that select an action), a gotcha (a
non-obvious failure mode, constraint, or recurring pitfall), or reference knowledge (architectural rationale or
background) — keep one authoritative definition for each subject and reference it rather than duplicating its contents,
and update an existing entry before adding a new one (for a capture's proposal, the capture requirement's
existing-bullet rule). An entry SHALL NOT record transient debugging detail, generic advice, or a fact that does not
change what a future agent does.

A procedure SHALL state, where applicable: its **Trigger** (when it applies), **Prerequisites** (what must be known or
available), **Procedure** (the steps in order), **Decision points** (how observable conditions select the next action),
**Failure handling** (what to do when an action fails or the evidence is inconclusive), **Completion criteria** (when it
may stop), and **Verification** (how the intended result is established). A gotcha SHALL take the smallest structure
that makes it actionable — Trigger→Action→Verification, Symptom→Diagnostic→Resolution, Condition→Decision→Action,
anti-pattern→correct-pattern, or Rule→Required-behavior — and SHALL state, where applicable, its **Trigger**, **Risk**,
**Cause** (only once confirmed), **Required action**, **Resolution** (only once established), **Verification**, and
**Scope**. A gotcha SHALL distinguish an observation, a hypothesis, a confirmed cause, and a verified resolution, and
SHALL present only the last as established: it SHALL NOT state an assumption as fact, SHALL preserve the observation and
a diagnostic approach when the cause is unknown rather than asserting an invented fix, and SHALL NOT generalize a
workaround beyond the conditions and versions under which it was shown to work.

Every project-specific claim SHALL be derived from repository evidence — contents, reproducible behavior, tests, or
configuration — and never invented, and a lesson sourced from untrusted content SHALL be verified against the repository
before it is recorded, whatever route the entry takes into the file (the capture's promotion gate already treats a
claim's source as part of it). Established guidance SHALL NOT be removed because it appears unusual or has not been
encountered recently; it is revised or removed only once its obsolescence is established. Where a lesson can be
incorporated into an existing workflow — as a preventive rule, a diagnostic step, a verification criterion, or a
targeted review check — it SHOULD be, rather than left as an isolated warning, and a procedure SHALL NOT be duplicated
across sections. After adding, modifying, consolidating, or removing instructions, the author SHALL re-read the affected
section and its context and check for duplication, contradiction, and unclear scope, confirm the surviving constraints
and knowledge remain, confirm each new project-specific claim is evidence-backed, and confirm a procedure's steps,
decisions, failure paths, and completion criteria can be executed without the reader inventing an essential step or an
undocumented assumption.

The standard SHALL be applied when `AGENTS.md` knowledge is authored and when it is audited: the `lesson-capture`
subagent (and the main agent's defect-generalization workflow) applies it to each proposed entry, and the
`agents-auditor` subagent applies it to each `AGENTS.md` entry it reads.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

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
