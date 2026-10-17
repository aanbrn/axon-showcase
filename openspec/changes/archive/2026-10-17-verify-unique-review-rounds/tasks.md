# Tasks

## 1. The check

- [x] 1.1 `scripts/commit-hygiene.py` — a `--unique-rounds` mode with `find_duplicate_review_rounds` and
      `check_unique_review_rounds`, asserting at most one row per `(unit, round)` and naming the unit, the round and
      both line numbers; the module docstring's mode list and `main`'s dispatch carry it, beside `--unique-crons`.
      **Done.**
- [x] 1.2 `scripts/test-commit-hygiene.py` — a case per direction, one for the same round of another unit, one for a
      missing ledger, and one driving `main(["--unique-rounds", …])` as the cron mode's test does. **Done** (the suite
      passes).

## 2. The build wiring

- [x] 2.1 `build.gradle.kts` — a `verifyUniqueReviewRounds` `Exec` task running the mode against
      `docs/review-rounds.tsv`, and its `check` dependency, mirroring the `verifyUniqueCronSchedules` registration.
      **Done.**

## 3. The spec

- [x] 3.1 Add the `ADDED` delta for `showcase/quality/commit-hygiene`: the review-round ledger carries at most one row
      per unit and round, verified in `check`, with a scenario per direction (the requirement's text is 412 characters,
      inside the 500-character guidance). **Done.**
- [x] 3.2 Record the `commit-hygiene` `## Purpose` refresh for the archive commit — its enumeration of the checks ends
      at "no two workflow cron schedules collide", so it gains this one. A delta cannot carry a Purpose, so the edit
      lands with the archive and the implementing PR's diff carries no main-spec change beyond the delta. **Recorded;
      owed at archive.**

## 4. The docs

- [x] 4.1 Every live enumeration of the check set gains the member: `AGENTS.md`'s `check` members list, the guard's
      module docstring and its argparse `--help` description, `README.md`'s build-side prose and its
      verification-command block, and the parked test-coverage idea's list. **Done** (the set derived by grepping
      `verifyLargeFiles` and `--large-files` across the live files, not from memory).
- [x] 4.2 `docs/ideas.md` — the entry this change implements is removed, and with it the day section it emptied.
      **Done.**

## 5. Verification

- [x] 5.1 Run the guard's suite and prove the check is evidence with its control: duplicating an **existing** pair (the
      ledger's last row) fails it naming both lines; restoring passes. **Done** — and my first attempt appended a
      _unique_ row and passed, which proved nothing: the control must perturb the surface the check reads.
- [x] 5.2 Run the mode on the repository (exit 0), then `spotlessApply`/`spotlessCheck`, `openspec validate --all`, and
      the manual 120-character check over the changed set. **Done.**
