# Tasks

## 1. The delta

- [x] 1.1 `## MODIFIED Requirements` replaces the guard requirement in full: the current block copied verbatim, the
      ideas-section refusal paragraph inserted before the scenarios, and the four new scenarios appended.
- [x] 1.2 `openspec validate --changes` passes and every retained scenario survives in its recorded order.

## 2. The guard

- [x] 2.1 The build check and the guard share one reporter, so the predicate, the message, and the count have one owner.
- [x] 2.2 `check_staged` judges a staged `docs/ideas.md` through that reporter, reading the index's content.
- [x] 2.3 A staged deletion of the file is refused as a missing ideas file.
- [x] 2.4 The check is skipped when the file is not staged.

## 3. The tests

- [x] 3.1 An emptied staged section refuses the commit and names the file, the line, and the heading.
- [x] 3.2 A staged file whose sections all carry entries passes.
- [x] 3.3 The judgment follows the index, not the working tree: a fixed working tree over an emptied staged copy still
      refuses, and an emptied working tree over a staged copy carrying entries passes.
- [x] 3.4 A staged deletion of the file refuses and names it.

## 4. The documents

- [x] 4.1 The hook header's refusal list names the emptied ideas section and the removed file.
- [x] 4.2 `AGENTS.md`'s staging gotcha and the README's guard paragraph name both too.
- [x] 4.3 The unit's process-defect record.

## 5. The claims

| Claim                                                           | Source                                                                                                                                                     | Result                                                                                                             |
| --------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| The build check and the guard share one predicate and message   | `report_empty_idea_sections` called from `check_idea_sections` and `check_staged_idea_sections`                                                            | one owner; `grep -c report_empty_idea_sections scripts/commit-hygiene.py` → 3 (the definition and both call sites) |
| The guard judges the index, not the working tree                | `test_the_judgment_reads_the_index_and_a_fixed_working_tree_changes_nothing` and `test_an_emptied_working_tree_over_a_staged_copy_carrying_entries_passes` | both pass; the guard follows the staged copy in either direction                                                   |
| A staged deletion refuses                                       | `test_a_staged_deletion_refuses_as_a_missing_file`                                                                                                         | passes, naming `docs/ideas.md`                                                                                     |
| The refusal is wired into the guard                             | removing `check_staged_idea_sections` from `check_staged` and re-running `StagedIdeaSectionTests`                                                          | `FAIL` for the emptied-section and deletion tests; restored, `Ran 6 tests … OK`                                    |
| The build check is unchanged for entries and for a missing file | the existing `IdeaSectionTests`                                                                                                                            | pass                                                                                                               |
| Every enumeration of the guard's refusals names both refusals   | the hook header and `AGENTS.md`: "carries no entry or that the commit removes"; `README.md:884`: "an emptied dated ideas section or a removed ideas file"  | all three name the emptied section and the removal                                                                 |
| The suite passes                                                | `python3 scripts/test-commit-hygiene.py`                                                                                                                   | 253 tests, OK                                                                                                      |
| The delta keeps the requirement's scenarios                     | the main spec's block against the `MODIFIED` block                                                                                                         | all retained, in order, four added                                                                                 |

## 6. Review

- [x] 6.1 Pre-write derivation pass and the claims table: recorded in section 5.
- [x] 6.2 Quick-review loop: rounds 1-3 in `docs/review-rounds.tsv`; round 3 reports no findings.
- [x] 6.3 Deep-review routing: `NOT_NEEDED` — `scripts/review-routing.py` reports no signal for the guard change, the
      delta's requirement block being an edit of an existing block rather than an added or removed one.
