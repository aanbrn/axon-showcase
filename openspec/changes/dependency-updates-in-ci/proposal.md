# Run the Gradle dependency update report on GitHub

## Why

The `dependencyUpdates` report (catalog-owned coordinates, majors for deferred groups suppressed) exists as a Gradle
task and is run via the `/dependency-updates` command, but nothing surfaces it automatically. The `merge-governance`
capability now covers `ci.yml`, `e2e.yml`, and `snyk.yml` as scheduled workflows; a scheduled dependency-update
report closes the same gap for dependency hygiene: it surfaces available catalog updates (and the Gradle wrapper
version) on a cadence, without the noise of Dependabot (which would not respect the deferred-major policy in
`config/dependency-updates/major-disabled.properties` and the calendar-versioning heuristic).

## What Changes

- Add a new `.github/workflows/dependency-updates.yml` workflow (separate from `ci.yml`, `e2e.yml`, and `snyk.yml`):
  - **Triggers**: `workflow_dispatch` (manual) and a scheduled cron — not `pull_request` and not `push` to `main`,
    consistent with the observational-gate pattern.
  - **Job**: single `dependency-updates` job on `ubuntu-latest`; runs `./gradlew dependencyUpdates` (writes
    `build/dependencyUpdates/report.txt`), then parses the report and opens or updates a GitHub issue summarizing
    the available updates (dependencies + the `Gradle CURRENT updates` wrapper section).
  - **Permissions**: `contents: read` and `issues: write` on the `GITHUB_TOKEN` — no PAT, no other secrets (the
    workflow only touches this repo's issues, so the least-privilege `GITHUB_TOKEN` suffices).
  - **Cache**: `gradle/actions/setup-gradle` restores the Gradle User Home, never the workspace `build/` directories.
- The issue is updated in place on subsequent runs (stable title, e.g. "Dependency updates"), so the report is
  preserved and reviewable instead of vanishing in the Actions tab. The body starts with `cc @<repository_owner>` so
  the owner receives an email notification on creation and on each update.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/merge-governance`: adds the requirement that the dependency update report runs on a schedule and
  on demand, and that it is not part of the merge gate.

## Impact

- **New files**: `.github/workflows/dependency-updates.yml`.
- **GitHub config**: no new secrets (uses `GITHUB_TOKEN` with `issues: write`); no ruleset changes (observational,
  never a required check).
- **Build/test**: no Gradle task changes — the workflow invokes the existing `dependencyUpdates` task and reads its
  report file.
- **Behavior**: a "Dependency updates" issue is created or updated on each run; no application, build, or dependency
  changes are made.