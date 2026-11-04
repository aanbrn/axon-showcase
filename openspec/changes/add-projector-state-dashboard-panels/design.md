# Design — projector operating-state panels

## Context

The projection service's `ShowcaseProjectorStateMonitor` registers three Micrometer gauges under the `showcaseProjector`
prefix, and the Prometheus registry exposes them as `showcaseProjector_state` (0 stopped, 1 running, 2 retrying),
`showcaseProjector_processing` (batches in flight), and `showcaseProjector_last_progress_seconds` (epoch seconds of the
last resolved batch, 0 before the first). The third name gains the unit suffix from the gauge's `baseUnit = seconds`;
the Micrometer name is `showcaseProjector.last.progress`. All three are scraped by the projection service's
ServiceMonitor.

The bundled dashboard (`helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`) holds 39 timeseries
panels in 7 row sections at this unit's base revision (`96edb035`), with unique panel ids (the highest is 83) and no
overlapping grid positions. Its Projection Service section shows event age, throughput, latency, batch size, and failure
rate — all of which stop changing when the projector stops consuming, exactly as they do when the system is idle. The
dashboard file is hand-maintained, and the only automated check over it is `verifyDashboardJson`, which parses it as one
JSON document and asserts nothing about its panels (`showcase/quality/code-quality`, "Bundled Grafana dashboard JSON is
valid").

ADR-0023 records the boundary this change must not overstate: a projector that has reached partition assignment can
remain reported as running while Kafka is unreachable or processing is stalled, and an unchanged progress timestamp
alone cannot distinguish that from healthy idleness. Consumer-lag-based stall detection needs a Kafka admin client the
service does not have and stays parked in `docs/ideas.md`.

## Goals / Non-Goals

**Goals**

- Make the projector's operating state, in-flight batch count, and time since its last resolved batch visible on the
  dashboard's Projection Service section.
- Keep the dashboard gate honest about the hand edit this change makes: a panel re-layout cannot ship overlapping
  panels, a repeated panel id, or a target whose query expression was emptied.
- Pin, on the producer side, the three exposition names the panels query.

**Non-Goals**

- Deciding whether the projector is stalled. The panels report the state the service reports and the age of its last
  resolved batch; a threshold on that age would claim a diagnosis ADR-0023 rules out.
- Adding a Kafka consumer-lag signal, which stays the parked entry's subject.
- Replacing the JSON-validity gate with a schema validator for Grafana's dashboard model.

## Decisions

1. **The panels join the existing Projection Service section rather than a new row.** They sit as the section's second
   row — state 6 columns wide, in-flight batches 6, since-last-resolved 12, each 7 rows high so the row below starts at
   `y=111` — and the 18 panels at or below that row move down 7 grid rows. The three take ids 84–86 (the file's highest
   is 83), the `DS_PROMETHEUS` datasource variable, and the `application="$application"` selector with
   `service="axon-showcase-projection-service"` that every existing projection panel uses. A new row would add a section
   to the dashboard (the README states the section count), and it would separate the state from the throughput and
   event-age panels it explains. Alternative rejected: adding the three signals as targets on the Event Age panel, whose
   unit is seconds and whose legend already carries three series — the in-flight count is a different quantity and the
   state is an enum.
2. **The state is a `stat` panel with value mappings and threshold colors.** It reads as Stopped, Running, or Retrying
   at a glance; a timeseries of 0, 1, and 2 requires the reader to know the encoding, and a line at 0 is ambiguous
   between "stopped" and "no data". The panel queries the gauge as an instant value (`instant: true`, `range: false`,
   reduced by `lastNotNull`), so a sample from inside the dashboard's range window cannot stand in for the current
   state; an absent series renders as `noValue` "No data" with the neutral base color, which the three mapped values do
   not share. Its description states what the state means at the source — the projector has reached partition assignment
   since startup or the last stream failure, not that the broker is reachable or that progress continues — so the panel
   is not read as a health verdict. Alternative rejected: a `state-timeline` panel, a second panel type for a history
   the readiness rollout already exposes. The dashboard has no `stat` panel today, so the type is new to the file but
   not to Grafana.
3. **The progress panel plots the age, derived in the query, and carries no threshold.** The expression is
   `time() - (showcaseProjector_last_progress_seconds{…} > 0)`, so a progress of 0 — "no batch has resolved yet" —
   yields no sample instead of an age measured from 1970. The panel's legend reads "since last resolved batch (idle time
   included)" and its description states ADR-0023's boundary, so a growing age reads as what it is on the panel itself:
   a healthy idle system and a stalled one are indistinguishable on this signal alone. Alternative rejected: plotting
   the raw epoch second, which states when the last batch resolved but not how long ago — the age is the figure an
   operator compares against the event age above it.
