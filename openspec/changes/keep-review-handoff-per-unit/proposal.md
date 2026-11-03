# Proposal

## Why

A reviewer invocation's hand-over — the record of what the reviewer was handed, written before the caller waits on it —
has no fixed per-unit home: it is written in the change directory, so a unit with no change directory has nowhere to put
it, and reading a unit's hand-over means knowing which change directory holds it. The finding instances now have a
per-unit store; the invocation context has none.

## What Changes

- The hand-over is written at `docs/units/<unit>/review-handover.md`, one section per quick-review invocation appended
  to the unit's file, so a unit without a change directory records it and successive invocations keep their own context.
- The hand-overs already recorded inside a change directory or its archive stay where they are and are read there; the
  new file is not a migration of them, and its absence does not mean a unit had no earlier invocation.
- The review gate's preflight names that path where it requires the record before waiting, for the quick loop's
  invocations; the deep review's hand-over stays `/review-thorough`'s.
- The `agent-skills` requirement names the location and the one-section-per-invocation rule.
- The `prior-findings` binding resolves the hand-over to the unit's file, and to the change directory or its archive for
  a round recorded before it.
- The parked idea for this work is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/agent-skills`: the quick review loop's hand-over is kept per unit at
  `docs/units/<unit>/review-handover.md`, one section per invocation, and an earlier hand-over is read where it was
  recorded.

## Impact

- Docs: `.opencode/skills/review-gate/SKILL.md`, `docs/contracts/process-bindings.md`, the `agent-skills` delta, and the
  removed `docs/ideas.md` entry.
- Data: one file per unit that runs a quick-review invocation, at `docs/units/<unit>/review-handover.md`.
- No scripts, build, runtime, API, or deployment change.
