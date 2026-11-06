# Review hand-over — unit-layout-allowlist

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `unit-layout-allowlist`, `/Users/afanasyev/Development/axon-showcase`, branch
  `unit-layout-allowlist`, base `origin/main` `d420246c`; the proposal staged.
- **Packs** — `gradle-build`: the change registers a task in the build and decides that it joins `check`, which is the
  gate membership the pack governs. `jvm-development`, `jvm-testing` and `deployment-operations` do not apply — the
  tests are Python, there is no JVM production code, and no image, chart value or environment/port wiring is touched.
- **Supplied artifacts** — `proposal.md`, `design.md` with its claims table, `tasks.md`, the delta (an added requirement
  and a modified one), and the unit record.
- **Prior findings with their instances** — none; this is the first round.
- **Completed checks** — `openspec validate --all` 27; `openspec validate unit-layout-allowlist --strict` valid;
  `spotlessCheck`; `verifyIdeasSections`; `commit-hygiene --review-ready`.
- **Known limitations** — the proposal states the layout as five kinds; whether a sixth kind is wanted is the owner's
  call, and the design's Non-Goals record what the rule deliberately excludes.

## Round 2 — proposal screen, after round 1's seven findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as round 1.
- **Supplied artifacts** — round 1's seven findings and their dispositions: the retained store scenario now names the
  layout's verification, the claims rows cite `d420246c`, a `### Dismissals` block covers the scan's four candidate
  kinds, task 2.3 names both stray shapes, the Why states the store-subtree enforcement, the grep row is scoped and the
  idea enumeration named, and task 2.2 names the sibling's command shape.
- **Prior findings with their instances** — round 1 in `findings/round-1.md`.
- **Completed checks** — `openspec validate --all` 27; `openspec validate unit-layout-allowlist --strict` valid;
  `spotlessApply`; `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation; the plan's edits do not exist yet, so their post-edit
  state is unverified by definition.

## Round 3 — proposal screen, after round 2's five findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as round 1.
- **Supplied artifacts** — round 2's five findings and their dispositions: the dismissals state one reason per kind by
  class rather than by location, the sixth field is the compliance-miss count (`1`), the checkpoint reaches round 1's
  closed state, the claim cells are scoped to the guidance and specs the grep covers, and the Non-Goals point at the
  claims row instead of repeating its tallies.
- **Prior findings with their instances** — rounds 1–2 in `findings/`.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply`; `verifyWorkspaceRecords`; `verifyIdeasSections`;
  `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation.

## Round 4 — proposal screen, after round 3's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as round 1.
- **Supplied artifacts** — round 3's three findings and their dispositions: the checkpoint is advanced when a row is
  appended and now names rounds 1–2 closed with round 3 in flight; the defect cites only what the repository shows (the
  rows against their instance counts), and the idea parked at the time carried the same figures and an executable remedy
  — `correct-merged-unit-records` records that correction; the dismissals reach the compound-name and table-row shapes.
- **Prior findings with their instances** — rounds 1–3 in `findings/`.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply`; `verifyIdeasSections`;
  `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation.

## Round 5 — proposal screen, after round 4's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as round 1.
- **Supplied artifacts** — round 4's three findings and their dispositions: the checkpoint's Stage now derives its state
  from `rounds.tsv` instead of enumerating rounds, process-defect `#2`'s Expected names the reviewer's definition as the
  field's source, and the `cross-ref` dismissal reaches slash-joined paths. Round 5's correction moves `#2`'s Knowledge
  boundary into `#2` itself — the first edit had landed it on `#1` — and restores `#1`'s own.
- **Prior findings with their instances** — rounds 1–4 in `findings/`.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply`; `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation.

## Round 6 — proposal screen, after round 5's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as round 1.
- **Supplied artifacts** — round 5's two findings and their dispositions: record `#2` now carries the boundary that
  belongs to it, and record `#1`'s own boundary is restored; the two records no longer share a sentence.
- **Prior findings with their instances** — rounds 1–5 in `findings/`.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply`; `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation.

## Consultation — the proposal's scope and wording, before the manual pass

- **Who and why** — `technical-advisor`, on the owner's instruction "Отревьювь с советником до согласия", with a
  follow-up carrying the owner's objection to "at any depth".
- **Its verdict** — retain the ownership split. A new requirement and mode are justified: directory admission and
  round-content validation have different subjects, and the retained store scenario names the layout's verification, so
  the block stays coherent. The boundary belongs at the unit directory; extending it to change directories, audits or
  ideas would import unrelated policies. The new-kind clause is a process obligation review establishes, not a second
  gate. It confirmed the field-6 reading: the format requirement names the field, the reviewer defines it.
- **First correction, applied** — the depth wording conflated inspection with admission. The requirement now admits
  exactly the four first-level filenames and the direct path `findings/round-<N>.md`, and rejects every other complete
  relative path; the predicate's description says recursion and admission are separate; the scenario rejects a third
  level and a path at another location alike, and the failure names the unit, the path and the condition.
