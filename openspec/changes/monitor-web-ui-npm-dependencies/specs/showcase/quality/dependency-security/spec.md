# dependency-security — Delta

## ADDED Requirements

### Requirement: Web UI dependency vulnerability scan

The build SHALL provide an `npmAudit` task that scans the web UI's npm dependencies with `npm audit`, covering both
production and development dependencies, and failing when a high-severity (or greater) vulnerability is present. The
task SHALL use the project's pinned Node and SHALL NOT be part of the `check` lifecycle.

#### Scenario: Developer runs the web UI vulnerability scan

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmAudit` and a high-severity vulnerability is present
- **THEN** the task fails and reports the vulnerable npm package and its advisory

#### Scenario: A development-dependency advisory is reported

- **WHEN** a high-severity vulnerability is present in a development dependency
- **THEN** the task fails, because the scan covers development as well as production dependencies

#### Scenario: A clean web UI audit passes

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmAudit` and no high-severity vulnerability is present
- **THEN** the task completes successfully

#### Scenario: Normal build does not run the web UI vulnerability scan

- **WHEN** a developer runs `./gradlew check` or any build task other than `npmAudit`
- **THEN** the web UI vulnerability scan does not run
