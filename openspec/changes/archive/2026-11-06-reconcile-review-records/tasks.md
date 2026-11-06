# Tasks

The unit record — its `Workspace:`, state, and pause checkpoint — is `docs/units/reconcile-review-records/unit.md`.

## 0. The unit's own records

- [x] 0.1 `docs/units/reconcile-review-records/`: the ledger, each round's instances file, the hand-over, and the
      process-defect record are written as the rounds run, not at the end. Verify:
      `python3 scripts/commit-hygiene.py --unique-rounds --expect reconcile-review-records:<n>` and
      `--finding-instances` pass, and the hand-over's last section matches the highest recorded round. Done: the ledger
      lists the rounds and their instances files, the hand-over carries a section per round, and defects `#1`–`#3` are
      recorded; `--unique-rounds --expect-clean reconcile-review-records` passes.

## 1. The records

- [x] 1.1 `docs/units/surface-projector-backlog/review-handover.md`: fold the four entries of `findings/advisor-1.md` —
      two findings, one hypothesis, one open question, each with its class, location and claim — into the section headed
      "Control evidence — the deployed positive backlog (no review round)", record that `findings/advisor-2.md` raised
      none, repoint every later reference to either file or to the pass "in `findings/`" at that section, and delete
      both. Verify: each entry's class, and its location where the entry carries one, greps in the hand-over;
      `git show 9ac45da7:docs/units/surface-projector-backlog/findings/advisor-1.md` has no entry the hand-over lacks;
      and
      `tr '\n' ' ' < docs/units/surface-projector-backlog/review-handover.md | grep -c "advisor[^.]\{0,60\}findings/"`
      prints 0, which the wrapped form needs and a line-oriented grep misses. Done: the four entries sit in "Control
      evidence — the deployed positive backlog (no review round)", the second pass is recorded as clean, six later
      references are repointed, and both files are deleted; the joined-text control prints 0.
- [x] 1.2 `docs/units/pin-wrapper-distribution-checksum/review-handover.md`: the proposal pass's five entries fold into
      the round-4 section, which already names that pass; the implementation pass's four entries get their own section,
      headed as a pass with no review round, because they concern the shipped tool and postdate that round. Its repeated
      "the advisor's pass, in `findings/`" references — several wrapped across lines — become citations of those two
      sections, and both files are deleted. Verify: as 1.1 over nine entries, with each command scoped to
      `docs/units/pin-wrapper-distribution-checksum`. Done: the proposal pass's five entries are in the round-4 section
      and the implementation pass's four in their own "no review round" section; eleven references are repointed; both
      files are deleted; the joined-text control prints 0.
- [x] 1.3 `docs/units/surface-projector-backlog/rounds.tsv`: round 12's ninth field records `definition-changed`, and no
      other row changes. The ledger's last field is the closed reason enum, so the explanation stays in the hand-over's
      round-13 section, which already states that the deep review raised the finding. Verify:
      `awk -F'\t' '$2=="surface-projector-backlog" && $3==12 {print $9}'` prints `definition-changed`, and
      `python3 scripts/commit-hygiene.py --session-continuity` passes. The hand-over's round-13 section gains a dated
      correction naming the value it recorded (`caller-restarted`) and the value that replaced it, so the record shows
      what changed rather than only the result. Done: row 12 records `definition-changed`, and the round-13 section
      carries the dated correction naming what it recorded and why it changed; `--session-continuity` passes.

## 2. The store's rule

- [x] 2.1 The delta at `specs/showcase/quality/commit-hygiene/spec.md` replaces the requirement
      `Every review round recorded after the boundary keeps its finding instances` with a block that states why the
      store exists beside the now-permanent hand-over, scopes the store to review rounds, sends any other pass's
      findings to the hand-over, and adds the verification's failure on a file the store does not hold. Verify: the
      block copies the current requirement's description and all fourteen scenarios in order, `openspec validate --all`
      passes, and `openspec validate --changes` reports no dropped or renamed scenario. Done: the delta is the block at
      `specs/showcase/quality/commit-hygiene/spec.md`, with the fourteen original scenarios and the two added;
      `openspec validate --all` reports 27 passed.
- [x] 2.2 `scripts/commit-hygiene.py`: `--finding-instances` fails on any tracked file under
      `docs/units/<unit>/findings/` whose name is not `round-<N>.md`, naming the file, and keeps passing a store holding
      only round files. Load `check-and-test-authoring` before writing the check. Verify: a scratch unit holding a stray
      file makes the mode name it, and a clean scratch unit passes. Done: `store_stray_files` lists every tracked
      non-round file under the stores at any depth below `findings/`, and the mode appends one failure per file; with
      the comparison removed the direct-child arm fails (0 != 1), and with the depth condition narrowed to the immediate
      children the nested arm fails, so each half discriminates.
