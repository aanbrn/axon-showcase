# Process defects — bump-dependencies-2026-11-09

### bump-dependencies-2026-11-09#1 — a self-record stated a figure its own run contradicted

- **Recorded** — 2026-11-09, by the unit's implementing agent.
- **Stage** — review.
- **Expected** — the review gate's pre-write pass requires each claim row and task `**Done**` note to be re-derived from
  the source it names, and the gate warns that a figure written from what the edits were meant to do is the claim the
  next round reports unreproducible.
- **Observed** — the closing quick-review rounds kept raising one instance each of a self-record figure that the tree or
  the tool output contradicted: the `@vitest/istanbul-*` declarer (round 10), a vacuous `git grep` pathspec (round 8),
  an over-stated verification reach (round 7), an over-stated change to the record itself (round 3, the hand-over's "one
  bullet per coordinate"), and a report row the claim list had missed (round 2).
- **Evidence** — `docs/units/bump-dependencies-2026-11-09/findings/round-2.md`, `round-3.md`, `round-7.md`, `round-8.md`
  and `round-10.md`, each naming the artifact line and the contradicting source.
- **Consequence** — five extra review rounds across the proposal and implementation stages; every instance was repaired
  in the working tree, and the closing round 11 raised none. Nothing is still broken.
- **Knowledge boundary** — observed the review reports and the artifacts they cite; did not measure whether the same
  pattern occurs in other units.

### bump-dependencies-2026-11-09#2 — a consultation's report was not recorded until a review round asked for it

- **Recorded** — 2026-11-09, by the unit's implementing agent.
- **Stage** — proposal.
- **Expected** — the review gate records a non-round pass's findings in the unit's `review-handover.md`.
- **Observed** — the `advisor-scope-1` section carried the `/consult` handoff fields only; the consultation's result and
  findings were written into it after round 6 reported `missing-task`.
- **Evidence** — `docs/units/bump-dependencies-2026-11-09/findings/round-6.md`, its first entry, against
  `review-handover.md`'s `advisor-scope-1` section.
- **Consequence** — one review round; the section now carries the result, findings and disposition. Nothing is still
  broken.
- **Knowledge boundary** — observed the section's state before and after the fix; did not check whether earlier units
  recorded consultations the same way.
