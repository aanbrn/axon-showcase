# Design

## Context

See `proposal.md` for motivation. The `commit-hygiene` checker already runs the guard's staged-set checks and two build
checks (`--markers`, `--tracked-ignored`), each wired into the root `check` task by an Exec task that resolves
`pythonExecutable`. Two classes remain uncovered: a merge conflict marker in a markdown/docs file survives the formatter
(Prettier rewrites `>>>>>>> branch` into a blockquote and passes), and a tracked hook or shell script without its
executable bit is silently skipped by git. Verified against the current tree: no tracked line starts with a conflict
branch marker, and every tracked `*.sh`, `gradlew`, and `scripts/git-hooks/*` file is `100755` while the vendored
`axon4to5-*` skills' `.sh` are `100644`.

## Goals / Non-Goals

**Goals:**

- Reject a conflict marker at both moments — the staged set (the hook) and the tracked set (the `check` gate).
- Verify the executable bit on every tracked file git runs directly.

**Non-Goals:**

- Flagging a lone `=======`: it is a valid setext heading underline, so only the unambiguous branch markers are matched.
- A `commit-msg` or `pre-push` hook, or any check beyond these two classes.

## Decisions

### Decision: Two modes in the checker, run by two root `check` tasks

`--conflict-markers` and `--executable-bits` join the checker, and `verifyConflictMarkers` / `verifyExecutableBits` join
`check`, mirroring `verifyTrackedIgnoredFiles` (an Exec resolving `pythonExecutable`, the script as an input,
`outputs.upToDateWhen { false }`).

- **Alternative — Kotlin tasks in `build-logic`:** duplicates the git logic the checker already holds.

### Decision: Match only the unambiguous branch markers, over the index and the tracked tree

The conflict-marker check greps `^(<<<<<<< |>>>>>>> )` with `git grep -I` — the index for the staged moment, the tracked
tree for the build moment — and the guard's `--staged` mode gains the same check so the hook refuses the commit; that
extends the guard requirement's defect-class list, so the delta modifies that requirement rather than only adding new
ones. A lone `=======` is not matched.

- **Alternative — match `=======` too:** false-positives on every setext heading underline.
- **Alternative — reuse the tracked check in the hook:** the hook must inspect the staged content, not the working tree.

### Decision: The executable-bit check targets every tracked file git runs directly, root-anchored

It filters `git ls-files -s` (whose paths are root-relative) to the hook files (`scripts/git-hooks/*`), the shell
scripts (`*.sh` at any depth), and `gradlew`, excluding the vendored `axon4to5-*` and generated `openspec-*` skill
subtrees — copied verbatim or generated, not run — and reports any whose mode is not `100755`. Matching is anchored to
the path's leading segment, not a right-anchored suffix match, so a vendored `.../scripts/foo.sh` is not caught by the
`scripts/` rule.

- **Alternative — the narrow `scripts/git-hooks/*` and `scripts/*.sh` set:** misses `db.sh`, `setup-hosts.sh`,
  `showcase-web-ui/start.sh`, and `gradlew`, which the repo runs directly.
- **Alternative — every tracked `*.py`:** not run directly (the checker and its tests are invoked through `python3`;
  `ensure-idea-settings.py` is `100755` but invoked as `python3 …` by `setup-idea.sh`), so the mode is not load-bearing.

## Risks / Trade-offs

- **A doc that quotes a conflict marker at line start** (e.g. a fenced example) → it would be flagged; indent or escape
  it, as the current tree does (the one mention in `AGENTS.md` is inline). A future need to show a marker verbatim is a
  prompt to revisit the pattern, not to drop the check.
- **`git grep` over the tracked tree** → bounded by the repository size, and the check runs only in `check`.
