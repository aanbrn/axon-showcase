# Review hand-over — generalize-process-defects-2026-11-07

One section per quick-review invocation, recorded before the caller waits on it.

## Round 1 — proposal screen

- **Unit** — `generalize-process-defects-2026-11-07`, a guidance unit with no change dir. It runs the generalization
  pass over the 59 process-defect records that carried no `Decision` line, appends one `Decision` line to each, applies
  P1 to `AGENTS.md`, and parks R1 as an idea.
- **Revisions** — base `origin/main` `1a4ece43c9fca466bfb439fa7d63d350be4ad521`; the work is uncommitted on the branch,
  so current content is the working tree at that base.
- **Workspace** — `/Users/afanasyev/Development/axon-showcase`, branch `generalize-process-defects-2026-11-07`.
- **Packs** — `none`. The artifacts decide no JVM, test, build, or deployment subject; the guidance they change is
  agent-tooling. Classify independently and report a missing pack rather than searching for one.
- **Supplied artifacts** — the proposal is `docs/units/generalize-process-defects-2026-11-07/unit.md`: its "Proposal"
  section carries the selection, the 59-row disposition table, P1's exact replacement text and evidence, R1, and the
  planned `Decision` forms. The PR-body draft with its claims table is outside the repository at
  `$TMPDIR/opencode/generalize-process-defects-2026-11-07-pr.md`. The record texts are in
  `$TMPDIR/opencode/undecided.md`; the caller's cluster map is in `$TMPDIR/opencode/dispositions.md`. No implementation
  edit exists yet: P1, R1's idea entry, and the 59 `Decision` lines are written after the owner's proposal approval.
- **Scope** — whether each record's recurrence disposition is supported by the rule it names; whether P1 is a genuine
  increment over the sentence it replaces rather than a restatement; whether P1's `captured:` chain is placed on a rule
  block; whether R1's premise holds and its route is right; and whether the claims table's rows reproduce. Not
  re-deciding the corpus: the owner named no units, so the whole undecided set is the selection.
- **Prior findings** — none; this is the unit's first invocation.
- **Completed checks** — the counts re-derived (`records 292 undecided 59 units 22`); the five pack records and the four
  hand-over records re-read; `grep -c handover scripts/commit-hygiene.py` → `1` (no mode); the drafted `AGENTS.md`
  replacement tested in memory against `find_misplaced_markers` (0 offenders) and its 120-character wrapping checked;
  `./gradlew spotlessApply` and `spotlessCheck` over the unit's markdown.
- **Known limitations** — P1's net line delta is unmeasured until the edit lands; the 59 `Decision` lines do not exist
  yet, so the reviewer sees their planned forms and the disposition table rather than the appended text; the unit
  record's own `process-defects.md` is written at the implementation stage.
- **Deadline** — none fixed: this record exists so a stall is recoverable from it.

## Round 2 — proposal screen, after round 1's fixes

- **Unit, tree, branch, base** — unchanged from round 1: `/Users/afanasyev/Development/axon-showcase`, branch
  `generalize-process-defects-2026-11-07`, base `origin/main` `1a4ece43c9fca466bfb439fa7d63d350be4ad521`.
- **Packs** — `none`, as round 1, and for the same reason.
- **Supplied artifacts** — the same proposal with round 1's fixes: P1 is now a bold-led bullet in the section's bullet
  list rather than a paragraph replacement (finding 6); its evidence sentence states the three kinds of hand-over
  (finding 5); `generalize-process-defects-2026-11-03#2`'s disposition names the `technical-advisor` handoff rule
  (finding 4); the unit record records the spec-delta determination with its `grep` result and the scenario
  counterfactual (finding 1); the PR draft's claims row states the anchored test (finding 2). The `lesson-capture`
  definition is supplied at `.opencode/agent/lesson-capture.md` — `review-gate` §2 requires it for a generalizing unit
  and round 1's hand-over omitted it (finding 3). The PR draft and claims table remain at
  `/private/var/folders/rd/nw7pt03523dc_z_pzq1_rdjw0000gn/T/opencode/generalize-process-defects-2026-11-07-pr.md`; the
  record texts at `.../undecided.md`; the map at `.../dispositions.md`.
