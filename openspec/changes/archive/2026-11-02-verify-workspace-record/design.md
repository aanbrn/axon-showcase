# Design

## Context

`AGENTS.md` requires each unit to record its workspace before its first write —
`Workspace: unit=<unit>; path=<absolute path>; branch=<branch>; writer=<ses_…>` — and states that the record lives with
the unit's own record: its `tasks.md` when it has a change dir. `pause-resume-unit` is its reader: it confirms the live
tree against the record's path, branch, and writer, and hands the writer role over by updating it. No gate reads it.

The record is missing in practice. The archived `2026-10-26-reject-a-foreign-change-dir-at-commit` has no record line at
all, and this session's own earlier unit recorded one only after a review round asked. The archive shows how recent the
rule is: 56 of its change directories carry a record, and 17 that landed after it carry none (claims table row 1), so a
verification over the archive would fail on the 374 units predating the rule as well as on those 17 post-rule omissions.

Three forms are recorded, each beginning at column zero: a single logical line wrapped in the source
(`Workspace: unit=…; path=…;` then `branch=…; writer=…`); `Workspace:` alone with the backticked value on the next line;
and a whole record inside one backticked line that begins with a backtick and `Workspace:`. A record also sometimes ends
its writer value with a sentence period. All are in the archive and all have to parse, while the anchor stays at column
zero so an indented task line that mentions the record is not read as one.

## Goals / Non-Goals

**Goals.**

- A live change directory whose `tasks.md` carries no workspace record, or one naming another unit, fails `check`.
- An archive created from the gate's introduction date onward is judged by the same rule, so a unit that archives before
  any CI run still has its record verified.
- The pre-commit guard refuses the same defect at the commit that first carries the change directory.
- The guard also refuses a record whose path or branch name a different tree than the one being committed.

**Non-Goals.**

- Re-judging an archive dated before `WORKSPACE_RECORD_ARCHIVE_SINCE`: that history is left as recorded.
- Verifying that the writer is the _writing_ session rather than another one — the check sees only the identifier's
  shape.
- A unit with no change directory: its record lives in its hand-off or pull request, which the offline guard does not
  fetch.

## Decisions

**1. A `--workspace-records` mode for `check`, and the same judgment inside `--staged`.**

- Chosen: a distinct mode registered in `MODE_FLAGS` and `verifyWorkspaceRecords` in the `check` block, plus a
  `--staged` clause that reads the staged copy — the shape `--ideas-sections` already uses, where
  `check_staged_idea_sections` applies the build verification's judgment to the index. The clause shares the check's
  scope as well as its rule, so a staged archive dated before the boundary, or one the acknowledgement names, is not
  re-judged: an edit to history stays history.
- Rejected — the build check alone: a defect caught only at `check` reaches the branch, and the record exists to be read
  by a resume long before a pull request.
- Rejected — the guard alone: it is activated per clone and bypassable with `--no-verify`, so it is never the durable
  gate.

**2. The subject is every live change directory plus every archive dated on or after the gate's introduction.**

- Chosen: each `openspec/changes/<name>/tasks.md`, and each `openspec/changes/archive/<YYYY-MM-DD>-<name>/tasks.md`
  whose name date is on or after `WORKSPACE_RECORD_ARCHIVE_SINCE` — `2026-11-02`, the day this gate lands. History
  before that date stays as recorded: the newest record-less archive is dated `2026-10-31`, and every archive dated on
  or after the constant carries a record (claims table row 14). A tree with neither a live change directory nor an
  archive at or after the constant passes, since the contract requires neither. An archive at or after the constant that
  landed before this gate is exempt only by name, in an acknowledgement that carries its reason — one entry per
  directory, never a moved constant, so a second directory with the same date and no record still fails.
- Rejected — the live directory alone: a unit whose only push carries the archive commit, with the hook absent and the
  archive taken before CI, never exposes its live directory to `check`, so its record would go unjudged by the durable
  gate.
- Rejected — every archive directory: 391 of the 447 present at introduction omit the record, so the check would fail
  the tree that introduces it.
- Rejected — the branch's own commits (`git diff origin/main...HEAD`): CI checks out the merge ref and has no
  `origin/main`, so the comparison has nothing to read there.

