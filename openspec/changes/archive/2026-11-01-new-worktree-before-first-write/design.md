# Design

## Context

The Workspace rule makes a created tree's preparation a second step after creation, and its deadline names an act the
agent cannot observe — opening the tree. Two records from 2026-11-01 (`bump-openspec-cli#1` and `bump-buildpacks#1`)
show the preparation omitted, and a third (`refresh-upstream-references#1`) shows the setup run before the
`.python-version` inheritance; the idea parked from them names the stronger fix: a wrapper that creates and prepares the
tree in one operation. The archived `auto-setup-new-worktree` design had rejected a wrapper because a caller could
bypass it; the recurrence since then is what reopens the decision, and this change keeps the bypass limit explicit
rather than claiming prevention.

## Goals / Non-Goals

**Goals:**

- Make the supported creation path create, inherit, prepare and verify a tree in one invocation that fails loudly.
- Move the guidance deadline to the first unit-content write, so a miss is recognizable.
- State the readiness outcome in `ide-config`, the capability that already owns created-tree interpreter inheritance.

**Non-Goals:**

- Any interception that would observe a premature write independently of the caller (a pre-commit check, a
  `post-checkout` hook, a doctor probe, an OpenCode hook). This unit ships none; the archived design already rejected
  the hook for its broader trigger, and the others observe later state than the harm.
- Changing the setup script's own contract: standalone runs keep their `SKIPPED`-with-zero-exit behavior.
- Automating the settle step, `session_move`, or the merge.

## Decisions

1. **Create and prepare in one operation.** The ordinary creation and preparation instructions in the Workspace bullet
   are replaced by the wrapper's invocation rather than left beside it, so the supported path has one step.
2. **Conditional readiness.** Without `.idea/` in the primary the wrapper creates the tree and reports that no IDE
   preparation was owed; with `.idea/` it requires a successful setup run **and** a `Python: CONFIGURED` stage line.
   Standalone setup keeps `SKIPPED` as a legitimate zero-exit outcome.
3. **A failed preparation keeps the tree.** The wrapper reports the tree's path, the stage lines it captured, and the
   remedy; it never deletes or recreates the tree, so a recoverable failure never costs the checkout.
4. **The primary is resolved independently of the caller's cwd**, from the tree that owns the script, so an invocation
   from any working directory targets the right checkout.
5. **A `--continue` branch carrying `/` gets a sanitized sibling destination**
   (`../axon-showcase-<branch-with-dashes>`), since the branch name cannot be a path.
6. **"First write" is the first unit-content edit after initialization** — not the checkout, not the setup's own writes,
   and not the workspace record the unit writes about itself.
7. **POSIX `sh`** for both the wrapper and its tests; the build registers shell tests through `shExecutable`, and the
   wrapper drives only `git`, `cp` and the setup script.
8. **`ide-config` carries the delta, not `agent-skills`.** That capability already owns created-tree inheritance; its
   requirement gains the first-write boundary and the unsuccessful-preparation outcome, and states no filename.
9. **Claims stay bounded**: sequencing automation when invoked, not a measured reduction in omissions.

## Risks / Trade-offs

- **A caller can bypass the wrapper.** The failure mode is unchanged for a caller that runs `git worktree add` directly;
  the guidance and the review gate are what make the wrapper the path the agent takes.
- **The plugin install can be legitimately skipped** while the IDE runs; the wrapper's readiness threshold is a
  successful run plus the Python stage, so a failed run blocks even when that stage printed `CONFIGURED`.
- **A primary without `.idea/`** creates an unprepared tree by design; the report says so rather than failing.

## Verification

- `sh scripts/test-new-worktree.sh` and `NEW_WORKTREE_SHELL=dash dash scripts/test-new-worktree.sh` (the build runs the
  suite through `sh`; the second run drives the wrapper itself under `dash`).
- A live run in a throwaway clone: a new tree is created, `.python-version` inherited, and the Python stage reported.
- `./gradlew spotlessApply`/`spotlessCheck`, `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`, and
  `openspec validate --all`.

## Claims

| Claim                                                   | Source                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| ------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The wrapper's governed behaviors pass under both shells | `sh scripts/test-new-worktree.sh` and `NEW_WORKTREE_SHELL=dash dash scripts/test-new-worktree.sh`, the second driving the wrapper itself under `dash`, → "All new-worktree controls passed."; the controls cover the success and nothing-owed paths (new tree, no upstream on the branch, a newly pushed `origin/main`, an alternate cwd, the sanitized `--continue` destination, a remote-only `--continue` branch, a tree's own selection kept, a primary without `.idea/`), the readiness refusals (`SKIPPED`, a failing run that retains the tree), the destination refusal, the git refusals (a failed fetch, a branch checked out elsewhere) and the usage errors |
| The controls can fail                                   | Each control was proved by removing its mechanism — readiness forced to yes, the inheritance `cp` removed, the fetch or its guarded `exit 2` removed, the primary resolved from the caller's cwd, `--no-track` dropped, the `worktree add` guard removed — which made the suite exit non-zero with that control's `FAIL` line, and restoring the file returned it green                                                                                                                                                                                                                                                                                                 |
| A live run creates and prepares a tree                  | `./scripts/new-worktree.sh live-probe` → `Tree ready: …/axon-showcase-live-probe (live-probe)`, exit 0, branch `live-probe`, `.python-version` `3.12.16`; the probe tree and branch were removed afterwards                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| The build runs the suite inside `check`                 | `./gradlew testNewWorktree` → passes; `build.gradle.kts` registers the task and `check` depends on it                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| The delta is valid and carries the new outcomes         | `openspec validate new-worktree-before-first-write --type change` → valid; the requirement's 7 scenarios are carried over and 3 are added                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| No live guidance still sets the opened-tree deadline    | `grep -rn "before it is opened" AGENTS.md .opencode` → no match                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| The parked idea is removed by this change               | `grep -n "Move the created-tree preparation" docs/ideas.md` → no match                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| The gates pass                                          | `./gradlew spotlessCheck` and `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false` → BUILD SUCCESSFUL; `openspec validate --all` → `0 failed`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| The wrapper does not claim prevention                   | `proposal.md` and `design.md` state sequencing automation when invoked; no row claims a measured reduction in omissions                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |

**Dismissals.** `scripts/claim-scan.py --base 1e4e8759` fires on shape rather than on claimhood, so each kind carries
one reason rather than a list of its candidates: `[path]` marks a file, directory or command mention, which the gate
that reads it or a run of the command resolves; `[count]` marks a numeral, which the row or task line that states it
derives; `[absolute]` marks a quantifying word, which a claims row carries when it spans a corpus and a task imperative
does otherwise; and `[cross-ref]` marks a repository path, an issue number, or a package scope, each resolved by the
lookup it names. A candidate is a claim only where a claims row states it, so the kinds are not enumerated member by
member here.
