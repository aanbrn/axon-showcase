# Repair the dashboard's stale panels and describe every signal

## Why

The bundled dashboard still describes a topology the repository no longer has. Its Query Service panels carry
`http_server_requests_*` series for a service that now serves reads over gRPC and exposes HTTP only on its actuator —
which those selectors exclude — so the series are permanently empty; the overview's CPU and Memory panels each carry a
`saga-service` selector for a service removed long ago; and a panel labelled "Availability" divides lifetime counters
without a window. Thirty-nine of the forty-two data panels also carry no description and eighteen no unit, so a reader
cannot tell what a number means or why a panel reads "No data".

## What Changes

- `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`:
  - the three Query Service panels lose their `http_server_requests_*` targets;
  - the two `fetch_showcase_*` targets of the Latency panel gain the label selectors their siblings already carry;
  - the overview's CPU and Memory panels lose their `saga-service` targets;
  - those two panels' thresholds move to the scale their values use;
  - the "Availability" panel is renamed to `Processing Ratio`, because it divides lifetime counters rather than
    measuring uptime;
  - its thresholds are expressed as fractions within the panel's `max`, and its description states that the value is
    cumulative since each process started and that a restart resets it;
  - its query-service target is rebuilt from the `error` labels of both `fetch_showcase_*` counters the section already
    reads, unioned before the division so the ratio follows both read paths;
  - the Query Service failure panel aggregates both `fetch_showcase_*` families without the per-label vector join that
    can drop an error present in only one of them;
  - every backend rate window becomes `$__rate_interval`, so a 30-second scrape no longer yields a two-sample rate;
  - the browser-experience windows keep their five minutes;
  - every data panel gains a description (what is counted, which statistic, what "No data" means), and every unit is
    corrected to the quantity it shows;
  - the dashboard's own description states what the view covers.
- `build-logic/src/main/kotlin/DashboardJsonRules.kt` with `DashboardJsonRulesTests.kt`: a panel that declares targets
  must carry a non-blank description; rows and target-less panels are exempt.
- `build.gradle.kts`: the `verifyDashboardJson` task's description no longer says it checks JSON only.
- `openspec/changes/repair-dashboard-panels-and-descriptions/specs/showcase/quality/code-quality/spec.md`: the delta
  gives the "Bundled Grafana dashboard JSON is valid" requirement the description obligation and its scenarios.

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/code-quality` — "Bundled Grafana dashboard JSON is valid" also rejects a dashboard whose panel
  declares a target but no description.

## Impact

- **Files**: the files this proposal's What Changes list names, and the change's own artifacts.
- **Code, APIs, dependencies**: none. No service, metric, template, or value changes; the panels read metrics the
  services already export.
- **Build and tests**: no new task — `verifyDashboardJson` gains a rule over the document it already parses; the rules
  tests grow by the new cases.
- **Deployment**: the dashboard ConfigMap's data changes, so the corrected panels appear wherever Grafana provisions the
  bundled dashboard. The panel count, section count, and every panel id stay the same.
- **Verification**: each rebuilt query runs against the local cluster's Prometheus; the provisioned dashboard is read
  back from Grafana's API; `$__rate_interval`'s expansion is checked on the deployed instance before adoption, with a
  fixed two-minute window as the fallback.
