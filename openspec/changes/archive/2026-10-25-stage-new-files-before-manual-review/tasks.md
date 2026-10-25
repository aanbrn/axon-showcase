# Tasks

Workspace: unit=stage-new-files-before-manual-review;
path=/Users/afanasyev/Development/axon-showcase-stage-new-files-before-manual-review;
branch=stage-new-files-before-manual-review; writer=ses_e8d34533fffeyvibaCv54L62xc

## 1. Workspace and pre-write pass

- [x] 1.1 Record the unit's workspace in this file and verify it against the registry: the line states
      `Workspace: unit=stage-new-files-before-manual-review; path=/Users/afanasyev/Development/axon-showcase-stage-new-files-before-manual-review; branch=stage-new-files-before-manual-review; writer=ses_e8d34533fffeyvibaCv54L62xc`,
      and `git worktree list --porcelain` shows this path on that branch while the primary checkout keeps
      `bump-dependencies-2026-10-25`.
- [x] 1.2 Run the review gate's pre-write derivation pass and land its output as the claims table already carried at
      `design.md` → "Claims table"; verify each row reproduces from the command it names at base
      `7f76216b7ff8c4963402305a0988d1b9942550e7`, and re-derive the table after the unit's last append to
      `docs/review-rounds.tsv`.

## 2. The skill carries the staged-files step

- [x] 2.1 Edit `.opencode/skills/review-gate/SKILL.md` §5 ("Manual review pass", line 141) so the step owns the staging
      rule, with these clauses each on its own line: the agent runs `git add -- <explicit paths>` for the unit's own new
      files, planning artifacts included, before asking for the pass; it re-runs that add for any of those files edited
      after the previous add; it never uses `git add -A`, `git add .`, or `git add -N` for this; a unit with no new
      files adds no step; the ask names the review surface — the unit's workspace tree and its IDEA Changes panel, where
      staged new files show as Added and the remaining modifications as Modified, with `git diff --cached` plus
      `git diff` as the CLI equivalent; and staging is not committing, so the approval stop is unchanged. Verify the
      section shows each clause once, and that the file contains at least one `git add -- ` line.
- [x] 2.2 Keep §5 free of reset or checkout guidance: the preserve → reset → restore ordering and the conditional
      guarantees for a staged new file live in the `AGENTS.md` failure-handling bullet (task 3.2), the site that states
      the hazard, so the skill adds no second home for them. Verify: a grep of the skill for `git reset --hard` and for
      `git checkout --` returns nothing.

## 3. The root states the surface and the narrowed hazard

- [x] 3.1 Edit `AGENTS.md` "Workflow: Change lifecycle" step 2 (line 340) so the review surface is the unit's workspace
      with its new files staged for the pass, pointing at the review-gate skill for the step and keeping the sentence's
      other obligations (work stays uncommitted; the `docs/ideas.md` removal rides the branch). Verify: the step shows
      the surface and the pointer, and carries no staging command of its own (the skill owns it).
- [x] 3.2 Rewrite `AGENTS.md` failure handling (line 385) so its guarantee is scoped and its ordering stated: a hard
      reset deletes a staged new file, and an untracked one survives only when the target revision does not contain its
      path; a checkout of a staged one silently restores the index copy. It names the two routes rather than one list:
      the reset route (preserve → `git reset --hard` → restore, by a `git stash pop` after a stash or a return to the
      preserved commit after a commit) and the rebase route (`git stash` → `git rebase` → `git stash pop`, or a rebase
      alone when the work is committed, where the rebase is the middle step and a hard reset would drop the commits the
      route keeps). It also states that the unit's new files are never unstaged before a hard reset, because a plain
      stash saves a staged new file and saves nothing for an untracked one, and it gives an untracked new file its own
      preserve action — stage it first, or `git stash -u`. The existing "never reset the only copy of the work" rule
      stays first and unweakened, and the `git clean -fd` warning is kept but loses its "one loss vector"
      characterization (line 386), since a hard reset is a loss vector too when the target contains the path. Verify:
      the bullet states both the condition and the two routes with their own restore steps, names the untracked file's
      preserve action, contains no unstage-before-reset instruction, a grep for `safe against` shows no unconditioned
      guarantee, and a grep for `one loss vector` returns nothing.
- [x] 3.3 Edit the commit-forced decision point (line 351) so "An untracked unit survives a reset" becomes the
      conditional statement (the target must lack that path) and points at the failure-handling bullet for the staged
      case and the ordering, keeping its `git switch` and directory-scoped-add obligations. Verify: the bullet states
      the condition and the pointer, and its opening phrase still matches exactly once.
- [x] 3.4 Rewrite the `git reset --hard` gotcha's untracked-premise sentences (lines 1620–1631), not only append to
      them: the "still-untracked change dir survives" sentence now holds only when the target revision lacks that path
      and says so; its consequence clause — "the whole change is untracked until the first commit", and the "loses only
      tracked-file edits" inference — is qualified, because the unit's new files are staged before the manual pass and
      still uncommitted; and the `git checkout --` sentence states that a new file has no `HEAD` entry, so a staged copy
      is restored from the index. The edit agrees with the gotcha's own stash → reset → pop recipe (that stash is the
      preserve step) instead of contradicting it; the untracked-survives sentence points at the failure-handling bullet
      for the ordering, and the checkout sentence points at the skill's re-stage rule, rather than either restating what
      it points at. The gotcha's other sentences (the soft/mixed reset, the stash recipe, the `git show HEAD:<file>`
      advice, the incident) stay unchanged. Verify: no sentence still asserts the whole change is untracked until the
      first commit, and no sentence still asserts that a pre-push reset loses only tracked-file edits (a grep for each
      phrase returns nothing); the unstage command appears nowhere as a reset precondition; and the gotcha still names
      every reset form it named before.
