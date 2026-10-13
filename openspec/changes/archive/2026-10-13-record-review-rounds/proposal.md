# Proposal

## Why

The review loop's cost is the 2026-10-13 retrospective's central finding, but it is unmeasured: a round's findings live
only in the session, so the cost cannot trend and `cut-review-loop-rounds`'s pre-write pass cannot be evaluated next
window. The parked 2026-10-13 idea names the gap and its two routes — a record line from the review, aggregated by the
digest — and this change lands them.

## What Changes

- **`.opencode/agent/review-quick.md`** — every round emits one machine-readable record line in its report (date, unit,
  round, the classes raised, which repeated a prior class, and how many findings were a compliance miss of an existing
  rule), named in the report contract as part of the report's output shape, so the calling agent can append it without
  re-deriving anything.
- **`.opencode/commands/retrospective.md`** and **`.opencode/agent/experience-analyzer.md`** — their enumeration of the
  derived-facts block's contents gains the window's review rounds (the description sweep the analogous digest change
  carried).
- **`AGENTS.md`** — the auto-review pointer names the per-round record line, a docs sweep the change causes.
- **`docs/review-rounds.tsv`** (new) — an append-only, one-line-per-round record the main agent appends to after each
  review round; a header line plus one row per round, including rounds for units with no change dir.
- **`scripts/experience-analysis.sh`** — the derived-facts block summarizes the window's record lines (rounds, their
  classes, and the compliance-miss count), so the retrospective reads the loop's cost from the digest.
- **`showcase/quality/agent-skills`** (delta) — the quick-review scenario gains the record-line clause.
- **`docs/ideas.md`** — the implementing change removes the 2026-10-13 "Record per-unit review rounds" entry.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Per-change quality-gate and analysis subagents are available" requirement's
  quick-review scenario (the record-line clause), and the "Experience analysis is available to agents" requirement (the
  digest's countable claims gain the window's review rounds).

## Impact

- Files: `.opencode/agent/review-quick.md` (the clause and its report contract), `docs/review-rounds.tsv`,
  `scripts/experience-analysis.sh` (its block and its header comment), `.opencode/commands/retrospective.md` and
  `.opencode/agent/experience-analyzer.md` (the block's description sweep), `AGENTS.md` (the auto-review pointer),
  `docs/ideas.md`, and the delta spec for `showcase/quality/agent-skills`.
- Not edited (deliberately), within `AGENTS.md`: its experience-analyzer bullet; and `README.md`'s `/retrospective`
  descriptions — capability-level summaries with no sub-enumeration of the block's contents.
- No build or test impact: a report-contract clause, a new data file, and a shell-script summary; the record is verified
  by running the digest against a seeded record.
