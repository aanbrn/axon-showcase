# showcase/quality/dependency-management Specification

## Purpose
Defines how the build's `dependencyUpdates` report is scoped and filtered: it reports only catalog-owned versions and
lets the project opt in to suppressing major-version updates for specific coordinates while keeping their minor/patch
updates visible.

## Requirements

### Requirement: Dependency update reporting covers only catalog-owned versions

The build's `dependencyUpdates` task SHALL report available updates only for dependencies whose version is explicitly
declared in the version catalog (`gradle/libs.versions.toml`) via an exact `version.ref`. Dependencies whose version is
inherited from a BOM or constraint — and thus not decided by the project — SHALL NOT be reported.

#### Scenario: Catalog-owned dependency reports updates

- **WHEN** a dependency coordinate has an exact `version.ref` in the version catalog and a newer version is available
- **THEN** the report lists the available update for that coordinate

#### Scenario: BOM-inherited dependency is not reported

- **WHEN** a dependency coordinate has no exact version in the version catalog (its version comes from a BOM or
  constraint)
- **THEN** the report does not list updates for that coordinate

### Requirement: Dependency update reporting defaults to all updates for catalog-owned dependencies

The build's `dependencyUpdates` task SHALL report every available update for every catalog-owned dependency by default —
including major version upgrades — with no major-blocking configuration required.

#### Scenario: No blocking configuration is present

- **WHEN** a developer runs `./gradlew dependencyUpdates` and the major-disabled list is empty
- **THEN** the report lists available updates for all catalog-owned dependencies, including major version jumps

#### Scenario: A blocked coordinate still reports minor and patch updates

- **WHEN** a coordinate is listed in the major-disabled configuration and a same-major (minor or patch) version is
  available
- **THEN** the report lists that minor or patch update for the coordinate

### Requirement: Major updates can be suppressed per coordinate

The build SHALL provide an opt-in configuration listing coordinates whose major version updates are suppressed from
the `dependencyUpdates` report. Minor and patch updates for those coordinates SHALL remain reported.

#### Scenario: Major jump is hidden for a listed coordinate

- **WHEN** a coordinate is listed in the major-disabled configuration and a candidate version whose major exceeds the
  current major is the newest available
- **THEN** the report does not list that major-jump update for the coordinate

#### Scenario: Same-major fallback is reported when a major jump is the newest

- **WHEN** a listed coordinate has both a major-jump candidate and a same-major candidate available
- **THEN** the report lists the newest same-major candidate instead of the major-jump candidate
