## MODIFIED Requirements

### Requirement: Local dependency security scan task

The build SHALL provide a `dependencySecurityCheck` Gradle task that runs the Snyk dependency scan
(`snyk test --all-sub-projects --policy-path=.snyk`) across all sub-projects and reports the result to the developer.
The `--policy-path=.snyk` flag SHALL point the scan at the repository's `.snyk` policy so its suppressed findings take
effect, and that policy SHALL suppress only advisories with no available fix, each pinned to the exact assessed
vulnerable version with a stated reason and an expiry date, so an unfixable advisory does not leave the scan permanently
red; a suppressed advisory SHALL re-surface when its `expires` date passes. The task SHALL NOT be part of the `check`
lifecycle.

#### Scenario: Developer runs the dependency security scan

- **WHEN** a developer runs `./gradlew dependencySecurityCheck` with the Snyk CLI installed
- **THEN** the task invokes `snyk test --all-sub-projects --policy-path=.snyk` against the build and reports the scan
  result, failing when vulnerable paths are found

#### Scenario: Normal build does not run the dependency scan

- **WHEN** a developer runs `./gradlew check` or any build task other than `dependencySecurityCheck`
- **THEN** the dependency scan does not run

#### Scenario: Snyk CLI is not installed

- **WHEN** a developer runs `./gradlew dependencySecurityCheck` without the Snyk CLI on `PATH`
- **THEN** the task fails with a clear message that the Snyk CLI is required

#### Scenario: A suppressed advisory passes until its expiry

- **WHEN** the only findings trace to an advisory listed in `.snyk` with an unexpired `expires` date
- **THEN** the scan passes, so an advisory with no available fix does not leave it permanently red

#### Scenario: An expired suppression re-surfaces

- **WHEN** the only findings trace to an advisory listed in `.snyk` whose `expires` date is in the past
- **THEN** the scan fails, so the suppression re-surfaces for re-assessment rather than hiding the finding indefinitely
