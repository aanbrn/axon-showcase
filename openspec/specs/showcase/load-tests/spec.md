# showcase/load-tests Specification

## Purpose
Documents the current behavior of the Gatling-based load-testing setup: the showcase simulation exercising the API
gateway, the scenario flow, the configurable injection profiles, and the per-profile pass assertions.

## Requirements
### Requirement: Simulation exercises the showcase REST API

The system SHALL provide a Gatling simulation named `ShowcaseSimulation` that exercises the API gateway over HTTP: a
list fetch, then a lifecycle where a random showcase is scheduled and, with decreasing probability, started, finished,
and removed, with polling fetches between steps.

#### Scenario: Scenario fetches the list first

- **WHEN** the simulation scenario runs
- **THEN** it first issues a `GET /showcases` request

#### Scenario: Showcase is scheduled with a 50 percent probability

- **WHEN** the scenario reaches the scheduling step
- **THEN** with a 50 percent probability a random title, future start time, and duration are generated and a
  `POST /showcases` request is sent, expecting status `201` and saving the returned `showcaseId`

#### Scenario: Started showcase is finished with a 5 percent probability

- **WHEN** the scenario has scheduled a showcase and the started showcase is polled until it reports status `STARTED`
- **THEN** a `PUT /showcases/{showcaseId}/start` request is sent expecting status `200`, and with a 5 percent
  probability the scenario continues to finish and remove the showcase

#### Scenario: Finished showcase is removed with a 5 percent probability

- **WHEN** the scenario continues past the start step and the showcase is polled until it reports status `FINISHED`
- **THEN** a `PUT /showcases/{showcaseId}/finish` request is sent expecting status `200`, and with a 5 percent
  probability the showcase is then removed via `DELETE /showcases/{showcaseId}` expecting status `200`

#### Scenario: Polling fetches retry until the expected state

- **WHEN** the scenario polls a showcase after scheduling, starting, or finishing
- **THEN** it issues `GET /showcases/{showcaseId}` requests every 500 milliseconds until the expected status is reached
  or a 5-minute window elapses

#### Scenario: Step failures stop the scenario

- **WHEN** any step in the scenario fails its status or payload checks
- **THEN** the scenario exits the block on failure

### Requirement: Configurable base URL and test type

The simulation SHALL be configurable via system properties: `baseUrl` (default `http://localhost`) for the target host
and `testType` (default `smoke`) selecting the injection profile and assertions.

#### Scenario: Default configuration targets localhost with smoke profile

- **WHEN** no system properties are provided
- **THEN** the simulation targets `http://localhost` and uses the smoke profile

#### Scenario: Custom base URL and test type are honored

- **WHEN** the `baseUrl` and `testType` system properties are provided
- **THEN** the simulation targets the given base URL and applies the profile and assertions for the given test type

### Requirement: Injection profiles

The simulation SHALL support the profiles `smoke`, `average`, `soak`, `stress`, `spike`, and `breakpoint`, each with a
defined user-injection curve; any other value SHALL fall back to the smoke profile.

#### Scenario: Smoke profile sends a fixed number of users

- **WHEN** the `testType` is `smoke` or an unknown value
- **THEN** the simulation injects three users at once

#### Scenario: Average profile ramps to 200 users per second

- **WHEN** the `testType` is `average`
- **THEN** the simulation ramps from 0 to 200 users per second over 5 minutes, holds 200 for 30 minutes, and ramps back
  to 0 over 5 minutes

#### Scenario: Soak profile sustains 200 users per second for 8 hours

- **WHEN** the `testType` is `soak`
- **THEN** the simulation ramps from 0 to 200 users per second over 5 minutes, holds 200 for 8 hours, and ramps back to
  0 over 5 minutes

#### Scenario: Stress profile ramps to 400 users per second

- **WHEN** the `testType` is `stress`
- **THEN** the simulation ramps from 0 to 400 users per second over 10 minutes, holds 400 for 30 minutes, and ramps
  back to 0 over 5 minutes

#### Scenario: Spike profile bursts 4000 users

- **WHEN** the `testType` is `spike`
- **THEN** the simulation bursts to 4000 users over 2 minutes and ramps back to 0 over 1 minute

#### Scenario: Breakpoint profile ramps to 40000 users over 2 hours

- **WHEN** the `testType` is `breakpoint`
- **THEN** the simulation ramps from 0 to 40000 users per second over 2 hours

### Requirement: Pass assertions

The simulation SHALL assert on global results per profile: the performance profiles assert response-time and success
percentiles, and the smoke profile asserts zero failed requests.

#### Scenario: Performance profiles assert response times and success rate

- **WHEN** the `testType` is `average`, `stress`, `spike`, `breakpoint`, or `soak`
- **THEN** the simulation asserts a mean response time at most 100 milliseconds, a 95th percentile at most 500
  milliseconds, a 99th percentile at most 1000 milliseconds, and at least 99.99 percent successful requests

#### Scenario: Smoke profile asserts zero failures

- **WHEN** the `testType` is `smoke` or an unknown value
- **THEN** the simulation asserts zero failed requests

### Requirement: Protocol configuration

The simulation SHALL send requests over HTTP with a `Host` header of `axon-showcase` and shared connections.

#### Scenario: Requests carry the configured host header

- **WHEN** the simulation sends requests
- **THEN** each request carries a `Host` header of `axon-showcase`

#### Scenario: Connections are shared

- **WHEN** the simulation sends requests
- **THEN** it shares connections across users
