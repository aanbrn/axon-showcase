# Record one continuous review-event history per unit

## Why

Two records describe one unit's reviews: `rounds.tsv` holds the quick loop's rounds and `sessions.tsv` the deep
reviewer's and the advisor's invocations, each with its own session fields and numbering, so a reader cannot see the
unit's review sequence without joining them, and the quick reviewer's session continuity lives in one record while every
other agent's lives in the other. `gate-review-handover-section` (#864) recorded the first live mixed history — quick
rounds interleaved with registered deep and consultation invocations — which is the condition the parked idea named for
revisiting it. The deep review's result has the same problem: its findings and statuses are recorded on the change's
review task, so a unit with no change directory has no proper home for them — the routing decision's pull-request
fallback never covered them — which the design's D9 records.

## What Changes

- `docs/units/<unit>/sessions.tsv` — the unit's review-event register: one row per stateful agent's invocation,
  including the quick reviewer's for a post-retirement unit, plus a unit-wide `event` number from the stated boundary
  and, on a quick row, the round it reports.
- `docs/units/<unit>/deep-review.md` — the unit's deep-review record: the routing decision and its signals, the passes
  and their kinds, each finding's status and evidence, and the owner's acceptance provenance.
- `docs/units/<unit>/rounds.tsv` — the quick loop's outcome detail (date, unit, round, classes, repeated, compliance).
  Its session fields retire per unit: a unit whose first round is dated on or after the retirement date carries six
  fields; a unit already carrying the session fields keeps them for its lifetime.
- `scripts/commit-hygiene.py` — `--agent-sessions` admits `review-quick` and validates the event sequence.
- `scripts/commit-hygiene.py` — `--agent-sessions` validates the quick row's round and its `Round <N>` hand-over link.
- `scripts/commit-hygiene.py` — `--agent-sessions` validates the register's row widths, while `--session-continuity`
  judges each unit's ledger width and the session fields of the nine-field units only.
- `scripts/commit-hygiene.py` — the checker cross-checks each post-retirement round against exactly one quick `report`
  row.
- `scripts/commit-hygiene.py` — the unit layout admits `deep-review.md` at the unit's first level.
- `scripts/review-rounds.py` — the digest joins the register for a six-field row's session and never requires one.
- `.opencode/skills/review-gate/SKILL.md` and `.opencode/agent/review-quick.md` — for a post-retirement unit the quick
  preflight resolves the session and writes the hand-over section, the register row is appended when the outcome is
  known, and the round's outcome goes to the ledger; a unit's earlier rounds keep the ledger's session fields.
- `showcase/quality/commit-hygiene` and `showcase/quality/agent-skills` (deltas) — the register's format and continuity,
  the round format and continuity, and the quick loop's record location.
- `docs/contracts/process-bindings.md` — the `agent-session-record` role holds the unit's event sequence, and a
  `deep-review-record` role names the unit's deep-review record.
- `.opencode/commands/review-thorough.md` and the review-gate skill — the deep review's routing decision and pass
  results are recorded on the unit's record; the change's task or the pull-request checklist carries the obligation and
  a pointer.
- `README.md` — the review paragraph names the one sequence.
- `docs/ideas.md` — the implemented idea is removed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene` — the agent-session register becomes the unit's review-event sequence, the quick
  round's session fields retire per unit, the checks validate both records' widths and links, a six-field round's ledger
  row and its register report row must correspond, and the layout admits the unit's deep-review record.
- `showcase/quality/agent-skills` — the quick reviewer's session is recorded in the register for a post-retirement unit,
  while the ledger keeps the loop's outcome detail and the gates' population, and the deep review's result is the unit's
  record rather than the change's task.

## Impact

- **Scripts and tests**: the checker's modes and the digest; no build task or pre-commit rule changes beyond what the
  existing modes already cover.
- **Records**: a new field on the register from the boundary; the ledger's width per unit; the unit's new
  `deep-review.md`. No existing unit's rows are rewritten.
- **Documents and specs**: the review-gate skill, the quick reviewer's definition, the `/review-thorough` command, the
  bindings declaration, the README, and the two deltas.
- **Unchanged**: the gates' typed projections (the latest quick round for `--expect-clean`, the ledger for the routing
  signal and the digest, full passes for the deep budget), the finding store, and the `--handover` check.
