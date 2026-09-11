# showcase/quality/merge-governance

## ADDED Requirements

### Requirement: Buildpack update report runs on a schedule and on demand

The Paketo buildpack update report SHALL run automatically on a schedule and be manually triggerable, as the same
`buildpack-updates` job in a dedicated workflow separate from the merge gate. It SHALL run the `buildpackUpdates` Gradle
check that reports, for the pinned Paketo builder and each pinned buildpack in the version catalog, the latest available
version from the registry, and SHALL surface the result by opening or updating a GitHub issue, SHALL run on
`ubuntu-latest` with the `GITHUB_TOKEN` granted `issues: write`, and SHALL NOT be part of the merge-gate `build` check
or a required check for merging into `main`.

#### Scenario: Scheduled trigger runs the buildpack update report

- **WHEN** the scheduled trigger fires
- **THEN** the `buildpack-updates` job runs the buildpack update check and opens or updates the "Buildpack updates"
  issue listing each pinned Paketo builder and buildpack coordinate with an available newer version, and posts a new
  comment mentioning the repository owner so they are notified

#### Scenario: Manual trigger runs the buildpack update report

- **WHEN** a maintainer dispatches the buildpack-updates workflow manually
- **THEN** the `buildpack-updates` job runs the same check against the current `main` and updates the issue

#### Scenario: The buildpack update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the buildpack-updates run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it

#### Scenario: No buildpack updates are available

- **WHEN** every pinned Paketo builder and buildpack coordinate is current
- **THEN** the issue states that no buildpack updates are available, and no notification comment is posted

#### Scenario: Repeated runs notify the owner without accumulating comments

- **WHEN** the workflow runs again on an existing issue and finds an available update
- **THEN** it posts a new comment mentioning the repository owner (so the owner is notified) and removes the previous
  bot-authored comment, keeping at most one bot comment on the issue
