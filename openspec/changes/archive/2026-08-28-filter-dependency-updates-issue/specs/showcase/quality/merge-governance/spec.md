# showcase/quality/merge-governance — Delta Spec

## MODIFIED Requirements

### Requirement: Dependency update report runs on a schedule and on demand

The dependency update report SHALL run automatically on a schedule and be manually triggerable, as the same
`dependency-updates` job in a dedicated workflow separate from the merge gate. It SHALL run the existing
`dependencyUpdates` Gradle task (catalog-owned coordinates, majors for deferred groups suppressed) and SHALL surface
the result by opening or updating a GitHub issue, SHALL run on `ubuntu-latest` with the `GITHUB_TOKEN` granted
`issues: write`, and SHALL NOT be part of the merge-gate `build` check or a required check for merging into `main`.

#### Scenario: Scheduled trigger runs the dependency update report

- **WHEN** the scheduled trigger fires
- **THEN** the `dependency-updates` job runs `./gradlew dependencyUpdates` and opens or updates the "Dependency
  updates" issue with the available stable catalog updates and the Gradle wrapper section (the actionable sections
  of the report only), mentioning the repository owner so they are notified

#### Scenario: Manual trigger runs the dependency update report

- **WHEN** a maintainer dispatches the dependency-updates workflow manually
- **THEN** the `dependency-updates` job runs the same report against the current `main` and updates the issue

#### Scenario: The dependency update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the dependency-updates run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it

#### Scenario: No stable updates are available

- **WHEN** the report contains no stable catalog updates (no "dependencies have newer versions" section)
- **THEN** the issue states that no stable catalog updates are available, without listing the non-actionable
  milestone sections