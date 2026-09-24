# Proposal: Bump the Jackson BOMs for the new Snyk findings

## Why

The Snyk scan (dispatched 2026-09-24) fails with **seven newly-published advisories** across both Jackson lines: four
(two High, two Medium) on **Jackson 2** at `2.22.2` (`com.fasterxml.jackson.core:jackson-core` and `jackson-databind`)
and three (two High, one Medium) on **Jackson 3** at `tools.jackson.core` `3.2.2` (`jackson-core` and
`jackson-databind`) — up to seven advisories and 63 vulnerable paths in a single project. `.snyk` masks neither, and
both are catalog-owned BOM pins the project applies through the platform. Snyk names the fixes (`2.22.3` and `3.2.3`),
and `dependencyUpdates` confirms both BOM updates are available. Two one-line bumps clear a real security signal with no
suppression.

## What Changes

- `gradle/libs.versions.toml` — bump `jackson2-bom` from `2.22.2` to `2.22.3` (`com.fasterxml.jackson:jackson-bom`) and
  `jackson3-bom` from `3.2.2` to `3.2.3` (`tools.jackson:jackson-bom`), resolving all seven advisories.
- `openspec/specs/showcase/quality/dependency-security` (delta) — the constraint requirement raises Jackson 3 to at
  least `3.2.3` and names `com.fasterxml.jackson.core` (Jackson 2) as a constrained transitive resolving through
  `com.fasterxml.jackson:jackson-bom` at least `2.22.3`, with a scenario for each.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/dependency-security`: the constraint requirement's Jackson 3 floor rises to `3.2.3`, and it gains
  `com.fasterxml.jackson.core` (Jackson 2) at least `2.22.3`, each with a resolution scenario.

## Impact

- **Build**: two catalog versions; no build logic, application code, or dependency graph shape changes.
- **Tests**: none beyond the normal gates; the existing suite runs against the bumped Jackson patches.
- **Deployment / CI**: clears the observational `dependency-security` run — neither a merge gate nor a required check.