- **Second correction, applied** — the planned controls covered only `rounds.tsv` and round files. Task 2.3 now covers
  acceptance of every admitted kind, a misplaced admitted name (`unit.md` under `findings/`) and a wrongly located
  round-shaped path (`other/round-1.md`), so a predicate that rejected an admitted name cannot pass.
- **Third correction, applied** — the historical field-6 claim exceeded the evidence. Process-defect `#2` now separates
  the admitted counting-method error from the unverified merged values, which equality with finding counts does not
  prove wrong; the idea parked at the time said the same, and that a later assessment is a dated retrospective
  reclassification, not a recovery. `correct-merged-unit-records` records the correction.
- **What it is not** — a recommendation, not approval; the manual pass is the owner's.

## Round 7 — proposal screen, after the consultation's three corrections

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as round 1.
- **Supplied artifacts** — the consultation's three corrections: the two-level admission wording across the requirement,
  the design and the tasks; the acceptance coverage for every admitted kind plus two misplacement arms; and the field-6
  record's split between an admitted method error and unverified history.
- **Prior findings with their instances** — rounds 1–6 in `findings/`, and the consultation's corrections in the section
  above.
- **Completed checks** — `openspec validate --all` 27; `openspec validate unit-layout-allowlist --strict` valid;
  `spotlessApply`; `verifyIdeasSections`; `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation.

## Round 8 — proposal screen, after round 7's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as round 1.
- **Supplied artifacts** — round 7's four findings and their dispositions: admission is stated as a name-and-position
  test in the proposal and the design's goal; the failure's content is stated once in the requirement and repeated
  identically; the two controls are separate verify lines; the idea leads with equality and calls the values unverified.
- **Prior findings with their instances** — rounds 1–7 in `findings/`, and the consultation's three corrections in the
  section above them.
- **Completed checks** — `openspec validate --all` 27; `openspec validate unit-layout-allowlist --strict` valid;
  `spotlessApply`; `verifyIdeasSections`; `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation; the new-kind rule moved into a scenario to keep the
  requirement within the schema's length, so the description no longer states it.

## Round 9 — proposal screen, after round 8's one finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as round 1.
- **Supplied artifacts** — round 8's finding and its disposition: task 2.1, the design's third goal and the Decision's
  body now name the condition beside the unit and the path, so the obligation reads the same at every site; the
  Decision's body explains its three parts instead of restating them.
- **Prior findings with their instances** — rounds 1–8 in `findings/`, and the consultation's three corrections in the
  section above them.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply`; `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation.

## Round 10 — proposal screen, after round 9's one finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as round 1.
- **Supplied artifacts** — round 9's finding and its disposition: task 2.1's verify and the Decision's body name all
  three parts, and both were checked on joined text, since a line-oriented grep missed the wrapped phrase twice.
- **Prior findings with their instances** — rounds 1–9 in `findings/`.
- **Completed checks** — `openspec validate --all` 27; `spotlessApply`; `commit-hygiene --review-ready`.
- **Known limitations** — written before this round's invocation.

## Round 11 — implementation screen

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as the proposal rounds.
- **Supplied artifacts** — the implementation: `layout_stray_paths`, `check_unit_layout`, the `--unit-layout` mode with
  its flag, parser entry, dispatch and coverage mapping; the `verifyUnitLayout` task and its `check` membership;
  `UnitLayoutTests`' eight arms; `--finding-instances`' stray clause and its three arms removed; the review gate's
  pointer; and the tasks' Done notes.
- **Prior findings with their instances** — rounds 1–10 in `findings/`, and the consultation's three corrections in the
  section above them.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green in 1 m 8 s with `verifyUnitLayout`
  executing; `python3 scripts/test-commit-hygiene.py` exit 0; `openspec validate --all` 27; `spotlessApply`;
  `--finding-instances`, `--session-continuity`, `--unique-rounds`, `--workspace-records`, `--review-ready` green;
  `review-routing.py` returns `REQUIRED` on `requirement-block-added`.
- **Known limitations** — the deep review is required by that routing and has not run yet; it precedes the manual pass.

## Round 12 — implementation screen, after round 11's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as the proposal rounds.
- **Supplied artifacts** — round 11's three findings and their dispositions: a path with no unit component is skipped
  and a ninth arm covers it; the claims row is anchored to `d420246c`; the eleven Done notes read once.
- **Prior findings with their instances** — rounds 1–11 in `findings/`.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green in 1 m 8 s;
  `python3 scripts/test-commit-hygiene.py` exit 0; `openspec validate --all` 27; `spotlessApply`; the six ledger modes
  green.
- **Known limitations** — written before this round's invocation.

