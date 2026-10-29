# Proposal

## Why

A markdown table row wrapped onto a second line becomes a spurious row, and a bare `|` inside a cell splits one row into
several columns — and Prettier treats both as its fixpoint, so `spotlessCheck` stays green while the rendered table is
wrong. Reproduced twice: a wrapped row during `adopt-worktree-isolation`'s review, and a bare `|` in a command cell that
split three claims rows into five-to-seven columns during `extend-owner-reply-language-scope`. No gate counts columns.

## What Changes

- `scripts/commit-hygiene.py` gains a `--table-rows` mode over the markdown the Spotless format owns: for each
  recognized table, every row carries the delimiter row's count of cells, so a wrapped row and a split cell both fail
  with the file and line.
- `build.gradle.kts` registers a `verifyTableRows` task beside `verifyConflictMarkers`, passing the Spotless markdown
  target's files so the scope has one owner, and `check` depends on it.
- `scripts/test-commit-hygiene.py` gains controls: a wrapped row, a split cell, an escaped `\|`, a fenced diagram, a
  clean table, and a run given no paths.
- The `commit-hygiene` spec gains the requirement for the check and its scenarios.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/commit-hygiene`: one added requirement — a markdown table's rows carry the delimiter's column count.

## Impact

- `scripts/commit-hygiene.py`, `scripts/test-commit-hygiene.py`, `build.gradle.kts`, and the delta for
  `showcase/quality/commit-hygiene`.
- No JVM production code, deployment, or API change; the check adds no dependency (stdlib only) and fails the build
  rather than a commit.
