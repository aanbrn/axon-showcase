# Tasks — operational-status overview and the dead SLO boundaries

The unit's record is `docs/units/add-operational-status-overview/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with `python3 scripts/claim-scan.py` over the staged
      artifacts; each candidate is resolved into `design.md`'s claims table or dismissed beside it, which states one
      reason per candidate kind. Verify: `design.md` carries the table and the dismissal line, and every scanned
      candidate is accounted for. Verified: the scan's candidates are the claims table's figures, the change's own
      statements and paths, and its verification commands; the dismissal line gives one reason per kind.

## 1. The overview

- [x] 1.1 `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`: add the **Operational status** row at
      `y=0` and shift every existing panel down by 8 rows, so the section order becomes operational status, the
      processing ratios and resources, then the per-service sections. Verify: no panel id changed, the row count is 8,
      the non-row panel count is 44, and the file holds 52 top-level entries. Verified: no panel id changed, the row
      count is 8, the non-row panel count is 44, and the file holds 52 top-level entries; no two top-level panels
      overlap.
- [x] 1.2 The **Scrape status** panel: a `stat` over `up{job=~"axon-showcase-.*"}` with `instant: true` and
      `range: false`, value mappings `1` → `Up` and `0` → `Down`, and no sparkline. Verify: each currently discovered
      target renders its instant state; a target that stops being discovered leaves no entry rather than a stale `Up`,
      and the panel's description says so. Verified: the provisioned panel renders one entry per discovered pod (the two
      command-service replicas are distinguishable by pod name), all reading Up; a query matching nothing renders the
      `noValue` text on the neutral base colour.
- [x] 1.3 The **Available / desired replicas** panel: one row per Deployment, joining
      `kube_deployment_status_replicas_available` and `kube_deployment_spec_replicas` on `deployment`, with both counts
      shown and no fallback shape that loses the pairing. Verify on the rendered panel, not only on its definition: five
      rows with both counts (`2` and `2` for the command service), and an unmatched side exercised by widening one query
      to a deployment that does not exist and confirming the row shows the missing side as missing rather than as zero.
      Verified on the rendered panel: five rows with both counts, the command service at `2/2`; restricting one query to
      a single Deployment leaves the other side empty for the remaining rows rather than zero, so the join is outer as
      intended.
- [x] 1.4 Give both panels a description stating what they read, that the `application` variable does not filter them,
      and what an empty or missing entry means. Verify: `verifyDashboardJson` passes and each description carries those
      three statements. Verified: `verifyDashboardJson` passes, and each description states what the panel reads, that
      the application variable does not filter it, and what a missing entry means.

## 2. The dead boundaries

- [x] 2.1 `showcase-query-service/src/main/resources/application.yml`: remove the `slo.http.server.requests` entry,
      leaving the gateway's. Verify: the query service's metrics block has no `slo` key, the gateway's still does, and
      the query service's tests pass. Verified: the query service's metrics block has no `slo` key, the gateway's still
      does, and the module's tests pass inside `check`.

## 3. The documentation

- [x] 3.1 `README.md`: the dashboard's panel and section counts, and the bullet's coverage sentence. Verify: the bullet
      states 44 panels across 8 sections, names the operational-status coverage beside the projector's operating state,
      and no other live copy of the old figures remains (`grep` for them across the tree). Verified: the README states
      44 panels across 8 sections and names the operational-status coverage; `grep` finds no other live copy of the old
      figures.
- [x] 3.2 Reconcile `design.md`'s claims table with the implemented files. Verify: every row's source still reproduces
      and the `skip_specs` reasoning names the surfaces it rests on. Verified: the two rows whose subject the change
      edits are pinned to the base revision and reproduce there, the rest are unchanged facts, and the `skip_specs`
      reasoning names the dashboard requirement and the absent distribution settings.

## 4. Verification

- [x] 4.1 Run the new queries against the cluster's Prometheus
      (`kubectl -n monitoring port-forward svc/kps-prometheus     19090:9090`). Verify: `up` returns one series per
      target and both replica queries return one series per Deployment, with the counts the panel needs. Verified: `up`
      returns eight series, and each replica query returns one series per Deployment.
