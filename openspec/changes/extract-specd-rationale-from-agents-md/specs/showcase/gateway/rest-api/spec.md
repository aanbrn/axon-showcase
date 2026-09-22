## MODIFIED Requirements

### Requirement: Command error translation

The system SHALL route every controller method on the bounded-elastic scheduler, so that request validation can resolve,
and SHALL map command failures to structured problem details with the error code: `INVALID_COMMAND` to 400 Bad Request
with a `fieldErrors` map, `NOT_FOUND` to 404 Not Found, and `TITLE_IN_USE` or `ILLEGAL_STATE` to 409 Conflict. The
routing SHALL NOT be removed as redundant: without it an invalid payload cannot be validated against its constraints and
is rejected as a `500 Internal Server Error` rather than the `400 Bad Request` above.

#### Scenario: Invalid command produces 400 with field errors

- **WHEN** a command fails with error code `INVALID_COMMAND`
- **THEN** the system responds with a `400 Bad Request` problem detail carrying the error message, the error code, and a
  `fieldErrors` map

#### Scenario: Unknown showcase produces 404

- **WHEN** a command fails with error code `NOT_FOUND`
- **THEN** the system responds with a `404 Not Found` problem detail carrying the error message and the error code

#### Scenario: Conflict produces 409

- **WHEN** a command fails with error code `TITLE_IN_USE` or `ILLEGAL_STATE`
- **THEN** the system responds with a `409 Conflict` problem detail carrying the error message and the error code

#### Scenario: An invalid payload is rejected as 400 only while the routing is present

- **WHEN** a command whose payload violates its constraints is submitted and controller methods run on the
  bounded-elastic scheduler
- **THEN** the system rejects it with a `400 Bad Request`
- **AND** if the routing is removed, the same submission surfaces as a `500 Internal Server Error`, because bean
  validation can no longer resolve against the request

### Requirement: Query error translation

The system SHALL route every controller method on the bounded-elastic scheduler, so that request validation can resolve,
and SHALL map query failures to structured problem details with the error code: `INVALID_QUERY` to 400 Bad Request with
a `fieldErrors` map, and `NOT_FOUND` to 404 Not Found. The routing SHALL NOT be removed as redundant: without it an
invalid query cannot be validated against its constraints and is rejected as a `500 Internal Server Error` rather than a
`400 Bad Request`. (A `NOT_FOUND` raised by the client stub is mapped without the routing, so the validation path — not
the not-found path — evidences the dependency.)

#### Scenario: Invalid query produces 400 with field errors

- **WHEN** a query fails with error code `INVALID_QUERY`
- **THEN** the system responds with a `400 Bad Request` problem detail carrying the error message, the error code, and a
  `fieldErrors` map

#### Scenario: Missing showcase produces 404

- **WHEN** a query fails with error code `NOT_FOUND`
- **THEN** the system responds with a `404 Not Found` problem detail carrying the error message and the error code

#### Scenario: An invalid query is rejected as 400 only while the routing is present

- **WHEN** a query whose payload violates its constraints is submitted and controller methods run on the bounded-elastic
  scheduler
- **THEN** the system rejects it with a `400 Bad Request`
- **AND** if the routing is removed, the same submission surfaces as a `500 Internal Server Error`, because the
  validator can no longer resolve against the request
