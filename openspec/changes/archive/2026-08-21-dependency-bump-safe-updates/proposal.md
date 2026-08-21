# Proposal: Apply safe dependency updates

## Why

The `dependencyUpdates` report surfaces routine same-major minor/patch bumps for catalog-owned coordinates that are
safe to take on the current Spring Boot 3.5 baseline. All are within their existing major (no SB4-locked trains, no
major-blocked groups), so applying them keeps the stack current without risking the deferred Spring Boot 4 migration.

## What Changes

- `gradle/libs.versions.toml`: bump the following catalog versions:
  - `spring-framework-bom` `6.2.18` → `6.2.19`
  - `spring-security-bom` `6.5.10` → `6.5.11`
  - `swagger` `2.2.53` → `2.2.54`
  - `swagger-ui` `5.32.13` → `5.32.14`
  - `reactor-bom` `2025.0.6` → `2025.0.7`
  - `protobuf` `4.35.1` → `4.36.0`
  - `httpclient5` `5.6.3` → `5.6.4`
  - `micrometer-bom` `1.17.0` → `1.17.1`
  - `micrometer-tracing-bom` `1.7.0` → `1.7.1`
  - `guava` `33.7.0-jre` → `33.7.1-jre`
  - `spotbugs` `4.10.3` → `4.10.4`
  - `spring-data-opensearch` `2.0.6` → `2.0.7`
  - `springdoc-openapi-starter` `2.8.17` → `2.9.0`

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. No spec-level requirement changes; this is a dependency-version bump. `skip_specs: true`.

## Impact

- **Code**: `gradle/libs.versions.toml` (thirteen version values).
- **Build**: `./gradlew dependencyUpdates` stops listing these rows; the affected services recompile against the new
  patch/minor versions.
- **Tests**: no test changes; verification compiles the affected modules and runs `dependencyUpdates` to confirm the
  rows are gone.
- **Not included**: `spring-data-bom` (`2025.0.13` → `2025.1.7` is the SB4 train — deferred), `log4j-core` (ADR-0007
  noise).