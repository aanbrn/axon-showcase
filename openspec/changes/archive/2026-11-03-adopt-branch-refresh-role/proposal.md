## Why

The dependency inventory's remaining section rows are five, and reading each one's use separates them:

- `/sync-branch` applies the branch-refresh rule to the state it finds;
- `/generalize-process-defects` decides dispositions against the generalization workflow's Decision points;
- `/apply-audit-reductions` routes an extraction through that procedure — so three rows are rules their components apply
  or route to.

The ship-change skill's two references are its own `Source:` lines: a component's entry pointer, which the declaration
keeps explicit. Counting those as dependencies was the inventory's over-count.

## What Changes

- `docs/contracts/process-bindings.md` gains the `branch-refresh` role, adopted by `/sync-branch`, with the stop clause.
- The inventory dispositions the ship and cloud-PR rows as entry pointers; the generalization and extraction rows stay
  candidates. This diff settles one row, leaving two settled, two entry pointers and two candidates.
- ADR-0028 and ADR-0025 gain one dated amendment each: a role added, and the owner's lift for this unit.
- No requirement changes: the `agent-skills` requirement delegates the role set to the declaration.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs` is recorded on the ground that the requirement points at the declaration for the role set.

## Impact

- `docs/contracts/process-bindings.md` — one role and one trace row.
- `.opencode/commands/sync-branch.md`.
- `docs/adr/0028-bindings-cover-the-process-defect-records.md`, `docs/adr/0025-process-core-and-project-substance.md` —
  one dated amendment each.
- `docs/decisions/2026-11-02-process-reuse-dependency-inventory.md` — one row settled, two reclassified.
- `docs/process-defects/adopt-branch-refresh-role.md`.
- No runtime code, build, API, schema, image, chart, or deployment change.
