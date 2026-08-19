# showcase/resilience4j-extension Specification

## Purpose
Documents the behavior of the Resilience4j extension: a Spring Boot `AutoConfigurationImportFilter` that provides
hierarchical, property-driven control over which Resilience4j feature auto-configurations are imported, enabling a
master enable/disable switch and per-feature toggles.

## Requirements
### Requirement: Master Resilience4j enablement flag

The system SHALL provide a `resilience4j.enabled` property (default `true`) that acts as a master switch for all
Resilience4j auto-configuration imports. When set to `false`, all Resilience4j feature auto-configurations SHALL be
excluded from the Spring Boot auto-configuration import process, regardless of individual feature flag values.

#### Scenario: Master switch enabled (default)

- **WHEN** `resilience4j.enabled` is not set or is set to `true`
- **THEN** Resilience4j feature auto-configurations are eligible for import, subject to their individual feature flags

#### Scenario: Master switch disabled

- **WHEN** `resilience4j.enabled` is set to `false`
- **THEN** all Resilience4j feature auto-configurations are excluded from import, regardless of individual feature
  flag values

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

### Requirement: Auto-configuration class matching by regex

The system SHALL identify Resilience4j auto-configuration classes by matching their fully qualified class names
against feature-specific regex patterns. Classes that do not match any feature pattern SHALL pass through
unfiltered (i.e., remain eligible for import).

#### Scenario: Non-Resilience4j auto-configuration class

- **WHEN** an auto-configuration class name does not match any Resilience4j feature pattern
- **THEN** the class remains eligible for import (the filter does not affect it)

#### Scenario: Resilience4j circuit breaker auto-configuration class

- **WHEN** an auto-configuration class name matches the circuit breaker pattern (e.g.,
  `io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration`)
- **THEN** the class's import eligibility is determined by the master flag and `resilience4j.circuitbreaker.enabled`

### Requirement: SPI registration via spring.factories

The system SHALL register the `AutoConfigurationImportFilter` implementation via the
`META-INF/spring.factories` resource under the
`org.springframework.boot.autoconfigure.AutoConfigurationImportFilter` key, ensuring the filter is discovered by
Spring Boot's auto-configuration mechanism.

#### Scenario: Filter is discovered by Spring Boot

- **WHEN** a Spring Boot application starts with the `showcase-resilience4j-extension` module on the classpath
- **THEN** the `AutoConfigurationImportFilter` is instantiated and invoked during the auto-configuration import
  process

### Requirement: Environment injection via EnvironmentAware

The system SHALL obtain the Spring `Environment` via the `EnvironmentAware` callback to resolve Resilience4j feature
flags. The filter SHALL fail fast with an `IllegalStateException` if the environment has not been injected when
`match` is invoked.

#### Scenario: Environment is injected before match is called

- **WHEN** Spring Boot instantiates the filter and calls `setEnvironment` before invoking `match`
- **THEN** the filter resolves all feature flags from the injected environment

#### Scenario: Environment is not injected

- **WHEN** `match` is invoked before `setEnvironment` has been called
- **THEN** the filter throws an `IllegalStateException` with the message `"environment" is required`

### Requirement: Spring configuration metadata for IDE support

The system SHALL declare all six properties (`resilience4j.enabled`, `resilience4j.bulkhead.enabled`,
`resilience4j.timelimiter.enabled`, `resilience4j.ratelimiter.enabled`, `resilience4j.circuitbreaker.enabled`,
`resilience4j.retry.enabled`) in `META-INF/additional-spring-configuration-metadata.json` with type
`java.lang.Boolean` and `defaultValue` of `true`, enabling IDE auto-completion and documentation in
`application.properties`/`application.yml` files.

#### Scenario: IDE auto-completion for resilience4j properties

- **WHEN** a developer edits `application.yml` in an IDE with Spring Boot configuration metadata support
- **THEN** all six `resilience4j.*.enabled` properties are suggested with their descriptions and default values

### Requirement: Module dependency exposure

The `showcase-resilience4j-extension` module SHALL be consumed as an `implementation` dependency by client modules
(e.g., `showcase-command-client`, `showcase-query-client`). The module's types SHALL not be transitively exposed on
the consumer's API classpath; the filter operates transparently at Spring Boot startup without requiring consumers
to reference any class from the extension directly.

#### Scenario: Client module depends on resilience4j-extension via implementation

- **WHEN** a module declares `implementation(project(":showcase-resilience4j-extension"))` (e.g.,
  `showcase-command-client`)
- **THEN** the `AutoConfigurationImportFilter` is on the runtime classpath and discovered by Spring Boot, but no
  extension types are exposed on the consumer's API classpath
