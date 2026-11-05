# Review hand-over — surface-projector-backlog

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `surface-projector-backlog`; tree `/Users/afanasyev/Development/axon-showcase` (the
  primary checkout, this unit's tree); branch of the same name; base `origin/main` `d8cf14ba`; nothing committed, every
  unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, and `deployment-operations`, all loaded in full: the change writes JVM
  production code, adds JVM tests at the unit and integration tiers, and reads the chart's ServiceMonitor wiring.
- **Supplied artifacts** — `proposal.md`, `design.md` (Context, Goals/Non-Goals, seven decisions with alternatives,
  risks, verification, the claims table and its dismissals), `tasks.md`, `.openspec.yaml`, the delta
  `specs/showcase/read-side/projection-service/spec.md`; the unit record.
- **The proposed change** — a lifecycle-managed `AdminClient` sampler in the projection service publishes the consumer
  group's committed-offset distance per partition (`showcaseProjector.consumerLag`), the last successful sample's epoch
  second, an unresolved-partition count, and a failure counter; the health indicator gains the largest distance and the
  observation time as details and keeps its status calculation; the dashboard gains a backlog panel and a sample-age
  panel; ADR-0023 gains a dated amendment retaining the `UP`-while-stalled boundary.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate surface-projector-backlog --strict` passes with the delta;
  `openspec validate --all` passes; `spotlessApply` has run; `commit-hygiene --review-ready` passes; the claims table's
  sources were re-run while the proposal was drafted.
- **Known limitations** — the sampler's behaviour is verified by unit and integration tests at implementation, not by a
  gate; the change deliberately ships no stall threshold, so the panel shows a number without a verdict.

## Round 2 — proposal screen, after round 1's eleven findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`.
- **Supplied artifacts** — round 1's fixes: the Context distinguishes the new backlog from the two time-based panels
  (with their real descriptions) and the Why is narrowed; the delta gains the failed-sample count, the
  vanished-partition cause, and a scenario for "no resolved partition resolves nothing"; decision 4 states the union of
  both partition sets and decision 6 the omitted summary; the "no chart change" Non-Goal is narrowed to templates,
  ServiceMonitor, and values; the sample-age expression is guarded like its sibling; tasks gain a package-private
  arithmetic seam, publication unit cases, a Prometheus exposition-name test, the property binding/rejection cases, the
  `docs/ideas.md` removal, and a README site sweep derived from the claim rather than a hand-written list.
- **Prior findings with their instances** — round 1's eleven, in `findings/round-1.md`.
- **Completed checks** — `openspec validate surface-projector-backlog --strict` passes with the delta;
  `openspec validate --all` passes; `spotlessApply` has run; `commit-hygiene --review-ready` passes; `verifyTableRows`
  passes.
- **Known limitations** — the sampler's behaviour is verified by unit, component, and integration tests at
  implementation.

## Round 3 — proposal screen, after round 2's nine findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`.
- **Supplied artifacts** — round 2's fixes: the delta's "no partition resolved" case is split from the never-committed
  group (whose sample succeeds), a failed sample has its own scenario, the normative clause names the largest-distance
  detail, and the requirement carries eleven scenarios; decision 4 says an absent partition is visited and publishes
  `NaN` rather than "keeps its series"; a claims row pins `AdminClientConfig.configNames()`; the Why ends on "how many
  records are outstanding"; tasks name the gauge-writing seam that task 2.2 drives, extend both enumerating property
  cases, and make task 3.3 supersede ADR-0023's pointer to the removed idea.
- **Prior findings with their instances** — rounds 1–2 in `findings/`.
- **Completed checks** — `openspec validate surface-projector-backlog --strict` passes; `spotlessApply` has run;
  `commit-hygiene --review-ready` passes; `verifyTableRows` passes.
- **Known limitations** — the sampler's behaviour is verified by unit, component, and integration tests at
  implementation.

## Round 4 — proposal screen, after round 3's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`.
- **Supplied artifacts** — round 3's fixes: the pre-first-sample scenario states that no distance is published and that
  the failure count reports how many attempts failed, and the design's Risk carries the same joint reading; the
  `configNames()` row cites the single-file probe that printed the 74 names (and the assertion task 1.2 adds) instead of
  `javap`; the done mark counts eleven rows; task 3.3's verify asks that no **live** pointer survives, keeping the
  quoted sentence as the amendment's record; the Why no longer repeats "how many records are outstanding".
- **Prior findings with their instances** — rounds 1–3 in `findings/`.
- **Completed checks** — `openspec validate surface-projector-backlog --strict` passes; `spotlessApply` has run;
  `commit-hygiene --review-ready` passes; `verifyTableRows` passes.
- **Known limitations** — the sampler's behaviour is verified by unit, component, and integration tests at
  implementation.

## Round 5 — implementation screen, first pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged, nothing committed.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build` (added after the review's
  classification stop: the change edits `showcase-projection-service/build.gradle.kts`).
