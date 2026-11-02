## Why

The dependency inventory recorded six `AGENTS.md` sections as unresolved. The strongest of them is "Workflow: Author a
spec delta and archive": three audit commands route a finding's spec change through it, so a target with differently
named sections edits three files, and nothing has ever exercised the route.

## What Changes

- `docs/contracts/process-bindings.md` gains the `spec-delta` role, with that workflow as its owner.
- `.opencode/commands/audit-agents.md`, `audit-architecture.md`, and `audit-specs.md` resolve it, and each carries the
  missing-binding stop the counterfactual showed they lacked.
- ADR-0028 gains a dated amendment extending its role list.
- No requirement changes: the `agent-skills` requirement points at the declaration for the role set, so a further role
  costs a declaration row.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — the requirement already delegates the role set to the declaration, so `skip_specs` is recorded with that reason.

## Impact

- `docs/contracts/process-bindings.md` — one role and three trace rows.
- The three audit commands.
- `docs/adr/0028-bindings-cover-the-process-defect-records.md` — one dated amendment.
- `docs/process-defects/adopt-spec-delta-role.md`.
- No runtime code, build, API, schema, image, chart, or deployment change.
