# Proposal

## Why

`openspec archive --yes` reports an incomplete task as a warning and continues, and no gate reads the archive
afterwards, so an archived `tasks.md` can carry an unticked box while the pull request merges. The archive already holds
dozens of files with historical unchecked boxes, so the check must judge only what a commit stages.

## What Changes

- `scripts/commit-hygiene.py`'s staged check refuses a staged `openspec/changes/archive/**/tasks.md` that carries an
  unticked task, naming the file and the task line, as a sibling of the archive-sync check.
- The check judges the whole content of a staged archived tasks file and reads no file the commit does not stage, so the
  archive on `main` is untouched.
- A commit that stages an already-archived file whose boxes are historical is refused too, and uses `--no-verify` when
  that staging is deliberate; the refusal never implies its tasks should be ticked.
- A merge commit that brings an archived tasks file unchanged is exempt (its staged blob equals the merged revision's);
  a resolution or an edit that leaves a different blob is judged.
- A bracket that is neither empty nor whitespace nor `x` nor `X` (`[~]`, `[-]`) is refused as an unsupported marker,
  never counted as done.
- An archived tasks file absent from the staged set is not judged.
- There is no task exemption: `AGENTS.md` already requires a deferred task to be reworded to record the deferral and
  then ticked, so the guard refuses an unticked box rather than excusing it by task category — except a file an incoming
  merge brings unchanged, whose content is not the commit's own.
- `scripts/test-commit-hygiene.py` gains the controls `tasks.md` §2 lists.
- `scripts/git-hooks/pre-commit`'s header, `README.md`'s guard enumeration and the `AGENTS.md` `git add <dir>` gotcha
  name the new refusal.
- `docs/ideas.md` loses the 2026-10-19 entry this change implements.
- The `commit-hygiene` spec's pre-commit requirement gains the archived-tasks refusal and its scenarios.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/commit-hygiene`: the pre-commit guard's enumerated checks gain the staged archived tasks file's
  unticked-task refusal.

## Impact

- `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, `scripts/git-hooks/pre-commit`, `README.md`,
  `AGENTS.md`, `docs/ideas.md`, and the delta for `showcase/quality/commit-hygiene`.
- No JVM code, build script, deployment, or API change; `openspec archive`'s own non-blocking warning is unchanged — the
  guard refuses the resulting commit, not the archive command.