- **Supplied artifacts** — the implementation: `ShowcaseProjectorBacklogSampler` (lifecycle-managed `AdminClient` over
  the consumer properties filtered by `AdminClientConfig.configNames()`, one virtual thread, `resolve` and `publish` as
  package-private seams, per-partition `showcaseProjector.consumerLag` with `NaN` for an unresolved partition, the
  `unresolved`/`failed`/`observed` companions); `ShowcaseProjectorProperties.Backlog` and its yml placeholders;
  `ShowcaseProjectorHealthIndicator`'s three details; the unit, component, and integration suites; dashboard panels 90
  and 91; the README's 46/8 and its two bullets; the ADR-0023 amendment; the deployment pack's reading bullet; the
  `docs/ideas.md` removal; and the change's delta.
- **Evidence** — `:showcase-projection-service:check` green with integration tests and the coverage gate; the repository
  `check` green Docker-free; `openspec validate --all` 27; the deployed readings recorded on task 2.5, including that a
  positive backlog was not observable on the local stack and stands on the integration test instead.
- **Prior findings with their instances** — proposal rounds 1–4 in `findings/`, the last clean.
- **Completed checks** — `spotlessApply` after the last edit; `spotlessCheck`; `verifyDashboardJson`; `verifyTableRows`;
  `commit-hygiene --review-ready`.
- **Known limitations** — the sampler's mixed-age replica view is unexercised on this single-replica stack; the positive
  deployed backlog was not observed (see task 2.5).

## Round 6 — implementation screen, after round 5's ten findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — round 5's fixes: `adminConfig()` as a seam with a unit control and a lifecycle control
  (`isRunning()` around start and stop); the failed-sample counter awaited in the integration test; the removed "unknown
  end offset" cause swept from the proposal and task 2.1 as well; the claims row re-pinned to `d8cf14ba` and to the
  change's 46 panels; the delta split into a `MODIFIED` lifecycle requirement and an `ADDED` backlog requirement; the
  suite-edge task 2.7 added and ticked; task 4.8 stated as a mechanism.
- **Prior findings with their instances** — rounds 1–5 in `findings/`.
- **Completed checks** — `:showcase-projection-service:check` green with integration tests and the coverage gate;
  `openspec validate --all` 27; `verifyDashboardJson`; `spotlessApply` after the last edit.
- **Known limitations** — the deployed positive-backlog reading was not observed (task 2.5 records it); the
  multi-replica mixed-age view is unexercised on this single-replica stack.

## Round 7 — implementation screen, after round 6's eight findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — round 6's fixes, each re-read against the tree: the integration test awaits the failed-sample
  counter; the removed cause is gone from the proposal and task 2.1 as well; task 4.8 states the mechanism; the claims
  row is re-pinned to `d8cf14ba` and 46 panels; the delta is split into a `MODIFIED` lifecycle requirement (six
  scenarios) and an `ADDED` backlog requirement (five); the failed-sample count is back in the added requirement's
  summary; and the start/stop contract has a control that also proves a stopped sampler attempts no further sample.
- **Prior findings with their instances** — rounds 1–6 in `findings/`.
- **Completed checks** — `:showcase-projection-service:test` and `:componentTest` green; `openspec validate --all` 27;
  `verifyDashboardJson`; `spotlessApply` after the last edit.
- **Known limitations** — the module `check` with integration tests is run at the closing round; the deployed positive
  backlog reading stands on the integration test (task 2.5).

## Round 8 — implementation screen, after round 7's six findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — round 7's fixes: the start/stop case proves a stopped sampler attempts no further sample; the
  proposal states the add-and-point shape and is back on the template's schema (`## Capabilities` with
  `### New Capabilities` (None) and `### Modified Capabilities`); this record; tasks 2.2 and 2.3 name where the
  failed-sample counter is verified; the pack, the ADR and the dashboard name the added requirement.
