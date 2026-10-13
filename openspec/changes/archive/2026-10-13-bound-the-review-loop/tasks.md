# Tasks

## 1. The bound

- [x] 1.1 Widened the `AGENTS.md` review-loop bullet's pointer enumeration to name the record-artifact loop bound, as
      the analogous edits added a pointer phrase (the clause text lives in `showcase/quality/agent-skills`, the spec it
      points at).
- [x] 1.2 Wrote the delta for `showcase/quality/agent-skills` — a `MODIFIED` block for "Per-change quality-gate and
      analysis subagents are available", copying the full requirement (description + all eight scenarios) and adding the
      bound clause to the quick-review scenario.

## 2. The parked entry

- [x] 2.1 Removed the 2026-10-13 "Bound the review loop" entry from `docs/ideas.md` (its section became empty and went
      with it), per the file's convention.
- [x] 2.2 Confirmed `README.md` needs no edit — its four auto-review phrases (`:303`, `:371`, `:380-381`, `:421`)
      describe the loop's common shape; the bound is a record-only exception the spec owns.
- [x] 2.3 Read the `showcase/quality/agent-skills` spec's `## Purpose` and decided no refresh is owed — it describes the
      subagent inventory, not the loop.
- [x] 2.4 Confirmed `.opencode/agent/review-quick.md` needs no edit — the bound is the calling agent's action (it stops
      and escalates), not the review's report.
- [x] 2.5 Confirmed `.opencode/commands/retrospective.md` and `.opencode/agent/experience-analyzer.md` need no edit —
      they describe the digest's inputs, not the loop's stopping condition, and the bound adds no digest field.
- [x] 2.6 Confirmed `scripts/experience-analysis.sh` and `docs/review-rounds.tsv` need no edit — no record field
      changes, and the block already prints every row verbatim, so the last row's repeats column shows the class the
      bound stopped on.
- [x] 2.7 Confirmed `AGENTS.md`'s experience-analyzer bullet and `README.md`'s `/retrospective` descriptions need no
      edit — both are capability-level summaries with no enumeration of the loop's stopping condition.

## 3. Verification

- [x] 3.1 Confirmed the premise: unit 1's derived-facts block (`scripts/experience-analysis.sh`), unit 2's exhaustion
      clause (`.opencode/agent/review-quick.md`), and unit 3's record line and `docs/review-rounds.tsv` are on `main`
      (#560/#561/#562), and the `AGENTS.md` pointer (with the bound) and the delta clause state the same scope and stop
      condition.
- [x] 3.2 Ran `./gradlew spotlessApply` then `spotlessCheck` (BUILD SUCCESSFUL), `openspec validate --all` (27 passed /
      0 failed), and the manual 120-character check over the changed non-formatter files (`docs/review-rounds.tsv`'s
      rows are data and are excluded, as the record change's task 4.2 recorded).
- [ ] 3.3 Run a `review-quick` pass over the implementation, repeating until clean.

## 4. Capture

- [x] 4.1 Ran the `lesson-capture` subagent; applied two in-place merges (no new bullet): the recurring-unit bullet's
      clause now says the proposal's site list is **pointed** at the tasks that name the sites rather than hand-written
      (the enumeration under-matched across six rounds here), and the capture-carriage clause now covers the per-round
      record rows (this unit ran seven rounds unrecorded). Applied net `AGENTS.md` delta: **+5 lines** (2980 → 2985),
      including the two `captured:` markers. Its verification note (the bound is record-scoped, so it would not have
      stopped this unit's own loop — a process-rule change is shipped behavior) was a mistake in the report, not the
      artifacts.
- [ ] 4.2 Ask the owner for the manual review pass, after 3.3 is clean and 4.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit and the main-spec sync ride this PR.
- This is the last unit of the review-cost work: the derived-facts digest (#560), the class exhaustion (#561), the
  per-round record (#562), and this bound.
