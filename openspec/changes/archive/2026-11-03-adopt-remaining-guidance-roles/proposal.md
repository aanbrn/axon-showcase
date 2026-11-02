## Why

Two candidate rows remain from the dependency inventory: the generalization workflow's recurrence dispositions, which
`/generalize-process-defects` decides against, and the extraction procedure, which `/apply-audit-reductions` routes a
move through. Both are rules their components apply, both are stated in `AGENTS.md`, and a target that renames either
edits the command that uses it.

## What Changes

- `docs/contracts/process-bindings.md` gains two roles — `generalization-dispositions` (the dispositions bullet and its
  Decision-points section) and `extraction-procedure` (the extraction heading) — adopted by the two commands, each with
  the stop clause.
- The inventory settles both rows, so no section row remains unresolved.
- ADR-0028 and ADR-0025 gain one dated amendment each: two roles added, and the owner's lift for this unit.
- No requirement changes: the `agent-skills` requirement delegates the role set to the declaration.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs` on the ground that the requirement points at the declaration for the role set.

## Impact

- `docs/contracts/process-bindings.md` — two roles and two trace rows.
- `.opencode/commands/generalize-process-defects.md`, `.opencode/commands/apply-audit-reductions.md`.
- `docs/adr/0028-bindings-cover-the-process-defect-records.md`, `docs/adr/0025-process-core-and-project-substance.md` —
  one dated amendment each.
- `docs/decisions/2026-11-02-process-reuse-dependency-inventory.md` — two rows settled, none left unresolved.
- `docs/process-defects/adopt-remaining-guidance-roles.md`.
- No runtime code, build, API, schema, image, chart, or deployment change.
