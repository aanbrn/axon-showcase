## Why

`Resilience4jAutoConfigurationImportFilter` — the class that conditionally disables Resilience4j feature
auto-configurations based on hierarchical properties — has **zero test coverage**. Its enablement logic is specified
in the `showcase/resilience4j-extension` spec but never verified. This change adds deterministic unit tests for the
filter, including the SPI and configuration-metadata registration contracts.

## What Changes

- Add `Resilience4jAutoConfigurationImportFilterTests` in
  `showcase-resilience4j-extension/src/test/java/showcase/resilience4j/` covering:
  - `match` fails fast with `IllegalStateException` (`"environment" is required`) when no environment is injected
  - Master flag `resilience4j.enabled=false` excludes all five Resilience4j feature auto-configurations regardless of
    per-feature flags
  - All features eligible by default (no flags set → defaults to `true`)
  - Per-feature disable (e.g., `resilience4j.circuitbreaker.enabled=false`) excludes only that feature
  - Single bulkhead flag gating (`resilience4j.bulkhead.enabled=false` excludes only bulkhead)
  - Non-Resilience4j auto-configuration class names pass through unfiltered
  - Regex matching against the exact circuit breaker FQCN from the spec
  - Null/empty class-name entries are excluded (result slot stays `false`)
- Add tests reading module resources:
  - `META-INF/spring.factories` lists `showcase.resilience4j.Resilience4jAutoConfigurationImportFilter`
  - `META-INF/additional-spring-configuration-metadata.json` declares all six properties as `java.lang.Boolean` with
    `defaultValue` `true`
- Add `testImplementation(libs.mockito.core)` to `build.gradle.kts`.

## Capabilities

### New Capabilities

_(none — pure test addition, no behavioral change)_

### Modified Capabilities

_(none)_

## Impact

- **Code**: new test source file under `showcase-resilience4j-extension/src/test/java/showcase/resilience4j/`; add
  `testImplementation(libs.mockito.core)` to `build.gradle.kts`.
- **Dependencies**: Mockito 5.23.0 (already in the version catalog and agent-enabled by `java-conventions`).
- **Behavior**: unchanged.