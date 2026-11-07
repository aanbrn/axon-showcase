### track-agent-session-continuity#1 — Two rounds' ledger rows were appended after the fixes they record

- **Recorded** — 2026-11-07, the agent.
- **Stage** — proposal review.
- **Expected** — the review gate's §2 step 2 appends each round's row, and writes its finding instances, when the report
  arrives and before any fix is applied.
- **Observed** — round 1's row was appended when its report arrived; rounds 2 and 3 were fixed before their rows were
  appended, and their `findings/round-<N>.md` files were written after the fixes as well.
- **Evidence** — the turn's tool sequence: `findings/round-2.md` and its row were written after the edits to both
  deltas, the design, and `tasks.md`; the same order held for round 3.
- **Consequence** — the rows still record the reports' own session, classes, and counts, so the ledger is correct; the
  ordering left each round unrecorded for the duration of its fixes, and the `--expect` check ran after them.
- **Knowledge boundary** — observed in this session's own tool sequence; whether a crash in that window would have lost
  the round's classes is a possibility, not an observed outcome.

### track-agent-session-continuity#2 — The implementation-readiness consultation ran unrecorded

- **Recorded** — 2026-11-07, the agent.
- **Stage** — implementation review.
- **Expected** — a registered invocation writes its hand-over section before the call and appends its register row when
  the outcome is known, as the change's own requirement states.
- **Observed** — the implementation-readiness consultation ran without a hand-over section and without a register row;
  the technical advisor found the omission while reviewing the implementation.
- **Evidence** — the advisor's follow-up named the register's seven rows and the missing eighth key; the rows were added
  afterwards, and both sections state that they were written late.
- **Consequence** — the record now covers both invocations; the omission cost one review round, and the late sections
  say so rather than presenting themselves as pre-call.
- **Knowledge boundary** — observed from the advisor's report and the register's content; whether an earlier write would
  have changed the consultation is not established.
