## ADDED Requirements

### Requirement: Helm update report runs on a schedule and on demand

The Helm update report SHALL run automatically on a schedule and be manually triggerable, as the same `helm-updates`
job in a dedicated workflow separate from the merge gate. It SHALL run an existing Gradle check (e.g.
`helmUpdates`) that reports, for the pinned Helm CLI version and each pinned Helm chart coordinate in the version
catalog, the latest available version from the Helm CLI's release channel and the charts' repositories, and SHALL
surface the result by opening or updating a GitHub issue, SHALL run on `ubuntu-latest` with the `GITHUB_TOKEN` granted
`issues: write`, and SHALL NOT be part of the merge-gate `build` check or a required check for merging into `main`.

#### Scenario: Scheduled trigger runs the Helm update report

- **WHEN** the scheduled trigger fires
- **THEN** the `helm-updates` job runs the Helm update check and opens or updates the "Helm updates" issue listing each
  pinned coordinate with an available newer version (Helm CLI and charts), and posts a new comment mentioning the
  repository owner so they are notified

#### Scenario: Manual trigger runs the Helm update report

- **WHEN** a maintainer dispatches the helm-updates workflow manually
- **THEN** the `helm-updates` job runs the same check against the current `main` and updates the issue

#### Scenario: The Helm update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the helm-updates run is not required, because it is not part of the merge-gate `build` check and no ruleset
  requires it

#### Scenario: No Helm updates are available

- **WHEN** every pinned Helm CLI and chart coordinate is current
- **THEN** the issue states that no Helm updates are available, and no notification comment is posted

#### Scenario: Major chart updates are suppressed for deferred charts

- **WHEN** a pinned chart is listed in the helm major-disabled configuration and only a major-jump chart version is
  available
- **THEN** the issue does not list that chart, because the major bump would carry a new preconfigured image tag that
  diverges from the test-surface `*-image-tag` pins; a same-major (minor or patch) chart update SHALL still be
  reported

#### Scenario: Repeated runs notify the owner without accumulating comments

- **WHEN** the workflow runs again on an existing issue and finds available Helm updates
- **THEN** it posts a new comment mentioning the repository owner (so the owner is notified) and removes the
  previous bot-authored comment, keeping at most one bot comment on the issue