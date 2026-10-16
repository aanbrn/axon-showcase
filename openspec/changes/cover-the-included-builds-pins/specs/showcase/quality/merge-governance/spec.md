## MODIFIED Requirements

### Requirement: Dependency update report runs on a schedule and on demand

The dependency update report SHALL run automatically on a schedule and be manually triggerable, as the same
`dependency-updates` job in a dedicated workflow separate from the merge gate. It SHALL run the existing
`dependencyUpdates` Gradle task (catalog-owned coordinates, majors for deferred groups suppressed), the web UI
`npmOutdated` task, and the `buildLogicUpdates` check for the plugin pins the included `build-logic` build declares,
specified in `showcase/quality/dependency-management`. It SHALL surface the result by opening or updating a GitHub
issue, SHALL run on `ubuntu-latest` with the `GITHUB_TOKEN` granted `issues: write`, and SHALL NOT be part of the
merge-gate `build` check or a required check for merging into `main`.

#### Scenario: Scheduled trigger runs the dependency update report

- **WHEN** the scheduled trigger fires
- **THEN** the `dependency-updates` job runs `./gradlew dependencyUpdates`, `./gradlew :showcase-web-ui:npmOutdated`,
  and `./gradlew buildLogicUpdates`, and opens or updates the "Dependency updates" issue with the available stable
  catalog updates, the build-logic pin updates, the Gradle wrapper section, and the web UI npm updates (the actionable
  sections of the reports only), and posts a new comment mentioning the repository owner so they are notified

#### Scenario: Manual trigger runs the dependency update report

- **WHEN** a maintainer dispatches the dependency-updates workflow manually
- **THEN** the `dependency-updates` job runs the same reports against the current `main` and updates the issue

#### Scenario: The dependency update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the dependency-updates run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it

#### Scenario: No stable updates are available

- **WHEN** none of the four inputs the workflow reads reports an actionable update (the catalog report has no
  "dependencies have later release versions" section, the Gradle wrapper is current, no build-logic pin is behind a
  newer stable release, and the web UI report is empty with a clean exit (`0`))
- **THEN** the issue states that no stable updates are available, without listing the report's up-to-date section, and
  no notification comment is posted

#### Scenario: Repeated runs notify the owner without accumulating comments

- **WHEN** the workflow runs again on an existing issue and finds actionable updates (stable dependency updates, a newer
  Gradle wrapper, web UI npm updates, or a build-logic pin behind a newer stable release)
- **THEN** it posts a new comment mentioning the repository owner (so the owner is notified) and removes the previous
  bot-authored comment, keeping at most one bot comment on the issue

#### Scenario: The web UI report is included in the dependency updates issue

- **WHEN** the web UI has outdated npm dependencies
- **THEN** the "Dependency updates" issue includes a web UI section listing them, and the run notifies the owner as for
  catalog updates

#### Scenario: A failed web UI report is not read as no updates

- **WHEN** the web UI update report is empty and the recorded npm exit code is non-zero
- **THEN** the run fails rather than reporting "no updates"

#### Scenario: The build-logic pin updates appear in the dependency updates issue

- **WHEN** the `buildLogicUpdates` check reports an update for a pin the included build declares, or names one as unread
- **THEN** the "Dependency updates" issue carries it in its own section, notifying the owner as catalog updates do
