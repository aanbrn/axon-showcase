# Design — repair the dashboard's stale panels and describe every signal

## Context

The bundled dashboard (`helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`) predates two architecture
changes. The query service now serves reads over gRPC (`ShowcaseQueryTransportService`) and exposes HTTP only on its
actuator, yet three of its panels still query `http_server_requests_*` with `uri!~"/actuator.*"`, so those targets can
never match; and a `saga-service` that no module declares still appears in the overview's CPU and Memory panels
(`settings.gradle.kts` lists no such module).

At this unit's base revision (`ef6fc2f4`) the file has 49 panels: 42 data panels and 7 row sections. Three data panels
carry a description (the projector panels added by the previous change); 18 carry no unit or `unit: "none"`; the windows
are 87 `[1m]` occurrences spread across 72 of the file's 97 targets (76 targets carry a window at all) while the cluster
scrapes every 30 s. The three `percentunit` panels are Availability, CPU Usage, and Memory Usage — the same three whose
thresholds sit on a different scale from their values (CPU and Memory at `80` against a fraction, Availability at
`99.9`/`99.99` against `max: 1`).

The advisor's consultation — authorized by the owner and recorded in
`docs/units/repair-dashboard-panels-and-descriptions/unit.md` — put correctness before presentation and rejected an
overview row, a `pluginVersion`/`schemaVersion` bump, a metadata campaign, and any new instrumentation. Its two open
points are addressed here as decisions: which surface the query-service panels should read, and how `$__rate_interval`
is verified before adoption.

## Goals / Non-Goals

**Goals**

- Make every existing panel read a signal the system actually emits.
- Make every panel's number, unit, and empty state readable from the dashboard itself.
- Keep one checker for the dashboard: a description obligation in the existing gate.

**Non-Goals**

- Adding panels or sections; the panel count, section count, and ids stay as they are.
- New instrumentation, including gRPC transport metrics or Kafka consumer lag.
- A dashboard generator, a PromQL linter, or a writing-quality gate.
- Refreshing `pluginVersion`/`schemaVersion`, or adding tags and links.

## Decisions

1. **The Query Service panels keep the `fetch_showcase_*` timers and lose the HTTP targets.** Those timers measure the
   query work the section is about; the HTTP targets measure a surface the service no longer has. The three live gRPC
   server families (`grpc_server_processing_duration_seconds_*`, `grpc_server_seconds_*`, and
   `grpc_server_call_duration_seconds_*`) were considered — and rejected: they measure the transport, not the query, and
   adding them would be new signal under the guise of a repair. Alternative rejected: keeping the HTTP targets and
   describing them as permanently empty.
2. **The Latency panel's two fetch targets gain the selectors their siblings carry.** Its `fetch_showcase_*` averages
   currently sum across every application and service, unlike the Throughput and Failure Rate panels beside them.
3. **The `saga-service` targets are removed, not re-pointed.** The saga runs inside the command service, so a re-pointed
   series would duplicate the command-service line under a wrong name; the overview then shows the four services that
   exist.
4. **Thresholds move to the value's scale.** CPU and Memory keep `percentunit` and take a `0.8` threshold with `max: 1`;
   a fraction-valued panel with an `80` threshold never turns red, so the current colour is a false signal rather than a
   missing one.
5. **"Availability" becomes "Processing Ratio", cumulative by construction and described as such.** Its four targets
   divide lifetime counters — the gateway's request outcomes, the command bus's processed and ingested counters, the
   projector's, and the query service's fetch outcomes — so it is not uptime, and one uniform success-rate sentence
   would misdescribe three of them. Its description names what each target divides and states that the value is
   cumulative since each process started, that a restart resets it, and that it is not an SLO; its thresholds become
   fractions (`0.999`, `0.9999`) with `max: 1`. Its query-service target is rebuilt from the `error` labels of the two
   `fetch_showcase_*` counters the section already reads — the union before the division, so the ratio follows both read
   paths — because the HTTP target it replaces is one of the dead ones. Alternative rejected: a windowed ratio
   (`increase(...[$__rate_interval])`) — it reads "No data" on an idle system, which on a reference app left running
   without traffic is most of the time, and an empty ratio panel reads like an outage.

