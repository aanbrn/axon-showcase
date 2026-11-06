# Review hand-over — reconcile-review-records

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `reconcile-review-records`, `/Users/afanasyev/Development/axon-showcase`, branch
  `reconcile-review-records`, base `origin/main` `9ac45da7`; the proposal staged.
- **Packs** — none: the change edits unit records, one capability requirement, one Python check's logic and a skill's
  pointer — no JVM code, test tier, Gradle build decision, or deployment surface.
- **Supplied artifacts** — `proposal.md`, `design.md` with its claims table, `tasks.md`, the MODIFIED delta for
  `showcase/quality/commit-hygiene`, the unit record, and the `docs/ideas.md` removal.
- **Prior findings with their instances** — none; this was the first round.
- **Completed checks** — `openspec validate --all` 27; `openspec validate reconcile-review-records --strict`;
  `spotlessCheck`; `commit-hygiene --review-ready`.
- **Known limitations** — the proposal's noon-hour justification for the round-only scope and the store's file-set check
  is the design's, not evidence from an earlier decision.

## Round 2 — proposal screen, after round 1's twelve findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as round 1.
- **Supplied artifacts** — round 1's twelve findings with a disposition table, and the revised `proposal.md`,
  `design.md`, `tasks.md`, and unit record.
- **Prior findings with their instances** — round 1, in `findings/round-1.md`; all twelve were verified against the
  repository and applied, and one reversed the design's conclusion about round 12.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply` after the last edit.
- **Known limitations** — the round ran in a session of its own without an evidenced recovery case, recorded as a
  continuity violation in `rounds.tsv` and in `process-defects.md#2`.

## Round 3 — proposal screen, after round 2's eleven findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as before.
- **Supplied artifacts** — round 2's eleven dispositions and the revised proposal, design, tasks and unit record.
- **Prior findings with their instances** — rounds 1–2 in `findings/`; all twenty-three were verified and applied.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply` after the last edit.
- **Known limitations** — the round ran in round 2's session, as the owner's instruction to continue resolved the
  recorded violation.

## Round 4 — proposal screen, after round 3's five findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as before.
- **Supplied artifacts** — round 3's five findings with their dispositions, and the revised checkpoint, tasks and claims
  row.
- **Prior findings with their instances** — rounds 1–3 in `findings/`; all twenty-eight were verified and applied.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply` after the last edit.
- **Known limitations** — the producer of this section is the caller, and it is written before the invocation because a
  stall would otherwise leave nothing.

## Round 5 — proposal screen, after round 4's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged, and the index is
  re-staged after round 4's finding that it had gone stale.
- **Packs** — none, as before.
- **Supplied artifacts** — round 4's four findings and their dispositions, with the re-staged index.
- **Prior findings with their instances** — rounds 1–4 in `findings/`; all thirty-two were verified and applied.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply` after the last edit;
  `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation, as the review gate requires.

## Round 6 — proposal screen, after round 5's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as before.
- **Supplied artifacts** — round 5's two findings and their dispositions: the checkpoint re-derived whole from the
  ledger, the index and the next action, and process-defect `#3` appended.
- **Prior findings with their instances** — rounds 1–5 in `findings/`; all thirty-four were verified and applied.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply` after the last edit;
  `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation.

## Consultation — the proposal's scope, before the manual pass

- **Who and why** — `technical-advisor`, on the owner's instruction "Отревьювь с советником до согласия". The question
  was whether the proposal's scope and mechanism are right: the three parked parts plus two additions — the store's
  round-only scope and the check that rejects a file the store does not hold.
- **Its verdict** — retain both additions, but revise first. It judged the cleanup's premise sound, both additions
  necessary ("dropping either leaves an unenforced boundary or an unexplained rejection"), and named three confirmed
  problems: historical corrections were not dated and additive, the design's pre-commit assurance exceeded the guard's
  mechanism, and the previous unit's defect `#6` was unrelated scope.
- **The revisions applied** — task 1.3 gains a dated correction in the round-13 section naming the old and new reason;
  task 3.2 adds its record beneath the file's "no process defect observed" statement instead of replacing it; the
  design's Risks bullet and a new Non-Goal state that the guard's staged selection is untouched, so the check is the
  protection; `#6` leaves the change and was parked in `docs/ideas.md`, and `correct-merged-unit-records` records it as
  `assert-paketo-pin-coverage#6`.
- **The follow-up** — "proceed to the owner's manual pass", with no confirmed problems remaining and the scope reopened
  only if implementation needs another record kind or guard behaviour.
