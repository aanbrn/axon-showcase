# Design

## Context

The ledger's header states the rule this change verifies: classes come from the canonical list the reviewer's contract
carries (`.opencode/agent/review-quick.md`), rows recorded before that list landed keep the labels they were recorded
with, and `scripts/review-rounds.py` maps those historical labels for the digest. The digest's `canonical_class` applies
that mapping only to a row dated on or before its `PRE_CANONICAL_THROUGH` and returns any later label unchanged, so a
non-canonical label on a later row is counted as if it were a class.

Four rows dated after that boundary carry labels the list does not name (claims table row 2). `check` runs two ledger
checks through `scripts/commit-hygiene.py` (`--unique-rounds` and `--session-continuity`), and the pre-commit guard runs
a third over the staged copy; none reads the class vocabulary, so the rule is prose only.

The parked idea (`docs/ideas.md`, 2026-10-17) proposed this check but routed it to a `code-quality` requirement plus an
`agent-skills` delta. That route is stale: `showcase/quality/agent-skills` now assigns the ledger row's format and
verification to `showcase/quality/commit-hygiene`, which is where the requirement lands.

The remedy a failure names is the reviewer contract's own rule: a finding that fits none of the listed classes has the
calling agent add a class to the definition in the same change, so the gate's message can point at one of two actions —
use an existing class, or add the class where the list lives.

## Goals / Non-Goals

**Goals.**

- Every class in a post-boundary ledger row is a class the definition names, or an acknowledged historical label,
  verified by `check`.
- The canonical list and the boundary keep their single owners; the check reads them rather than restating either.

**Non-Goals.**

- Changing the reviewer's rule or its class list.
- Relabelling the four historical rows.
- Correcting the digest's `unmapped` line for post-boundary rows (parked as its own idea).

## Decisions

**1. A new `--class-vocabulary` mode and its own `check` task, not a clause inside `--unique-rounds`.**

- Chosen: a distinct mode, registered in `MODE_FLAGS`, with `verifyReviewClassVocabulary` in `check`.
- Rejected — folding into `--unique-rounds`: that mode's name and its spec requirement carry row uniqueness, contiguity,
  `--expect`, and `--expect-clean`; adding the vocabulary would make one requirement state two subjects, and the
  mode-coverage control keys a test class to a mode.
- Rejected — the pre-commit guard only: the guard is activated per clone and bypassable with `--no-verify`, so a
  violation reaches `main` whenever either holds; `check` is the gate every pull request runs.

**2. The canonical list is parsed from the reviewer's definition, not copied.**

- Chosen: extract the backticked class tokens from the paragraph the definition opens with
  `**Give every finding one class**` and closes before `A finding that fits none`; the anchor must occur exactly once
  and the span must yield at least one class, and either failure is a `CheckFailure` rather than a clean pass.
- Rejected — a `config/` copy of the list: a second copy is the drift surface the single-sourcing rule names, and the
  definition is where the reviewer contract's owner states it.

**3. The boundary is loaded from `scripts/review-rounds.py`, not duplicated.**

- Chosen: load the digest module by path (the shape `scripts/test-review-rounds.py` already uses for the same kebab-case
  filename) and read `PRE_CANONICAL_THROUGH`; a load failure is a `CheckFailure`.
- Rejected — a second literal: a drifted date would silently exempt the rows between the two values.
- Rejected — a `config/` file read by both scripts: it moves the owner out of the digest and changes the digest's tests,
  widening a bounded defect fix.

**4. The four historical rows are acknowledged, not relabelled and not hidden by a later boundary.**

- Chosen: a comment form `# --- Acknowledged class: <unit> round <N> <label> - <reason>`, exempting one label on one
  row, stated in the ledger's header beside the existing gap form.
- Rejected — relabelling: no canonical class is derivable for the four labels (row 4's scan: the archived change dirs
  carry no occurrence of them, and the two `docs/process-defects/` records that describe an incident in the relevant
  round name none), so a replacement — for instance reading the stale index as `missing-task` — would be this change's
  interpretation rather than the class the reviewer recorded.
