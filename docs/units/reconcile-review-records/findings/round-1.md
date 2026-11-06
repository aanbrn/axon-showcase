- [claim-unreproducible] design.md:13 — round 12 is attributed to the advisor's definition, while `round-12.md` holds
  the ADR over-claim that the round-13 hand-over section calls the deep review's, and no other row shares its session.
- [coherence] design.md:61 — the design and the task require a row explanation, but the ledger's ninth field is the
  closed reason enum and a free-text reason fails the continuity verification.
- [test-control] tasks.md:18-19 — the verification runs `--session-continuity` and `--unique-rounds`, which pass on the
  unmodified `caller-restarted` row, so it cannot detect the reclassification.
- [impact-omission] tasks.md:7-11 — task 1.1 folds into one section while the hand-over cites the deleted files in six
  places, leaving five dead references.
- [incomplete-enumeration] tasks.md:14 — "over five entries" under-counts; the pin unit's two files hold nine.
- [incomplete-enumeration] tasks.md:27 — "all thirteen scenarios"; the requirement carries fourteen.
- [claim-unreproducible] design.md:13,99 — `advisor-1.md` holds four bullets (two findings, a hypothesis, an open
  question), not "four more findings", and round 12's over-claim is a different instance.
- [claim-unreproducible] design.md:103 — the grep's "two hits" is not anchored to the base, so the change's own
  artifacts inflate it.
- [coherence] design.md:5-7 — the parked idea's "with the same class recorded in each unit's `process-defects.md`" is
  dropped with no task and no Non-Goal.
- [contradiction] design.md:11 — process-defect `#3` of `assert-paketo-pin-coverage`, which task 3.1 edits, states that
  the two merged units carry five such files.
- [claim-unreproducible] design.md:61-62 — round 13's cause is asserted, though its session is the quick reviewer's own
  resumed one and the Non-Goals leave those rows to their own evidence.
- [over-claim] design.md:78 — "a check a commit cannot pass" exceeds the delta, which fails only on a file whose name is
  not `round-<N>.md`.
