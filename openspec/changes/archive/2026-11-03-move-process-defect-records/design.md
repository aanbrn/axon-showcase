# Design

## Context

`docs/process-defects/` holds 111 records, one per unit, named `<unit>.md`. Twelve names carry a date inside the unit's
name, and one carries a suffix (`-second`); all of them are directory-safe. The unit's other artifacts — `unit.md`,
`rounds.tsv`, `findings/`, `review-handover.md` — already live under `docs/units/<unit>/` (ADR-0029), so the move
completes that dossier rather than inventing one.

Readers, measured in the working tree: `scripts/review-routing.py` (its record-artifact class),
`scripts/experience-analysis.sh` (its git-log filters), their two test suites, `AGENTS.md`,
`.github/PULL_REQUEST_TEMPLATE.md`, `README.md`, `docs/contracts/process-bindings.md` (the role value and the consumer
trace), ADR-0028, and `openspec/config.yaml`. The `.opencode/` components adopt the binding rather than naming the path.

## Goals / Non-Goals

**Goals.**

- Every record at `docs/units/<unit>/process-defects.md`, byte-identical, with its unit name preserved.
- The same routing and digest behaviour over the new path.
- One physical dossier per unit.

**Non-Goals.**

- Rewriting any record's content, merging records, or renaming a unit.
- A build check over the records' presence or format.
- Moving the retrospectives, load-test records, or decision records.

## Decisions

**1. The file is `docs/units/<unit>/process-defects.md`.**

- Chosen: the directory already names the unit, so the basename carries the artifact's kind rather than repeating the
  unit; a date or suffix inside a unit's name stays in the directory name, where it already is.
- Rejected — `docs/units/<unit>/<unit>.md`: it repeats the unit and collides in reading with `unit.md`.

**2. Migration is a `git mv` per record, verified before anything is retargeted.**

- The move preserves the blob: 108 records are byte-identical (`R100`), and the three records that link out with
  `../../` are the only content changes (`R097` or `R098`). The count and the name set are compared before and after; a
  unit directory is created where the record is its first artifact.
- Three records link out with `../../` — `exempt-merge-commits-from-assignment.md`,
  `clarify-generalization-parked-match.md`, and `gate-manual-review-readiness.md` — and one directory level is added
  beneath them, so those links are retargeted to `../../../` in the same commit. The migration's byte comparison
  therefore expects exactly those three content changes and no other.
- The old directory is removed when empty, in the same commit.

**3. The readers take the new path in their own shape.**

- `review-routing.py`: the record-artifact class gains a pattern for `docs/units/*/process-defects.md`, kept beside the
  Markdown record directories, so a record-only diff still does not raise the floor.
- `experience-analysis.sh`: its git-log filters select **both** locations — the old `docs/process-defects/` for a window
  whose commits predate the move, and `docs/units/*/process-defects.md` for the ones after.
- A record is identified by its unit, so one met under both paths counts once.
- Its body is read at the new path when the index holds it.
- A record the index does not hold keeps the path it was selected under — the original one, for a record deleted before
  the move — so the digest's "absent at `<rev>`: read it at the revision that removed it" line names a path a reader can
  find in history.
- The bindings' `process-defect-records` value names the new path, and its consumer trace is unchanged.

**4. ADR-0028 is amended, not edited.**

- Its **Decision** states the role's value as `docs/process-defects/<unit>.md`; the Decision (bindings cover the
  process-defect records) stands, so a dated `## Amendment` records the location change, with an inline pointer at that
  role bullet and the new value named there.

## Risks / Trade-offs

- A unit whose directory is created by the move now holds only `process-defects.md`; the workspace-record check
  enumerates units by `unit.md`, so such a directory stays outside its subject, as a rounds-only directory does.
- A tool or command outside the repository that names the old path breaks; none is recorded, and the binding is the
  declared route.
- The move touches 111 paths in one commit; the diff is renames, so review reads the name set rather than 111 bodies.
- `docs/ideas.md` cites two moved records as evidence for a parked entry; those pointers are retargeted in the same
  change, while the narrative they support stays as written.

## Migration

1. `git mv docs/process-defects/<name>.md docs/units/<name>/process-defects.md` for every record.
2. Verify: 111 moved — 108 byte-identical `R100` renames and the three link retargets expected above — with no unit name
   lost and the old directory empty.
3. Retarget the readers and the docs in the same commit, then delete the empty directory.

## Claims table

Base revision: `7d70e6d3`.

| #   | Claim                                                                                         | Source                                                                                                                                                      | Result                                                |
| --- | --------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| 1   | The directory holds 111 records at the base                                                   | `git ls-tree -r --name-only 7d70e6d3 -- docs/process-defects \| wc -l`                                                                                      | 111                                                   |
| 2   | Twelve names carry a date, and one a second-run suffix                                        | `git ls-tree -r --name-only 7d70e6d3 -- docs/process-defects \| grep -cE '[0-9]{4}-[0-9]{2}-[0-9]{2}'`                                                      | 12; `generalize-process-defects-2026-11-01-second.md` |
| 3   | Eleven live documents name the directory as a location                                        | `git grep -l 'docs/process-defects' 7d70e6d3 -- ':!openspec/changes' ':!docs/process-defects' ':!docs/decisions' ':!docs/units' ':!docs/ideas.md' \| wc -l` | 11                                                    |
| 4   | Three records link out with `../../` at the base, and `docs/ideas.md` cites two moved records | `git grep -c '\.\./\.\./' 7d70e6d3 -- docs/process-defects \| wc -l`; `rg -n 'docs/process-defects/' docs/ideas.md`                                         | 3 files; two citations in the `## 2026-10-27` entry   |
| 5   | Every unit name is a single path segment, so no name adds a directory                         | `git ls-tree -r --name-only 7d70e6d3 -- docs/process-defects \| sed 's\|docs/process-defects/\|\|; s\|\.md$\|\|' \| grep -c '/'`                            | 0                                                     |
| 6   | No `.opencode/` component names the directory                                                 | `git grep -l 'docs/process-defects' 7d70e6d3 -- .opencode \| wc -l`                                                                                         | 0                                                     |

**Dismissals**, from `python3 scripts/claim-scan.py --base origin/main`, one reason per kind:

- **absolute** — every candidate is a quantifier in prose the change rewrites or reflows without asserting a count or a
  state of the repository, or one in the change's own record.
- **count** — every candidate is a figure a row states and its command reproduces, a numeral the scanner reads from
  prose or punctuation, or a count of this change's own artifact.
- **cross-ref** — every candidate is a path-like string inside code the change edits, a link inside a record it moves
  (three of which it rewrites), or a reference in prose it edits; the scanner matches path-like text rather than a claim
  about an external artifact.
- **path** — every candidate is a path the change creates or moves, or a path named in a line the change edits without
  asserting anything about that path.
