## MODIFIED Requirements

### Requirement: Pass assertions

The simulation SHALL assert on the read and write request results per profile — the performance profiles that sit at or
below the knee-rate (`average`, `stress`, `soak`) assert response-time percentiles derived from the recorded baseline
and a fixed success rate, the smoke profile asserts zero failed requests, and the baseline profile asserts the
configured response-time percentiles and success rate over its plateau. The `calibrate` profile SHALL carry no pass
assertions, because its purpose is to measure rather than to gate, and the above-knee profiles (`spike`, `breakpoint`)
SHALL carry none, because their purpose is to probe the ceiling. The assertions SHALL NOT include the SSE streams'
long-lived connection times, but every profile that carries assertions SHALL also assert that the SSE event check has no
failed events, so a stalled event stream fails the run rather than passing silently. When the baseline reference is
unreadable, does not cover a request, or records a target other than the one configured, the performance profiles SHALL
assert the absolute thresholds for it instead — a mean response time at most 100 milliseconds, a 95th percentile at most
500 milliseconds, and a 99th percentile at most 1000 milliseconds.

#### Scenario: Performance profiles assert response times and success rate

- **WHEN** the `profile` is `average`, `stress`, or `soak`
- **THEN** the simulation asserts, for each read and write request, a mean response time, a 95th percentile, and a 99th
  percentile at most the derived threshold — the recorded baseline value for that request and percentile times the
  configured factor, floored at the configured floor — and at least 99.99 percent successful requests

#### Scenario: An unusable baseline falls back to the absolute thresholds

- **WHEN** no baseline reference is readable, it does not cover a request, or it records a target other than the
  configured one
- **THEN** the performance profiles assert the absolute thresholds for it (a mean at most 100 milliseconds, a 95th
  percentile at most 500 milliseconds, and a 99th percentile at most 1000 milliseconds)

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

### Requirement: Configurable target, profile, rate, ratio, and duration

The simulation SHALL be configurable via system properties: `baseUrl` for the target host, `profile` for the injection
curve and assertions, `rate` for the total workload rate in units per second (a unit is a read iteration — the list
fetch, and a showcase fetch for the configured detail share — or a write-lifecycle completion), `ratio` for the read
share of that rate, `duration` for the run's plateau or ramp length, a count for the SSE connections, `kneeRate` for the
reference rate the performance profiles scale from, `thinkTime` for the pause between a stream's actions, `detailShare`,
`startShare`, and `finishShare` for the mix, `hold` for the soak profile's plateau length, and `baselineFile` for the
committed baseline reference the performance profiles derive their thresholds from. The default `baseUrl` SHALL be the
local cluster's gateway ingress hostname, the default `profile` SHALL be `smoke`, the default `ratio` SHALL be
three-quarters reads, the default `rate` SHALL be 100 workload units per second, the default `duration` SHALL be 10
minutes, the default `kneeRate` SHALL be 200 workload units per second, the default `thinkTime` SHALL be one second, the
default `detailShare` SHALL be `0.15`, the default `startShare` SHALL be `0.6`, the default `finishShare` SHALL be
`0.5`, the default `hold` SHALL be 2 hours, and the default `baselineFile` SHALL be the module's baseline reference,
whose absence falls back to the absolute thresholds.

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

### Requirement: A baseline run reports its measurements

A `baseline` run SHALL produce a report of its measurement: the target's shape, the calibration knee, the operating
point and duration, the plateau's response times, and the per-service resource usage during the plateau. It SHALL also
record the plateau's response times per read and write request, with the derivation policy and the target it measured,
as the committed baseline reference the performance profiles assert against, and write a dated record of the run,
identified by the environment it measured when that is not the default, ready to be annotated and committed. The report
SHALL take the calibration knee from the `calibrate` run's written output rather than re-deriving it at run time. When
the calibration finds no sustained departure, the report SHALL state that the knee was not measured and give the
operating point as a fraction of the calibration ceiling.

#### Scenario: The run writes a report

- **WHEN** a `baseline` run completes
- **THEN** it writes a report describing the run under the module's build output

#### Scenario: The run records the baseline reference

- **WHEN** a `baseline` run completes
- **THEN** it writes the baseline reference — the plateau's response times per read and write request, with the
  derivation policy and the target measured — for the performance profiles to assert against

#### Scenario: The run writes a committable record

- **WHEN** a `baseline` run completes
- **THEN** it writes a dated record of the run's numbers under `docs/load-tests/`, identified by the environment it
  measured when that is not the default, ready to be annotated and committed

#### Scenario: The report names the run's method and numbers

- **WHEN** the report is written
- **THEN** it states the target shape, the calibration knee, the operating point, the plateau duration, the plateau's
  response times, and the per-service resource usage

#### Scenario: An unmeasured knee is labeled as the ceiling

- **WHEN** the calibration finds no sustained departure
- **THEN** the report states that the knee was not measured and gives the operating point as a fraction of the
  calibration ceiling
