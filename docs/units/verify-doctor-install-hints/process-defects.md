# Process defects — verify-doctor-install-hints

### verify-doctor-install-hints#1 — a claims-table row rebuilt from a naive pipe split, and failed twice

- **Recorded** — 2026-11-03, by the unit's agent.
- **Stage** — implementation.
- **Expected** — a claims-table cell quoting a command that carries a pipe is written with the pipe escaped, which the
  table gate accepts.
- **Observed** — the first row 11 carried the pipe bare, and `verifyTableRows` failed with
  `A markdown table row has 5 cells where its delimiter row has 4`. The fix split the row's line on `|` in a script and
  replaced one cell, then joined the pieces back: the split had already turned the pipe into a cell boundary, so the
  join reproduced a five-cell row and the gate failed again on the same line. A second row (row 5) carried the same
  defect and was reported only after the first was fixed, because the task stops at the first offending line.
- **Evidence** — `./gradlew verifyTableRows` printed
  `A markdown table row has 5 cells where its delimiter row has 4: openspec/changes/verify-doctor-install-hints/design.md:124`
  on both runs; the row's raw `|` count was 6 against the four-column delimiter row. `split_cells` in
  `scripts/commit-hygiene.py` tracks backslashes and splits only on an unescaped pipe, and the README's OpenCode cell —
  which carries an escaped pipe inside a table row — passes the same gate.
- **Consequence** — two authoring attempts at the same cells; both rows were then written without any pipe, describing
  it in words. No other artifact carried the mistake.
- **Knowledge boundary** — verified from the source that the gate honours an escaped pipe and from the README that such
  a row passes; did not check whether the naive-split-and-rejoin pattern appears in other scripts of this repository.

- **Decision** — no-change: the `Formatting` convention already requires escaping a table cell's pipe, and
  `verifyTableRows` now fails the shape.