- **What it is not** — a recommendation, not approval: the manual pass is the owner's, and implementation and delivery
  each need their own word.

## Round 7 — implementation screen

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none: the implementation edits two units' records, one capability requirement (unchanged since the
  proposal), one Python check's logic and its tests, a skill pointer, and three process-defect records — no JVM code, no
  JVM test tier, no Gradle build decision, no deployment surface.
- **Supplied artifacts** — the implementation: `scripts/commit-hygiene.py`'s `store_stray_files` and the mode's new
  failure, its two tests, the four folds with their repointed references and deleted files, round 12's corrected reason
  with the dated note, the two merged units' process-defect records, `#3`'s correction, and the review gate's writer
  bullet.
- **Prior findings with their instances** — rounds 1–6 in `findings/`, plus the advisor consultation's three revisions,
  all applied.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green (1 m 8 s);
  `python3 scripts/test-commit-hygiene.py` exit 0; `openspec validate --all` 27; `spotlessApply` then `spotlessCheck`;
  `--finding-instances`, `--session-continuity`, `--unique-rounds --expect-clean`, `--workspace-records` green;
  `review-routing.py` returned `NOT_NEEDED`.
- **Known limitations** — the two merged units' fold is a historical edit, verified by a class-and-location grep against
  the deleted files rather than by a gate.

## Round 8 — implementation screen, after round 7's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as round 7.
- **Supplied artifacts** — round 7's four findings and their dispositions: the correction now names round 12, the
  checkpoint names the finished unit, the duplicated Done prefix is gone, and the seven long lines are wrapped.
- **Prior findings with their instances** — rounds 1–7 in `findings/`, and the consultation's three revisions in the
  Consultation section above.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green in 1 m 9 s;
  `python3 scripts/test-commit-hygiene.py` exit 0; `openspec validate --all` 27; `spotlessApply` then `spotlessCheck`;
  `--finding-instances`, `--session-continuity`, `--unique-rounds --expect-clean`, `--workspace-records` green.
- **Known limitations** — written before this round's invocation.

## Round 9 — implementation screen, after round 8's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as round 7.
- **Supplied artifacts** — round 8's three findings and their dispositions: the Change set names the record directory
  and its ledger instead of a moving count, the six wraps are re-flowed at natural boundaries, and the round-8
  prior-findings line points at the Consultation section.
- **Prior findings with their instances** — rounds 1–8 in `findings/`, and the consultation's three revisions in the
  Consultation section above.
- **Completed checks** — `python3 scripts/test-commit-hygiene.py` exit 0; `spotlessApply`; both Python files within 120
  characters by the `perl` recipe; `--finding-instances`, `--session-continuity`, `--unique-rounds`, `--review-ready`
  green.
- **Known limitations** — written before this round's invocation; `check` was not re-run after round 8's wraps, which
  touch comments and docstrings only, and will run before the manual pass.

## Round 10 — implementation screen, after round 9's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as round 7.
- **Supplied artifacts** — round 9's two findings and their dispositions: task 0.1 states the record set's shape rather
  than a tally, task 4.1's note reads "Done:" once, and `check` was re-run after the wraps — green in 1 m 4 s — with
  task 4.3's evidence refreshed to that run.
- **Prior findings with their instances** — rounds 1–9 in `findings/`, and the consultation's three revisions in the
  Consultation section above.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green in 1 m 4 s;
  `python3 scripts/test-commit-hygiene.py` exit 0; `openspec validate --all` 27; `spotlessApply`; `--finding-instances`,
  `--session-continuity`, `--unique-rounds`, `--review-ready` green.
- **Known limitations** — written before this round's invocation.

## Consultation — the implementation, before the manual pass

- **Who and why** — `technical-advisor`, follow-up, on the owner's instruction "Отревьювь с советником до согласия". The
  question was whether the implementation is a faithful and sufficient execution of the approved scope.
- **Its verdict** — two bounded corrections first, then proceed; it also settled the two judgements I asked for: the
  fold keeps every substantive claim (classes, locations, the clean second pass), and the check's post-commit arrival is
  the right protection, because `verifyFindingInstances` runs in the pull request's quality gate — allowing a bad local
  commit is not allowing it through verification.
- **First correction, applied** — the file-set scan examined only immediate children, so a nested
  `findings/consultations/advisor-1.md` was skipped. `store_stray_files` now rejects any tracked descendant below the
  store's directory, and a nested arm joins the tests. Control: with the depth condition narrowed to the immediate
  children, that arm fails (`0 != 1`).
