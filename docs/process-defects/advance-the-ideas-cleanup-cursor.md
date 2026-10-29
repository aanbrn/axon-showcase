# Process defects — advance-the-ideas-cleanup-cursor

### advance-the-ideas-cleanup-cursor#1 — A header field named an event but held a position

**Recorded** — 2026-10-30, by the unit's writing session.

**Stage** — review.

**Expected** — a header field's name states what its value is, so a reader cannot take it for something else
(`AGENTS.md`'s doc-claim rule: a claim must match its source and its strength). The record's own reasoning applies that
rule to a state field: its name is a claim about what the file records.

**Observed** — the field was named `Last cleanup:` and held the date of the **section covered**, not the date the
cleanup ran. The owner read `2026-09-01` as when the cleanup happened, saw it two months before the day of the run, and
asked why; neither the quick round nor the deep review flagged the label.

**Evidence** — the header line read
`**Last cleanup:** 2026-09-01 — the next cleanup starts at the oldest section after it.` The owner's reply reported the
value as the cleanup's own date, roughly two months before the run. The field is now
`**Cleanup cursor:** 2026-09-01 — every section dated on or before it is covered.`, with the command's steps 1 and 4
renamed to match and the reset text defined.

**Consequence** — one review round of confusion and a rename in two files; the value itself was correct throughout.

**Knowledge boundary** — observed: the label, the value, the owner's reading, and the rename. Not checked: whether any
other state field in the repository names an event while holding a position.
