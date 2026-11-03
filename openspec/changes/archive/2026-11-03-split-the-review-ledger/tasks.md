# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is `docs/units/split-the-review-ledger/unit.md`.

## 1. The data split

- [x] 1.1 A one-off script splits the working tree's `docs/review-rounds.tsv` — the base's rows plus this unit's own
      uncommitted rounds — into `docs/units/<unit>/rounds.tsv`: each unit's data rows in their recorded order, every
      comment line that names or belongs to it (acknowledgement, correction, and the session marker), and the marker
      where the unit has rows on both sides of it. The script is not committed.
- [x] 1.2 The split is verified before the global file is deleted: the source's 2,327 rows (the base's 2,326 plus this
      unit's round 1) and the 14 non-header comment lines present exactly once, each unit's rows in order, a marker only
      in a file holding rows on both sides (none does at this split), and every acknowledgement and correction comment
      in its unit's file.
- [x] 1.3 `docs/review-rounds.tsv` is deleted in the same commit that adds the per-unit files, and no unit record
      (`unit.md`) is fabricated for a unit the ledger alone knows.

## 2. The checks

- [x] 2.1 `scripts/commit-hygiene.py` reads the set of records through one helper: the uniqueness and contiguity check,
      `--expect`, `--expect-clean`, the session-continuity check, the class-vocabulary check, the acknowledgement
      comments, and the unit column against the directory name are judged per unit.
- [x] 2.2 The pre-commit guard selects staged `docs/units/*/rounds.tsv` files, judges each staged record's content, and
      compares each record's rows against `HEAD`'s for the same unit, refusing a removal and a deleted or renamed record
      whose rows disappear.
- [x] 2.3 `scripts/test-commit-hygiene.py`'s ledger fixtures build per-unit files, and the controls cover: a duplicate
      row, a gap, an acknowledged gap, a continuity contradiction, a removed row, a duplicated row's repair, an
      unparseable round, a commit that stages no record, a deleted or renamed record, a record whose unit column names
      another unit, a marker-less record whose rows are all on one side, a marker-less record holding rows on both
      sides, which fails, a six-field row dated after the fields, which fails, a gap acknowledgement filed under another
      unit, which exempts nothing, and a deleted record with a non-ASCII unit name.

## 3. The consumers

- [x] 3.1 `scripts/review-rounds.py` reads the set; its window, counts, and class figures match the pre-split
      working-tree file's output (the base's rows plus this unit's own round), and `scripts/test-review-rounds.py`
      builds per-unit fixtures.
- [x] 3.2 `scripts/review-routing.py` reads the set; its floor and signals match the base revision's output, and
      `scripts/test-review-routing.py` builds per-unit fixtures.

## 4. The build and the docs

- [x] 4.1 `build.gradle.kts`'s four ledger input sites — `testReviewRounds`, `verifyUniqueReviewRounds`,
      `verifyReviewSessionContinuity`, and `verifyReviewClassVocabulary` — name `docs/units/`.
- [x] 4.2 The two capability specs' requirements are implemented as the delta states, and each live site is retargeted:
      `docs/contracts/process-bindings.md` (its path, and its owner cell, which must name the spec rather than the
      deleted header), `AGENTS.md` (the ledger path, and the "exactly one record" line qualified for a rounds-only
      directory), `.opencode/skills/review-gate/SKILL.md` (its "read the ledger's own header" pointer, retargeted to the
      record-format requirement), `docs/ideas.md` (its three live mentions beside the parked entry), ADR-0027 (its
      `Revisit when:` coordinates), and a dated `## Amendment` on ADR-0026's Context.
- [x] 4.3 The parked idea is removed from `docs/ideas.md`, and a sweep finds no **live** document still naming the
      global file as the ledger; the dated historical records that cite it stay as recorded.
- [x] 4.4 The three ideas the split's design surfaced are parked under `docs/ideas.md`'s 2026-11-03 section: a unit's
      review hand-off in the unit directory, persisting each round's finding instances, and moving the process-defect
      records.

## 5. The gates

- [x] 5.1 The claims table in `design.md` is re-derived after the last edit, and each row reproduces.
- [x] 5.2 `openspec validate --all` passes, and `./gradlew spotlessApply` runs after the final edit.
- [x] 5.3 The Python suites run under the declared floor, and the touched build's `check` passes.
- [x] 5.4 `docs/process-defects/split-the-review-ledger.md` records what the unit observed.
- [x] 5.5 The review gate runs: the quick pass is clean at round 9; the routing decision is `REQUIRED`
      (`requirement-block-added` on the commit-hygiene delta). Thorough pass 1 found four findings, all
      `FIXED_AND_VERIFIED` at pass 2, which reports nothing remains: `S1`, the guard now reads only the affected units'
      predecessors, once, and passes them to the preservation check; `N1`, a rename's destination is no longer collected
      as a removed unit; `N2`, a gap acknowledgement counts only in the unit's own record; and `N3`, both `DATA_RECORD`
      branches carry controls. The owner's manual pass approves the implementation.
