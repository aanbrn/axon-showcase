# Process defects — repair-dashboard-panels-and-descriptions

### repair-dashboard-panels-and-descriptions#1 — A prose replacement that did not match reported success

- **Recorded** — 2026-11-04, by the main agent.
- **Stage** — proposal.
- **Expected** — after round 1's finding 7, the design's decision 5 should state that the ratio's description says the
  value is cumulative since each process started and that a restart resets it.
- **Observed** — the edit script used `str.replace` without an occurrence assertion for that prose block, the text had
  been re-wrapped by the formatter, the replacement matched nothing, and the script printed its success line and wrote
  the file unchanged.
- **Evidence** — after the round that claimed the fix, decision 5 still read "the name and description say what it is",
  and only a later block rewrite that named the four ratios put the cumulative-and-reset sentence in place; the same
  script's table-row edit was assertion-guarded and its failure was visible.
- **Consequence** — the design's decision and the task were inconsistent for two rounds (the task carried the content,
  the decision did not); both are now aligned, and no implementation was affected.
- **Knowledge boundary** — observed: the missing text and the script's success output; not checked: whether other
  unasserted replacements in the same edit chain silently no-opped beyond the ones later found by review.

### repair-dashboard-panels-and-descriptions#2 — A 120-character check that could never report a violation

- **Recorded** — 2026-11-04, by the main agent.
- **Stage** — implementation.
- **Expected** — the review rounds' "no line over 120 in the changed set" result should come from a command that can
  report an over-long line.
- **Observed** — the rounds ran an `awk` filter whose pattern required a literal field the script had already stripped,
  so it matched nothing and printed nothing; the review reported "no line over 120" from it for four rounds while
  thirteen lines of `DashboardJsonRulesTests.kt` ran to 155 characters.
- **Evidence** — a later round's own check found the thirteen lines (123–155 characters); re-running the repository's
  documented `perl -CSD -lne 'print if length > 120'` recipe over the same file lists them, and the `awk` form lists
  none.
- **Consequence** — the fixture lines were split; no other artifact was affected, and the change's own verification now
  uses the documented recipe.
- **Knowledge boundary** — observed: the vacuous command, its empty output, and the thirteen lines; not checked: how
  many earlier units' 120-character claims rested on the same form.
