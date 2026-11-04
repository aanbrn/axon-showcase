# Review hand-over — guard-recreate-strategy-across-deployments

One section per quick-review invocation, recorded before the caller waits on it.

## Round 1 — proposal screen

- **Unit** — `guard-recreate-strategy-across-deployments`, an OpenSpec change. It moves the `rollingUpdate: null` rule
  into one named template used by all five service Deployments, adds a render-based check, and specs the outcome. The
  parked entry (2026-10-24) is withdrawn by it.
- **Revisions** — base `origin/main` `0d6fee83`; the work is uncommitted on the branch.
- **Workspace** — `/Users/afanasyev/Development/axon-showcase`, branch `guard-recreate-strategy-across-deployments`.
- **Packs** — `deployment-operations` (the chart, its templates, and the Helm path this change edits).
- **Supplied artifacts** — the change dir (`proposal.md`, `design.md` with the helper, `tasks.md` with the claims table,
  and the delta on `helm-chart`'s "Service deployments").
- **Scope** — whether one helper genuinely covers the five call sites, whether the render check can fail (both
  directions) and is wired where Helm already runs, whether the delta preserves every scenario, and whether the parked
  entry's removal is complete.
- **Prior findings** — none; the unit's first invocation.
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`.
- **Known limitations** — nothing is implemented; the check's Helm dependency is the design's claim, verified at
  implementation.

## Round 2 — proposal screen, with `deployment-operations` and `gradle-build`

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — **`deployment-operations`** (the chart, its templates, Helm) **and `gradle-build`** (the change registers
  a `verification` task, rests a premise on what `check` already depends on, and edits `check`'s composition). Round 1
  stopped on the missing second pack; the unit's process-defect record gains `#1` for the classification miss.
- **Supplied artifacts** — as round 1: `proposal.md`, `design.md` (the helper and five decisions), `tasks.md` (with the
  claims table), and the delta on `helm-chart`'s "Service deployments".
- **Prior findings** — none; round 1 produced no screen.
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`; routing `NOT_NEEDED`.
- **Known limitations** — nothing is implemented; the `check`-Helm premise and the render check are design claims.

## Round 3 — proposal screen, after round 2's six findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the proposal revised:
  - the helper renders `strategy:` with the mapping at two spaces and `rollingUpdate: null` beside it, verified in a
    scratch chart; each call site uses `| nindent 2`;
  - the renders use the plugin's own `HelmTemplate` tasks, so no host Helm is needed (the plugin downloads its client,
    CI installs none, the doctor keeps Helm optional);
  - the negative render sets all five to `RollingUpdate`, because the projection service's default is already
    `Recreate`;
  - the rules live in `build-logic` (`ChartStrategyRules` + tests) with a `VerifyChartStrategiesTask` wired to the two
    render directories as declared inputs and a `dependsOn` on both renders;
  - the tasks name both the checker and its test, and add a refresh of the `deployment-operations` chart-validation
    paragraph.
- **Prior findings with their instances** — all six in `findings/round-2.md`.
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`; the helper's render verified in a scratch
  chart; the claims rows re-derived at the base.
- **Known limitations** — nothing is implemented; the HelmTemplate task wiring and the rules' tests are design claims.

## Round 4 — proposal screen, after round 3's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the proposal with round 3's fixes: What Changes names the second render as all five set to
  `RollingUpdate`; the design requires each render task to declare the collected chart tree as an input (the plugin's
  `chart` is a path string, so a dependency alone leaves a re-collected chart invisible); a claims row records the
  `HelmTemplate` capability and its source; task 1.1 moves the projection template's rationale into the helper's doc
  comment.
- **Prior findings with their instances** — all four in `findings/round-3.md`.
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`; no "on the defaults" instance remains.
- **Known limitations** — nothing is implemented; the `HelmTemplate` wiring and the rules' tests are design claims.

## Round 5 — proposal screen, after round 4's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the proposal with round 4's fixes: the helper block now shows its doc comment and the moved
  rationale; task 1.1 requires that comment and task 1.2 says the projection template's comment moves with the guard;
  the `HelmTemplate` claims row cites the plugin's sources jar for the property semantics. The unit's defect record
  gains `#2` for the script that reported success while one replacement no-opped.
- **Prior findings with their instances** — both in `findings/round-4.md`.
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`; the task text read back after the edit.
- **Known limitations** — nothing is implemented; the `HelmTemplate` wiring and the rules' tests are design claims.

## Round 6 — proposal screen, after round 5's fix

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the same proposal with the claims row re-derived from one extraction over the plugin's
  sources (`*.kt`), whose output shows `chart`, `outputDir` and the `--set-string` partition together.
- **Prior findings with their instances** — round 5's `claim-unreproducible` (`findings/round-5.md`).
- **Completed checks** — the extraction was run and its line numbers copied into the row; `openspec validate --all` 27
  items; `spotlessApply`.
- **Known limitations** — nothing is implemented; the `HelmTemplate` wiring and the rules' tests are design claims.

## Round 7 — proposal screen, after round 6's fix

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the same proposal with the `HelmTemplate` claims row re-derived from an extraction plus
  `grep -rn`, whose real output carries filenames and per-file line numbers; the unit's defect record gains `#3` for the
  row that could not produce its own result.
- **Prior findings with their instances** — round 6's `claim-unreproducible` (`findings/round-6.md`).
- **Completed checks** — the row's command run and its output copied verbatim; `openspec validate --all` 27 items;
  `spotlessApply`.
- **Known limitations** — nothing is implemented; the `HelmTemplate` wiring and the rules' tests are design claims.

## Round 9 — proposal screen, after the advisor's over-claim correction

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the proposal with the guarantee qualified at three sites (the design's Consequences, the
  delta's scenario, the proposal's Modified Capabilities): the guard covers a Helm-owned transition; a `rollingUpdate`
  block owned by another manager still fails and stays the separate parked entry's subject.
- **Prior findings with their instances** — round 8's `over-claim` (`findings/round-8.md`, raised by the advisor).
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`.
- **Known limitations** — the two extra renders' `check` cost is unmeasured; the advisor treats that as no reason to
  drop either assertion, and it is not a delivery blocker for a proposal.

## Round 10 — closing proposal screen

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the proposal with round 9's fix: decision 6 and the helper's doc comment now name the
  Helm-owned scope, and the delta drops the pointer into `docs/ideas.md` (a prunable scratchpad) while keeping the
  normative "a block owned by another manager is outside this remedy".
- **Prior findings with their instances** — round 9's `over-claim` (`findings/round-9.md`).
- **Completed checks** — `openspec validate --all` 27 items; `spotlessApply`.
- **Known limitations** — the two renders' `check` cost is unmeasured; routing for the implementation stage is
  `RECOMMENDED` (repeated classes), so the owner's run-or-skip decision is owed then.

## Round 11 — implementation screen, after the deep review and the advisor's two corrections

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the implementation with the deep review's five items and the advisor's two corrections: the
  three task ticks (plus the deep review's mechanical edits); the assertion parses `spec.strategy` with comments
  excluded (two new false-positive fixtures: a commented guard and a nested wrong shape), 8 tests; the renderer prefers
  the plugin's client at the catalog's Helm version (`--client-version`, declared as a task input), then the
  environment, then `PATH`, then the newest cached client; the cache sort is numeric and the environment path is checked
  executable.
- **Prior findings with their instances** — the deep review's five (`F1`–`F5`) and the advisor's two, recorded in
  `findings/round-11.md` and the review hand-over.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green (407 tasks); 8 tests; the
  renders re-run from scratch and the assertion passes; `openspec validate --all` 27 items; the 120-character scan over
  the three scripts is clean.
- **Known limitations** — task 6.3 (the gate) closes at the manual pass; the `check` cost was measured at ≈3.4 s.

## Round 12 — implementation screen, after round 11's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the implementation with round 11's four fixes: the design, task 2.1 and the pack state the
  implemented client order (catalog-pinned plugin client first); task 3.2 says eight tests and names the commented-guard
  and nested-shape fixtures, as decision 4 does; the dead `GUARD` constant is gone; decision 8 sits inside the Decisions
  list.
- **Prior findings with their instances** — all four in `findings/round-12.md`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green; 8 tests;
  `openspec validate --all` 27 items; the 120-character scan clean.
- **Known limitations** — task 6.3 (the gate) closes at the owner's manual pass, which is this request.

## Round 13 — closing implementation screen, after round 12's two findings (clean)

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0d6fee83`.
- **Packs** — `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the same implementation with round 13's fixes applied at each remaining site by an
  occurrence-asserted edit, read back afterwards: the design's decision 2, task 2.1 and the `deployment-operations` pack
  now state the catalog-pinned client first; design decision 4 lists all eight test cases.
- **Prior findings with their instances** — both in `findings/round-13.md`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green; 8 tests;
  `openspec validate --all` 27 items; each site grepped back.
- **Known limitations** — task 6.3 (the gate) closes at the owner's manual pass.
