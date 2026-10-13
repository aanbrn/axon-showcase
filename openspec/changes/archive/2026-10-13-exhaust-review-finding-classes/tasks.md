# Tasks

## 1. The review rule

- [x] 1.1 Add the exhaustion clause to `.opencode/agent/review-quick.md`: a class raised on any round, the first
      included, is searched for every instance of its shape across the artifact and reported as one clustered finding
      (each instance still carrying its `file:line`).
- [x] 1.2 Write the delta for `showcase/quality/agent-skills` — a `MODIFIED` block for "Per-change quality-gate and
      analysis subagents are available", copying the full requirement (description + all eight scenarios) and adding the
      exhaustion clause to the quick-review scenario.

## 2. The parked entry

- [x] 2.1 Removed the 2026-10-12 "cluster co-classed findings on the first round" entry from `docs/ideas.md`, per the
      file's convention (an idea is removed once implemented).
- [x] 2.2 Confirmed `README.md` needs no edit — the derived surface
      (`grep -niE "quick-review|quick review|auto-review|     review-quick" README.md`) is four mentions (`:303`,
      `:371`, `:380`, `:421`), all describing the loop's shape ("repeated until it finds nothing new"), none the
      report's classes.
- [x] 2.3 Read the `showcase/quality/agent-skills` spec's `## Purpose` and decided no refresh is owed — it describes the
      subagent inventory, not the review report, so a delta cannot carry it and the main spec needs no edit.

## 3. Verification

- [x] 3.1 Ran the smoke-run after the OpenCode reload. Planted a co-classed pair (a count-class defect stated twice —
      "the requirement carries **six** scenarios" in `proposal.md` and `design.md`, against the delta's eight) and
      invoked `review-quick` with no prior-round list: it reported them as **one clustered finding**
      (`contradiction (count)`, `file:line` for both, naming the exhaustion search it ran and stating "this is one
      clustered finding, not two"), and reported no class it did not find (a no-defect class stayed silent). The rule
      took effect; the plants were then reverted. The same run surfaced one real gap — the proposal's Impact omitted
      `AGENTS.md` — now fixed.
- [x] 3.2 Ran `./gradlew spotlessApply` then `spotlessCheck` (BUILD SUCCESSFUL), `openspec validate --all` (27 passed /
      0 failed), and the manual 120-character check over the changed set (clean).
- [x] 3.3 Ran a `review-quick` pass over the implementation: 2 rounds, the `task-drift` finding fixed, round 2 clean.

## 4. Capture

- [x] 4.1 Ran the `lesson-capture` subagent; its verdict is **net 0** — every candidate the unit's review cycle surfaced
      (the smoke-run reload precondition, the README/`## Purpose` decision tasks, the unrecorded round count, the
      unticked tasks) is already covered by an existing rule the author did not follow, so no `AGENTS.md` addition is
      proposed and there are no retirements. Applied net `AGENTS.md` delta: **0 lines** — one docs-sweep edit widened
      the auto-review pointer to name the class-exhaustion clause (a same-line change; the file's `19/19` diff is the
      formatter's reflow).
- [ ] 4.2 Ask the owner for the manual review pass, after 3.3 is clean and 4.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit and the main-spec sync ride this PR.
- The remaining two units follow, each parked in `docs/ideas.md` by title: the per-round record ("Record per-unit review
  rounds and whether each finding was a compliance miss", 2026-10-13) and the loop bound ("Bound the review loop for a
  record artifact", 2026-10-13). Each implementing change removes its entry.