- [x] 5.3 Run a `review-quick` pass over the proposal and the implementation, repeating until clean. This unit ran no
      separate proposal pass, so round 1 covered both stages. **Round 1 — 7 classes, all fixed**: three live
      enumerations of the check set lacked the member (the guard's module docstring, `README.md`'s build-side prose, and
      the parked test-coverage idea's list; `AGENTS.md`'s was already right, and the guard's argparse `--help`
      description surfaced in round 2); no task for the `commit-hygiene` `## Purpose` refresh; my spliced `**Done.**`
      records fused the plan text; 7.1 unticked while its text said it ran; the `main` dispatch untested; the Non-Goal's
      parked-idea clause with no claims row; an emptied `## 2026-10-17` section header. **Round 2 — 3 classes, two
      repeating, all fixed**: the README's cron sentence opened on a bare parenthesized identifier; the ideas list
      gained a nested group and a duplicate member — and my first rewrite then changed its _membership_ (the list names
      checks lacking coverage, so the two test tasks must not join it); a 214-character docstring line; no blank line
      before the new test; the argparse description still named only `--unique-crons`. The round-2 root cause was the
      edit method — span-edits over wrapped prose re-mangling the clause around the insertion — and its remedy is to
      rewrite the whole clause from the site's own subject and re-read the sentence, which is what caught the membership
      error. **Round 3 — 2 classes, both repeating, all fixed**: the docstring's rewritten mode-list sentence had gained
      a spurious parenthetical (its opening `(` landed mid-list, after the `--conflict-markers` clause); the blank line
      added to the test file landed _inside_ the previous method's body while the new def still lacked one; and the
      description's continuations were 14 spaces where the file uses 12. The round-3 remedy — re-derive each site whole
      from the file's own convention (the sentence from its eight modes, the separator from the class's own layout, the
      indent from its siblings) and re-read the file — is what closed them. **Round 4 — 4 classes, two repeating, all
      fixed**: the ledger's round-4 row was recorded clean before the round reported (corrected to this round's line);
      task 6.1's net-`AGENTS.md`-delta figure did not reproduce (`+2` stated against `+5` measured); the proposal's
      What-Changes list omitted `README.md`; the capture clause B doubled the conjunction in the enumeration bullet's
      grep list; and the claims table's trailing parenthetical invented a `check` list the coverage requirement does not
      have. The recurring `wording` class (every round) has the same root cause the change itself captured — span-edits
      over wrapped prose — so the fixes are whole-unit re-derivations. **Round 5 — 3 classes, all repeating, all
      fixed**: task 4.1's enumeration list omitted the guard's argparse `--help` description; the round-1 record's "four
      … lacked the member" miscounted its own list; and the proposal's What-Changes/Impact omitted
      `docs/review-rounds.tsv`. The root cause was the lists being written from memory, so they were re-derived by grep
      — which grew task 4.1's set from four sites to five. **Round 6 — 2 classes, one repeating, all fixed**: the
      Decision "A check, not more prose" rested on the parked entry this change removes, with no claims row for it
      (added, anchored to the change's base — `git show origin/main:docs/ideas.md`); the `--unique-rounds` help said
      "one row per round" where the predicate is per `(unit, round)`; and the claims table's `verifyUniqueCronSchedules`
      row still carried a stray space from the earlier mangle ("( → "). **Round 7 — 2 classes, both repeating, all
      fixed**: the round-6 narrative said "3 classes" where its ledger row records two; and task 4.1's enumeration set
      omitted `README.md`'s verification-command block, whose member was then added. **Round 8 — 2 classes, one
      repeating, all fixed**: now that the unit's work is a commit, the claims table's `git show HEAD:docs/ideas.md`
      anchor no longer reproduced (repointed to the change's base, `git show origin/main:docs/ideas.md`); and the Risks
      bullet claimed a shape change fails the build where a delimiter change makes the check pass silently (narrowed to
      state that accepted false-negative surface). Also captured stderr in the two direct `check_unique_review_rounds`
      calls, matching the file's convention. **Round 9 — 1 class (contradiction), repeating, all fixed (round 8's
      session, continued)**: the round-8 narrative had predicted round 9 would be a fresh session where it continued
      round 8's; corrected. Rounds 1-7's session ids were not carried across the park, so the reuse resumed from
      round 8. **Round 10 — nothing remains (same session): the loop closed.** Rounds 1-7 ran each in its own session
      (this change predates the merged reuse clause); rounds 8-10 reused one session, so the reuse path ran here too.
      **Done.**

## 6. Capture

- [x] 6.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. **The capture ran and proposed two `AGENTS.md` changes**, and the first
      application attempts misfired — the A clause landed in the wrong bullet, B's marker mid-sentence, and the revert
      overshot and deleted a real rule (the delta-spec-path paragraph, restored from `HEAD`). Both are now applied as
      wording extensions of the bullets they extend: **A** extends the structured-file bullet's "match one full line
      verbatim" remedy with "replace the whole unit in one edit"; **B** adds `scripts/` and a tool's own docstring and
      `--help` to the enumeration sweep's grep places. Net `AGENTS.md` delta: **+5 lines**
      (`git diff --numstat origin/main -- AGENTS.md` = 12 insertions / 7 deletions), the two clauses plus the one-line
      `check` members list, with no retirement. The capture's third item, `scripts/commit-hygiene.py`'s `def main`
      separator, is applied.
- [x] 6.2 Ask the owner for the manual review pass, after 5.3 is clean and 6.1 is done. **Asked** — the request is in
      the session; the outcome is the owner's.

## 7. The change's own records

- [x] 7.1 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, and its rows are derived from the ledger's own header, the consumer script, the dedupe change's diff,
      the cron check's registration and requirement, and the archived design's Non-Goal. **Done.**

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
