## Why

Two tooling CLI pins lag their latest releases. The repository's own `helmUpdates` task reports the pinned Helm CLI
(`helm = "4.2.4"`) against the latest release (`v4.3.0`), and the Snyk CLI pin in the scan workflow
(`snyk-version: v1.1307.0`) lags the latest release (`v1.1307.2`). Both should be current so the build's downloaded
client and the CI scan use the latest tooling.

## What Changes

- `gradle/libs.versions.toml`: `helm` bumps from `4.2.4` to `4.3.0` (the Helm CLI distribution the gradle-helm-plugin
  downloads and the build's Helm tasks use).
- `.github/workflows/snyk.yml`: `snyk-version` bumps from `v1.1307.0` to `v1.1307.2` (the Snyk CLI the weekly,
  observational security-scan workflow installs).

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. This is a tooling version bump: the specs describe behavior (the `helmUpdates` report, the Snyk scan) and remain
  accurate regardless of the pinned version values, so no requirement changes.

## Impact

- `gradle/libs.versions.toml` and `.github/workflows/snyk.yml`.
- No effect on application code, tests, or deployment behavior; the Helm CLI bump affects build-time Helm tasks only.
