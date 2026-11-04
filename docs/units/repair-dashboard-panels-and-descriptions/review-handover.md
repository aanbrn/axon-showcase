# Review hand-over — repair-dashboard-panels-and-descriptions

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `repair-dashboard-panels-and-descriptions`; tree
  `/Users/afanasyev/Development/axon-showcase` (the primary checkout, this unit's tree); branch of the same name; base
  `origin/main` `ef6fc2f4`; nothing committed, every unit file staged.
- **Packs** — `deployment-operations` (the bundled dashboard and its provisioning), `gradle-build` (the `check` gate and
  its rules object), plus `check-and-test-authoring` for the new rule's controls.
- **Supplied artifacts** — `proposal.md`, `design.md` (Context, Goals/Non-Goals, ten decisions with their rejected
  alternatives, risks, verification, the claims table and its dismissals), `tasks.md`, and the
  `showcase/quality/code-quality` delta; the unit record.
- **The proposed change** — repair the dashboard's stale panels (query-service HTTP targets for a service that reads
  over gRPC, `saga-service` selectors for a removed service, thresholds on the wrong scale, an "Availability" panel that
  divides lifetime counters, a failure panel whose per-label vector join can drop an error), switch backend rate windows
  to `$__rate_interval` after verifying its expansion, describe all 42 data panels and correct their units, and add a
  description obligation to `verifyDashboardJson` with controls.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate repair-dashboard-panels-and-descriptions --strict` passes; `spotlessApply`
  has run; the staged tree passes `commit-hygiene --review-ready`; the claims table's sources were re-run against the
  base revision while the proposal was drafted.
- **Known limitations** — `$__rate_interval`'s expansion on the deployed Grafana is a task, with a fixed `[2m]`
  fallback; the by-id query timer appears only after its first use, which the descriptions must say.

## Round 2 — proposal screen, after round 1's fourteen findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 1's fixes: the batch-size panel takes `short` (a count) in decision 8 and task 5.2; the
  window figures are restated as 87 `[1m]` occurrences across 72 of 97 targets (76 with any window) and 4 `[5m]`
  windows, in the Context and the claims table; task 4.1 converts one representative panel before inspecting the
  expansion and task 4.2 converts the rest; task 3.2's verification names the runnable control (the absent by-id family)
  and records that the one-family error case is not induced, with the disjoint-label-sets evidence; task 6.2 adds a
  nested target-bearing panel without a description, and task 6.1 covers the rules object's KDoc and the Gradle task
  description; decision 5 and task 3.1 carry the cumulative-and-reset description content; decision 1 names all three
  live gRPC families and decision 8 names the families and the panels the lazy metrics back (60, 61, 64, and 83); the
  proposal names the change's own delta path and splits the Availability and rate-window bullets; the claims-table
  result cells escape their pipes; the unit record records the owner's authorization of the advisor consultation and the
  table gains the row pointing at it; the inline code span's spacing is fixed.
- **Prior findings with their instances** — round 1's fourteen, in `findings/round-1.md`.
- **Completed checks** — `openspec validate repair-dashboard-panels-and-descriptions --strict` passes; `spotlessApply`
  has run; `commit-hygiene --review-ready` passes.
- **Known limitations** — the `$__rate_interval` expansion and the one-family error case remain implementation-time
  checks, as tasks 4.1 and 3.2 state.

## Round 3 — proposal screen, after round 2's five findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 2's fixes: task 3.2's verification now names a runnable, discriminating control — the
  union against the unfiltered families while the by-id series is absent (the union returns the list value, the `+` form
  returns nothing), then one by-id read through the gateway so both families are present and the union equals their sum
  — and states that `rate` drops `__name__`, so `reactor_type` is the distinguishing label; the proposal's Query Service
  and overview bullets are split one action each; task 8.2 records the proposal-stage consultation; the claims table
  gains the row mapping the lazy families to panels 60, 61, 64, and 83.
- **Prior findings with their instances** — rounds 1 and 2, in `findings/round-1.md` and `findings/round-2.md`.
- **Completed checks** — `openspec validate --strict` passes; `spotlessApply` has run; `commit-hygiene --review-ready`
  passes.
- **Known limitations** — the error-only-in-one-family case is not induced live (the union's behaviour there rests on
  `or`'s semantics); the `$__rate_interval` expansion and the provisioned read-back are implementation-time tasks.

## Round 4 — proposal screen, after round 3's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 3's fixes: `build.gradle.kts` joins the What Changes list (the `verifyDashboardJson`
  task description), and the Impact's `Files` line points at that list instead of repeating it, so one edit updates the
  touched set; the unit record's Stage and Next step describe the running loop and the manual pass and leave the round
  history to `rounds.tsv`; task 3.2 asks for a by-id read that is not cached, since a cached read records no timer.
- **Prior findings with their instances** — rounds 1–3, in `findings/round-1.md` to `findings/round-3.md`.
- **Completed checks** — `openspec validate --strict` passes; `spotlessApply` has run; `commit-hygiene --review-ready`
  passes.
- **Known limitations** — the error-only-in-one-family case is not induced live; `$__rate_interval` and the provisioned
  read-back are implementation-time tasks.

## Round 5 — proposal screen, after the advisor's second pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, plus `check-and-test-authoring`.
- **Supplied artifacts** — the owner-authorized second consultation over the written proposal found one coherence defect
  and one content constraint, both applied: the delta's description obligation now carries the row exemption the design
  states (with a scenario accepting a target-bearing row without a description), and task 5.1 forbids describing the
  `fetch_showcase_*` error families as covering every query failure, because the by-ID `NOT_FOUND` is raised after the
  observation tap. The claims table gains the row for that behaviour, task 6.2 gains the accepted row fixture, and task
  8.2 records both consultation passes.
- **Prior findings with their instances** — rounds 1–4 in `findings/`, and the two consultation passes on task 8.2.
- **Completed checks** — `openspec validate --strict` passes; `spotlessApply` has run; `commit-hygiene --review-ready`
  passes.
- **Known limitations** — the error-only-in-one-family case is not induced live; `$__rate_interval` and the provisioned
  read-back are implementation-time tasks.

## Round 6 — proposal screen, after round 5's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Supplied artifacts** — round 5's fix: design decision 5 now names the four ratios the Processing Ratio panel
  divides, requires its description to name each of them, and states the cumulative-and-reset and not-an-SLO content in
  one place; task 5.1 points at that decision instead of naming an unreferenced set. Process-defect record `#1` records
  the silent no-op replacement that let the decision and the task disagree for two rounds.