- **Prior findings with their instances** — rounds 1–7 in `findings/`.
- **Completed checks** — `:showcase-projection-service:test` and `:componentTest` green; `openspec validate --all` 27;
  `verifyDashboardJson`; `spotlessApply` after the last edit.
- **Known limitations** — the module `check` with integration tests is owed at the closing round; the deployed
  positive-backlog reading stands on the integration test (task 2.5).

## Round 9 — implementation screen, after round 8's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — round 8's fixes: the sampler takes an `AdminClientFactory` seam, with `@Autowired` on the
  context-facing constructor so Spring's two-constructor selection is unambiguous, and `stop_closesTheAdminClient`
  verifies `close(Duration)`; the case fails when the close is mutated away and passes when it is restored. The module
  `check` with integration tests and the coverage gate ran green in 1m 2s, which round 8's closing note owed.
- **Prior findings with their instances** — rounds 1–8 in `findings/`.
- **Completed checks** — `:showcase-projection-service:check` green (Docker, coverage gate on);
  `openspec validate --all` 27; `verifyDashboardJson`; `spotlessApply` after the last edit.
- **Known limitations** — the deployed positive-backlog reading stands on the integration test (task 2.5); the
  multi-replica mixed-age view is unexercised on this single-replica stack.

## Round 10 — implementation screen, after round 9's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — round 9's fixes: the hand-over's sections renumbered, so "Round 8" records only what round 8
  was given and "Round 9" carries the `AdminClientFactory` seam, the close-on-stop case and the module `check`; task
  1.2's Done note records the seam, the `close(Duration)` control, and the start/stop-plus-frozen-counter control.
- **Prior findings with their instances** — rounds 1–9 in `findings/`.
- **Completed checks** — `:showcase-projection-service:check` green (Docker, coverage gate on);
  `openspec validate --all` 27; `verifyDashboardJson`; `spotlessApply` after the last edit.
- **Known limitations** — the deployed positive-backlog reading stands on the integration test (task 2.5); the
  multi-replica mixed-age view is unexercised.

## Round 11 — implementation screen, after round 10's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — round 10's fixes: this section (written before the invocation, per the reviewer's own
  self-check) and process-defect `#3`, which records the hand-over recurrence with its five instances rather than
  patching it again.
- **Prior findings with their instances** — rounds 1–10 in `findings/`.
- **Completed checks** — `:showcase-projection-service:check` green (Docker, coverage gate on);
  `openspec validate --all` 27; `verifyDashboardJson`; `spotlessApply` after the last edit;
  `commit-hygiene --review-ready`.
- **Known limitations** — the deployed positive-backlog reading stands on the integration test (task 2.5); the
  multi-replica mixed-age view is unexercised.

## Round 13 — implementation screen, after the deep review's one nitpick

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the deep review's finding: the ADR-0023 amendment's opening read as though stall detection
  shipped, while the change's Non-Goals exclude the detection and the Decision retains the `UP`-while-stalled boundary.
  The opening now names the observation as what lands and the detection as superseded out of scope. Nothing else
  changed; the deep pass re-checked the sampling loop, the `NaN` publication, the lifecycle, the admin-config filter,
  the delta, the tests, the panels and the durable records as sound.
- **Prior findings with their instances** — rounds 1–12 in `findings/`.
- **Completed checks** — `:showcase-projection-service:check` green (Docker, coverage gate on);
  `openspec validate --all` 27; `verifyDashboardJson`; `spotlessApply` after the last edit.
- **Known limitations** — the deployed positive-backlog reading stands on the integration test (task 2.5); the
  multi-replica mixed-age view is unexercised.

## Control evidence — the deployed positive backlog (no review round)

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the advisor's first implementation pass (`findings/advisor-1.md`): its two confirmed problems
  and its hypothesis are applied, and its open question is settled by the owner-approved control. The sample panel's
  wording now defines a healthy zero as resolved zero distances with zero unresolved partitions; its field override
  matches query B by frame reference rather than a templated legend; task 2.5 records the positive backlog the control
  produced through the exact panel expressions.
