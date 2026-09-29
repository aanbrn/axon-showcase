# Proposal

## Why

The weekly update checks (`dependency-updates`, the 2026-09-28 run) flagged newer release versions for several
catalog-owned coordinates and the web UI's npm dependencies. Keeping the pins current is the routine hygiene the checks
exist to surface.

## What Changes

- `gradle/libs.versions.toml` — `lz4-java` 1.11.3 → 1.12.0, `logback` 1.6.3 → 1.6.4, `zstd-jni` 1.5.7-19 → 1.5.7-20,
  `nullaway` 0.14.1 → 0.14.2, `spotless-plugin` 8.10.2 → 8.10.3.
- `showcase-web-ui/package.json` (+ the lockfile) — `@reduxjs/toolkit` → 2.13.0, `@tanstack/react-query` → 5.104.0,
  `@types/node` → 26.6.3, `@typescript-eslint/eslint-plugin`/`parser`/`typescript-eslint` → 8.71.0, `react-hook-form` →
  7.89.0.
- Deliberately excluded, per recorded decisions: `log4j-core` (the known spurious row from
  `checkBuildEnvironmentConstraints` — ADR-0007), `opensearch-java` 3.10.0 (held, binary-incompatible with
  `spring-data-opensearch` 2.x — parked in `docs/ideas.md`), and `typescript` 7 (a deferred major; `typescript-eslint`
  caps TypeScript below 6.1).

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — a pure dependency bump (`.openspec.yaml` sets `skip_specs: true`); no requirement changes.

## Impact

- `gradle/libs.versions.toml` and `showcase-web-ui/package.json`/`package-lock.json`.
- 13 Java test sources reformatted by the `spotless-plugin` 8.10.3 bump — the formatter's new output (palantir
  chain-splitting), whitespace-only, applied via `spotlessApply`.
- No runtime behavior change.
