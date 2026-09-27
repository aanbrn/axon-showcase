# showcase/quality/load-tests Specification

## Purpose

Documents the Gatling-based load-testing setup: the showcase simulation's three concurrent streams — a read stream, a
write-lifecycle stream, and an SSE stream — driving the deployed API gateway, the configurable injection profiles and
their pass assertions, and the calibration/baseline measurement with the report it produces.

## Requirements

### Requirement: Pass assertions

The simulation SHALL assert on the read and write request results per profile — the performance profiles that sit at or
below the knee-rate (`average`, `stress`, `soak`) assert response-time and success percentiles, the smoke profile
asserts zero failed requests, and the baseline profile asserts the configured response-time percentiles and success rate
over its plateau. The `calibrate` profile SHALL carry no pass assertions, because its purpose is to measure rather than
to gate, and the above-knee profiles (`spike`, `breakpoint`) SHALL carry none, because their purpose is to probe the
ceiling. The assertions SHALL NOT include the SSE streams' long-lived connection times, but every profile that carries
assertions SHALL also assert that the SSE event check has no failed events, so a stalled event stream fails the run
rather than passing silently.

#### Scenario: Performance profiles assert response times and success rate

- **WHEN** the `profile` is `average`, `stress`, or `soak`
- **THEN** the simulation asserts a mean response time at most 100 milliseconds, a 95th percentile at most 500
  milliseconds, a 99th percentile at most 1000 milliseconds, and at least 99.99 percent successful requests

#### Scenario: The SSE event check's failures are asserted

- **WHEN** the `profile` is `average`, `stress`, `soak`, `baseline`, or `smoke`
- **THEN** the simulation asserts that the SSE event check has no failed events, while the connection's long-lived time
  remains excluded

#### Scenario: Smoke profile asserts zero failures

- **WHEN** the `profile` is `smoke`
- **THEN** the simulation asserts zero failed requests

#### Scenario: Baseline profile asserts the configured thresholds hold

- **WHEN** the `profile` is `baseline`
- **THEN** the simulation asserts zero failed requests and the configured response-time percentiles over the plateau

#### Scenario: Calibrate profile asserts nothing

- **WHEN** the `profile` is `calibrate`
- **THEN** the simulation reports the run's results without a pass assertion

#### Scenario: Above-knee profiles assert nothing

- **WHEN** the `profile` is `spike` or `breakpoint`
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
the showcase list on every iteration and an individual showcase for a configured share of iterations; the
write-lifecycle stream SHALL always schedule a showcase and always remove it, but start it only for a configured share
of iterations and finish it, when started, for a further configured share, polling between steps. When the list is empty
— a cold target — the read stream SHALL create one showcase, once per run, so the detail path is exercised rather than
skipped; that showcase is not removed, since it is the run's precondition rather than a lifecycle. The two streams SHALL
be injected at rates derived from the rate the profile supplies and the read share — the read stream at the read share
of that rate and the write-lifecycle stream at the remainder — so their split is the configured ratio. Each stream SHALL
pause for a configured think time between its actions.

#### Scenario: Read stream fetches the list and a showcase

- **WHEN** the read stream runs
- **THEN** it issues `GET /showcases` every iteration and issues `GET /showcases/{showcaseId}` for the configured share
  of iterations, checking their status

#### Scenario: A cold target still exercises the detail path

- **WHEN** the showcase list is empty
- **THEN** the read stream creates one showcase, once per run, and fetches its detail for the configured share of
  iterations, so the detail endpoint is exercised

#### Scenario: Write stream schedules a showcase

- **WHEN** the write stream runs
- **THEN** it sends `POST /showcases` with a generated title, start time, and duration, expects status `201`, and saves
  the returned `showcaseId`

#### Scenario: Write stream polls until queryable then starts

