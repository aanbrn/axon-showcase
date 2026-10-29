# Design

This design accompanies `proposal.md`: a markdown table whose rows disagree with its delimiter row must fail the
standard `check`, because Prettier accepts both a wrapped row and a split cell as its fixpoint.

## Context

- `scripts/commit-hygiene.py` already carries build-side modes beside the pre-commit guard: `--conflict-markers`,
  `--executable-bits`, `--large-files`, and the ledger verifications, each registered as a `verify*` task in
  `build.gradle.kts` and depended on by `check`.
- The Spotless markdown target owns the gated markdown: `docs/`, `AGENTS.md`, `README.md`, `openspec/specs/`, the active
  `openspec/changes/*/`, `SECURITY.md`, the `.github/` markdown, and the project-authored `.opencode/` markdown, with
  `openspec/changes/archive/**`, the generated `opsx-*`/`openspec-*` files, and the vendored skill trees excluded.
- Measured with the build's Prettier 3.9.10 under this repository's config (`--prose-wrap always`): a wrapped row is
  kept as a second row carrying fewer separators, and a bare pipe inside a cell is kept as an extra column, so both
  survive formatting with their defect; an escaped `\|` is kept as cell content. Each is the fixture the controls use.

## Goals / Non-Goals

### Goals

- Every recognized table in the gated markdown has rows whose **cell count** matches its delimiter row's, and a mismatch
  fails `check` naming the file and line.
- The scope is the Spotless target's file set, owned once.

### Non-Goals

- No change to the pre-commit hook, to what Spotless formats, or to any non-markdown file.
- No general markdown validator: a table whose opening row begins with a blockquote or list prefix is **outside this
  check's promised coverage**, and the requirement says so.
- No alignment or aesthetic rule: the check counts cells, never spacing.

## Decisions

### D1 — The build owns the file set, and the mode scans what it is given

- What: `--table-rows` reads the paths it is given as positional arguments; `verifyTableRows` passes the Spotless
  markdown target's resolved files, and `check` depends on it.
- What: a run with no path fails, a supplied file that cannot be read fails naming it, and a readable file with no table
  passes.
- Why: the scope must not be restated in Python, where it would drift from the formatter's own target, and a vacuous
  scan must not read as clean.

### D2 — The table is the block between its delimiter row and the next block boundary

- What: a table opens on a line carrying an unescaped pipe whose next line is a delimiter row that itself carries an
  unescaped pipe (cells of `-` with optional `:`), and it runs from there while a line belongs to it.
- What: it ends at a blank line, and at a line that begins another block: a heading (`#`), a fence (``` or `~~~`), a
  thematic break (`---`, `***`, `___`), a list item (`-`, `*`, `+`, or `1.`), a blockquote (`>`), or an HTML block.
- What: a non-blank line inside the block carrying no unescaped pipe is a wrapped row and fails.
- What: a table whose opening row begins with a blockquote or list prefix is not judged.
- What: a line beginning with `<` is read as an HTML block and ends the recognized table, so a row that begins with `<`
  is not examined and neither are the rows after it — an accepted false negative this design and the requirement state,
  not a silent one.
- Why: with those boundaries fixed, the check judges what the rendered table occupies; beyond them it makes no claim.

### D3 — Columns are cells, and an escaped pipe is content

- What: a line's cells are what splitting on its unescaped pipes yields after one optional leading pipe and one optional
  trailing pipe are removed; the comparison is cell counts, so a table whose outer borders differ still matches.
- What: a pipe is a separator unless it follows an odd number of backslashes, so `\|` stays content and `\\|` separates.
- Why: the cell count is the property the rendered row has, and the escape rule is what keeps the documented remedy
  legal.

### D4 — The report names the file, the line, and the counts

- What: each offender prints its path, its line, its own cell count, and the delimiter row's; a wrapped row says it
  carries no separator.
- Why: a table defect is invisible in the rendered diff and in `spotlessCheck`, so the message is what a reader acts on.

## Claims table

| #   | Claim                                                 | Source                                                                              | Reproduced                                            |
| --- | ----------------------------------------------------- | ----------------------------------------------------------------------------------- | ----------------------------------------------------- |
| 1   | The build already registers per-check `verify*` tasks | `grep -n "verifyConflictMarkers" build.gradle.kts` and `grep -n "verifyLargeFiles"` | both registered, both depended on by `check`          |
| 2   | A mode is a flag plus a handler                       | `grep -n "MODE_FLAGS" scripts/commit-hygiene.py`                                    | the tuple and the argument block                      |
| 3   | The markdown target's scope and exclusions            | `grep -n "format(\"markdown\")" build.gradle.kts`                                   | the target and its `targetExclude` entries            |
| 4   | Prettier keeps a wrapped row as a short second row    | the probe in Context (`npx prettier@3.9.10 --prose-wrap always`)                    | the probe's two-row output                            |
| 5   | Prettier keeps a bare pipe as an extra column         | the same probe                                                                      | its four-column output against a three-cell delimiter |
| 6   | Prettier keeps an escaped pipe as content             | the same probe                                                                      | its three-cell output                                 |
| 7   | The escape is the documented pipe remedy              | `grep -n "escape it as" AGENTS.md`                                                  | the `Formatting` convention's pipe clause             |

## Risks

- A table directly followed by prose without a blank line is malformed markdown; the block rule ends at a block start,
  so such prose is judged inside the block and reported rather than silently accepted.
- A new markdown target inherits the check because the build passes its own resolved set.
