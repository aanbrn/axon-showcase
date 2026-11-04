## MODIFIED Requirements

### Requirement: Bundled Grafana dashboard JSON is valid

The standard `check` task SHALL parse every bundled Grafana dashboard file
(`helm/chart/src/main/helm/files/grafana-dashboards/*.json`) and fail when one is not valid JSON, naming the offending
file and the parse error — because the chart emits each dashboard as an opaque string, so `helm lint` and
`helm template` never parse it and a malformed file would otherwise surface only when a live Grafana fails to provision
the dashboard. The check SHALL parse every file before failing, so a second malformed file is not hidden by the first,
and SHALL fail when the dashboards directory is absent or empty, so a renamed path cannot disable it silently. It SHALL
also reject a dashboard whose panels cannot be provisioned faithfully — a panel id repeated at any depth, a top-level
panel without a grid position, two top-level panels on overlapping grid positions, a top-level panel target whose query
expression is blank, or a panel that declares a target and is not a row and carries no description — naming the file and
the offending panel, since such a dashboard provisions into panels that collide, overlap, query nothing, or report a
number a reader cannot interpret.

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

#### Scenario: A dashboard that repeats a panel id fails the check

- **WHEN** two panels of a bundled dashboard carry the same `id`, a panel nested in a collapsed row included
- **THEN** the check fails naming the offending file and the repeated id

#### Scenario: A dashboard whose panels occupy overlapping grid positions fails the check

- **WHEN** two top-level panels of a bundled dashboard have overlapping `gridPos` rectangles, or a top-level panel
  carries no grid position
- **THEN** the check fails naming the offending file and the offending panels

#### Scenario: A dashboard with a target that carries no query fails the check

- **WHEN** a top-level panel of a bundled dashboard declares a target whose query expression is blank
- **THEN** the check fails naming the offending file and the panel

#### Scenario: A panel that queries without a description fails the check

- **WHEN** a panel of a bundled dashboard that is not a row declares at least one target and its `description` is
  missing, blank, or not a string
- **THEN** the check fails naming the offending file and the panel

#### Scenario: A row that declares a target needs no description

- **WHEN** a row panel of a bundled dashboard declares a target and no `description`
- **THEN** the check passes for that panel, because a row renders no query

#### Scenario: Valid dashboards pass the build

- **WHEN** every bundled dashboard file is one valid JSON document, its panel ids are unique, its top-level panels carry
  non-overlapping grid positions, every target a top-level panel declares carries a query expression, and every panel
  that is not a row and declares a target carries a description
- **THEN** the check passes and the standard `check` task succeeds
