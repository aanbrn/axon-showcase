## MODIFIED Requirements

### Requirement: Pass assertions

The simulation SHALL assert on the read and write request results per profile — the performance profiles that sit at or
below the knee-rate (`average`, `stress`, `soak`) assert response-time and success percentiles, the smoke profile
asserts zero failed requests, and the baseline profile asserts the configured response-time percentiles and success rate
over its plateau. The `calibrate` profile SHALL carry no pass assertions, because its purpose is to measure rather than
to gate, and the above-knee profiles (`spike`, `breakpoint`) SHALL carry none, because their purpose is to probe the
ceiling. The assertions SHALL NOT include the SSE streams' long-lived connection times.

#### Scenario: Performance profiles assert response times and success rate

- **WHEN** the `profile` is `average`, `stress`, or `soak`
- **THEN** the simulation asserts a mean response time at most 100 milliseconds, a 95th percentile at most 500
  milliseconds, a 99th percentile at most 1000 milliseconds, and at least 99.99 percent successful requests

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
- **THEN** the simulation bursts to `1.5×` the knee-rate users over 2 minutes and ramps back to 0 over 1 minute

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
