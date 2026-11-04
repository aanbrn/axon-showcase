# Tasks — repair the dashboard's stale panels and describe every signal

The unit's record is `docs/units/repair-dashboard-panels-and-descriptions/unit.md`.

## 0. The pre-write pass

- [x] 0.1 The review gate's pre-write derivation pass ran, seeded with `python3 scripts/claim-scan.py` over the staged
      artifacts; each candidate is resolved into `design.md`'s claims table or dismissed beside it, which states one
      reason per candidate kind. Verify: `design.md` carries the table and the dismissal line, and the scan's candidates
      are each accounted for. Verified: the scan's candidates are the claims table's figures, the change's own
      statements, its own paths, and its verification commands, and the dismissal line gives one reason per kind.

## 1. The query-service panels

- [x] 1.1 Remove the `http_server_requests_*` targets from panels 4 (`Availability`), 60 (`Throughput`), and 61
      (`Latency`), leaving the `fetch_showcase_*` targets that measure the query work. Verify: no target in the file
      selects `http_server_requests` for `service="axon-showcase-query-service"`, and the other services' HTTP panels
      are unchanged. Verified: no target in the file selects `http_server_requests` for the query service (0 matches),
      and the other services' HTTP panels are unchanged.
- [x] 1.2 Add the `application` and `service` selectors to panel 61's two `fetch_showcase_*` targets, matching panels 60
      and 64. Verify: every `fetch_showcase_*` target in the file carries both selectors, and the panel's two averages
      return the same series the selectors name when run against Prometheus. Verified: every `fetch_showcase_*` target
      carries both selectors; the list average returns a series, and the by-id average stays empty until the first by-id
      read, which the panel's description states.

## 2. The overview

- [x] 2.1 Remove the `saga-service` targets from panels 29 (`CPU Usage`) and 44 (`Memory Usage`). Verify: no target in
      the file names `saga-service`, and each panel still renders one series per deployed service. Verified: no target
      names `saga-service` (0 matches), and each panel carries one target per deployed service.
- [x] 2.2 Move panels 29 and 44 to the scale their values use: threshold `0.8` and `max: 1` on the `percentunit` field.
      Verify: the threshold value is a fraction and the panel declares `max: 1`, so a value near the limit colours.
      Verified: both panels carry threshold `0.8` and `max: 1`.

## 3. The ratio and failure panels

- [x] 3.1 Rename panel 4 to `Processing Ratio`, express its thresholds as fractions (`0.999`, `0.9999`) with `max: 1`,
      write its description so it states that the value is cumulative since each process started and that a restart
      resets it, and rebuild its query-service target from the `error` labels of both `fetch_showcase_*` counters.
      Verify: each of the panel's four targets divides counters of one family, its title no longer claims availability,
      its threshold values lie within `max`, and its description carries the cumulative-and-reset sentence. Verified:
      the panel is titled `Processing Ratio`, carries four targets with thresholds `[null, 0.999, 0.9999]` under
      `max: 1`, and its description carries the per-ratio, cumulative, and restart statements.
- [x] 3.2 Aggregate panel 64's two targets as `sum(rate(list) or rate(by-id))` and
      `sum by (error) (rate(list) or rate(by-id))`, so an error carried by one family survives and an absent family
      contributes nothing. Verify, against Prometheus and with the unfiltered families:
      `sum(rate(fetch_showcase_list_seconds_count[2m]) or rate(fetch_showcase_by_id_seconds_count[2m]))` returns the
      list family's value while `sum(rate(list[2m])) + sum(rate(by-id[2m]))` returns nothing (the two results recorded);
      then read a showcase by id that the gateway has not served yet — a newly created one, since a cached read records
      no timer (`curl localhost:8080/showcases/<id>`) — so the by-id timer appears, and confirm the union's value equals
      the two families summed. The error-only-in-one-family case is not induced live; the union keeps both because `or`
      returns the left vector plus the right's non-matching series, which differ in `reactor_type` (`Flux` versus
      `Mono`) — `rate` drops `__name__`, so that is the only label that distinguishes them. Verified: with the by-id
      family absent the union returned the list value while `sum + sum` returned nothing; after one unknown-id read
      created the by-id series the union equalled the sum, and that series carries `error="none"`, confirming the
      not-found is raised after the tap.

## 4. Query windows

- [x] 4.1 Verify `$__rate_interval`'s expansion on the deployed Grafana before changing any window: convert one
      representative panel (API Gateway `Throughput`) to `$__rate_interval`, provision it, and read the expanded range
      in the browser's query inspector; then compare its rendered series with the same query at a fixed `[2m]`. Verify:
      the expanded window is recorded and is at least four scrape intervals at 30 s; if it is not, 4.3 applies instead
      of 4.2. Verified: `$__rate_interval` resolved to `[2m0s]` at a 30 s step in the deployed Grafana's executed query
      string — four scrape intervals — while the `[1m]` control stayed unchanged. The expansion was re-read at `now-5m`
      and `now-1h` and stayed `[2m0s]` at both, so a shorter view does not drop it below four scrape intervals.
