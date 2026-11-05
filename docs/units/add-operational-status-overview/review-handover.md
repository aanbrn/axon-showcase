# Review hand-over — add-operational-status-overview

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `add-operational-status-overview`; tree `/Users/afanasyev/Development/axon-showcase`
  (the primary checkout, this unit's tree); branch of the same name; base `origin/main` `8bd9cb1e`; nothing committed,
  every unit file staged.
- **Packs** — `deployment-operations` (the dashboard and its provisioning), `gradle-build` (the `verifyDashboardJson`
  gate), and `jvm-development` (the metrics configuration in the service's `application.yml`).
- **Supplied artifacts** — `proposal.md`, `design.md` (Context, Goals/Non-Goals, seven decisions with their rejected
  alternatives, risks, verification, the claims table and its dismissals), `tasks.md`, the `.openspec.yaml` declaring
  `skip_specs`; the unit record.
- **The proposed change** — a new **Operational status** section at the top of the dashboard (a per-target scrape-status
  stat with Up/Down/No-telemetry mappings and a sparkline, and an available-versus-desired replica table joined on the
  `deployment` label), every existing panel shifted down 8 rows, the query service's dead `slo.http.server.requests`
  entry removed, and the README's counts updated.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate add-operational-status-overview --strict` passes with the `skip_specs`
  declaration; `openspec validate --all` 27; `spotlessApply` has run; the staged tree passes
  `commit-hygiene --review-ready`; the claims table's sources were re-run against the base revision and the live cluster
  while the proposal was drafted.
- **Known limitations** — a healthy cluster shows neither a down target nor a replica shortfall, so those states are
  asserted through the panels' mappings and the table's join rather than by inducing an outage; the table depends on a
  Grafana transformation with a `stat` fallback named in the design.

## Round 2 — proposal screen, after round 1's ten findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 1's fixes: the counting convention is stated once (49 entries → 52, 42 non-row → 44, 7
  sections → 8) and tasks 1.1/4.3 use it; the per-target "No telemetry" state is gone — the panel shows discovered
  targets, and a target that stops being discovered leaves no entry, which its description says; task 3.1 adds the
  README's coverage sentence; decision 4 states the series' actual labels (`up`: job/pod/service; kube-state:
  deployment/namespace/service) and drops the convention analogy; the alerting claim narrows to "no Alertmanager route
  and no rule consumes a boundary"; the bucket count is 73; the fallback is two `stat` panels, one per metric, with the
  pairing loss stated; the proposal names the single query-service entry; decision 2 is split into bullets.
- **Prior findings with their instances** — round 1's ten, in `findings/round-1.md`.
- **Completed checks** — `openspec validate --strict` passes; `spotlessApply` has run; `commit-hygiene --review-ready`
  passes.
- **Known limitations** — a healthy cluster shows neither a down target nor a replica shortfall; the table's join is
  verified on the provisioned panel with the stated fallback.

## Round 3 — proposal screen, after round 2's six findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 2's fixes: task 1.2's verify now matches the corrected design (discovered targets only,
  a missing target leaves no entry); the `up` claims row states `job`/`pod`/`service` and the absence of `application`;
  the first claims row states the convention (49 entries, 42 non-row, 7 rows); the alerting row narrows to "no rule
  selects this application's histogram series", with the chart's own groups named; the proposal's README bullet names
  the coverage sentence; task 5.2 separates the two corrections from the recommendation's content.
- **Prior findings with their instances** — rounds 1 and 2, in `findings/`.
- **Completed checks** — `openspec validate --strict` passes; `spotlessApply` has run; `commit-hygiene --review-ready`
  passes.
- **Known limitations** — the stat/table behaviours are asserted from their definitions pending the provisioned
  read-back; the consultation's authorization is conversation-only.

## Round 4 — proposal screen, after the advisor's pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — the advisor's two confirmed problems applied: Scrape status now queries `up` with
  `instant: true`/`range: false`, the pattern the projector-state panel already uses, so a stale sample cannot pose as
  the current state, and it carries no sparkline; its wholly empty state is asserted in the rendered check rather than
  promised. Task 1.3 and 4.3 now require rendered verification of the joined replica rows, including an unmatched side,
  and the pre-authorized two-stat fallback is gone — a join that cannot be made to work re-opens the decision instead.