6. **The failure panel aggregates both `fetch_showcase_*` families with `or` before summing.** `sum(rate(a) or rate(b))`
   and `sum by (error) (rate(a) or rate(b))` include an error present in only one family, and a family with no series
   contributes nothing rather than collapsing the whole expression. Alternative rejected: the current
   `sum(...) + sum(...)` of two separately aggregated vectors, which matches on every label — including `error` in the
   second target — so a value only one side carries is dropped, and an absent side empties the sum.
7. **Backend rate windows become `$__rate_interval`; the browser-experience windows stay at `[5m]`.** A `[1m]` rate over
   a 30-second scrape reads two samples. `$__rate_interval` is Grafana's interval for rate queries and adapts to the
   scrape interval; its expansion is **verified on the deployed instance before adoption** (a browser inspection of one
   panel's query, plus a comparison of the rendered series against a fixed `[2m]` reference), because the expansion is a
   property of the Grafana version rather than of the file. If the check cannot be made, the fallback is `[2m]` — four
   samples at the deployed scrape interval — recorded as the accepted limit. Alternative rejected: `irate`, which is
   noisier at this cadence and would change what the latency percentiles read.
8. **Every data panel gains a description, and units follow the quantity.** A description states what is counted, which
   statistic is shown, and what an empty result means — where an empty panel cannot distinguish no recorded activity
   from missing telemetry, and says both. The families whose series is created on first use say so —
   `fetch_showcase_by_id_*` backs panels 60, 61, and 64, and `showcase_web_errors_total` backs panel 83 — because an
   empty panel otherwise reads as a quiet system. Rates take `reqps`, connection gauges and the batch-size panel (events
   per batch, a count) take `short`, and the state panel keeps its value mappings and no unit.
9. **The gate requires a description only where a panel queries something.** `DashboardJsonRules` gains one rule: a
   panel that declares at least one target carries a non-blank description, at any depth, with rows and target-less
   panels exempt. The rule asserts presence, not quality — a minimum length, a keyword, or a template would be
   unenforceable judgement — and the `code-quality` requirement owns it. Alternatives rejected: gating units (a state
   enum and a dimensionless count legitimately differ) and gating prose quality (not machine-checkable).
10. **The dashboard's own description states what the view covers.** It replaces the placeholder with what the view
    shows and what a panel's description is for, and no tags or links are added — the advisor's "metadata campaign"
    exclusion.

## Risks / Trade-offs

- **`$__rate_interval`'s expansion is unverified until the browser check.** Mitigated by the fallback in decision 7 and
  by the check running before the change is reported done.
- **Removing the HTTP targets removes three visible series.** They can never match, so the removal loses no signal; the
  section then measures the fetch timers, whose by-id member appears only after the first by-id read.
- **The cumulative ratio can look alarming right after a restart.** Its description says the value is cumulative since
  each process started, and that a restart resets it.
- **A 42-description edit is a large hand edit of one JSON file.** The gate's rule and a scripted completeness check
  cover the omission case; the review covers whether each description is true.
- **A metric that later disappears still renders "No data".** The description convention names what the panel reads, so
  a reviewer can tell a legitimate empty state from a wrong name; the gate cannot.

## Verification

```bash
# The queries, against the cluster's Prometheus (port-forward svc/kps-prometheus 19090:9090)
# .../api/v1/query?query=<each rebuilt expression>  — every one returns the expected series or an explained none
./gradlew verifyDashboardJson
./gradlew -p build-logic test --tests 'DashboardJsonRulesTests'
./gradlew check -PskipITs -Pcoverage.gate.enabled=false
openspec validate --all

# The provisioned dashboard, read back from Grafana
./gradlew :helm:chart:helmPackageMainChart
helm upgrade axon-showcase helm/chart/build/helm/charts/axon-showcase \
  -f helm/values/axon-showcase/values-local.yaml --namespace axon-showcase --wait
# then GET /api/dashboards/uid/d5ebc0e9-… with the chart's admin credentials
```

## Claims table

| Claim                                                                                                                          | Source                                                                                                                                        | Result                                                                                                  |
| ------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| The base revision's dashboard has 42 data panels, 7 rows, and 3 described panels                                               | `git show ef6fc2f4:helm/…/axon-showcase.json` piped to a JSON counter                                                                         | `panels 49 \| data 42 \| rows 7`; `described: 3`                                                        |
| 18 data panels carry no unit or `unit: "none"`                                                                                 | The same dump                                                                                                                                 | `no-or-none unit: 18 \| real unit: 24`                                                                  |
| 87 `[1m]` windows sit across 72 of 97 targets, and 4 `[5m]` windows across 4 targets                                           | The same dump, counting windows per target                                                                                                    | `targets 97 \| with a window 76 \| [1m] occurrences 87 \| targets with [1m] 72 \| [5m] occurrences 4`   |
| Panels 4, 60, and 61 carry `http_server_requests_*` targets selecting the query service                                        | The same dump                                                                                                                                 | `panels with query-service HTTP targets: [4, 60, 61]`                                                   |
| Panels 29 and 44 carry `saga-service` targets, and no module declares a saga service                                           | The same dump; `grep -c "saga-service" settings.gradle.kts`                                                                                   | `panels with saga-service targets: [29, 44]`; `0`                                                       |
| The `percentunit` panels are Availability, CPU Usage, and Memory Usage                                                         | The same dump, selecting panels whose `unit` is `percentunit`                                                                                 | `percentunit panels: [4, 29, 44]`                                                                       |
| Every data panel declares at least one target, so the description rule covers all 42                                           | The same dump                                                                                                                                 | `data panels without targets: []`                                                                       |
| The query service serves reads over gRPC and exposes HTTP only on its actuator                                                 | `showcase-query-service/src/main/java/showcase/query/ShowcaseQueryTransportService.java`; the dashboard's own selectors exclude `/actuator.*` | the transport class implements the gRPC service; the panels' `uri!~"/actuator.*"` leaves no HTTP series |
| `fetch_showcase_list_seconds_*` is served live, while `fetch_showcase_by_id_seconds_*` and `showcase_web_errors_total` are not | Prometheus `api/v1/label/__name__/values` and a per-name query (port-forward `svc/kps-prometheus`)                                            | the list family returns series tagged `error`/`reactor_type`; the other two return none                 |
| The by-id family backs panels 60, 61, and 64, and the web-errors family backs panel 83                                         | The base dump, selecting panels whose targets name each family                                                                                | `fetch_showcase_by_id` in [60, 61, 64]; `showcase_web_errors_total` in [83]                             |
| The cluster scrapes every 30 seconds                                                                                           | Prometheus `api/v1/status/config`                                                                                                             | `scrape_interval: 30s`                                                                                  |
| The by-ID `NOT_FOUND` is raised after the publisher's observation tap, so the timer records a completed-empty read             | `showcase-query-service/src/main/java/showcase/query/ShowcaseQueryHandler.java`, the by-ID handler                                            | `.tap(observationListenerFactory)` precedes `switchIfEmpty(Mono.error(… NOT_FOUND …))`                  |
| Grafana 13.3.0 serves the provisioned dashboard                                                                                | The `kps` release's chart version and the Grafana API's health/version endpoints                                                              | Grafana answers the dashboard API and reports its build version                                         |
| The advisor's consultation ran under the owner's authorization and its recommendation is this change's scope                   | `docs/units/repair-dashboard-panels-and-descriptions/unit.md`, the `Pending decision` field                                                   | the record names the owner's request, the recommendation, and the four exclusions                       |

Dismissals, one reason per kind: `path` — the named paths are this change's own artifacts and the files it edits;
`absolute` — each flagged word sits in a statement a table row covers or in the change's own prohibition; `count` — the
figures are the table's rows; `cross-ref` — the flagged commands and paths are this change's own verification
instructions, run for the rows above.

## Consequences

- The dashboard's Query Service section stops showing a series that can never exist, and its overview stops showing a
  service that does not exist.
- Every panel becomes readable without the code: what it counts, in which unit, and what an empty result means.
- The gate's description obligation keeps a future panel from silently losing that property.
- The panel set, the section count, and the ids are unchanged, so the README's counts stay true.
