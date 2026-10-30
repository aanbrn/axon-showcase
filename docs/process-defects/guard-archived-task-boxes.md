# Process defects — guard-archived-task-boxes

Process defects observed while working this change. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### guard-archived-task-boxes#1 — Another unit's tree was taken for the new branch and then moved off it

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), at the unit's start.
- **Stage** — proposal (workspace setup).
- **Expected** — a new unit's branch is created in a tree that belongs to it: `AGENTS.md`'s workspace rule requires the
  tree's occupancy to be established from the registry before the first write, holds that a tree with a unit branch
  checked out belongs to that unit whether it is clean or dirty, and releases the primary to the next unit only after
  the previous one completed with no work of it remaining — a clean `git status` alone neither releases it nor proves it
  safe.
- **Observed** — `git switch -c guard-archived-task-boxes --no-track origin/main` ran in the primary checkout, which was
  checked out on another unit's branch, `correct-readme-mechanism-claims`, and held that unit's uncommitted work (a
  modified `README.md`, `docs/process-defects/proofread-readme-language.md` and `docs/review-rounds.tsv`, and an
  untracked `docs/process-defects/correct-readme-mechanism-claims.md`). The switch carried those changes onto the new
  branch, and `openspec new change` wrote the change directory there.
- **Observed** — the primary was then switched off that unit's branch: `git switch main` ran in the same tree one minute
  later, leaving the neighbour unit's branch no longer checked out anywhere while its uncommitted work sat on `main`.
  The unit's tree was thus moved to another branch instead of being left to its owner.
- **Evidence** — the primary's reflog records the sequence, and the owner's report is the signal that it harmed the
  neighbour: `git -C ../axon-showcase reflog -n 8` prints
  `06:30:39 checkout: moving from correct-readme-mechanism-claims to guard-archived-task-boxes` and
  `06:31:00 checkout: moving from guard-archived-task-boxes to main`. The follow-up `git status --porcelain -uall` on
  the new branch showed `M README.md`, `M docs/process-defects/proofread-readme-language.md`,
  `M docs/review-rounds.tsv`, `?? docs/process-defects/correct-readme-mechanism-claims.md`, and
  `git worktree list --porcelain` showed the primary's single worktree on that branch. Whether the neighbour session
  kept writing afterwards is not in this evidence.
- **Consequence** — the change directory was deleted from the primary, a sibling worktree
  (`../axon-showcase-guard-archived-task-boxes`) took the branch, and the session moved there. The foreign changes rode
  four branch switches in that tree before the neighbour unit committed them on its own branch and merged as #738, so
  the work itself survived; the neighbour unit's tree was checked out on `main` while that work was still uncommitted.
- **Knowledge boundary** — observed for this unit and this tree. Did not check whether a `git switch -c` in a tree with
  an untracked-only foreign unit behaves differently; the outcome here involved tracked modifications.

- **Decision** — no-change: the Workspace rule already requires occupancy read from the registry before the first write,
  and a clean `git status` never releases a tree; this is a compliance miss, not a wording gap.

### guard-archived-task-boxes#2 — The quick reviewer was restarted without a recovery case

- **Recorded** — 2026-10-29, by the writing session, during the review gate.
- **Stage** — review (round 2).
- **Expected** — each review round continues the unit's reviewer session; a replacement needs one of the recovery cases
  the review gate names, recorded with both identifiers before the replacement starts.
- **Observed** — round 2 invoked `review-quick` as a new session (`ses_e7b188549ffeLWwNArbfWIqFrw`) while round 1's
  session (`ses_e7b20612cffef22MZDR4wjGOoD`) had reported normally and no recovery case existed.
- **Evidence** — `docs/review-rounds.tsv` row 2 carries `replaced` with `caller-restarted`; rows 3–6 continue the second
  session; the review gate stopped for the owner's recovery decision, which was to accept the violation and continue.
- **Consequence** — one recorded continuity violation; no finding was lost, since round 1's report was applied before
  the restart. The gate stopped once for the owner's decision.
- **Knowledge boundary** — observed for this unit's two sessions. Did not verify whether the harness would have
  continued round 1's session had its identifier been passed; the invocation was written without one.

- **Decision** — no-change: review-gate §2's reviewer-session preflight already requires a permitted recovery case and
  both identifiers before a replacement.

### guard-archived-task-boxes#3 — A multi-edit script wrote once and lost the edits before its failure

