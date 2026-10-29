# Tasks

## 1. The check

- [x] 1.1 In `scripts/commit-hygiene.py`, add a `--table-rows` mode whose positional paths are read as markdown, with a
      run given no path failing rather than passing.
- [x] 1.2 Implement the block rule: a fence disables it; outside one, a pipe row followed by a delimiter row opens a
      table that ends at a blank line or a line beginning another block (heading, fence, thematic break, list item,
      blockquote, HTML block); each non-blank line in the block must carry the delimiter row's cell count, and a line
      with no unescaped separator is a wrapped row. A blockquote- or list-prefixed table is skipped.
- [x] 1.3 Count cells rather than raw pipes — split on separators, then drop one optional leading and one optional
      trailing pipe — so a table whose outer borders differ still matches; a pipe separates only after an even number of
      backslashes, so `\|` is content and `\\|` separates; report each offender with its path, line, counts, and the
      wrapped-row wording.
- [x] 1.4 State the mode in the module docstring's mode list, and keep every other mode's behaviour unchanged.

## 2. The build

- [x] 2.1 In `build.gradle.kts`, register `verifyTableRows` beside `verifyConflictMarkers`, passing the Spotless
      markdown target's files, and add it to the `check` block's dependencies.
- [x] 2.2 Confirm the task's file set is the target's own resolution, not a second list.

## 3. The controls

- [x] 3.1 The Prettier output of a wrapped row — `| one | two |` then `| wrapped |` — fails, naming the file and line.
- [x] 3.2 The Prettier output of a bare pipe — `| one | a | b |` against a 3-cell delimiter — fails; the escaped form
      `| one | a \| b |` passes.
- [x] 3.3 A fenced diagram containing pipes passes, a clean table passes, and a table with no leading or trailing pipe
      passes.
- [x] 3.4 A table directly followed by a heading, a list, a blockquote and a thematic break, each without a blank line,
      ends at that line; a blockquote-prefixed table is skipped; a line with no separator inside the block fails.
- [x] 3.5 A run with no paths fails, and a supplied path that cannot be read fails naming it; a readable file with no
      table passes.
- [x] 3.6 Mutate the check (drop the count comparison; count escaped pipes as separators; treat a fence as content;
      ignore the block boundaries; scan no file when given none; swallow a read failure) and confirm 3.1–3.5 fail;
      restore.

## 4. The spec delta

- [x] 4.1 Write the `ADDED` requirement for the check with its nine scenarios (the standard check runs it, a wrapped row
      fails, a split cell fails, an escaped pipe passes, a fenced block is not a table, a clean table passes, a table
      without outer pipes passes, a supplied file that cannot be read fails, and a run with no path fails).

## 5. The human-facing record

- [x] 5.1 Remove the implemented `docs/ideas.md` entry, and name the check where the build-check enumeration lives
      (`README.md` / the `AGENTS.md` gotcha), if either enumerates the `verify*` tasks.

## 6. The capability Purpose

- [ ] 6.0 Refresh the `commit-hygiene` `## Purpose` in the archive commit: it enumerates the build checks, and this
      change adds one to that class (a delta cannot carry a Purpose).

## 7. Verification

- [x] 7.1 Run the pre-write derivation pass and record its claims table (`design.md`).
- [x] 7.2 Run `openspec validate --all`, `./gradlew spotlessApply`, `./gradlew testCommitHygiene`,
      `./gradlew verifyTableRows`, and the Docker-free `./gradlew check`, and record the results.
- [x] 7.3 Write the unit's process-defect record.
- [x] 7.4 Run the review gate: the quick loop (seven rounds on one reviewer session, rounds 4, 6 and 7 clean) and the
      routing decision — `REQUIRED` (`[requirement-block-added]`), so the deep pass ran: pass 1 found S1 (a pipe-less
      `---` read as a delimiter, so prose carrying a pipe false-failed) and S2 (the parked idea enumerating the
      `verify*` checks omitted the new task), both fixed with a control, and pass 2 reports "nothing remains". The
      manual pass follows.
