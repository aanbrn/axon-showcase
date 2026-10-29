# Process defects — widen-architecture-auditor-to-parked-revisit-conditions

### widen-architecture-auditor-to-parked-revisit-conditions#1 — The `MODIFIED` block was written from a truncated read of the requirement

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — proposal.

**Expected** — a `MODIFIED` block copies the requirement's whole body and every scenario (`AGENTS.md`'s "Author a spec
delta" procedure states it), so the scenario set is enumerated from the requirement's own boundary — its header to the
next `### Requirement:` — rather than from an excerpt.

**Observed** — the delta was written from a `sed -n '<header>,+45p'` slice of
`openspec/specs/showcase/quality/agent-skills/spec.md`, which ended after the seventh scenario. The requirement has
nine, so the block omitted "A deferral's condition is classified before it is reported" and "A parked idea whose work
only partly landed is reported as partial".

**Evidence** — `openspec validate --all` reported
`MODIFIED "Unexplained design intent is surfaced for clarification" omits scenario(s) the current spec still has: "A deferral's condition is classified before it is reported", "A parked idea whose work only partly landed is reported as partial". The omission was corrected by inserting both blocks in their original relative position, after which the run reported `27
passed, 0 failed`.

**Consequence** — one failed validation and one repair before any review round; no other artifact was affected, and the
two scenarios were restored verbatim.

**Knowledge boundary** — observed: the slice's line range, the two omitted scenarios, the validator's message, and the
repair. Not checked: whether any other artifact in this unit was written from a bounded read rather than from its
subject's own boundary.
