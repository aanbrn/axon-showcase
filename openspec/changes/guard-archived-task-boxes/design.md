# Design

This design accompanies `proposal.md`: the pre-commit guard must refuse a staged archived change directory whose tasks
are not all ticked, judging the staged file's whole content and no file the commit does not stage.

## Context

- `openspec archive <change> --yes` relocates the change directory and syncs its delta; the change's `tasks.md` travels
  with it. `AGENTS.md` records that the command continues past an incomplete task with a warning line rather than
  failing, so the warning is the only signal and nothing re-reads it.
- A verification whose only run needs the pushed branch is ticked in the archive commit's own edit of the archived
  `tasks.md` or in a commit before the archive, so an unticked box at archive time is a tick nobody performed, and the
  guard is the only reader of either kind (the record: `add-architecture-auditor-agent` archived with its smoke-run
  unchecked).
- The archive already carries dozens of `tasks.md` files with historical unchecked boxes, so the check must judge the
  staged content only; the idea records the same requirement.
- `find_unpaired_archive_sync` already inspects staged archived change dirs, so the check is its sibling.
- `AGENTS.md`'s deferred-task clause requires a task the owner decouples to be reworded to record the deferral and then
  ticked, so no exemption list is needed.

## Goals / Non-Goals

### Goals

- A staged `openspec/changes/archive/**/tasks.md` with an unticked task is refused, naming the file and the task line.
- A fully ticked archived tasks file passes, and an archived file the commit does not stage is not judged.

### Non-Goals

- No change to `openspec archive`'s own non-blocking behavior; the guard refuses the commit, not the command.
- No check of any other archived file, and no judgment of the archive on `main` that a commit does not stage.

## Decisions

### D1 — Selection by staged path, judgment of the entire indexed content

- What: the selection is a staged path under `openspec/changes/archive/**` whose basename is `tasks.md`; for each such
  path the finder reads the **staged blob** through `staged_file_text` and refuses any task line whose checkbox is not
  ticked, naming the file and the line.
- What: the working tree's copy is never substituted for the indexed one, exactly as the ledger guard reads its staged
  blob.
- What: a selected path is exempt when the commit's merge brings it unchanged: the index blob equals the blob of the
  same path in a merged revision; a resolution or an edit that leaves a different blob is judged.
- What: the lookup is quiet: a merged revision that does not hold the path is no match rather than a failed read, so
  `git rev-parse`'s exit status for `<revision>:<path>` is read instead of the raising helper's output.
- What: an archived `tasks.md` absent from the staged set is not judged at all.
- Why: the archive commit carries the whole file, and a diff-only judgment would let a historical box the commit
  re-stages pass unread; a worktree read would judge content the commit does not carry.

### D2 — No task exemption; historical unchecked content also refuses the commit

- What: every unticked box in a staged archived tasks file fails by task category, whether the commit introduced it or
  the file was already archived — apart from the file-level merge exemption D1 states.
- What: refusing the commit does not imply that such a task should now be marked completed; the archive's historical
  record is left as recorded, and a commit that must stage it deliberately uses `--no-verify`, the project's existing
  deliberate-exception path.
- Why: the workflow's own rules already make an unticked box either a defect or a task to reword-and-tick (`AGENTS.md`),
  so an exemption list would carve out states the process does not permit; the 2026-10-23 retrospective's open question
  is settled by that rule, not by new exceptions.

### D3 — A checkbox is a list item, and `x`/`X` is the only done form

- What: a task line is a list item at any indentation — `-`, `*`, `+`, or an ordered marker — whose bracket holds at
  most one character and is not followed by `(`, which is a Markdown link's syntax; a trailing annotation after the box
  is part of the task text.
- What: a bracket whose content is empty or whitespace is unticked; `x` or `X` is ticked; any other content (`[~]`,
  `[-]`) is refused as an unsupported marker rather than counted as ticked or silently exempt.
- Why: the corpus's tasks files use `[ ]` and `[x]` only, and a third marker must fail loudly rather than pass as done;
  the link exclusion keeps `- [text](url)` from being read as a task.

### D4 — The spec delta is one `MODIFIED` block

- What: the pre-commit requirement gains the archived-tasks refusal and seven scenarios (an unticked box refused, an
  unsupported marker refused, a fully ticked file passing, an unstaged file not judged, the indexed content judged
  rather than the working tree's, a link bullet not being a task, and a merge commit's file exempt).
- Why: the requirement already enumerates the guard's refusals; the new behavior refines it.

### D5 — No Purpose refresh

- What: the capability Purpose keeps its text.
- Why: it describes the hook as inspecting the staged set and names the ledger checks it added; the archived-tasks
  refusal is another staged-set check, so the Purpose is not falsified. A delta cannot carry a Purpose, and a refresh is
  owed only where the Purpose's own text becomes false.

## Claims table

| #   | Claim                                                                     | Source                                                                                                                      | Reproduced                                                             |
| --- | ------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------- |
| 1   | `openspec archive --yes` continues past an incomplete task with a warning | `git show 6f74424d:AGENTS.md \| grep -n "continues past an"`                                                                | the verification bullet's sentence naming the warning line             |
| 2   | The archive-sync sibling already reads staged archived dirs               | `git show 6f74424d:scripts/commit-hygiene.py \| grep -n "def find_unpaired_archive_sync"`                                   | the function, called from `check_staged`                               |
| 3   | The archive path pattern                                                  | `git show 6f74424d:scripts/commit-hygiene.py \| grep -n "CHANGES_PREFIX\|ARCHIVE"`                                          | the prefix and the archive path the sync check builds                  |
| 4   | Archived files carry historical unchecked boxes                           | `git grep -c "^- \[ \]" 6f74424d -- 'openspec/changes/archive/*/tasks.md' \| wc -l`                                         | a non-zero count of files, which is why the check is staged-set scoped |
| 5   | The corpus's boxes are `[ ]` and `[x]` only                               | `git grep -h "^- \[[^]]*\]" 6f74424d -- 'openspec/changes/archive/*/tasks.md' \| sed 's/^\(- \[[^]]*\]\).*/\1/' \| sort -u` | `- [ ]`, `- [x]`                                                       |
| 6   | A deferred task is reworded and then ticked                               | `git show 6f74424d:AGENTS.md \| grep -n "reword it"`                                                                        | the smoke-run bullet's clause                                          |
| 7   | The pre-commit requirement lists the guard's refusals                     | `git show 6f74424d:openspec/specs/showcase/quality/commit-hygiene/spec.md \| sed -n '21,26p'`                               | the description's enumerated refusals, which the delta extends         |

## Risks

- A deliberately staged historical file with an unticked box is refused, so that staging uses `--no-verify`, the
  project's existing deliberate-exception path; a deferred box in a change being archived is reworded and then ticked
  instead. The refusal never establishes that a historical task is incomplete, and never justifies ticking one to
  satisfy the guard.
- An archive move stages every file, so the check runs on the change's own archive commit; that is its purpose.

## Non-Goals

- No check of an archived file outside `tasks.md`, and no reading of the working tree's archive.