- **Scope** — whether each round-1 finding is fixed at its cause; whether the bullet form is a genuine increment and its
  `captured:` chain is legal; whether the spec-delta determination holds against the requirement's scenarios; and any
  new defect in the changed content. Not re-deciding the corpus.
- **Prior findings with their instances** — all six from `findings/round-1.md`: the spec-delta claim (`unit.md:126`),
  the claims-row method (PR draft line 21), the omitted definition (`review-handover.md:15-20`), the `generalize…#2`
  disposition (`unit.md:49`), the P1 evidence sentence (`unit.md:103-106`), and the paragraph structure
  (`unit.md:92-99`).
- **Completed checks** — the counts re-derived with the anchored test (`records 292 undecided 59 units 22`); the drafted
  bullet tested in memory against `find_misplaced_markers` (0 offenders); `./gradlew spotlessApply` and `spotlessCheck`
  after the last edit; `--unique-rounds --expect …:1` and `--session-continuity` pass.
- **Known limitations** — P1's net line delta is unmeasured until the edit lands; the 59 `Decision` lines do not exist
  yet; implementation begins only after the owner's proposal approval.
- **Deadline** — none fixed: this record exists so a stall is recoverable from it.

## Round 3 — proposal screen, after round 2's fix

- **Unit, tree, branch, base** — unchanged from rounds 1–2.
- **Packs** — `none`, for the same reason.
- **Supplied artifacts** — the same proposal with round 2's fix: P1's evidence is now a five-row table quoting each
  record's own field about the hand-over, with no inferred `Packs:` content, and the PR draft's claims row matches it.
  Everything else stands as round 2 supplied it, `lesson-capture.md` included.
- **Scope** — whether round 2's finding is fixed at its cause, whether the new table's five quotes reproduce, and
  whether the rest of the round-2 state stands. Not re-deciding the corpus.
- **Prior findings with their instances** — all six from `findings/round-1.md`, plus round 2's single instance from
  `findings/round-2.md`: `unit.md:101-105`, the inferred hand-over field.
- **Completed checks** — the five quotes re-read against their records; the counts re-derived; `spotlessApply` and
  `spotlessCheck` after the last edit; `--unique-rounds --expect …:2` and `--session-continuity` pass.
- **Known limitations** — P1's net line delta is unmeasured until the edit lands; the 59 `Decision` lines do not exist
  yet; implementation begins only after the owner's proposal approval.
- **Deadline** — none fixed: this record exists so a stall is recoverable from it.

## Round 4 — proposal screen, after round 3's fix

- **Unit, tree, branch, base** — unchanged from rounds 1–3.
- **Packs** — `none`, for the same reason.
- **Supplied artifacts** — the same proposal with round 3's fix: the `guard-recreate-strategy-across-deployments#1`
  evidence cell is now the record's own span (`the hand-over's `Packs: deployment-operations` line`) rather than an
  assembled quotation; the other four cells were already exact substrings. Everything else stands as round 3 supplied
  it.
- **Scope** — whether round 3's finding is fixed at its cause, whether the five evidence cells are now verbatim or
  explicitly unquoted, and whether the rest of the round-3 state stands. Not re-deciding the corpus.
- **Prior findings with their instances** — all six from `findings/round-1.md`, round 2's instance (`unit.md:101-105`,
  the inferred field) from `findings/round-2.md`, and round 3's instance (`unit.md:106`, the composite quotation) from
  `findings/round-3.md`.
- **Completed checks** — the five cells re-read against their records; `--unique-rounds --expect …:3` and
  `--session-continuity` pass; `spotlessApply` and `spotlessCheck` after the last edit.
- **Known limitations** — P1's net line delta is unmeasured until the edit lands; the 59 `Decision` lines do not exist
  yet; implementation begins only after the owner's proposal approval.
- **Deadline** — none fixed: this record exists so a stall is recoverable from it.

## Advisor consultation — proposal, no review round

- **Authorization** — the owner's instruction "Отревьювь с советником до согласия".
- **Question** — whether the disposition map, P1's form and site, R1's parking, and the no-spec-delta determination hold
  before the owner's proposal pass.
- **First report** — P1 addresses a real classification failure; R1's parking and the no-delta determination are
  defensible. Two confirmed problems: P1's sentence "an edited build script … is a `gradle-build` decision whatever the
  change's subject" made a path an unconditional trigger and contradicted the routine-verification exception; and two
  disposition reasons named only part of their coverage (`assert-paketo-pin-coverage#4`,
  `make-unit-lifecycle-explicit#1`). One hypothesis: a heading-presence check does not establish invocation ordering.
