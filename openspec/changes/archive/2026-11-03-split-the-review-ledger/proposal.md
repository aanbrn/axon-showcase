# Proposal

## Why

The review-round ledger is one append-only file for every unit: 2,366 lines covering 256 units. Every unit that runs the
review loop appends to it, so parallel units in sibling worktrees contend on one shared file, and a unit's own history
is scattered through a file it shares with 255 others. `docs/units/<unit>/` now exists as each unit's home (ADR-0029),
so the rounds can live beside the record they describe.

## What Changes

- Each unit's rows move to `docs/units/<unit>/rounds.tsv`: the data rows exactly as recorded, its acknowledgement
  comments, and its share of the session-fields boundary. A full split, so every unit in the ledger — including one that
  never had a unit record — gets its own file; the global `docs/review-rounds.tsv` is retired.
- The record format is stated once, in the capability spec, and the per-unit files carry no normative header.
- `commit-hygiene.py` reads the set of records for its four round checks and its ledger-preservation guard; the guard
  judges a staged `rounds.tsv` per file.
- `scripts/review-rounds.py` and `scripts/review-routing.py` read the set, and their aggregates and routing results are
  unchanged.
- `docs/contracts/process-bindings.md`, `AGENTS.md`, and the two capability specs name the per-unit path; the build's
  ledger inputs point at `docs/units/`.
- The parked idea for this move is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/commit-hygiene`: the round records are per-unit files; the guard, the uniqueness and contiguity
  verification, the acknowledgement comments, and the class vocabulary judge them per unit.
- `showcase/quality/agent-skills`: the retrospective digest reads the per-unit records.

## Impact

- Data: `docs/units/<unit>/rounds.tsv` for 256 units; `docs/review-rounds.tsv` deleted. No row value, round number, or
  acknowledgement is changed; a unit that only the ledger knows gets a directory with `rounds.tsv` and no `unit.md`.
- Scripts: `scripts/commit-hygiene.py`, `scripts/review-rounds.py`, `scripts/review-routing.py`, and their three test
  suites.
- Build: every ledger input site in `build.gradle.kts` (task 4.1 names them).
- Docs: the two capability specs, the bindings, `AGENTS.md`, `docs/ideas.md` (its parked entry and three live mentions),
  `.opencode/skills/review-gate/SKILL.md`, ADR-0026's Context (a dated `## Amendment`), and ADR-0027's `Revisit when:`
  coordinates.
- No runtime, API, or deployment change.
