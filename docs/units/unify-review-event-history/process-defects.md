# Process defects — unify-review-event-history

### unify-review-event-history#1 — The proposal-readiness consultations ran unrecorded

- **Recorded** — 2026-11-08, the agent.
- **Stage** — proposal review.
- **Expected** — a consultation the unit binds to itself resolves its session from the unit's agent-session register and
  records each invocation there, with its hand-over section written before the call (the `agent-skills` requirement and
  `/consult` step 4).
- **Observed** — two advisor invocations on this unit's proposal ran with no register row and no hand-over section; the
  unit's `sessions.tsv` did not exist until the owner asked why.
- **Evidence** — the consultation's session `ses_e46e7bee6ffeTd3OP71pqnjY7N` is absent from every unit record; the two
  rows and their sections were added afterwards, and both sections state that they were written late.
- **Consequence** — the record now covers both invocations; the omission left the unit's history without its only
  registered consultations for the duration of the review, and a reader of the register would have concluded that no
  consultation ran.
- **Knowledge boundary** — observed from the unit's files and the conversation's invocation identifiers; whether the
  consultations would have been recorded had `/consult` rather than a direct delegation been used is not established.
- **Decision** — no-change: the same rule as `track-agent-session-continuity#2`; the advisor's manual-reconciliation
  idea is a hypothesis, not a promoted rule.

### unify-review-event-history#2 — A new helper shadowed an existing function of the same name

- **Recorded** — 2026-11-08, the agent.
- **Stage** — implementation.
- **Expected** — the new helper reads a round record's round column, as a function beside the other record helpers.
- **Observed** — it was named `ledger_rounds`, which the module already defined earlier as
  `ledger_rounds(repo, records)`; the later definition replaced the earlier one, so the existing round-gap check called
  the new signature and every path through it raised `TypeError`.
- **Evidence** — `python3 scripts/test-commit-hygiene.py` reported 443 tests with 1 failure and 39 errors, each error
  tracing `check_staged` → `check_unique_review_rounds` → `find_round_gaps` at `scripts/commit-hygiene.py:1560`.
- **Consequence** — 39 tests errored and one failed for the duration of the edit; renaming the helper to
  `round_record_rounds` restored them (472 tests OK).
- **Knowledge boundary** — observed from that run's traceback; whether the same name collision exists for other new
  helpers added in this unit was not checked beyond that run's passing result.
- **Decision** — no-change: below the promotion gate — one incident, caught by the module's suite before any review.

### unify-review-event-history#3 — A batch edit script wrote its pre-edit copy and dropped an earlier edit

- **Recorded** — 2026-11-08, the agent.
- **Stage** — implementation.
- **Expected** — one script applying two edits to a test file lands both.
- **Observed** — the script wrote `p.write_text(t.replace(first, ...))` and then `p.write_text(t.replace(second, ...))`,
  where `t` was still the pre-edit text, so the second write overwrote the first: the fixture's findings directory was
  never created.
- **Evidence** — the next run failed with `FileNotFoundError: …/docs/units/unit-a/findings/round-2.md` in
  `AgentSessionStagedTests.test_the_staged_round_record_decides_the_completeness_rule`.
- **Consequence** — that test errored once; re-applying the dropped edit alone restored it.
- **Knowledge boundary** — observed in this unit's session; other scripts run in the same session were not re-read for
  the same shape.
- **Decision** — no-change: covered by `AGENTS.md`'s edit-verification gotcha (a batching script is a third mode; write
  each edit as it succeeds and verify the batch).
