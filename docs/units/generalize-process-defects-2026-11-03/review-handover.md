# Review hand-over — generalize-process-defects-2026-11-03

One section per quick-review invocation, recorded before the caller waits on it.

## Round 1 — implementation screen

- **Unit** — `generalize-process-defects-2026-11-03`, a guidance unit with no change dir. It analyzed the 44
  process-defect records that carried no `Decision` line, appended one `Decision` line to each, and applied the
  improvements the owner approved after a technical-advisor consultation narrowed the set.
- **Revisions** — base `origin/main` `ca9e7dcb154deb5e5ba3f6f5870aa631cac7ee52`; the work is uncommitted on the branch,
  so current content is the working tree at that base.
- **Workspace** — `/Users/afanasyev/Development/axon-showcase`, branch `generalize-process-defects-2026-11-03`.
- **Packs** — `none`. The artifacts decide no JVM, test, build, or deployment subject; the guidance they change is
  agent-tooling. Classify independently and report a missing pack rather than searching for one.
- **Supplied artifacts** — the diff against the base: `AGENTS.md` (one clause replaced in "A parsed command's output is
  a third under-match source"), `.opencode/skills/check-and-test-authoring/SKILL.md` (a restore-from-snapshot control
  bullet), `.opencode/skills/review-gate/SKILL.md` (§2's mid-loop re-stage bullet), `openspec/config.yaml`
  (`rules.tasks` gains a review-gate stage item), `docs/ideas.md` (a third reply-language instance and a new parked
  entry), the 44 records' appended `Decision` lines under `docs/units/*/process-defects.md`, and this unit's own files
  under `docs/units/generalize-process-defects-2026-11-03/`. The pull-request body draft, with its claims table, is
  saved outside the repository at `$TMPDIR/opencode/generalize-process-defects-2026-11-03-pr.md`.
- **Scope** — coherence of the applied rules with the guidance they extend, the `Decision` lines' correspondence to the
  dispositions, and the two records' accuracy. Not re-deciding the dispositions: the owner approved the narrowed set
  after two advisor consultations.
- **Prior findings** — none; this is the unit's first invocation.
- **Completed checks** — `./gradlew spotlessApply` and `./gradlew spotlessCheck` after the last edit;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; `openspec validate --all` (26 passed, 0 failed) and the
  config list-shape probe (no warning line); the 120-character scan over the changed set.
- **Known limitations** — the `proposed` `Decision` lines carry the applied site and not a pull-request link, which
  lands before the merge; the parked corrected-scope consultation needs a `agent-skills` delta in its own change; the
  advisor's second pass checked the disposition map but not each of the 29 covered rows' rule text.
- **Deadline** — none fixed: the record exists so a stall is recoverable from it.

## Round 2 — implementation screen, after round 1's fixes

- **Unit** — same unit, same branch and tree; base `origin/main` `ca9e7dcb`.
- **Packs** — `none`, as round 1.
- **Supplied artifacts** — the same diff, with round 1's seven fixes applied: the over-claim in `review-gate` §2
  reworded; the `check-and-test-authoring` mode now carries its incident and confirmation; the `rules.tasks` item names
  the routed deep review; the unit record's two unsupported statements corrected and the unit count derived; the nested
  backtick span reformatted; and `spotlessApply` re-run after those edits.
- **Prior findings with their instances** — all seven from
  `docs/units/generalize-process-defects-2026-11-03/findings/round-1.md`: the hand-over's `spotlessApply` claim
  (`review-handover.md:27`), the `review-gate` stale-index over-claim (`SKILL.md:169-171`), the
  `check-and-test-authoring` mode's missing confirmation and incident (`SKILL.md:97-102`), the `rules.tasks` stage list
  omitting the deep review (`openspec/config.yaml:122-125`), the unit record's "two more records are parked"
  (`unit.md:25`), the underived unit count (`unit.md:13`), and the nested backtick span (`process-defects.md:49`).
- **Completed checks** — `spotlessApply` and `spotlessCheck` after the last edit, with `spotlessCheck` exit 0;
  `git diff --numstat` re-measured (`AGENTS.md` 15/14, `check-and-test-authoring` 10/0, `review-gate` 5/0,
  `openspec/config.yaml` 4/0, `docs/ideas.md` 24/0); the unit count re-derived (`22`); the two ledger checks pass for
  round 1.
- **Known limitations** — unchanged from round 1; the full `./gradlew check` is re-run after this round's fixes.

## Round 3 — implementation screen, after round 2's fix

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with round 2's one fix: the `check-and-test-authoring` delta corrected to 10/0
  in the unit record and the PR-body draft.
- **Prior findings with their instances** — round 2's single instance
  (`docs/units/generalize-process-defects-2026-11-03/findings/round-2.md`): the `+7` / `7/0` figure for
  `check-and-test-authoring`.
- **Completed checks** — `spotlessApply` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` after the last
  edit (`BUILD SUCCESSFUL in 1m 18s`, 402 tasks); both ledger checks pass for rounds 1–2; `git diff --numstat`
  re-measured for every artifact.
- **Known limitations** — as round 1.

## Round 4 — implementation screen, after the advisor's review

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with two advisor corrections applied: the `check-and-test-authoring` mode is
  narrowed to a control that mutates **a copy of the subject it must keep** (a disposable fixture owes no restore); and
  the `AGENTS.md` replacement is re-worded to `take the tool's machine-readable form`, which makes it **net zero lines**
  (`2 2`). The `sqlite3` example now lives in the record behind the marker rather than in the rule text.
- **Prior findings with their instances** — rounds 1–2 (`docs/units/generalize-process-defects-2026-11-03/findings/`),
  all previously fixed; round 3 was clean.
- **Completed checks** — `spotlessApply` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` after the last
  edit (`BUILD SUCCESSFUL in 1m 13s`, 402 tasks); `git diff --numstat` re-measured (`AGENTS.md` 2/2 net 0,
  `check-and-test-authoring` 11/0, `review-gate` 5/0, `openspec/config.yaml` 4/0, `docs/ideas.md` 24/0); the unit record
  and the PR-body claims table updated to those figures.
- **Known limitations** — as round 1; the dropped `-json` example is stated in `record-ast-grep-skill-usage#1`.

## Round 5 — implementation screen, after round 4's fix

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with round 4's fix: the `check-and-test-authoring` mode now states one mutation
  model — take the snapshot first, never mutate it, mutate the subject or a working copy made from it — with the
  disposable-fixture exception kept.
- **Prior findings with their instances** — rounds 1–4 (`findings/round-1.md` … `round-4.md`); round 4's contradiction
  is the one being re-checked.
- **Completed checks** — `spotlessApply`/`spotlessCheck` after the fix; both ledger checks pass for rounds 1–4;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` re-run after this round's fix.
- **Known limitations** — as round 1.

## Round 6 — implementation screen, after round 5's fix

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with round 5's fix: the PR-body draft's Summary now describes the applied mode
  ("a subject it must keep, or a working copy made from it").
- **Prior findings with their instances** — round 5's `claim-unreproducible` (`findings/round-5.md`).
- **Completed checks** — `spotlessCheck` clean before the invocation; both ledger checks pass for rounds 1–5.

## Round 7 — implementation screen, after round 6's fixes

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with round 6's two fixes: this hand-over's missing Round 6 section, appended
  (the preflight requires one section per invocation, recorded before the wait), and `findings/round-5.md`'s single line
  wrapped, so `spotlessMarkdownCheck` passes.
- **Prior findings with their instances** — round 6's two (`findings/round-6.md`): the missing hand-over section and the
  unwrapped findings line that broke the `spotlessCheck` claim.
- **Completed checks** — `spotlessApply` and `spotlessCheck` after these edits; the full
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; both ledger checks for rounds 1–6.
- **Known limitations** — as round 1; the index is stale by design until §5's re-stage.

## Round 8 — implementation screen, after the advisor's final reconciliation

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with the advisor's two reconciliation fixes: the unit's process-defect record
  gained `#6`, the missing hand-over section; and the PR-body draft's claims table splits the baseline population
  (`233 records in 112 files`) from the selected set's unit count (`22`), which the first row had conflated.
- **Prior findings with their instances** — rounds 1–7 (`findings/`); round 7 was clean, and these two changes follow
  the advisor's review rather than a quick-review finding.
- **Completed checks** — `spotlessApply` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` after the last
  edit (`BUILD SUCCESSFUL`, 402 tasks); both ledger checks and `--finding-instances` for rounds 1–7; `--expect-clean`
  for the unit.
- **Known limitations** — as round 1; the index is re-staged after the closing round.

## Round 9 — implementation screen, after round 8's fix

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with round 8's fix: `process-defects.md#6`'s Evidence now quotes
  `findings/round-6.md`'s own sentence instead of the round report's session-only prose.
- **Prior findings with their instances** — round 8's `claim-unreproducible` (`findings/round-8.md`).
- **Completed checks** — `spotlessApply` after the fix; both ledger checks and `--finding-instances` pass for rounds
  1–8; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` re-run after the fix.
- **Known limitations** — as round 1; the index is re-staged before the manual pass.

## Round 10 — implementation screen, after round 9's fix

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with round 9's fix: round 8's instance location is a stable token
  (`process-defects.md#6`) instead of a line number that the record's own growth moved.
- **Prior findings with their instances** — round 9's `claim-unreproducible` (`findings/round-9.md` is `- none`; the
  finding concerned round 8's stored location).
- **Completed checks** — `spotlessApply` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` after the last
  edit (`BUILD SUCCESSFUL`, 402 tasks); `--unique-rounds --expect …:9`, `--expect-clean`, `--session-continuity`, and
  `--finding-instances` all exit 0.
- **Known limitations** — as round 1; the index is re-staged before the manual pass.

## Round 11 — implementation screen, after round 10's fix

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with round 10's fix: `findings/round-9.md` now carries round 9's
  `claim-unreproducible` instance, and ledger row 9 records that class and its miss, so the store and the row agree on a
  round that did raise a finding.
- **Prior findings with their instances** — round 10's `missing-task` (`findings/round-10.md`).
- **Completed checks** — `spotlessApply` after the fix; `--unique-rounds --expect …:10`, `--session-continuity` and
  `--finding-instances` pass; `--expect-clean` is re-measured at the manual-pass boundary.
- **Known limitations** — as round 1; the index is re-staged before the manual pass.

## Round 12 — closing screen, after the draft's table fix and record `#7`

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with two changes after round 11: the PR-body draft's "22 units" row escapes the
  two pipes its inline command carried (every table row now counts four pipes, `rows 16 bad 0`), and the unit's
  process-defect record gained `#7` for it.
- **Prior findings with their instances** — round 11 was clean; these two changes come from the consultation hand-off
  rather than a quick-review finding.
- **Completed checks** — `spotlessApply` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` after the last
  edit (`BUILD SUCCESSFUL`, 402 tasks); ledger checks for rounds 1–11.
- **Known limitations** — as round 1; the draft is outside every gate, which is why `#7` survived to this point.

## Round 13 — closing screen, after the draft's evidence-row split and record `#8`

- **Unit, tree, branch, base** — unchanged (`ca9e7dcb`).
- **Packs** — `none`.
- **Supplied artifacts** — the same diff with two changes after round 12: the draft's config row is split into a spec-
  validation row (`openspec validate --all`) and a config-read-path row (`openspec new change <probe>`, the path
  `ci.yml`'s probe uses), and `process-defects.md` gained `#8` for the conflation.
- **Prior findings with their instances** — round 12 was clean; these changes come from the consultation.
- **Completed checks** — `spotlessApply` and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` after the last
  edit (`BUILD SUCCESSFUL`); the config probe re-run (`Created change '<probe>' …`, no warning line, directory removed).
- **Known limitations** — as round 1; the draft remains outside every gate.