- [x] 3.5 Qualify the capture decision point's phrase at line 490–491 ("the change dir is deliberately left untracked")
      so it reads "until the manual pass stages it", keeping the sentence's decision-difference argument and its pointer
      to `lesson-capture.md`. Verify: the sentence still states the commit-discipline gap it was written for, and no
      other clause of that bullet changes.

## 4. The README shows the loop as the owner experiences it

- [x] 4.1 Add one clause to the development narrative at `README.md:305` so the manual review is described as showing
      the unit's new files, staged by the agent for the pass. Verify: the sentence keeps the paragraph's two-review
      structure and names no command (the skill owns the procedure).

## 5. Verification

- [x] 5.1 Run `./gradlew spotlessApply` and then `spotlessCheck`, after the last edit to any of the three files, and
      confirm no file is skipped as non-converging; the changed set is markdown only, so Prettier owns wrapping and the
      manual 120-character check does not apply to it.
- [x] 5.2 Run `openspec validate --all` and confirm the change validates with `skip_specs: true` and no delta spec.
- [x] 5.3 Re-derive the site set and confirm it and the task set agree in both directions: run
      `grep -n "untracked" AGENTS.md` and `grep -n "review pass\|review runs against\|review surface" AGENTS.md`; add a
      row for any hit decision 6's table lacks; confirm every row marked edited has a task in groups 2–4; and confirm
      every site those tasks name is a row in the table. Verify: the greps return no hit absent from the table, the
      table→task and task→table checks each find no gap, and every row marked edited reads as edited in the file.
- [x] 5.4 Apply the rule to this unit's own review: stage the change dir's files with explicit paths, then confirm the
      staged set lists them and that the tracked-file edits remain unstaged modifications.
- [x] 5.5 Run `./gradlew check` and confirm it is green, since its guidance-file scans read `AGENTS.md` and
      `.opencode/skills/*/SKILL.md`; report any task that fails and why.

## 6. Review gate

- [x] 6.1 Run the `review-quick` loop with the session-continuity preflight, append each round's row to
      `docs/review-rounds.tsv`, and run
      `python3 scripts/commit-hygiene.py --unique-rounds --expect stage-new-files-before-manual-review:<round>` and
      `python3 scripts/commit-hygiene.py --session-continuity` after each row; stop on either failure.
- [x] 6.2 Derive the deep-review floor with
      `python3 scripts/review-routing.py --change stage-new-files-before-manual-review`, decide at or above it, and
      record the outcome and the signals it relied on on this task. **Routing**: `RECOMMENDED`, signal
      `[repeated-class]` (four ledger rows repeat a prior class); resolved to run the deep pass, with a verification
      pass after the fixes. **Deep pass 1** (`ses_e8d1873ffffef6pVNlO9rpgFDz`, initial): verdict 1 should-fix —
      claims-table row 12 stated three `untracked` matches in `openspec/specs/` where its own command returns two.
      **Status**: `FIXED_AND_VERIFIED` — row 12 reads two, both `commit-hygiene` (`:25`, `:93`), re-grepped and
      confirmed by pass 2. **Deep pass 2** (`ses_e8d1873ffffef6pVNlO9rpgFDz`, verification) reproduced the reversal
      independently and raised 1 should-fix (task 3.2 kept `git clean -fd`'s "one loss vector", which the new evidence
      falsifies) and 1 nitpick (decision 4's absolute `checkout --` wording); both are applied. **No third pass** — a
      recorded decision: the residual after pass 2 is one shipped-guidance correction whose exact wording this change
      specifies and whose effect is bounded to one phrase of one sentence (`AGENTS.md:386`'s "one loss vector"), plus
      one design-wording correction; both are verified textually by the quick loop, and a third pass over the same four
      planning files would read no new material, so its expected value does not justify its cost. **Implementation
      stage**: routing re-derived from the completed implementation → `RECOMMENDED`, `[repeated-class]` (8 ledger rows);
      resolved with the change's first deep pass over the shipped text (pass 3, `ses_e8d1873ffffef6pVNlO9rpgFDz` — a new
      subject, since passes 1–2 reviewed the planning artifacts): verdict nothing remains, so no finding carries a
      status.
- [x] 6.3 Run the `lesson-capture` decision: run the subagent unless the skip condition holds in full, apply the durable
      proposals, and record the applied net delta or the skip and its reason on this task. **Run**: the skip condition
      does not hold (classes beyond the local set; the diff touches authoring surfaces). **Applied**: +3 lines in
      `.opencode/skills/review-gate/SKILL.md` §1 step 2 — a prescribed command sequence is one fact, reproduced whole in
      a scratch tree — with its `captured:` marker. **Rejected**: replacing the `git reset --hard` gotcha's "With local
      commits, `git rebase` … is the way" with a pointer; the single-sourcing carve-out states an imperative at every
      site where its step appears, and round 14 verified the same. **Parked** in `docs/ideas.md`: two mechanism gaps the
      capture surfaced (the marker check's bold-lead requirement; `claim-scan.py`'s `absolute` pattern missing "the
      one"). **Net `AGENTS.md` delta: 0.**
- [x] 6.4 Stage the unit's new files once more if anything above changed them, ask the owner for the manual review pass,
      and end the turn — nothing committed, pushed, opened as a PR, or archived before the approval.

## Workflow follow-up

- After the owner approves: run CI, archive the change (delta-free, `skip_specs`), and merge per the `ship-change`
  skill.
