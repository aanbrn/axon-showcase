## Why

The boundary record (`docs/decisions/2026-11-02-self-learning-loop-boundary.md`) named one candidate a later unit may
adopt: the unit's process-defect record. Six components named that location literally — two the per-unit path, four the
directory — so a target that keeps its defect records elsewhere edits six files, and the loop's own entry point is one
of them.

## What Changes

- `docs/contracts/process-bindings.md` gains the `process-defect-records` role, with the record's field set as its
  referenced owner, and states that the role locates the record rather than creating the directory a target lacks.
- Six components adopt it: `flag-process-defect`, `generalize-process-defects`, `lesson-capture`, `review-quick`,
  `experience-analyzer`, `retrospective`.
- ADR-0028 replaces ADR-0026's six-role list with seven.
- ADR-0026 gains a dated amendment naming the replaced list and the count its revisit clause now covers.
- ADR-0025 gains the scoped pause lift the owner gave for this unit.
- The `agent-skills` requirement stops enumerating the roles, so the next role costs a declaration row, not a delta.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the declared-bindings requirement's role set moves to the declaration.

## Impact

- `docs/contracts/process-bindings.md` — one role and six adopter rows.
- `.opencode/agent/lesson-capture.md`, `.opencode/agent/experience-analyzer.md`, `.opencode/agent/review-quick.md`,
  `.opencode/commands/flag-process-defect.md`, `.opencode/commands/generalize-process-defects.md`,
  `.opencode/commands/retrospective.md`.
- `docs/adr/0028-bindings-cover-the-process-defect-records.md` (new), `docs/adr/0026-process-bindings-are-declared.md`
  and `docs/adr/0025-process-core-and-project-substance.md` (one dated amendment each).
- `docs/process-defects/adopt-process-defect-records.md`.
- No runtime code, build, API, schema, image, chart, or deployment change.
