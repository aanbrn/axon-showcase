# Process defects — record-stale-origin-main-check

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### record-stale-origin-main-check#1 — A figure written into a record did not reproduce

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the review gate.
- **Stage** — review (the unit's first quick-review round, before its commit).
- **Expected** —
  - A figure written into a durable record is written from a command whose output was read at the value recorded.
  - A figure a reader will re-run reproduces.
- **Observed** — while writing entry #5 of `docs/process-defects/guard-archived-task-boxes.md`, the value `3` was
  written into the entry and attributed to
  `git show b5606da1:scripts/commit-hygiene.py | grep -c find_unticked_archived_tasks` without a direct run of that
  command; the quick review's first round reported that it prints `2` (the definition at line 307 and its single caller
  at line 797), and direct re-runs print `2`.
- **Evidence** —
  - PR #741's body records the round's finding: the merged-commit count was non-reproducible at `3`.
  - `docs/review-rounds.tsv` records that round with the class `claim-unreproducible`.
  - The entry's corrected sentence now reads `2` with the two referents (`grep -n find_unticked_archived_tasks` at
    `b5606da1`).
- **Consequence** — the figure was corrected before the commit, so nothing shipped with `3`. PR #741's body stated the
  incident was "recorded in the same entry's review history"; entry #5 carries no review history, and before this file
  the finding existed only in that pull request's review record and the ledger row. This file is its record and
  supersedes that claim.
- **Knowledge boundary** — observed that the written value did not reproduce under a direct invocation. Did not
  establish what the command printed at that time, nor why the value was attributed to it (a misread, a stray reading
  from a neighbouring command, or execution behaviour), and did not reproduce `3` in any direct invocation.