- **Prior findings with their instances** — rounds 1–3 in `findings/`; the advisor's two on this round.
- **Completed checks** — `openspec validate --strict` reports the `skip_specs` declaration; `spotlessApply` has run.
- **Known limitations** — the rendered checks run at implementation, and the owner's manual pass sees the screenshots.

## Round 5 — proposal screen, after round 4's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 4's fixes: the flat-panel risk no longer claims a sparkline (the mappings carry the
  current state, and range history is deliberately absent); the empty state's route is stated — point the query at a
  `job` that matches nothing, screenshot the text, restore — in task 4.3 and the design's verification; the
  instant-query precedent has a claims row (panel id 84: `instant: true`, `range: false`, `graphMode: none`).
- **Prior findings with their instances** — rounds 1–4 in `findings/`; the advisor's two on round 4.
- **Completed checks** — `openspec validate --strict` reports the `skip_specs` declaration; `spotlessApply` has run.
- **Known limitations** — the rendered checks run at implementation; the owner's manual pass sees their screenshots.

## Round 6 — proposal screen, after round 5's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 5's fix: decision 7 splits the unshowable states by what establishes them — the
  down-target and shortfall states stay definition-level, while the wholly empty scrape panel and the unmatched-side
  table are exercised in the rendered check by a temporary Grafana query edit, screenshotted and reverted — matching the
  Verification section.
- **Prior findings with their instances** — rounds 1–5 in `findings/`; the advisor's two on round 4.
- **Completed checks** — `openspec validate --strict` reports the `skip_specs` declaration; `spotlessApply` has run.
- **Known limitations** — the rendered checks run at implementation; the owner's manual pass sees their screenshots.

## Round 7 — implementation screen, first pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged, nothing committed.
- **Packs** — `deployment-operations` (the dashboard), `gradle-build` (the gate), `jvm-development` (the yml).
- **Supplied artifacts** — the implementation: a new `Operational status` row with the two panels (ids 87–89) and every
  existing panel shifted by 8 rows; **Scrape status** as an instant `up{job=~"axon-showcase-.*"}` stat with pod-named
  entries, `1`→Up / `0`→Down mappings, `noValue` "No telemetry" and a neutral base colour; **Available / desired
  replicas** as a table over two instant queries joined by `labelsToFields` → `joinByField(deployment, outer)` →
  `organize`, showing five Deployments with both counts; the query service's `slo` entry removed; the README at 44
  panels / 8 sections with the coverage sentence.
- **Rendered evidence** — screenshots of both panels and of the two edge states (a query matching nothing; one replica
  query restricted to a single Deployment). The first render exposed two defects, both fixed: `merge` left one row per
  side (replaced by `joinByField`), and the empty state painted red (base threshold now gray).
- **Prior findings with their instances** — the proposal rounds 1–6 in `findings/`; the advisor's two on round 4.
- **Completed checks** — `verifyDashboardJson`; `check` green (73 s); `openspec validate --all` 27; the release is at
  revision 13 and the provisioned dashboard equals the staged file (52 entries, 44 non-row, 8 sections); the session
  cookie used for the screenshots is rejected by Grafana now (401), and the throwaway edge dashboards were deleted.
- **Known limitations** — the `vision` hand-over could not read the images (process defect `#1`); the down-target and
  replica-shortfall states remain definition-level, as decision 7 states.

## Round 8 — implementation screen, after round 7's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 7's fixes: the two claims rows whose subject the change edits are pinned to the base
  revision and reproduce there, with task 3.2's note corrected; the unit record is rewritten for the implementation
  stage and records the owner's approval; tasks 5.1 and 5.3 are ticked against the ledger and that approval.
- **Prior findings with their instances** — the proposal rounds 1–6 and the implementation round 7, in `findings/`.
- **Completed checks** — `verifyDashboardJson`; `check`; `openspec validate --all` 27; the release at revision 13 with
  the provisioned dashboard equal to the staged file.
- **Known limitations** — the `vision` hand-over could not read the screenshots (process defect `#1`); the down-target
  and shortfall states remain definition-level.

