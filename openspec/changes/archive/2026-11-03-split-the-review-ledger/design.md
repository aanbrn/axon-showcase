# Design

## Context

The review-round ledger is one append-only file. At the base revision it holds 2,366 lines for 256 units, with a header
that states its field contract, one comment marking where the reviewer-session fields begin, and seven acknowledgement
comments.

Its readers, all measured at the base revision:

- `scripts/commit-hygiene.py`: the uniqueness and contiguity check (`--unique-rounds`, with `--expect` and
  `--expect-clean`), the session-continuity check, the class-vocabulary check, and the pre-commit ledger preservation.
- `scripts/review-rounds.py`: the retrospective digest, whose default record is the file.
- `scripts/review-routing.py`: the deep-review floor, whose default ledger is the file.
- Fourteen live surfaces name it — the two capability specs, the bindings, two ADRs, `AGENTS.md`, `docs/ideas.md`,
  `build.gradle.kts`, and six scripts — and thirty-four dated records cite it as history, which stays as recorded.

The file is the only unit-scoped artifact that is not already per unit: the workspace record, the pause state, and the
process-defect record each have a per-unit home (`docs/units/<unit>/unit.md` from ADR-0029; `docs/process-defects/`).
The ledger is also the one shared append target the worktree workflow creates, since every unit's rounds are appended to
the same file.

## Goals / Non-Goals

**Goals.**

- One record per unit at `docs/units/<unit>/rounds.tsv`, holding exactly that unit's rows, in their recorded order.
- Every value preserved: no row rewritten, no round renumbered, no label or acknowledgement changed.
- The same checks, over the set of records, with the same outcomes; the guard judges a staged record.
- The aggregates and the routing results unchanged.

**Non-Goals.**

- Moving the process-defect records: they are already per-unit, and the move would buy locality only.
- A universal per-unit dossier, mandatory empty files, or a new auditor.
- Keeping `docs/review-rounds.tsv` beside the new files: one mutable source only.
- Storing the round finding instances, which no repository file holds today.

## Decisions

**1. One `rounds.tsv` per unit, rows verbatim.**

- Chosen: the file holds the unit's data rows exactly as recorded, the unit column included; the check asserts the
  column agrees with the directory name, so a file filed under the wrong unit fails rather than passing uniqueness and
  contiguity on its own rows. The delta states that assertion as its own scenario, "A row filed under another unit's
  directory fails the check".
- Rejected — dropping the unit column: it makes every historical row incompatible with the parser that reads it, for a
  column the directory already carries.
- Rejected — a per-unit file with a normative header: the contract belongs in the spec, not in 256 copies.

**2. Comments and the session boundary travel with the unit.**

- Chosen: an acknowledgement comment naming a unit moves into that unit's file; where the file has rows on both sides of
  the global session-fields boundary, the marker comment is inserted at that position, so the rows above it keep their
  exemption and the rows below it are judged.
- Chosen: a file with all rows on one side carries no marker where it would separate nothing; the reader treats a file
  without a marker as having all rows on the side its own rows show, so the marker is required only where a file holds
  rows on both sides.
- Chosen: the pre-fields width is exempt only for a row dated on or before 2026-10-18, the last day the fields were
  absent. Without that bound, writing the old width today would claim the historical exemption silently, and the
  session-continuity check would not run for a new unit's record at all.
- Rejected — a global boundary constant in the checker alone: it would silently reclassify a history the file records.

**3. The format is stated once.**

- Chosen: the field contract, the acknowledgement forms, and the boundary's meaning live in the `commit-hygiene`
  requirement "The review-round record format is stated once", which the delta adds; the files carry data and the marker
  only.
- Chosen: that requirement owns the format, so the `review-ledger` binding's owner cell names it rather than the retired
  header — the header was the format's only statement, and deleting it without a named successor would leave the
  binding's owner resolving to nothing.
- Rejected — a `docs/contracts/` copy: the capability spec already owns the checks that read the format.

**4. Readers take the set of records, sorted by unit.**

- `commit-hygiene` gains one helper that returns each unit's record text, and its checks run over that set; a row from
  any file is judged by the same rules as before, and a row whose unit column differs from its directory's name fails.
- `review-rounds.py` and `review-routing.py` read the set; their filtering, aggregation, and floor keep their meaning,
  and a unit's rows keep their order within its file.
- Rejected — a generated aggregate file: a second mutable copy, and the one thing this change exists to remove.

**5. Preservation is compared per unit.**

- The guard refuses a staged record whose rows the commit removes without a surviving `(unit, round)` key in the same
  unit's staged file, and refuses a staged deletion or rename of a record whose rows disappear.
- Rejected — comparing the whole concatenation only: a row moved between units would pass.

**6. Migration is a one-off, verified operation.**

- A temporary script splits the file; it is not committed. Its result is verified by: the row count, each unit's row
  order, the marker's position, the acknowledgement comments' placement, and the three consumers' output before and
  after.
- A unit that only the ledger knows gets `docs/units/<unit>/rounds.tsv` and no `unit.md`; no `Workspace:` line, state,
  or writer is fabricated.
