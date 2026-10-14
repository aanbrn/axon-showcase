# Proposal

## Why

A check's known-bad control is required as a **task**, and a task is a box that gets ticked without the control running.
The repo's working pattern is a committed test; the figures this rests on are in this change's claims table
(`design.md`), each with the command that derives it.

## What Changes

- **`openspec/config.yaml`** — the check-control rule requires a **committed test** (the known-bad input fails, the
  known-good passes) instead of a task.
- **`AGENTS.md`** — the check-control gotcha says the same, and names the `*RulesTests` shape.
- **`showcase/quality/code-quality`** (delta) — the code-quality requirement gains the check-control convention.
- **`build-logic`** — no edit owed: every rules-object gate's `*RulesTests` already carries its known-bad case.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/code-quality`: a new requirement, "A check's control is a committed test".

## Impact

- Files: `openspec/config.yaml`, `AGENTS.md`, the `showcase/quality/code-quality` delta.
- No runtime impact: a convention and its spec clause.