- [x] 4.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`. Verify: both pass.
      Verified: `check` succeeded and `openspec validate --all` reports 27 items.
- [x] 4.3 Provision the dashboard and verify it **rendered**, not only provisioned:
      `./gradlew     :helm:chart:helmPackageMainChart`, then
      `helm upgrade axon-showcase helm/chart/build/helm/charts/axon-showcase -f helm/values/axon-showcase/values-local.yaml --namespace axon-showcase --wait`,
      then read the dashboard back from Grafana and screenshot the new section. Verify: 52 top-level entries — 44
      non-row panels and 8 sections — the scrape panel's instant states and mappings, and the replica table's five
      joined rows with an unmatched side exercised (task 1.3). Reach the scrape panel's empty state by temporarily
      editing its query in the Grafana UI to a `job` that matches nothing, screenshotting the text it renders, and
      restoring the query; record that text. The owner's manual pass sees the same screenshots. Verified: the release is
      at revision 13 and the provisioned dashboard equals the staged file — 52 entries, 44 non-row, 8 sections; the
      rendered section was screenshotted (scrape status Up per pod, the replica table 2/2 and 1/1), the one-sided table
      left the other side empty, and a query matching nothing rendered `No telemetry` on the neutral base colour.
- [x] 4.4 `./gradlew spotlessApply` after the last edit to a Spotless-owned file. Verify: the tree is clean of formatter
      changes. Verified: `spotlessApply` ran after the last edit and the tree carries no formatter change.

## 5. The review gate and the records

- [x] 5.1 The proposal stage's quick review: `review-quick` over the planning artifacts until a clean round, with each
      round's row and findings under `docs/units/add-operational-status-overview/`. Verify: `--expect-clean` and
      `--session-continuity` pass. Verified: rounds 1–6 in `rounds.tsv`, the closing round 6 with no finding class;
      `--expect-clean` and `--session-continuity` pass.
- [x] 5.2 The technical advisor's consultation on the proposal, when the owner authorizes it. Verify: the authorization
      is recorded and the findings are applied or their rejection recorded. Verified: the owner authorized it ("Обсуди с
      это советником…"), and the consultation ran before this unit's artifacts existed; its recommendation is the
      design's Context, two of the author's proposals were corrected (the binary `min_over_time`, the rejected ratio
      panel), and the dead query-service entry is the recommendation's content.

- [x] 5.3 The owner's manual pass over the proposal. Verify: the approval is recorded before any implementation edit.
      Verified: the owner approved the proposal after the advisor's pass over the written artifacts and instructed the
      implementation; the approval precedes every implementation edit, and the unit record states it.
- [x] 5.4 The implementation stage's quick review. Verify: the closing round is clean and the ledger checks pass.
      Verified: rounds 7–10 in `rounds.tsv`, the closing round 10 with no finding class; `--expect-clean`,
      `--session-continuity` and `--finding-instances` pass.

- [x] 5.5 The deep-review routing decision on the completed diff. Verify: the outcome and its signals are recorded.
      Verified: `ROUTING: REQUIRED` — `[infrastructure-or-deployment]` on
      `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`; the deep review runs before completion.

- [x] 5.6 The deep review, when 5.5 selects one, with its findings applied and re-reviewed. Verify: the pass's status
      and its findings' disposition are recorded. Verified: pass 1 (routing-required) reported no defects — it confirmed
      the lossless +8 shift, both panels' definitions against their decisions, the transformation chain's outer join,
      the yml edit with no surviving consumer, the counting convention, and `skip_specs`; it recommended the owner's
      manual pass.

- [x] 5.7 The technical advisor's consultation on the implementation, when the owner authorizes it. Verify: the
      authorization and the findings' disposition are recorded. Verified: the owner authorized it ("Отревьювь с
      советником до согласия"); the consultation found one problem — the unit record still said it had not been
      authorized, corrected here — and recommended the manual pass, with the panels' narrow claims, the transformation
      chain, and the `slo` removal confirmed.

- [x] 5.8 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or merged before it. Verified: the owner reviewed the implementation and the rendered panels and
      instructed delivery; nothing was committed before that approval.

- [x] 5.9 `docs/units/add-operational-status-overview/process-defects.md`: one record per defect observed, or the
      statement that none was. Verify: the file exists before 5.8 is requested. Verified: the file exists with one
      record — the planned `vision` hand-over could not read the screenshots.
