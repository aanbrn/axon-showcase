# Proposal

## Why

The per-round record measures the review's cost and shows it persisting, while the pre-write derivation pass those rules
describe has only a ticked box as its output, so each round re-derives the same classes. The figures this rests on are
in this change's claims table (`design.md`), each with the source or command that derives it.

## What Changes

- **The pass emits a claims table** — each fact the change states about its evidence, with its source or the deriving
  command. `AGENTS.md`'s review-loop bullet says so; `openspec/config.yaml`'s `design` rule carries it and its `tasks`
  rule names it; `.github/PULL_REQUEST_TEMPLATE.md` records it for a unit with no change dir.
- **The caller passes the prior rounds' instances** (`file:line` and the claim), not only their classes, so a repeat is
  reported as an unfixed prior instance or as a new instance of an exhausted class — in `AGENTS.md` and
  `.opencode/agent/review-quick.md`'s instructions list.
- **`showcase/quality/agent-skills`** (delta) — the quick-review scenario gains both clauses.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Per-change quality-gate and analysis subagents are available" requirement's
  quick-review scenario.

## Impact

- Files: `AGENTS.md`, `openspec/config.yaml`, `.github/PULL_REQUEST_TEMPLATE.md`, `.opencode/agent/review-quick.md`, the
  `showcase/quality/agent-skills` delta, and this change's own claims table (dogfooding the mechanism).
- No code, build, or test impact: a process change to the review contract and the pass's output.
