# Proposal

## Why

The review loop has no upper bound, and the 2026-10-13 retrospective's own review showed the cost: eleven rounds (the
session's own count — the rounds are recorded nowhere before unit 3), each raising a new instance of a class an earlier
round had already named. For a **record artifact** — a retrospective, a load-test record — the loop has no shipped
behavior to converge on, so the bound accepts losing the coherence classes a further round cannot exhaust, which the
digest's countable coverage does not replace. The parked 2026-10-13 idea names the bound; unit 1's derived-facts digest,
unit 2's class exhaustion, and unit 3's per-round record are what make it safe to apply.

## What Changes

- **`AGENTS.md`** — the review-loop bullet's pointer enumeration widens to name the record-artifact bound (a pointer
  phrase; the clause text lives in the spec it points at, as the analogous edits did).
- **`showcase/quality/agent-skills`** (delta) — the quick-review scenario gains the bound clause.
- **`docs/ideas.md`** — the implementing change removes the 2026-10-13 "Bound the review loop" entry.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Per-change quality-gate and analysis subagents are available" requirement's
  quick-review scenario.

## Impact

- Files: `AGENTS.md` (the review-loop bullet and its clause enumeration), `docs/ideas.md`, and the delta spec for
  `showcase/quality/agent-skills`.
- Considered, no edit owed: every other site the two analogous changes name — the union read off
  `archive/2026-10-13-record-review-rounds/` and `archive/2026-10-13-exhaust-review-finding-classes/` — is its own
  decision task in `tasks.md` §2 (2.2 `README.md`'s four loop phrases, 2.3 the `agent-skills` `## Purpose`, 2.4
  `.opencode/agent/review-quick.md`, 2.5 the two digest descriptions, 2.6 `scripts/experience-analysis.sh` +
  `docs/review-rounds.tsv`, 2.7 `AGENTS.md`'s experience-analyzer bullet + `README.md`'s `/retrospective` descriptions).
  Each needs no edit because the bound is a record-only loop stop the spec the pointer names owns, and the record's last
  row already shows the class it stopped on.
- **A deliberate trade:** the bound stops a record's loop before it is clean, so a claim it had not yet raised can ship
  unverified — accepted only for a record (no shipped behavior to converge on) and only where the digest derives the
  claims the round would re-check.
- No build or test impact: a process rule and its spec clause.