- Rejected — moving the check's boundary past the four rows: it would hide that the rule was violated and leave the
  header's boundary stating something the check no longer enforces.

**5. Both class columns are verified.**

- Chosen: the classes-raised and the classes-that-repeated column, because the ledger's header ties the repeat column to
  the same canonical list and says that vocabulary is what makes it comparable.
- Rejected — the raised column only: a repeat entry could then name a class the list does not carry, and the header's
  comparability claim would be false for it. The digest's own tally reads the raised column alone, so the repeat column
  is gated for the record's sake, not for a figure it feeds.

**6. The digest's `unmapped` line stays as it is, and the gap is parked.**

- Chosen: park it in `docs/ideas.md` with the condition that reopens it, since the gate makes it unreachable for new
  rows and the acknowledged labels are the only remaining inputs.
- Rejected — widening this change into `scripts/review-rounds.py`: it would give the digest a second consumer of the
  list while the gate is still unproven, and the retrospective's figures are not changed by the line's wording.

## Risks / Trade-offs

- **Extraction from prose is brittle.** The anchor is asserted exactly once, a zero-class extraction fails, and a
  control drives a mutated definition (a second list, a removed anchor) so a parser that silently returns nothing cannot
  pass.
- **A new comment form on a durable record.** The form exempts one `(unit, round, label)` triple; the controls assert
  that the same label on another row fails and that another unknown label on the acknowledged row fails, and the header
  states the form beside the gap form it mirrors.
- **The check reads two files outside the ledger.** Both are declared as task inputs, so a warm build re-runs when the
  definition or the digest changes rather than replaying a stale verdict.
- **Accepted false negatives.** The check verifies membership, not judgement: a label that is canonical but names the
  wrong kind passes, the repeat column's agreement with an earlier round is not checked, and an acknowledgement comment
  is trusted as recorded, so a later row could be acknowledged without its report having been lost. Each is outside a
  vocabulary gate's reach — the first needs the finding's text, the second its history, the third the report the
  repository does not hold — and the acknowledgement's trust is the ledger's gap form's, not a new concession.

## Claims

Base revision for the before-state rows: `git merge-base HEAD origin/main` = `2e48630fe2f113e920130d4ec6baa5f4dc30677f`.

