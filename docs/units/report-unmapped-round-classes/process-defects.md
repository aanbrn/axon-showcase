# Process defects — report-unmapped-round-classes

### report-unmapped-round-classes#1 — The change dir was staged at the proposal and went stale during implementation

- **Recorded** — 2026-11-02, by the unit's agent.
- **Stage** — implementation.
- **Expected** — the review gate stages the unit's new files before the manual pass, so the index carries the artifacts
  the review screens.
- **Observed** — the change dir was staged for the proposal's manual-pass request, and the implementation then edited
  `design.md` and `tasks.md` plus three scripts, so `git status` reported `AM` for the two change-dir files, an unstaged
  `build.gradle.kts`, and unstaged `scripts/commit-hygiene.py` and `scripts/review-rounds.py`.
- **Evidence** — `git status --short` at the implementation stage:
  `AM openspec/changes/report-unmapped-round-classes/ design.md`, `AM …/tasks.md`, ` M build.gradle.kts`,
  ` M scripts/commit-hygiene.py`, ` M scripts/review-rounds.py`.
- **Consequence** — the stale index would have committed the proposal's copies of the two artifacts and none of the
  implementation; no commit was made, and the readiness step and the re-stage before the manual pass repair it.
- **Knowledge boundary** — observed the stale index and the paths; did not check whether a `git add` of the single
  script paths at the proposal would have avoided it.

- **Decision** — proposed: applied as `review-gate` §2's mid-loop re-stage bullet; the staging-snapshot rule already
  named the stale-index state.
