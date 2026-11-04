# Add projector operating-state panels to the Grafana dashboard

## Why

The projection service exports the projector's operating state, in-flight batch count, and last resolved batch time as
`showcaseProjector.state`, `.processing`, and `.last.progress`, and its ServiceMonitor scrapes them — but the bundled
Axon Showcase dashboard has no panel for any of the three. Its Projection Service section shows throughput, latency,
batch size, failure rate, and event age, and a projector that has stopped consuming or is waiting to restart a failed
stream stops producing those samples exactly as a healthy idle one does. An operator reading the dashboard therefore
cannot tell a retrying projector from a quiet system; the state is visible only in the readiness endpoint.

## What Changes

- `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`: three panels in the Projection Service section
  — **Projector State** (a stat panel mapping the state value to Stopped, Running, or Retrying), **In-flight Batches**
  (a timeseries of `showcaseProjector_processing`), and **Since Last Resolved Batch** (a timeseries of the age derived
  from `showcaseProjector_last_progress_seconds`) — inserted as the section's second row, with the 18 panels at or below
  that row shifted down by 7 grid rows.
- `build-logic/src/main/kotlin/DashboardJsonRules.kt` and its test: the `verifyDashboardJson` gate gains three
  panel-level rules — panel ids unique at any depth, non-overlapping grid positions over the top-level panels, and a
  query expression on every target a top-level panel declares — so the hand re-layout that adds a panel cannot ship
  overlapping or empty panels that the JSON-validity gate cannot see.
- `showcase-projection-service/src/test/java/showcase/projection/ShowcaseProjectorStateMonitorPrometheusTests.java`: a
  unit test over a `PrometheusMeterRegistry` scrape that pins the three exposition names the panels query.
- `openspec/specs/showcase/quality/code-quality` delta: the "Bundled Grafana dashboard JSON is valid" requirement gains
  the panel-level obligations and their scenarios.
- `README.md`: the dashboard's panel count and the coverage wording.
- `.opencode/skills/deployment-operations/SKILL.md`: a pointer in "Chart validation" to the dashboard gate.
- `docs/ideas.md`: the parked entry this change implements is removed.

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/code-quality` — "Bundled Grafana dashboard JSON is valid" gains the panel-level obligations: the
  standard `check` also rejects a dashboard whose panels repeat an id, overlap in the grid, or declare a target whose
  query expression is blank.

## Impact

- **Files**: the dashboard JSON, `DashboardJsonRules.kt` with `DashboardJsonRulesTests.kt`, one new projection-service
  test, `README.md`, the `deployment-operations` pack, `docs/ideas.md`, and the change's own artifacts.
- **Code, APIs, dependencies**: no service code, API, or dependency change; the gate's rules object is build tooling.
  The metrics, their Prometheus names, and the ServiceMonitor scrape already exist.
- **Build and tests**: no new task — `verifyDashboardJson` already runs in `check` and gains rules over the document it
  already parses, so no additional Gradle work is scheduled; one unit test joins the projection service's `test` suite.
- **Deployment**: the dashboard ConfigMap's data changes, so the panels appear wherever Grafana provisions the bundled
  dashboard. No template, value, or image change.
- **Verification**: the three queries were executed against the local cluster's Prometheus while this proposal was
  drafted, and the change is verified end to end by upgrading the local release from the packaged chart and reading the
  provisioned dashboard back from Grafana's API (design.md, "Verification").
