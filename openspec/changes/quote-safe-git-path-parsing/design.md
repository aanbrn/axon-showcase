# Design

## Context

See `proposal.md` — Why. What constrains the approach, verified against the repository:

- **The precedent is in the same file.** `find_non_executable_scripts` already reads `git ls-files -s -z` and splits on
  `"\0"`, so the fix follows an established in-file pattern rather than inventing one.
- **The git calls divide by kind, and only one kind needs `-z`.** Four sites _enumerate paths_ — `staged_paths`
  (`diff --cached --name-only`), `find_staged_then_edited` (`status --porcelain`), `find_tracked_ignored`
  (`ls-files --cached --ignored`), and the already-fixed `find_non_executable_scripts` (`ls-files -s -z`) — so three
  need the fix. Two more are not path enumerations: `staged_file_text` _receives_ a path and passes it to
  `git show :<path>`, and `find_conflict_markers` reads `git grep` output, whose shape is `path:line:text` (contents).
  Neither needs `-z`; adding it to `git grep` would change the output shape for no gain.
- **`status --porcelain -z` reverses the rename field order, and this is the trap in the fix.** Observed in a scratch
  repo with distinct names (`git mv OLD.txt NEW.txt`), so the order is unambiguous: the newline form is
  `R  OLD.txt -> NEW.txt` (**old first**, then the arrow, then the new), while the `-z` form is `R  NEW.txt\0OLD.txt\0`
  (**new first**, in the status-prefixed field, then the old name as a **separate NUL field**). The order genuinely
  swaps, so under `-z` the rename branch takes the first field as the path and **skips the second**, and both the
  `strip('"')` and the `" -> "` handling become dead. The trap is not a dropped record but a **bogus path**: fed the
  real `-z` output with only the split changed, the current body misreads the old name as a status-prefixed record, so
  `git mv RM.txt New.txt` yields the offender `'txt'` (the old name's tail past its two "status" characters) rather than
  nothing — reproduced, so the second field must be skipped explicitly rather than left to the status-column check.
- **The failure is a silent under-detection, not a false alarm.** Reproduced: a staged `café.md` yields
  `formatter_owned() == []` today and `['café.md']` with `-z`. The guard reported a clean set it never examined, which
  is this repository's stated worst failure mode for a check (an unexercised check passing silently).
- **`find_force_staged_artifacts` compares two of these sets.** It hashes `find_tracked_ignored` and tests membership of
  `staged_paths`; if either side quotes differently the comparison misses, so both must be fixed together rather than
  one.
- **The checker is stdlib-only and must run under macOS's system `python3` (3.9).** `-z` and `str.split("\0")` are plain
  stdlib and 3.9-compatible.

## Goals / Non-Goals

**Goals:**

- Every path the guard reads is the real path, not git's C-quoted rendering, so a non-ASCII path is inspected exactly
  like an ASCII one.
- The fix is gated by a test per fixed site, so quote-safety cannot silently regress.
- The spec states the property, closing the gap that let the defect ship unnoticed.

**Non-Goals:**

- **Not touching `find_conflict_markers`.** It parses `git grep` line output (contents), not a path list; adding `-z`
  there would be a different change with a different output shape.
- **Not changing what the guard detects.** This is a parsing fix: the same defects are detected, plus the ones the
  mis-parse hid. No new check, no new severity.
- **Not reworking the guard's structure.** Each site keeps its current shape; only the git invocation and the split
  change.

## Decisions

### Split on NUL with `-z`, matching the in-file precedent

`git`'s `-z` emits NUL-terminated, unquoted paths; the fix is `--porcelain -z` / `--name-only -z` / the `ls-files -z`
form plus `.split("\0")` and dropping the now-unneeded quote handling. Rejected: setting `-c core.quotePath=false` — it
changes the rendering but still leaves newline-splitting, which breaks on a path containing a newline (a filename can
hold one), so it is a weaker fix for the same defect class. Rejected: unquoting the C-escaped string in Python — that
re-implements git's escaping rules, which is exactly the kind of hand-rolled complement the repository's own lessons
warn against.

### Drop `strip('"')` at the `status --porcelain` site rather than keep it

`find_staged_then_edited` currently does `path.strip('"')`, which only removes the surrounding quotes and leaves the
escaped bytes; with `-z` the path is unquoted already, so the strip is dead. Removing it is correct, not cosmetic:
keeping it would silently mangle a path whose last character is a `"`. Rejected: leaving it in for safety — a no-op on
correct input and a hazard on a path ending in a quote.

### Test each fixed function directly, with the cross-site case kept separate

A single test would prove only the one it exercises (the repository's own rule: one arm proves only that arm), so the
cases are derived from the **three functions**, each exercised by calling it directly: `staged_paths` (a staged
non-ASCII formatter-owned file is selected by `formatter_owned`), `find_staged_then_edited` (a staged-then-edited
non-ASCII path is reported), and `find_tracked_ignored` (a tracked-ignored non-ASCII path is reported). Each fails on
the pre-fix code and passes after. Two of the three follow an existing direct-call precedent (`TrackedIgnoredTests`
calls `find_tracked_ignored`, `StagedThenEditedTests` calls `find_staged_then_edited`, and `ExecutableBitTests` already
carries a `café.sh` case), but **`FormatterTests` does not** — it drives `check_staged`, the composed entry point — so
that arm adds a direct `staged_paths` assertion the class does not have today rather than matching its shape.

`find_force_staged_artifacts` is deliberately **not** one of those three arms: it composes `staged_paths` and
`find_tracked_ignored`, so a fixture for it fails if _either_ site is wrong and cannot attribute a regression to one. It
gets its own case in `ForceStagedArtifactTests`, labelled as the combined check — the one fixture that legitimately
spans two sites.

## Risks / Trade-offs

- **A `-z` split can leave a trailing empty element.** `"a\0b\0".split("\0")` yields `["a", "b", ""]`; every site must
  filter empties, which the existing code already does for the newline form (`if line`) and the `-z` site does by its
  `len(parts) < 4` guard. Mitigation: each fixed site filters empty elements explicitly, and the tests include a normal
  single-path case so a future over-filter is caught.
- **The fix changes the guard's behavior for paths it previously skipped**, so a developer with a non-ASCII path may see
  a new refusal. That is the intended correction — the guard was under-detecting — and it is recorded in the proposal's
  Impact rather than presented as a regression.
