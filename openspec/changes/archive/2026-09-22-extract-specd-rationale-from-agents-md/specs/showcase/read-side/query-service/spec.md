## MODIFIED Requirements

### Requirement: Query validation

The system SHALL route every controller method on the bounded-elastic scheduler, so that inbound requests can be
validated on the scheduler that carries them, and SHALL validate query payloads against bean validation constraints,
enabled by default and configurable via the `showcase.query.validation-enabled` property, and SHALL reject invalid
queries with a 400 Bad Request problem detail whose `fieldErrors` property maps each offending property path to its
violation messages. The routing SHALL NOT be removed as redundant: without it an invalid query cannot be rejected as a
`400 Bad Request` and surfaces as a `503 Service Unavailable` instead.

#### Scenario: Invalid list query is rejected with property errors

- **WHEN** a `FetchShowcaseListQuery` is dispatched whose payload violates its constraints (for example an `afterId`
  that is not a valid KSUID or a `size` outside 1 to 1000 inclusive) and validation is enabled (the default)
- **THEN** the system rejects the request with a 400 Bad Request, detail "Given query is not valid", and a `fieldErrors`
  map of each offending property path to its validation messages

#### Scenario: Invalid by-ID query is rejected with property errors

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched with a `showcaseId` that is not a valid KSUID and validation is
  enabled (the default)
- **THEN** the system rejects the request with a 400 Bad Request, detail "Given query is not valid", and a `fieldErrors`
  map of the `showcaseId` property to its validation messages

#### Scenario: Query violating constraints succeeds when validation is disabled

- **WHEN** a query is dispatched whose payload violates its constraints while `showcase.query.validation-enabled` is set
  to `false`
- **THEN** the query proceeds to handling without validation, and no 400 Bad Request is produced

#### Scenario: Rejection as 400 depends on the request routing

- **WHEN** a query whose payload violates its constraints is dispatched with validation enabled and controller methods
  run on the bounded-elastic scheduler
- **THEN** the system rejects it with a `400 Bad Request`
- **AND** if the routing is removed, the same query surfaces as a `503 Service Unavailable` instead

### Requirement: Fetch showcase by ID query

The system SHALL handle `FetchShowcaseByIdQuery`, responding with the matching showcase or a NOT_FOUND error when the
showcase is absent. The system SHALL route every controller method on the bounded-elastic scheduler, so that a query-bus
`NOT_FOUND` can reach its error translation; without that routing a missing showcase surfaces as a
`503 Service Unavailable` rather than the `404 Not Found` above.

#### Scenario: Existing showcase is returned

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched for a showcase ID that exists in the projection
- **THEN** the system responds with the showcase for that ID

#### Scenario: Missing showcase produces NOT_FOUND

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched for a showcase ID that does not exist in the projection
- **THEN** the system responds with a 404 Not Found problem detail with message "No showcase with given ID"

#### Scenario: A missing showcase is reported as 404 only while the routing is present

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched for a showcase ID that does not exist, and controller methods run on
  the bounded-elastic scheduler
- **THEN** the system responds with a `404 Not Found` problem detail
- **AND** if the routing is removed, the same query surfaces as a `503 Service Unavailable` instead
