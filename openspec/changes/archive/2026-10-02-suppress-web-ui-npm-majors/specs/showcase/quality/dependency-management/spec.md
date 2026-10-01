# Spec Delta: showcase/quality/dependency-management

## MODIFIED Requirements

### Requirement: Web UI dependency update reporting

The build SHALL report available updates for the web UI's npm dependencies, separate from the catalog-owned Gradle
reporting: the web UI module SHALL expose an `npmOutdated` task that reports the packages whose resolved version is
behind the newest version available in the npm registry (as `npm outdated` reports them), using the project's pinned
Node. The task SHALL NOT be part of the `check` lifecycle, and it SHALL NOT fail when updates exist. The report SHALL
support a suppression list, `config/web-ui-updates/major-disabled.txt`, listing npm packages whose **major** updates are
suppressed: for a listed package the report SHALL list an update only when the newest version the manifest admits
(`npm outdated`'s `Wanted`) shares the newest published version's (`Latest`) leading integer — so a suppressed major is
dropped while that package's same-major updates stay reported. The shipped list SHALL include `typescript`, deferred
because `typescript-eslint` caps TypeScript below the next major, and the list's comment SHALL name the package and
point at this rationale. A row the report cannot parse SHALL be kept unchanged rather than dropped.

#### Scenario: Web UI dependency updates are reported

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmOutdated` and an npm dependency has a newer version available
- **THEN** the report lists that package with its current, wanted, and latest versions

#### Scenario: Available updates do not fail the task

- **WHEN** the web UI has outdated npm dependencies (so `npm outdated` exits non-zero)
- **THEN** the `npmOutdated` task still completes and its output is available to be reported

#### Scenario: Normal build does not run the web UI update report

- **WHEN** a developer runs `./gradlew check` or any build task other than `npmOutdated`
- **THEN** the web UI update report does not run

#### Scenario: A suppressed package's major update is dropped

- **WHEN** a package is listed in the suppression list and its newest published version is a new major (the manifest's
  admitted version shares the current major)
- **THEN** the report does not list that major-only update for the package

#### Scenario: A suppressed package's same-major update stays reported

- **WHEN** a package is listed in the suppression list and a newer same-major version is available
- **THEN** the report lists that same-major update for the package

#### Scenario: A package not listed is reported unchanged

- **WHEN** a package is absent from the suppression list and any newer version is available
- **THEN** the report lists it, whether the update is major, minor, or patch
