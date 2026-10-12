# Proposal

## Why

The 2026-10-12 `toolingUpdates` report names one row: `snyk-cli: v1.1307.4 -> v1.1308.0`. Re-resolved before bumping,
per the stale-target rule, against the tool's own lookup (`gh api repos/snyk/cli/releases/latest`) — the latest release
is still `v1.1308.0` (published 2026-10-08), so the row is current. No Snyk product change depends on the pin; it is the
CLI the credentialed scan installs.

## What Changes

- **`.github/workflows/dependency-security.yml`** — `snyk-version` `v1.1307.4` → `v1.1308.0` in the `snyk/actions/setup`
  step. The action ref itself (`snyk/actions/setup@master`) is a deliberate floating ref Dependabot does not manage, and
  stays.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. A tool version pin; no specified behavior changes. (The `quality/dependency-security` spec
owns the scan's _policy_ — the suppression file and the floors — not the CLI's version.)

## Impact

- Files: `.github/workflows/dependency-security.yml`.
- **Not verifiable by `check`**: `workflowLint` (actionlint) proves only that the YAML lints, not that the tag is
  installable. The first real execution is the **credentialed dispatch** of `dependency-security.yml` on this branch
  (`gh workflow run … --ref <branch>`, using the repository's `SNYK_TOKEN` secret), which installs the pinned CLI and
  runs the scan — that dispatch is this change's verification of record.