- **WHEN** the write stream has scheduled a showcase
- **THEN** for the configured start share of iterations (default `0.6`) it polls `GET /showcases/{showcaseId}` until the
  response is `200`, then sends `PUT /showcases/{showcaseId}/start` expecting status `200`

#### Scenario: Write stream polls until STARTED then finishes

- **WHEN** the write stream has started a showcase
- **THEN** for the configured finish share of iterations (default `0.5`) it polls `GET /showcases/{showcaseId}` until
  the showcase reports status `STARTED`, then sends `PUT /showcases/{showcaseId}/finish` expecting status `200`

#### Scenario: Write stream polls until FINISHED then removes

- **WHEN** the write stream has finished a showcase, or has only scheduled or started it
- **THEN** it polls until the showcase reports `FINISHED` when it was finished, and then sends
  `DELETE /showcases/{showcaseId}` expecting status `200`, so every lifecycle terminates and the read model stays
  bounded

#### Scenario: Polling retries until the expected state

- **WHEN** the write stream polls a showcase after scheduling, starting, or finishing
- **THEN** it re-issues the poll every 500 milliseconds until the expected HTTP status or showcase status is reached or
  a 5-minute window elapses

#### Scenario: The streams run concurrently

- **WHEN** the simulation runs
- **THEN** the read and write streams are injected together rather than in sequence

#### Scenario: The streams are injected in the configured ratio

- **WHEN** the profile's rate and the read share are configured
- **THEN** the read stream is injected at the read share of that rate and the write-lifecycle stream at the remainder
  (default three-quarters reads, one-quarter write-lifecycle completions)

#### Scenario: A failing step stops its stream

- **WHEN** a step in a stream fails its status or payload checks
- **THEN** that stream exits the block on failure

### Requirement: Simulation exercises the SSE event stream

The simulation SHALL hold `/events` connections open against the API gateway like browser tabs, assert that showcase
events arrive for the write lifecycle, and keep the connection open across a quiet period. For a profile whose
write-lifecycle stream runs for the connection's hold, it SHALL also check for a further showcase event during the hold,
and assert that failure for every profile that carries pass assertions, so a stream that stalls after its first event
fails rather than passing silently.

#### Scenario: SSE stream connects to the event stream

- **WHEN** the SSE stream runs
- **THEN** it opens an SSE connection to `/events` and expects it to be established

#### Scenario: SSE stream observes events for a write lifecycle

- **WHEN** the SSE stream is connected while a write-lifecycle stream schedules, starts, finishes, or removes a showcase
- **THEN** the SSE stream receives the corresponding showcase events (the gateway replays its buffered events on connect
  and streams live ones)

#### Scenario: SSE stream receives a further event during the hold

- **WHEN** a profile's write-lifecycle stream runs for the connection's hold
- **THEN** the SSE stream receives at least one further showcase event after its first, so delivery across the hold is
  verified, and the failure is asserted where the profile carries assertions

#### Scenario: SSE stream stays open across a quiet period

- **WHEN** no showcase event occurs for longer than the keep-alive interval
- **THEN** the SSE connection remains open and receives the keep-alive

#### Scenario: SSE stream closes cleanly

- **WHEN** the SSE stream finishes
- **THEN** it closes the connection

### Requirement: Configurable target, profile, rate, ratio, and duration

The simulation SHALL be configurable via system properties: `baseUrl` for the target host, `profile` for the injection
curve and assertions, `rate` for the total workload rate in units per second (a unit is a read iteration — the list
fetch, and a showcase fetch for the configured detail share — or a write-lifecycle completion), `ratio` for the read
share of that rate, `duration` for the run's plateau or ramp length, a count for the SSE connections, `kneeRate` for the
reference rate the performance profiles scale from, `thinkTime` for the pause between a stream's actions, `detailShare`,
`startShare`, and `finishShare` for the mix, and `hold` for the soak profile's plateau length. The default `baseUrl`
SHALL be the local cluster's gateway ingress hostname, the default `profile` SHALL be `smoke`, the default `ratio` SHALL
be three-quarters reads, the default `rate` SHALL be 100 workload units per second, the default `duration` SHALL be 10
minutes, the default `kneeRate` SHALL be 200 workload units per second, the default `thinkTime` SHALL be one second, the
default `detailShare` SHALL be `0.15`, the default `startShare` SHALL be `0.6`, the default `finishShare` SHALL be
`0.5`, and the default `hold` SHALL be 2 hours.

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

