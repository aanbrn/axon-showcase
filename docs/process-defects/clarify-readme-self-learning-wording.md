# clarify-readme-self-learning-wording

### clarify-readme-self-learning-wording#1 — The unit's record was written only after a review required it

- **Recorded**: 2026-10-27, by the writing session (`ses_e84c9b955ffeY6V3z6ZN1VELva`).
- **Stage**: review.
- **Expected**: the unit's process-defect record exists — stating what the unit observed, or that it observed none —
  before the quick review's closing round and the manual-pass request, as `AGENTS.md`'s "Workflow: Record process
  defects" requires.
- **Observed**: the unit changed the README and ran its gates without creating `docs/process-defects/<unit>.md`; the
  record appeared only after the first quick-review round reported its absence.
- **Evidence**: `docs/review-rounds.tsv`'s round 1 for this unit carries `missing-task`, and that round's report named
  the absent file as the finding.
- **Consequence**: one review round raised it; the file was created in the same turn, and what it must say was itself
  corrected in round 2, when the first version stated no defect had been observed. Nothing remains broken.
- **Knowledge boundary**: observed this unit's round 1 and its class. Did not check whether any earlier unit wrote its
  record only after a review found it missing, so this is not evidence of a recurrence.
