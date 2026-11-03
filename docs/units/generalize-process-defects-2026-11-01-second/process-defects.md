# Process defects — generalize-process-defects-2026-11-01-second

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### generalize-process-defects-2026-11-01-second#1 — The undecided-record scan matched a form the corpus does not use

- **Recorded** — 2026-11-01, by the writing session, at the selection step.
- **Stage** — proposal (step 1 of the pass, listing the records that carry no `Decision` line).
- **Expected** — the selection lists every record without a `Decision` line, per record, so the owner sees the real set
  and the hand-off analyzes only the unanalyzed records.
- **Observed** — the first scan matched only `Decision:` with a colon and reported 189 undecided — every record the
  corpus held at the base revision — while 18 were undecided. The corpus writes the field as
  `- **Decision** — <disposition>: …` with an em dash, so the 171 already-decided records read as undecided. The scan
  was replaced with one accepting the bold lead and any of `: — -` after it, which reported 18.
- **Evidence** — the first scan printed `undecided records: 189`; the replacement printed `decided: 171  undecided: 18`;
  `grep -rn "Decision" docs/process-defects/` shows the em-dash form (`retry-install-path-fetches.md:21`, for example).
- **Consequence** — one discarded scan and one corrected run, both before any hand-off; the set the owner saw and the
  records analyzed are the corrected 18.
- **Knowledge boundary** — observed both scans' output and the corpus's field form. Not checked: whether another record
  field has a second accepted spelling in the corpus, which a single-form pattern would under-match the same way.

### generalize-process-defects-2026-11-01-second#2 — An append guard's "already decided" test matched a quoted literal

- **Recorded** — 2026-11-01, by the writing session, while appending the `Decision` lines.
- **Stage** — implementation (appending the decision lines to the 18 selected records).
- **Expected** — the append writes each record's `Decision` line and refuses a record that already carries one, so a
  record is analyzed once.
- **Observed** — the guard tested `**Decision**` as a substring anywhere in the record block. The
  `generalize-process-defects-2026-11-01` record's `Evidence` quotes the literal "`- **Decision**` line", so the last of
  the 18 records aborted with "already decided" after its 17 siblings had been written; its line was appended by hand.
- **Evidence** — the run printed `ABORT: generalize-process-defects-2026-11-01#1 already decided`, while
  `grep -c '^- \*\*Decision\*\*' docs/process-defects/generalize-process-defects-2026-11-01.md` returned `0`.
- **Consequence** — one manual edit for that record; the other 17 were written as intended, since the script wrote each
  record to disk before moving to the next.
- **Knowledge boundary** — observed the abort message and the grep count; did not test whether the same substring test
  misfires on any other record in the corpus.
