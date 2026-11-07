# Process defects — record-unit-change-layering

### record-unit-change-layering#1 — A hand-over recorded checks as run before the file it sat in existed

- **Recorded** — 2026-11-06, by the main agent.
- **Stage** — proposal.
- **Expected** — the review gate's hand-over records the checks completed before the reviewer invocation, and the
  boundary's readiness check verifies the staged set; a check recorded there should have run over the artifact carrying
  the record.
- **Observed** — round 1's hand-over stated that the unit's files were staged and that `spotlessApply` had run, but it
  was written after that run, so the reviewer read an untracked, unformatted file and a `tasks.md` staged before its
  last edit.
- **Evidence** — the round-1 finding `[claim-unreproducible]` at
  `docs/units/record-unit-change-layering/findings/round-1.md`; `git status` showed the hand-over untracked and
  `tasks.md` as `AM`, and `spotlessCheck` failed on the hand-over.
- **Consequence** — one of round 1's six findings and a re-stage pass; repaired by formatting and staging every unit
  file before round 2, so no residue reached the owner's pass.
- **Knowledge boundary** — observed this unit's hand-over, the status output, and the finding; not checked whether
  earlier units' hand-overs carried the same defect, since only this unit's file was inspected.
- **Decision** — no-change: already covered by `AGENTS.md`'s Formatting convention — run `spotlessApply` after the final
  write, and treat the tick claiming a gate as the edit that falsifies it.

### record-unit-change-layering#2 — A duplicate requirement was proposed with the existing owners already in hand

- **Recorded** — 2026-11-06, by the main agent.
- **Stage** — proposal.
- **Expected** — before proposing a new capability requirement, establish whether the corpus already owns the behavior:
  the premise rule requires the existing requirement to be adopted rather than re-derived.
- **Observed** — the scope extension proposed a new `agent-skills` requirement for "a unit without a change directory is
  governed as a unit", after a grep returning the four requirements that already state those behaviors, and against the
  ownership model's ban on a second copy.
- **Evidence** — the four anchors the consultation named and this unit re-read: `agent-skills` "A unit without a change
  directory resolves like any other", `commit-hygiene` "A unit without a change directory is judged" and "A unit that
  declares no change directory stages none", and `agent-skills`'s no-change-dir pull-request route; ADR-0025's "behavior
  in the capability spec … rather than carrying a second copy".
- **Consequence** — one consultation round and a rewritten proposal (A+B narrowed to A); no edit was written under the
  wider scope, and the owner's condition on the consultation is what surfaced it.
- **Knowledge boundary** — observed the grep, the proposal as first written, and the consultation's citations; not
  checked whether a stricter reading of the ownership model alone would have stopped the proposal before the
  consultation.
- **Decision** — no-change: already covered by the premise rule and the ownership model (ADR-0025) the record cites.
