# Design

This design accompanies `proposal.md`: the pre-commit guard must run the ledger's own checks on the ledger the commit
carries, and must refuse a ledger that loses a row.

## Context

- The pre-commit hook runs `commit-hygiene.py --staged` (`scripts/git-hooks/pre-commit`); `check_staged` runs the
  assignment, formatter, generated-artifact, stale-index, conflict-marker, marker and archive-sync checks, and none of
  the ledger's.
- The ledger's verification exists as `--unique-rounds` and `--session-continuity`; the `check` task runs both
  (`verifyUniqueReviewRounds`, `verifyReviewSessionContinuity`), and the checks read the tracked
  `docs/review-rounds.tsv`.
- The ledger's append-only property is a convention: the 2026-10-24 idea states it, no requirement does. Contiguity
  catches a removed middle row as a gap, but not a removed highest row, not a unit's whole set, and not a ledger deleted
  or renamed away.
- The incidents the idea records (#670 and `projector-operability`) are CI-time discoveries; the guard moves the same
  checks to the commit. The ledger's acknowledged gaps for `projector-operability` are a state the main spec lets pass
  and are not what this guard changes.
- `staged_file_text(repo, path)` already reads a path's staged blob (`git show :<path>`), and `check_staged` uses it for
  the marker scan; `find_staged_then_edited` does not cover a type change such as a symlink replacing the file.

## Goals / Non-Goals

### Goals

- A staged ledger that fails the duplicate, contiguity or continuity checks is refused at commit time, reading the
  staged blob rather than the working tree.
- A staged ledger that loses a row fails, while a duplicated row's repair passes.
- An unparseable ledger row is a named failure, not a traceback.
- A commit that does not stage the ledger runs no ledger check.

### Non-Goals

- No change to the `check` task's two verifications, to the caller-driven `--expect`/`--expect-clean` modes, or to the
  ledger's format.

## Decisions

### D1 — Reuse the checks, not a second implementation

- What: `check_staged` calls the same finders the two modes call, and counts their failures with its own.
- Why: one implementation keeps the commit-time and build-time verdicts identical.

### D2 — The ledger checks run only when the ledger is staged

- What: gate the calls on a ledger-scoped query — the ledger in the staged paths, or its deletion or rename in
  `git diff --cached --name-status --diff-filter=DR -- docs/review-rounds.tsv` — and treat an absent staged blob as no
  rows remaining. The guard's change-path helpers are scoped to `openspec/changes/` and are not reused.
- Why: an unrelated commit must not fail on an uncommitted ledger edit, while deleting the file would otherwise bypass
  the check.

### D3 — The staged blob is the ledger the checks read

- What: thread an optional ledger text through the ledger finders, and pass `staged_file_text(repo, LEDGER)` from
  `check_staged`, with an absent blob read as empty text; the modes keep reading the file.
- Why: the stale-index check misses a type change, and the commit's content is what the guard judges.

### D4 — Append-only allows a duplicate's repair

- What: a staged removal of a data row fails unless that `(unit, round)` remains in the staged ledger.
- Why: the rule's purpose is that no row's history disappears; removing a duplicated row (the #606 repair) loses none,
  while removing a unit's only row does.

### D5 — An unparseable row is a named failure

- What: wrap the ledger checks in `check_staged` so a `ValueError` becomes a guard reason naming the file and the
  failure, not a traceback.
- Why: the guard's contract is to name each offending path and reason; the modes already report this failure.

## Claims table

| #   | Claim                                                             | Source                                                                                                                                        | Reproduced                                                                                                                                                                              |
| --- | ----------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The hook runs only `--staged`                                     | `git show 8f99989a:scripts/git-hooks/pre-commit \| tail -1`                                                                                   | `exec python3 "$root/scripts/commit-hygiene.py" --staged`                                                                                                                               |
| 2   | `check_staged` runs no ledger check                               | `git show 8f99989a:scripts/commit-hygiene.py \| sed -n '678,770p' \| grep -c "review_rounds\|session_continuity"`                             | `0`                                                                                                                                                                                     |
| 3   | The ledger checks exist; one name is shadowed                     | `git show 8f99989a:scripts/commit-hygiene.py \| grep -n "def check_unique_review_rounds\|def check_session_continuity\|def ledger_data_rows"` | four lines: `check_unique_review_rounds` at 893 and 1001 (the later is live), `ledger_data_rows` at 905, `check_session_continuity` at 1140                                             |
| 4   | No requirement states append-only                                 | `git grep -n "append-only" 8f99989a -- openspec/specs`                                                                                        | no hits (exit 1); the idea states it as a convention                                                                                                                                    |
| 5   | The idea and its incident record                                  | `git show 8f99989a:docs/ideas.md \| grep -n -A6 "Add a pre-commit ledger guard"`                                                              | line 344: the entry cites #670 and `projector-operability` and the append-only gap                                                                                                      |
| 6   | A removed data row has a `-` line, and a duplicate removal exists | `git show 7a7c8714 -- docs/review-rounds.tsv \| grep -E "^-2026"`                                                                             | the removed round-14 row of `widen-architecture-auditor-to-stale-ideas`, a duplicate repair                                                                                             |
| 7   | The staged blob reader exists                                     | `git show 8f99989a:scripts/commit-hygiene.py \| sed -n '116,122p'`                                                                            | `staged_file_text` reads `git show :<path>`                                                                                                                                             |
| 8   | `check` runs both verifications                                   | `git show 8f99989a:build.gradle.kts \| sed -n '700,710p'`                                                                                     | `dependsOn("verifyUniqueReviewRounds")` and `dependsOn("verifyReviewSessionContinuity")`                                                                                                |
| 9   | The staged path does not catch a parse failure                    | `git show 8f99989a:scripts/commit-hygiene.py \| sed -n '917,922p'`; `:1283`                                                                   | `ledger_data_rows` raises `ValueError`; `main` catches it only for the mode, not the staged path                                                                                        |
| 10  | Contiguity misses a highest-row removal                           | `git show 8f99989a:scripts/commit-hygiene.py \| sed -n '948,956p'`                                                                            | `find_round_gaps` scans `range(1, max+1)`, so a removed highest row leaves no gap                                                                                                       |
| 11  | The stale-index check excludes a type change                      | `git show 8f99989a:scripts/commit-hygiene.py \| sed -n '125,146p'`                                                                            | the index statuses it matches are `M/A/R/C` and the worktree ones `M/A/D/R/C`; `T` is not matched, so a symlink replacing the ledger is not caught                                      |
| 12  | The guard's composition is scoped to change paths                 | `git show 8f99989a:scripts/commit-hygiene.py \| sed -n '571,591p'`                                                                            | `deleted_change_paths` and `rename_sources` pass `-- openspec/changes/` (`CHANGES_PREFIX`) and `staged_change_paths` filters `startswith`, so the composition never contains the ledger |
| 13  | The ledger-scoped deletion query is expressible                   | `git diff --cached --name-status --diff-filter=DR -- docs/review-rounds.tsv`                                                                  | empty on a clean index; a staged `git rm` of the path prints `D\tdocs/review-rounds.tsv`                                                                                                |

## Risks

- Threading a text through the ledger finders touches call sites; the modes' behaviour is asserted by their existing
  tests, and the staged path by the new controls.
- The ledger check adds a small Python pass per commit that stages the ledger.

## Non-Goals

- No new reader for the other checks, no change to the guard's other reasons, and no new ledger column.
