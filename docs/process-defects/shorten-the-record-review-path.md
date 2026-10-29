# Process defects — shorten-the-record-review-path

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### shorten-the-record-review-path#1 — A fix was reported as landed without reading the file

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the review gate.
- **Stage** — proposal (the round-2 fix pass).
- **Expected** — a fix is verified by reading the replacement into the file before the hand-over reports it landed.
- **Observed** — the round-2 pass reported the delta's duplicate `## MODIFIED Requirements` heading merged and task 5.2
  added; the round-3 review found both absent from the staged tree, and a direct `grep -n '^## '` then showed two
  headings while `grep -n 5.2` found nothing.
- **Evidence** — the round-3 report's two findings naming the heading at `spec.md:123` and the missing 5.2; the later
  `grep` that showed one heading and 5.2 at `tasks.md:47`; the hand-over text claiming both were applied.
- **Consequence** — two findings stayed live for one round; both were re-applied and verified by reading the files, and
  the loop's round 3 carried them as repeats.
- **Knowledge boundary** — observed this pass in this unit. Did not establish whether the script's write had been lost
  to its own assertion, or whether the printed report simply did not correspond to the write.

### shorten-the-record-review-path#2 — A claims row enumerated instances where the set needed shapes

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the review gate.
- **Stage** — proposal (rounds 2–4).
- **Expected** — a claims row that dismisses `scripts/claim-scan.py`'s candidates covers the candidate kinds it emits,
  by the shape each kind matches.
- **Observed** — row 11 was written four times: first naming three kinds and a "table figure" shape, then naming four
  kinds with counts the unit's own appended rows moved, then naming four kinds with reasons that covered only the sites
  the review had named, and finally stating one shape per kind.
- **Evidence** — the round-2, round-3 and round-4 reports, each quoting the row and the scan's actual output; the final
  row naming `absolute` as a scope word, `count` as a task or step ordinal or a ledger field, `cross-ref` as the copied
  scenario line, and `path` as a named path or command.
- **Consequence** — three rounds spent on one table cell; the later versions were corrected before the commit.
- **Knowledge boundary** — observed this row in this unit. Did not establish whether other units' claims tables carry
  the same instance-instead-of-shape pattern.

### shorten-the-record-review-path#3 — A control's fixture could not produce the status it asserted

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the review gate.
- **Stage** — implementation (the round-6 control work).
- **Expected** — a control that asserts behaviour for a git status shape runs a fixture that makes git emit it.
- **Observed** — the copy control set `diff.renames=copies`, committed a source file, copied it into a record directory
  and asserted `RECOMMENDED`; git emitted an addition for the copied file rather than a copy (the source was
  unmodified), the assertion failed, and the branch was covered by a mocked `_changed_paths` record instead.
- **Evidence** — the failing assertion quoting `ROUTING: NOT_NEEDED` with `[record-only]` where the test expected
  `RECOMMENDED`; the replacement control feeding a `C100` record to `_changed_paths`.
- **Consequence** — one fixture discarded and replaced before the commit; the parser branch is covered, git's own copy
  emission is not.
- **Knowledge boundary** — observed the fixture's behaviour in this session. Did not establish why git emitted an
  addition, nor whether any configuration would make the CLI copy route reachable.
