## Purpose

Documents the current behavior of the REST entry point of the CQRS showcase application: the `/showcases` command and
query endpoints, asynchronous write handling with idempotency keys, cache fallback on query failures, and structured
error mapping.

## ADDED Requirements

### Requirement: Schedule showcase endpoint

The system SHALL expose `POST /showcases` accepting a JSON request with a unique title, a future start time, and a
duration, and SHALL return `201 Created` with the generated showcase ID and a `Location` header pointing to the new
showcase, or `202 Accepted` with the idempotency key in the response header when the command times out.

#### Scenario: Successful schedule returns 201

- **WHEN** a valid `POST /showcases` request is received
- **THEN** the system responds with `201 Created`, a `Location` header starting with `/showcases/`, and a JSON body with
  the generated showcase ID

#### Scenario: Timeout returns 202 with idempotency key

- **WHEN** scheduling times out
- **THEN** the system responds with `202 Accepted` and the idempotency key in the `Idempotency-Key` response header

#### Scenario: Client-provided idempotency key is honored

- **WHEN** a `POST /showcases` request carries an `Idempotency-Key` header with a valid KSUID
- **THEN** the system schedules the showcase using that ID as the showcase ID

#### Scenario: Invalid idempotency key is rejected

- **WHEN** a `POST /showcases` request carries an `Idempotency-Key` header that is not a valid KSUID
- **THEN** the system responds with a `400 Bad Request` problem detail, detail "Invalid request.", and a `headerErrors`
  map containing the `Idempotency-Key` property

#### Scenario: Invalid schedule request is rejected

- **WHEN** a `POST /showcases` request violates its constraints (blank title, missing or past start time, or duration
  outside 1 to 10 minutes inclusive)
- **THEN** the system responds with a `400 Bad Request` problem detail, detail "Invalid request.", and a `bodyErrors`
  map of each offending field to its violation messages

### Requirement: Start showcase endpoint

The system SHALL expose `PUT /showcases/{showcaseId}/start` and SHALL return `200 OK` on success or `202 Accepted` when
the command times out.

#### Scenario: Successful start returns 200

- **WHEN** a valid `PUT /showcases/{showcaseId}/start` request is received
- **THEN** the system dispatches a start command and responds with `200 OK`

#### Scenario: Timeout returns 202

- **WHEN** starting times out
- **THEN** the system responds with `202 Accepted`

#### Scenario: Invalid showcase ID is rejected

- **WHEN** a `PUT /showcases/{showcaseId}/start` request carries a showcase ID that is not a valid KSUID
- **THEN** the system responds with a `400 Bad Request` problem detail, detail "Invalid request.", and a `pathErrors`
  map containing the `showcaseId` property

### Requirement: Finish showcase endpoint

The system SHALL expose `PUT /showcases/{showcaseId}/finish` and SHALL return `200 OK` on success or `202 Accepted`
when the command times out.

#### Scenario: Successful finish returns 200

- **WHEN** a valid `PUT /showcases/{showcaseId}/finish` request is received
- **THEN** the system dispatches a finish command and responds with `200 OK`

#### Scenario: Timeout returns 202

- **WHEN** finishing times out
- **THEN** the system responds with `202 Accepted`

#### Scenario: Invalid showcase ID is rejected

- **WHEN** a `PUT /showcases/{showcaseId}/finish` request carries a showcase ID that is not a valid KSUID
- **THEN** the system responds with a `400 Bad Request` problem detail, detail "Invalid request.", and a `pathErrors`
  map containing the `showcaseId` property

### Requirement: Remove showcase endpoint

The system SHALL expose `DELETE /showcases/{showcaseId}` and SHALL return `200 OK` on success or `202 Accepted` when
the command times out.

#### Scenario: Successful removal returns 200

- **WHEN** a valid `DELETE /showcases/{showcaseId}` request is received
- **THEN** the system dispatches a remove command and responds with `200 OK`

#### Scenario: Timeout returns 202

- **WHEN** removing times out
- **THEN** the system responds with `202 Accepted`

#### Scenario: Invalid showcase ID is rejected

- **WHEN** a `DELETE /showcases/{showcaseId}` request carries a showcase ID that is not a valid KSUID
- **THEN** the system responds with a `400 Bad Request` problem detail, detail "Invalid request.", and a `pathErrors`
  map containing the `showcaseId` property

### Requirement: Fetch showcase list endpoint

The system SHALL expose `GET /showcases` returning showcases sorted by ID in descending order, optionally filtered by a
full-text title match and one or more statuses, with cursor pagination via `afterId` and a bounded page size (default
20, between 1 and 1000).

#### Scenario: Successful list fetch returns the showcases

- **WHEN** a `GET /showcases` request is received
- **THEN** the system responds with `200 OK` and the matching showcases