#### Scenario: The knee-rate scales the performance profiles

- **WHEN** the `kneeRate` system property is provided
- **THEN** the performance profiles scale their peaks and ramps from it (default 200 workload units per second)

#### Scenario: The mix shares are configurable

- **WHEN** the `detailShare`, `startShare`, or `finishShare` system properties are provided
- **THEN** the read stream's detail share and the write stream's start and finish shares use those values

#### Scenario: The think time is configurable

- **WHEN** the `thinkTime` system property is provided
- **THEN** each stream pauses for it between its actions

#### Scenario: The soak hold is configurable

- **WHEN** the `hold` system property is provided
- **THEN** the soak profile sustains its plateau for that duration

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

### Requirement: Knee-relative injection profiles

The simulation SHALL support the profiles `average`, `soak`, `stress`, `spike`, and `breakpoint`, each with an injection
curve expressed as a multiple of the configured knee-rate, and a `smoke` profile that injects a fixed number of users.
An unsupported profile value SHALL fail the run before any load is injected, rather than fall back to another profile.
It SHALL also support a `calibrate` profile that ramps the mixed workload to find the load knee, and a `baseline`
profile that holds a constant plateau at a configured operating point for a configured duration.

#### Scenario: Smoke profile sends a fixed number of users

- **WHEN** the `profile` is `smoke`
- **THEN** the simulation injects three users at once

#### Scenario: An unsupported profile fails the run

- **WHEN** the `profile` is not one of the supported values
- **THEN** the simulation fails the run before injecting load, naming the unsupported profile

#### Scenario: Average profile ramps to a fraction of the knee-rate

- **WHEN** the `profile` is `average`
- **THEN** the simulation ramps from 0 to `0.6×` the knee-rate over 5 minutes, holds that for 30 minutes, and ramps back
  to 0 over 5 minutes

#### Scenario: Soak profile sustains a fraction of the knee-rate for the hold

- **WHEN** the `profile` is `soak`
- **THEN** the simulation ramps from 0 to `0.6×` the knee-rate over 5 minutes, holds that for the configured hold
  (default 2 hours), and ramps back to 0 over 5 minutes

#### Scenario: Stress profile ramps to a higher fraction of the knee-rate

- **WHEN** the `profile` is `stress`
- **THEN** the simulation ramps from 0 to `0.9×` the knee-rate over 10 minutes, holds that for 30 minutes, and ramps
  back to 0 over 5 minutes

#### Scenario: Spike profile bursts above the knee-rate

- **WHEN** the `profile` is `spike`
- **THEN** the simulation bursts to and holds `1.5×` the knee-rate workload units per second for 2 minutes, and ramps
  back to 0 over 1 minute

#### Scenario: Breakpoint profile ramps past the knee-rate

- **WHEN** the `profile` is `breakpoint`
- **THEN** the simulation ramps from 0 to `1.5×` the knee-rate over 20 minutes

#### Scenario: Calibrate profile finds the load knee

- **WHEN** the `profile` is `calibrate`
- **THEN** the simulation ramps the mixed workload, and the run's written log records the knee — the workload rate at
  which the response time departs the flat baseline — for the wrapper to derive

#### Scenario: Baseline profile holds a plateau at the operating point

- **WHEN** the `profile` is `baseline`
- **THEN** the simulation holds a constant plateau at the configured operating point (a workload rate below the knee)
  for the configured duration and reports the plateau's statistics
