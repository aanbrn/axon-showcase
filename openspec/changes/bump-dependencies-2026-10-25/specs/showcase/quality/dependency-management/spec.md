## MODIFIED Requirements

### Requirement: Major updates can be suppressed per coordinate

The build SHALL provide an opt-in configuration listing coordinates whose major version updates are suppressed from the
`dependencyUpdates` report. Minor and patch updates for those coordinates SHALL remain reported. For calendar versioned
coordinates (versions following the Spring `YYYY.MINOR.MICRO` scheme, where the leading segment is a 4-digit year), a
change in the `YYYY.TRAIN` pair (the first two version segments) SHALL be treated as a major update — matching the
Spring release-train definition, where `2025.0` and `2025.1` are distinct trains — while a change only in the
service-release segment (the third segment) within the same train SHALL be treated as a non-major update. For
non-calendar (semver) coordinates, the existing leading-integer major comparison SHALL be unchanged.

#### Scenario: Major jump is hidden for a listed coordinate

- **WHEN** a coordinate is listed in the major-disabled configuration and a candidate version whose major exceeds the
  current major is the newest available
- **THEN** the report does not list that major-jump update for the coordinate

#### Scenario: Same-major fallback is reported when a major jump is the newest

- **WHEN** a listed coordinate has both a major-jump candidate and a same-major candidate available
- **THEN** the report lists the newest same-major candidate instead of the major-jump candidate

#### Scenario: Calendar-train change is classified as a major update

- **WHEN** a calendar-versioned coordinate (e.g. `io.projectreactor:reactor-bom` on the `2025.0` train) has a candidate
  whose `YYYY.TRAIN` pair differs from the current pair (e.g. `2025.1.x` or `2026.0.x`) is available
- **THEN** the report treats that train change as a major update for the coordinate

#### Scenario: Same-train service release is classified as a non-major update

- **WHEN** a calendar-versioned coordinate has a candidate with the same `YYYY.TRAIN` pair but a newer service release
  (e.g. a later `2025.0.x`) is available
- **THEN** the report treats that service-release change as a non-major update for the coordinate

#### Scenario: Semver coordinates keep the leading-integer major comparison

- **WHEN** a non-calendar (semver) coordinate is evaluated
- **THEN** a change in its leading integer is treated as a major update and a same-leading-integer change as a non-major
  update, unchanged from current behavior
