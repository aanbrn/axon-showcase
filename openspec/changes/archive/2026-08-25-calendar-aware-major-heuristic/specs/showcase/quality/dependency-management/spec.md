## ADDED Requirements

### Requirement: Calendar-versioned coordinates treat train changes as major updates

The build's major-comparison logic SHALL treat calendar-versioned coordinates specially. A coordinate is calendar
versioned when its current version follows the Spring `YYYY.MINOR.MICRO` scheme (leading segment is a 4-digit year).
For such a coordinate, a change in the `YYYY.TRAIN` pair (the first two version segments) SHALL be treated as a major
update — matching the Spring release-train definition, where `2025.0` and `2025.1` are distinct trains — while a change
only in the service-release segment (the third segment) within the same train SHALL be treated as a non-major update.
For non-calendar (semver) coordinates, the existing leading-integer major comparison SHALL be unchanged.

#### Scenario: Train change is classified as a major update

- **WHEN** a calendar-versioned coordinate (e.g. `io.projectreactor:reactor-bom` at `2025.0.7`) has a candidate whose
  `YYYY.TRAIN` pair differs from the current pair (e.g. `2025.1.x` or `2026.0.x`) is available
- **THEN** the report treats that train change as a major update for the coordinate

#### Scenario: Same-train service release is classified as a non-major update

- **WHEN** a calendar-versioned coordinate has a candidate with the same `YYYY.TRAIN` pair but a newer service release
  (e.g. `2025.0.8` vs `2025.0.7`) is available
- **THEN** the report treats that service-release change as a non-major update for the coordinate

#### Scenario: Semver coordinates keep the leading-integer major comparison

- **WHEN** a non-calendar (semver) coordinate is evaluated
- **THEN** a change in its leading integer is treated as a major update and a same-leading-integer change as a non-major
  update, unchanged from current behavior
