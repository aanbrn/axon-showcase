# Design

## Context

See `proposal.md` — Why. What shapes the approach:

- The guard is `check_staged(repo, formatter)` in `scripts/commit-hygiene.py`; it reads the staged set through
  `staged_paths(repo)`, which is `git diff --cached --name-only -z --diff-filter=ACMR` — additions, copies,
  modifications and renames, **not** deletions.
- The archive pairing already exists (`find_unpaired_archive_sync`), and it derives archived changes from the staged
  _added_ paths under `openspec/changes/archive/`, never from git's rename detection.
- ADR-0024 records the write prohibition as unensured, and lists "a staged-set ownership check" among its **rejected
  alternatives** — "it sees the consequence after the write, and name-based attribution breaks on `opencode/…` branches
  and on archive moves". It was rejected as a substitute for the prohibition, which is the reading this change acts on:
  the recorded claim supplies the attribution a branch name cannot (the `opencode/…` continuation), and comparing change
  identity answers the archive move without rename detection.
- The ownership record `AGENTS.md` requires (`Workspace: unit=…; path=…; branch=…; writer=…`) lives inside the unit's
  own artifacts (`tasks.md`, else the hand-off and the pull-request checklist), so a program running at commit time
  cannot read it.
- A worktree's git directory is per-tree: the primary resolves `.git`, a linked worktree resolves
  `<common>/.git/worktrees/<name>`. The hook is installed per clone by pointing `core.hooksPath` at `scripts/git-hooks`.

## Goals / Non-Goals

**Goals:**

- Refuse a staged set that carries another change's directory, including the case where it is the only one staged.
- Establish the assignment from a recorded source before the staged set is read, and stop when it cannot be established.
- Keep the legitimate cases working: the assigned change's own directory and its archive move, and a unit that declares
  it works without a change dir.

**Non-Goals:**

- Proving who wrote a file, forbidding the write, or resisting a _spoofed_ assignment: the branch and the claim are
  mutable declarations, so a unit that renames its branch after a foreign change can still commit that change's
  directory. ADR-0024 owns the write gap; these artifacts must not read as ownership proof either.
- Seeing a foreign edit _inside_ the assigned change's directory or in another file of the unit.
- Judging paths outside `openspec/changes/`.
- Cleaning up orphaned directories: a name mismatch does not prove abandonment, and the guard's contract forbids
  changing the working tree or the index.
- Requiring a worktree for every unit: that variant was rejected on cost and IDE grounds in `adopt-worktree-isolation`,
  and it separates files rather than sessions.

## Decisions

- **The assignment is read from a recorded source, never inferred from the staged set.**
  - The **branch** names the assignment when its name is a change name — one that has a live `openspec/changes/<name>/`
    directory **or** an archived `openspec/changes/archive/<date>-<name>/` one. The archived case is what makes the
    guard's own ship path work: `openspec archive` moves the directory before the commit, so a resolution keyed on the
    live directory alone would refuse the archive commit itself.
  - Otherwise a **claim** names it — this covers `opencode/…` continuations and any branch not named after its change —
    and the literal `none` declares a unit that works without a change dir. The claim holds one line: a change name, or
    `none`.
  - When both name a change and disagree, the check fails: a contradiction is a finding, not a precedence question.
  - Alternatives rejected: an environment variable (a plain `git commit` from another shell loses it); a tracked file
    (it would be committed, shared across trees, and would itself need ownership); deriving from the staged set (a
    single foreign directory would become "expected", which is the failure being prevented).
- **The claim lives at `git rev-parse --git-path axon-showcase-change`.** One fixed path component, so no artifact has
  to guess a name and it cannot collide with git's own entries. That resolves to the per-worktree git directory — `.git`
  in the primary, `.git/worktrees/<name>` in a linked worktree — so two units cannot share a claim, and the file is
  untracked by construction. Resolving through `--git-path` keeps working when the common directory is not named `.git`.
