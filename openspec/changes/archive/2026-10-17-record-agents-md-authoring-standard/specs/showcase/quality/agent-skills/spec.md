## ADDED Requirements

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
subagent (and the main agent's capture workflow) applies it to each proposed entry, and the `agents-auditor` subagent
applies it to each `AGENTS.md` entry it reads.

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

- **WHEN** the `lesson-capture` subagent (or the main agent's capture workflow) proposes an `AGENTS.md` entry
- **THEN** the proposal follows this standard — its representation, its shape, its evidence, and its validation — in
  addition to the capture requirement's promotion gate

#### Scenario: The audit reports an entry that violates the standard

- **WHEN** the `agents-auditor` subagent reads an `AGENTS.md` entry that violates this standard — a procedure or gotcha
  leaving a required slot unfilled, a claim stated as fact without evidence, or a subject whose representation is
  duplicated
- **THEN** it reports the entry as a finding with its location and a concrete rewrite that conforms it
