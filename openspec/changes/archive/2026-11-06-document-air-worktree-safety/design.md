# Design — keep Air-hosted units on the repository's worktree process

## Context

- PR #853 adopted JetBrains Air as one of the two ways to reach the IDE's MCP server, and its design assigned the
  procedure to `.opencode/skills/setup-agent-tools/` and the integrations' names to the README.
- The owner's concern is narrower than "an agent can damage files": it is **worktree correctness** — that Air's default
  capabilities cannot break adherence to this repository's worktree process.
- The two processes differ at the root. This repository's unit owns one tree — the primary checkout while it is free,
  otherwise a sibling worktree created and prepared by `scripts/new-worktree.sh` — named by `docs/units/<unit>/unit.md`,
  with review-stage work deliberately uncommitted. Air's worktree mode creates its own branch and a tree under
  `~/.air/worktrees/temporary/` that no record names; in the two recorded disposable-clone runs on IDEA 2026.3 it
  created clean checkouts at the source commit, and the source retained all its staged, unstaged, and untracked work.
- The premise was verified against the bundled plugin and the product rather than from memory: the strings below come
  from `air-plugin` for IDEA 2026.3, the worktree behaviour from a disposable exercise on the same build
  (`docs/decisions/2026-11-06-air-worktree-exercise.md`), and the guard's behaviour from its own source. The claims
  table records each with its source.

## Decisions

### D1 — The rule sits where the tree is established, the procedure in the skill

- The warning goes into `AGENTS.md`'s Workspace rule because that is where an agent establishes a unit's tree and reads
  the occupancy rules; an existing contributor may never rerun `/setup-agent-tools`, so the skill alone would not reach
  them.
- It is one bullet that names the choice and points at the skill, not a second worktree procedure: the skill owns the
  session selection, the version boundary, and the recovery steps.
- The skill gains the Air block: before-start rules, the accidental-mode recovery, and the tested-version boundary it
  already states for the rest of the recipe.

### D2 — No guard and no wrapper change

- `new-worktree.sh` cannot intercept a creation that never calls it; Air's worktree mode is its own path.
- The commit guard is a **consequence** check with boundaries: it resolves the claim from the current worktree's git
  directory, but a branch named after a live or archived change needs no claim, and the path/branch comparison applies
  to a **staged unit record** rather than to every commit.
- Extending the guard would therefore neither prevent Air's copy nor close the split; it would be an unrequested change
  to a gate whose contract is specified. The guidance states the boundary instead, and the exercise decides whether a
  guard change is owed.

### D3 — Version-qualified wording, no claim of a global off switch

- The Air strings are labelled UI text from one bundled build; another release can rename the mode or move it. The skill
  keeps the tested-version boundary it already applies. The `worktree.settings.disabled` state does not establish a
  user-facing switch, so the guidance never says worktrees can be turned off globally.

### D4 — Recovery preserves both trees and returns the decision to the owner

- A stray Air tree is recoverable only while its unique files and commits exist; the guard preserves nothing, and Air's
  deletion removes its branch. The procedure therefore stops the work, forbids destructive steps, finds both trees in
  the registry, preserves what is unique, and asks the owner.
- Making the stray tree pass the guard — adding a claim or rewriting the record — would hide the split rather than
  reconcile it, so the procedure forbids it.

## Non-goals

- Changing the guard, `new-worktree.sh`, the README, or the project permission block.
- Investigating the sandbox and read-only modes: they do not address the worktree lifecycle.
- Establishing a supported way to disable Air's worktree mode globally; the evidence does not show one.

## Claims table

