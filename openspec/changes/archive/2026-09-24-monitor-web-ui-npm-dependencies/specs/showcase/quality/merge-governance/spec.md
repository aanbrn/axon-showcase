# merge-governance — Delta

## MODIFIED Requirements

### Requirement: Dependency security scan runs on a schedule and on demand

The dependency security scan SHALL run automatically on a schedule and be manually triggerable, as the `snyk` and
`web-ui-audit` jobs in a dedicated workflow (`.github/workflows/dependency-security.yml`) separate from the merge gate.
The `snyk` job SHALL install the Snyk CLI, SHALL run the existing `:dependencySecurityCheck` task (Snyk
`test --all-sub-projects --policy-path=.snyk`), and SHALL authenticate with the `SNYK_TOKEN` secret. The `web-ui-audit`
job SHALL run the web UI `npmAudit` task, failing when a high-severity npm vulnerability is present, and SHALL need no
token. Both SHALL run on `ubuntu-latest`, and neither SHALL be part of the merge-gate `build` check or a required check
for merging into `main`.

#### Scenario: Scheduled trigger runs the dependency security scan

- **WHEN** the scheduled trigger fires
- **THEN** the `snyk` job runs `./gradlew dependencySecurityCheck`, scanning all sub-projects with the root `.snyk`
  policy applied

#### Scenario: Scheduled trigger runs the web UI vulnerability scan

- **WHEN** the scheduled trigger fires
- **THEN** the `web-ui-audit` job runs `./gradlew :showcase-web-ui:npmAudit`, failing when a high-severity npm
  vulnerability is present

#### Scenario: Manual trigger runs the dependency security scan

- **WHEN** a maintainer dispatches the dependency-security workflow manually
- **THEN** the `snyk` and `web-ui-audit` jobs run the same scans against the current `main`

#### Scenario: The dependency security scan is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the dependency-security run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it

### Requirement: Dependency update report runs on a schedule and on demand

The dependency update report SHALL run automatically on a schedule and be manually triggerable, as the same
`dependency-updates` job in a dedicated workflow separate from the merge gate. It SHALL run the existing
`dependencyUpdates` Gradle task (catalog-owned coordinates, majors for deferred groups suppressed) and the web UI
`npmOutdated` task, and SHALL surface the result by opening or updating a GitHub issue, SHALL run on `ubuntu-latest`
with the `GITHUB_TOKEN` granted `issues: write`, and SHALL NOT be part of the merge-gate `build` check or a required
check for merging into `main`.

#### Scenario: Scheduled trigger runs the dependency update report

- **WHEN** the scheduled trigger fires
- **THEN** the `dependency-updates` job runs `./gradlew dependencyUpdates` and `./gradlew :showcase-web-ui:npmOutdated`,
  and opens or updates the "Dependency updates" issue with the available stable catalog updates, the Gradle wrapper
  section, and the web UI npm updates (the actionable sections of the report only), and posts a new comment mentioning
  the repository owner so they are notified

#### Scenario: Manual trigger runs the dependency update report

- **WHEN** a maintainer dispatches the dependency-updates workflow manually
- **THEN** the `dependency-updates` job runs the same reports against the current `main` and updates the issue

#### Scenario: The dependency update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the dependency-updates run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it

#### Scenario: No stable updates are available

- **WHEN** the reports contain no stable catalog updates (no "dependencies have newer versions" section) and the web UI
  report is empty with a clean exit (`0`)
- **THEN** the issue states that no stable catalog updates are available, without listing the non-actionable milestone
  sections, and no notification comment is posted

#### Scenario: Repeated runs notify the owner without accumulating comments

- **WHEN** the workflow runs again on an existing issue and finds actionable updates (stable dependency updates, a newer
  Gradle wrapper, or web UI npm updates)
- **THEN** it posts a new comment mentioning the repository owner (so the owner is notified) and removes the previous
  bot-authored comment, keeping at most one bot comment on the issue

#### Scenario: The web UI report is included in the dependency updates issue

- **WHEN** the web UI has outdated npm dependencies
- **THEN** the "Dependency updates" issue includes a web UI section listing them, and the run notifies the owner as for
  catalog updates

#### Scenario: A failed web UI report is not read as no updates

- **WHEN** the web UI update report is empty and the recorded npm exit code is non-zero
- **THEN** the run fails rather than reporting "no updates"
