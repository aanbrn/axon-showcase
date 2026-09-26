# showcase/quality/load-tests Specification

## Purpose

Documents the Gatling-based load-testing setup: the showcase simulation's three concurrent streams — a read stream, a
write-lifecycle stream, and an SSE stream — driving the deployed API gateway, the configurable injection profiles and
their pass assertions, and the calibration/baseline measurement with the report it produces.

## Requirements

### Requirement: Injection profiles

The simulation SHALL support the profiles `smoke`, `average`, `soak`, `stress`, `spike`, and `breakpoint`, each with a
defined injection curve applied to the mixed read/write workload; any other value SHALL fall back to the smoke profile.
It SHALL also support a `calibrate` profile that ramps the mixed workload to find the load knee, and a `baseline`
profile that holds a constant plateau at a configured operating point for a configured duration.

#### Scenario: Smoke profile sends a fixed number of users

- **WHEN** the `profile` is `smoke` or an unknown value
- **THEN** the simulation injects three users at once

#### Scenario: Average profile ramps to 200 users per second

- **WHEN** the `profile` is `average`
- **THEN** the simulation ramps from 0 to 200 users per second over 5 minutes, holds 200 for 30 minutes, and ramps back
  to 0 over 5 minutes

#### Scenario: Soak profile sustains 200 users per second for 8 hours

- **WHEN** the `profile` is `soak`
- **THEN** the simulation ramps from 0 to 200 users per second over 5 minutes, holds 200 for 8 hours, and ramps back to
  0 over 5 minutes

#### Scenario: Stress profile ramps to 400 users per second

- **WHEN** the `profile` is `stress`
- **THEN** the simulation ramps from 0 to 400 users per second over 10 minutes, holds 400 for 30 minutes, and ramps back
  to 0 over 5 minutes

#### Scenario: Spike profile bursts 4000 users

- **WHEN** the `profile` is `spike`
- **THEN** the simulation bursts to 4000 users over 2 minutes and ramps back to 0 over 1 minute

#### Scenario: Breakpoint profile ramps to 40000 users over 2 hours

- **WHEN** the `profile` is `breakpoint`
- **THEN** the simulation ramps from 0 to 40000 users per second over 2 hours

#### Scenario: Calibrate profile finds the load knee

- **WHEN** the `profile` is `calibrate`
- **THEN** the simulation ramps the mixed workload, and the run's written log records the knee — the workload rate at
  which the response time departs the flat baseline — for the wrapper to derive

#### Scenario: Baseline profile holds a plateau at the operating point

- **WHEN** the `profile` is `baseline`
- **THEN** the simulation holds a constant plateau at the configured operating point (a workload rate below the knee)
  for the configured duration and reports the plateau's statistics

### Requirement: Pass assertions

The simulation SHALL assert on the read and write request results per profile — the performance profiles assert
response-time and success percentiles, the smoke profile asserts zero failed requests, and the baseline profile asserts
the configured response-time percentiles and success rate over its plateau. The `calibrate` profile SHALL carry no pass
assertions, because its purpose is to measure rather than to gate. The assertions SHALL NOT include the SSE streams'
long-lived connection times.

#### Scenario: Performance profiles assert response times and success rate

- **WHEN** the `profile` is `average`, `stress`, `spike`, `breakpoint`, or `soak`
- **THEN** the simulation asserts a mean response time at most 100 milliseconds, a 95th percentile at most 500
  milliseconds, a 99th percentile at most 1000 milliseconds, and at least 99.99 percent successful requests

#### Scenario: Smoke profile asserts zero failures

- **WHEN** the `profile` is `smoke` or an unknown value
- **THEN** the simulation asserts zero failed requests

#### Scenario: Baseline profile asserts the configured thresholds hold

- **WHEN** the `profile` is `baseline`
- **THEN** the simulation asserts zero failed requests and the configured response-time percentiles over the plateau

#### Scenario: Calibrate profile asserts nothing

- **WHEN** the `profile` is `calibrate`
- **THEN** the simulation reports the run's results without a pass assertion

### Requirement: Protocol configuration

The simulation SHALL send requests over HTTP with a `Host` header derived from the configured target and shared
connections.

#### Scenario: Requests carry the configured host header

- **WHEN** the simulation sends requests
- **THEN** each request carries the host of the configured `baseUrl`

#### Scenario: Connections are shared

- **WHEN** the simulation sends requests
- **THEN** it shares connections across users

### Requirement: Simulation exercises the gateway's read and write streams

The simulation SHALL run a read stream and a write-lifecycle stream against the API gateway. The read stream SHALL fetch
the showcase list and individual showcases; the write-lifecycle stream SHALL schedule a showcase and carry it through
start, finish, and removal with polling between steps. The two streams SHALL be injected at rates derived from the
configured total rate and read share — the read stream at the read share of the total and the write-lifecycle stream at
the remainder — so their split is the configured ratio.

#### Scenario: Read stream fetches the list and a showcase

- **WHEN** the read stream runs
- **THEN** it issues `GET /showcases` and `GET /showcases/{showcaseId}` requests and checks their status

#### Scenario: Write stream schedules a showcase

- **WHEN** the write stream runs
- **THEN** it sends `POST /showcases` with a generated title, start time, and duration, expects status `201`, and saves
  the returned `showcaseId`

