## MODIFIED Requirements

### Requirement: Health and readiness probes

Each service Deployment SHALL configure startup, liveness, and readiness probes on the management port against the
Spring Boot actuator endpoints, all gated by their `enabled` flags (default true); the projection service's startup
probe SHALL target the liveness endpoint, because the root `/actuator/health` endpoint aggregates the projector's
indicator, and a startup probe that reports a retrying projector would restart a container that has not yet subscribed.

#### Scenario: Startup probe checks the health endpoint

- **WHEN** a service's startup probe is enabled
- **THEN** the container is probed with `GET /actuator/health` on the management port — except the projection service,
  which is probed with `GET /actuator/health/liveness`

#### Scenario: Liveness probe checks the liveness endpoint

- **WHEN** a service's liveness probe is enabled
- **THEN** the container is probed with `GET /actuator/health/liveness` on the management port

#### Scenario: Readiness probe checks the readiness endpoint

- **WHEN** a service's readiness probe is enabled
- **THEN** the container is probed with `GET /actuator/health/readiness` on the management port
