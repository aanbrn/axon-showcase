# Design

## Context

See `proposal.md` — Why. The ledger's contract and its consumer are both explicit: `docs/review-rounds.tsv`'s header
says "One row per review round", and `scripts/experience-analysis.sh` sums its compliance-miss column
(`rounds++; miss += $6`), so a duplicate distorts a figure. The incident is recorded in the change that corrected it:
`dedupe-round-14-row-2026-10-16`'s own row, and the round-14 row it deleted. The repository already has the shape to
mirror — `verifyUniqueCronSchedules` (`build.gradle.kts`'s `verifyUniqueCronSchedules`) runs
`commit-hygiene.py --unique-crons`, whose requirement "Workflow cron schedules do not collide" sits beside the one this
change adds — and the archived `record-review-rounds` design deferred this decision as a Non-Goal ("a later change
decides what the numbers should license").

## Goals / Non-Goals

**Goals:**

- Make the ledger's declared invariant mechanical, so a duplicated row fails `check` rather than silently
  double-counting.
- Keep it in the guard script's family, where the other content checks over tracked files live.

**Non-Goals:**

- The pre-commit guard's staged-set path: the ledger is a tracked artifact, not a property of a staged set, so the check
  runs in `check` only — the cron check's own position.
- Widening the spec's test-coverage clause to every build check: that is a separate parked idea, and this change adds
  its tests without claiming the clause covers them.

## Decisions

**A check, not more prose.** The parked entry said so, and the archive's `record-review-rounds` Non-Goal named the
decision as the thing a later change owed: the invariant is mechanical content, and prose cannot fail.

**A `commit-hygiene.py` mode and a `check` `Exec` task, mirroring the cron check.** That is the repository's shape for a
content invariant over tracked files, and it reuses the script's argument dispatch and its test file.

**The positive control is a committed test.** The check's known-bad case is a duplicated pair in a fixture ledger, not
the repository's own file being wrong — which is also why the check needs no `--unique-crons`-style normalization: rows
are tab-separated, and the pair is compared verbatim.

## Risks / Trade-offs

- **A false positive on a legitimate row shape** → the check compares the `(unit, round)` pair only, as tab-separated
  fields; a row missing its round is not a pair, and the ledger's leading `#` comment lines are skipped.
- **The check goes stale as the ledger's shape changes** → its tests pin both directions, but on the ledger's TSV shape:
  a delimiter change leaves every row a non-pair, so the check passes silently — that accepted false-negative surface is
  the shape being part of the check's contract.

## Migration Plan

None: one `check` member reading an existing file, taking effect on the next run.

## Claims table

| Claim                                                                           | Source                                                                                                                                                                                                         |
| ------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The ledger declares one row per round, and its consumer sums a column           | `docs/review-rounds.tsv`'s header; the compliance-miss column `scripts/experience-analysis.sh` sums (`rounds++; miss += $6`)                                                                                   |
| A duplicate happened and was corrected by hand                                  | `dedupe-round-14-row-2026-10-16` (PR #606), whose own row records it and whose diff deletes the duplicate                                                                                                      |
| The repository's shape for such a check is a guard-script mode + a `check` task | `build.gradle.kts`'s `verifyUniqueCronSchedules` registration (`commit-hygiene.py --unique-crons`), and the spec requirement "Workflow cron schedules do not collide"                                          |
| The spec scopes test coverage to the guard's checks, not the build checks       | `showcase/quality/commit-hygiene`'s "The guard's checks are covered by tests run in the build", whose scope is "each defect class the guard detects" — the guard's own checkers, not the build checks          |
| This decision was deferred to "a later change"                                  | the archived `record-review-rounds` design's Non-Goal: "Acting on the measurement (a check, a gate, or a threshold that fails a build) — this records; a later change decides what the numbers should license" |
| The coverage-widening idea is parked                                            | `docs/ideas.md`'s "Widen the `commit-hygiene` test-coverage requirement to every check" entry, which is why this change adds its tests without claiming that clause covers them                                |
| The "a check, not more prose" decision is the parked entry's own proposal       | `docs/ideas.md`'s "Guard the review-round ledger against a duplicate row" entry at the change's base (`git show origin/main:docs/ideas.md`), which this change removes                                         |
