# Spec Delta

## MODIFIED Requirements

### Requirement: Extra deployments and dashboards

The chart SHALL render extra deployments from `extraDeploy` verbatim, and SHALL render a ConfigMap per bundled Grafana
dashboard under the `grafana_dashboard: "1"` label so dashboards auto-provision. The chart SHALL bundle the Axon
Showcase dashboard, a custom observability view of the application.

#### Scenario: Extra resources are rendered from extraDeploy

- **WHEN** `extraDeploy` is set
- **THEN** each entry is rendered verbatim (templates allowed) as its own YAML document

#### Scenario: Grafana dashboards are provisioned as ConfigMaps

- **WHEN** the chart is rendered
- **THEN** a ConfigMap is created for the bundled Axon Showcase dashboard carrying the `grafana_dashboard: "1"` label
  and the dashboard JSON as data
