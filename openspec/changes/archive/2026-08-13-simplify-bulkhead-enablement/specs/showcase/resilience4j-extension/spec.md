## MODIFIED Requirements

### Requirement: Per-feature enablement flags

The system SHALL provide individual boolean properties (all default `true`) that control whether each Resilience4j
feature's auto-configuration classes are imported: `resilience4j.bulkhead.enabled`,
`resilience4j.timelimiter.enabled`, `resilience4j.ratelimiter.enabled`, `resilience4j.circuitbreaker.enabled`, and
`resilience4j.retry.enabled`. A feature's auto-configuration SHALL be imported only when both the master flag and
the feature's flag are enabled.

#### Scenario: Feature flag disabled while master is enabled

- **WHEN** `resilience4j.enabled` is `true` and `resilience4j.circuitbreaker.enabled` is set to `false`
- **THEN** all auto-configuration classes matching the circuit breaker pattern are excluded from import

#### Scenario: Feature flag enabled while master is disabled

- **WHEN** `resilience4j.enabled` is set to `false` and `resilience4j.retry.enabled` is `true`
- **THEN** retry auto-configuration classes are excluded from import because the master flag is disabled

### Requirement: Bulkhead auto-configuration gated by single flag

The system SHALL gate the bulkhead auto-configuration class import on `resilience4j.enabled` &&
`resilience4j.bulkhead.enabled`. There is no separate thread-pool-bulkhead auto-configuration class or property;
the `resilience4j.thread-pool-bulkhead.enabled` property is no longer recognized.

#### Scenario: Bulkhead flag enabled

- **WHEN** `resilience4j.enabled` is `true` and `resilience4j.bulkhead.enabled` is `true`
- **THEN** the bulkhead auto-configuration class is eligible for import

#### Scenario: Bulkhead flag disabled

- **WHEN** `resilience4j.enabled` is `true` and `resilience4j.bulkhead.enabled` is `false`
- **THEN** the bulkhead auto-configuration class is excluded from import

### Requirement: Spring configuration metadata for IDE support

The system SHALL declare all six properties (`resilience4j.enabled`, `resilience4j.bulkhead.enabled`,
`resilience4j.timelimiter.enabled`, `resilience4j.ratelimiter.enabled`, `resilience4j.circuitbreaker.enabled`,
`resilience4j.retry.enabled`) in `META-INF/additional-spring-configuration-metadata.json` with type
`java.lang.Boolean` and `defaultValue` of `true`, enabling IDE auto-completion and documentation in
`application.properties`/`application.yml` files.

#### Scenario: IDE auto-completion for resilience4j properties

- **WHEN** a developer edits `application.yml` in an IDE with Spring Boot configuration metadata support
- **THEN** all six `resilience4j.*.enabled` properties are suggested with their descriptions and default values
