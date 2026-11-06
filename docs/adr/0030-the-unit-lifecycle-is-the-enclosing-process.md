# ADR-0030: The unit lifecycle is the enclosing process

Date: 2026-11-06

Status: Accepted

## Context

A unit of work is the process's base unit, and an OpenSpec change is one form it may take (`AGENTS.md`; ADR-0029's
`## Amendment — 2026-11-06`). The lifecycle that unit follows was never stated as one model: the governing procedure was
titled for the change form while its trigger governed every unit, its steps mixed unit-generic work with change-specific
artifacts, workspace establishment was split between a command and prose, and the change's archive was the only place
its lifecycle met the unit's. Records and rules had grown around the parts — the review gate at two boundaries, pause
and resume as a transition, process-defect records, the bindings declaration — without a statement of how they compose.

ADR-0025 fixes where each kind of obligation lives (behaviour in the capability spec, procedure in a skill); ADR-0026
makes the locations declared bindings; ADR-0029 places the unit's record outside the change plan. This decision supplies
the model those three describe the ownership of.

## Decision

The unit lifecycle is the enclosing process for all repository work intended to ship as one pull request. An OpenSpec
change is an optional workflow contained within it.

- `AGENTS.md` owns the operational stage model and the cross-stage authority boundaries.
- Capability requirements state the lifecycle's observable outcomes.
- Each stage procedure has one named owner, and an entry command invokes that owner rather than reproducing it.
- Stage, recorded state, pending decision, and outcome are distinct; no record field or second ledger carries the stage.
- Commands declare their applicability and invocation authority, and a declaration grants none.
- Existing records and mechanisms remain authoritative for their subjects.

No central workflow engine, second unit registry, or session-bound write guarantee is introduced.

## Consequences

- A change's archive is a step inside the unit's Deliver, conditional on the unit carrying a change directory, so a
  change-less unit's delivery is representable end to end.
- Workspace establishment has one procedure: a `begin-unit` skill owns the ordered steps, `/begin-unit` is its entry,
  and the tree-creation script keeps its bounded mechanics.
- `closed` is a pre-merge write that proves neither the merge nor the settlement; settlement is what releases the tree.
- The stage vocabulary is explanatory prose, not a machine state: nothing derives a stage from a record field.
- The command projection is verified mechanically for completeness, values, references, preflight anchors, and mapping
  coverage — never for a command's behaviour.
- A future change may move a stage's owner or rename a stage; that is a change to this decision, not a wording edit.

## Alternatives considered and rejected

- **A workflow engine or a persistent stage ledger.** Existing artifacts already establish their facts, and a second
  ledger would need reconciling against them.
- **Stage tags in command frontmatter.** The loader contract is not established here, and the file reads as prose.
- **Four stages with review as a gate only.** It hides the two owner hand-offs every owner decision passes through.
- **Keeping `new-worktree` and pointing the lifecycle at it.** The name describes a mechanism, not the stage, and the
  primary path, the record, and the claim would stay in prose.
- **Naming stages after record states.** It conflates state with stage and loses pause-over-any-stage.
- **Verifying a command's behaviour.** A mechanical check cannot prove that prose has its declared effect; the claim is
  bounded instead.
