# Proposal

## Why

`dependencyUpdates` reports `org.apache.logging.log4j:log4j-core [2.17.1 -> 2.26.1]` while the project takes `2.26.1`
everywhere — the same report lists `log4j-core:2.26.1` among its current versions, and `2.17.1` is a version the project
declares nowhere. The floor is **Gradle's own** Log4Shell ban (`Log4jBannedVersion` in the distribution's
`gradle-logging` jar, `LOG4J2_CORE_REQUIRED_VERSION = 2.17.1`), applied to every classpath Gradle builds, and
`checkBuildEnvironmentConstraints` reports it as the entry's "current version". ADR-0007 recorded the row as known noise
and rejected disabling the constraint check; the plugin does consult `rejectVersionIf` for the build-environment path,
so the row can be filtered where the wrong value is read. It invites a bump already applied, weekly.

## What Changes

- **`DependencyUpdateRules.shouldReject`** also rejects a candidate no newer than the version the catalog declares
  **while the plugin's reported current differs from that version** — the signature of a current read from a constraint
  floor. The catalog is the project's version authority, so such a coordinate reports nothing to update; an up-to-date
  coordinate, whose current does agree, keeps its place in the report's up-to-date section.
- The catalog's `coordinate -> version` pairs come from a **`catalogVersions(lines)`** function on the same object, so
  the resolve step (a `version.ref` through `[versions]`) is unit-testable rather than buried in a convention script.
- `checkBuildEnvironmentConstraints` stays `true`: the rule filters the report, not what the task reads.
- **`docs/adr/0021-*.md`** supersedes ADR-0007's Decision; ADR-0007's Context and its recorded experiments stay as they
  are.
- **`AGENTS.md`** and **`README.md`** correct the row's attribution — Gradle's own ban, not a `spotbugs-annotations`
  constraint, which the pinned `4.10.5` no longer publishes — and record that a report rule does reach it.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-management`: the "Dependency update reporting covers only catalog-owned versions"
- `showcase/quality/dependency-management`: the "Dependency update reporting covers only catalog-owned versions"
  requirement gains the rule that a catalog-owned coordinate whose declared version is not older than the candidate —
  while the plugin's reported current differs from it — is not reported, and states the false-negative surface that rule
  accepts.

## Impact

- **Build**: `build-logic` only — the rule, its tests, the parse function, and the convention that feeds it.
  `dependencyUpdates` is not part of `check`, and nothing about the built artifacts changes.
- **Cost**: rejecting the whole candidate set makes each project's resolution walk more versions than before. Measured
  on a full-rejection run at roughly tens of minutes against the usual couple, so the weekly update workflow gets
  slower; recorded in the design rather than discovered later.
- **Observable**: the report's update section loses the `log4j-core` row and keeps its other four; the coordinate's
  projects that resolve it at `2.26.1` still list it as up to date, while those whose reported current is the ban floor
  move it to the informational "Failed to determine the latest version" section, which the weekly issue does not
  extract.
- **Risk**: a catalog-owned coordinate whose _resolved_ version another surface governs below the catalog's would no
  longer be reported — stated in the spec, the design, and ADR-0021 rather than left to be discovered.
- **Docs**: `AGENTS.md`, `README.md`, `docs/adr/` (ADR-0007 superseded by ADR-0021), and a parked `docs/ideas.md` idea
  for the gap this work surfaced (`build-logic`'s own pins are outside every update check).