- **Prior findings with their instances** — rounds 1–13 and the advisor's first pass, in `findings/`.
- **Completed checks** — `verifyDashboardJson` after the panel edits; `openspec validate --all` 27; the local stack
  restored (OS_URIS and the readiness path back to the chart's values, the backlog drained to zero, the pod ready).
- **Known limitations** — the sample age reads 20–40 s at a scrape because the ServiceMonitor's scrape interval and the
  sampler's 15-second interval add up; the multi-replica mixed-age view remains unexercised.

## Round 14 — implementation screen, after the advisor's agreement and the control

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — everything since round 13: the sample panel's description now defines a healthy zero as
  resolved zero distances with zero unresolved partitions and names the never-committed case separately; its field
  override matches query B by frame reference; task 2.5 records the four deployed readings, the positive one produced by
  the owner-approved control (readiness probe pinned to liveness, `OS_URIS` pointed at a dead endpoint, five showcases
  created, `2`/`1`/`NaN` observed through the panel expressions across four cycles, `failed_total = 0`), and its
  restoration; task 4.6 records the advisor's two rounds, the second closing in explicit agreement.
- **Prior findings with their instances** — rounds 1–13 and the advisor's first pass, in `findings/`; the second pass
  closed in agreement and its outcome is recorded on task 4.6.
- **Completed checks** — `verifyDashboardJson`; `openspec validate --all` 27; `:showcase-projection-service:check` green
  before these edits (the panel and task text are outside its compile path, and `verifyDashboardJson` re-ran); the local
  stack restored and drained; `spotlessApply` after the last edit.
- **Known limitations** — the sample age reads 20–40 s at a scrape because the ServiceMonitor interval and the sampler's
  15 second cadence add up; the multi-replica mixed-age view is unexercised.

## Round 15 — implementation screen, after round 14's two record findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — round 14's fixes: task 2.5's four readings are separate list items under the expressions that
  produced them; `findings/advisor-2.md` records the advisor's clean second pass, and the Round 14 section names the
  first pass in `findings/` and the second on task 4.6.
- **Prior findings with their instances** — rounds 1–14 and the advisor's two passes, in `findings/`.
- **Completed checks** — `spotlessCheck`; `verifyDashboardJson`; `openspec validate --all` 27;
  `commit-hygiene --review-ready`; the local stack restored and drained.
- **Known limitations** — the sample age reads 20–40 s at a scrape because the ServiceMonitor interval and the sampler's
  15 second cadence add up; the multi-replica mixed-age view is unexercised.

## Round 16 — implementation screen, after the IDE-inspection pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — the owner asked for the IDE's warnings over the eight new and modified Java files, run
  through IntelliJ 2026.2's own inspection engine. It reported five: two `Convert2Diamond` and one
  `AutoCloseableResource` in the sampler, two `BusyWait` in the unit test, and two `ExtractMethodRecommender` in the
  integration test. Fixed: the two diamonds now infer; `start()` lost its `updateAndGet` lambda for a plain guard, which
  cleared the `AutoCloseableResource` warning rather than suppressing it — a probe establishes that, since removing the
  suppression entry leaves the inspection reporting nothing, and a second probe showed the `FutureReturnValueIgnored`
  entry is still needed, as the compile warns without it; the two integration cases share a `consumerProperties()`
  helper; and the unit test's bounded wait carries `@SuppressWarnings("BusyWait")`, since it is the deliberate window
  that proves a stopped sampler attempts nothing. A re-run over the same eight files reports no findings and no failed
  tools, with the suppression entries probed rather than assumed.
- **Prior findings with their instances** — rounds 1–15 and the advisor's two passes, in `findings/`.
- **Completed checks** — `:showcase-projection-service:check` green with integration tests and the coverage gate (1 m 14
  s); `spotlessCheck`; `openspec validate --all` 27.
- **Known limitations** — the sample age reads 20–40 s at a scrape; the multi-replica mixed-age view is unexercised.

## Round 17 — implementation screen, after round 16's record finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `d8cf14ba`; every unit file staged.
- **Packs** — `jvm-development`, `jvm-testing`, `deployment-operations`, `gradle-build`.
- **Supplied artifacts** — round 16's fix: the `AutoCloseableResource` entry is gone from `start()`, and the record now
  states what the two probes established — the inspection reports nothing without the entry (so the refactor cleared
  it), and the `FutureReturnValueIgnored` entry is still needed (the compile warns without it). The final annotation is
  `@SuppressWarnings("FutureReturnValueIgnored")`.
- **Prior findings with their instances** — rounds 1–16 and the advisor's two passes, in `findings/`.
- **Completed checks** — the IDE inspection re-run over the eight files with the entry removed (no findings, no failed
  tools); `:showcase-projection-service:check` green; `spotlessCheck`; `openspec validate --all` 27.
- **Known limitations** — the sample age reads 20–40 s at a scrape; the multi-replica mixed-age view is unexercised.
