# Filter the dependency-updates issue to actionable sections

## Why

The `dependency-updates` workflow posts the entire raw `build/dependencyUpdates/report.txt` into the "Dependency
updates" issue. The report contains mostly noise for a reader: the "using the latest milestone version" list (deps
that are current), and the "have later milestone versions" list (non-stable/milestone candidates and
build-environment noise like the known `log4j-core` constraint row), which the `/dependency-updates` command
explicitly ignores. Only the "dependencies have newer versions" section (stable, catalog-owned updates) and the
`Gradle CURRENT updates` wrapper section are actionable — but they are buried in ~200 lines of noise, and the
notification email carries the whole blob.

## What Changes

- In `.github/workflows/dependency-updates.yml`, the issue-body step extracts only the actionable sections from
  `build/dependencyUpdates/report.txt`:
  - The "The following dependencies have newer versions:" section (stable catalog updates), when present.
  - The "Gradle CURRENT updates:" section (wrapper status).
  - When there are no stable updates, the body states that explicitly ("No stable catalog updates available")
    instead of dumping the noise sections.
- The issue title, in-place-update behavior, and the `cc @<owner>` notification are unchanged.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/merge-governance`: the dependency-updates requirement's scheduled-run scenario now states that the
  issue carries only the actionable sections of the report, not the raw report.

## Impact

- **Changed file**: `.github/workflows/dependency-updates.yml` (the issue-body step only).
- **Behavior**: the "Dependency updates" issue and notification email contain only stable update candidates and the
  wrapper status, or an explicit "no stable updates" line.
- **No secrets, no Gradle task changes**: the report file and `dependencyUpdates` task are untouched.