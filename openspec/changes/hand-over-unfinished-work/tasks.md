# Tasks

Workspace: unit=hand-over-unfinished-work; path=/Users/afanasyev/Development/axon-showcase-hand-over-unfinished-work;
branch=hand-over-unfinished-work; writer=ses_e84bc611dffe5NgNeD5jGGOLsk

## 1. Planning

- [x] 1.1 Record the pre-write derivation pass in `design.md`'s claims table, each row re-derived from the source it
      names. Verify: every row names a command, a file, or a message sequence, and the two figures measured from the
      session store (`~54 %` context, no compaction) reproduce from the commands the row implies. **Done:** every row
      names its source; the harness rows reproduce from `opencode --version`, `opencode debug agents`, and the two
      config files, and the session-store rows from the queries that produced them.
- [x] 1.2 Write the delta at `specs/showcase/quality/agent-skills/spec.md` as `MODIFIED` blocks for the two pause
      requirements, copying each current block in full and adding only the new clauses and scenarios. Verify:
      `openspec validate --all` passes and no existing scenario is dropped or renamed.
- [x] 1.3 Confirm no `## Purpose` refresh is owed: the capability names the project-authored skills by member and this
      change adds none. Verify: `grep -n "pause-resume-unit" openspec/specs/showcase/quality/agent-skills/spec.md` still
      matches the Purpose line unchanged.

## 2. Implementation

- [x] 2.1 Extend `AGENTS.md`'s pause entry-point bullet with the agent's own handover decision, keeping the skill's
      path, the pointer to what it owns, the stop-and-report rule, and the `captured:` marker. Verify: the bullet still
      names `.opencode/skills/pause-resume-unit/SKILL.md` and the stop-and-report rule, and
      `git diff --numstat AGENTS.md` reports the measured delta recorded here. **Done:** the bullet names the decision
      as a trigger, keeps the skill path and the stop-and-report rule, carries both `captured:` markers, and the
      post-`spotlessApply` delta is `+6 −5` lines and `+126` bytes (261,552 → 261,678), the one new trigger case and its
      marker.
- [x] 2.2 Extend `.opencode/skills/pause-resume-unit/SKILL.md`: the trigger gains the agent's own decision; the pause
      section states that the agent pauses instead of requesting confirmation for successive work steps, that an
      unmeasured limitation is described as an assessment, that approval stops are unchanged, and that the pause report
      names the checkpoint and `/resume-unit`. Verify: the skill carries no copy of the checkpoint field set outside its
      table and no new procedure section; `grep -c "confirmation" .opencode/skills/pause-resume-unit/SKILL.md` is at
      least the two new clauses.
- [x] 2.3 Update `README.md`'s pausing bullet with the same trigger. Verify: the bullet keeps its current claims
      (checkpoint, no commit/stash/teardown, new session); the `/pause-unit` table row keeps describing the command's
      effect with no trigger, and `.opencode/commands/pause-unit.md` keeps the owner as its only trigger.
- [x] 2.4 Write `docs/process-defects/hand-over-unfinished-work.md` with the incident's record in the field set
      `AGENTS.md` states, carrying no diagnosis, no proposal, and no cost estimate. Verify: the file carries `Recorded`,
      `Stage`, `Expected`, `Observed`, `Evidence`, `Consequence`, and `Knowledge boundary` for its one record, and no
      `Decision` line.

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply` then `./gradlew spotlessCheck` after the final edit of every markdown file,
      including this task list. Verify: `spotlessCheck` passes and reports no skipped file — a skip counts as a failure
      for that file. **Done:** `spotlessCheck` passes over the final edits of all changed files, with no skipped file.
- [x] 3.2 `openspec validate --all`. Verify: it passes with the delta at the mirrored path. **Done:** 27 passed, 0
      failed, with the delta at the mirrored path.
- [x] 3.3 Run the guidance checks the pre-commit guard runs for these files: marker placement, the change-directory
      claim, and a conflict-marker scan over the changed set. Verify: each check exits zero on the staged set, and each
      has been seen to fail on a planted violation where the tooling supports a control. **Done:** `--markers`,
      `--conflict-markers`, and `--staged` exit 0 on the unit's set, and both controls exit 1 on a scratch repository
      carrying a plain-bullet marker and a tracked conflict marker.
- [x] 3.4 Sweep the entry-point set derived in `design.md`: `AGENTS.md`, both commands, the skill, both README sites,
      the spec requirement, and the new record. Verify: `grep -rn "pause-unit\|Pause and resume\|pause-resume-unit"`
      over those paths returns no site still describing an owner-announced pause as the only trigger, excluding the two
      commands' own dispatch triggers, which stay owner-invoked. **Done:** the sweep returns only those two dispatch
      triggers.
- [x] 3.5 Measure `AGENTS.md`'s net delta after `spotlessApply` and record it on task 2.1. **Done:** `+6 −5` lines,
      `+126` bytes, recorded on task 2.1.

## 4. Review gate

- [x] 4.1 Run the quick-review loop over the proposal and then the implementation, appending each round's row to
      `docs/review-rounds.tsv` when its report arrives, and run the ledger checks §2 requires. **Done:** rows 1–9 are
      present and contiguous, each reproduces its report's classes, repeats, and compliance count, and the ledger checks
      pass; the round count and the clean state are `docs/review-rounds.tsv`'s. The two rounds in which the record
      trailed the loop are in process defect #3.
- [x] 4.2 Derive the deep-review routing decision with `scripts/review-routing.py --change hand-over-unfinished-work`,
      record its outcome, run the pass when it is `REQUIRED`, and record an explicit resolution when it is
      `RECOMMENDED`. **Done:** routing `RECOMMENDED` with `[repeated-class]`. The pass ran before the owner resolved it
      — recorded as process defect #2 — and the owner accepted the result and the run on 2026-10-27 when approving the
      unit (`ACCEPTED`: the findings are fixed and verified and the review is complete), which closes #2's open item.
      Findings: pass 1 — `design.md`'s `#19471` citation and `tasks.md` task 5.1 (stale issue status):
      `FIXED_AND_VERIFIED` by pass 2. Pass 2 — nothing remains.
- [x] 4.3 Tick §3's gate tasks, re-run `spotlessApply`/`spotlessCheck` after the tick, and ask the owner for the manual
      review pass. Stop there. **Done:** §3 ticked, the formatter re-run, and the manual pass requested below.

## 5. Upstream preparation

- [x] 5.1 Draft a comment carrying the measured reproduction — version, model and context limit, context at the claim,
      the absent runtime signals, and the successful pause after the owner's command, with no reasoning traces, session
      dump, or local paths — in `$TMPDIR/opencode/upstream-19471-comment.md`, and record beside it the venue's current
      state: `anomalyco/opencode#19471` is closed `not_planned` (2026-07-05) and unlocked, and
      `gh search issues --repo anomalyco/opencode --state open "context budget" --limit 20` (2026-10-27) returns five
      open issues, the closest live thread being `#58459` (a per-turn context drift inspector) with `#50574` (compaction
      at 1M context) and `#56582` (model variants overriding the limit) related but distinct. Do not publish; the venue
      (a comment on one of those threads, a new issue, or none) is the owner's decision. Verify: the draft states the
      claim it supports, the evidence, and what it does not establish, and the recorded venue state matches a fresh
      `gh api` read of the issue. **Done:** the owner chose a new issue; the draft was reworked into one (the closed
      thread's bot comment invites it), reviewed through two `review-quick` rounds with every finding fixed, and posted
      as `anomalyco/opencode#59531`. The reference and its retirement condition are in the pause skill.
