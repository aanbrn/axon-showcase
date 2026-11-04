# Review hand-over — add-projector-state-dashboard-panels

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `add-projector-state-dashboard-panels`; tree
  `/Users/afanasyev/Development/axon-showcase` (the primary checkout, this unit's tree); branch
  `add-projector-state-dashboard-panels`; base `origin/main` `96edb035`; nothing committed, all unit files staged.
- **Packs** — `deployment-operations` (the bundled dashboard and the chart's observability wiring), `gradle-build` (the
  `check` gate and its `build-logic` rules), `jvm-testing` (the new projection-service unit test), plus the
  `check-and-test-authoring` skill for the gate rules.
- **Supplied artifacts** — the change's `proposal.md`, `design.md` (with the claims table), `tasks.md`, and the
  `showcase/quality/code-quality` delta; the unit record `docs/units/add-projector-state-dashboard-panels/unit.md`.
- **The proposed change** — three panels in the bundled dashboard's Projection Service section (state, in-flight
  batches, age since the last resolved batch), three panel-level rules added to the existing `verifyDashboardJson` gate
  with their controls, a projection-service test pinning the three Prometheus exposition names, a `MODIFIED` delta on
  `code-quality`, the README's panel count and wording, a pointer sentence in the `deployment-operations` pack, and the
  parked `docs/ideas.md` entry removed.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate --all` (27 items) and
  `openspec validate add-projector-state-dashboard-panels --strict` pass; `spotlessApply` has run; the projection is
  staged and `commit-hygiene --review-ready` passes; the claims table's sources were executed at draft time (Prometheus
  queries against the local cluster, the base-revision dashboard dump, the Grafana read-back).
- **Known limitations** — the panel queries are verified against the local cluster at implementation, not at proposal
  time; the dashboard gate's rules are structural and cannot judge a query's meaning (design.md, "Risks / Trade-offs").

## Round 2 — proposal screen, after round 1's eleven findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; all unit files staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — the same four planning artifacts with round 1's fixes: decision 4 now states the three rules
  as bullets with their scope (ids at any depth; geometry and expressions over the top-level panels, rows included in
  the geometry comparison; `text` exempt from the expression rule) and decision 1 states the panels' height, ids,
  datasource, and selector; the risk paragraph narrows what the overlap rule controls; the delta's requirement and
  scenarios carry the same scope; proposal's Impact and the gate bullet are reworded; task 1.1 carries the object
  parameters and task 2.3 the snapshot-and-restore control; the claims table's "every base panel is a timeseries" row
  became "every non-row panel", and the "no production source set" row now names `build-logic`'s rules object as build
  tooling; the dismissals gained the `cross-ref` kind; the unit record states the staged set and schedules the advisor
  consultation as a proposal to the owner rather than an unconditional step.
- **Prior findings with their instances** — round 1's eleven, listed in `findings/round-1.md`.
- **Completed checks** — `openspec validate add-projector-state-dashboard-panels --strict` passes; `spotlessApply` has
  run; `commit-hygiene --review-ready` passed before the round.
- **Known limitations** — the panel queries and the provisioned read-back are verified at implementation; the gate's
  structural rules cannot judge a query's meaning.

## Round 3 — proposal screen, after round 2's five findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 2's fixes applied: the expression rule is now "every target a top-level panel declares
  carries a non-blank `expr`", with no panel-type list — the design states the surface it leaves out (a panel declaring
  no target at all) as a risk and an alternative rejected (a query-rendering type list rots, and a `text`-only exemption
  failed the `dashlist` case); the delta's requirement, fail scenario, and pass scenario carry the same predicate and
  the same top-level scope as the other two rules; the proposal's gate bullet matches; task 0.1 points at the design's
  dismissal block instead of restating its kinds; task 2.2 names three fixtures (nested duplicate id, a row panel that
  meets the panel below it, a blank target expression) and a tiling known-good; the unit record's stage and change set
  state the staged tree.
- **Prior findings with their instances** — rounds 1 and 2, in `findings/round-1.md` and `findings/round-2.md`.
- **Completed checks** — `openspec validate add-projector-state-dashboard-panels --strict` passes; `spotlessApply` has
  run; `commit-hygiene --review-ready` passed before the round.
- **Known limitations** — the panel queries and the provisioned read-back are verified at implementation; the gate's
  structural rules cannot judge a query's meaning; a panel declaring no target is outside the expression rule by design.

## Round 4 — proposal screen, after round 3's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 3's fixes: the Goal, the proposal's Modified Capabilities line, and the added
  scenario's title now state the covered predicate (a declared target whose expression is emptied) rather than "a panel
  that queries nothing"; task 2.2 adds a target that omits `expr` and a `text` panel declaring no target as the
  known-good accepted branch; the unit record counts rounds 1 to 3.
- **Prior findings with their instances** — rounds 1–3, in `findings/round-1.md` to `findings/round-3.md`.
- **Completed checks** — `openspec validate add-projector-state-dashboard-panels --strict` passes; `spotlessApply` has
  run; `commit-hygiene --review-ready` passes.
- **Known limitations** — implementation-time verification only for the panel queries and the provisioned read-back; the
  expression rule's accepted surface (a target-less panel) is a stated risk.

## Round 5 — proposal screen, after round 4's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 4's fix: task 2.2's fixture list now covers every condition the delta states, including
  a top-level panel without `gridPos` and a panel whose first target carries a query beside a second whose `expr` is
  blank, plus the target-omitting-`expr` case; the known-good dashboard keeps the target-less `text` panel.
- **Prior findings with their instances** — rounds 1–4, in `findings/round-1.md` to `findings/round-4.md`.
- **Completed checks** — `openspec validate add-projector-state-dashboard-panels --strict` passes; `spotlessApply` has
  run; `commit-hygiene --review-ready` passes.
- **Known limitations** — implementation-time verification only for the panel queries and the provisioned read-back; the
  expression rule's accepted surface (a target-less panel) is a stated risk.

## Round 6 — proposal screen, after round 5's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 5's fix: the unit record no longer counts rounds or names a round as the next step; its
  Stage points at `rounds.tsv` and `findings/` as the round inventory and its Next step is the owner's manual pass, with
  the advisor consultation proposed alongside it.
- **Prior findings with their instances** — rounds 1–5, in `findings/round-1.md` to `findings/round-5.md`.
- **Completed checks** — `openspec validate add-projector-state-dashboard-panels --strict` passes; `spotlessApply` has
  run; `commit-hygiene --review-ready` passes.
- **Known limitations** — implementation-time verification only for the panel queries and the provisioned read-back; the
  expression rule's accepted surface (a target-less panel) is a stated risk.

## Round 7 — proposal screen, after the advisor's consultation

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — the advisor's confirmed problem and hypothesis applied: decision 2 now specifies the state
  panel as an instant, `lastNotNull`-reduced query whose absent series renders as `noValue` "No data" on the neutral
  base color (distinct from the three mapped values), with the panel's description carrying the source's own meaning
  rather than a health verdict; decision 3's age panel legend reads "since last resolved batch, idle time included"; the
  Verification section and task 5.2 assert the provisioned panel objects (instant/range/calcs, mappings, `noValue`, no
  age threshold), the running projector returning `1`, and a no-match selector returning no series, with the retry and
  stopped cases recorded as not induced live; task 1.1 and 1.2 carry the same parameters; the risks record the accepted
  limit of the live check.
- **Prior findings with their instances** — rounds 1–6, in `findings/round-1.md` to `findings/round-6.md`.
- **Completed checks** — `openspec validate add-projector-state-dashboard-panels --strict` passes; `spotlessApply` has
  run; `commit-hygiene --review-ready` passes.
- **Known limitations** — Retrying and Stopped are not induced live; the gate's structural rules cannot judge a query's
  meaning.

## Round 8 — proposal screen, after round 7's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 7's fix: the resume checkpoint is rewritten as one snapshot rather than patched field
  by field — the Stage records the finished, reviewed proposal including the advisor's applied amendments, the Next step
  is the owner's manual pass alone, and the Pending decision is the approval that gates implementation.
- **Prior findings with their instances** — rounds 1–7, in `findings/round-1.md` to `findings/round-7.md`.
- **Completed checks** — `openspec validate add-projector-state-dashboard-panels --strict` passes; `spotlessApply` has
  run; `commit-hygiene --review-ready` passes.
- **Known limitations** — Retrying and Stopped are not induced live; the gate's structural rules cannot judge a query's
  meaning; the stat panel's rendered look is asserted through its provisioned object, not a screenshot.

## Round 9 — implementation screen, first pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; 23 files staged, nothing committed.
- **Packs** — `deployment-operations` (the dashboard JSON), `gradle-build` (the gate and its rules object),
  `jvm-testing` (the projection-service test), plus `check-and-test-authoring`.
- **Supplied artifacts** — the implementation: three panels in the bundled dashboard (`gridPos.y=104`, ids 84–86, the 18
  panels at or below shifted by 7); `DashboardJsonRules` gains the repeated-id (recursive), placed/overlapping (rows
  included), and blank-expression (per declared target) rules with nine new cases in `DashboardJsonRulesTests`;
  `ShowcaseProjectorStateMonitorPrometheusTests` pins the three exposition names; the `code-quality` delta was written
  at proposal stage; `README.md` (42 panels / 7 sections and the projector's operating state), the
  `deployment-operations` pointer sentence, and the `docs/ideas.md` removal.
- **Prior findings with their instances** — rounds 1–8, all proposal-stage, in `findings/round-1.md` to
  `findings/round-8.md`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green (a chart-dependency fetch
  failed once and succeeded on retry, its own bounded-retry path); `DashboardJsonRulesTests` 14 tests; each rule's
  removal fails only its own cases (1/3/3); duplicating a panel id makes `verifyDashboardJson` fail naming both panels;
  renaming the state gauge fails the exposition test; the release was upgraded to revision 2 and Grafana returns 49
  panels with the three titles and the asserted settings; the state query returns 1 and a no-match selector returns no
  series.
- **Known limitations** — Retrying and Stopped were not induced live; the gate's rules are structural; the stat panel's
  rendered appearance is asserted through its provisioned object and the live data, not a screenshot.
- **Process defects** — `#1` (a range replacement deleted a section heading) and `#2` (a control's `git checkout --`
  discarded the uncommitted rules object; re-authored and the controls re-run against copies).

