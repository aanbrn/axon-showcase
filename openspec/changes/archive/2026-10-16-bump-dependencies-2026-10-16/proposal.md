# Proposal

## Why

The update-check reports list stable catalog updates and in-range npm updates; taking them keeps the pinned set current.
Re-resolved on 2026-10-16 with the repo's own checks rather than read off the tracker issue: the catalog has three rows
(`caffeine` `3.3.0`, `grpc-bom` `1.84.2`, `swagger-ui` `5.33.1`) and the web UI's npm report twelve direct in-range
rows. The Gradle wrapper is already current (`9.8.1`, `UP-TO-DATE`), so this unit does not touch it. The previous unit
took the five plugin pins the new `buildLogicUpdates` check surfaced; this is the routine set the other two reports
list.

## What Changes

- **`gradle/libs.versions.toml`** — three keys: `swagger-ui` `5.33.1`→`5.33.2`, `caffeine` `3.3.0`→`3.3.1` (which the
  `caffeine` and `jcache` libraries both reference), and `grpc` `1.84.2`→`1.85.0` (the report resolves
  `1.84.2 → 1.84.3 → 1.85.0`, the gRPC BOM the platform imports).
- **`showcase-web-ui/package-lock.json`** — an `npm update` reconciliation across the twelve direct rows and the graph
  they re-resolve: 50 packages moved, including `react`/`react-dom` `19.3.0`→`19.3.1`, `vitest` and `@vitest/*`
  `5.0.4`→`5.0.5`, the `typescript-eslint` set `8.71.1`→`8.71.2`, `jsdom` `30.1.2`→`30.1.3`, `zod` `4.6.6`→`4.6.7`,
  `@playwright/test` `1.64.0`→`1.64.1`, `rolldown` `1.2.13`→`1.2.14` with its platform bindings, and one **cross-major**
  move, `@asamuzakjp/dom-selector` `9.2.4`→`10.0.3` (a `jsdom` transitive). `package.json` is unchanged: its ranges
  already admit these versions.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: a pure dependency bump, which no capability spec covers (`skip_specs`). The catalog's entries and the npm graph
are the pinned set, not behavior.

## Impact

- **Runtime path, so the integration tier verifies it**: the three keys reach the command service, the gateway and the
  query side (the modules each one reaches are enumerated once in the change's design). Those bumps can break a
  cross-library contract only at runtime, so the integration tests are the tier that verifies them — caffeine's cache
  behavior in the command service and the gateway, and the gRPC transport between the gateway and the query service.
- **Bundle path**: `react`/`react-dom` are in the production bundle, so the web UI's `build` is run as well as its
  `check`.
- **No formatter reflow**: neither the Spotless plugin nor the npm `prettier` is in the set, so `spotlessApply` has
  nothing to reflow.
- **Observable**: the catalog report's update section and the npm report both come back empty on the next run.
- **No suppression moves**: `.snyk`'s ignores are all Spring coordinates, and `npmAudit` passing shows no
  `npm-audit-ignores` entry went stale.