#### Scenario: Invalid afterId is rejected

- **WHEN** a `GET /showcases` request carries an `afterId` that is not a valid KSUID
- **THEN** the system responds with a `400 Bad Request` problem detail, detail "Invalid request.", and a `paramErrors`
  map containing the `afterId` property

#### Scenario: Invalid size is rejected

- **WHEN** a `GET /showcases` request carries a `size` outside 1 to 1000 inclusive
- **THEN** the system responds with a `400 Bad Request` problem detail, detail "Invalid request.", and a `paramErrors`
  map containing the `size` property

### Requirement: Fetch showcase by ID endpoint

The system SHALL expose `GET /showcases/{showcaseId}` returning the matching showcase or a `404 Not Found` problem
detail when it does not exist.

#### Scenario: Existing showcase is returned

- **WHEN** a `GET /showcases/{showcaseId}` request is received for an existing showcase
- **THEN** the system responds with `200 OK` and the showcase

#### Scenario: Missing showcase produces 404

- **WHEN** a `GET /showcases/{showcaseId}` request is received for a non-existent showcase
- **THEN** the system responds with a `404 Not Found` problem detail with the error message from the query service

#### Scenario: Invalid showcase ID is rejected

- **WHEN** a `GET /showcases/{showcaseId}` request carries a showcase ID that is not a valid KSUID
- **THEN** the system responds with a `400 Bad Request` problem detail, detail "Invalid request.", and a `pathErrors`
  map containing the `showcaseId` property

### Requirement: Cache fallback on transient query failures

The system SHALL maintain in-memory caches of fetch-showcase-list and fetch-showcase-by-id results, and SHALL serve
cached results when a query fails with a transient error, falling back to the cached IDs and showcases before failing.

#### Scenario: Successful list fetch populates the caches

- **WHEN** a `GET /showcases` request succeeds
- **THEN** the fetched showcases are stored in the by-ID cache and their IDs in the list cache

#### Scenario: List fetch falls back to cached results on transient error

- **WHEN** fetching the showcase list fails with a transient error and cached IDs with their showcases exist
- **THEN** the system responds with `200 OK` and the cached showcases

#### Scenario: List fetch with no cached IDs produces 503

- **WHEN** fetching the showcase list fails with a transient error and no cached IDs exist
- **THEN** the system responds with a `503 Service Unavailable` problem detail

#### Scenario: List fetch with missing cached showcase produces 503

- **WHEN** fetching the showcase list fails with a transient error and a cached ID has no cached showcase
- **THEN** the system responds with a `503 Service Unavailable` problem detail

#### Scenario: By-ID fetch falls back to cached showcase on transient error

- **WHEN** fetching a showcase by ID fails with a transient error and the showcase is cached
- **THEN** the system responds with `200 OK` and the cached showcase

#### Scenario: By-ID fetch with no cached showcase produces 503

- **WHEN** fetching a showcase by ID fails with a transient error and the showcase is not cached
- **THEN** the system responds with a `503 Service Unavailable` problem detail

### Requirement: Command error translation

The system SHALL map command failures to structured problem details with the error code: `INVALID_COMMAND` to 400 Bad
Request with a `fieldErrors` map, `NOT_FOUND` to 404 Not Found, and `TITLE_IN_USE` or `ILLEGAL_STATE` to 409 Conflict.

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

### Requirement: Query error translation

The system SHALL map query failures to structured problem details with the error code: `INVALID_QUERY` to 400 Bad
Request with a `fieldErrors` map, and `NOT_FOUND` to 404 Not Found.

#### Scenario: Invalid query produces 400 with field errors

- **WHEN** a query fails with error code `INVALID_QUERY`
- **THEN** the system responds with a `400 Bad Request` problem detail carrying the error message, the error code, and a
  `fieldErrors` map

#### Scenario: Missing showcase produces 404

- **WHEN** a query fails with error code `NOT_FOUND`
- **THEN** the system responds with a `404 Not Found` problem detail carrying the error message and the error code

### Requirement: Availability error translation

The system SHALL map downstream availability failures to `503 Service Unavailable` problem details: Axon Framework
failures, WebClient failures, circuit breaker rejections, and unknown errors.

#### Scenario: Downstream failure produces 503

- **WHEN** a command or query fails with an Axon, WebClient, or circuit breaker error, or an unknown error
- **THEN** the system responds with a `503 Service Unavailable` problem detail

#### Scenario: Timeout produces 504

- **WHEN** a command or query times out outside the accepted asynchronous handling path
- **THEN** the system responds with a `504 Gateway Timeout` problem detail with detail "Operation timeout exceeded."

#### Scenario: Aborted inbound connection produces 408

- **WHEN** the inbound connection aborts during request handling
- **THEN** the system responds with a `408 Request Timeout` and an empty body
