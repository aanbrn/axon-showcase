# Design — operational-status overview and the dead SLO boundaries

## Context

The dashboard was repaired in the previous change (PR #843): its stale query-service HTTP targets and `saga-service`
selectors are gone, its panels read the surfaces the stack actually has, and every panel carries a description. What it
still cannot show is whether the system is running: `up` and the replica counts are collected but never displayed, and
the only panel that ever claimed "availability" was deliberately re-framed as a cumulative processing ratio.

The owner asked whether Spring Boot's SLO configuration could express availability.
`management.metrics.distribution.slo` exists in Spring Boot 3.5.16 (`spring-boot-actuator-autoconfigure`, type
`Map<String, ServiceLevelObjectiveBoundary[]>`) and both the gateway and the query service configure it for
`http.server.requests` (300 ms, 500 ms, 1 s, 2 s). It sets **latency boundaries**, not an availability percentage: a
boundary alone carries neither a required success fraction nor a window. The gateway's entry is consumed by its latency
panel; the query service's decorates `http.server.requests`, which for that service is the actuator — its reads moved to
gRPC — so it is dead weight of the same class as the dashboard's removed HTTP targets.

The advisor's consultation (owner-authorized) settled the shape: an operational-status overview of what the telemetry
can actually establish, not an availability SLO; keep the gateway's boundaries, remove the query service's, add no
boundaries without a consumer. It also corrected two of the author's proposals, both recorded below as rejected
alternatives.

## Goals / Non-Goals

**Goals**

- Show, in one place, whether every discovered scrape target is being collected and whether every Deployment has its
  replicas.
- Say what a missing entry means on both panels, so a target that stops existing is not read as healthy.
- Remove the query service's SLO entry, which decorates a surface the service no longer serves.

**Non-Goals**

- An availability percentage or an SLO attainment panel: `up` and replica counts establish reachability and capacity,
  not that a user can complete work (ADR-0023 records the same boundary for the projector).
- New histogram boundaries, Alertmanager, or synthetic probes (`probe_success` does not exist in this deployment).
- A windowed success-ratio panel: the projector's `ingested` and `processed` counters are asynchronous, and the HTTP and
  gRPC surfaces classify business rejections differently, so a single "success probability" would mix unlike outcomes.
- Changing the gateway's SLO boundaries.

## Decisions

1. **A new section at the top, not a panel folded into the existing overview.** The top block is full at 24 columns
   (Processing Ratio at `y=0`, CPU and Memory at `y=7`), so a new **Operational status** row takes `y=0` with its two
   panels at `y=1`, and every existing panel shifts down 8 rows. The dashboard keeps its section order otherwise:
   operational status, the processing ratios and resources, then the per-service sections. Alternative rejected:
   replacing a panel in the top block, which would remove a signal to make room for another.
2. **Scrape status shows the current status per target.**
   - It queries `up` with `instant: true` and `range: false`, the pattern the projector-state panel already uses, so a
     sample from earlier in the range cannot stand in for the current state.
   - Value mappings render `1` as `Up` and `0` as `Down`.
   - A target that is no longer discovered is simply absent from the current result; its absence is what the reader
     sees, not a `Down` and not a stale `Up`.
   - The panel carries no sparkline: a range query would let a vanished target's last `1` render as healthy, which is
     the ambiguity this shape exists to avoid.
   - Its empty state — no series at all — is asserted in the rendered check rather than promised here, because Grafana's
     `noValue` treatment of a wholly empty panel is not established by the artifacts.
   - Alternatives rejected: `min_over_time(up[$__range])`, which over a binary series is only ever `0` or `1` — one
     failed scrape would blank the whole window and the 90 %/99 % thresholds would be meaningless — and
     `avg_over_time(up[$__range])`, a real share of successful scrapes, but a number a reader takes for availability,
     which `up` cannot establish; and a range query with a sparkline, which shows history only by weakening the
     current-status claim.

3. **The replica panel shows both counts.** One row per Deployment, with `kube_deployment_status_replicas_available` and
   `kube_deployment_spec_replicas` as separate instant queries joined on the `deployment` label, so a reader sees `2`
   and `2` rather than a ratio that hides an intentional zero or a missing series; a Deployment with no series is
   visible by its absence, which the panel's description says. Alternative rejected: `available / spec`, which renders
   `0/0` and a shortfall identically.
4. **The new panels select by the labels those series carry.** `up` carries `job`, `pod`, and `service` but no
   `application`; the kube-state series carry `deployment`, `namespace`, and `service`. The panels select
   `job=~"axon-showcase-.*"` and `deployment=~"axon-showcase-.*"`, and their descriptions say that the dashboard's
   `application` variable does not filter them. Alternative rejected: selecting by `namespace`, which couples the panels
   to a deployment choice the chart leaves to values.
5. **The query service's `slo` entry is removed, and the gateway's stays.** Nothing consumes the query service's
   boundaries — its HTTP surface is the actuator — while the gateway's are read by its latency panel. Alternative
   rejected: re-pointing the entry at the gRPC timer, which would add four boundaries to a metric already carrying 345
   series with no panel or alert consuming them.
6. **No spec delta.** The chart's dashboard requirement covers the dashboard's existence and purpose, not its panels,
   and no capability specifies the services' `management.metrics.distribution` settings (`git grep` over
   `openspec/specs/` finds neither `distribution.slo` nor `percentiles-histogram`), so the change records
   `skip_specs: true` with this reasoning rather than inventing a requirement for either.
