## ADDED Requirements

### Requirement: Axon Framework major updates are suppressed until the framework's 5.x dependency surface exists

The shipped major-disabled configuration SHALL include `org.axonframework` as a group prefix: the framework major is
deferred because the modules and extensions the project runs on have no 5.x release — the Kafka and JGroups extensions
(event propagation and command routing) and the Micrometer metrics and OpenTelemetry tracing modules — with the Spring
Boot starter at a preview only. The deferral and its reopen trigger are recorded in ADR-0011, which the configuration's
comment SHALL name. Minor and patch updates for these coordinates SHALL remain reported.

#### Scenario: The Axon framework major is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and an Axon Framework 5.x candidate is available for a pinned
  `org.axonframework` coordinate
- **THEN** the report does not list that major-jump update

#### Scenario: Axon minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 4.x (same-major) version of an `org.axonframework`
  coordinate is available
- **THEN** the report lists that 4.x update

#### Scenario: The Axon rationale resolves

- **WHEN** a reader follows the `org.axonframework` comment in the major-disabled configuration
- **THEN** it names ADR-0011, which records the deferral and the condition that reopens the migration
