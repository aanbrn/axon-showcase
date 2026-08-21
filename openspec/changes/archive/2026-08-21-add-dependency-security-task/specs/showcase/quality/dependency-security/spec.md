# showcase/quality/dependency-security

## ADDED Requirements

### Requirement: Local dependency security scan task

The build SHALL provide a `dependencySecurityCheck` Gradle task that runs the Snyk dependency scan (`snyk test
--all-sub-projects`) across all sub-projects and reports the result to the developer. The task SHALL NOT be part of the
`check` lifecycle.

#### Scenario: Developer runs the dependency security scan

- **WHEN** a developer runs `./gradlew dependencySecurityCheck` with the Snyk CLI installed
- **THEN** the task invokes `snyk test --all-sub-projects` against the build and reports the scan result, failing when
  vulnerable paths are found

#### Scenario: Normal build does not run the dependency scan

- **WHEN** a developer runs `./gradlew check` or any build task other than `dependencySecurityCheck`
- **THEN** the dependency scan does not run

#### Scenario: Snyk CLI is not installed

- **WHEN** a developer runs `./gradlew dependencySecurityCheck` without the Snyk CLI on `PATH`
- **THEN** the task fails with a clear message that the Snyk CLI is required