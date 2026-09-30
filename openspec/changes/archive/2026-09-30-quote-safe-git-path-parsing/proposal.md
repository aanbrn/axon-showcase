# Proposal

## Why

Three of `scripts/commit-hygiene.py`'s four **path-enumerating** git calls parse paths from **newline**-separated output
without `-z`, so git C-quotes any path holding non-ASCII bytes (`"caf\303\251.md"`). The fourth,
`find_non_executable_scripts`, was already fixed to use `-z`; the others were not. (Two further git calls are not path
enumerations and are out of scope: `staged_file_text` passes a path _to_ `git show`, and `find_conflict_markers` reads
`git grep` _contents_.) Reproduced: with a staged `café.md`, `staged_paths()` yields the quoted literal, so
`formatter_owned()`'s `.endswith(".md")` returns **False** and the formatter check is **silently skipped** for that file
— the guard reports a clean staged set it never examined. `find_staged_then_edited` and `find_tracked_ignored` mis-parse
the same way, and `find_force_staged_artifacts` compares two such sets, so either side quoting breaks the membership
test.

## What Changes

- `scripts/commit-hygiene.py`: read every git path list with `-z` and split on NUL in the three remaining sites —
  `staged_paths` (`git diff --cached --name-only`), `find_staged_then_edited` (`git status --porcelain`), and
  `find_tracked_ignored` (`git ls-files --cached --ignored`) — matching the `-z` pattern `find_non_executable_scripts`
  already uses. `find_conflict_markers` reads `git grep` output (file **contents**), not paths, so it is out of scope.
- `scripts/test-commit-hygiene.py`: cover each fixed site with a non-ASCII path, so the quote-safety is gated rather
  than dependent on a manual check.
- The `showcase/quality/commit-hygiene` spec's guard requirement gains the quote-safety rule: the guard's path parsing
  SHALL be quote-safe, so a path with non-ASCII bytes is inspected like any other.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/commit-hygiene`: "A pre-commit guard blocks a mechanically defective staged set" gains the
  requirement that its path parsing is quote-safe — currently the guard's contract says what it detects but not that a
  non-ASCII path is inspected at all, which is exactly the gap that let the silent skip ship.

## Impact

- Edited files: `scripts/commit-hygiene.py` (three parse sites), `scripts/test-commit-hygiene.py` (coverage), and the
  change's delta spec. The checker is stdlib-only Python and must stay compatible with macOS's system `/usr/bin/python3`
  (3.9) — `-z` and NUL-splitting are plain stdlib, so no compatibility risk.
- The guard's behavior changes only for paths it previously mis-parsed: a non-ASCII staged path is now inspected, so a
  commit that previously slipped past the formatter check for such a file is now correctly refused. No behavior change
  for ASCII paths.
- No build, CI, or deployment change; the guard runs from the tracked hook and the `verify*` Gradle tasks, which are
  unaffected because their inputs are the current repository's ASCII paths.
