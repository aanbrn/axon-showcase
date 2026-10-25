# Tasks

Workspace: unit=fix-advisor-worktree-read; path=/Users/afanasyev/Development/axon-showcase-fix-advisor-worktree-read;
branch=fix-advisor-worktree-read; writer=ses_e8f05c061ffeVdZniQbS3yuTHk.

## 1. Implementation

- [x] 1.1 `.opencode/agent/technical-advisor.md` — the handoff's excerpt field states the verified boundary: the advisor
      reads files inside its session's workspace itself, and a path outside it travels as an excerpt because the sandbox
      refuses it. Replace the field's no-shell justification with the boundary statement, keeping no shell as one of the
      two limits. **Done**: the field now reads "any pre-change or history excerpts the decision needs", with no reason
      given; the operational paragraph states the two limits (no shell; file tools that stop at the session's creation
      workspace, a path beyond it refused unless a configured grant allows it) and its four bullets carry the inspection
      instruction, the caller's history/diff/revision duty, and the two imperatives the old text held.
- [x] 1.2 `.opencode/commands/consult.md` — step 3's matching field keeps the caller's action and points at the
      definition's boundary statement instead of restating it; step 4 records that a continued session keeps the
      workspace it was created in. **Done**: the advisor's review of the implementation found the first draft's root
      bound to the caller's directory, which a continued session contradicts; both sites now name the session's creation
      workspace, and the design carries the three probes.

## 2. Verification

- [x] 2.1 `./gradlew spotlessApply`, then `spotlessCheck`, after the last edit. **Done**: both exit 0.
- [x] 2.2 `openspec validate --all` passes with the new change dir. **Done**: 27 passed, 0 failed.
- [x] 2.3 `opencode debug config` exits 0 and `opencode debug agents` still resolves `technical-advisor` with its
      ruleset, so the edited definition loads. **Done**: `debug config` exit 0; the `debug agents` entry carries the new
      boundary paragraph on disk and no longer the old reason. Its resolved `permissions` array mixes the definition's
      own block (`*/*` deny, then `read`/`glob`/`grep`/`skill` allows and the `.env` denies) with the engine's and the
      project config's entries (`shell`, the two `external_directory` grants, `browser` deny, `doom_loop` deny); the
      frontmatter is untouched.
- [x] 2.4 The 120-character manual check over the whole changed set. **Done**: no non-table line exceeds it.
- [x] 2.5 Refresh the branch onto the advanced `origin/main` before the review continues. **Done**: `origin/main` moved
      `91cd0fd8` → `dfe52325` (#681) while this unit was in flight; the branch has no commits, so the ledger's own row
      was saved, `docs/review-rounds.tsv` restored from `HEAD`, `.opencode/package.json` restored (the startup install
      had rewritten its pin), and the branch fast-forwarded. The unit's diff is now its two files plus the change dir,
      and #681's rows and files are intact.

## 3. Documentation and capture

- [x] 3.1 Sweep the repository for the claim this change corrects:
      `grep -rn "no shell" .opencode/ AGENTS.md README.md docs/` and check each site. **Done**: the three sites are the
      definition's handoff field and operational paragraph and the command's field, all corrected; the fourth hit is
      `check-and-test-authoring`'s `(no shellcheck)`, unrelated. The README's consultation section and the `AGENTS.md`
      bullet state no readable-root claim, and `docs/guidance-evidence/` carries none.
- [x] 3.2 Confirm no spec delta is owed (`skip_specs: true`): the `agent-skills` technical-advisor requirements.
      **Done**: the read-only requirement states the split this change keeps — "It SHALL rely on its read tools and the
      handoff for history and diff excerpts" — and its handoff-context scenario gives the no-shell reason; the boundary
      is a mechanism those outcomes do not distinguish, so no requirement or scenario moves. The capture's
      `.opencode/skills/review-gate/SKILL.md` edit is covered the same way: the review-gate requirement states that "a
      correction to a step SHALL be made in the skill" and states no claim-class guidance.
- [x] 3.3 Run the `lesson-capture` subagent over the diff and the review rows, and apply its durable proposals (or
      record the skip and its reason). **Done**: two proposals applied. (1) `AGENTS.md`'s "Refreshing a committed
      branch" bullet became "Refreshing a branch" and now covers an uncommitted tree —
      `git merge --ff-only origin/main`, and the check that the refreshed diff deletes nothing the unit did not change —
      since this unit's stale base produced the round-2 `dropped-content` finding. (2)
      `.opencode/skills/review-gate/SKILL.md` step 1's universal-claim parenthetical stopped enumerating a set that had
      diverged from `scripts/claim-scan.py`'s `absolute` class (it named `each`/`no`/`nothing` and omitted
      `only`/`never`/`always`) and now points at that class as a floor. Rejected as already covered: the "one refusal is
      not a boundary — probe it" lesson (`AGENTS.md`'s reproduce-the-condition clause). Net delta: `AGENTS.md` +11/−6,
      the skill +12/−12.

## 4. Review

- [x] 4.1 Pre-write derivation pass and claims table: `python3 scripts/claim-scan.py` seeded the candidates, each
      resolved into `design.md`'s claims table or a dismissal beside it.
- [x] 4.2 Quick-review loop: fix every round's findings and re-run until a round reports no new observation; each
      round's classes and state are `docs/review-rounds.tsv`'s appended rows, not restated here. **Done**: the loop ran
      to a clean round twice — once on the implementation, then again after the independent consultant corrected the
      boundary rule — and the ledger's rows for this unit are its record.
- [x] 4.3 Deep-review routing: derive the floor with
      `python3 scripts/review-routing.py --change fix-advisor-worktree-read`, record its outcome here, and run
      `/review-thorough` when it is `REQUIRED`. **Done**: the derivation returned `RECOMMENDED` on `[repeated-class]`
      (the ledger's rows are that state), which the caller resolved to run because the unit edits two authoring
      surfaces. Pass 1 (`ses_e8e6e503bffeK4aNztae4avSeL`) returned no finding: it verified the `AGENTS.md` bullet's
      preservation and its agreement with the rewritten reset/stash gotchas, the review-gate replacement's accuracy
      against `scripts/claim-scan.py`, the boundary claims against the tool output, and the `skip_specs` argument for
      both edits. With no valid finding to fix, the budget's second (verification) pass was not owed at that point; the
      independent consultant's review then corrected the boundary rule, which reopened the quick loop and is recorded in
      the ledger's later rows for this unit, so the verification pass ran on the corrected state. Pass 2
      (`ses_e8e6e503bffeK4aNztae4avSeL`) returned one should-fix: the design's Premise check still said the spec "says
      nothing about how it reads evidence", contradicting C6 and Decision 6 — status `FIXED_AND_VERIFIED` after the
      bullet was narrowed to the path boundary; quick round 12 confirmed it and the gate closed. The budget of two
      passes is used. The independent consultant's third review then found one evidence claim — the design inferred a
      check order from a permission error ("the sandbox answers first") — fixed in the same bullet and the Risks and
      confirmed by round 13.
- [x] 4.4 Ask the owner for the manual review pass; commit, push, archive, and merge only after approval. **Done**: the
      owner approved on 2026-10-25 after three independent consultations, the last of which found nothing left to
      correct; the unit shipped as pull request #684 and is archived in this same change dir.
