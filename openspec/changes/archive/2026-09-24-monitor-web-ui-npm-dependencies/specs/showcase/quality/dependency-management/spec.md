# dependency-management — Delta

## ADDED Requirements

### Requirement: Web UI dependency update reporting

The build SHALL report available updates for the web UI's npm dependencies, separate from the catalog-owned Gradle
reporting: the web UI module SHALL expose an `npmOutdated` task that reports the packages whose resolved version is
behind the newest version available in the npm registry (as `npm outdated` reports them), using the project's pinned
Node. The task SHALL NOT be part of the `check` lifecycle, and it SHALL NOT fail when updates exist.

#### Scenario: Web UI dependency updates are reported

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmOutdated` and an npm dependency has a newer version available
- **THEN** the report lists that package with its current, wanted, and latest versions

#### Scenario: Available updates do not fail the task

- **WHEN** the web UI has outdated npm dependencies (so `npm outdated` exits non-zero)
- **THEN** the `npmOutdated` task still completes and its output is available to be reported

#### Scenario: Normal build does not run the web UI update report

- **WHEN** a developer runs `./gradlew check` or any build task other than `npmOutdated`
- **THEN** the web UI update report does not run
