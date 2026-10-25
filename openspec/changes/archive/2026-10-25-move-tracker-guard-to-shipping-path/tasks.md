# Tasks

Workspace: unit=move-tracker-guard-to-shipping-path;
path=/Users/afanasyev/Development/axon-showcase-move-tracker-guard-to-shipping-path;
branch=move-tracker-guard-to-shipping-path; writer=ses_e8d476658ffep4lI2e5U7jH5gj.

## 1. The move

- [x] 1.1 Read both procedures and their `Source:` lines before editing, so the branch lands in the procedure whose
      trigger it belongs to and no other obligation moves with it. **Done**: both `Source:` lines quote their own
      `AGENTS.md` heading, and the branch's condition — the tracker's lifetime — is the shipping stage's, not the cloud
      action's.
- [x] 1.2 Add the branch to "Ship a change"'s `### Decision points`, conditioned on the tracker outliving the pull
      request, with the one-shot exception stated and the covered tracker issues named in the rule. **Done**: the bullet
      sits after the docs bullets and states both branches.
- [x] 1.3 Reduce the cloud section's bullet to the cloud-specific provenance — the action writes the closing reference
      into the pull request it opens — plus a pointer to the shared branch. **Done**: three lines, no remedy, and it
      names the shared decision.
- [x] 1.4 Prove the rule now has exactly one live statement, and that no third copy exists in the repository. **Done**:
      `git grep` for `is:issue is:open`/`closing keyword` outside the archive returns the five tracker-opening workflows
      and one statement of the remedy, in the shipping procedure's decision points.
- [x] 1.5 Move the tracker outcome into the deciding procedure's completion criteria, and make the cloud criterion point
      at it instead of restating it. **Done**: "Ship a change"'s criteria carries "any tracker it implements preserved
      when that tracker outlives it", and the cloud criterion names that outcome rather than asserting it.

## 2. Verification

- [x] 2.1 Run `./gradlew spotlessApply`, then `spotlessCheck`; the skill markdown is a Spotless target. **Done**: green;
      the formatter reflowed the new bullet's wrapping and nothing else.
- [x] 2.2 Run `openspec validate --all`. **Done**: 27 passed, 0 failed.
- [x] 2.3 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` — the `build` gate's definition-inventory probe
      must still resolve every agent, command, and skill definition. **Done**: BUILD SUCCESSFUL.
- [x] 2.4 Run the manual 120-character check over the change's whole changed set, derived from
      `git status --porcelain --untracked-files=all`. **Done**: measured with `perl -CSD -lne 'print if length > 120'`
      (a character count, not `awk`'s byte count — an em dash makes a 120-character line 122 bytes), the longest added
      line is 120 characters; the claims table's padded rows are the formatter's own output, the accepted trade-off.

## 3. Review, capture and the manual pass

- [x] 3.1 Record the pre-write derivation pass and its claims table in `design.md`, seeded by `scripts/claim-scan.py`,
      with every candidate resolved into a row or a class-level dismissal. **Done**: the table sits in `design.md`,
      seeded at the base this record names in task 3.7, with the per-kind dismissals beside it.
- [x] 3.2 Run the `review-quick` loop, appending each round's row to `docs/review-rounds.tsv` as its report arrives and
      running both ledger checks after each append; repeat until a round reports nothing new. **Done**: one reviewer
      session (`ses_e8cbf395…`, `created` then `continued`); each round's row and classes are the ledger's, both checks
      ran clean after every append, and the loop closed on a round with no new observation after the last fix.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py`, record its outcome and signals here, and run
      the routed pass when it is `REQUIRED`. **Done**: derived from the completed implementation and the ledger rows the
      router reads, `python3 scripts/review-routing.py --change move-tracker-guard-to-shipping-path` prints
      `RECOMMENDED` with its `[repeated-class]` signal. That signal's count and rows are the ledger's and are read live
      from it rather than quoted here, since each appended round would falsify a copy. An earlier derivation, taken
      while the ledger was still growing, read `NOT_NEEDED`; the router reads the ledger, so that reading was
      provisional and this one governs. The owner resolved the `RECOMMENDED` outcome on 2026-10-25 in favour of a
      targeted deep pass, whose status and findings are recorded on the resolved deep-pass task below.
- [x] 3.4 Run the `lesson-capture` subagent and record its applied net delta or its skip reason. **Done**: one of its
      three proposals applied — the capture workflow's placement rule, which rests on two independent occurrences
      (`#243`/`#651`'s two placements of the tracker ban under the cloud-only trigger, and
      `reduce-agents-md-loaded-surface`'s five rules behind a check-authoring trigger) — with the capture's review-gate
      check and its replacement of the extraction clause both declined, the first for one occurrence and no severe
      incident, the second because a whole-clause pointer would drop obligations the addition does not carry. The
      capture also retargeted the `AGENTS.md` cross-reference this move made stale. Applied net delta:
      `git diff --numstat AGENTS.md` reads `13 8`, so +5 lines, and no clause among them restates a rule another site
      owns.
- [x] 3.5 Run the targeted deep pass the owner resolved to run at 3.3 — `review-thorough` focused on the capture's two
      additions (`AGENTS.md`'s placement rule, the `review-gate` supplied check) and the artifacts' consistency with the
      diff — and record its status and findings here, before the manual pass is asked for. **Done**, two passes: pass 1
      returned one blocking (the capture record asserted the promotion gate without walking its threshold, and the
      review-gate addition rested on one near-miss) and one should-fix (the retargeted cross-reference sent "the
      action's closing reference" to a section that does not carry it) — `FIXED_AND_VERIFIED`: the record now cites the
      threshold per addition, the review-gate addition is declined and reverted byte-identically, and the pointer names
      the skill's two decision points. Pass 2 verified both fixes independently and found nothing further.
- [x] 3.6 Stage the unit's own new files, then ask the owner for the manual review pass; commit and push only after
      approval. **Done**: the change dir was staged with an explicit `git add -- <paths>` and the add re-run after a
      later edit, the ask named the review surface, and the owner approved on 2026-10-25; the unit ships as PR #693. The staging is the review gate's §5: `git add -- openspec/changes/move-tracker-guard-to-shipping-path`
      and the explicit paths of any other new file, re-run after a later edit, never `git add -A`/`.`/`-N`. The ask
      names the review surface (this tree and its Changes panel, `git diff --cached` for new files and `git diff` for
      modifications).
- [x] 3.7 Keep the branch refreshed from `origin/main`. **Done**: `origin/main` advanced to `3456bcb1` ("Stage a unit's
      new files before its manual review"), which touches `AGENTS.md`, `review-gate/SKILL.md`, `README.md`,
      `docs/ideas.md`, the ledger, and its own archived change dir. The stash → fast-forward → pop route applied it:
      `AGENTS.md` merged with all three of this unit's edits intact, the `review-gate/SKILL.md` incoming version applies
      cleanly (this unit has no diff there), and the ledger was rebuilt as the new base's rows plus this unit's twelve.
      The claim-seed and dismissals anchors record `3456bcb1` as the base, and the incoming gate now stages a unit's new
      files before its manual pass, which 3.6 does.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
