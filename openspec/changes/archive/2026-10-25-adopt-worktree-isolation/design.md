# Design

## Context

See `proposal.md` — Why. The change-lifecycle workflow (`AGENTS.md`, "Workflow: Change lifecycle") assumes one working
tree: the unit's branch is created in the primary checkout and the work stays there uncommitted until approval. Two
constraints follow from that assumption:

- The primary checkout is also the folder the owner's IDE project is attached to and the one they work in by habit, so
  it is the default workspace for the current unit, not a checkout parked on `main`.
- A second unit that starts while the primary holds an in-flight unit has no owned workspace. In this session it wrote a
  scratch test into the occupied tree, and the owner stopped it before anything else landed.

The parked idea (`docs/ideas.md`, 2026-10-10) chose a per-unit `git worktree` to remove the seam, with a sibling path
template (`../axon-showcase-<unit>`) and a historical measurement: ~628 MB per tree after its first Docker-free `check`,
a one-time ~1 min first build, and an IDE setup step where a tree is opened in the IDE, because `.idea/` is git-ignored.
The hooks need no per-tree setup: `core.hooksPath = scripts/git-hooks` lives in the common config
(`git config --get core.hooksPath`).

## Goals / Non-Goals

**Goals:**

- Give every active unit a recorded working tree, so a unit finding another unit's tree assigned — or an ownership it
  cannot establish — chooses its own before writing.
- Keep the sequential path cheap: the primary checkout stays the sequential unit's workspace.
- Make the ownership check and the cleanup mechanical enough that a reviewer can verify them from the record.
- Cover the surfaces that state the lifecycle, so the commands and the workflow describe one process.

**Non-Goals:**

- Moving builds, dependencies, or IDE configuration out of the primary checkout.
- Vendoring a worktree skill, adding a CLI, or changing the GitHub workflow, the ledger format, or the review gate's
  stages.
- Making the harness worktree tools mandatory (their contract is unverified — see D7).

## Decisions

**D1: Hybrid, not per-unit worktrees for every unit.** The primary checkout is the sequential unit's tree; a unit that
finds the primary assigned to another unit works in a sibling worktree. _Alternatives:_ a worktree for every unit
(rejected: it pays the historical per-tree cost and the IDE/setup step on every sequential unit, while the workspace
preflight it would still need is the actual safeguard); the all-in-one container directory used this session (rejected
by the owner; the container outlived the tree it held).

