# Design

## Context

The digest's contract is `showcase/quality/agent-skills`'s experience-analysis requirement: the review-rounds block
states the window's classes normalized to the reviewer's canonical vocabulary, and a label that cannot be normalized is
reported rather than passed through silently. The requirement states that second outcome for a **pre-canonical** label
only, and `scripts/review-rounds.py` implements exactly that: `canonical_class` maps a label on a row dated on or before
`PRE_CANONICAL_THROUGH` and returns any later label unchanged, so the digest counts it as a class.

The gate `verify-review-class-vocabulary` (#813) makes new rows carry only canonical classes and acknowledges four
historical labels in the ledger (`add-extraction-procedure` round 7 `marker-placement`, `bump-dependencies-2026-10-25`
round 16 `output-ordering`, `scope-chart-repository-fetch` round 9 `staged-index-stale`,
`state-the-adr-decision-boundary` round 4 `staging`). Those four are the only non-canonical inputs left, and this digest
counts each as a class while printing `unmapped … none` (claims table row 2).

The list's reader is `canonical_classes` in `scripts/commit-hygiene.py`, added by the same change; the digest reads the
same list, so one of the two must not grow a second parser.

`scripts/experience-analysis.sh` invokes the digest and prints its block into the retrospective gather, so the digest is
a report consumed by a script, not a gate: a non-zero exit would break the gather.

## Goals / Non-Goals

**Goals.**

- Every label no normalization yields a class for is reported in the digest's `unmapped` line instead of counted as a
  class.
- One parser serves the gate and the digest.
- An unreadable list degrades to a stated limitation, never to `none`.

**Non-Goals.**

- Relabelling the four historical rows; the ledger's record stays as acknowledged.
- Gating the ledger from the digest (that is `verifyReviewClassVocabulary`'s job).
- Changing the window split, the counts' arithmetic, or the requirement's other clauses.

## Decisions

**1. The canonical-list parser moves into `scripts/review_vocabulary.py`, imported by both consumers.**

- Chosen: a module owning `REVIEW_QUICK_DEFINITION`, the two list anchors, the label shape, and `canonical_classes`;
  `commit-hygiene.py` imports them and re-exports `canonical_classes`, so its callers and tests are unchanged.
- Rejected — a second parser in the digest: two readers of one list drift, and the gate's exact-once anchor is the
  behaviour the digest needs.
- Rejected — the digest loading `commit-hygiene.py` by path: a report tool would depend on the pre-commit guard's
  module, inheriting its `argparse`, `subprocess`, and ledger semantics for a parser it uses alone.

**2. A label no normalization yields a class for is reported unmapped, not counted as a class.**

- Chosen: `canonical_class` returns `None` for a post-boundary label the list does not name, which the existing `tally`
  already routes to the `unmapped` bucket; a pre-boundary label keeps its mapping, so a mapped label stays a class.
- Rejected — keeping it as a class and adding a separate warning: the `classes raised` figure would still count a label
  the requirement says must be normalized, so the figure the retrospective quotes stays wrong.

**3. An unreadable canonical list prints the unmapped line as unestablished, with its reason, and exits 0.**

- Chosen: the line keeps the pairs it can state and appends
  `— unestablished: <reason>; post-boundary labels counted as classes`, and the block still prints with the exit status
  unchanged, so the report states what it could not do, a pre-boundary label with no mapping entry stays unmapped, and
  the gather keeps running.
- Rejected — a non-zero exit: `scripts/experience-analysis.sh` runs the digest under `set -euo pipefail`, so a failure
  would abort the retrospective gather on a cause the retrospective cannot remediate; a gate for the list already exists
  in the build.
- Rejected — printing `none`: it is the defect this change removes, in another form.

**4. The shared module joins the input lists of the tasks whose verdict it decides.**

- Chosen: `testReviewRounds` gains the module and `.opencode/agent/review-quick.md` (the digest reads both), and
  `testCommitHygiene` and `verifyReviewClassVocabulary` gain the module.
- Rejected — declaring it on every `commit-hygiene.py` task: those tasks always execute (`upToDateWhen { false }`) and
  their verdicts do not read the list, so the entry would be noise rather than a cache key.

**5. The delta is a `MODIFIED` block on the experience-analysis requirement, not an `ADDED` one.**

- Chosen: the outcome — a label that cannot be normalized is reported — already exists; only its scope (pre-canonical)
  and the unreadable-list case are widened, so the block copies the requirement and edits those clauses.
- Rejected — a new requirement: it would restate an outcome the corpus already holds.

**6. No `## Purpose` refresh is owed.**

- The agent-skills Purpose names the experience-analyzer as an analysis subagent and says nothing about which labels the
  digest normalizes, so widening a clause inside its requirement does not falsify the Purpose's own text.

## Risks / Trade-offs

- **The four historical labels change bucket.** A window containing them now reports them under `unmapped` and drops
  them from `classes raised`; that is the correction (the 2026-10-31 retrospective already recorded the line as wrong),
  and the design states it rather than leaving the figure's movement unexplained.
- **A labelled fallback can be missed by a reader.** Accepted: the alternative is a silent `none`, which is the defect.
- **A new module both scripts import by name.** If it is absent, the checker and the digest fail loudly at load, which
  is the honest failure; the build declares it where the verdict depends on it.
- **The parser stays prose-based.** Its extraction failures are already loud in the gate (an anchor that is absent or
  repeated, or a token that is not a class); the digest inherits that loudness as the unestablished line.

## Claims

Base revision for the before-state rows: `git merge-base HEAD origin/main` = `bd8084f71e562e97b78214c48b79334171b1ceb6`.

| #   | Claim                                                                                                           | Source                                                                                                                                                                                                        | Result                                                                                                                                                                                                                   |
| --- | --------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | The digest returns a post-boundary label unchanged                                                              | `git show <base>:scripts/review-rounds.py`, `canonical_class`                                                                                                                                                 | returns `PRE_CANONICAL_MAP.get(label)` at or before the boundary and `label` after it                                                                                                                                    |
| 2   | At the base, the digest counts the three in-window non-canonical labels as classes and prints `unmapped … none` | the base script and ledger copied out (`git show <base>:scripts/review-rounds.py`, `git show <base>:docs/review-rounds.tsv`), run as `python3 review-rounds.py --since 2026-10-23 --record review-rounds.tsv` | `output-ordering 1, staged-index-stale 1, staging 1` inside the classes-raised line, and `unmapped … none`; `marker-placement` (2026-10-19) sits before that window                                                      |
| 3   | The requirement states the reporting outcome for a pre-canonical label only                                     | the experience-analysis requirement, its review-rounds clause                                                                                                                                                 | "and a pre-canonical label that cannot be normalized reported as such rather than passed through silently"                                                                                                               |
| 4   | The canonical list's parser lives in the checker                                                                | `git grep -n 'def canonical_classes'` at the base over `scripts`                                                                                                                                              | `scripts/commit-hygiene.py`, exercised by `ClassVocabularyTests`                                                                                                                                                         |
| 5   | The digest runs inside the retrospective gather under `set -euo pipefail`, so a non-zero exit aborts it         | `grep -n review-rounds scripts/experience-analysis.sh`                                                                                                                                                        | the script runs `python3 …/review-rounds.py --since "${SINCE}"` as a standalone command under `set -euo pipefail` at line 2                                                                                              |
| 6   | The digest's tests run in `check` and declare their inputs                                                      | `git grep -n 'testReviewRounds'` at the base over `build.gradle.kts`                                                                                                                                          | the task registers the script, its tests, the ledger, and the archived mapping design, and `check` depends on it                                                                                                         |
| 7   | The old behaviour has no case naming a non-canonical post-boundary label                                        | `git show <base>:scripts/test-review-rounds.py`, `CandidateClassTests`                                                                                                                                        | `test_a_post_boundary_label_is_taken_as_recorded` uses the canonical `structured-writing`, so it passes unchanged under the new rule and is kept as the canonical case (task 3.2) rather than replaced                   |
| 8   | The four acknowledgements are the only non-canonical inputs the digest miscounts                                | `python3 scripts/commit-hygiene.py --class-vocabulary` on the live tree                                                                                                                                       | exit 0, and `grep -c '^# --- Acknowledged class' docs/review-rounds.tsv` = 4                                                                                                                                             |
| 9   | The 2026-10-31 retrospective recorded the digest's unmapped line as wrong                                       | `git show <base>:docs/retrospectives/2026-10-31.md`, line 17                                                                                                                                                  | "… carry a label the canonical list does not name (`output-ordering`, `staged-index-stale`, `staging`), so the digest's `unmapped: none` line …"                                                                         |
| 10  | No live doc states the digest's normalization                                                                   | `git grep -n 'unmapped'` and `git grep -ni 'normaliz'` at the base over `AGENTS.md`, `README.md`, `docs/adr`, `docs/contracts`                                                                                | no `unmapped` match; the `normaliz` matches are unrelated rules and formatter behaviour, never the digest's                                                                                                              |
| 11  | The digest reports the in-window non-canonical labels as unmapped and none of them as a class                   | `python3 scripts/review-rounds.py --since 2026-10-23`                                                                                                                                                         | `unmapped …: output-ordering 1, staged-index-stale 1, staging 1`; the classes-raised line ends at `proposal-schema 9`, so none of the three appears there                                                                |
| 12  | The shared module resolves for both consumers, whichever route loads them                                       | `python3 scripts/commit-hygiene.py --class-vocabulary` and `python3 scripts/review-rounds.py` (both exit 0), plus both test modules loading their script by path                                              | both exit 0; `python3 scripts/test-commit-hygiene.py` 276 tests OK and `python3 scripts/test-review-rounds.py` 21 OK                                                                                                     |
| 13  | The controls bite                                                                                               | The two mutations over `scripts/review-rounds.py`                                                                                                                                                             | membership neutered → 1 failing case (the non-canonical label); unestablished branch removed → 1 failing case (the unreadable definition); restored → 21 pass                                                            |
| 14  | The touched gates pass                                                                                          | `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `openspec validate --all`, and the manual 120-character recipe                                                                                     | the check BUILD SUCCESSFUL on the refreshed tree (an earlier run failed at `:helm:chart:fetchMainChartDependencies`, a Helm CLI fetch, exit 1, and the next identical run passed); 27 passed, 0 failed; no line over 120 |

Dismissals, one reason per candidate kind from `python3 scripts/claim-scan.py --base origin/main` over the unit's diff
against that base:

- `[path]` — a path the artifact names as a pointer to the artifact a claim rests on, never a claim about that file's
  content; the row a path supports states the content claim beside it.
- `[absolute]` — a quantifier inside a decision's own rule statement, a Non-Goal's exclusion, or a count's wording; a
  quantifier that is evidence is stated with the instances its row names.
- `[count]` — a figure a named row derives (the digest's output at a revision, a test count, a mutation's failing cases,
  a gate's exit status); each such figure is stated with that row's source, never in this list.
- `[cross-ref]` — a merged change's or an issue's identifier, or a slash-joined phrase the scan reads as an
  `owner/repository` shape, cited as a pointer; the claim it supports is stated by the row or sentence carrying it,
  never by the reference.
