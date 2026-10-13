# Proposal

## Why

The 2026-10-13 retrospective's review took eleven rounds (the session's own count; the rounds are recorded nowhere), and
the recurring `contradiction` class was raised instance by instance rather than whole: each round reported the one
instance it happened to find, the fix addressed it, and the next round found another of the same shape. The parked idea
(2026-10-12) names the mechanism: with no prior-round list on round 1, co-classed findings are reported as independent
items, so the root-cause signal fires only after two or more rounds — and the rounds a class is split across are exactly
the cost this work exists to cut.

## What Changes

- **`.opencode/agent/review-quick.md`** — a class raised on **any** round, the first included, is **exhausted**: the
  review searches the artifact for every instance of that class's shape and reports them as **one clustered finding**,
  so a class surfaces whole rather than one instance per round.
- **`showcase/quality/agent-skills`** (delta) — the "Quick review runs after proposal and implementation" scenario gains
  the exhaustion clause (a `MODIFIED` block carrying the requirement's description and all eight scenarios).
- **`AGENTS.md`** — the auto-review pointer widens to name the class-exhaustion clause, a docs sweep the change causes.
- **`docs/ideas.md`** — the implementing change removes the 2026-10-12 entry, per the file's convention.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Per-change quality-gate and analysis subagents are available" requirement's
  quick-review scenario.

## Impact

- Files: `.opencode/agent/review-quick.md`, `docs/ideas.md`, `AGENTS.md` (the auto-review pointer, a docs sweep), and
  the delta spec for `showcase/quality/agent-skills`.
- Considered, no edit owed: `README.md` (its quick-review mentions describe the loop's shape, not the report's classes)
  and the `agent-skills` `## Purpose` (it describes the subagent inventory, not the review report) — each recorded as a
  decision task.
- No build or test impact: a subagent-instruction change. Its verification is a smoke-run — the rule is exercised by the
  unit's own implementation review, which must report an artificially planted co-classed pair as one clustered finding.
