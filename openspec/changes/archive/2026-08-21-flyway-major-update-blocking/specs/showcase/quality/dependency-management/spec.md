# showcase/quality/dependency-management

## ADDED Requirements

### Requirement: Flyway major updates are suppressed until the Spring Boot 4 migration

The shipped major-disabled configuration SHALL include `org.flywaydb` as a group prefix: Flyway major bumps (12.x,
13.x) belong with the deferred Spring Boot 4 migration, because Spring Boot 3.5 (the current baseline, per ADR-0004)
manages Flyway 11.x, and even Spring Boot 4.0 manages Flyway 11.x — Flyway major 12 only appears with Spring Boot 4.1.
Minor and patch updates for these coordinates SHALL remain reported.

#### Scenario: Flyway major jump is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of `org.flywaydb:flyway-core` or
  `org.flywaydb:flyway-database-postgresql` whose major exceeds the current major (12.x/13.x vs 11.x) is available
- **THEN** the report does not list that major-jump update

#### Scenario: Flyway minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 11.x (same-major) version of
  `org.flywaydb:flyway-core` is available
- **THEN** the report lists that 11.x update