- **Second correction, applied** — the pin unit's implementation-pass section was placed before round 5 and carried a
  contemporary hand-over's fields, implying it preceded rounds it postdates. It is now a section labelled as a
  reconstruction, dated 2026-11-06, stating that the pass had no round of its own and asserting no place in the round
  sequence; its entries are unchanged.
- **What it is not** — a recommendation, not approval; the manual pass is the owner's.

## Round 11 — implementation screen, after the consultation's two corrections

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as round 7.
- **Supplied artifacts** — the consultation's two corrections: `store_stray_files` now rejects any tracked descendant
  below `findings/` with a nested test arm, and the pin unit's implementation-pass section is a dated reconstruction
  rather than a contemporary hand-over.
- **Prior findings with their instances** — rounds 1–10 in `findings/`, and the consultation's corrections in the
  Consultation section above.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green in 1 m 4 s;
  `python3 scripts/test-commit-hygiene.py` exit 0 with three stray-file arms; both stray arms fail against the pre-fix
  scan; `spotlessApply`; `--finding-instances`, `--session-continuity`, `--unique-rounds --expect-clean`,
  `--review-ready` green.
- **Known limitations** — written before this round's invocation.

## Round 12 — implementation screen, after round 11's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as round 7.
- **Supplied artifacts** — round 11's two findings and their dispositions: the design and task 2.3 now cost the check at
  three arms, and the pin unit's round-4 pointer reads "above".
- **Prior findings with their instances** — rounds 1–11 in `findings/`, and the consultation's corrections in the
  Consultation section above.
- **Completed checks** — `python3 scripts/test-commit-hygiene.py` exit 0; `spotlessApply`; `--finding-instances`,
  `--session-continuity`, `--unique-rounds`, `--review-ready` green.
- **Known limitations** — written before this round's invocation.

## Round 13 — implementation screen, after round 12's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as round 7.
- **Supplied artifacts** — round 12's two findings and their dispositions: task 2.3 enumerates all three arms and the
  checkpoint costs the check at three tests.
- **Prior findings with their instances** — rounds 1–12 in `findings/`, and the consultation's corrections in the
  Consultation section above.
- **Completed checks** — `python3 scripts/test-commit-hygiene.py` exit 0; `spotlessApply`; `--finding-instances`,
  `--session-continuity`, `--unique-rounds`, `--review-ready` green.
- **Known limitations** — written before this round's invocation.

## Round 14 — implementation screen, after round 13's one finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `9ac45da7`; every unit file staged.
- **Packs** — none, as round 7.
- **Supplied artifacts** — round 13's finding and its rebuttal: the tasks carry the joined-text command, not the
  `git grep` form the finding quotes, and both commands were run on the final tree — 0 hits in each hand-over. The one
  new mention of a deleted file is the pin unit's reconstruction provenance, which states where the entries came from
  rather than pointing a reader at the file.
- **Prior findings with their instances** — rounds 1–13 in `findings/`, and the consultation's corrections in the
  Consultation section above.
- **Completed checks** — both fold commands re-run (0 each); `python3 scripts/test-commit-hygiene.py` exit 0;
  `spotlessApply`; `--finding-instances`, `--session-continuity`, `--unique-rounds`, `--review-ready` green.
- **Known limitations** — written before this round's invocation.

## Correction — the ledger's sixth field (2026-11-06)

- **What the field means** — the review-round record's sixth field is the number of findings that missed an existing
  rule — `AGENTS.md`, a subject pack, the specs, or the reviewer's definition — as `showcase/quality/commit-hygiene`'s
  record-format requirement names it and `.opencode/agent/review-quick.md` defines it.
- **What the rows carry** — every row of `rounds.tsv` carries, in that field, the number of finding instances its
  `findings/round-<N>.md` holds, which is consistent with the field having been written from the finding count, and
  `unit-layout-allowlist`'s process defect `#2` records that method error, found when a later unit compared its own row
  against the reviewer's line.
- **What the true values are** — unestablished. The reviewer's per-round compliance figure was session-only — the stored
  instances carry each finding's class, not the judgment that it missed a rule — so equality with the finding count does
  not determine the right figure: every finding might have qualified as a compliance miss, and a clean round's zero is
  correct either way. A summary derived from the field — `scripts/review-rounds.py` sums it — inherits that, so such a
  digest is not a verified compliance figure.
- **What this note does not do** — it does not rewrite the numbers. They stand as recorded; a later pass that judges
  each finding against the rules in force at its round's date is a dated retrospective reclassification, not recovery of
  the original figures.