## Round 13 — implementation screen, after round 12's one finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as the proposal rounds.
- **Supplied artifacts** — round 12's finding and its disposition: task 2.3's Done note counts nine arms and names the
  outside-a-unit-directory arm.
- **Prior findings with their instances** — rounds 1–12 in `findings/`.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green, re-run after the correction;
  `spotlessApply`; the six ledger modes green.
- **Known limitations** — written before this round's invocation.

## Round 14 — implementation screen, after round 13's one finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as the proposal rounds.
- **Supplied artifacts** — round 13's finding and its disposition: task 2.3's Verify sentence is whole again, and its
  Done note begins as its own sentence.
- **Prior findings with their instances** — rounds 1–13 in `findings/`.
- **Completed checks** — `spotlessApply`; the six ledger modes green; `python3 scripts/test-commit-hygiene.py` exit 0.
- **Known limitations** — written before this round's invocation; the full `check` last ran before this text-only edit.

## Round 15 — implementation screen, after round 14's one finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as the proposal rounds.
- **Supplied artifacts** — round 14's finding and its disposition: all four dismissal reasons are stated by class, and
  `count` no longer enumerates categories.
- **Prior findings with their instances** — rounds 1–14 in `findings/`.
- **Completed checks** — `spotlessApply`; the six ledger modes green.
- **Known limitations** — written before this round's invocation.

## Deep review — the routing decision's required pass

- **Who and why** — `review-thorough`, in its own session `ses_e4ff042c4ffeg07bUWXO0uAVPp`, because `review-routing.py`
  returned `REQUIRED` on `requirement-block-added`. The ledger's round 16 records the session change as
  `replaced`/`definition-changed`: the deep pass runs another definition. Its report states no ledger line, so the sixth
  field is a **caller-derived classification**, not a recovered reviewer figure, and it records `3`: severity and
  compliance are independent, and each of the three findings anchors to an existing obligation — the lost negative to
  the store requirement's prohibition, the mode survey's omission to the rule that a list must not read as complete
  while naming a subset, and the wrong ordinal to the rule that a documented claim matches its source. A dated
  correction, 2026-11-06: the first derivation recorded `1`, excluding the two nitpicks for their severity; that was
  wrong, and the row and this note now say so.
- **Its verdict** — three findings: one should-fix and two nitpicks, and everything else verified — the requirement →
  check → task chain complete, the two-level semantics exact, the split honest, nothing lost by the store clause's
  removal, the controls sound (re-derived, not re-run), the build side matching its sibling, and the records' counts
  reproducing.
- **The should-fix, applied** — the writer bullet had lost its "never in a round file" negative, and the layout
  requirement cannot carry it: a round file is an admitted path, so the layout check passes and the store check passes
  while the classes agree. The negative is restored in the skill and in the delta's store description, beside the
  positive destination rather than left to the layout pointer.
- **The nitpicks, applied** — task 2.3's control names the outside-a-unit arm rather than a number, and the module
  docstring's mode survey gains `--unit-layout` and `--finding-instances`.

## Round 17 — implementation screen, after the deep review's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d420246c`; every unit file staged.
- **Packs** — `gradle-build`, as before.
- **Supplied artifacts** — the deep review's three findings and their dispositions: the "never in a round file" negative
  is restored in the skill and in the delta's store description; task 2.3's control names the arm instead of numbering
  it; the module docstring's survey covers `--unit-layout` and `--finding-instances`.
- **Prior findings with their instances** — rounds 1–16 in `findings/`; round 16 holds the deep review's.
- **Completed checks** — `check -PskipITs -Pcoverage.gate.enabled=false` green in 1 m 7 s after the corrections;
  `python3 scripts/test-commit-hygiene.py` exit 0; `openspec validate --all` 27; `spotlessApply`; the six ledger modes.
- **Known limitations** — written before this round's invocation.

## Consultation — the implementation, before the manual pass

- **Who and why** — `technical-advisor`, follow-up, on the owner's instruction "Отревьювь с советником до согласия". The
  question was whether the implementation carries the approved scope and whether the query's four judgements hold.
- **Its verdict** — retain the implementation. It confirmed the two-level semantics and optional presence as
  implemented, the removal as a transfer of enforcement rather than a loss (the layout rejects both noncanonical shapes;
  the store keeps round-content validation; both tasks remain in `check`), and the top-level skip as the correct
  boundary — a file under `docs/units/` lies outside any unit's dossier, and moving material out of a dossier escapes
  this check's jurisdiction, not the process's obligations. It advised not widening this unit again.
- **Its one confirmed problem, applied** — round 16's derived compliance count excluded the two nitpicks for their
  severity, but severity and compliance are independent: each of the three findings anchors to an existing obligation.
  The row now records `3`, the hand-over labels it a caller-derived classification rather than a recovered reviewer
  figure, and a dated correction states that the first derivation recorded `1` and why that was wrong.
- **What it is not** — a recommendation, not approval; the manual pass is the owner's.