- **Applied** — P1 narrowed to "paths are evidence for the table's decision triggers, not triggers by themselves", with
  the formatter-only counterexample; the two reasons corrected; R1 reworded as a proposed presence check whose value
  depends on the invocation route running it.
- **Second report** — agreement with the revised substantive proposal; one presentation problem, that the PR summary
  attached "22 units" to the 292-record population, corrected.
- **Limitation** — the advisor's `external_directory` grant denied the out-of-repo PR draft; its text was supplied
  inline in the follow-up.

## Round 5 — proposal screen, after the advisor's corrections

- **Unit, tree, branch, base** — unchanged from rounds 1–4.
- **Packs** — `none`, for the same reason.
- **Supplied artifacts** — the proposal as round 4 left it, with the advisor's corrections: P1 narrowed so paths are
  evidence for the table's decision triggers, not triggers themselves (a formatter-only build-script edit loads no
  pack); the `assert-paketo-pin-coverage#4` and `make-unit-lifecycle-explicit#1` reasons now name their full coverage;
  R1 is a proposed presence check. The consultation is recorded in this file's advisor section.
- **Scope** — whether the advisor's corrections are reflected at their causes, whether the narrowed P1 still covers the
  five records, and whether any new defect entered. Not re-deciding the corpus.
- **Prior findings with their instances** — the six from `findings/round-1.md`, rounds 2 and 3's singles from their
  files, and the advisor's two confirmed problems recorded above.
- **Completed checks** — `spotlessApply` and `spotlessCheck` after the last edit; `--unique-rounds --expect …:4` and
  `--session-continuity` pass.
- **Known limitations** — P1's net line delta is unmeasured until the edit lands; the 59 `Decision` lines do not exist
  yet; implementation begins only after the owner's proposal approval.
- **Deadline** — none fixed: this record exists so a stall is recoverable from it.

## Round 6 — implementation screen

- **Unit** — `generalize-process-defects-2026-11-07`, a guidance unit with no change dir. The implementation applies P1
  to `AGENTS.md`, parks R1 in `docs/ideas.md`, and appends 59 `Decision` lines.
- **Revisions** — base `origin/main` `1a4ece43c9fca466bfb439fa7d63d350be4ad521`; the work is uncommitted on the branch.
- **Workspace** — `/Users/afanasyev/Development/axon-showcase`, branch `generalize-process-defects-2026-11-07`.
- **Packs** — `none`. The artifacts decide no JVM, test, build, or deployment subject; the guidance they change is
  agent-tooling. Classify independently and report a missing pack rather than searching for one.
- **Supplied artifacts** — the diff against the base: `AGENTS.md` (one bold-led bullet in "Subject guidance packs",
  `git diff --numstat` `5 0`); `docs/ideas.md` (the R1 entry under `## 2026-11-07`); the 59 `Decision` lines in 22
  `docs/units/*/process-defects.md` files; and this unit's own files. The PR-body draft and its claims table are at
  `/private/var/folders/rd/nw7pt03523dc_z_pzq1_rdjw0000gn/T/opencode/generalize-process-defects-2026-11-07-pr.md`; the
  record texts at `.../undecided.md`; the map at `.../dispositions.md`; `lesson-capture.md` is supplied per
  `review-gate` §2.
