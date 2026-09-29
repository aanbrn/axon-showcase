# Design

## Context

See `proposal.md` — Why. The Snyk CLI is pinned in `.github/workflows/dependency-security.yml`'s `snyk/actions/setup`
step (`snyk-version`), and the `toolingUpdates` task / `tooling-updates` workflow is the check that reports newer
versions (it is the only thing that detects a release for a workflow-pinned CLI).

## Goals / Non-Goals

**Goals:**

- Move the pin to the latest Snyk CLI release and confirm the check is clean.

**Non-Goals:**

- Any change to the scan behavior; only the CLI version the workflow installs moves.

## Decisions

### Decision: bump the pin, confirm the tag exists and lints, and verify with the credentialed run

`workflowLint` (actionlint) proves only that the YAML lints, not that the version tag is installable, and a local
`dependencySecurityCheck` uses the developer's own `snyk` on PATH, not the workflow's pinned version — so neither
verifies the pin. The verification is therefore: confirm the release tag exists
(`gh api repos/snyk/cli/releases/tags/<tag>`), keep `workflowLint` green, and let the credentialed run — a
`gh workflow run dependency-security.yml --ref <branch>` dispatch against the pushed branch (before the archive commit),
or the next scheduled run — be the first real execution that installs the pinned CLI.

- **Alternative — trust `workflowLint`:** it reads the YAML, not the registry; an uninstallable tag passes it.

## Risks / Trade-offs

- **The pinned tag is uninstallable** → the dispatch (or scheduled run) surfaces it; confirming the release exists first
  narrows the window.
- **The dispatch needs the branch pushed first** → run it against the pushed branch
  (`gh workflow run dependency-security.yml --ref <branch>`) before the archive commit, so the task completes inside the
  PR rather than archiving unticked.