- Rejected — a permanent migration mode: the split runs once.

**7. The unit record and the round record are siblings, not one artifact.**

- `unit.md` stays the authority for workspace, state, and pause; `rounds.tsv` is the review history. The resolver reads
  `unit.md` only.
- A rounds-only directory is not a unit record: the workspace-record requirement enumerates the units that carry a
  `unit.md`, so a directory holding `rounds.tsv` alone is outside that requirement's subject and owes no delta.
  `AGENTS.md`'s "every unit has exactly one record" line is qualified for such a directory by the docs task.

## Risks / Trade-offs

- `docs/units/` grows by one directory per reviewed unit; a directory for a unit without a record carries only
  `rounds.tsv`, and the resolver ignores it while the aggregates read only the files they need.
- The global line order is lost; nothing consumes it — the digest windows by date and the routing floor reads a unit's
  own rounds.
- A unit renamed in the ledger would need its file renamed; the split records the name as the file's directory, and a
  future rename is a rename, not a rewrite.

## Migration

The split is executed once, from the working tree's file — the base's rows plus this unit's own round — and verified
before the change is reviewed: every row of the source present exactly once (the base's 2,326 plus this unit's own
rounds recorded at the time), each unit's rows in their recorded order, the seven acknowledgements and the one
correction comment in their units, and no record carrying a marker, because at the split no unit holds rows on both
sides. The digest's row set and its window, count, and class figures are identical to the source's, and the routing
signal is identical for every unit; only the digest's row order changes, following the records' unit order. The global
file is deleted in the same commit that adds the per-unit files.

## Claims table

Base revision: `952934b5`.

| #   | Claim                                                                                                             | Source                                                                                                                                                                                                                                                        | Result                                                   |
| --- | ----------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------- |
| 1   | The ledger holds 2,366 lines over 256 units                                                                       | `git show $B:docs/review-rounds.tsv \| wc -l`; `… \| rg -v "^#" \| cut -f2 \| sort -u \| wc -l`                                                                                                                                                               | 2,366 lines; 256 units                                   |
| 2   | The session-fields boundary is a two-line comment block                                                           | `git show $B:docs/review-rounds.tsv \| rg -n "Session continuity\|replacement's reason"`                                                                                                                                                                      | lines 492–493                                            |
| 3   | Seven acknowledgement comments exist                                                                              | `git show $B:docs/review-rounds.tsv \| rg -c "^# --- Acknowledged"`                                                                                                                                                                                           | 7                                                        |
| 4   | Every unit name is filesystem-safe                                                                                | `git show $B:docs/review-rounds.tsv \| rg -v "^#" \| cut -f2 \| sort -u \| rg -c "[^A-Za-z0-9._-]"`                                                                                                                                                           | 0                                                        |
| 5   | 48 files name the ledger path: 14 live and 34 dated                                                               | `git grep -l "review-rounds\.tsv" $B -- ":!openspec/changes/archive" \| wc -l`                                                                                                                                                                                | 48; 14 live, 34 dated                                    |
| 6   | The digest and routing defaults name the ledger                                                                   | `git grep -n "RECORD_DEFAULT = \|LEDGER_DEFAULT = " $B -- scripts`                                                                                                                                                                                            | one default each, both the ledger path                   |
| 7   | The parked idea records this move                                                                                 | `git show $B:docs/ideas.md \| rg -n "Move the review-round ledger"`                                                                                                                                                                                           | present                                                  |
| 8   | The three ledger checks are separate modes                                                                        | `rg -n '"--unique-rounds"\|"--session-continuity"\|--class-vocabulary' scripts/commit-hygiene.py`                                                                                                                                                             | three modes registered, plus the guard's staged judgment |
| 9   | Every data row from the source file is present exactly once: 2,326 outside this unit, plus this unit's own rounds | `cat docs/units/*/rounds.tsv \| awk -F'\t' '$2 != "split-the-review-ledger"' \| grep -vc '^#'`; `ls -d docs/units/*/rounds.tsv \| wc -l`; `cat docs/units/*/rounds.tsv \| grep -c '^#'`; `cat docs/units/*/rounds.tsv \| grep -c '^# --- Session continuity'` | 2,326; 257; 14; 0                                        |
| 10  | Each record's data rows equal the source's rows for that unit, in order                                           | `for f in docs/units/*/rounds.tsv; do u=$(basename "$(dirname "$f")"); diff -q <(grep -v '^#' "$f") <(git show $B:docs/review-rounds.tsv \| awk -F'\t' -v u="$u" '$2==u'); done`                                                                              | 256 units identical; the 257th is this unit's own record |

**Dismissals**, from `python3 scripts/claim-scan.py --base origin/main`, one reason per kind:

- **absolute** — each candidate either states the repository fact a row reproduces or quantifies this change's own
  rules; no candidate stands as a repository claim without a row behind it.
- **count** — each is a figure a row states and its command reproduces, or a count of the artifact this change creates.
- **cross-ref** — the candidate is a phrase of this change's own rules the scanner reads as a reference.
- **path** — a cited source is reproduced where a row cites it with a result; a planned path names a file this change
  creates.
