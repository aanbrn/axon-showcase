# Process defects — seed-and-home-a-recordless-units-claims

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### seed-and-home-a-recordless-units-claims#1 — A premise was reworded three times instead of read off its source

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the review gate.
- **Stage** — proposal (rounds 1–3).
- **Expected** — a claim about what a tool scans is read from the tool's own scan, then written once, with other sites
  pointing at it.
- **Observed** — the proposal's Why and the design's Context said a "record-only" diff gives the seed nothing; round 1
  showed an untracked record is scanned, the fix said "deletion-only" at two sites and left two others, round 2 found
  the remaining pair asserting the same false cause, and round 3 found the two corrected sites in the same file as the
  Context's accurate statement — four restatements of one fact.
- **Evidence** — the three rounds' findings quoting the sentences and `scripts/claim-scan.py`'s scan; the final Context
  sentence, which states the scan's two input sets and conditions the empty case.
- **Consequence** — three proposal rounds on one sentence; the final version points at the Context instead of restating
  it.
- **Knowledge boundary** — observed this premise in this unit. Did not check whether other units' motivation sections
  restate a tool's behaviour instead of reading it.

### seed-and-home-a-recordless-units-claims#2 — A section rebuild removed a step and moved an inserted bullet to the wrong section

- **Recorded** — 2026-10-29, by the writing session, during the review gate.
- **Stage** — implementation (the round-5 fix to the gate's step 2).
- **Expected** — an edit that repairs a step's structure leaves every other step as it was and places the inserted text
  in the step it belongs to.
- **Observed** — the repair rebuilt step 2 from `HEAD` up to the first `3. Fix everything it finds` line, which is the
  quick-review section's step 3, not the pre-write step's neighbour; the inserted bullet ended at the end of the
  quick-review section's step 2, and the file grew by six lines without the reviewer's word-level diff agreeing that
  only the intended text had changed.
- **Evidence** — round 6's finding quoting the bullet at `SKILL.md:127-130` inside §2 step 2 while §1 step 2 referred to
  a "saved body" none of its text created; the later move that put it at the end of §1 step 2 (`:83-86`), verified by
  reading both sections.
- **Consequence** — one round spent on the misplaced bullet; the file's structure was restored and a word-level diff
  against `HEAD` then showed exactly three intended changes.
- **Knowledge boundary** — observed this edit in this unit. Did not establish whether the rebuild also altered any
  whitespace the word-level diff normalizes away.

### seed-and-home-a-recordless-units-claims#3 — A ledger row was appended after its successor

- **Recorded** — 2026-10-29, by the writing session, during the review gate.
- **Stage** — review (the implementation rounds).
- **Expected** — each round's row is appended when its report arrives, so the rows stay contiguous and ordered.
- **Observed** — the round-4 report was left without a row, and round 5's row was appended after it, so the unit's rows
  read 1, 2, 3, 5 and the contiguity check failed with "missing round 4".
- **Evidence** — `python3 scripts/commit-hygiene.py --unique-rounds --expect seed-and-home-a-recordless-units-claims:5`
  printing "Unit 'seed-and-home-a-recordless-units-claims' is missing round 4"; the inserted row 4 before row 5, after
  which both `--unique-rounds` and `--session-continuity` pass.
- **Consequence** — one failed check and one inserted row; nothing shipped with the gap.
- **Knowledge boundary** — observed this sequence in this unit. Did not check whether earlier units' rows were appended
  out of order and later reordered.