- **Prior findings with their instances** — rounds 1–5 in `findings/`, plus the two consultation passes on task 8.2.
- **Completed checks** — `openspec validate --strict` passes; `spotlessApply` has run; `commit-hygiene --review-ready`
  passes.
- **Known limitations** — the error-only-in-one-family case is not induced live; `$__rate_interval` and the provisioned
  read-back are implementation-time tasks.

## Round 7 — implementation screen, first pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; 18 files staged, nothing committed.
- **Packs** — `deployment-operations` (the dashboard), `gradle-build` (the gate and its rules object), plus
  `check-and-test-authoring`.
- **Supplied artifacts** — the implementation: panels 4/60/61 lost their query-service HTTP targets, 61's fetch averages
  gained the selectors, 29/44 lost the saga targets and took `0.8` with `max: 1`, panel 4 became `Processing Ratio` with
  fraction thresholds and a rebuilt query-service ratio, panel 64 aggregates the two families with `or`, every `[1m]`
  became `$__rate_interval` (84 occurrences, the four `[5m]` untouched), all 42 data panels carry descriptions and
  quantity units, and `DashboardJsonRules` gained the description rule with six new cases and an updated KDoc and task
  description.
- **Prior findings with their instances** — the proposal stage's rounds 1–6 in `findings/`, and the two consultation
  passes on task 8.2.
- **Completed checks** — `verifyDashboardJson` green; 23 rules tests; removing the rule fails only the five description
  cases; removing one panel's description fails the gate naming it (restored byte-identical); the rebuilt queries were
  run against Prometheus, including the absent-family control and the both-present equality after one unknown-id read;
  `check` green; `openspec validate --all` 27; the release is at revision 5 and Grafana returns the corrected dashboard.
- **Known limitations** — the error-only-in-one-family case is not induced (the created by-id series carries
  `error="none"`, confirming the not-found is raised after the tap); the rendered appearance is asserted through the
  provisioned objects and the live data, not a screenshot.

## Round 8 — implementation screen, after round 7's seven findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 7's fixes: every description now states what an empty result means (32 gained a
  tailored sentence; the ratio, the lazy families, and the state panel already said so and were left as written); Event
  Age says the age is read at ingest; Memory Usage describes the service aggregate; Processing Ratio says commands and
  events; the bus failure panel no longer claims handler rejections are excluded; design decision 5 and task 3.1 state
  the two-family union the code implements; decision 10 and task 5.3 no longer promise a single sentence.
- **Prior findings with their instances** — the proposal rounds 1–6 and the implementation round 7, in `findings/`.
- **Completed checks** — `verifyDashboardJson` green with all 42 descriptions; the rules tests pass;
  `openspec validate --strict` passes; the queries and the provisioned read-back were verified in round 7 and are
  unchanged except the description text.