| #   | Claim                                                                                                                                                                           | Source                                                                                                                                                             |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | Air's worktree mode is a per-session run mode labelled "New Local Worktree", described as running the agent in a new Git worktree                                               | `worktree.run.mode` and `worktree.run.mode.tooltip` in `AirWorktreeBundle.properties` of the bundled `air-plugin` (IDEA 2026.3)                                    |
| 2   | The creation flow promises to create the branch and copy the local project changes into that tree                                                                               | `worktree.creation.progress.details` and the "Saving/Applying local changes…" progress strings in the same bundle                                                  |
| 3   | A branch name or a detached checkout is required                                                                                                                                | `worktree.launch.branch.required`                                                                                                                                  |
| 4   | Air's automatic worktree cleanup is opt-in and its own enable dialog warns it can delete worktrees with uncommitted changes                                                     | `worktree.settings.temporary.auto.delete.checkbox.*`, `…auto.delete.warning`, `…auto.delete.confirm.dialog.*`                                                      |
| 5   | Deleting an Air worktree removes its branch; a worktree removed outside Air keeps it                                                                                            | `worktree.settings.remove.confirm.message` and `worktree.removed.outside.air.commits.kept.error`                                                                   |
| 6   | Sessions are grouped by project and worktree, and a folder can be attached to a window                                                                                          | `toolwindow.worktree.group`, `action.AgentThreads.TreePopup.CopyProjectPath.description`, `action.AgentThreads.AttachProject.*` in `AgentThreadsBundle.properties` |
| 7   | The commit guard resolves the claim from the current worktree's git directory                                                                                                   | `scripts/commit-hygiene.py` — the claim/assignment resolution                                                                                                      |
| 8   | A branch named after a live or archived change needs no claim                                                                                                                   | `scripts/commit-hygiene.py` — the assignment check's branch-name branch                                                                                            |
| 9   | The tree path/branch comparison applies to a staged unit record, not to every commit                                                                                            | `scripts/commit-hygiene.py` — the workspace-record verification over the staged set                                                                                |
| 10  | `new-worktree.sh` creates and prepares the unit's tree and refuses an occupied destination                                                                                      | `scripts/new-worktree.sh`                                                                                                                                          |
| 11  | The v2 surface offers no session-bound write control, v1 is unverified, and the property is recorded as not ensured — so the guards, not a prohibition, are the boundary        | `docs/adr/0024-tree-write-right-is-not-session-bound.md`, its Decision and Consequences                                                                            |
| 12  | PR #853 assigned the Air procedure to `.opencode/skills/setup-agent-tools/` and the integration names to the README                                                             | `openspec/changes/archive/2026-11-06-adopt-builtin-ide-mcp-server/design.md`, D5                                                                                   |
| 13  | In the two recorded disposable-clone runs on IDEA 2026.3, Air created clean checkouts at the source commit and the source retained all its staged, unstaged, and untracked work | `docs/decisions/2026-11-06-air-worktree-exercise.md`                                                                                                               |

## Dismissals

`python3 scripts/claim-scan.py --base origin/main` seeds the table above; its candidates are accounted for by kind:

- **`[path]`** — each candidate is a coordinate the sentence names, resolved by opening it or re-running the row's
  command.
- **`[count]`** — each count is a value the sentence sources from a row, or a coordinate into the unit's own records.
- **`[absolute]`** — each limit or absence is the subject of the decision that states it, bounded by the row or artifact
  it names.
- **`[cross-ref]`** — each names a file, task, or row the repository or this change holds.

## Verification

- The disposable exercise: in a throwaway clone with the guard armed and one marker in each of the staged, unstaged, and
  untracked classes plus one created after the IDE opened, start a fresh Air session in **New Local Worktree** twice,
  recording each time the clone's `git worktree list` and each tree's path, branch, index-versus-worktree state
  (`git status --porcelain`, `git diff --cached --name-status`, `git diff --name-status`), and marker contents. Both
  recorded runs created a clean checkout at the source commit and left every marker in the source project, so the result
  establishes what Air's tree holds rather than assuming the copy its dialog promises. The exercise is not run against
  this repository's own trees, and cleanup is never exercised on real work.
- The backstop check runs inside Air's own tree, with the guard armed in the clone before the session: record that
  tree's effective `core.hooksPath`, branch, `git rev-parse --git-path axon-showcase-change`, the absent claim, and
  `HEAD`; confirm the resolved hook file exists and a commit identity is set; ensure a path is staged (the copy's state
  is already recorded), then commit and capture the output. The refusal is expected to carry `No commit assignment:`
  naming that branch and that claim path, with `HEAD` unmoved. A pass with the guard armed is the signal to design a
  scoped guard change, not to write the claim, and a failure for another reason — including one that never reached the
  hook — is inconclusive.
- The change's gates: `spotlessApply`/`spotlessCheck`, `check -PskipITs -Pcoverage.gate.enabled=false`, and
  `openspec validate --all`.