4. **The gate's new rules live in the existing rules object.** `DashboardJsonRules` already owns the dashboards' file
   set, the empty-directory failure, and the `check` wiring, and the panel rules are more assertions over the document
   it has already parsed, so the task's inputs and its cost are unchanged. The rules are:
   - every panel id is unique across the document, at any depth, so a child of a collapsed row cannot repeat a top-level
     id;
   - every top-level panel carries a `gridPos`, and no two top-level panels' rectangles overlap — row panels included,
     because a row occupies grid space and a forgotten row shift is exactly what this edit can produce;
   - every target a top-level panel declares carries a non-blank `expr`, so a half-edited copy of a panel cannot ship a
     query that was emptied on the way.

   The three rules read the top-level `panels` array — the id rule recursing into nested panels — so a collapsed row's
   children are outside the geometry and expression rules; the dashboard has none today, and the id rule still covers
   them. The expression rule deliberately says nothing about a panel that declares no target at all: a query-less panel
   type (`text`, `dashlist`) is legitimate, and a data panel added without any target renders "No data" in front of the
   author who added it, while the half-filled target is the defect a gate can distinguish. Alternatives rejected: a
   panel-type list of query-rendering types (it rots as Grafana adds types, and exempting `text` alone failed the
   `dashlist` case); a new Python script and task (a second file-discovery path and a second Gradle task for the same
   input); and asserting a declared list of metric names (the producers own those names, and the list would drift).

5. **The exposition names are pinned on the producer side.** `ShowcaseProjectorStateMonitorPrometheusTests` scrapes a
   `PrometheusMeterRegistry` after driving the monitor through running, a started batch, and a resolved one, and asserts
   the three names and their values. A renamed gauge then fails a test rather than leaving a panel that renders "No
   data" — the failure mode a dashboard has for a wrong or renamed metric. Alternative rejected: asserting the names
   from a dashboard-side check, which would have to read the Java source or a build-order-coupled artifact.
6. **The spec delta lands on `quality/code-quality`; `helm-chart` is not modified.** The gate's behavior is that
   requirement's, so extending the gate owes its `MODIFIED` block. `helm-chart`'s "Extra deployments and dashboards"
   owns the dashboard's existence and purpose, and a panel inventory is presentation the spec does not carry — the
   boundary `add-web-ui-dashboard-panels` applied when it added panels with no delta.
7. **Verification uses the deployed signal, not the configuration.** At proposal time the three queries were executed
   against the local cluster's Prometheus, which serves all three series with the `service` label the panels select. At
   implementation the packaged chart is upgraded into the local release and the provisioned dashboard is read back from
   Grafana's API, asserting the three panel titles — the check that Grafana accepted the file and the sidecar
   provisioned it. Alternative rejected: resting the claim on the JSON gate, which proves the file's shape and nothing
   about its queries or provisioning.

## Risks / Trade-offs

- **The gate's rules are structural, so a semantically wrong query passes.** Mitigated by decision 5 for the metric
  names and decision 7 for the queries resolving against live data; a wrong label selector in a new panel is caught only
  by the live read-back.
- **Shifting 18 panels risks an overlap or a gap.** The overlap rule compares every top-level panel, rows included, so a
  forgotten row shift is caught; a _gap_ between rows is not gated, and the base file was measured at zero overlaps, so
  this edit is the rule's first real exercise.
