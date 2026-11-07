# Process defects — generalize-process-defects-2026-11-07

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### generalize-process-defects-2026-11-07#1 — Round 1's ledger row was appended after its fixes

- **Recorded** — 2026-11-07, by the writing session, while appending the row.
- **Stage** — review.
- **Expected** — `review-gate` §2 step 2 runs when the report arrives and before a fix is applied: the round's row and
  its instances file are written first, so a loop that loses its context still holds the round's record.
- **Observed** — round 1's report was read, findings 1–2 verified and findings 3–6 applied to the unit record and the
  pull-request draft, and only then were `rounds.tsv` and `findings/round-1.md` written.
- **Evidence** — the fixes to `unit.md` and the PR draft precede the first write of `rounds.tsv` and
  `findings/round-1.md` in this turn; the row records round 1 with `5` compliance misses as the reviewer's line stated.
- **Consequence** — the round's instances were reconstructed from the report's text rather than captured at arrival;
  nothing was lost because the report was still in context.
- **Knowledge boundary** — observed the write order and the report. Did not check whether the same order was used for
  the proposal-screen round in an earlier generalization unit.

### generalize-process-defects-2026-11-07#2 — A green formatter claim was recorded before the section's last edit, in the unit that generalized the class

- **Recorded** — 2026-11-07, by the writing session, on round 6's report.
- **Stage** — review.
- **Expected** — after the Round 6 hand-over section was appended, `spotlessApply` runs before the check is recorded,
  since the section is a Spotless-owned markdown file and the edit that follows a green check can falsify it.
- **Observed** — the Round 6 section was appended after the last `spotlessApply` and staged, and its "Completed checks"
  line stated that `spotlessApply`/`spotlessCheck` had run after the last edit. Round 6's re-run failed
  `:spotlessMarkdownCheck` naming `review-handover.md`; the section itself was unwrapped.
- **Evidence** — round 6's finding; a `./gradlew spotlessCheck` run naming
  `docs/units/generalize-process-defects-2026-11-07/review-handover.md`.
- **Consequence** — one review round and one formatter run; nothing was committed, and the claim was corrected.
- **Knowledge boundary** — observed the append order and the failing re-run. Did not check whether rounds 1–5 shared it;
  each of their sections was followed by a `spotlessApply` before its check, so only this one is evidenced.

### generalize-process-defects-2026-11-07#3 — The `proposed` Decision lines were merged without their pull-request link

- **Recorded** — 2026-11-07, by the shipping session, while settling the unit.
- **Stage** — ship.
- **Expected** — the command's step 7 records a `proposed` line with a link, and the prior generalization unit's
  practice adds the pull request's link to those lines before the merge.
- **Observed** — the five `proposed` lines were committed and merged on #859 without a link, and this unit record's
  "Applied" section claimed the link was added before the merge.
- **Evidence** — `git show 9b9d7906:docs/units/assert-paketo-pin-coverage/process-defects.md` reads
  `proposed: applied as P1 …`; the record's "Applied" sentence; #859's merged diff.
- **Consequence** — `main` carried the non-conforming lines between the merge and the follow-up docs unit
  `link-generalization-decision-records`, which adds the link and corrects the sentence.
- **Knowledge boundary** — observed the merged lines and the sentence. Did not check whether a gate validates those
  lines; `docs/ideas.md` 2026-11-01 records that none does.
