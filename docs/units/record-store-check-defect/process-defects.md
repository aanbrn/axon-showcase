# Process defects — record-store-check-defect

Process defects observed while working this change. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### record-store-check-defect#1 — A corrected claim was left stale in the unit's other copies of it

- **Recorded** — 2026-11-01, by the writing session, during the quick-review loop.
- **Stage** — review.
- **Expected** — the doc-consistency sweep rule: a corrected fact is swept to "every repetition, plus the statements
  that depend on it without repeating it", so correcting one copy of an assertion corrects its siblings in the same
  edit.
- **Observed** — record `#7`'s query-timing assertion was corrected in the record's `Evidence`, while the saved
  pull-request body's Summary and one of its claims-table dismissals kept the removed wording; the next round reported
  both as contradictions.
- **Evidence** — quick-review round 3, findings 1 and 2, quoting the body's Summary and the dismissal against record
  `#7`'s `Knowledge boundary`; round 2's correction had touched the record alone.
- **Consequence** — further review rounds; the body's Summary and dismissal now state what the record states, and the
  dismissal list no longer quotes removed text.
- **Knowledge boundary** — observed the artifacts and the rounds' reports; whether the same sweep would have caught the
  third copy had the edit started elsewhere is not established.

- **Decision** — no-change: the corrected-fact sweep rule and `review-gate` §5's "Re-read every artifact that states the
  corrected claim" already require the sibling copies in the same edit.
