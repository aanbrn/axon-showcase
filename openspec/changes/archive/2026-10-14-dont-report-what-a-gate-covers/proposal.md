# Proposal

## Why

The review reports a class a deterministic gate already rewrites — a formatting defect `./gradlew spotlessApply` (or the
web module's `npmFormat`) would rewrite needs no finding — so a round can carry a finding the build would have fixed for
free. The figures are in this change's claims table (`design.md`), each with the source that derives it.

## What Changes

- **`.opencode/agent/review-quick.md`** — the convention-violation bullet routes a defect the applying command rewrites
  to that command rather than reporting it.
- **`showcase/quality/agent-skills`** (delta) — the quick-review scenario gains the clause.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Per-change quality-gate and analysis subagents are available" requirement's
  quick-review scenario.

## Impact

- Files: `.opencode/agent/review-quick.md`, the `showcase/quality/agent-skills` delta, `docs/review-rounds.tsv` (this
  unit's per-round record), and this change's own claims table.
- No runtime impact: a review-instruction clause.
