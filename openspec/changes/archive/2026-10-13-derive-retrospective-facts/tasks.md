# Tasks

## 1. The derived-facts block

- [x] 1.1 Add the block to `scripts/experience-analysis.sh`, printing: the window's merged-PR set with its boundary
      stated (the search's count, each PR merged on the `since` date, and the previous retrospective's file); the
      archived changes scoped to the window's commits (a time-explicit `git log --since="${SINCE}T00:00:00"`, naming the
      `since`-date dirs for the same resolution); `AGENTS.md`'s line/top-level-bullet/`captured:` metrics at HEAD and
      just before each `since`-date PR (so the baseline is read off the first counted PR); and the `captured:` markers
      added since the window start, each with the commit that introduced it.
- [x] 1.2 Update the script's header comment to describe the block.

## 2. The digest's described contents

- [x] 2.1 Write the delta for `showcase/quality/agent-skills` — a `MODIFIED` block for "Experience analysis is available
      to agents", copying the full requirement (description + all three scenarios) and adding the derived-facts block to
      the digest description plus a scenario for it.
- [x] 2.2 Name the block in `.opencode/commands/retrospective.md`'s digest step.
- [x] 2.3 Widen the other digest-contents descriptions, derived by grepping the corpus rather than hand-enumerated: the
      `AGENTS.md` experience-analyzer bullet and `.opencode/agent/experience-analyzer.md`'s "You are given" list both
      name the block; the two `README.md` `/retrospective` descriptions are deliberately left as capability-level
      summaries (they already omit `docs/ideas.md`), and that omission is stated in `proposal.md`/`design.md`.
- [x] 2.4 Park the two review-contract follow-ons this analysis surfaced in `docs/ideas.md` under a new `## 2026-10-13`
      section, each tagged and routed: the per-round record (per-unit review rounds, each round's findings by class, and
      whether each was a compliance miss of an existing rule — the retrospective's S3) and the loop bound (safe only
      once the digest derives the countable claims, so it depends on this change). The class-exhaustion unit is already
      parked (2026-10-12).

## 3. Verification

- [x] 3.1 Ran `./scripts/experience-analysis.sh 2026-10-05` and checked the block against
      `docs/retrospectives/2026-10-13.md`: the search returned 60 (the design's "60 now / 59 at authoring"), the
      `2026-10-05` merges are `#499–#513`, the window's commits added 39 archive dirs (against the window's 36), and the
      block's `before #504` (2693 lines, 164 bullets, 121 captured) to `HEAD` (2965, 172, 162) reproduces the
      retrospective's +272 / +8 / +41 exactly. The block's first cut reported the baseline at the raw git window's start
      (2672, `#494`'s parent — a PR merged 2026-10-04 but committed 2026-10-05), which the per-`since`-date-PR metrics
      fixed; that was a **block defect**, recorded in the design's Context.
- [x] 3.2 `spotlessApply` then `spotlessCheck` pass, `openspec validate --all` is 27 passed / 0 failed, and the manual
      120-character check over the changed set is clean (`scripts/experience-analysis.sh` is the only changed file
      Spotless does not own).
- [x] 3.3 Ran `review-quick` over the implementation; four rounds, all findings fixed, round 4 clean (0 findings).

## 4. Capture

- [x] 4.1 Ran the `lesson-capture` subagent; applied both durable proposals as merges (the auto-review bullet's
      non-convergence clause now covers a claim about the change's own mechanism, re-derived by running the tool; the
      `gh pr list` gotcha now carries the git-window scoping), with no retirements. Applied net `AGENTS.md` delta: +38
      insertions / -29 deletions = **+9 lines** (2965 → 2974).
- [ ] 4.2 Ask the owner for the manual review pass, after 3.3 is clean and 4.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit and the main-spec sync ride this PR.
- The remaining three units follow, each parked in `docs/ideas.md` by title: the class exhaustion ("Make the
  quick-review subagent cluster co-classed findings on the first round", 2026-10-12), the per-round record ("Record
  per-unit review rounds and whether each finding was a compliance miss", 2026-10-13), and the loop bound ("Bound the
  review loop for a record artifact", 2026-10-13). Each implementing change removes its entry.