- **The in-flight count is occupancy, not progress.** Partitions write concurrently and a batch can complete as another
  starts, so a sustained nonzero value is compatible with healthy continuous processing; the panel's description says
  so, and neither this panel nor the age panel is a stall verdict.
- **An age panel invites a false alarm.** The legend names idle time, the description states ADR-0023's boundary, and no
  threshold colors the panel; the parked lag entry is where a real stall signal would land. The live check exercises the
  running and the absent-series states only — inducing a retry or a stopped projector is the disruptive experiment this
  change does not need, and `projector-operability` verified the three values at the source.
- **The expression rule accepts a panel that declares no target.** Its surface is the half-filled target, not every
  query-less panel; a data panel added from scratch with no target passes the gate and is caught only by looking at the
  dashboard.
- **A dashboard-side rule set invites more presentation rules.** The three rules were chosen for the classes this edit
  can produce; a rule about titles, datasources, or panel counts would drift with every panel change and is not added.

## Verification

```bash
# The gate, its tests, and the producer-side name pin
./gradlew verifyDashboardJson :build-logic:test --tests 'DashboardJsonRulesTests' \
  :showcase-projection-service:test --tests 'ShowcaseProjectorStateMonitorPrometheusTests'
./gradlew check -PskipITs -Pcoverage.gate.enabled=false

# The provisioned dashboard, read back from the running Grafana
./gradlew :helm:chart:helmPackageMainChart
helm upgrade axon-showcase helm/chart/build/helm/charts/axon-showcase \
  -f helm/values/axon-showcase/values-local.yaml --namespace axon-showcase --wait
PW=$(kubectl -n monitoring get secret kps-grafana -o jsonpath='{.data.admin-password}' | base64 -d)
kubectl -n monitoring port-forward svc/kps-grafana 13000:80 &
curl -s -u "admin:$PW" http://127.0.0.1:13000/api/dashboards/uid/d5ebc0e9-ba9c-40d5-92c2-e1f7263290c6
```

The read-back asserts the panels a reader sees, not only that the file parsed: the three titles; the state panel's
`instant`/`range`/`calcs` settings, its three value mappings, and its `noValue`; the age panel carrying no threshold;
and the state query returning `1` for the running projector, while the same expression with a selector that matches no
series returns nothing — the "unavailable" case, distinct from `0` (stopped). The read-back was run against the base
revision while this proposal was drafted: Grafana returns the dashboard titled "Axon Showcase" with its 46 panels, so
the sidecar provisions the release's ConfigMap and the API reports what a reader sees.

## Claims table

