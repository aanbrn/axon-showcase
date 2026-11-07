# Process defects — require-owner-authorization-for-start-and-implementation

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### require-owner-authorization-for-start-and-implementation#1 — The round-4 hand-over section was again written after its invocation

- **Recorded** — 2026-11-04, by the writing session, on round 4's report.
- **Stage** — review (the quick loop's preflight).
- **Expected** — one hand-over section per invocation, written before the caller waits; the merged units recorded this
  same miss twice (`generalize-process-defects-2026-11-03#6`, `record-link-sweep-overreach#1`).
- **Observed** — round 4's invocation ran with the file ending at the round-3 section, so the record a successor would
  recover named the previous round's artifact set.
- **Evidence** — `docs/units/require-owner-authorization-for-start-and-implementation/findings/round-4.md`'s
  `missing-task` entry; the file's last section was "Round 3" at the invocation.
- **Consequence** — the section was appended afterwards; nothing was lost, because the round answered. This is the third
  occurrence: `generalize-process-defects-2026-11-03#6`, then `record-link-sweep-overreach#1`, then this one, with
  `make-advisor-consultation-owner-authorized` between the last two recording no such miss.
- **Knowledge boundary** — observed this unit's sequence. Did not check whether the preflight reaches a caller at every
  round, rather than only as the skill's opening rule.
- **Decision** — no-change: already covered by `review-gate` §2's preflight, which records the invocation's hand-over
  before the caller waits; the pre-invocation check is parked in `docs/ideas.md` 2026-11-07.
