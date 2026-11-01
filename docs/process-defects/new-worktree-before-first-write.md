# new-worktree-before-first-write process defects

### new-worktree-before-first-write#1 — A `captured:` marker was appended to a plain bullet

- **Recorded**: 2026-11-01, by the implementing session.
- **Stage**: implementation.
- **Expected**: `AGENTS.md`'s origin marker sits inside the rule block it records, never on a plain bullet; the
  workspace bullet that carries the rule keeps the markers at its end.
- **Observed**: the first edit appended `captured: auto-setup-new-worktree` to the end of the adjacent plain bullet
  ("The primary checkout carries the sequential unit…"), and the new bold-lead bullet carried both markers as well.
- **Evidence**: `./gradlew :check` failed at `:verifyCapturedMarkers` with "Misplaced captured: marker in AGENTS.md:323"
  (the plain bullet's line); removing that one marker returned `verifyCapturedMarkers` and `testCommitHygiene` to green.
- **Consequence**: one gate run stopped, with no artifact damage; the same commit's later runs are green.
- **Knowledge boundary**: observed the failing gate and the fix. Did not check whether the checker would also accept a
  marker on a continuation line of a bold-lead rule, which the rule text does not distinguish.

### new-worktree-before-first-write#2 — A claims-table row was hand-wrapped into six physical rows

- **Recorded**: 2026-11-01, by the implementing session, on the review's finding.
- **Stage**: review.
- **Expected**: a markdown table row is one physical line; the formatter aligns its columns but never re-joins rows.
- **Observed**: the round-5 edit to the "governed behaviors" claims row wrote it as a six-line string, each line closing
  with a `|`, so the table carried six rows; the join applied as its fix produced one line carrying the continuation
  lines' inner pipes, which is the state the gate refused.
- **Evidence**: `./gradlew verifyTableRows` failed with "A markdown table row has 12 cells where its delimiter row has
  2: openspec/changes/new-worktree-before-first-write/design.md:69" for the joined line; rewriting the row as one clean
  line returned it green, while the six-row state emits one-cell offenders instead.
- **Consequence**: one gate run stopped and one review round went to the rejoin; no claim's content changed.
- **Knowledge boundary**: observed the wrap and the gate failure. Did not check whether Prettier would re-join a row
  wrapped without the trailing pipes.
