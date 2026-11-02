# Tasks

Workspace:
`unit=report-unmapped-round-classes; path=/Users/afanasyev/Development/axon-showcase-report-unmapped-round-classes; branch=report-unmapped-round-classes; writer=ses_e653ab5caffeGjp1myeRL03Emh`

## 1. The shared parser

- [x] 1.1 `scripts/review_vocabulary.py` carries `REVIEW_QUICK_DEFINITION`, `CANONICAL_LIST_OPEN`,
      `CANONICAL_LIST_CLOSE`, `CLASS_LABEL_RE`, and `canonical_classes(text)` with its docstring, moved verbatim from
      `scripts/commit-hygiene.py`.
- [x] 1.2 `commit-hygiene.py` imports them from the module and re-exports `canonical_classes`, so its callers and
      `ClassVocabularyTests` are unchanged; its own copies are deleted.
- [x] 1.3 `python3 scripts/test-commit-hygiene.py` still passes, proving the re-export and the import path.

## 2. The digest

- [x] 2.1 `review-rounds.py` imports the parser and reads the definition at `REVIEW_QUICK_DEFINITION`, relative to the
      working directory it already resolves `--record` from.
- [x] 2.2 `canonical_class(label, row_date, canonical)` returns the label only when the canonical list names it; `tally`
      and `render` take the class set, and the `unmapped` line reports every label no normalization yields a class for.
- [x] 2.3 An unreadable definition, or a list that cannot be extracted, keeps the `unmapped` pairs it can state and
      appends `— unestablished: <reason>; post-boundary labels counted as classes`, and exits 0; a pre-boundary label
      with no mapping entry stays unmapped.
- [x] 2.4 The module docstring states the digest's normalization and the fallback.
- [x] 2.5 `python3 scripts/review-rounds.py --since 2026-10-23` reports the three in-window labels under `unmapped` and
      none of them under `classes raised`.

## 3. The controls

- [x] 3.1 A new case: a post-boundary label the list does not name is reported in `unmapped` and absent from
      `classes raised`.
- [x] 3.2 `test_a_post_boundary_label_is_taken_as_recorded` keeps its canonical label and continues to pass: the
      canonical case was never the defect's control.
- [x] 3.3 The pre-canonical normalization and the unmapped pre-canonical label keep their current cases.
- [x] 3.4 A definition with no anchor makes the `unmapped` line unestablished naming the reason, driven through
      `definition_classes`; the other three extraction failures are covered where the parser lives
      (`ClassVocabularyTests`), and the labels are still counted as classes.
- [x] 3.5 A missing definition file, driven through `main`, prints the unestablished line naming the reason and exits 0.
- [x] 3.6 The live definition yields `contradiction` among the classes, so a parser returning nothing cannot pass.
- [x] 3.7 Measured: neutering the membership test fails 3.1 (1 of the 21 digest cases), and removing the unestablished
      branch fails the unreadable-definition case (1); restored, 21 pass.

## 4. The wiring

- [x] 4.1 `testReviewRounds` gains `inputs.file("scripts/review_vocabulary.py")` and
      `inputs.file(".opencode/agent/review-quick.md")`.
- [x] 4.2 `testCommitHygiene` and `verifyReviewClassVocabulary` gain `inputs.file("scripts/review_vocabulary.py")`, and
      `testCommitHygiene` also gains `inputs.file(".opencode/agent/review-quick.md")`, which its live case reads today
      without declaring it.

## 5. The sweep

- [x] 5.1 Remove the idea from `docs/ideas.md` under `## 2026-11-02`, removing the section's heading with its last
      entry.
- [x] 5.2 Docs refresh: grep `AGENTS.md`, `README.md`, `docs/adr/`, and `docs/contracts/` for an enumeration of the
      digest's lines or of its normalization, and update what states one.
- [x] 5.3 Confirm the `## Purpose` refresh is not owed: the agent-skills Purpose names the analyzer, not the digest's
      normalization; record the determination rather than editing the Purpose.
- [x] 5.4 Read every artifact this change creates for a restated procedure — the delta points at the requirement it
      widens rather than restating the ledger's format or the reviewer's list.

## 6. The pre-write pass and the claims

- [x] 6.1 Record the pre-write derivation pass as `design.md`'s claims table, seeded with `scripts/claim-scan.py`, with
      each dismissal stated by kind.
- [x] 6.2 Re-derive the table after the unit's last append to its own record.

## 7. The review

- [x] 7.1 `review-quick` rounds, recorded in `docs/review-rounds.tsv` with their session fields: rounds 1–7, the closing
      one carrying no class.
- [x] 7.2 Deep-review routing: `RECOMMENDED`, signal `repeated-class` (4 rows repeat a prior class), derived with
      `scripts/review-routing.py --unit report-unmapped-round-classes --change report-unmapped-round-classes`; the owner
      chose to run it.
- [x] 7.3 Deep review. Pass 1 (initial, the owner's selection): two nitpicks — the delta body's "rather than reported as
      none" read as an either/or the code does not implement, and the design said the digest is "piped into" the gather
      where the script runs it standalone under `set -euo pipefail` — both `FIXED_AND_VERIFIED` by pass 2. Pass 2
      (independent fix verification): zero findings; the reviewer re-checked both clauses against the code and the
      script, and confirmed the delta's scenario preservation.
- [x] 7.4 The unit's process-defect record (`docs/process-defects/report-unmapped-round-classes.md`), or the statement
      that none was observed.

## 8. Verification

- [x] 8.1 `python3 scripts/test-review-rounds.py` and `python3 scripts/test-commit-hygiene.py` pass, and the digest's
      new cases are the ones that fail when the membership test is neutered.
- [x] 8.2 The Formatting convention's manual check over the changed formatter-unowned files —
      `perl -CSD -lne 'print if length > 120'` over `scripts/review_vocabulary.py`, `scripts/review-rounds.py`,
      `scripts/commit-hygiene.py`, and `scripts/test-review-rounds.py` — returns no line.
- [x] 8.3 `./gradlew spotlessApply` then `./gradlew spotlessCheck`, after the final edit to a formatter-owned file.
- [x] 8.4 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes: the root build and the scripts' suites are
      the touched gates.
- [x] 8.5 `openspec validate --all` passes.