7. **Verification splits the states a healthy cluster cannot show by what can establish them.** The down-target and
   replica-shortfall states stay definition-level: they are asserted through the panels' mappings and the table's join,
   and no outage is induced. The states that a _definition_ cannot establish — the wholly empty scrape panel and the
   replica table with an unmatched side — are exercised in the rendered check by a temporary query edit in Grafana,
   screenshotted and then reverted, which is what the Verification section states.

## Risks / Trade-offs

- **Both panels are flat while the system is healthy.** That is what an operational-status view is for: the mappings
  make the current state legible at a glance, and range history is deliberately absent from the scrape panel
  (decision 2) so a stale sample cannot pass for the current one.
- **The table's join depends on a transformation.** If Grafana's transformation behaviour differs from the pinned
  13.3.0, the panel would show two unjoined frames. The rendered check exercises the join, with an unmatched side
  included; a fallback that loses the per-Deployment pairing is not accepted, so a join that cannot be made to work
  re-opens this decision instead.
- **A wholly empty panel's text is unverified until the rendered check.** `noValue` is the field-level fallback the
  other panels use, but this panel's empty state has no series at all, and the artifacts do not establish Grafana's
  treatment there; the verification asserts an honest empty state and records what it renders.
- **Removing the query service's `slo` entry changes exported series.** Its actuator histograms lose four explicit
  boundaries; nothing reads them, and the default histogram boundaries remain.
- **The `application` variable does not filter the new panels.** Their descriptions say so; a reader who switches the
  variable sees the same operational state, which is correct for a single-application deployment but reads as
  inconsistent.

## Verification

```bash
# The new queries against the cluster's Prometheus (port-forward svc/kps-prometheus 19090:9090)
#   up{job=~"axon-showcase-.*"}            -> one series per target, each 1 today
#   kube_deployment_status_replicas_available{deployment=~"axon-showcase-.*"}
#   kube_deployment_spec_replicas{deployment=~"axon-showcase-.*"}
./gradlew verifyDashboardJson
./gradlew -p build-logic test --tests 'DashboardJsonRulesTests'
./gradlew check -PskipITs -Pcoverage.gate.enabled=false
openspec validate --all

# Provision and read back
./gradlew :helm:chart:helmPackageMainChart
helm upgrade axon-showcase helm/chart/build/helm/charts/axon-showcase \
  -f helm/values/axon-showcase/values-local.yaml --namespace axon-showcase --wait
# then GET /api/dashboards/uid/d5ebc0e9-… with the chart's admin credentials
```

