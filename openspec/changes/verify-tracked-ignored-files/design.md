# Design

## Context

See `proposal.md` for motivation. The pre-commit guard's force-staged-artifact check inspects the staged set, so it
catches a generated artifact only on a clone that activated the hook. Nothing verifies the **tracked** set: a file
force-added and committed stays tracked, and neither `spotlessCheck` nor `verifyCapturedMarkers` looks for it. The root
`check` task already runs Exec tasks that invoke the Python checker (`verifyCapturedMarkers`, `testCommitHygiene`), and
the checker already computes `git ls-files --cached --ignored --exclude-standard` for the staged case.

## Goals / Non-Goals

**Goals:**

- Gate the tracked set in `check`, so a committed ignored artifact fails the CI `build` gate for every PR, not only a
  clone with the hook active.
- Keep one implementation of the git logic (the checker), matching the existing root-check pattern.

**Non-Goals:**

- Replacing the guard's staged-set check: the two cover different states (the staged set before a commit, the tracked
  set at the gate).
- Checking working-tree-only ignored files (untracked ignored files are normal and out of scope).

## Decisions

### Decision: A `--tracked-ignored` mode in the checker, run by a root `verifyTrackedIgnoredFiles` task

The checker gains a mode that reports every path `git ls-files --cached --ignored --exclude-standard` lists; a root Exec
task runs it and `check` depends on it, mirroring `verifyCapturedMarkers`.

- **Alternative — a Kotlin task in `build-logic`:** duplicates the git logic in a second language, which the existing
  checker/task split avoids.

### Decision: Verify the tracked set, not the staged set

In a CI checkout the index is `HEAD`, so `git ls-files --cached --ignored --exclude-standard` names every tracked file
matching an ignore rule — the committed state the merge gate must reject. The guard's staged check stays for the
pre-commit moment.

- **Alternative — re-run the staged check in CI:** a CI checkout has nothing staged, so it would verify nothing.

## Risks / Trade-offs

- **A file tracked on purpose despite matching a pattern** → the check flags it and the remedy is `git rm --cached` plus
  an ignore entry (the check's intent); such a file is a defect in this repository, whose ignore rules cover generated
  artifacts.
- **The check needs git** → `check` runs in a checkout, and the existing checker already shells out to git.
