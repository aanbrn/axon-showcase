## ADDED Requirements

### Requirement: The web UI's test coverage is gated by the build

The frontend test run SHALL measure the web UI's code coverage with Vitest's V8 provider and fail the standard `check`
task when the measured statement coverage falls below a minimum declared in
`config/web-ui-coverage/coverage-baseline.properties`, so a change that drops the frontend's coverage fails the build
the way the JVM modules' JaCoCo gate does.

#### Scenario: The standard check measures and gates frontend coverage

- **WHEN** the standard `check` task runs
- **THEN** it runs the frontend tests with coverage and enforces the committed statement-coverage minimum

#### Scenario: Coverage below the minimum fails the build

- **WHEN** the web UI's measured statement coverage is below the committed minimum
- **THEN** the coverage check fails and reports the measured figure against the minimum

#### Scenario: Coverage at or above the minimum passes

- **WHEN** the web UI's measured statement coverage is at or above the committed minimum
- **THEN** the coverage check passes and the coverage report is written under the module's build directory