| #   | Claim                                                                                 | Source                                                                                                                                                                                                                                                              | Result                                                                                                                                                                                                                                                                                                                                                                |
| --- | ------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The definition carries the canonical list, and it is the list's only owner            | A python extraction over `.opencode/agent/review-quick.md` at the base: the backticked tokens between `**Give every finding one class**` and `A finding that fits none`, then `git grep -n 'canonical list'` at the base over `.opencode`, `docs`, `openspec/specs` | 20 classes, `claim-unreproducible` first and `concurrency` last; one paragraph, the one opening `**Give every finding one class**`                                                                                                                                                                                                                                    |
| 2   | Four post-boundary rows carry a label the list does not name                          | A python comparison of every class field of every row dated after `2026-10-15` against row 1's set, over `docs/review-rounds.tsv` at the base                                                                                                                       | `add-extraction-procedure` r7 `marker-placement`, `bump-dependencies-2026-10-25` r16 `output-ordering`, `scope-chart-repository-fetch` r9 `staged-index-stale`, `state-the-adr-decision-boundary` r4 `staging`; base lines 581, 1203, 1520, and 1799 in that order                                                                                                    |
| 3   | The boundary's single source is the digest script                                     | `git grep -n 'PRE_CANONICAL_THROUGH ='` at the base over `scripts`                                                                                                                                                                                                  | `scripts/review-rounds.py:28`, `"2026-10-15"`                                                                                                                                                                                                                                                                                                                         |
| 4   | No canonical class is derivable for any of the four labels                            | `git ls-tree` each unit's archive path at the base; `git grep -n '<each label>'` over `openspec/changes/archive` and `docs/process-defects`; and the two recording units' defect records at the base                                                                | `scope-chart-repository-fetch` and `state-the-adr-decision-boundary` have no archived dir; neither existing archive carries its label; no record names a class — `scope-chart-repository-fetch#4` describes staging defects without a class, and `state-the-adr-decision-boundary#1` describes a stale index at round 4, quoting that report only for its `MM` status |
| 5   | `check` already runs the ledger's sibling checks                                      | `git grep -n 'verifyUniqueReviewRounds'` at the base over `build.gradle.kts`                                                                                                                                                                                        | the task registration and `dependsOn("verifyUniqueReviewRounds")`                                                                                                                                                                                                                                                                                                     |
| 6   | A mode added without a test fails the build                                           | `git show <base>:scripts/test-commit-hygiene.py`, the `ModeCoverageTests` class                                                                                                                                                                                     | asserts `MODE_FLAGS` equals `MODE_TEST_COVERAGE`'s keys, and that each named class's source names the mode and its function                                                                                                                                                                                                                                           |
| 7   | The digest normalizes a label only on a pre-boundary row                              | `git show <base>:scripts/review-rounds.py`, `canonical_class`                                                                                                                                                                                                       | returns the mapped class when the row date is at or before the boundary, and the label itself after it                                                                                                                                                                                                                                                                |
| 8   | The retrospective's "three window-proper rows" is window-scoped, not corpus-wide      | `docs/retrospectives/2026-10-31.md`'s header for its window, against row 2's scan                                                                                                                                                                                   | the window starts at `2026-10-23`; `add-extraction-procedure` is dated `2026-10-19`, so the corpus has four while the window has three                                                                                                                                                                                                                                |
| 9   | The ledger row's format and verification belong to `commit-hygiene`                   | `git show <base>:openspec/specs/showcase/quality/agent-skills/spec.md`, the requirement that the quick review loop runs on every unit                                                                                                                               | "the ledger row's format and verification are `showcase/quality/commit-hygiene`'s", so the vocabulary delta belongs to that capability rather than to `agent-skills`                                                                                                                                                                                                  |
| 10  | The gate's bad state is reachable, and its acknowledgement exempts exactly its triple | `python3 scripts/commit-hygiene.py --class-vocabulary`, run before and after the four acknowledgement comments land                                                                                                                                                 | before: exit 1 naming lines 581, 1203, 1520, and 1799 with `marker-placement`, `output-ordering`, `staged-index-stale`, and `staging`; after: exit 0, and `--unique-rounds` and `--session-continuity` still exit 0                                                                                                                                                   |
| 11  | The controls bite                                                                     | The mutations over `scripts/test-commit-hygiene.py`'s `ClassVocabularyTests`                                                                                                                                                                                        | with the membership predicate neutered, 4 of the 23 cases fail; with the label half of the exemption key dropped, 1 fails; with the unit dimension dropped, 1 fails; with the boundary's calendar validation neutered, its three sub-cases fail; with the row's date check removed (an unreadable row skipped), its three sub-cases fail; restored, 23 pass           |
| 12  | `check` runs the new verification                                                     | `./gradlew check --dry-run`, and the Docker-free `check` itself                                                                                                                                                                                                     | `:verifyReviewClassVocabulary SKIPPED` in the dry-run graph; the Docker-free `check` finished BUILD SUCCESSFUL                                                                                                                                                                                                                                                        |

Dismissals, one reason per candidate kind from `python3 scripts/claim-scan.py --base origin/main` over the unit's diff
against that base:

- `[path]` — a path the artifact names as a pointer to the artifact a claim rests on, never a claim about that file's
  content; the row a path supports states the content claim beside it.
- `[absolute]` — a quantifier inside a decision's own rule statement, a Non-Goal's exclusion, or a label count's
  wording; a quantifier that is evidence is stated with the instances its row names.
- `[count]` — resolved into rows 1 through 3, whose sources are the scans the rows record.
- `[cross-ref]` — a merged change's or an issue's identifier, or a slash-joined phrase the scan reads as an
  `owner/repository` shape, cited as a pointer; the claim it supports is stated by the row or sentence carrying it,
  never by the reference.
