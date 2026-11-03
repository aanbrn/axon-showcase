# Process defects — persist-review-round-findings

### persist-review-round-findings#1 — the instances check demanded files for rounds recorded before the store

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`).
- **Stage** — proposal and design.
- **Expected** — the boundary exempts every round recorded before the store exists, including the rounds this unit and
  its neighbours record while the change is in flight.
- **Observed** — the plan first set the boundary as "a round recorded on or after the landing date needs a file". Every
  round already recorded on the landing day — this repository's records included — would then be required, and no
  recovery exists for the rounds an earlier session wrote. The technical-advisor consultation raised it and the plan was
  changed to a "through" boundary, the last exempt day, assigned at implementation to the landing date.
- **Evidence** — the design's Decision 2 now names `FINDING_INSTANCES_THROUGH`; the rejected "on or after" form is
  recorded there, and the task list's 1.1 names the constant.
- **Consequence** — the plan changed before any code was written; no store entry or check shipped from the first form.
- **Knowledge boundary** — observed: the first wording and its change, in this session. Assumed: the landing day's own
  rounds are recorded before the merge, which the ledger appends show. Not checked: whether a future unit could record a
  round on the landing day after the merge, which the exemption would still cover.

- **Decision** — no-change: the archived design's Decision 2 already chose the last-exempt-day boundary for this case;
  the advisor rejected a universal first-date rule as unsupported by that evidence.

### persist-review-round-findings#2 — the instances check read a round it could not parse as a failure

- **Recorded** — 2026-11-03, the agent (session `ses_e5fa0fb2dffe0pwcgRMG3WCl2N`).
- **Stage** — implementation.
- **Expected** — the finding-instances check judges dates, rounds, and classes it can read, and leaves an unparseable
  round to the uniqueness check that already reports it.
- **Observed** — the first implementation parsed records through `record_data_rows`, which raises on a non-numeric
  round, so the guard crashed with a traceback when a staged record carried one — a case the test suite's own fixtures
  produce. The parse now skips a row whose round is not a number.
- **Evidence** — the guard's test run reported `ValueError: … non-numeric round 'x'` from `record_data_rows` before the
  change; after it, the suite passes with the same fixtures.
- **Consequence** — one implementation pass was corrected before the gate; no committed state carried the crash.
- **Knowledge boundary** — observed: the traceback and the passing suite after the change. Assumed: the uniqueness check
  is the owner of the parse failure, which its mode already reports. Not checked: whether another caller of
  `record_data_rows` needs the same tolerance.

- **Decision** — no-change: the record reports a local reader mismatch, repaired in the same pass, and labels the
  parse-failure ownership an assumption; one occurrence, caught by its own suite.
