# Proposal

## Why

A quick-review round's findings — each one's location, class, and claim — live only in the reviewer's report and, when
the unit pauses, in the `Resume checkpoint`'s "Findings no record holds" field. `rounds.tsv` records classes and counts,
never instances, and the hand-over leaves with the archive. So a resumed round whose checkpoint omitted an instance, or
an audit after the merge, cannot read what a round actually found. This unit's own round 1 survived only because the
checkpoint carried it; the checkpoint's replacement would have erased it.

## What Changes

- Each review round's finding instances are written to `docs/units/<unit>/findings/round-<N>.md` when the round's report
  arrives: one entry per finding with its class, its location (`file:line`), and its claim, in the reviewer's order.
- The `rounds.tsv` row is unchanged; instances are a per-round file beside it, so the record's format stays fixed.
- A `commit-hygiene` check reports every failure the requirement
  `Every review round recorded after the boundary keeps its finding instances` names for a round recorded after the
  store's boundary; rounds dated on or before the boundary are exempt, because they were recorded before the store
  existed.
- The pre-commit guard judges the index's record and instances files for the affected unit, so a staged deletion and a
  file an earlier commit recorded are both seen.
- The `prior-findings` binding names the store, with the checkpoint or the hand-over as the fallback for a round that
  predates it; a unit holding neither reads as unrecovered context rather than as a clean round.
- The quick-review loop, the pause-and-resume procedure, the review-gate skill, `AGENTS.md`, and the bindings name the
  store; the parked idea for this work is removed from `docs/ideas.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/agent-skills`: the quick review loop persists each round's finding instances, and the
  `prior-findings` binding resolves to that store rather than to a checkpoint field alone.
- `showcase/quality/commit-hygiene`: a round recorded after the boundary carries its instances file, verified by the
  build and by the pre-commit guard.

## Impact

- Scripts: `scripts/commit-hygiene.py` (one new check, and the guard's staged judgment) and
  `scripts/test-commit-hygiene.py`.
- Build: one new input for the records directory already declared by the four ledger tasks.
- Docs: `AGENTS.md`, `.opencode/skills/review-gate/SKILL.md`, `.opencode/skills/pause-resume-unit/SKILL.md`,
  `docs/contracts/process-bindings.md`, ADR-0026's Context (a dated `## Amendment`), and the removed `docs/ideas.md`
  entry.
- Data: one file per review round recorded after the boundary, under `docs/units/<unit>/findings/`.
- No runtime, API, or deployment change.
