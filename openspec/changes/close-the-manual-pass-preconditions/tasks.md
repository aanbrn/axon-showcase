# Tasks

## 1. The clean-round mode

- [x] 1.1 `scripts/commit-hygiene.py` — extend the **live** `check_unique_review_rounds` (the later definition, the one
      taking `expected`; the earlier `repo`-only one is shadowed and dead, and is left to its own parked idea) with the
      clean-round assertion over the units the caller names: fail when a named unit has no rows, when its numerically
      highest round's `classes` field is not `-`, or when that field is unreadable, each message naming the unit, the
      round, and the classes found.
- [x] 1.2 `scripts/commit-hygiene.py` — parse `--expect-clean <unit>` as repeatable, require `--unique-rounds` (an
      expectation without the mode fails rather than skips, mirroring `--expect`), and fail on an empty or malformed
      unit name.
- [x] 1.3 `scripts/test-commit-hygiene.py` — cover, through `main` and asserting both the exit status and the message: a
      clean highest round passing over earlier unclean rounds; an unclean highest round failing with unit, round and
      classes named; an earlier clean round not masking a later unclean one; a clean named unit passing while an unclean
      unit the caller did not name sits in the same ledger; a repeated `--expect-clean` failing when the second named
      unit is unclean; a named unit with no rows failing rather than passing vacuously; a present-but-empty `classes`
      field and a row short of the field failing rather than reading as clean; the numerically highest round being
      checked even when its row is not the last and its number sorts below another's as a string; `--expect-clean`
      without `--unique-rounds` failing; and a malformed unit name failing.
- [x] 1.4 `scripts/commit-hygiene.py` — extend the module docstring's enumeration of the `--unique-rounds` modes with
      the clean-round assertion, since the same edit makes the docstring incomplete.
- [x] 1.5 Confirm the existing test that pins `--expect unit-a:1` passing against a row carrying `wording` still holds
      unchanged: the new mode must not alter `--unique-rounds`'s behaviour.

## 2. The review gate

- [x] 2.1 `.opencode/skills/review-gate/SKILL.md` §2 — name the invocation at the manual-pass boundary, in the same
      bullet as `--expect` and the session check but explicitly outside the "repeat both before the next reviewer
      invocation" rule, since a round in flight is unclean by design; state that the shape checks' passing is not a
      clean verdict.
- [x] 2.2 `.opencode/skills/review-gate/SKILL.md` §5 — state the ask's conditions at the step: the recorded pre-write
      pass and its claims table, the unit's highest recorded round carrying no class at every stage, and — at the
      **implementation** stage, since a capture runs after the implementation's quick review and a proposal's approval
      precedes it — the unit's capture record exists (its applied delta or a recorded skip), beside the clean quick
      review and any required deep review the step already names, and pointing at `AGENTS.md`'s capture workflow rather
      than restating its steps.
- [x] 2.3 `.opencode/skills/review-gate/SKILL.md` completion criteria — rework them **and** the "ask the user now"
      sentence beside them together: the conditions the sentence names are the recorded clean closing round, the
      recorded pre-write pass, any required deep review, and — at the implementation stage — the capture record, so the
      skill no longer states one unconditional instruction and a conditional list beside it.

## 3. Docs sweep and the parked idea

- [x] 3.1 `docs/ideas.md` — remove the 2026-10-26 parked precondition this change delivers.
- [x] 3.2 Sweep every site that describes the ledger's modes or the manual-pass preconditions —
      `.opencode/skills/check-and-test-authoring/SKILL.md`, `openspec/specs/showcase/quality/agent-skills/spec.md`, and
      `openspec/specs/showcase/quality/commit-hygiene/spec.md` — and correct any that enumerates them incompletely.
      `docs/retrospectives/2026-10-23.md` is a historical record and stays as recorded.
