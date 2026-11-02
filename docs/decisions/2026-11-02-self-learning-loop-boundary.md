# Decision record — the self-learning loop's boundary and its consolidation candidates

Date: 2026-11-02

Status: a bounded exploration. It binds no standing rule; it records what the loop's own components and its reuse pilots
establish about consolidating it.

## The goal

The owner's subject is consolidation of the self-learning loop. Lesson capture is one of its parts, not the whole. This
record bounds the loop, classifies each dependency that crosses between its parts, and names one candidate a later unit
may adopt under ADR-0027.

## The loop's boundary

The loop is a set of feedback paths, not a mandatory sequence: generalization is owner-triggered, and a defect may be
recorded, reconciled, or retired without travelling the whole way. Each part keeps its existing owner.

| Part                        | Owner in this repository                                                                                      |
| --------------------------- | ------------------------------------------------------------------------------------------------------------- |
| Observation and recording   | `AGENTS.md`, "Workflow: Record process defects"; `/flag-process-defect` for the verified entry                |
| Selection and disposition   | the generalization workflow's first step and `/generalize-process-defects`, with the recurrence check         |
| Generalization              | `/generalize-process-defects` and the `lesson-capture` analysis                                               |
| Application and closure     | the unit that implements a proposal, and the record's `Decision` line                                         |
| Memory and reuse            | the guidance destinations, and `docs/guidance-evidence/`                                                      |
| Reconciliation and feedback | the audits, `/apply-audit-reductions` for their reduction candidates, `/retrospective`, `experience-analyzer` |

## Each crossing, classified

ADR-0025's four classes are the test: an obligation, a binding, supporting material, or project substance. The lesson
capture pilot's manifest supplies the observed evidence for each row.

| Crossing                                | Class                       | Disposition                                                                            |
| --------------------------------------- | --------------------------- | -------------------------------------------------------------------------------------- |
| The unit's process-defect record        | explicit project binding    | a later unit may adopt it; see the candidate below                                     |
| `AGENTS.md`'s "### Evidence and search" | explicit project binding    | stays explicit under ADR-0026's exception: the section is a destination                |
| The guidance destinations               | explicit project binding    | stays explicit under the same exception                                                |
| The recurrence dispositions             | reusable process obligation | unchanged; the caller's workflow owns it                                               |
| `docs/ideas.md`                         | explicit project binding    | not declared; the ideas file is a recurrence input, not a destination                  |
| `docs/guidance-evidence/`               | explicit project binding    | recorded as an incompatibility: a target without it cannot be given evidence retention |
| The `/audit-agents` reference           | explicit project binding    | recorded as an incompatibility: a path cannot give a target the audit control          |

Every row falls inside the four classes, so the first clause of ADR-0025's boundary signal — "a dependency the four
classes do not place" — is not met, and neither is its second, "an obligation the boundary calls portable that cannot
move": the task here is disposition, not a new class.

The first row is the only one whose evidence supports adopting a binding now, and two shapes are refused here so the
candidate is not widened into either:

- **A role for a destination.** It contradicts the exception ADR-0026 keeps, whatever authority the section also
  carries.
- **A declaration entry before the component carries the role.** It asserts an adoption that has not happened.

## The candidate a later unit may adopt

- **Role** — the unit's process-defect record, valued at `docs/process-defects/<unit>.md`, owned by the record's field
  set (`AGENTS.md`, "Workflow: Record process defects").
- **Adopters** — the six components that name the path today: `flag-process-defect`, `generalize-process-defects`,
  `lesson-capture`, `review-quick`, `experience-analyzer`, `retrospective`.
- **What the adoption must preserve** — which records a component selects, the record's field set, and the `Decision`
  line's meaning. A role that located the file and lost any of the three would be a regression.
- **Evidence it owes** — same-change adoption in those six, and a renaming counterfactual plus a missing-binding control
  recorded as tool sequences. ADR-0027's other clauses are the proposal's to satisfy.

## What this record does not do

- It adopts nothing: the declaration is unchanged, and no component carries a new role because of this record.
- It lifts no pause: ADR-0025's standing consolidation pause keeps its condition, and the candidate unit needs the
  owner's resolution as its predecessors did.
- It claims no portability: the loop's portability is unmeasured, and the pilot's ceiling bounds what its one target
  shows.

## The ceiling

**A boundary and a classification, derived from the loop's own components and one pilot's manifest.** It establishes
which crossing has evidence for adopting a binding and which do not; it establishes no transfer cost, no cross-project
generality, and no net saving.

## Reopening condition

Reopen this record when any one of the following holds:

- **The candidate is adopted or rejected** — the adopting unit's record replaces this classification for that crossing
  (internal trigger: that unit's archived change).
- **A transfer record names a crossing this map omits** — the boundary is then incomplete and is re-derived (internal or
  external trigger: any transfer record, whose provenance decides which).
- **The owner changes ADR-0025's pause or ADR-0027's criterion** — this record's disposition column follows the owning
  decision (internal trigger: that ADR).