- [x] 2.3 `scripts/test-commit-hygiene.py`: three arms for 2.2 — a stray file under the store fails naming it, a nested
      file under a directory inside the store fails naming it, and a store holding only round files passes — each shown
      to fail against the pre-change behaviour where it should. Verify: the suite passes, and the stray-file arm fails
      when the new comparison is removed. Done: three arms — a direct stray file fails naming it, a nested one under
      `findings/consultations/` fails naming it, and a store holding only round files passes with empty stderr; each
      stray arm fails against the pre-change scan, the nested one included, whose depth the first fix still skipped.
- [x] 2.4 `.opencode/skills/review-gate/SKILL.md`: the writer step that places a round's instances gains the clause for
      a pass that is not a review round — its findings go to the hand-over — and points at the store's requirement
      rather than restating it. Verify: the step names that requirement and carries no copy of its text. Done: the
      writer step's new bullet sends a non-review pass's findings to the hand-over and points at the store's requirement
      rather than restating it.

## 3. The class and the leftovers

- [x] 3.1 `docs/units/surface-projector-backlog/process-defects.md`: append one record for the invented-file class, in
      the shape the workflow states. Verify: the record carries its six fields and `verifyWorkspaceRecords` passes.
      Done: `surface-projector-backlog#4` records the class with its six fields.
- [x] 3.2 `docs/units/pin-wrapper-distribution-checksum/process-defects.md`: the same record, added without deleting the
      file's "No process defect observed while working this unit" statement: that was the observation while the unit
      ran, and the record states that the class was found later, during this change's reconciliation. Verify: the
      original statement survives with the later record beneath it, `verifyWorkspaceRecords` passes, and the record
      carries its six fields. Done: `pin-wrapper-distribution-checksum#1` sits beneath the file's original "no process
      defect observed" statement, which stands as what was true while the unit ran.
- [x] 3.3 `docs/units/assert-paketo-pin-coverage/process-defects.md`: append a dated correction to `#3` stating that the
      two merged units carry four `advisor-*.md` files, not five. Verify: `#3` keeps its original observation with the
      correction beneath it, and `verifyWorkspaceRecords` passes. That unit's `#6` — this change's own formatter
      incident during the previous unit's ship — is outside this change's scope and is parked in `docs/ideas.md`
      instead. Done: `#3` keeps its observation and gains the dated correction; `#6` is parked in `docs/ideas.md`.
- [x] 3.4 `docs/ideas.md`: this work's entry leaves the `## 2026-11-06` section; the section keeps the IDEA-integration
      entry and gains one for the previous unit's parked defect. Verify: `./gradlew verifyIdeasSections` passes and the
      section names both remaining entries. Done: the ideas section keeps the IDEA-integration entry and the newly
      parked defect entry; `verifyIdeasSections` passes.

## 4. The gates

- [x] 4.1 The claims table in `design.md` is re-derived after the last edit, each row run against the base or the final
      tree as the row states, and every row reproduces. Verify: each source command re-run after the final change. Done:
      the table was re-derived after the last edit, and each row's command was re-run against the base or the final tree
      as the row states.
- [x] 4.2 `./gradlew spotlessApply` runs after the final edit to a Spotless-owned file, and `openspec validate --all`
      passes. Verify: both commands, in that order, on the final tree. Done: `spotlessApply` after the last edit, then
      `openspec validate --all` at 27 passed.
- [x] 4.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes, and `python3 scripts/test-commit-hygiene.py`
      passes under the declared Python floor. Verify: both green on the final tree. Done:
      `check -PskipITs -Pcoverage.gate.enabled=false` green in 1 m 4 s, re-run after the implementation's own review
      rounds, and `python3 scripts/test-commit-hygiene.py` exits 0.
- [x] 4.4 The deep-review routing decision runs once from the completed implementation —
      `python3 scripts/review-routing.py --unit reconcile-review-records --change reconcile-review-records` — and its
      outcome and the signals it matched are recorded on this task. Verify: the outcome is recorded with its signals,
      and a `REQUIRED` outcome is run before completion, while `NOT_NEEDED` spends no deep-review session. Done:
      `review-routing.py` returns `NOT_NEEDED` on the completed implementation, so no deep-review session is created and
      no hand-over is prepared.
- [x] 4.5 `openspec/specs/showcase/quality/commit-hygiene/spec.md`: refresh the `## Purpose`, because it enumerates the
      guard's reach ("that every round recorded after the findings store's boundary carries its instances file") and the
      widened check now also rejects a file the store does not hold. A delta cannot carry a Purpose, so the edit rides
      the archive commit. Verify: the Purpose names the store's file set, and the requirement's text is untouched by the
      edit.
      Done: the Purpose now names the store's file set, the requirement's text is untouched by the edit.

## 5. The consultation

- [x] 5.1 The owner authorized an advisor consultation ("Отревьювь с советником до согласия"). It confirmed the parked
      premises, judged both additions this proposal makes beyond them necessary, and required three revisions before the
      manual pass: historical corrections dated and additive, an unsupported pre-commit assurance removed, and the
      previous unit's defect `#6` dropped as unrelated scope. A follow-up confirmed the revisions and recommended
      proceeding to the manual pass. Verify: the consultation and its dispositions are recorded in
      `docs/units/reconcile-review-records/review-handover.md`.
