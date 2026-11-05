# Add an operational-status overview and drop the dead SLO boundaries

## Why

The dashboard shows what each service does under load but not whether the system is running at all: nothing on it
reports a scrape target that has disappeared or a Deployment that is short of replicas, and the only panel that used to
claim "availability" was removed as a processing ratio (PR #843). The query service also still configures
`management.metrics.distribution.slo` for `http.server.requests`, a surface it no longer serves — its reads moved to
gRPC — so that entry — one meter, four boundaries — decorates actuator traffic nobody reads.

## What Changes

- `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`: a new **Operational status** section at the
  top of the dashboard, shifting every existing panel down by 8 grid rows, with two panels:
  - **Scrape status** — one entry per currently discovered scrape target, showing its instant state as Up or Down; a
    target that stops being discovered leaves its entry missing rather than reading a stale `Up`, which the panel's
    description says;
  - **Available / desired replicas** — one row per Deployment showing both counts (for example `2` and `2`), so a
    shortfall and a missing series are distinguishable.
- `showcase-query-service/src/main/resources/application.yml`: the `slo.http.server.requests` entry is removed; the
  gateway's own boundaries stay.
- `README.md`: the dashboard's panel and section counts, and the coverage sentence that gains the operational-status
  view beside the projector's operating state.

### New Capabilities

None.

### Modified Capabilities

None. The chart's dashboard requirement specifies the bundled dashboard's existence and purpose, not its panel
inventory, and no capability specifies the services' `management.metrics.distribution` settings — so this change is
`skip_specs: true`, with the counts and the cleanup recorded in the change's own artifacts.

## Impact

- **Files**: the dashboard JSON, the query service's `application.yml`, `README.md`, and the change's own artifacts.
- **Code, APIs, dependencies**: none. The new panels read `up` and `kube_state_metrics` series the cluster already
  collects; removing the query-service entry changes which histogram boundaries that service exports for its actuator.
- **Build and tests**: no new task; `verifyDashboardJson` already parses the file, and its description rule refuses a
  panel that queries without a description, so both new panels carry one.
- **Deployment**: the dashboard ConfigMap's data changes; the query service's next image carries the removed entry.
- **Verification**: the new queries run against the cluster's Prometheus and the provisioned dashboard is read back from
  Grafana, including the states that a healthy cluster cannot show (a down target, a replica shortfall) by inspecting
  the panels' value mappings rather than by inducing an outage.
