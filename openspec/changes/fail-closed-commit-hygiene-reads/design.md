# Design

## Context

`commit-hygiene.py` reads git through one thin wrapper (`git`) that returns the completed process, so each caller
decides what a non-zero exit means. The readiness mode added by `gate-manual-review-readiness` learned to split answer
from failure (`git_output`, `GitFailure`); its siblings did not, and they all parse `result.stdout` directly:

- `diff --cached --name-only` in `staged_paths` — an empty staged set.
- `show :<path>` in `staged_file_text` — a silently skipped marker scan.
- `status --porcelain -z` in `find_staged_then_edited` — no stale-index path.
- `ls-files --cached --ignored` in `find_tracked_ignored` — no tracked-ignored file.
- `diff`/`ls-files` over `openspec/specs` in `unstaged_spec_paths` — a paired archive move.
- `grep -E <conflict marker>` in `find_conflict_markers` — no conflict marker.
- `ls-files -s -z` in `find_non_executable_scripts` — every script executable.
- `ls-files -z` in `tracked_paths` — no tracked file, so no oversized one.
- `rev-parse --git-path` in `claim_file` — the claim path `axon-showcase-change`.
- `rev-parse --abbrev-ref HEAD` in `resolve_assignment`, later `current_branch` — no branch, so the claim alone assigns.
- `diff --cached --name-status -z --diff-filter=R` in `rename_sources` — no rename source.
- `diff --cached --name-only -z --diff-filter=D` in `staged_change_paths` — no deleted change path.

The pattern is the repository's own rule: a derivation whose failure maps onto the benign outcome reads as a clean
result. `--review-ready` closes it for one mode; the guard and the `check` tasks keep it open, and they are the paths
that gate a commit and a pull request.

## Goals and non-goals

- **Goal** — every mode reports a failed read, naming it, and exits non-zero.
- **Goal** — the exits that are answers stay answers, so a clean repository still passes.
- **Non-goal** — changing what any check looks for, or its offender messages.
- **Non-goal** — a new mode, task, or workflow step: the behaviour lives in the checker's own reads.

## Decisions

### Two raising helpers, one catch

Two failure kinds cover every read that must produce data: `git_output` raises `GitFailure` naming the command, and
`tracked_file_size` raises `FileFailure` naming the path. Both derive from `CheckFailure`, which `main` catches once —
around `dispatch` — printing `The check could not run: <failure>` and returning 1, so one catch leaves each `check_*`
function about its offenders. The manual-pass readiness mode keeps its own boundary wording — "Readiness is not
established" — while this catch prints "The check could not run".

### The exits that are answers, named where they are read

Three commands keep an explicit branch instead of going through `git_output`, because their non-zero exit is an answer:
`git grep`'s 1 means "no match"; `rev-parse --verify --quiet`'s 1 means the revision does not resolve; and
`merge-base`'s and `merge-base --is-ancestor`'s 1 mean "no common ancestor" and "not an ancestor". Every other non-zero
exit from those commands raises, including `rev-parse`'s and `merge-base`'s higher codes, which a fatal repository error
produces.

The branch read is a fourth shape: `symbolic-ref --quiet --short HEAD` exits 0 with the branch HEAD points at — an
unborn HEAD included, since HEAD already names its branch — and exits 1 when HEAD is detached, which `current_branch`
answers with an empty name because a detached HEAD names no change. Every higher exit raises, so an unreadable HEAD
stops the check rather than reading as "no branch". The first version of this read answered _any_ non-zero exit with an
empty name, on the argument that `claim_file` had already established the repository; the third consultation showed that
argument checks a different invocation, and that the widening would also hide a contradiction between a claim and the
branch it belongs to.

### The staged-content read cannot skip its path

`git show :<path>` answers a path absent from the index with exit 128, the same code a fatal read produces, so the check
cannot tell the two apart — and it does not need to: it reads a path it has just seen staged, where a failed read means
the index changed under the check. The read therefore raises, and a changed index stops the guard instead of skipping
that path's marker scan as its `None` branch did.

### The file measurement fails closed too

`--large-files` measured each tracked path with `stat()` and skipped a path it could not measure, so its success line
could follow a file that was never measured. The owner decided to close that here rather than park it, so the mode's
rule now matches the git reads': a path absent from the worktree is measured from its index blob — what a commit would
carry, and a normal state for a file deleted but not yet staged — while any other failure to measure raises
`FileFailure`, which `main` reports as the check having failed. `FileFailure` and `GitFailure` share a `CheckFailure`
base, so one catch covers both.

## Accepted false negatives

- **A read that succeeds with wrong content.** The rule covers a read that fails; a command that exits zero while
  reporting nothing is indistinguishable from a clean tree, and no check here can tell the two apart.

## Claims table

Every row's source reproduces at the commit this change was authored against; the pre-change rows use `git grep` at that
commit, since the working tree no longer shows the state they describe.

- The twelve Context bullets name the thirteen reads that parsed stdout unguarded: bullet 5 (`unstaged_spec_paths`)
  covers two commands, `diff` and `ls-files`. The deriving command prints 18 lines —
  `git grep -n "git(repo" c93607bd -- scripts/commit-hygiene.py` — of which five are excluded: the helper's declaration,
  its own internal call, and the three revision reads that already answered by exit code.
- `git_output` and `GitFailure` already exist, added by the manual-pass readiness change —
  `grep -n "class GitFailure\|def git_output" scripts/commit-hygiene.py`.
- `git grep` answers "no match" with exit 1 and the marker read parses its stdout as the offender list —
  `git grep --help` (the "exit status is 0 if a match is found, 1 if not" line) and `find_conflict_markers`.
- The checker's tests run in `check` through `testCommitHygiene` — `build.gradle.kts`, the `check` block's
  `dependsOn("testCommitHygiene")`.

## Alternatives rejected

- **Make `git` itself raise.** Rejected: the answer-by-exit sites the Decisions section names legitimately ask questions
  whose answer is a non-zero exit, and a global raise would push each of them into an exception handler that reads worse
  than the branch it has.
- **Check the exit inside each `check_*` function.** Rejected: the same twelve reads are reached from six entry points;
  one raise at the read plus one catch in `main` states the rule once.
- **Report the failure as an offender of the mode.** Rejected: an offender list means "this path is wrong", and a failed
  read is "the check did not run" — conflating them would let a caller believe a specific path was inspected.
