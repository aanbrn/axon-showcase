# Tasks

## 1. The delta

- [x] 1.1 `## REMOVED Requirements` names the old header with a `Reason` and a `Migration`.
- [x] 1.2 `## ADDED Requirements` carries the retitled requirement with its full description, both retained scenarios in
      their existing order, and two new scenarios — a mode absent from the mapping, and one mapped to a class that never
      names it.

## 2. The control the widened requirement needs

- [x] 2.1 `scripts/test-commit-hygiene.py` declares `MODE_TEST_COVERAGE`: per mode, the test class that exercises it and
      the checker function that class calls.
- [x] 2.2 Three controls: the mapping's keys equal the checker's own `MODE_FLAGS`; each named class exists and carries
      at least one test; each named class's own source names the mode and contains its checker function's call form (the
      name followed by an opening parenthesis) — text presence, not proof of execution — so an entry pointed at a class
      that never mentions its mode fails the build.
- [x] 2.3 The controls bite: drop a mode from the mapping; swap two entries' values; and break one call form by a space
      before the parenthesis (the code still runs, the text is gone) — each fails, and the restore is green.

## 3. The records

- [x] 3.1 The implemented `docs/ideas.md` entry is removed.
- [x] 3.2 The unit's process-defect record.
- [x] 3.3 Docs refresh: `README.md`, `AGENTS.md`, and `docs/adr/` state neither the coverage rule nor an exhaustive list
      of the `check` members. The README's illustrative "build-side counterparts" sentence (`README.md:885`) names five
      of them and stays accurate, so nothing needed updating.

## 4. The claims

| Claim                                              | Source                                                                                                                                                                                                           | Result                                                                                                                                                               |
| -------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The mapping names every mode the checker exposes   | `python3 -c "import importlib.util as u; s=u.spec_from_file_location('c','scripts/commit-hygiene.py'); m=u.module_from_spec(s); s.loader.exec_module(m); print(len(m.MODE_FLAGS))"` against `MODE_TEST_COVERAGE` | 12 modes, 12 entries; `test_the_mapping_names_every_mode_the_checker_exposes` asserts equality                                                                       |
| Each named class carries a test                    | `ModeCoverageTests.test_each_named_class_carries_a_test`                                                                                                                                                         | passes for all twelve                                                                                                                                                |
| Each named class names mode and call form          | `ModeCoverageTests.test_each_named_class_names_its_mode_and_its_function_call`, which reads the class's own source (`inspect.getsource`) and asserts the mode's flag and the function's call form both appear    | passes for all twelve; swapping two entries' values fails it, and so does a space inserted before one call's parenthesis                                             |
| The controls bite                                  | drop `--ideas-sections` from the mapping; then swap the `--large-files` and `--ideas-sections` values; then insert a space before `TableRowTests`' call's parenthesis                                            | `FAIL: test_the_mapping_names_every_mode_the_checker_exposes` and `FAIL: test_each_named_class_names_its_mode_and_its_function_call`; restored, `Ran 247 tests … OK` |
| The delta preserves both retained scenarios        | the old requirement's scenarios against the `ADDED` block                                                                                                                                                        | both present, same order, two added                                                                                                                                  |
| The tests run in the standard check                | `build.gradle.kts`'s `check` block                                                                                                                                                                               | `dependsOn("testCommitHygiene")`                                                                                                                                     |
| The parked entry is removed and no neighbour moves | `git diff --stat docs/ideas.md`                                                                                                                                                                                  | 7 deletions, one entry, `Carry findings forward` intact                                                                                                              |

## 5. Review

- [x] 5.1 Pre-write derivation pass and the claims table: recorded in section 4.
- [x] 5.2 Quick-review loop: rounds 1-8 in `docs/review-rounds.tsv`; round 8, run after the deep-review fixes, reports
      no findings.
- [x] 5.3 Deep-review routing: `REQUIRED` — `scripts/review-routing.py` raises `requirement-block-added` and
      `requirement-block-removed` for the delta.
- [x] 5.4 Deep-review pass 1 (initial, `ses_e739484a6ffeAMSd9N1WsuoK9m`): one nitpick — the requirement's verb
      "exercises" and the fourth scenario's title claim more than the control enforces, since its mode leg is a
      substring check that a docstring satisfies. Owner referred the decision to `technical-advisor`
      (`ses_e738baaeeffefhsz3ah7Pu1jKA`), which chose a bounded strengthening plus a precise lexical description.
      Status: **FIXED_AND_VERIFIED** — the control now requires the checker function's call form, and the requirement,
      scenario, task list, and claims table state the lexical predicate; pass 2 confirms the fix and finds only one
      comment line over 120 characters.
- [x] 5.5 Deep-review pass 2 (verification, `ses_e739484a6ffeAMSd9N1WsuoK9m`): the pass-1 nitpick is resolved and no
      execution claim remains; one nitpick — `scripts/test-commit-hygiene.py`'s comment line at 121 characters. Status:
      **FIXED_AND_VERIFIED** — the comment is rewrapped and the 120-character check is clean.
