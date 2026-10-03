## ADDED Requirements

### Requirement: Bundled Grafana dashboard JSON is valid

The standard `check` task SHALL parse every bundled Grafana dashboard file
(`helm/chart/src/main/helm/files/grafana-dashboards/*.json`) and fail when one is not valid JSON, naming the offending
file and the parse error — because the chart emits each dashboard as an opaque string, so `helm lint` and
`helm template` never parse it and a malformed file would otherwise surface only when a live Grafana fails to provision
the dashboard. The check SHALL parse every file before failing, so a second malformed file is not hidden by the first,
and SHALL fail when the dashboards directory is absent or empty, so a renamed path cannot disable it silently.

#### Scenario: A malformed dashboard fails the build

- **WHEN** a dashboard file under `helm/chart/src/main/helm/files/grafana-dashboards/` is not one valid JSON document —
  for example truncated, empty, or carrying trailing content after the value
- **THEN** the check fails naming the offending file and its parse error

#### Scenario: Every malformed dashboard is reported

- **WHEN** more than one dashboard file is not valid JSON
- **THEN** the check reports each offending file, not only the first

#### Scenario: A dashboard directory that is missing or empty fails the check

- **WHEN** the dashboards directory does not exist or holds no dashboard file
- **THEN** the check fails, rather than passing vacuously

#### Scenario: Valid dashboards pass the build

- **WHEN** every bundled dashboard file is valid JSON
- **THEN** the check passes and the standard `check` task succeeds
