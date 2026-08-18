# ADR-0004: Defer the Spring Boot 4 migration

Date: 2026-08-18

Status: Accepted

## Context

Spring Boot 4 is the current major with an active support window, and Axon Framework 4.13 officially supports it, so a
migration is desirable to keep the reference stack current. However, an attempt to move from Spring Boot 3.5 to 4.0.7
surfaced deep, cross-cutting API changes beyond a version bump: Flyway auto-configuration moved to a new
`spring-boot-starter-flyway` module, `JCacheManagerCustomizer` was removed, the Jackson 2 auto-configuration package
was relocated (Jackson 3 is the new default), and the Actuator health package moved. Testcontainers 2.x also renamed its
artifacts. The change ballooned past a dependency-coordinate swap into a substantial application-code migration.

## Decision

Defer the Spring Boot 4 migration. Remain on Spring Boot 3.5 as the baseline. The migration is planned (catalog and
BOM coordinates are known, the affected APIs are enumerated) and will be reopened as a dedicated change when there is
capacity, sequenced as: catalog/BOM bump → Resilience4j extension regex update → service build swaps → per-service
API fixes (Flyway, Jackson 2 bridge, cache, actuator) → full build, integration, and e2e verification. Reopening
does not require revisiting the direction, only the effort.

## Consequences

- The project stays on a known-good Spring Boot 3.5 baseline with a working build and test suite.
- The reference stack lags the current major; a future migration is a substantial, dedicated effort rather than a bump.
- The testcontainers artifact-name and other low-risk fixes found during the attempt can be landed independently.