- **Known limitations** — the error-only-in-one-family case is not induced; the rendered appearance is asserted through
  the provisioned objects and the live data.

## Round 9 — implementation screen, after round 8's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 8's fixes: `Accepted / Handled` gained its empty-state sentence, so all 42 carry one;
  Event Age's sentence now says no event reached the projector; CPU Usage says the line disappears when the service's
  pods stop being scraped; the proposal's ratio bullet states the two-family union, matching the design, the task, and
  the code. A completeness scan over all 42 descriptions finds none without an empty-state statement.
- **Prior findings with their instances** — rounds 1–8, in `findings/`.
- **Completed checks** — `verifyDashboardJson` green; the rules tests pass; `openspec validate --strict` passes.
- **Known limitations** — the error-only-in-one-family case is not induced; the rendered appearance is asserted through
  the provisioned objects and the live data.

## Round 10 — implementation screen, after the deep review's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, plus `check-and-test-authoring`.
- **Supplied artifacts** — the deep review's findings applied: panel 80's description now names TTFB beside LCP, FCP,
  and INP, with each metric's band; CPU Usage describes the machine's CPU as each JVM reports it (the JVMs share one
  node); Memory Usage describes total memory usage including page cache, over the declared limits; Processing Ratio says
  "answered without a 5xx"; tasks 8.1 and 8.6 record their evidence, and 8.4 follows this round.
- **Prior findings with their instances** — rounds 1–9 in `findings/`; the deep pass's four on task 8.6.
- **Completed checks** — `verifyDashboardJson` green; the rules tests pass; `openspec validate --strict` passes.
- **Known limitations** — the error-only-in-one-family case is not induced; the rendered appearance is asserted through
  the provisioned objects and the live data, not a screenshot.

## Round 11 — implementation screen, after the advisor's correction

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, plus `check-and-test-authoring`.
- **Supplied artifacts** — the advisor's confirmed problem applied: 28 empty-state sentences now read "no such activity
  was recorded in the window, or that this series is not being scraped", panel 83 says an empty panel "cannot on its own
  prove that reporting works", and panels 4, 44, 60, and 61 carry the same distinction. Its hypothesis about a
  shorter-range floor is refuted with evidence: the deployed Grafana resolved `$__rate_interval` to `[2m0s]` at both
  `now-5m` and `now-1h`, recorded on task 4.1.
- **Prior findings with their instances** — rounds 1–10 in `findings/`; the deep pass's four and the advisor's one on
  tasks 8.6 and 8.7.
- **Completed checks** — `verifyDashboardJson` green; the rules tests pass; `openspec validate --strict` passes.
- **Known limitations** — the error-only-in-one-family case is not induced; the rendered appearance is asserted through
  the provisioned objects and the live data, not a screenshot.

## Round 12 — implementation screen, after round 11's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, plus `check-and-test-authoring`.
- **Supplied artifacts** — round 11's fixes: the ten descriptions that still asserted a single cause now say "or that
  this series is not being scraped"; the nineteen double-verb sentences are rewritten; design decision 8 and task 5.1
  state the two-cause rule so the set is checkable; task 8.7 records the consultation, its finding, and the refuted
  shorter-range hypothesis; the thirteen fixture lines are split, and the 120-character check now runs the documented
  `perl` recipe. Process-defect record `#2` records the vacuous `awk` form the earlier rounds used.
- **Prior findings with their instances** — rounds 1–11 in `findings/`; the deep pass's four on task 8.6 and the
  advisor's one on task 8.7.
- **Completed checks** — `verifyDashboardJson` green; 23 rules tests; `openspec validate --strict` passes; the changed
  Kotlin files carry no line over 120.
- **Known limitations** — the error-only-in-one-family case is not induced; the rendered appearance is asserted through
  the provisioned objects and the live data.

## Round 13 — implementation screen, after the advisor's confirmation pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `ef6fc2f4`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, plus `check-and-test-authoring`.
- **Supplied artifacts** — the confirmation pass's one finding applied: panel 60 separates a measured zero ("no such
  read was recorded in the sampled interval") from missing points ("the series is unavailable"), keeping the first-use
  explanation for the by-id timer; task 8.7 records both consultation passes.
- **Prior findings with their instances** — rounds 1–12 in `findings/`; the deep pass's four on task 8.6 and the
  advisor's passes on task 8.7.
- **Completed checks** — `verifyDashboardJson` green; the rules tests pass; `openspec validate --strict` passes.
- **Known limitations** — the error-only-in-one-family case is not induced; the rendered appearance is asserted through
  the provisioned objects and the live data.
