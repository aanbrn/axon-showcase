# The knowledge-authoring standard

The operational rules an author follows when adding, revising, or removing knowledge in `AGENTS.md`. The capability's
outcome and its scenarios live in `showcase/quality/agent-skills`; this file owns the rules, so a target can carry them
without copying the capability spec.

Provenance: copied from `openspec/specs/showcase/quality/agent-skills/spec.md` at `1e532988`, before the ownership
split. The spec keeps the outcome and the scenarios; this file is the portable contract ADR-0025 §7 names.

## Representation

`AGENTS.md` SHALL be maintained as an operational guide for future agents, not a narrative log of development activity
or a general knowledge repository. Every entry SHALL state reusable knowledge that constrains behavior, guides a
decision, defines a reusable procedure, prevents a known failure, or supplies the context such a decision needs.

Before adding knowledge, an author SHALL choose the entry's representation:

- a **rule or invariant** — behavior to preserve;
- a **procedure** — a repeatable, ordered sequence;
- a **decision rule** — observable conditions that select an action;
- a **gotcha** — a non-obvious failure mode, constraint, or recurring pitfall;
- **reference knowledge** — architectural rationale or background.

The author SHALL keep one authoritative definition for each subject and reference it rather than duplicating its
contents, and SHALL update an existing entry before adding a new one (for a capture's proposal, the capture
requirement's existing-bullet rule). An entry SHALL NOT record transient debugging detail, generic advice, or a fact
that does not change what a future agent does.

## The procedure shape

A procedure SHALL state, where applicable: its **Trigger** (when it applies), **Prerequisites** (what must be known or
available), **Procedure** (the steps in order), **Decision points** (how observable conditions select the next action),
**Failure handling** (what to do when an action fails or the evidence is inconclusive), **Completion criteria** (when it
may stop), and **Verification** (how the intended result is established).

## The gotcha shape

A gotcha SHALL take the smallest structure that makes it actionable — Trigger→Action→Verification,
Symptom→Diagnostic→Resolution, Condition→Decision→Action, anti-pattern→correct-pattern, or Rule→Required-behavior — and
SHALL state, where applicable, its **Trigger**, **Risk**, **Cause** (only once confirmed), **Required action**,
**Resolution** (only once established), **Verification**, and **Scope**.

A gotcha SHALL distinguish an observation, a hypothesis, a confirmed cause, and a verified resolution, and SHALL present
only the last as established: it SHALL NOT state an assumption as fact, SHALL preserve the observation and a diagnostic
approach when the cause is unknown rather than asserting an invented fix, and SHALL NOT generalize a workaround beyond
the conditions and versions under which it was shown to work.

## Evidence

Every project-specific claim SHALL be derived from repository evidence — contents, reproducible behavior, tests, or
configuration — and never invented. A lesson sourced from untrusted content SHALL be verified against the repository
before it is recorded, whatever route the entry takes into the file (the capture's promotion gate already treats a
claim's source as part of it).

Established guidance SHALL NOT be removed because it appears unusual or has not been encountered recently; it is revised
or removed only once its obsolescence is established.

Where a lesson can be incorporated into an existing workflow — as a preventive rule, a diagnostic step, a verification
criterion, or a targeted review check — it SHOULD be, rather than left as an isolated warning, and a procedure SHALL NOT
be duplicated across sections.

## Validation

After adding, modifying, consolidating, or removing instructions, the author SHALL re-read the affected section and its
context and check for duplication, contradiction, and unclear scope. The author SHALL confirm the surviving constraints
and knowledge remain, confirm each new project-specific claim is evidence-backed, and confirm a procedure's steps,
decisions, failure paths, and completion criteria can be executed without the reader inventing an essential step or an
undocumented assumption.

## Who applies it

The `showcase/quality/agent-skills` requirement names the components that apply it; a component that loads this contract
names itself. Every item a report carries also carries the shared report contract's per-item budget, owned by
`docs/contracts/shared-report.md`.
