# Process defects — verify-review-class-vocabulary

### verify-review-class-vocabulary#1 — The change dir was staged before the proposal review and went stale

- **Recorded** — 2026-11-02, by the unit's agent.
- **Stage** — proposal.
- **Expected** — the review gate stages the unit's new files before the manual pass, so the index carries the artifacts
  the review screened.
- **Observed** — the change dir was staged before round 1, to give `scripts/claim-scan.py` a diff to scan (an untracked
  directory appears in no diff), and the four review rounds then edited it, so the index held the round-0 artifacts
  while the reviewer screened the working tree. Rounds 4 and 5 each reported the stale index as a limitation.
- **Evidence** — round 4's limitation: "the staged index is older than the working tree for `design.md` (its index copy
  predates the round-2 claims row)"; round 5's: "the staged index is one line behind the working tree for `design.md`
  and behind `review-handover.md`".
- **Consequence** — no commit was made from the stale index and the readiness step would have named it; the cost was two
  review limitations and a re-stage before the manual pass.
- **Knowledge boundary** — observed the stale index and the two reports; did not check whether an earlier `git add` of a
  single path (rather than the directory) would have fed the scan without staling the rest.

- **Decision** — proposed: applied as `review-gate` §2's mid-loop re-stage bullet; the staging-snapshot rule already
  named the state.

### verify-review-class-vocabulary#2 — The stale-index defect recurred after every fix round

- **Recorded** — 2026-11-02, by the unit's agent.
- **Stage** — proposal (the consultation and re-review rounds after the first manual-pass request).
- **Expected** — record #1's remedy: re-stage the unit's files after each edit, so the index and the working tree agree
  when the next round reads them.
- **Observed** — each round's edit landed in the working tree while the index kept the previous round's copy, and round
  7 reported `MM docs/review-rounds.tsv` and `AM design.md`; nothing was re-staged between rounds 6 and 7.
- **Evidence** — round 7's limitation: "the staged index is behind the working tree — the round-6 ledger row and the
  Decision 4 edit are unstaged (`MM docs/review-rounds.tsv`, `AM design.md`)".
- **Consequence** — the same review limitation three times (rounds 4, 5, and 7); no commit was made, and the readiness
  step is the last check before the manual pass.
- **Knowledge boundary** — observed the recurrence and the reviewer's report; did not test whether staging once at the
  end, rather than after each round, would avoid the limitation without staling the scan's input.

- **Decision** — proposed: applied as `review-gate` §2's mid-loop re-stage bullet, which applies §5's explicit-add
  remedy after each round's fixes.