**D2: Sibling layout `../axon-showcase-<unit>`.** The parked idea's template, and it keeps the trees outside the primary
checkout's clean/stash surface. _Alternatives:_ nested `<primary>/.worktrees/<unit>` (needs a `.gitignore` entry the
repository does not have, and puts live trees inside the directory a careless `git clean`/`git stash` runs in); an OS
temp directory (outside the repository's conventions and not covered by the cleanup step).

**D3: Occupancy is read from the Git registry, and an unestablished ownership counts as occupied.** A tree with a unit
branch checked out belongs to that unit whether the tree is clean or dirty and whether the branch is merged or awaiting
review; a tree with an unknown branch, a detached HEAD, or evidence contradicting the registry is occupied until the
owner resolves it. A tree becomes free only through the standard release (D6) and a clean `main` checkout — the `main`
checkout is the marker that the transition happened, never on its own proof that the tree is safe. The states and their
transitions are the table below. _Alternatives:_ treat a clean tree as free (rejected: a unit's review tree can be
clean); treat an unknown owner as free (rejected: it is the collision this change exists to prevent).

### Working-tree assignment, release, and cleanup

| State                                                                               | Who may write                | How it ends or is released                                                                                                                                                                                                                                                           |
| ----------------------------------------------------------------------------------- | ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Continuing unit: the tree on its branch, its record matching unit, path, and branch | that unit's session          | the unit ships; see the cleanup rows                                                                                                                                                                                                                                                 |
| Tree on a unit branch whose unit has not completed                                  | no other unit's session      | that unit completes; the tree is never taken over mid-flight; the owner's own edits in it are the unrelated work a preflight preserves                                                                                                                                               |
| Unknown branch, detached HEAD, or evidence contradicting the registry               | no unit's session — occupied | the unit establishes its own tree (the primary only when its previous unit released it, a sibling otherwise); the owner resolves the tree's ownership and the record gains the resolved owner; no unit removes or takes it over                                                      |
| Primary released by its previous unit                                               | the next sequential unit     | a new branch in the same directory; the directory is never removed. Released means the previous unit completed (merged, or rejected by the owner), no work of it remains (extra commits and unique local data included), no writer continues, and the tree is back on a clean `main` |
| Linked worktree, unit shipped and merged                                            | nobody                       | verify no unique work remains, including ignored files → move the session out → `git worktree remove` (no `--force`) → `git branch -D` after verifying that unit's PR merged with no unshipped commits                                                                               |
| Unit rejected or abandoned                                                          | nobody                       | delete its branch per the existing rule; a sibling is removed after verifying nothing unique remains; the primary is released through the same path, back to a clean `main`                                                                                                          |

The primary is released only when its unit has completed and no work of it remains, and returning the tree to a clean
`main` is the release's last step; a clean `git status` alone neither releases it nor proves it safe.

**D4: Creation runs against an explicit primary checkout.**

```bash
git -C <primary> fetch origin
git -C <primary> worktree add --no-track -b <unit> <primary-parent>/axon-showcase-<unit> origin/main
```

_Alternatives:_ create relatively from the current directory (rejected: once the session is inside a worktree, the
relative path and the "primary" notion diverge); the harness's `worktree_create` (see D7).

**D5: A recorded preflight before the first write.** The agent establishes the unit's assignment and records it as
`Workspace: unit=<unit>; path=<absolute-path>; branch=<branch>; writer=<session>`. The record is context and evidence of
the assignment — it states what was established and never substitutes for reading the registry (D3) — and it lives with
the unit's own record: its `tasks.md` when it has a change dir, otherwise its hand-off or report and then the
pull-request checklist. No shared state file is introduced. If the tree belongs to another unit, or the ownership cannot
be established, the agent creates the sibling, moves the session (`session_move`), and re-checks the path and branch.
The record is also how the owner reaches a sibling tree's diff, since the owner's review pass otherwise runs against the
primary checkout their IDE project is attached to, not the sibling the session wrote in. _Alternative:_ a check with no
record (rejected: neither the owner nor a reviewer can verify afterwards which tree the unit claimed).

**D6: Cleanup differs by tree kind; a shipped unit's cleanup runs in `ship-change` after the merge.** A linked worktree
is removed and the primary is left alone: verify no unique work remains (ignored files included), move the session out,
`git worktree remove` without `--force`, then `git branch -D` only after confirming that unit's PR merged with no
unshipped commits. A unit that ran in the primary never removes it — it is released: confirm the unit completed (merged,
or rejected by the owner), no work of it remains (extra commits and unique local data included), and no writer
continues, then return the tree to a clean `main` — the release's last step — before handing the directory to the next
unit. A unit rejected or abandoned before shipping takes the failure-handling path instead: its branch is deleted per
`AGENTS.md`'s rejection rule, a sibling is removed only after verifying nothing unique remains, and the primary is
released (task 1.6). _Alternative:_ leave trees until `git worktree prune` (rejected: a stale tree keeps its disk cost
and reads as an active unit).

**D7: No vendored skill, no Worktrunk, no new CLI.** The registry is `git worktree list --porcelain`; the harness's
`worktree_list` was stale in this session (it listed a removed tree), so it is not the source of truth. _Alternatives:_
vendoring `davidondrej/skills`, `iliaal/ai-skills`, or `matt-riley/agent-skills` (the verbatim-copy rule needs a
verified licence and pinned revision; no licence text was fetched for the first two, and `matt-riley` declares GPL-3.0);
Worktrunk (a new prerequisite whose own merge pipeline duplicates the merge step); the harness worktree tools (their
path/base/tracking contract is unverified, and a mandatory tool with an unverified contract is worse than raw Git); a
manager script (would belong under `scripts/`, which is not needed for four documented commands).

**D8: One source per record; integration stays serial.** Each unit appends its `docs/review-rounds.tsv` rows and edits
docs in its own tree and ships them with its PR; a second merge resolves the overlap by keeping both sides and
re-running the existing ledger checks. _Alternative:_ a shared writable ledger or a union merge (rejected: a second
source of truth for the record the review gate owns).

**D9: The spec delta modifies two existing requirements.** The command-preflight requirement gains the workspace clause
and its scenario; the quick-review requirement's hand-over field set gains the unit's assigned working tree and branch
(the field set's owner, which `review-gate` points at rather than restating). No new capability is introduced.
_Alternative:_ a new capability for the workspace rule (rejected: it would split one obligation across two specs, and
the preflight requirement already owns what a mutating command must state).

**D10: A unit that continues an existing branch takes a tree on that branch, not a new one.** A cloud agent's
`opencode/…` branch or a head pushed from elsewhere already carries the unit's work, so the tree is created on it
(`git -C <primary> worktree add <path> <branch>`, git's DWIM creating the tracking branch when only `origin/<branch>`
exists) and a busy primary is never switched to it. _Alternative:_ branching from `origin/main` for every unit
(rejected: it discards the existing branch and its pull request, the case the cloud-PR procedure exists for).

### Amended requirement clauses (`showcase/quality/agent-skills`, `MODIFIED`)

| Clause today                                                                                                                                                  | Replacement                                                                                                                                                                                                                                                                                                                                                                                                                                                         | Why                                                                                                                                                                                  |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| "…state the lifecycle its unit owes: its own branch from a freshly fetched `origin/main`; unrelated dirty changes preserved; …"                               | "…state the lifecycle its unit owes: its workspace — the working tree assigned to the unit, the primary checkout when no other unit holds it and a sibling `../axon-showcase-<unit>` worktree otherwise, established before its first write, with an ownership it cannot establish treated as occupied; its own branch from a freshly fetched `origin/main` for a new unit, or the existing branch for a unit continuing one; unrelated dirty changes preserved; …" | The observed failure was workspace ownership, not branching; the command's preflight is where the agent chooses where to write.                                                      |
| No scenario for the workspace                                                                                                                                 | New scenario "A mutating command states the workspace preflight": the tree is established before the first write, a sibling is named when the primary belongs to another unit, and an unestablished ownership is treated as occupied                                                                                                                                                                                                                                | Each stated obligation is exercised by a scenario; the workspace clause would otherwise be unverified.                                                                               |
| "its own branch from a freshly fetched `origin/main`" (description)                                                                                           | "…for a new unit, or the existing branch for a unit continuing one"                                                                                                                                                                                                                                                                                                                                                                                                 | A unit continuing a branch or pull request must not be given a new branch, which would discard its work and its PR (D10).                                                            |
| Scenario "A mutating command states the branch rule": "the unit starts on its own branch from a freshly fetched `origin/main`"                                | "a new unit starts on its own branch from a freshly fetched `origin/main`, and a unit continuing an existing branch or pull request works on that branch"                                                                                                                                                                                                                                                                                                           | The scenario is narrowed to a new unit and gains the continuation obligation; a scenario may be edited, never renamed.                                                               |
| Scenarios "preserves unrelated work", "work stays uncommitted", "session preflight"                                                                           | Retained verbatim, in their order                                                                                                                                                                                                                                                                                                                                                                                                                                   | A `MODIFIED` block replaces the whole requirement; none of these obligations changes.                                                                                                |
| "…the unit's identity, its base and current revisions, the supplied artifacts, the scope, the prior findings…" (quick-review requirement, handover field set) | "…the unit's identity, its base and current revisions, the unit's assigned working tree and branch, the supplied artifacts, the scope, the prior findings…"                                                                                                                                                                                                                                                                                                         | A reviewer or a replacement reviewer must be able to read the tree the unit actually wrote in; the field set is the spec's, and `review-gate` points at it rather than restating it. |

## Risks / Trade-offs

- [The preflight is procedural; an agent can ignore it] → the record is written before the first write and is visible in
  the unit's report, and an ownership the record cannot establish reads as occupied rather than free; a recurrence of a
  foreign-unit write after this lands reopens the decision in favour of a technically enforced check.
- [Two parallel units both append to `docs/review-rounds.tsv` and edit shared docs] → the second merge resolves by
  keeping both sides, then `commit-hygiene.py --unique-rounds` and `--session-continuity` re-run; integration into
  `main` stays serial.
- [A tree holds work only the tree knows about, such as an ignored file] → the cleanup checks for unique work, ignored
  files included, before removal; `git worktree remove` without `--force` remains a second guard, not the check.
- [Per-tree disk and IDE setup] → the cost is paid only by parallel units; cleanup removes the tree after merge; the IDE
  question stays a per-tree setup step, not a gate.
- [A tree awaiting review is indistinguishable from an abandoned one] → both stay occupied until the unit completes or
  the owner rejects it; the registry cannot tell them apart and does not have to.
- [A stale or foreign worktree is mistaken for the unit's] → occupancy is read from the registry
  (`git worktree list --porcelain`: the checked-out branch), the record is evidence rather than the source, and an
  unknown ownership is occupied; a name alone is never the identification.
- [A unit's tree is removed with unsaved work] → `git worktree remove` runs without `--force`, and the branch delete
  verifies that unit's merge first.

## Migration Plan

1. The rule applies to units started after it lands; a unit already in flight stays in its tree and is not committed,
   stashed, or moved to satisfy the rule.
2. A new unit that finds the primary assigned to an in-flight unit starts its sibling, so no in-flight unit is
   interrupted.
3. The primary is released only when its unit has completed and no work of it remains; the next sequential unit then
   starts its branch in the primary.
4. Rollback is textual: the added clauses are removed and the parked idea is restored.

## Verification route

The rule's practical test cannot run inside this change: it needs two real parallel units, which exist only after the
change lands. Two parts are recorded separately so neither is lost with the archive:

- Available before completion: the rule's texts, the delta, and the command bullets state one obligation — checked by
  this change's tasks and the review gate.
- After landing, parked in `docs/ideas.md` with these completion criteria, each observed on two real parallel units:
  - each unit's tracked and untracked files are confined to its own tree;
  - the primary stays the other unit's workspace;
  - the recorded preflight names the right tree and branch;
  - a sibling carries no `origin/main` upstream;
  - the first unit's cleanup leaves the second untouched.

## Open Questions

None that change the specs, the approach, or the task breakdown.

## Claims table

| Fact the change states                                                                                                                              | Source / deriving command                                                                                                                                                                                   |
| --------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| A second unit's scratch test landed in the occupied primary and the owner stopped it                                                                | This working session's record (2026-10-25), labelled as the caller's account                                                                                                                                |
| The parked idea chose the sibling layout and reported, from a 2026-10-12 measurement, ~628 MB per tree, a ~1 min first build, and an IDE setup step | `git show HEAD:docs/ideas.md` (section `## 2026-10-10`)                                                                                                                                                     |
| The hooks need no per-tree setup                                                                                                                    | `git config --get core.hooksPath` → `scripts/git-hooks`                                                                                                                                                     |
| `.gitignore` carries no `.worktrees/` entry                                                                                                         | `git show HEAD:.gitignore`                                                                                                                                                                                  |
| Eleven project commands carry the `Repository lifecycle` block, ten of them mutating                                                                | `grep -rln "Repository lifecycle" .opencode/commands/` (11 paths); `grep -rln "starts no mutating unit" .opencode/commands/` → `consult.md` only                                                            |
| The current text and scenarios of both requirements the delta modifies                                                                              | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` (the command-preflight and quick-review requirements)                                                                                  |
| The harness worktree list is not authoritative                                                                                                      | This session: `worktree_list` reported a removed tree while `git worktree list --porcelain` did not, labelled as the caller's account                                                                       |
| The vendored-skill candidates' licences                                                                                                             | `matt-riley/agent-skills` frontmatter declares `license: GNU GPL v3` (the fetched `SKILL.md`); no licence text was fetched for `iliaal/ai-skills` or `davidondrej/skills`, so their terms are unestablished |
| The container directory was rejected by the owner, and it outlived the tree it held                                                                 | The owner's statement and this session's cleanup (2026-10-25), labelled as the owner's account and the caller's observation                                                                                 |
| Worktrunk's own merge pipeline duplicates the merge step, and the tool adds a prerequisite                                                          | The tool's documentation (`worktrunk.dev/config/`: `wt merge` squashes, rebases, validates and fast-forwards; the `[merge]` keys), fetched 2026-10-25                                                       |
| `.idea/` is git-ignored, so a tree opened in the IDE needs its own setup                                                                            | `git show HEAD:.gitignore` (the `.idea/` entry)                                                                                                                                                             |
| The primary checkout is the folder the owner's IDE project is attached to and the one they work in by habit                                         | The owner's account in this session (2026-10-25), labelled as the owner's account                                                                                                                           |

### Scan dispositions

`python3 scripts/claim-scan.py --base origin/main` reports candidates of four kinds over the implementation diff; none
states a fact about prior evidence outside the claims table. Each kind's reason:

| Kind      | Disposition                                                                                                                                                                                                                                        |
| --------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| path      | The sibling template, the `origin/main` refs, the command paths, and the hooks path are this change's own artifacts and the commands it writes; none cites prior evidence.                                                                         |
| cross-ref | The hits are pre-existing `AGENTS.md` text the diff reflows (`#212`), the change dir's own tokens (`IDE/setup`, `clean/stash`, `path/base/tracking`), `origin/main`, and command placeholders — nothing cites a repository artifact without a row. |
| absolute  | `never`, `only`, `every`, `all`, `none` are the rules' own normative quantifiers and prose connectives; each universal that carries a fact is a row above or a rule's stated scope.                                                                |
| count     | The ledger's own fields (round number, class counts, session identifier) and the parked idea's historical figures, which rows above already source.                                                                                                |

### Task 2.2: the command bullets against the delta scenario

| Obligation in the new scenario                              | The bullet in each of the ten commands                                                                         |
| ----------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| the working tree is established before the first write      | "…is established and recorded before the first write…"                                                         |
| a sibling is named when the primary belongs to another unit | "…the primary checkout when no other unit holds it, otherwise the sibling worktree `../axon-showcase-<unit>`…" |
| an ownership it cannot establish is treated as occupied     | "…with an ownership it cannot establish treated as occupied…"                                                  |

`consult.md` keeps no workspace bullet: it starts no mutating unit.

### Task 4.3: the sweep's hits and dispositions

| Hit                                                                                                                | Disposition                                                                                                                       |
| ------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------- |
| `AGENTS.md`'s procedure, forced-commit decision point, "No in-flight branch", rejection bullet, and fork invariant | Updated in this change (tasks 1.1-1.6).                                                                                           |
| `AGENTS.md`'s premise-interrogation bullet                                                                         | The idea's stale status word corrected to "then-parked" (deep-review finding 3); its measured figures stay as an incident record. |
| `AGENTS.md`'s stash and `reset --hard` gotchas                                                                     | Retained: they govern a switch inside one tree, which the rule leaves possible.                                                   |
| The ten commands' `Repository lifecycle` blocks                                                                    | Updated (task 2.1).                                                                                                               |
| `review-quick.md`'s hand-over enumeration                                                                          | Updated (task 3.1).                                                                                                               |
| `ship-change`'s steps and completion criteria                                                                      | Updated (tasks 3.2-3.3, and the completion criteria this review added).                                                           |
| `README.md`                                                                                                        | New `### Parallel Units` subsection (task 4.1).                                                                                   |
| `docs/ideas.md`                                                                                                    | The implemented idea removed and the follow-up parked (task 4.2).                                                                 |
| `openspec/config.yaml`                                                                                             | No lifecycle wording to refresh.                                                                                                  |
| `openspec/specs/`                                                                                                  | Only the delta's two requirements change; the main spec is synced at archive.                                                     |
