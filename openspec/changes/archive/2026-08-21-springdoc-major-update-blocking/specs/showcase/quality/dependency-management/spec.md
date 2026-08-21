# showcase/quality/dependency-management

## ADDED Requirements

### Requirement: springdoc major updates are suppressed until the Spring Boot 4 migration

The shipped major-disabled configuration SHALL include the exact coordinate `org.springdoc:
springdoc-openapi-starter-webflux-ui`: springdoc 3.x is built against Spring Boot 4.x (both 3.0.3 on SB 4.0 and 3.1.0
on SB 4.1) and pulls the SB4-modularized auto-configuration artifacts, which belong with the deferred Spring Boot 4
migration (per ADR-0004). Minor and patch updates for this coordinate SHALL remain reported.

#### Scenario: springdoc major jump is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of `org.springdoc:
  springdoc-openapi-starter-webflux-ui` whose major exceeds the current major (3.x vs 2.x) is available
- **THEN** the report does not list that major-jump update

#### Scenario: springdoc minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 2.x (same-major) version of `org.springdoc:
  springdoc-openapi-starter-webflux-ui` is available
- **THEN** the report lists that 2.x update