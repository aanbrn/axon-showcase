# showcase/quality/dependency-management

## ADDED Requirements

### Requirement: spring-data-opensearch major updates are suppressed until the Spring Boot 4 migration

The shipped major-disabled configuration SHALL include the exact coordinates `org.opensearch.client:
spring-data-opensearch`, `org.opensearch.client:spring-data-opensearch-starter`, and `org.opensearch.client:
spring-data-opensearch-testcontainers`: spring-data-opensearch 3.x is built on the Spring Data 2025.1 train and
Spring Framework 7, which belong with the deferred Spring Boot 4 migration (per ADR-0004). Minor and patch updates for
these coordinates SHALL remain reported. The suppression SHALL NOT cover `opensearch-java` or `opensearch-rest-client`
(the transport clients in the same group), which are independent and remain reported for their majors.

#### Scenario: spring-data-opensearch major jump is suppressed

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of `org.opensearch.client:
  spring-data-opensearch` (or its `-starter`/`-testcontainers` variants) whose major exceeds the current major (3.x vs
  2.x) is available
- **THEN** the report does not list that major-jump update

#### Scenario: spring-data-opensearch minor and patch updates stay visible

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a 2.x (same-major) version of `org.opensearch.client:
  spring-data-opensearch` is available
- **THEN** the report lists that 2.x update

#### Scenario: transport client majors remain reported

- **WHEN** a developer runs `./gradlew dependencyUpdates` and a candidate version of `org.opensearch.client:
  opensearch-java` or `org.opensearch.client:opensearch-rest-client` whose major exceeds the current major is available
- **THEN** the report lists that major-jump update