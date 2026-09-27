## MODIFIED Requirements

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
