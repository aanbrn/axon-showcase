# Spec Delta

## ADDED Requirements

### Requirement: Each JVM service bakes its build identity

Each JVM service's build SHALL generate Spring Boot build information (`META-INF/build-info.properties`) carrying the
project version, with the build time excluded so the generation task stays cacheable across builds.

#### Scenario: The build carries the project version

- **WHEN** a JVM service is built
- **THEN** its artifact carries `META-INF/build-info.properties` reporting the project version

#### Scenario: No build time is recorded

- **WHEN** a JVM service is built
- **THEN** its build information records no build time, so the generation task is cacheable rather than differing on
  every build

### Requirement: Each JVM service reports its build identity at runtime

Each JVM service SHALL expose the actuator `info` endpoint on its management port, so a running service reports the
build version it was built from.

#### Scenario: The info endpoint reports the build version

- **WHEN** a running JVM service's management endpoint `/actuator/info` is requested
- **THEN** the response reports the build version the service was built from

#### Scenario: The info endpoint is reachable on the management port

- **WHEN** a JVM service is running
- **THEN** the `info` endpoint is among its exposed management endpoints, reachable on the management port