- **Deletions and rename sources under `openspec/changes/` are read too.** `staged_paths` excludes deletions, and a
  rename reports only its **new** name, so a deletion read and a rename-source read join it: together they catch a
  foreign change's directory removed, or a file of it moved into the assigned change. The assigned change's own
  deletions pass, since this check judges whose directory a path belongs to and not whether the change keeps an
  artifact.
- **Archive transitions are compared by change identity, not by rename detection.** The added
  `openspec/changes/archive/<date>-<name>/` path and the deleted `openspec/changes/<name>/` path must both carry the
  assigned change's name; git may report either an add or a rename, and neither shape changes the verdict.
- **A continued unit records the claim when it establishes its workspace.** A branch that is not a change name — an
  `opencode/…` continuation, a head pushed from elsewhere — has no branch-derived assignment, so its workspace step
  writes the claim before its first commit. That is the bootstrap: no separate migration, and the guard's failure
  message says exactly which line to write.
- **The claim is removed with the tree, by the procedure that owns teardown.** The primary's claim is removed when the
  tree is released to a clean `main` (`ship-change`'s settle step); a sibling worktree's claim lives in its own git
  directory and disappears with `git worktree remove`, so no one clears a neighbour's claim from outside.
- **The check runs in the hook only.** The `check` task has no assignment to read, so it keeps its current invocation;
  the guard's test suite lives in `check` as it already does.

### Requirement amended

The delta replaces the whole `A pre-commit guard blocks a mechanically defective staged set` requirement. No existing
clause is reworded; one paragraph is added after the existing description, and nine scenarios are appended after the
existing ones in their order.

| Clause                | Was    | Now                                                                                                                                                                                            | Why                                                                                                                                           |
| --------------------- | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| Assigned-change check | absent | "The hook SHALL also refuse a staged set that carries another change's directory. Before it reads the staged set it SHALL establish the commit's **assigned change** from a recorded source …" | The guard verified the staged set's mechanics and its archive pairing; nothing related the staged change directories to the commit's own unit |

## Risks / Trade-offs

- **A foreign edit inside the assigned change's directory stays invisible** → accepted: the check is about directory
  ownership, and saying so is what keeps it from being read as authorship proof.
- **`git commit --no-verify` bypasses the hook** → existing contract, restated by the guard's own requirement; CI does
  not replace it either, since `check` cannot know the assignment.
- **A forgotten claim blocks commits** → the release step removes it, the failure message names the path, its expected
  content and the two sources, and the claim is optional on a branch named after its change, which is the common case.
- **A clone that never installed the hook is unaffected** → the same as today: the guard is per-clone, installed by
  `scripts/install-git-hooks.sh`.

## Migration Plan

None: the check is additive and runs inside the hook. Existing clones get it on their next hook invocation; no ledger,
no data, no `check` wiring changes.

## Claims table

| Claim                                                                                                      | Source or command                                                                                      | Reproduces |
| ---------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ | ---------- |
| The guard's staged read excludes deletions                                                                 | `scripts/commit-hygiene.py`: `staged_paths` runs `git diff --cached --name-only -z --diff-filter=ACMR` | yes        |
| Archive pairing already derives changes from staged additions, not renames                                 | `find_unpaired_archive_sync` and `_staged_archived_changes`                                            | yes        |
| A linked worktree's git directory is per-tree                                                              | `git -C ../axon-showcase-pause-resume-unit rev-parse --git-dir` → `<common>/.git/worktrees/<name>`     | yes        |
| The hook installs through `core.hooksPath`                                                                 | `git config --get core.hooksPath` → `scripts/git-hooks`                                                | yes        |
| The workspace record currently lives in the unit's own artifacts                                           | `AGENTS.md`'s `Workspace` rule, "it lives with the unit's own record"                                  | yes        |
| ADR-0024 rejects the staged-set check as a prohibition substitute and records the prohibition as unensured | `docs/adr/0024-tree-write-right-is-not-session-bound.md`, "Rejected alternatives" and Decision         | yes        |
