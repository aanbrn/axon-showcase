## ADDED Requirements

### Requirement: Chart linting

The chart SHALL be linted with `helm lint` in strict mode, treating warnings as errors, and SHALL also lint the
Bitnami `common` subchart.

#### Scenario: Chart lints clean in strict mode

- **WHEN** the chart is linted
- **THEN** `helm lint` runs with warnings treated as errors and reports no failures for the chart or its subcharts

### Requirement: Lint configurations cover template branches

The chart SHALL be linted with a `full` configuration that enables every optional feature and a `minimal`
configuration that disables the default-on features, so all conditional template branches are rendered during lint.

#### Scenario: Full configuration renders optional branches

- **WHEN** the chart is linted with the `full` configuration
- **THEN** the optional template branches are rendered, including ingress, HTTPRoute, VPA and HPA, secured
  OpenSearch, observability, extraDeploy, RBAC rules, NetworkPolicy extras, and ServiceMonitor tuning

#### Scenario: Minimal configuration renders disabled branches

- **WHEN** the chart is linted with the `minimal` configuration
- **THEN** the templates render with the default-on features disabled, including ServiceAccount, RBAC,
  NetworkPolicies, PDBs, ServiceMonitors, probes, autoscaling, and observability
