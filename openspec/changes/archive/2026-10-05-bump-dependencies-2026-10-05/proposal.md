# Proposal

## Why

The weekly update check (`dependency-updates`, the 2026-10-04 run) flagged newer release versions for several
catalog-owned coordinates. Keeping the pins current is the routine hygiene the check exists to surface.

## What Changes

- `gradle/libs.versions.toml` — `logback` 1.6.4 → 1.6.5, `zstd-jni` 1.5.7-20 → 1.5.7-21, `guava` 33.7.1-jre →
  33.7.2-jre, `commons-lang3` 3.20.0 → 3.21.0, `jgroups` 4.2.30.Final → 4.2.32.Final, `opensearch-client-rest` 3.8.0 →
  3.9.0 (the `opensearch-rest-client` and `opensearch-rest-high-level-client` libraries), `swagger-ui` 5.32.15 → 5.33.1.
- Deliberately excluded, per a recorded decision: `log4j-core` 2.17.1 → 2.26.1 (the known spurious row from
  `checkBuildEnvironmentConstraints` — ADR-0007).

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — a pure dependency bump (`.openspec.yaml` sets `skip_specs: true`); no requirement changes.

## Impact

- `gradle/libs.versions.toml` only. No source, test, or deployment change is intended; any behavior shift surfaces
  through the verification tasks.
- Runtime-path libraries (`logback`, `zstd-jni`, `jgroups`, the OpenSearch REST clients) are verified at the integration
  tier — a same-major release can be binary-incompatible while compilation and the Docker-free check pass.