**3. `check` asserts the record's shape and its unit; the guard adds path and branch.**

- Chosen: `check` requires the four fields, an absolute `path`, a non-empty `branch`, a `writer` matching `ses_` plus
  alphanumerics, and `unit` equal to the change directory's name (the archive's stripped name when the move is staged).
  The guard additionally requires `path` to be the worktree's own top level and `branch` the branch being committed.
- Rejected — asserting path and branch in `check`: CI checks out a detached head under a runner path, so neither is the
  unit's, and a check that read them would fail every pull request.
- Rejected — skipping the path/branch half entirely: a record naming another tree is the defect a resume acts on.

**4. The record is read as one logical line, from the `Workspace:` line to the first blank line.**

- Chosen: accept the marker after an optional leading backtick at column zero, join the lines up to the first blank
  line, strip backticks, split on `;` into `key=value` pairs, and drop one trailing sentence period from a value — so
  all three recorded forms parse and a wrapped line does not need the writer to keep it on one line.
- Rejected — requiring the single-line form: this session's own units record the backticked two-line form, and a gate
  that rejected them would fail the tree that introduces it.
- Rejected — accepting an indented marker: task text mentions the record mid-line and indented, so the anchor stays at
  column zero and a prose mention is not read as a record.

**5. An unknown field fails, and the writer is checked by shape only.**

- Chosen: the record's four fields are closed — a fifth key fails, naming it — since the format is `AGENTS.md`'s. The
  writer's value is matched against `ses_[A-Za-z0-9]+`, which is all a static check can establish about it.
- Rejected — accepting extra keys: an extension would then pass silently and read as the standard format.
- Recorded limitation: the reviewer's identifier has the same shape, so the check cannot tell the writing session from
  the reviewer's; the requirement states this rather than implying a guarantee.

**6. One added requirement covers both entry points.**

- Chosen: a single `ADDED` requirement states the build verification and the guard's matching refusal, because they are
  one subject — what a unit's record must contain — with two readers.
- Rejected — a `MODIFIED` block on "A pre-commit guard blocks a mechanically defective staged set": it would copy a
  four-thousand-character requirement to add one clause, and that requirement's subject is the staged set's mechanical
  defects rather than the unit's record.

## Risks

- **The guard's path check refuses a continued unit until its record is updated.** That is the intended behaviour — the
  resume's own step updates the writer and, when the tree moved, the path — but it makes a stale record a commit-blocker
  rather than a nuisance.
- **`check` passes with no live change directory.** A docs-only pull request has none, and the contract does not require
  one, so this is the legitimate empty case rather than a hole: the pull request that adds a change directory carries
  the subject. The archive half judges only the seven directories dated on or after `2026-11-02`, which makes its
  controls — not a live corpus — the evidence that it works.
- **The archive keeps its missing records** — most of its change directories predate the rule, and 17 landed after it
  carry none — so a reader who greps the archive for the record finds it in a minority of units.
- **The acknowledgement is a suppression surface.** It is empty at introduction, and each entry will name one directory
  with its reason; a pre-introduction archive at or after the constant is the only thing it may cover.

## Claims

Base revision for the before-state rows: `git merge-base HEAD origin/main` = `31ddfd5c`.

