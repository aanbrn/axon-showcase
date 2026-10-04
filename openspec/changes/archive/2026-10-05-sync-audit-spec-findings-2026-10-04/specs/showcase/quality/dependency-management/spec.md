# Spec Delta

## ADDED Requirements

### Requirement: Spring major updates are suppressed until the Spring Boot 4 migration

The shipped major-disabled configuration SHALL include `org.springframework` as a group prefix: a Spring Framework major
bump (7.x vs 6.x) belongs with the deferred Spring Boot 4 migration, because Spring Boot 3.5 (the current baseline, per
ADR-0004) manages Spring Framework 6.x, so a Spring Framework 7 migration is not actionable until that coordinated
migration is taken. The configuration's comment SHALL name the deferral and ADR-0004. Minor and patch updates for these
coordinates SHALL remain reported.

#### Scenario: Spring major jump is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of an `org.springframework:*`
  coordinate whose major exceeds the current major (7.x vs 6.x) is available
- **THEN** the report does not list that major-jump update

#### Scenario: Spring minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 6.x (same-major) version of an `org.springframework:*`
  coordinate is available
- **THEN** the report lists that 6.x update
