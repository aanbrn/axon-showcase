# Tasks

Workspace:
`unit=verify-review-class-vocabulary; path=/Users/afanasyev/Development/axon-showcase-verify-review-class-vocabulary; branch=verify-review-class-vocabulary; writer=ses_e653ab5caffeGjp1myeRL03Emh`

## 1. The check

- [x] 1.1 `scripts/commit-hygiene.py` loads `scripts/review-rounds.py` by path (the shape
      `scripts/test-review-rounds.py` uses for the same kebab-case filename) and reads `PRE_CANONICAL_THROUGH` from it;
      a load failure raises `CheckFailure`, naming the path, so it can never read as a clean pass.
- [x] 1.2 `canonical_classes(text)` extracts the classes from `.opencode/agent/review-quick.md`: the backticked tokens
      between `**Give every finding one class**` and `A finding that fits none`, with the anchor required exactly once
      and every token matching `[a-z][a-z-]*`; a missing or repeated anchor and a zero-class span each return a reason
      naming the defect.
- [x] 1.3 `find_unknown_classes(repo)` reports `(line, unit, round, column, label)` for every class in the two class
      columns of a row dated after the boundary that the list does not name and no `# --- Acknowledged class:` comment
      exempts; a row whose date, round, or classes column cannot be read fails, naming the row.
- [x] 1.4 `check_class_vocabulary(repo)` names each offender as
      `docs/review-rounds.tsv:<line> <unit> round <n> <column> "<label>"`, returns the count, `--class-vocabulary`
      dispatches to it, and `MODE_FLAGS` gains the flag.
- [x] 1.5 The module docstring's mode enumeration states `--class-vocabulary` and what it verifies, beside its siblings.

## 2. The wiring

- [x] 2.1 `build.gradle.kts` registers `verifyReviewClassVocabulary` like its siblings, with `inputs.file` on
      `scripts/commit-hygiene.py`, `scripts/review-rounds.py`, `docs/review-rounds.tsv`, and
      `.opencode/agent/review-quick.md`, `outputs.upToDateWhen { false }`, and the mode's `commandLine`.
- [x] 2.2 The `check` block depends on it.

## 3. The controls

- [x] 3.1 A post-boundary row whose classes-raised column carries an unknown label fails, naming the line, unit, round,
      column, and label.
- [x] 3.2 The same class in the classes-that-repeated column fails, naming that column.
- [x] 3.3 Canonical labels and `-` pass.
- [x] 3.4 A pre-boundary row carrying an unknown label passes.
- [x] 3.5 An acknowledged `(unit, round, label)` passes; the same label on another row or round of that unit fails; and
      another label the list does not name, on the acknowledged row itself, fails. Neutering the label half of the
      exemption key makes the last case fail.
- [x] 3.6 A row with an unreadable date, or with no classes column, fails rather than passing.
- [x] 3.7 A definition with no anchor, with two anchors, or with no backticked class fails, naming the source.
- [x] 3.8 The live definition yields a class the corpus records (`contradiction`), so a parser that returns nothing
      cannot pass.
- [x] 3.9 The entry point exits 1 naming the offender on a fixture, and 0 over the live ledger once the acknowledgements
      land.
- [x] 3.10 The controls bite: with the membership predicate neutered, 3.1, 3.2, and 3.9 fail; restored afterwards.
- [x] 3.11 `MODE_TEST_COVERAGE` gains `--class-vocabulary` → `("ClassVocabularyTests", "find_unknown_classes")`, and
      `ModeCoverageTests` passes.
- [x] 3.12 Each unreadable source fails, naming it: a missing `docs/review-rounds.tsv`, a missing
      `.opencode/agent/review-quick.md`, and an unloadable `scripts/review-rounds.py` (loaded from a path the fixture
      makes absent). A missing ledger must not read as zero rows.
- [x] 3.13 A digest script declaring a boundary that is not a written-out calendar date fails, naming the script, so a
      boundary compared against every row cannot skip the whole ledger: the cases are `"banana"`, `"2026-99-99"`, and
      `"20261015"`, each beside a row carrying an unknown class; removing the validation makes all three sub-cases fail.
      A ledger row's own date takes the same calendar check, so `20-10-2026`, `2026-99-99`, and `20261015` fail as
      unreadable there; removing that check (an unreadable row skipped) fails all three of its sub-cases.

## 4. The record's history

- [x] 4.1 The ledger's header states the vocabulary check and the class-acknowledgement comment form beside the gap
      form, naming the check that exempts exactly one `(unit, round, label)` triple.
