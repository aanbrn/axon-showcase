# Process defects — reconcile-review-records

### reconcile-review-records#1 — The unit's review records were not written as its rounds ran

- **Recorded** — 2026-11-06, by the main agent, on round 2's finding.
- **Stage** — proposal.
- **Expected** — each review round's ledger row, its instances file, its hand-over section and the unit's
  `process-defects.md` are written when the report arrives, and the checkpoint's "Findings no record holds" field
  matches what exists.
- **Observed** — after round 1 the unit held only `unit.md`: no `rounds.tsv`, no `findings/round-1.md`, no
  `review-handover.md`, and no `process-defects.md`, while `unit.md` stated that round 1 raised twelve findings and that
  no finding lacked a record. Round 2's reviewer reported the absence as a `missing-task` finding.
- **Evidence** — round 2's first entry, "the unit holds only `unit.md`: no ledger, no instances files, no hand-over and
  no process-defect record", and `ls docs/units/reconcile-review-records` before the correction.
- **Consequence** — one review round was spent reporting the gap; the records for rounds 1 and 2 were written afterwards
  from the reports, and the checkpoint's "Findings no record holds" field was false until then.
- **Knowledge boundary** — observed: the missing files and the finding that named them; not checked: whether the same
  omission happened in the previous unit before its round 5 caught a related one.

### reconcile-review-records#2 — A review round was started in a new session without an evidenced recovery case

- **Recorded** — 2026-11-06, by the main agent, on its own observation while writing the ledger.
- **Stage** — proposal.
- **Expected** — a replacement reviewer session starts only after one of the recovery cases the review gate names
  (`expired`, `definition-changed`, `stopped-responding`) is evidenced, and the round is invoked in the session that
  carries the previous round's context.
- **Observed** — round 2 was invoked without the round's session identifier, so it started
  `ses_e50c53a98ffeHkTzb23uJaA9nG` where round 1 ran in `ses_e50d7fc82ffer27IR8L9zlCagv`; no recovery case was
  evidenced.
- **Evidence** — the two identifiers in `rounds.tsv`, the second row's `replaced` transition, and its `caller-restarted`
  reason, which is the ledger's marker for a continuity violation.
- **Consequence** — the ledger records the violation, and the review gate stopped for the owner's recovery decision.
  **The owner resolved it on 2026-11-06 by instructing the unit to continue** ("Продолжай раз уж начал"), so round 2's
  findings stand and the next round continues round 2's session.
- **Knowledge boundary** — observed: the two identifiers and the absence of an evidenced case; not checked: whether the
  round would have continued round 1's session had its identifier been supplied.

### reconcile-review-records#3 — Files were edited after staging, and the staged set went stale

- **Recorded** — 2026-11-06, by the main agent, on round 4's finding.
- **Stage** — proposal.
- **Expected** — a file edited after it was staged is re-staged before the next reviewer invocation or hygiene check,
  since staging is a snapshot and `--review-ready` judges the index.
- **Observed** — five paths were staged once and edited afterwards, and `findings/round-3.md` was never added;
  `commit-hygiene --review-ready` then failed naming all six, and round 4's reviewer read the index, which still held
  the pre-revision copies.
- **Evidence** — round 4's first finding, "the staged set is stale and one record is untracked", with the `AM` and `??`
  statuses it cites; the `--review-ready` failure naming the six paths.
- **Consequence** — one review round spent on a staging snapshot rather than on the artifacts; re-staging corrected it,
  and the hand-over's round-5 section records that the index is re-staged.
- **Knowledge boundary** — observed: the stale index, the untracked file and the failed check; not checked: whether an
  earlier round in this unit also read a stale index without reporting it.
