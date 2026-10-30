# Process defects — verify-empty-ideas-sections

### verify-empty-ideas-sections#1 — A new mode was not added to the script's own mode list

**Recorded** — 2026-10-30, by the unit's writing session.

**Stage** — implementation.

**Expected** — `scripts/commit-hygiene.py` declares its modes twice: the parser's mutually exclusive group, and the
`MODE_FLAGS` tuple a test reads to derive its cases. A new mode joins both.

**Observed** — `--ideas-sections` was added to the parser and dispatched, and `MODE_FLAGS` was left unchanged. The
suite's first run failed on `test_expect_without_the_mode_is_an_error_for_every_mode`, whose assertion compares the
tuple with the parser's group.

**Evidence** — `python3 scripts/test-commit-hygiene.py` printed
`FAIL: test_expect_without_the_mode_is_an_error_for_every_mode` and `FAILED (failures=1)`; after adding
`"--ideas-sections"` to `MODE_FLAGS` the same command printed `Ran 244 tests` and `OK`.

**Consequence** — one test run and one edit; the guard's own test caught the omission before the review round.

**Knowledge boundary** — observed: the failing assertion, the tuple, and the passing rerun. Not checked: whether the
prompt or the script's docstring states that a mode joins both places, which would have been the rule to read first.

- **Decision** — no-change: the commit-hygiene suite already asserts `MODE_FLAGS` against the parser's group.

### verify-empty-ideas-sections#2 — A padded table row defeated a scripted replace, and the hand-over reported it as done

**Recorded** — 2026-10-30, by the unit's writing session.

**Stage** — review.

**Expected** — a scripted edit of a markdown table row either lands and is verified by reading the row back, or fails
loudly; the round hand-over then describes the tree.

**Observed** — the claim row that mapped the delta's scenarios was rewritten with `str.replace` against an unpadded
rendering of the row. The formatter pads table cells, so the anchor matched nothing, the replace was a no-op, and the
script printed a success line covering three edits. The round-2 hand-over then told the reviewer the row had been
restated.

**Evidence** — the reviewer reproduced the stale text at `openspec/changes/verify-empty-ideas-sections/tasks.md:52`
("the four scenarios map to the four cases"); the row now reads the corrected mapping after a line-indexed edit and a
`spotlessApply` re-pad.

**Consequence** — one review round reported an unfixed instance; the row is corrected in the same unit.

**Knowledge boundary** — observed: the no-op replace, the stale row, and the correction. Not checked: whether the same
script's other two edits in that run landed (the task 5.2 and 4.2 insertions were made in a later, line-indexed pass).

- **Decision** — no-change: the programmatic-edit rule already requires a padded row keyed by its text and the result
  read back; the hand-over's claim is the 'Never chain an edit to a commit' rule's.