## Round 10 — implementation screen, after round 9's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; 23 files staged, nothing committed.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 9's fixes: `DashboardJsonRules.emptyExpressions` no longer skips row panels (a row that
  declares no target still passes through the null-targets path, and a row declaring a blank target is now a violation);
  three cases were added, including the row-with-blank-target, the non-numeric `gridPos`, and the untitled-panel naming
  — 17 tests, and removing each rule still fails only its own cases (1, 5, 5); the design's and task 1.1's quoted legend
  now matches the JSON ("since last resolved batch (idle time included)"); task 6.2 records the owner's proposal
  approval, and the unit record is rewritten as the implementation-stage snapshot.
- **Prior findings with their instances** — rounds 1–9, in `findings/round-1.md` to `findings/round-9.md`.
- **Completed checks** — `verifyDashboardJson` and the rules tests pass (17); the projection-service test passes; the
  release is at revision 2 with the three panels provisioned and the data cases verified; `openspec validate --strict`
  passes; `spotlessApply` has run.
- **Known limitations** — Retrying and Stopped not induced live; the gate's rules are structural; the stat panel's
  rendered appearance is asserted through its provisioned object and the live data.

## Round 11 — implementation screen, after round 10's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged, nothing committed.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 10's fixes: the Next step now names the **second** advisor consultation — the one the
  owner asked for over the implementation, distinct from the proposal-stage consultation that ran and whose amendments
  are in the design; the change-set field states the set instead of a count that every findings file moves.