#### Scenario: Write stream polls until queryable then starts

- **WHEN** the write stream has scheduled a showcase
- **THEN** it polls `GET /showcases/{showcaseId}` until the response is `200`, then sends
  `PUT /showcases/{showcaseId}/start` expecting status `200`

#### Scenario: Write stream polls until STARTED then finishes

- **WHEN** the write stream has started a showcase
- **THEN** it polls `GET /showcases/{showcaseId}` until the showcase reports status `STARTED`, then sends
  `PUT /showcases/{showcaseId}/finish` expecting status `200`

#### Scenario: Write stream polls until FINISHED then removes

- **WHEN** the write stream has finished a showcase
- **THEN** it polls `GET /showcases/{showcaseId}` until the showcase reports status `FINISHED`, then sends
  `DELETE /showcases/{showcaseId}` expecting status `200`

#### Scenario: Polling retries until the expected state

- **WHEN** the write stream polls a showcase after scheduling, starting, or finishing
- **THEN** it re-issues the poll every 500 milliseconds until the expected HTTP status or showcase status is reached or
  a 5-minute window elapses

#### Scenario: The streams run concurrently

- **WHEN** the simulation runs
- **THEN** the read and write streams are injected together rather than in sequence

#### Scenario: The streams are injected in the configured ratio

- **WHEN** the total rate and read share are configured
- **THEN** the read stream is injected at the read share of the rate and the write-lifecycle stream at the remainder
  (default three-quarters reads, one-quarter write-lifecycle completions)

#### Scenario: A failing step stops its stream

- **WHEN** a step in a stream fails its status or payload checks
- **THEN** that stream exits the block on failure

### Requirement: Simulation exercises the SSE event stream

The simulation SHALL hold `/events` connections open against the API gateway like browser tabs, assert that showcase
events arrive for the write lifecycle, and keep the connection open across a quiet period.

#### Scenario: SSE stream connects to the event stream

- **WHEN** the SSE stream runs
- **THEN** it opens an SSE connection to `/events` and expects it to be established

#### Scenario: SSE stream observes events for a write lifecycle

- **WHEN** the SSE stream is connected while a write-lifecycle stream schedules, starts, finishes, or removes a showcase
- **THEN** the SSE stream receives the corresponding showcase events (the gateway replays its buffered events on connect
  and streams live ones)

#### Scenario: SSE stream stays open across a quiet period

- **WHEN** no showcase event occurs for longer than the keep-alive interval
- **THEN** the SSE connection remains open and receives the keep-alive

#### Scenario: SSE stream closes cleanly

- **WHEN** the SSE stream finishes
- **THEN** it closes the connection

### Requirement: Configurable target, profile, rate, ratio, and duration

The simulation SHALL be configurable via system properties: `baseUrl` for the target host, `profile` for the injection
curve and assertions, `rate` for the total workload rate in units per second (a unit is a read iteration — the list
fetch and a showcase fetch — or a write-lifecycle completion), `ratio` for the read share of that rate, `duration` for
the run's plateau or ramp length, and a count for the SSE connections. The default `baseUrl` SHALL be the local
cluster's gateway ingress hostname, the default `profile` SHALL be `smoke`, the default `ratio` SHALL be three-quarters
reads, the default `rate` SHALL be 100 workload units per second, and the default `duration` SHALL be 10 minutes.

#### Scenario: Default configuration targets the local gateway

- **WHEN** no system properties are provided
- **THEN** the simulation targets the local cluster's gateway ingress hostname and uses the smoke profile, the
  three-quarter read share, the 100-unit-per-second rate, and the 10-minute duration

#### Scenario: Custom target and profile are honored

- **WHEN** the `baseUrl` and `profile` system properties are provided
- **THEN** the simulation targets the given base URL and applies the profile and assertions for the given profile

#### Scenario: The total rate and read share are configurable

- **WHEN** the `rate` and `ratio` system properties are provided
- **THEN** the simulation injects the read and write streams at those rates split by that read share

#### Scenario: The duration is configurable

- **WHEN** the `duration` system property is provided
- **THEN** the simulation holds the plateau (or runs the calibration ramp) for that duration

#### Scenario: The SSE connection count is configurable

- **WHEN** the SSE connection-count system property is provided
- **THEN** the simulation opens that many `/events` connections

### Requirement: A baseline run reports its measurements

A `baseline` run SHALL produce a report of its measurement: the target's shape, the calibration knee, the operating
point and duration, the plateau's response times, and the per-service resource usage during the plateau. The report
SHALL take the calibration knee from the `calibrate` run's written output rather than re-deriving it at run time. When
the calibration finds no sustained departure, the report SHALL state that the knee was not measured and give the
operating point as a fraction of the calibration ceiling.

#### Scenario: The run writes a report

- **WHEN** a `baseline` run completes
- **THEN** it writes a report describing the run under the module's build output

#### Scenario: The report names the run's method and numbers

- **WHEN** the report is written
- **THEN** it states the target shape, the calibration knee, the operating point, the plateau duration, the plateau's
  response times, and the per-service resource usage

#### Scenario: An unmeasured knee is labeled as the ceiling

- **WHEN** the calibration finds no sustained departure
- **THEN** the report states that the knee was not measured and gives the operating point as a fraction of the
  calibration ceiling
