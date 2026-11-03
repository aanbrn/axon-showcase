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

- **`process-defect-records`** — the unit's defect record, valued at `docs/process-defects/<unit>.md` (now
  `docs/units/<unit>/process-defects.md`, see the location amendment below), owned by the record's field set
  (`AGENTS.md`, "Workflow: Record process defects"). A component that resolves the location still needs the record's
  shape, so the field set is the referenced owner rather than a second declaration.
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

## Amendment — 2026-11-02

The role list grows by one, and this amendment records it without reopening the Decision.

- **`spec-delta`** joins the list: the "Workflow: Author a spec delta and archive" heading in `AGENTS.md`, owned by that
  workflow, adopted by the three audit commands that route a finding's spec change through it. It satisfies ADR-0026's
  role-not-string rule — the declaration binds a location or an authority, not a string — and the declaration's table,
  not this decision, owns how many roles exist.
- The **Decision**'s sentence "The role list is seven." no longer holds and is superseded: read the declaration's table
  for the count.

## Amendment — 2026-11-03

The role list grows by one, and this amendment records it without reopening the Decision.

- **`branch-refresh`** joins the list: the "Refreshing a branch" bullet under "Workflow: Change lifecycle" → "Failure
  handling" in `AGENTS.md`, owned by that rule, adopted by `/sync-branch`, which follows the route the rule gives the
  state it found. It satisfies ADR-0026's role-not-string rule, and the declaration's table, not this decision, owns how
  many roles exist.

## Amendment — 2026-11-03 (the guidance roles)

The role list grows by two, and this amendment records it without reopening the Decision.

- **`generalization-dispositions`** joins the list: the "Where the repository already records a recurrence decides its
  disposition" bullet in "Workflow: Generalize process defects" → "Decision points", adopted by
  `/generalize-process-defects`, which decides each record's disposition against it.
- **`extraction-procedure`** joins the list: the "Workflow: Extract an `AGENTS.md` section" heading, adopted by
  `/apply-audit-reductions`, which routes an extraction through it.

Both satisfy ADR-0026's role-not-string rule, and the declaration's table, not this decision, owns how many roles exist.

## Amendment — 2026-11-03 (the record's location)

The Decision's role bullet names a location that has changed, and this amendment records it without reopening the
Decision.

- The `process-defect-records` value is now `docs/units/<unit>/process-defects.md`, so a unit's defect history sits in
  the same dossier as its `unit.md`, `rounds.tsv`, `findings/`, and `review-handover.md`. The role, its referenced owner
  (the record's field set), and the locating-not-creating rule are unchanged; only the value the bullet states moves.
