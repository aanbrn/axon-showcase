# Spec Delta

## MODIFIED Requirements

### Requirement: Simulation exercises the SSE event stream

The simulation SHALL hold `/events` connections open against the API gateway like browser tabs, assert that showcase
events arrive for the write lifecycle, and keep the connection open across a quiet period. For a profile whose
write-lifecycle stream runs for the connection's hold, it SHALL also check for a further showcase event during the hold,
and assert that failure for every profile that carries pass assertions, so a stream that stalls after its first event
fails rather than passing silently. When the run is handed the identities of showcases published before it started — the
deployment smoke's live-event wait — it SHALL NOT count those showcases' events toward either check, so a buffered event
the run did not produce cannot satisfy one.

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

#### Scenario: A pre-run showcase's event does not satisfy the checks

- **WHEN** the run is given warm-up showcases' identities and the gateway replays only those showcases' events
- **THEN** neither the initial check nor the mid-hold check counts them, and the check fails when no event the run
  produced arrives

### Requirement: Configurable target, profile, rate, ratio, and duration

The simulation SHALL be configurable via system properties: `baseUrl` for the target host, `profile` for the injection
curve and assertions, `rate` for the total workload rate in units per second (a unit is a read iteration — the list
fetch, and a showcase fetch for the configured detail share — or a write-lifecycle completion), `ratio` for the read
share of that rate, `duration` for the run's plateau or ramp length, `sseConnections` for the SSE connection count
(default 10), `kneeRate` for the reference rate the performance profiles scale from, `thinkTime` for the pause between a
stream's actions, `detailShare`, `startShare`, and `finishShare` for the mix, `hold` for the soak profile's plateau
length, `baselineFile` for the committed baseline reference the performance profiles derive their thresholds from, and
`warmUpShowcaseIds` for a comma-separated list of showcases published before the run started, whose events neither SSE
check counts (unset by default, which counts every event). The default `baseUrl` SHALL be the local cluster's gateway
ingress hostname, the default `profile` SHALL be `smoke`, the default `ratio` SHALL be three-quarters reads, the default
`rate` SHALL be 100 workload units per second, the default `duration` SHALL be 10 minutes, the default `kneeRate` SHALL
be 200 workload units per second, the default `thinkTime` SHALL be one second, the default `detailShare` SHALL be
`0.15`, the default `startShare` SHALL be `0.6`, the default `finishShare` SHALL be `0.5`, the default `hold` SHALL be 2
hours, and the default `baselineFile` SHALL be the module's baseline reference, whose absence falls back to the absolute
thresholds. The `gatlingRun` task exposes each property as a Gradle property of the same name (`-Pprofile=smoke`,
`-PbaseUrl`, `-PsseConnections`, and so on), which `load-tests/build.gradle.kts` forwards into the simulation's system
properties.

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

#### Scenario: The baseline reference is configurable

- **WHEN** the `baselineFile` system property is provided
- **THEN** the simulation derives the performance profiles' thresholds from that reference when it records the
  configured target, and falls back to the absolute thresholds otherwise

#### Scenario: The mix shares are configurable

- **WHEN** the `detailShare`, `startShare`, or `finishShare` system properties are provided
- **THEN** the read stream's detail share and the write stream's start and finish shares use those values

#### Scenario: The think time is configurable

- **WHEN** the `thinkTime` system property is provided
- **THEN** each stream pauses for it between its actions

#### Scenario: The soak hold is configurable

- **WHEN** the `hold` system property is provided
- **THEN** the soak profile sustains its plateau for that duration

#### Scenario: The warm-up showcases are configurable

- **WHEN** the `warmUpShowcaseIds` system property is provided
- **THEN** both SSE checks exclude those showcases' events, and with it unset the checks count every event as before
