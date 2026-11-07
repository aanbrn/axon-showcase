# Process defects — record-link-sweep-overreach

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### record-link-sweep-overreach#1 — The hand-over section was again written after the invocation

- **Recorded** — 2026-11-04, by the writing session, on round 2's report.
- **Stage** — review (the quick loop's preflight).
- **Expected** — one hand-over section per quick-review invocation, written before the caller waits; the merged unit
  recorded this same miss as `generalize-process-defects-2026-11-03#6`.
- **Observed** — round 2's invocation was made with the hand-over carrying only a Round 1 section, so the record a
  successor would recover named a stale artifact set and "Prior findings — none".
- **Evidence** — round 2's `missing-task` finding in `docs/units/record-link-sweep-overreach/findings/round-2.md`; the
  file held no Round 2 section at the invocation.
- **Consequence** — the section was appended afterwards; nothing was lost, because the round answered. The same miss at
  the merged unit is `#6`, so this is its recurrence one unit later.
- **Knowledge boundary** — observed this unit's own sequence. Did not check whether the preflight step reaches a caller
  at every round, rather than only as the skill's opening rule.
- **Decision** — no-change: already covered by `review-gate` §2's preflight, which records the invocation's hand-over
  before the caller waits; the pre-invocation check is parked in `docs/ideas.md` 2026-11-07.