- [x] 4.2 Replace `[1m]` with `$__rate_interval` in every remaining backend rate and histogram-rate expression, leaving
      the five-minute browser-experience windows alone. Verify: no `[1m]` remains, the four `[5m]` windows are
      unchanged, and each rewritten query returns the series it returned before at the same instant. Verified: no `[1m]`
      remains, 84 `$__rate_interval` occurrences are served, and the four `[5m]` windows are unchanged.
- [x] 4.3 IF 4.1 cannot establish the expansion, THEN use a fixed `[2m]` instead and record the fallback's limit in
      `design.md`. Verify: the choice is stated with the evidence that drove it. Verified: not applied — 4.1 established
      the expansion, so the fallback was unnecessary.

## 5. Descriptions and units

- [x] 5.1 Give every data panel a description that states what it counts, which statistic it shows, and what "No data"
      means there — where an empty panel cannot distinguish no recorded activity from missing telemetry, say both;
      describe each of the Processing Ratio panel's four ratios on its own, as design decision 5 requires; do not let a
      description claim the `fetch_showcase_*` error families cover every query failure, since the by-ID `NOT_FOUND` is
      raised after the publisher's observation tap and records a completed-empty read; and make the panels whose metric
      appears only after first use say so. Verify: every one of the 42 data panels carries a non-blank description, and
      `verifyDashboardJson` passes. Verified: 42 of 42 data panels carry a non-blank description and
      `verifyDashboardJson` passes.
- [x] 5.2 Correct every unit to the quantity shown: `reqps` for the rate panels, `short` for the connection gauges and
      the batch-size panel (events per batch, a count), and no unit on the state panel. Verify: no data panel carries
      `unit: "none"` or no unit except the state panel, and each rate panel's unit is a per-second one. Verified: only
      the state panel carries no unit; every rate panel is `reqps`, the connection gauges and the batch-size panel are
      `short`.
- [x] 5.3 Extend the dashboard's own `description` to state what the view covers. Verify: the field says what the view
      shows and what a panel's description is for, and the dashboard's tags and links are unchanged. Verified: the
      dashboard description states both, and its tags and links are unchanged.

## 6. The gate

- [x] 6.1 `build-logic/src/main/kotlin/DashboardJsonRules.kt`: a panel that declares at least one target carries a
      non-blank string description, at any depth, with rows and target-less panels exempt; extend the object's KDoc and
      the `verifyDashboardJson` task description in `build.gradle.kts` so neither still describes a JSON-only gate.
      Verify: the bundled dashboard passes the rule, a panel with an empty, whitespace-only, or non-string description
      fails it, and both descriptions name the description obligation. Verified: the bundled dashboard passes; a
      missing, empty, whitespace-only, or non-string description fails naming the panel; both the rules object's KDoc
      and the Gradle task description name the obligation.
- [x] 6.2 `build-logic/src/test/kotlin/DashboardJsonRulesTests.kt`: a known-bad case per rejected shape — a missing
      description, an empty one, a whitespace-only one, a non-string one, and a nested panel that declares a target
      without a description — plus a known-good dashboard whose querying panels carry descriptions and which includes a
      row that declares a target and carries none, and a text panel that declares no target. Verify: each bad fixture
      yields exactly one violation naming the file and the panel, the known-good one yields none, and removing the rule
      fails only its own cases. Verified: 23 tests; removing the rule fails only the five description cases, and the
      known-good fixture with a target-bearing row and a target-less text panel passes.
- [x] 6.3 Reconcile the `code-quality` delta with the rule as implemented. Verify: the requirement's clause and its
      scenario name exactly what the rule rejects and nothing it does not. Verified: the delta's clause and scenario
      carry the row exemption and the presence-only rule the code implements.

## 7. Verification

- [x] 7.1 Run every rebuilt query against the cluster's Prometheus
      (`kubectl -n monitoring port-forward svc/kps-prometheus 19090:9090`). Verify: each returns the expected series,
      and a series that is legitimately absent (the by-id timer before its first use) is explained in the panel's
      description rather than left looking broken. Verified: each rebuilt expression was run against the cluster's
      Prometheus, and the absent by-id and web-error families are explained in their panels' descriptions.
- [x] 7.2 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and `openspec validate --all`. Verify: both pass.
      Verified: `check` succeeded and `openspec validate --all` reports 27 items.