| #   | Claim                                                                                                                | Source                                                                                                                                                                                                                                                              | Result                                                                                                                                                                                                                                         |
| --- | -------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Most archived change directories carry no workspace record                                                           | `grep -l 'Workspace:' openspec/changes/archive/*/tasks.md \| wc -l` (58), `grep -l 'unit='` over those files (56), and `git log --diff-filter=A --name-only --format=@@%ad --date=short -- openspec/changes/archive` joined against them for the landing-date split | 58 files carry the token, 56 carry a record; the split is by landing date against the rule's commit `7e0e564f` (2026-10-25): 17 directories that landed after it carry none, and 374 landed on or before it                                    |
| 2   | `main` carries no live change directory                                                                              | `git ls-tree --name-only 31ddfd5c:openspec/changes`                                                                                                                                                                                                                 | `archive` only at the base revision, so the live half has no subject on `main`; the archive half judges the seven directories dated on or after `2026-11-02`                                                                                   |
| 3   | Three record forms are recorded                                                                                      | the archived `2026-11-02-process-bindings-contract`, `2026-11-02-report-unmapped-round-classes`, and `2026-10-27-unify-agent-language-policy` `tasks.md`                                                                                                            | a wrapped logical line; `Workspace:` plus a backticked value line; and one backticked line beginning with the marker; 15 files carry a period, 14 within the record                                                                            |
| 4   | `AGENTS.md` states the record's canonical form                                                                       | `AGENTS.md`, the Change lifecycle's **Workspace** bullet                                                                                                                                                                                                            | `Workspace: unit=<unit>; path=<absolute-path>; branch=<branch>; writer=<ses_…>`                                                                                                                                                                |
| 5   | The record's reader is the pause/resume procedure                                                                    | `.opencode/skills/pause-resume-unit/SKILL.md`                                                                                                                                                                                                                       | it confirms the tree's path and branch against the record and hands the writer role over through it                                                                                                                                            |
| 6   | The guard already applies a build verification to the staged copy                                                    | `check_staged_idea_sections` in `scripts/commit-hygiene.py`                                                                                                                                                                                                         | the ideas-section judgment runs over the index's content, not the working tree's                                                                                                                                                               |
| 7   | The gate's bad state is reachable and it fires                                                                       | a live change's `record` removed from this unit's `tasks.md`                                                                                                                                                                                                        | `Task :verifyWorkspaceRecords FAILED`, naming `openspec/changes/verify-workspace-record/tasks.md` and `no Workspace: record`                                                                                                                   |
| 8   | The live tree passes                                                                                                 | `./gradlew verifyWorkspaceRecords` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`                                                                                                                                                                    | both BUILD SUCCESSFUL; `python3 scripts/test-commit-hygiene.py` 308 tests OK                                                                                                                                                                   |
| 9   | The controls bite                                                                                                    | five mutations over `scripts/commit-hygiene.py`                                                                                                                                                                                                                     | the missing-record branch neutered → 1 failing case; the field validation skipped → 3 malformed-field subcases; the `unit` comparison neutered → 1; the boundary made exclusive → 1; the guard's `unit` branch removed → 1; restored → 22 pass |
| 10  | The mode joins the script's exposed modes and their test coverage                                                    | the equality control plus the module run                                                                                                                                                                                                                            | the mapping names every mode the checker exposes; 308 tests OK                                                                                                                                                                                 |
| 11  | The verification is part of the standard build                                                                       | `build.gradle.kts` and the Docker-free `check` run                                                                                                                                                                                                                  | `dependsOn("verifyWorkspaceRecords")`, and the check BUILD SUCCESSFUL                                                                                                                                                                          |
| 12  | No gate reads the workspace record                                                                                   | `git grep -n 'Workspace:' 31ddfd5c -- scripts build.gradle.kts`                                                                                                                                                                                                     | no match at the base revision: the record was read by the resume procedure only                                                                                                                                                                |
| 13  | CI checks out a detached head under a runner path                                                                    | `.github/workflows/ci.yml`'s checkout step, then the same in a CI run                                                                                                                                                                                               | `actions/checkout` defaults to the merge ref, so `git symbolic-ref` has no branch and the toplevel is a runner directory                                                                                                                       |
| 14  | Every archive dated on or after the gate's landing date carries a record, and the newest record-less one predates it | a per-directory check of `openspec/changes/archive/*/tasks.md` dated on or after `2026-11-02` against the record parse                                                                                                                                              | the seven directories at or after `2026-11-02` all carry a record; the newest record-less archive is `2026-10-31-add-claims-execution-verify`                                                                                                  |

Dismissals, one reason per candidate kind `scripts/claim-scan.py` reports:

- `[count]` — the archive tallies are row 1's derivation and row 14's is its own derivation, each stated with it; every
  other count is a task or requirement number, which labels a step rather than asserting a figure.
- `[absolute]` — a universal qualifier inside a requirement, a decision, or a control scenario is normative text rather
  than a claim about the repository; the factual absolutes are rows 1–6 and 12–14, each derived by its own source.
- `[cross-ref]` — the two candidates are `path`/`branch` and `pause`/`resume`, written with slashes; the unit's
  artifacts cite no tracker reference.
- `[path]` — a path candidate is a file this change reads or edits, or a git command it records; none is a path the
  repository does not track.