- **Recorded** — 2026-10-29, by the writing session, during the proposal round.
- **Stage** — proposal (the round-3 fixes).
- **Expected** — a batch of edits either writes each edit as it succeeds or is verified before its result is reported;
  `AGENTS.md`'s programmatic-edit gotcha records the single-write failure mode.
- **Observed** — a script holding four replacements wrote the file only at its end; the third replacement's anchor had
  been re-wrapped by `spotlessApply`, the assertion aborted the script, and the first two replacements reached the file
  while the last two did not.
- **Evidence** — the script's `AssertionError` naming the `tasks.md` 2.9 anchor, followed by a read showing the delta
  and D1 edits applied but the task and proposal edits absent; the hand-over section written afterwards claimed all
  three were fixed.
- **Consequence** — the hand-over briefly over-claimed; the missing two edits were re-applied and verified by a file
  read, and the round-4 screen found the residue. No committed artifact carried the gap.
- **Knowledge boundary** — observed for this unit's scripts. Did not count how many earlier scripts in the chain shared
  the single-write shape; the ones that aborted reported it.

- **Decision** — no-change: the 'Never chain an edit to a commit' rule already records the single-write batch loss and
  requires each edit written as it succeeds.

### guard-archived-task-boxes#4 — The same single-write loss repeated at the implementation stage

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the review gate.
- **Stage** — implementation (the round-8 control extension, at the quick loop's verification round).
- **Expected** — a batch of edits writes each edit as it succeeds, and a mutation proof run after the batch is what
  establishes that the controls can fail.
- **Observed** — a script replacing two blocks of `scripts/test-commit-hygiene.py` wrote the file only at its end,
  aborted on a stale anchor at a third replacement, and landed none of them; the round that followed ran the suite and
  reported it green, and the first mutation run printed `STILL PASSES (blind)` for the dropped-`*` and dropped-`)`
  alternatives because the assertions the edits were meant to add had never arrived.
- **Evidence** — the aborted script's `AssertionError` on the `1.1 Done` fixture line; the re-applied replacement (read
  back in the file); the first mutation run's two `STILL PASSES (blind)` lines, and the second run's `FAILS as required`
  for the same two mutations.
- **Consequence** — two controls were reported green while blind for one round; the replacements were re-applied and all
  four mutations then failed their controls. No committed state carried the gap.
- **Knowledge boundary** — observed for this unit's scripts and verification rounds. Did not establish why the suite's
  green run did not reveal the missing assertions; only that it did not.

- **Decision** — no-change: the same rule already records the single-write loss; its repetition within one unit
  questions retrieval, not the rule's obligation.

### guard-archived-task-boxes#5 — The merged state was verified against a stale remote ref

- **Recorded** — 2026-10-29, by the writing session (`ses_e7e0a3d04ffepyFcMfYYisYm9y`), during the ship/settle stage.
- **Stage** — merge (the settle step's verification of the merged result).
- **Expected** —
  - A claim about the remote's default branch is a query whose output you have.
  - The ref such a claim reads is refreshed before the claim is made: the merge landed on the server, so the local
    `origin/main` ref predates it.
- **Observed** —
  - After `gh pr merge` reported success, the merged content was checked without a `git fetch`, so the local
    `origin/main` ref still named the pre-merge commit `1842b105`.
  - `git show 1842b105:scripts/commit-hygiene.py | grep -c find_unticked_archived_tasks` printed `0`;
    `git ls-tree --name-only 1842b105 openspec/changes/archive/ | grep -c guard-archived-task-boxes` printed `0`; and a
    grep of that commit's `commit-hygiene` spec for `archived change` printed `2` — pre-existing text about archived
    change directories, read as the sync having landed.
- **Evidence** —
  - Those counts (`0`, `0`, and a `2` from the non-discriminating pattern) formed the false conclusion.
  - `git reflog show refs/remotes/origin/main` records the transition:
    `1842b105 … pull --ff-only origin main: fast-forward` precedes `b5606da1 … fetch origin --quiet: fast-forward`.
  - At `b5606da1` the first two checks print `2` (the definition and its single caller) and `1`, while a grep for
    `unticked` — the pattern that discriminates — prints `7` where `1842b105` prints `0`.
- **Consequence** — one false conclusion was formed ("the guard is not on main") and corrected by the next command; no
  report, artifact or commit carried it, and the settle proceeded against the fetched ref. Cost: one extra command
  sequence.
- **Knowledge boundary** — observed for this unit's settle sequence. Did not check whether another session's
  `origin/main` was stale at the same time, and did not establish how long the ref had lagged.

- **Decision** — no-change: one occurrence, cost one command, no artifact carried the false conclusion; below the gate.
