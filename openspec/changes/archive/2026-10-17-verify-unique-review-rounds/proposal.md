# Proposal

## Why

`docs/review-rounds.tsv` declares one row per review round, and `scripts/experience-analysis.sh` sums its
compliance-miss column (`rounds++; miss += $6`), so a duplicated row double-counts a round in every window figure a
retrospective aggregates. Nothing enforced that: the same round's row written twice — one `printf` run in two calls — is
byte-identical and adjacent, so it is invisible even to a line count, since the file grows legitimately. It happened, on
the auditor-widening unit's round 14, and was corrected by hand. The archived `record-review-rounds` design deferred
exactly this ("a later change decides what the numbers should license"), so this change is the decision: a check, not
more prose, because the invariant is content and cheap to test.

## What Changes

- **`scripts/commit-hygiene.py`** — a `--unique-rounds` mode (with its `check_unique_review_rounds`), asserting at most
  one row per `(unit, round)` in `docs/review-rounds.tsv` and naming the unit, round and both lines when it is not.
- **`scripts/test-commit-hygiene.py`** — a case per direction: a duplicated pair is reported, a unique ledger is not.
- **`build.gradle.kts`** — a `verifyUniqueReviewRounds` `Exec` task and its `check` dependency, mirroring
  `verifyUniqueCronSchedules`.
- **`showcase/quality/commit-hygiene`** — an `ADDED` requirement stating the invariant and its two scenarios.
- **`README.md`** — the build-side prose enumerating the check set gains the new task.
- **`AGENTS.md`** — the `check` members list gains the task, since it enumerates the commit-hygiene tasks.
- **`docs/ideas.md`** — the entry this change implements is removed.
- **`docs/review-rounds.tsv`** — this unit's own per-round record rows.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: a new requirement — the review-round ledger carries at most one row per unit and
  round, verified in `check`.

## Impact

- **Build / tests / services**: one new `check` member reading a tracked file; no code, runtime or deployment change.
  The review-round ledger gains this unit's rows.
- A duplicated ledger row now fails `check` instead of silently distorting a retrospective figure.