- **Prior findings with their instances** — rounds 1–10, in `findings/round-1.md` to `findings/round-10.md`.
- **Completed checks** — `verifyDashboardJson`, the rules tests (17), the projection-service test, `check`, and
  `openspec validate --strict` pass; the release is at revision 2 with the three panels provisioned and the data cases
  verified.
- **Known limitations** — Retrying and Stopped not induced live; the gate's rules are structural; the stat panel's
  rendered appearance is asserted through its provisioned object and the live data.

## Round 12 — implementation screen, after the deep review's two nitpicks

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — the deep review's two nitpicks applied: `VerifyDashboardJsonTask`'s class KDoc now states the
  gate's panel-level obligations beside the JSON one, pointing at `DashboardJsonRules` as the rule set; tasks 6.1 and
  6.5 record their evidence (the proposal loop's rounds 1–8 with a clean round 8, and the deep pass's two nitpicks).
- **Prior findings with their instances** — rounds 1–11 in `findings/round-1.md` to `findings/round-11.md`; the deep
  pass's two nitpicks are recorded on task 6.5.
- **Completed checks** — `verifyDashboardJson` and the 17 rules tests pass after the KDoc edit; `check` and
  `openspec validate --strict` passed before it and the edit touches a comment only.
- **Known limitations** — Retrying and Stopped not induced live; the gate's rules are structural; the stat panel's
  rendered appearance is asserted through its provisioned object and the live data.