The rendered checks cover what the definitions cannot: the replica table's joined rows with an unmatched side, and the
scrape panel's wholly empty state, reached by temporarily pointing its query at a `job` that matches nothing and
recording the text it renders.

## Claims table

| Claim                                                                                                         | Source                                                                                                                           | Result                                                                                                                                                             |
| ------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The projector-state panel already queries an instant value                                                    | The dashboard's Projector State panel (id 84)                                                                                    | `instant: true`, `range: false`, `graphMode: none`                                                                                                                 |
| The dashboard has 49 top-level entries — 42 non-row panels in 7 sections — and no free space in its top block | `git show 8bd9cb1e:helm/…/axon-showcase.json` piped to a JSON counter                                                            | 49 entries, 42 non-row, 7 rows; Processing Ratio 24 columns at `y=0`, CPU and Memory 12+12 at `y=7`                                                                |
| The README states the dashboard's panel and section counts                                                    | `git show 8bd9cb1e:README.md \| grep -n "panels across"`                                                                         | one line, `42 panels across 7 sections`                                                                                                                            |
| `up` covers eight targets and carries `job`, `pod`, and `service` but no `application`                        | Prometheus `api/v1/query` for `up` with its label set (port-forward)                                                             | 8 series; labels `container`, `endpoint`, `instance`, `job`, `namespace`, `pod`, `service`                                                                         |
| The replica metrics carry `deployment` and cover five Deployments                                             | Prometheus queries for both replica metrics                                                                                      | five Deployments, the command service at `2/2`, the rest at `1/1`                                                                                                  |
| The cluster has no failure in the retained window, so both panels read healthy today                          | `min_over_time(up[7d])` and `max_over_time(kube_deployment_status_replicas_unavailable[7d])`                                     | `1` for every target; `0` for every Deployment                                                                                                                     |
| The query service configures an HTTP SLO entry while its HTTP surface is the actuator                         | `git show 8bd9cb1e:showcase-query-service/src/main/resources/application.yml \| grep -n "slo" -A2`; the service's gRPC transport | the entry names `http.server.requests`; the query service's reads are gRPC, and its HTTP is the actuator                                                           |
| The gateway configures the same entry, and its latency panel reads the resulting buckets                      | The gateway's `application.yml` and the dashboard's Latency panel                                                                | `slo.http.server.requests` present; the panel's `histogram_quantile` reads `_bucket`                                                                               |
| The gateway's HTTP bucket series carry 73 `le` values, four of them the configured boundaries                 | Prometheus `count by (le) (http_server_requests_seconds_bucket{service="axon-showcase-api-gateway"})`                            | 73 values, including `0.3`, `0.5`, `1`, `2`                                                                                                                        |
| No rule selects this application's histogram series, and no Alertmanager route exists                         | Prometheus `api/v1/rules`; `helm/values/kps/values-local.yaml`                                                                   | no rule query mentions `http_server_requests`; the chart's own groups alert on infrastructure and replica state; `alertmanager.enabled: false`; no `probe_success` |
| No capability spec covers the services' distribution settings                                                 | `git grep -n "distribution.slo\|percentiles-histogram" 8bd9cb1e -- openspec/specs`                                               | no match                                                                                                                                                           |

Dismissals, one reason per kind: `path` — the named paths are this change's own artifacts and the files it edits;
`absolute` — each flagged word sits in a statement a table row covers or in the change's own prohibition; `count` — the
figures are the table's rows; `cross-ref` — the flagged commands and paths are this change's own verification
instructions, run for the rows above.

## Consequences

- The dashboard answers "is everything being collected and replicated?" before it answers anything about traffic.
- The query service stops exporting four histogram boundaries nobody reads; the gateway keeps the ones its latency panel
  uses.
- A future SLO panel has a precedent to build on — the gateway's boundaries — without new configuration until something
  consumes it.
