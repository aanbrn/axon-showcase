# ADR-0025: The process core and the project substance

Date: 2026-10-31

Status: Accepted

Revisit when **any** of the following is met — each alone would change this decision:

- **A real target project's transfer shows the boundary wrong** — the target's transfer record names a dependency the
  four classes do not place, or an obligation the boundary calls portable that cannot move (external trigger: an
  owner-named project's record).
- **A third slice records a coupling kind outside the three the pilots recorded** — repo-specific paths/headings, absent
  governance contracts, absent supporting material (internal trigger: the next reuse unit's record).
- **The first consolidation cannot separate the contracts without changing policy or hiding a required instruction** —
  the clause-to-owner trace fails for the knowledge-authoring standard or the shared report contract (internal trigger:
  the consolidation unit's design or review).
- **A template repository's signal is met** — multiple active consumers share a lesson-routing and version-ownership
  model (external trigger: the second consumer's record).
- **A generator or broad extraction's signal is met** — a second target needs the same adaptations (external trigger:
  the second target's transfer record).
- **An auditor axis's signal is met** — later transfers record recurring coupling drift (internal trigger: a later
  transfer record).

## Context

The owner's goal is an agent process prepared for reuse in similar projects, and the expected destination is the
consolidation of its component parts. Two bounded transfer pilots are the evidence, each moving a real component of this
repository into one constructed target:

- The **pause/resume** slice transferred with six concept-level mappings (twelve occurrences), six structural removals,
  one root pointer, zero process-policy changes, one rescue, and zero undeclared support files. The reviewer-session and
  subject-pack contracts were unexercised, and the ADR-0024 citation and its analysis were dropped.
- The **lesson-capture** slice transferred with six mappings, three structural adaptations across seven sites, one
  confinement block, one declared support document, and one root pointer. Six branches were unexercised, including the
  novel addition (covered by the first slice's skill) and the authoring-mechanism recommendation (no template in the
  fixture).

Three coupling kinds recur across the two slices: **repo-specific paths and headings**, **absent governance contracts**,
and **absent supporting material**. The fourth — the trial harness's confinement — is a property of the harness, not of
a slice.

The evidence is bounded: two slices, one constructed target, and a fixture shaped to exercise the slices. It establishes
a cross-slice coupling pattern within that target, not cross-project generality.

The owner accepted a standing consolidation pause on 2026-10-23 (`docs/retrospectives/2026-10-23.md`), whose recorded
resumption condition is a confirmed contradiction between owners, a retrieval failure in practice, or another material
process defect. On 2026-10-31 the owner lifted that pause for this unit and the first consolidation by explicit
instruction; the lift departs from the recorded condition, and the owner's decision is the authority for it. Unrelated
parked moves remain paused.

The repository already has an ownership model: behaviour in the spec, procedure in the skill, reviewer contract in the
subagent definition, record format in the ledger's spec, and the decision floor in the script
(`consolidate-review-gate-ownership`). This decision adopts that model rather than inventing a competing one.

The **knowledge-authoring standard** (the `agent-skills` spec's entry-authoring rules) is distinct from the **Structured
Writing Standard** (`AGENTS.md`), which governs artifact drafting and deliberately lives at root. The contract layer
below concerns the former; it does not relocate the latter.

## Decision

### 1. Four classes

The four classes classify a component's **contents and dependencies**, not whole files into mutually exclusive buckets:
one file can carry an obligation and a binding, and a support document can carry a contract another component depends
on.

- **Reusable process obligation** — a behavior a slice must preserve, independent of the repository: the pause
  checkpoint's field set, the lesson-capture promotion gate, the split and retirement dispositions.
- **Explicit project binding** — a name, path, heading, or record a slice points at in a target: the review ledger's
  path, the lifecycle headings, the writer record, the change directory.
- **Supporting material** — the excerpt a component needs to execute its obligations: an ADR's rationale, a contract's
  text. It accompanies its owning component or its portable contract; it is not a separate bundling mechanism.
- **Project substance** — the application stack, the module list, the domain conventions, the deployment, and the
  repository's own architecture. It is not part of the process core.

### 2. The composition principle

A process component is portable when its obligations are separable from its bindings and its dependencies have named
owners. Each component identifies its dependencies and their owners. A missing contract is an explicit incompatibility,
recorded as such; it is never a reason to delete the obligation or to invent the missing artifact.

### 3. The ownership model

The repository's existing model applies: behavior in the capability spec, procedure in the skill, reviewer contract in
the subagent definition, record format in the ledger's spec, and the decision floor in the script. The contract layer
adds one owner class: a **portable contract** — a file under `docs/contracts/` that carries an excerpt of a capability's
contract with provenance, for a component that cannot load the capability spec. A process component names its
dependencies' owners by this model rather than carrying a second copy.

### 4. The contract layer

- The **knowledge-authoring standard** and the **shared report contract** get portable owners
  (`docs/contracts/knowledge-authoring.md`, `docs/contracts/shared-report.md`) with explicit load routes.
- The **audit assumptions** stay with the audit workflow, and the **recurrence dispositions** stay with the caller's
  workflow; the first consolidation does not take them.
- The **path and heading bindings** stay explicit; abstracting them is later work, not this decision.
- The **supporting material** — the provenance and excerpts a component needs — accompanies its owning component or its
  portable contract.

### 5. The inventory scope

The classes place only the components the pilots examined. This decision does not declare every `.opencode/` artifact
portable, and it does not inventory the application stack.

### 6. What this decision does not authorize

It does not authorize a template repository, a generator, broad extraction, or a new auditor axis. Each keeps the signal
recorded in the pilot's reopening condition: multiple active consumers with a lesson-routing and version-ownership model
for a template; a second target needing the same adaptations for a generator or broad extraction; recurring coupling
drift for an auditor axis.

### 7. The first consolidation

The first consolidation unit:

- takes the knowledge-authoring standard and the shared report contract into portable owners
  (`docs/contracts/knowledge-authoring.md`, `docs/contracts/shared-report.md`) with explicit load routes;
- keeps the recurrence dispositions in their existing workflow;
- derives its consumer set from the references to those contracts, not only from the two pilot slices;
- retargets the ownership clauses through an `agent-skills` delta, so the spec keeps the outcomes and scenarios rather
  than a second complete operational contract;
- refreshes the `agent-skills` Purpose, which currently describes the knowledge-authoring standard as a capability
  requirement, since the portable owner changes that description.

## Consequences

- The first consolidation unit is the next work; this ADR is its decision record and the pilots remain its dated
  evidence.
- The path and heading abstraction is deferred; the supporting-material bundle is not a separate mechanism.
- The four deferred proposals keep their signals; none is authorized by this decision.
- The capability spec keeps the outcomes and scenarios; the consolidation changes the ownership clauses and the Purpose,
  not the behavior.
- A real target or a third slice can falsify the boundary; the `Revisit when` clauses above are the checks.