## Round 13 — implementation screen, after round 12's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 12's fixes: the task's class KDoc is a pointer again ("`DashboardJsonRules` owns what a
  dashboard must satisfy") instead of a partial restatement; the unit record's Next step is the second advisor
  consultation and the manual pass only; task 6.5's note no longer claims 6.3 was applied and says 6.3 closes with the
  implementation loop's last clean round.
- **Prior findings with their instances** — rounds 1–12 in `findings/round-1.md` to `findings/round-12.md`; the deep
  pass's two nitpicks are on task 6.5.
- **Completed checks** — `verifyDashboardJson`, the build-logic tests, `check`, and `openspec validate --strict` pass.
- **Known limitations** — Retrying and Stopped not induced live; the gate's rules are structural; the stat panel's
  rendered appearance is asserted through its provisioned object and the live data.

## Round 14 — implementation screen, after the advisor's correction

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — the owner-authorized second consultation (over the implementation) confirmed one problem and
  no blocking one: the In-flight Batches description claimed "a value that stays above zero means writes are not
  resolving", while partitions write concurrently and a nonzero count is compatible with healthy processing. Its
  description now reads "A sustained nonzero count can occur during healthy continuous processing; this gauge alone does
  not establish stalled writes.", the design's risks record that occupancy is not progress, and task 5.2's evidence
  names the release's third revision, which provisions the corrected text.
- **Prior findings with their instances** — rounds 1–13 in `findings/round-1.md` to `findings/round-13.md`; the deep
  pass's two nitpicks on task 6.5; the advisor's one problem applied.
- **Completed checks** — `verifyDashboardJson`, the build-logic tests, and `check` pass; the release is at revision 3
  and Grafana returns 49 panels with the two corrected descriptions.
- **Known limitations** — Retrying and Stopped not induced live; the gate's rules are structural; the stat panel's
  rendered appearance is asserted through its provisioned object and the live data.

## Round 15 — implementation screen, after round 14's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `96edb035`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-testing`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 14's fix: the Next step is the owner's manual pass alone, and the Pending decision
  records the second consultation as run, with its one problem applied and provisioned at release revision 3.
- **Prior findings with their instances** — rounds 1–14 in `findings/round-1.md` to `findings/round-14.md`; the deep
  pass's two nitpicks on task 6.5; the advisor's one problem applied.
- **Completed checks** — `verifyDashboardJson`, the build-logic tests, and `check` pass; the release is at revision 3
  and Grafana returns 49 panels with the corrected descriptions.
- **Known limitations** — Retrying and Stopped not induced live; the gate's rules are structural; the stat panel's
  rendered appearance is asserted through its provisioned object and the live data.
