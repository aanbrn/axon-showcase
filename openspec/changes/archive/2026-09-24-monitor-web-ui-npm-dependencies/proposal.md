# Proposal: Monitor the web UI's npm dependencies

## Why

The web UI's npm dependencies sit outside every dependency check. `dependencyUpdates` reports only catalog-owned Gradle
coordinates, and the scheduled Snyk scan runs `snyk test --all-sub-projects` (Gradle only) under a `.snyk` policy that
holds no npm findings — so an outdated or vulnerable package in `showcase-web-ui` produces neither an update issue nor a
security finding, while the JVM modules get both. The two ecosystems are monitored asymmetrically.

## What Changes

- `build-logic/src/main/kotlin/frontend-conventions.gradle.kts` — register two npm-backed tasks: `npmOutdated` (group
  `help`; reports the web UI's outdated packages; not part of `check`) and `npmAudit` (group `verification`; runs
  `npm audit`, failing on high-severity findings; not part of `check`). Both use the node plugin's managed npm.
- `showcase-web-ui/package.json` — an `outdated:report` script that writes the update report and the npm exit code the
  workflow reads.
- `.github/workflows/dependency-updates.yml` — run `:showcase-web-ui:npmOutdated` and add the web UI section to the
  existing "Dependency updates" issue; add the Node/npm cache includes.
- `.github/workflows/snyk.yml` → **rename** to `.github/workflows/dependency-security.yml` (`name: Dependency Security`;
  **BREAKING** for `gh workflow run snyk.yml`) and add a `web-ui-audit` job running `:showcase-web-ui:npmAudit`.
- `build.gradle.kts` — repoint the `toolingUpdates` check's `workflowFile` and `pinFiles` entries to the renamed
  workflow.
- Docs — `README.md`, `AGENTS.md`, `SECURITY.md`, `.opencode/commands/dependency-security-check.md`,
  `.opencode/commands/dependency-updates.md`, and `docs/adr/0006-*.md` reference the renamed workflow and the added web
  UI checks; a new `docs/adr/0014-*.md` records the npm-audit-over-Snyk choice.
- `docs/ideas.md` — remove the implemented "Dependency updates for the web UI" idea.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/dependency-management`: adds a requirement that the build reports the web UI's npm dependency
  updates, alongside the catalog-owned Gradle reporting.
- `showcase/quality/dependency-security`: adds a requirement for a local npm vulnerability-scan task for the web UI,
  alongside the Snyk JVM scan.
- `showcase/quality/merge-governance`: the dependency-update and dependency-security workflow requirements gain the web
  UI checks (the latter under the renamed `dependency-security` workflow).

## Impact

- **Build**: two new `frontend-conventions` tasks; `build.gradle.kts`'s `toolingUpdates` check repointed. No application
  runtime change.
- **Tests**: no `build-logic` unit target exists for the frontend convention, so the two checks are verified live by the
  change's positive-control tasks — a known-older dependency is reported, and a dependency pinned to a known-vulnerable
  version fails the audit.
- **Deployment / CI**: observational only — neither check is part of the merge-gate `build`; the renamed
  `dependency-security` workflow runs the same schedule as before plus the new job.
