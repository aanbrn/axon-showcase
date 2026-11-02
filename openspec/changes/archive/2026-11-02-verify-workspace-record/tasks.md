# Tasks

Workspace:
`unit=verify-workspace-record; path=/Users/afanasyev/Development/axon-showcase-verify-workspace-record; branch=verify-workspace-record; writer=ses_e653ab5caffeGjp1myeRL03Emh`

## 1. The check

- [x] 1.1 `scripts/commit-hygiene.py` gains `parse_workspace_record(text)`: the `Workspace:` marker at column zero,
      optionally preceded by a backtick, the lines up to the first blank line joined into one logical line, backticks
      stripped, one trailing sentence period dropped from each value, and the `;`-separated `key=value` pairs returned,
      with a reason naming what is missing or malformed otherwise.
- [x] 1.2 The parser requires the four fields, an absolute `path`, a non-empty `branch`, a `writer` matching
      `ses_[A-Za-z0-9]+`, and no unknown key, failing on each with its own message.
- [x] 1.3 `find_invalid_workspace_records(repo)` reports each live `openspec/changes/<name>/tasks.md` whose record is
      absent or malformed, or whose `unit` is not the directory's name, and each archived
      `openspec/changes/archive/<date>-<name>/tasks.md` whose name date is on or after `WORKSPACE_RECORD_ARCHIVE_SINCE`
      (`2026-11-02`) and whose record fails the same judgment; a directory carrying no `tasks.md`, and an archive dated
      before the constant, are not this verification's subject.
- [x] 1.4 `ACKNOWLEDGED_RECORDLESS_ARCHIVES` is a map of one archived directory name to the reason it landed before this
      gate without a record, empty at introduction; the archived scan skips a directory the map names, so a second
      directory sharing its date still fails.
- [x] 1.5 `check_workspace_records(repo)` names each offender with its path and defect, returns the count, and
      `--workspace-records` dispatches to it.
- [x] 1.6 `--staged` gains `check_staged_workspace_records(repo, paths)`: the same judgment over a staged live or
      archived `tasks.md` (the archived name with its date segment stripped), plus a refusal when `path` is not the
      worktree's top level or `branch` is not the branch being committed. The branch comparison is skipped only on a
      detached head, which is git's answer rather than a failure; a failed top-level or branch read refuses, naming the
      command, per the capability's failed-read requirement. A staged archived record a merge brings unchanged is
      exempt, through the existing `merge_carries_unchanged` distinction, because merged content is not the commit's
      own.
- [x] 1.7 `MODE_FLAGS` gains the flag, the module docstring's mode enumeration states it, and `build_parser`'s
      `description` lists its subject.
- [x] 1.8 `scripts/test-commit-hygiene.py`'s `MODE_TEST_COVERAGE` maps the mode to `WorkspaceRecordTests` and to
      `check_workspace_records`, so the mode-coverage control sees its class and its call.

## 2. The wiring

- [x] 2.1 `build.gradle.kts` registers `verifyWorkspaceRecords` like its siblings: `inputs.file` on the script,
      `inputs.dir` on `openspec/changes`, `outputs.upToDateWhen { false }`, and the mode's `commandLine`.
- [x] 2.2 The `check` block depends on it.

## 3. The controls

- [x] 3.1 A live change directory whose `tasks.md` carries no record fails, naming the file.
- [x] 3.2 A record naming another unit fails.
- [x] 3.3 A relative `path`, an empty `branch`, a `writer` outside the shape, and an unknown key each fail, naming the
      field.
- [x] 3.4 Each recorded form parses: the wrapped single logical line, `Workspace:` followed by a backticked value line,
      one backticked line beginning with the marker, and a writer value ending in a sentence period.
- [x] 3.5 A tree with neither a live change directory nor an archive dated on or after the constant passes, and a live
      directory without `tasks.md` is not judged while such an archive still is.
- [x] 3.6 The guard refuses a staged `tasks.md` whose record is missing, and one whose `path` or `branch` names another
      tree; the same file staged with an agreeing record passes, and a detached head skips only the branch comparison
      rather than failing.
- [x] 3.7 The entry point (`main([...])`) exits 1 and prints the offender beside the exit code it derives.
- [x] 3.8 Measured, five mutations: the missing-record branch neutered → 1 failing case; the field validation skipped →
      3 malformed-field subcases; the `unit` comparison neutered → 1; the boundary made exclusive → 1; the guard's
      `unit` branch removed → 1; restored, 22 pass.
- [x] 3.9 An archived directory dated on or after `WORKSPACE_RECORD_ARCHIVE_SINCE` whose `tasks.md` carries no record
      fails; the same directory dated before it passes.
- [x] 3.10 A staged archived record a merge brings unchanged is not judged, while one the commit authors is.
- [x] 3.11 An archived directory the acknowledgement names passes without a record, while a second directory sharing its
      date and carrying none fails.

## 4. The records

- [x] 4.1 The unit's process-defect record (`docs/process-defects/verify-workspace-record.md`), or the statement that
      none was observed.
- [x] 4.2 The idea is removed from `docs/ideas.md`.
- [x] 4.3 The capability's `## Purpose` enumerates each build check this capability owns and a delta cannot carry one:
      add the workspace-record gate to it in the archive commit.
- [x] 4.4 Docs refresh: add the new refusal to `AGENTS.md`'s enumeration of the pre-commit guard's refusals and a
      sentence naming the verification to its **Workspace** bullet; check `README.md` and `docs/adr/` for an enumeration
      of the `check` members or of `verify*` tasks.
- [x] 4.5 Confirm in the archive commit that no archive dated on or after `WORKSPACE_RECORD_ARCHIVE_SINCE` lacks a
      record. If one landed before this gate, add that directory to `ACKNOWLEDGED_RECORDLESS_ARCHIVES` with its reason —
      the constant stays fixed, so a second directory sharing its date still fails.

## 5. The claims

- [x] 5.1 Every row of `design.md`'s claims table whose Result reads `pending implementation` is re-derived from the
      implementation and states what its command yields.
- [x] 5.2 Re-derived after the unit's last ledger append: 447 directories, 58 token files, 56 records, 7 at or after
      `2026-11-02`, the newest record-less `2026-10-31-add-claims-execution-verify`; row 2 prints `archive` at the base
      and row 12's grep returns no match there (both now anchored to `31ddfd5c`).

## 6. The review

- [x] 6.1 `review-quick` rounds, recorded in `docs/review-rounds.tsv` with their session fields, the closing one
      carrying no class.
- [x] 6.2 Deep-review routing: `REQUIRED`, signal `requirement-block-added` (the delta's ADDED requirement), derived
      with `scripts/review-routing.py --unit verify-workspace-record --change verify-workspace-record`.
- [x] 6.3 Deep review. Pass 1: two `should-fix` — task 3.8 under-recorded its own measurement, and the guard's
      absent/malformed branch had no control — and one nitpick, that the guard's archive half was date-agnostic while
      its wording claimed the check's rule. All applied: 3.8 now lists five mutations and 22 passing cases; a guard case
      stages a record-less and a malformed `tasks.md`; and the guard now shares the check's scope through one
      `archived_change_out_of_scope` predicate, with cases pinning both exclusions. Pass 2 verified those and raised two
      more, both applied: the delta's guard scenario is scoped to the verification's reach, and a boundary-dated guard
      case pins its own `<`. Zero findings remain.