- [x] 3.3 `AGENTS.md` — reword "A clean quick review is a precondition for asking for the manual review … it means _ask
      the user now_" so the preconditions the skill now states — the recorded clean closing round, the recorded
      pre-write pass, any required deep review, and the capture record at the implementation stage — are carried at the
      root sentence a reader acts on, without restating the procedure.
- [ ] 3.4 `openspec/specs/showcase/quality/commit-hygiene` — record a Purpose refresh and apply it in the archive
      commit: the Purpose enumerates the ledger verifications, and a delta cannot carry one.

## 4. Verification

- [x] 4.1 `./gradlew testCommitHygiene` green.
- [x] 4.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green, proving the new mode leaves the `check`
      task's behaviour unchanged on the ledger's existing units.
- [x] 4.3 Positive control: run `--unique-rounds --expect-clean <unit>` against a unit whose highest round carries a
      class and confirm the failure names it, then against a clean unit and confirm it passes.
- [x] 4.4 `./gradlew spotlessApply` after the final edit, then `spotlessCheck`;
      `python3 scripts/commit-hygiene.py --markers`.
- [x] 4.5 `openspec validate --all` green.
- [x] 4.6 Re-derive the survey figure in `design.md`'s claims table after the final edit of the ledger, which adds rows.

## 5. Review gate and delivery

- [x] 5.1 Quick-review loop over the implementation clean, with a row per round in `docs/review-rounds.tsv` and both
      ledger checks green after each; the closing round recorded clean, and confirmed with the new mode applied to this
      unit first: `--unique-rounds --expect-clean close-the-manual-pass-preconditions`.
- [x] 5.2 Deep-review routing decision recorded with its outcome. Routing: `RECOMMENDED`, one signal — six recorded
      rounds repeated a prior class **at the time of routing**, before the rounds that followed it added more; the owner
      resolved to run the pass. **Pass 1 of 2** (initial review): one `should-fix` finding, `OPEN` — `review-gate` §5's
      opening omitted the recorded pre-write pass while `AGENTS.md` and the completion criteria named it, and the
      planning copies said "both preconditions"; fixed by adding the condition to §5 and aligning the four copies.
      Everything else passed: all thirteen delta scenarios judged conformant to the code, the pre-existing modes
      interacting correctly, the `check` invocation naming no unit, and an acknowledged-gap-only unit failing as "no
      round". **Pass 2 of 2** (independent verification): pass 1's finding verified fixed at every site it named —
      `AGENTS.md`, §5's opening, the completion criteria, the proposal, the design's goal and decision, and tasks 2.2,
      2.3 and 3.3 — with no duplicate and no merely skimmable checklist; it found one `nitpick`, the single planning
      copy the fix had left stale (task 3.3 still listing three conditions where the shipped set is four), applied here,
      with its confirmation recorded by this unit's closing round in `docs/review-rounds.tsv`. Pass 1's Python verdict
      carries forward unchanged (those files are byte-identical).
- [x] 5.3 The unit's own capture decision recorded — its applied delta, or the skip and its reason. Done: the capture
      applied nothing (net 0) — all four incidents it was asked about are `already covered` by rules that already
      prescribe the same action. Its side observations are recorded here rather than as rules: the requirement's title
      is narrower than its content (a rename is `REMOVED` + `ADDED`, out of this change's scope), and `--expect-clean`
      reads only the closing row, so a stale intermediate row is outside what it can catch. The unit's own measured
      delta, from `git diff --numstat` after `spotlessApply` and after the last edit to each file: `AGENTS.md` +7/−6,
      `review-gate` +15/−7, `commit-hygiene.py` +68/−10, its tests +135, and `docs/ideas.md` −6, the delta spec and the
      planning artifacts as the tasks state.
- [x] 5.4 The pre-write derivation pass recorded as its own task here, with the claims table re-derived in `design.md`
      after the final edit; the table does not stand in for the pass's record.
- [ ] 5.5 Archive the change after the owner's approval, with the delta synced into the main spec in the same pull
      request.