- [x] 7.3 Provision the dashboard and read it back: `./gradlew :helm:chart:helmPackageMainChart`, then
      `helm upgrade axon-showcase helm/chart/build/helm/charts/axon-showcase -f helm/values/axon-showcase/values-local.yaml --namespace axon-showcase --wait`,
      then read the dashboard from Grafana's API. Verify: the panel count and ids are unchanged, the renamed panel and
      the corrected descriptions appear, and the two removed families are gone. Verified: the release is at revision 5;
      Grafana returns 49 panels with unchanged ids, 42 described, `Processing Ratio` with fraction thresholds, four
      targets on panels 29 and 44, no `saga-service` or query-service HTTP target, 84 `$__rate_interval`, and four
      `[5m]`.
- [x] 7.4 `./gradlew spotlessApply` after the last edit to a Spotless-owned file. Verify: the tree is clean of formatter
      changes. Verified: `spotlessApply` ran after the last edit and the tree carries no formatter change.

## 8. The review gate and the records

- [x] 8.1 The proposal stage's quick review: `review-quick` over the planning artifacts, looping until a round reports
      no new observations, with each round's row and finding instances under
      `docs/units/repair-dashboard-panels-and-descriptions/`. Verify: `--expect-clean` and `--session-continuity` pass.
      Verified: rounds 1–6 in `rounds.tsv`, the closing round 6 with no finding class; the owner approved the proposal
      after the advisor's second pass.

- [x] 8.2 The technical advisor's consultation on the proposal. Verify: the owner's authorization is recorded, and the
      advisor's findings are applied or their rejection is recorded. Verified: the owner's request to consult
      ("Подумайте с советником…") is recorded in the unit record's `Pending decision`, the recommendation is the
      design's Context, and the four exclusions are its Non-Goals. A second pass, which the owner requested over the
      written proposal before their approval, found one coherence defect — the delta's description obligation did not
      carry the row exemption — and one content constraint for the descriptions (the by-ID `NOT_FOUND` is raised after
      the observation tap); both are applied, and it recommended the proposal for the owner's decision otherwise
      unchanged.

- [x] 8.3 The owner's manual pass over the proposal. Verify: the approval is recorded before any implementation edit.
      Verified: the owner approved the proposal after the advisor's second pass, with the instruction that the
      implementation may begin once the advisor's findings were applied; the closing quick round was clean and every
      finding was applied, so implementation started from this point.

- [x] 8.4 The implementation stage's quick review. Verify: the closing round is clean and the ledger checks pass.
      Verified: rounds 7–10 in `rounds.tsv`, the closing round 10 with no finding class; the deep pass's findings are
      applied, and `--expect-clean`, `--session-continuity` and `--finding-instances` pass.

- [x] 8.5 The deep-review routing decision on the completed diff. Verify: the outcome and its signals are recorded on
      this task. Verified: `ROUTING: REQUIRED` — `[infrastructure-or-deployment]` on
      `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`; the deep review runs before completion.

- [x] 8.6 The deep review, when 8.5 selects one, with its findings applied and re-reviewed. Verify: the pass's status
      and its findings' disposition are recorded. Verified: pass 1 (routing-required) reported two should-fix findings —
      panel 80's description omitted TTFB, which its selector includes, and the CPU/Memory descriptions named per-pod
      figures where the metrics report the node's CPU and total memory usage — and two nitpicks (Processing Ratio's "did
      not fail" wording, which counts 4xx; tasks 8.1/8.4 unticked); all applied, and the review confirmed the removed
      surfaces, the rebuilt queries, the window change, the gate rule, and the delta. The fixes are re-screened by the
      implementation quick round.

- [x] 8.7 The technical advisor's consultation on the implementation. Verify: the owner's authorization is recorded, and
      the advisor's findings are applied or their rejection is recorded. Verified: the owner's plan carries this pass
      and their approval of it; the consultation found one problem — empty-state sentences turned missing telemetry into
      reassurance — now fixed on all 42 descriptions, and its shorter-range hypothesis was tested and refuted (`[2m0s]`
      at `now-5m` and `now-1h`, on task 4.1). A confirmation pass over the corrected tree found one problem — panel 60's
      description attached missing telemetry to a measured zero — now split into "a line at zero" and "missing points";
      it recommended the manual pass otherwise.

- [x] 8.8 The owner's manual pass over the finished unit. Verify: the approval is recorded, and nothing is committed,
      pushed, or merged before it. Verified: the owner reviewed the implementation after the advisor's confirmation pass
      and instructed delivery; nothing was committed before that approval.

- [x] 8.9 `docs/units/repair-dashboard-panels-and-descriptions/process-defects.md`: one record per defect observed while
      working, or the statement that none was. Verify: the file exists before 8.8 is requested. Verified: the file
      exists with one record — a prose replacement that matched nothing and reported success.
