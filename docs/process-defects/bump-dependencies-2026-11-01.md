# Process defects — `bump-dependencies-2026-11-01`

### bump-dependencies-2026-11-01#1 — The full gate ran before the change's markdown was formatted

**Recorded** — 2026-11-01, by the implementing agent.

**Stage** — implementation.

**Expected** — the `Formatting` convention runs `spotlessApply` after each edit, so a module's gates start from a
formatted tree.

**Observed** — `./gradlew check` ran immediately after the change's `proposal.md`, `design.md` and `tasks.md` were
written; `:spotlessMarkdownCheck` failed on them and the run stopped after 102 tasks. `spotlessApply` was run
afterwards, and the full `check` was re-run.

**Evidence** — `/tmp/full-check.log` reports `> Task :spotlessMarkdownCheck FAILED` and `BUILD FAILED in 1m 4s`; the
second run, after `./gradlew spotlessApply`, is `/tmp/full-check2.log`.

**Consequence** — one discarded gate run (about a minute) and a second full run. Repaired inside the unit by formatting
and re-running the gate before the review.

**Knowledge boundary** — observed the failure and the re-run. Did not inspect what the formatter changed in the three
artifacts, since they were untracked at that point and therefore had no diff to read.

### bump-dependencies-2026-11-01#2 — A gate result was recorded, then falsified by the next edit to the same file

**Recorded** — 2026-11-01, by the implementing agent.

**Stage** — review.

**Expected** — the `Formatting` convention's rule that the tick claiming a gate is the edit that falsifies it: after the
last edit to a formatter-owned file, run `spotlessApply` and `spotlessCheck` again before reporting the gate green.

**Observed** — task 3.5's Done note recorded `spotlessCheck` green, then the task-4.3 edit rewrote `tasks.md`; the next
review round found `:spotlessMarkdownCheck` failing on that file.

**Evidence** — quick-review round 4's first finding (`:spotlessMarkdownCheck` failing on `tasks.md` after the task-4.3
edit). Repaired by running `./gradlew spotlessApply` and then `./gradlew spotlessCheck`, which printed
`BUILD SUCCESSFUL`, after the last edit to a formatter-owned file.

**Consequence** — one extra review round. Repaired inside the unit by re-running the formatter and the check after the
last edit.

**Knowledge boundary** — observed the failure in the reviewer's report and the green re-run. Did not measure the miss's
cost beyond that one round.