## Round 9 — implementation screen, after round 8's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 8's fixes: the two base-pinned claims rows escape their shell pipes, so
  `verifyTableRows` and `check` are green again; the unit record's change-set line names the three implementation files
  that live outside the change dir and the unit directory.
- **Prior findings with their instances** — rounds 1–8 in `findings/`.
- **Completed checks** — `verifyTableRows`, `check`, `verifyDashboardJson`, `openspec validate --all` 27.
- **Known limitations** — the rendered claims rest on screenshots absent from the repository (process defect `#1`).

## Round 10 — implementation screen, after round 9's finding

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 9's fix: the unit record's change-set line now names the three implementation files
  beside the change dir and the unit directory.
- **Prior findings with their instances** — rounds 1–9 in `findings/`.
- **Completed checks** — `check`, `verifyTableRows`, `verifyDashboardJson`, `openspec validate --all` 27.
- **Known limitations** — the rendered claims rest on screenshots absent from the repository (process defect `#1`).

## Round 11 — implementation screen, after the advisor's pass

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — the advisor's one problem applied: the unit record no longer says the implementation
  consultation was unauthorized, and states its disposition; task 5.7 records the authorization, the finding, and the
  recommendation that the manual pass follow. The advisor confirmed the panels' narrow claims, the transformation chain,
  the placement with the 44/8 convention, the `slo` removal, and that rendered evidence is the right class here.
- **Prior findings with their instances** — rounds 1–10 in `findings/`.
- **Completed checks** — `check`, `verifyTableRows`, `verifyDashboardJson`, `openspec validate --all` 27.
- **Known limitations** — the screenshots' pixels are the owner's to confirm (process defect `#1`).

## Round 12 — implementation screen, at the manual-pass boundary

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 11's finding fixed by recomputing the whole checkpoint in one pass rather than patching
  the field it named: the Stage, Next step, Change set, and Pending decision are one snapshot taken from `git status`,
  `rounds.tsv`, and `tasks.md`; task 5.9 records the process-defect file.
- **Prior findings with their instances** — rounds 1–11 in `findings/`.
- **Completed checks** — `check`, `verifyTableRows`, `verifyDashboardJson`, `openspec validate --all` 27.
- **Known limitations** — the screenshots' pixels are the owner's to confirm (process defect `#1`).

## Round 13 — implementation screen, closing the loop

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 12's fix: the Stage line no longer names a round as the clean one; it states that the
  loop closed with a clean round and points at `rounds.tsv` as the round inventory, so the record cannot lag the ledger
  again.
- **Prior findings with their instances** — rounds 1–12 in `findings/`.
- **Completed checks** — `check`, `spotlessCheck`, `verifyTableRows`, `verifyDashboardJson`, `openspec validate --all`
  27; `--review-ready` passes on the current base.
- **Known limitations** — the screenshots' pixels are the owner's to confirm (process defect `#1`).

## Round 14 — implementation screen, closing the loop

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 13's finding fixed structurally: the Stage no longer states whether the loop has
  closed; it records the completed stages and says the ledger alone states closure.
- **Prior findings with their instances** — rounds 1–13 in `findings/`.
- **Completed checks** — `check`, `spotlessCheck`, `verifyTableRows`, `verifyDashboardJson`, `openspec validate --all`
  27; `--review-ready` passes.
- **Known limitations** — the screenshots' pixels are the owner's to confirm (process defect `#1`).

## Round 15 — implementation screen, closing the loop

- **Unit, tree, branch, base** — unchanged; base `origin/main` `8bd9cb1e`; every unit file staged.
- **Packs** — `deployment-operations`, `gradle-build`, `jvm-development`.
- **Supplied artifacts** — round 14's finding fixed by deletion, not rewording: the Stage's opening clause no longer
  says the loop closed; the record states only what it verifies (implementation complete, stages recorded) and delegates
  closure to the ledger.
- **Prior findings with their instances** — rounds 1–14 in `findings/`.
- **Completed checks** — `check`, `spotlessCheck`, `verifyTableRows`, `verifyDashboardJson`, `openspec validate --all`
  27; `--review-ready` passes.
- **Known limitations** — the screenshots' pixels are the owner's to confirm (process defect `#1`).
