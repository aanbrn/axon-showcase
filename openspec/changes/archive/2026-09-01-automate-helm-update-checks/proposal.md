## Why

Helm CLI and Helm chart updates are invisible today: `dependencyUpdates` only checks Gradle library coordinates, and
`verifyInfraImageVersions` only checks a pinned chart's image tag against the catalog — neither surfaces "a newer
version of Helm CLI / a chart exists". With all chart coordinates now concrete (see `pin-helm-chart-versions`), an
automated check can report exactly which pinned coordinates are stale, mirroring the existing dependency-updates
workflow.

## What Changes

- Add a Gradle check (e.g. `helmUpdates`) that reports, for each pinned coordinate:
  - the **Helm CLI** (`helm = "4.2.4"` in the catalog) against the latest Helm release, and
  - each **Helm chart** coordinate (bitnami charts, kps, tempo, common) against the latest version in its repository,
    via the plugin-managed Helm client.
- Add a weekly GitHub workflow (`helm-updates.yml`) that runs the check and opens/updates a "Helm updates" GitHub
  issue with the actionable coordinates — mirroring the existing `dependency-updates.yml` pattern.
- The check is observational only: it SHALL NOT be part of the `build` merge gate or a required check for `main`.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `showcase/quality/merge-governance`: a new requirement "Helm update report runs on a schedule and on demand"
  describing the weekly workflow, mirroring the existing dependency-updates/snyk/e2e requirements.

## Impact

- New build-logic task or extension (`helmUpdates`) querying the Helm CLI and chart repositories.
- New `.github/workflows/helm-updates.yml` (weekly + `workflow_dispatch`), reusing the dependency-updates issue
  machinery (open/update issue, mention the owner, keep one comment).
- `gradle/libs.versions.toml` — no change (reads existing coordinates); the Helm CLI version is the existing `helm`
  coordinate.
- `AGENTS.md` — note the new workflow under the CI section.