| Claim                                                                                                                                 | Source                                                                                                                                                                                        | Result                                                                                                                                                                                                                               |
| ------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The three gauges are served as `showcaseProjector_state`, `showcaseProjector_processing`, `showcaseProjector_last_progress_seconds`   | `kubectl -n monitoring port-forward svc/kps-prometheus 19090:9090`, then `curl -sG http://127.0.0.1:19090/api/v1/query --data-urlencode 'query=<name>'` per name                              | `status success`, one series per query, `__name__` equal to the queried name                                                                                                                                                         |
| Those series carry `application="axon-showcase"` and `service="axon-showcase-projection-service"`                                     | The same three responses' metric labels                                                                                                                                                       | both labels present on every series, beside `container`, `endpoint`, `instance`, `job`, `namespace`, `pod`                                                                                                                           |
| The derived-age query parses and returns a series while progress is above zero                                                        | The same port-forward, `query=time() - (showcaseProjector_last_progress_seconds{application="axon-showcase", service="axon-showcase-projection-service"} > 0)`                                | `status success`, one series whose value is the current instant minus the gauge's stamp (about 767274 at the drafting read); the stamp is 2026-10-26T14:03Z, the day the release was installed, so no event has been projected since |
| The base revision's dashboard has 39 non-row panels and 7 row panels, unique ids (max 83), and no overlapping grid positions          | `git show 96edb035:helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json` piped to a JSON counter                                                                              | `total 46 rows 7 nonrow 39`; `ids max 83 unique True`; `overlaps 0`                                                                                                                                                                  |
| Every base non-row panel is a timeseries; the dashboard has no `stat` panel                                                           | The same dump, counting `type`                                                                                                                                                                | `types {'timeseries': 39, 'row': 7}` — the 7 rows are the sections                                                                                                                                                                   |
| 18 panels sit at `gridPos.y >= 104` (15 non-row and 3 rows) and the lowest panel bottom is 163                                        | The same dump                                                                                                                                                                                 | `nonrow y>=104 15`, `all y>=104 18`, `max y bottom 163`                                                                                                                                                                              |
| The README states the dashboard's panel and section counts                                                                            | `git show 96edb035:README.md \| grep -n "panels across"`                                                                                                                                      | `1170:- **Grafana**: … (39 panels across 7 sections covering`                                                                                                                                                                        |
| `showcase/quality/code-quality` owns the dashboard gate's behavior, and the only other spec mentioning a dashboard owns its existence | `git grep -l "dashboard" 96edb035 -- openspec/specs`                                                                                                                                          | `openspec/specs/showcase/quality/code-quality/spec.md` and `openspec/specs/showcase/deployment/helm-chart/spec.md`                                                                                                                   |
| `verifyDashboardJson` is part of the standard `check`                                                                                 | `grep -n 'dependsOn("verifyDashboardJson")' build.gradle.kts`                                                                                                                                 | `910:    dependsOn("verifyDashboardJson")`                                                                                                                                                                                           |
| The parked entry this change implements is the `docs/ideas.md` line naming the projector panel                                        | `grep -n "Add a Grafana panel for the projector" docs/ideas.md`                                                                                                                               | line 311                                                                                                                                                                                                                             |
| Grafana provisions the bundled dashboard, so a read-back reports the panels a reader sees                                             | `kubectl -n monitoring port-forward svc/kps-grafana 13000:80`, then `curl -s --user "admin:$PW" http://127.0.0.1:13000/api/dashboards/uid/d5ebc0e9-ba9c-40d5-92c2-e1f7263290c6`               | `title Axon Showcase panels 46`; the release's ConfigMap `axon-showcase-axon-showcase` carries the file                                                                                                                              |
| No service module's production source set changes                                                                                     | The change's own file list (proposal, "What Changes"), verified against the staged diff at implementation                                                                                     | one test file under `showcase-projection-service/src/test/java`; the only `src/main` path is `build-logic`'s rules object, which is Gradle build tooling, and no dependency changes                                                  |
| The gauges' documented values: state 0 for stopped, 1 for running, 2 for retrying; progress 0 before the first resolved batch         | `git show 96edb035:showcase-projection-service/src/main/java/showcase/projection/ShowcaseProjectorStateMonitor.java \| grep -n 'case STOPPED\|case RUNNING\|case RETRYING\|before the first'` | `77: case STOPPED -> 0;` `78: case RUNNING -> 1;` `79: case RETRYING -> 2;`, and the Javadoc lines 55, 87, 152 for the zero-progress sentinel                                                                                        |
| `verifyDashboardJson` is the only automated check over the dashboard file                                                             | `git grep -n "grafana-dashboards" 96edb035 -- build.gradle.kts build-logic scripts .github`                                                                                                   | one hit, `build.gradle.kts:891` (the gate's input); no other task or script reads the directory                                                                                                                                      |

Dismissals, one reason per kind: `path` — the named paths are this change's own artifacts and the files it edits, so
there is no external path claim to verify; `absolute` — each flagged word sits in a statement a table row covers or in
the change's own prohibition, which the staged diff evidences; `count` — the figures are the table's rows; `cross-ref` —
the flagged commands and paths are this change's own verification instructions, whose inputs are repository files and
the local release's objects, and each was run or read for the rows above.

## Consequences

- The dashboard shows the projector's state where its consequences (throughput, event age) are already shown.
- `verifyDashboardJson` gains three structural rules over the dashboard file; `check` schedules no additional task.
- The `deployment-operations` pack's "Chart validation" paragraph points at the gate, so the next dashboard edit knows
  the rules exist.
- The README's panel count and the parked entry move with the change.