- **Scope** — whether P1 landed as the owner approved it (the narrowed form), whether each `Decision` line matches its
  record's disposition, whether the R1 entry is placed and tagged correctly, whether the measured net delta and the
  claims table reproduce, and whether the gates cover the changed set. Not re-deciding the corpus.
- **Prior findings with their instances** — rounds 1–3 as recorded, round 2's and round 3's singles, and the advisor's
  two confirmed problems, all in this file's earlier sections.
- **Completed checks (corrected after round 6's finding)** — `openspec validate --all` 26 passed, 0 failed; the root
  `check` was green before this section was appended, and this section's own unwrapped lines falsified it until
  `spotlessApply` ran; the check recorded in Round 7 covers the formatted section. The 120-character scan over the
  changed set flags only formatter-left table rows, record headings, and the record's `Workspace:` line; the anchored
  `Decision` scan reads `records 294 decided 292 undecided 2` (this unit's `#1` and `#2`).
- **Known limitations** — the five `proposed` lines carry the applied site, not a pull-request link, which lands before
  the merge; R1 is parked, not implemented; this unit's `process-defects.md#1` and `#2` stay undecided for a later pass.
- **Deadline** — none fixed: this record exists so a stall is recoverable from it.

## Round 7 — implementation screen, after round 6's fixes

- **Unit, tree, branch, base** — unchanged from round 6.
- **Packs** — `none`, for the same reason.
- **Supplied artifacts** — the same implementation with round 6's fixes: this section is formatted before the check
  (round 6's own false-green is recorded as `process-defects.md#2`); the Round 6 check line is corrected; the claims
  table's pre-change row is anchored to base `1a4ece4` with its counts in the past tense; the `Decision`-count row
  states the 59 as the difference (233 decided at the base → 292 now). Everything else stands.
- **Scope** — whether round 6's two findings are fixed at their causes, whether the formatted hand-over now passes
  `spotlessMarkdownCheck`, and whether any new defect entered. Not re-deciding the corpus.
- **Prior findings with their instances** — rounds 1–5 and the advisor's two as recorded, plus round 6's two from
  `findings/round-6.md`.
- **Completed checks** — `spotlessApply` after the last edit; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
  exit 0; `openspec validate --all` 26 passed, 0 failed; the 120-character scan over the changed set flags only
  formatter-left table rows, record headings, and the record's `Workspace:` line; the anchored `Decision` scan reads
  `records 294 decided 292 undecided 2` (this unit's `#1` and `#2`).
- **Known limitations** — the five `proposed` lines carry the applied site, not a pull-request link, which lands before
  the merge; R1 is parked, not implemented; this unit's `process-defects.md#1` and `#2` stay undecided for a later pass.
- **Deadline** — none fixed: this record exists so a stall is recoverable from it.

## Round 8 — implementation screen, after round 7's fix

- **Unit, tree, branch, base** — unchanged from rounds 6–7.
- **Packs** — `none`, for the same reason.
- **Supplied artifacts** — the same implementation with round 7's fix: the counts are re-derived from the post-`#2` tree
  and read `records 294 decided 292 undecided 2` in the unit record, this file's Round 6 and Round 7 lines, and the
  claims table's row; the "stays undecided" text names this unit's `#1` and `#2`. Everything else stands.
- **Scope** — whether round 7's finding is fixed at every count site, whether any count remains stale, and whether any
  new defect entered. Not re-deciding the corpus.
- **Prior findings with their instances** — rounds 1–6 and the advisor's two as recorded, plus round 7's instance from
  `findings/round-7.md`.
- **Completed checks** — `spotlessApply` after the last edit; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
  exit 0; the anchored `Decision` scan reads `records 294 decided 292 undecided 2` (this unit's `#1` and `#2`); the
  120-character scan flags only formatter-left content.
- **Known limitations** — the five `proposed` lines carry the applied site, not a pull-request link, which lands before
  the merge; R1 is parked, not implemented; this unit's `process-defects.md#1` and `#2` stay undecided for a later pass.
- **Deadline** — none fixed: this record exists so a stall is recoverable from it.

## Round 9 — implementation screen, after the deep review's fixes

- **Unit, tree, branch, base** — unchanged from rounds 6–8.
- **Packs** — `none`, for the same reason.
- **Supplied artifacts** — the same implementation with the deep review's three citation fixes:
  `reconcile-review-records#3` cites `review-gate` §2 step 3's re-stage, `reconcile-review-records#1` attributes the
  process-defect record to `AGENTS.md`'s record workflow, and `verify-air-model-pin#1` cites the discrimination rule
  with the `git diff --` mechanism instead of the 120-character recipe; `unit.md` row 67 matches. Deep pass 2 reports
  nothing remains. Everything else stands.
- **Scope** — whether the citation fixes are reflected at their sites and no new defect entered; this is the closing
  round over the final tree. Not re-deciding the corpus.
- **Prior findings with their instances** — rounds 1–8, the advisor's three, and deep pass 1's single should-fix, now
  `FIXED_AND_VERIFIED` by pass 2.
- **Completed checks** — `spotlessApply` after the last edit; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
  exit 0; `--unique-rounds --expect …:8` and `--session-continuity` pass; the anchored `Decision` scan reads
  `records 294 decided 292 undecided 2` (this unit's `#1` and `#2`).
- **Known limitations** — the five `proposed` lines carry the applied site, not a pull-request link, which lands before
  the merge; R1 is parked, not implemented; this unit's `process-defects.md#1` and `#2` stay undecided for a later pass.
- **Deadline** — none fixed: this record exists so a stall is recoverable from it.

## Round 10 — implementation screen, after round 9's fix

- **Unit, tree, branch, base** — unchanged from rounds 6–9.
- **Packs** — `none`, for the same reason.
- **Supplied artifacts** — the same implementation with round 9's fix: `unit.md` row 65 now splits the coverage of
  `reconcile-review-records#1` between `review-gate` §2 (row, instances, hand-over) and `AGENTS.md`'s record workflow
  (process-defect record), matching its `Decision`; every other row and `Decision` stands as round 9 left them.
- **Scope** — whether round 9's single unswept site is fixed and no new defect entered; this is the closing round over
  the final tree. Not re-deciding the corpus.
- **Prior findings with their instances** — rounds 1–8, the advisor's three, deep pass 1's should-fix (verified by pass
  2), and round 9's instance from `findings/round-9.md`.
- **Completed checks** — `spotlessApply` after the last edit; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
  exit 0; `--unique-rounds --expect …:9` and `--session-continuity` pass; the anchored `Decision` scan reads
  `records 294 decided 292 undecided 2` (this unit's `#1` and `#2`).
- **Known limitations** — the five `proposed` lines carry the applied site, not a pull-request link, which lands before
  the merge; R1 is parked, not implemented; this unit's `process-defects.md#1` and `#2` stay undecided for a later pass.
- **Deadline** — none fixed: this record exists so a stall is recoverable from it.

## Correction — the five proposed lines' link and the undecided records (2026-11-07)

The delivery rounds' Known-limitations entries (rounds 6–10) say the five `proposed` lines' pull-request link "lands
before the merge", and name this unit's `process-defects.md#1` and `#2` as the undecided records; the earlier rounds'
entries concern other limitations. Both statements were falsified: #859 merged without the link, and the follow-up docs
unit `link-generalization-decision-records` added `https://github.com/aanbrn/axon-showcase/pull/859` to the five lines
and appended process-defect `#3`, which stays undecided with `#1` and `#2`. The sections above stand as written,
corrected here.
