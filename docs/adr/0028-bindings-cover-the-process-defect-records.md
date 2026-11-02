# ADR-0028: The bindings cover the unit's process-defect record

Date: 2026-11-02

Status: Accepted

Revisit when **any** of the following is met — each alone reopens this decision for reconsideration:

- **No component resolves the role** — every adopter stops naming the records, leaving a declaration row with no
  consumer (internal trigger: the consumer trace).
- **A target's transfer shows the record's field set cannot travel** — a transfer record needs the record's shape
  changed rather than relocated, which the role does not attempt (external trigger: an owner-named project's record).

## Context

The self-learning loop's boundary record (`docs/decisions/2026-11-02-self-learning-loop-boundary.md`) classed the unit's
process-defect record as an explicit project binding and the only crossing whose evidence supported adopting a role: six
components named the record's location — two the per-unit path, four the directory — and the lesson-capture pilot's
manifest classed the same path an existing equivalent in its target — the target had the records; what it lacked was
nothing here.

ADR-0026 fixed six roles in its Decision, and that sentence is what this decision supersedes; its revisit clause does
not cover a seventh role joining, which is why the replacement is recorded here and the old count is amended there.

## Decision

The declaration gains one role, and the rest of ADR-0026 stands.

- **`process-defect-records`** — the unit's defect record, valued at `docs/process-defects/<unit>.md`, owned by the
  record's field set (`AGENTS.md`, "Workflow: Record process defects"). A component that resolves the location still
  needs the record's shape, so the field set is the referenced owner rather than a second declaration.
- **Locating, not creating.** A target without such records treats that as a missing contract; the role never instructs
  a component to create the directory. That is the pilot's distinction between locating an existing contract and
  inventing an absent one.
- **What each adoption preserves.** The records a component selects — the unit's own, the merged unit's on a
  merge-revealed defect, the window's by commit, the selected set for a generalization, the diff that drew guidance for
  a capture review — the record's field set, and the `Decision` line's meaning.

The role list is seven. ADR-0026's six-role sentence is therefore replaced by this decision and its own list is amended;
its declared-not-substituted decision, read routes, role-not-string rule, missing-binding stop, non-adopter trace, and
explicit exceptions — the lesson destinations and recurrence routing among them — are untouched.

## Consequences

- Six components resolve the location instead of naming it, so a target keeps its own records directory without editing
  them.
- The `agent-skills` requirement stops enumerating roles, so a further role costs a declaration row and its adopters,
  not a spec delta.
- The boundary record's other crossings keep their dispositions: destinations explicit, `docs/guidance-evidence/` and
  the audit reference recorded as incompatibilities.
