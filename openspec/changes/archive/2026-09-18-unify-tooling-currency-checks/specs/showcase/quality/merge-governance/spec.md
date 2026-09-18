# showcase/quality/merge-governance — Delta Spec

## ADDED Requirements

### Requirement: Pinned-tool update report runs on a schedule and on demand

The pinned-tool update report SHALL run automatically on a schedule and be manually triggerable, as its own
`tooling-updates` job in a dedicated workflow separate from the merge gate. It SHALL run a Gradle check that reports,
for each declared pinned-tool check — a tool whose version is pinned in a workflow rather than in the version catalog —
the latest available version, so a pin that lags is visible, and SHALL surface the result by opening or updating a
GitHub issue, updated silently when nothing is actionable. It SHALL run on `ubuntu-latest` with the `GITHUB_TOKEN`
granted `issues: write`, and SHALL NOT be part of the merge-gate `build` check or a required check for merging into
`main`.

#### Scenario: Scheduled trigger runs the pinned-tool update report

- **WHEN** the scheduled trigger fires
- **THEN** the `tooling-updates` job runs the tool-pin check and opens or updates the "Tooling updates" issue listing
  each pinned tool with an available newer version, mentioning the repository owner so they are notified

#### Scenario: Manual trigger runs the pinned-tool update report

- **WHEN** a maintainer dispatches the tooling-updates workflow manually
- **THEN** the `tooling-updates` job runs the same check against the current `main` and updates the issue

#### Scenario: A current pin reports no update

- **WHEN** every checked tool's pin matches the latest available version
- **THEN** the report carries no actionable update, and the run updates the issue without mentioning the repository
  owner

#### Scenario: Repeated runs replace the notification rather than stack it

- **WHEN** the workflow runs again on an existing issue with an available update
- **THEN** it replaces the previous owner-mention comment rather than adding another

#### Scenario: The pinned-tool update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the tooling-updates run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it