- [x] 4.2 Four acknowledgement lines name their rows and labels, each stating that the round's report was never
      reconstructed: line 581 `add-extraction-procedure` r7 `marker-placement`; 1203 `bump-dependencies-2026-10-25` r16
      `output-ordering`; 1520 `scope-chart-repository-fetch` r9 `staged-index-stale`; 1799
      `state-the-adr-decision-boundary` r4 `staging`. The line numbers are read back from the ledger at the moment the
      lines are added.
- [x] 4.3 `python3 scripts/commit-hygiene.py --unique-rounds` and `--session-continuity` still pass, and
      `python3 scripts/review-rounds.py` still prints its digest.

## 5. The sweep

- [x] 5.1 Remove the idea `2026-10-17#1` from `docs/ideas.md`, keeping its section's other entry, and park the digest
      follow-up under `## 2026-11-02` with its status tag.
- [x] 5.2 Mark suggestion S2's disposition in each report that carries it — `docs/retrospectives/2026-10-15.md` (its row
      and the paragraph below it), `2026-10-23.md` (its row), and `2026-10-31.md` (its row) — keeping each report's
      window text and the reopening record.
- [ ] 5.3 The capability's `## Purpose` enumerates each build check it owns and a delta cannot carry one: add the
      vocabulary check to it in the archive commit.
- [x] 5.4 Docs refresh: grep `AGENTS.md`, `README.md`, `docs/adr/`, and `scripts/commit-hygiene.py`'s mode enumeration
      for an enumeration of the `check` members, the ledger's class rule, or the checker's modes, and update what states
      one.
- [x] 5.5 Read every artifact this change creates for a restated procedure — the requirement points at the definition
      and the digest script rather than restating the list or the boundary date — and for a second copy of the class
      list.

## 6. The pre-write pass and the claims

- [x] 6.1 Record the pre-write derivation pass as `design.md`'s claims table, seeded with `scripts/claim-scan.py`, with
      each dismissal stated by kind.
- [x] 6.2 Re-derive the table after the unit's last append to its own record.

## 7. The review

- [x] 7.1 `review-quick` rounds, recorded in `docs/review-rounds.tsv` with their session fields: rounds 1–12, the
      closing one carrying no class.
- [x] 7.2 Deep-review routing: `REQUIRED`, signal `requirement-block-added` (the delta spec), derived with
      `scripts/review-routing.py --unit verify-review-class-vocabulary --change verify-review-class-vocabulary`.
- [x] 7.3 Deep review. Pass 1 (initial, `REQUIRED`): one should-fix — the exemption's unit dimension had no
      discriminating fixture, so a key of `(round, label)` would have passed every case — `FIXED_AND_VERIFIED` by a
      `unit-b` round-1 fixture, with the mutation that drops `unit` from the key failing that case. Pass 2 (independent
      fix verification): zero findings; the reviewer reproduced the mutation in a scratch copy and re-ran the suite.
      Pass 3 (targeted fix verification only, not a full pass — the recorded decision rests on the fail-open risk a
      consultation found behind the two passes' green checks): the boundary's non-date validation, zero findings; the
      reviewer reproduced the fail-open with the validation removed and confirmed the four prior boundary paths. Pass 4
      (targeted verification of the calendar completion, after the consultation's second counterexample — a date-shaped
      non-date — outlived pass 3's shape check): zero findings; the reviewer showed `2026-99-99` rejected by the
      calendar parse and `20261015` only by the round-trip check, so both halves are load-bearing, and confirmed the
      normal boundary, the pre/post-boundary split, and the single caller.
- [x] 7.4 The unit's process-defect record (`docs/process-defects/verify-review-class-vocabulary.md`), or the statement
      that none was observed.

## 8. Verification

- [x] 8.1 `python3 scripts/test-commit-hygiene.py`: 276 tests, OK; with the membership predicate neutered 4 of the 23
      `ClassVocabularyTests` cases fail; with the exemption key's label half dropped 1 fails; with its unit dimension
      dropped 1 fails; with the boundary's calendar validation neutered its three sub-cases fail; with the row's date
      check removed its three sub-cases fail.
- [x] 8.2 `./gradlew verifyReviewClassVocabulary` passes on the live tree, and fails with a planted post-boundary label
      even when an acknowledgement comment names another label on that row: planted row `gate-probe` round 1
      `gamma-probe` failed the task naming `docs/review-rounds.tsv:2204`, and the restored ledger passes.
- [x] 8.3 `./gradlew spotlessApply` then `spotlessCheck` clean, after the final edit to a formatter-owned file.
- [x] 8.4 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` finished BUILD SUCCESSFUL (1m28s), with
      `:verifyReviewClassVocabulary` in the graph (`check --dry-run`).
- [x] 8.5 `openspec validate --all` passes: 27 items, 0 failed.
