# Spec Delta

## Purpose

Documents the gateway's client-telemetry ingestion endpoint, through which the standalone web UI reports browser-side
Core Web Vitals and JavaScript errors for Prometheus to expose.

## ADDED Requirements

### Requirement: Client telemetry ingestion endpoint

The gateway SHALL expose `POST /telemetry`, accepting a JSON report of browser-side Core Web Vitals and JavaScript
errors, and SHALL respond `204 No Content` once the report is recorded. An omitted or `null` vitals or errors list SHALL
be treated as empty. Recording SHALL NOT dispatch a command or a query, so a browser report never touches the command or
query side.

#### Scenario: A valid report returns 204

- **WHEN** a `POST /telemetry` request carries a well-formed report
- **THEN** the gateway responds with `204 No Content`

#### Scenario: An omitted or null list is treated as empty

- **WHEN** a `POST /telemetry` request omits a vitals or errors list or carries it as `null`
- **THEN** the gateway treats it as an empty list and records the report's other content

#### Scenario: Recording a report dispatches nothing

- **WHEN** a report is recorded
- **THEN** no command and no query is dispatched, even though the command and query sides are available

### Requirement: Client telemetry is recorded as bounded metrics

The gateway SHALL record a report as metrics the management endpoint exposes: each Core Web Vital as a value in a
distribution and each JavaScript error as an increment of a counter, tagged only with a bounded set of labels (the vital
name, its rating, the normalized error type, and the normalized route). Free-form client strings (an error message or
source) SHALL NOT become metric labels.

#### Scenario: Vitals are recorded with bounded labels

- **WHEN** a report carries Core Web Vitals
- **THEN** each vital's value is recorded in the distribution, tagged by vital name, rating, and route

#### Scenario: Errors are counted with bounded labels

- **WHEN** a report carries JavaScript errors
- **THEN** the error counter is incremented, tagged by error type and route

#### Scenario: Free-form text is not a metric label

- **WHEN** a report carries an error message or source
- **THEN** the recorded metrics carry no label whose value comes from that free-form text

### Requirement: Client telemetry labels are normalized

The gateway SHALL normalize a report's client-supplied labels before they are recorded, so a forged or custom value
cannot create unbounded label values: the route's query string SHALL be dropped and a route outside the known
application routes recorded as a fixed `other`; the error type SHALL be folded to a known error name, with an
unrecognized type recorded as a fixed `Error`.

#### Scenario: The query string is dropped

- **WHEN** a report's route carries a query string
- **THEN** the recorded label holds the route without its query string

#### Scenario: An unknown route is bucketed

- **WHEN** a report's route is not one of the known application routes
- **THEN** the recorded label is the fixed `other` value

#### Scenario: An unrecognized error type is bucketed

- **WHEN** a report carries an error whose type is not a known error name
- **THEN** the recorded label is the fixed `Error` value

### Requirement: Malformed client telemetry is rejected

The gateway SHALL reject a malformed report — more vitals or errors than allowed, an unknown vital name or rating, a
negative value, or a field longer than its cap — with a `400 Bad Request` problem detail carrying a `bodyErrors` map,
and SHALL record nothing from the rejected request.

#### Scenario: A malformed report is rejected

- **WHEN** a `POST /telemetry` request carries more vitals or errors than allowed, an unknown vital name or rating, a
  negative value, or an over-long field
- **THEN** the gateway responds with a `400 Bad Request` problem detail carrying a `bodyErrors` map

#### Scenario: A rejected report records nothing

- **WHEN** a report is rejected
- **THEN** no vital value and no error count is recorded from it
