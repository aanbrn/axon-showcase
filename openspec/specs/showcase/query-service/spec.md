# showcase/query-service Specification

## Purpose
Documents the current behavior of the read side of the CQRS showcase application: exposing protobuf query endpoints,
dispatching Axon streaming queries, and searching the `showcases` projection in OpenSearch.

## Requirements
### Requirement: Query transport and endpoints

The system SHALL expose two HTTP endpoints accepting a protobuf `QueryRequest` body: `POST /streaming-query` returning
the full response stream, and `POST /query` returning only the first response.

#### Scenario: Streaming query returns the full response stream

- **WHEN** a `POST /streaming-query` request carrying a valid protobuf `QueryRequest` is received
- **THEN** the system responds with every query response produced for that request

#### Scenario: Query returns only the first response

- **WHEN** a `POST /query` request carrying a valid protobuf `QueryRequest` is received
- **THEN** the system responds with only the first query response

#### Scenario: Unknown expected response type is rejected

- **WHEN** a `QueryRequest` references a response type that cannot be resolved
- **THEN** the system rejects the request with a 400 Bad Request and detail "Unknown expected response type"

#### Scenario: Tracing context is propagated to the dispatched query

- **WHEN** a `QueryRequest` is dispatched to the query bus
- **THEN** the tracing context is propagated with the query message

### Requirement: Fetch showcase list query

The system SHALL handle `FetchShowcaseListQuery`, optionally filtering by title and statuses, sorting results by
`showcaseId` in descending order, supporting cursor pagination via `afterId` and a bounded page size.

#### Scenario: No filtering returns all showcases sorted by ID descending

- **WHEN** a `FetchShowcaseListQuery` without title, statuses, or `afterId` is dispatched
- **THEN** the system responds with all showcases sorted by `showcaseId` in descending order

#### Scenario: Title filter restricts results

- **WHEN** a `FetchShowcaseListQuery` with a `title` is dispatched
- **THEN** the system responds with only showcases whose title full-text matches the given title

#### Scenario: Single status filter restricts results

- **WHEN** a `FetchShowcaseListQuery` with a single `status` is dispatched
- **THEN** the system responds with only showcases in that status

#### Scenario: Multiple statuses filter restrict results

- **WHEN** a `FetchShowcaseListQuery` with multiple `statuses` is dispatched
- **THEN** the system responds with showcases in any of the given statuses

#### Scenario: Cursor pagination returns subsequent showcases

- **WHEN** a `FetchShowcaseListQuery` with an `afterId` is dispatched
- **THEN** the system responds with showcases that sort after the showcase with that ID, in descending `showcaseId`
  order

#### Scenario: Page size limits the result count

- **WHEN** a `FetchShowcaseListQuery` with a `size` is dispatched
- **THEN** the system responds with at most `size` showcases

### Requirement: Fetch showcase by ID query

The system SHALL handle `FetchShowcaseByIdQuery`, responding with the matching showcase or a NOT_FOUND error when the
showcase is absent.

#### Scenario: Existing showcase is returned

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched for a showcase ID that exists in the projection
- **THEN** the system responds with the showcase for that ID

#### Scenario: Missing showcase produces NOT_FOUND

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched for a showcase ID that does not exist in the projection
- **THEN** the system responds with a 404 Not Found problem detail with message "No showcase with given ID"

### Requirement: Query validation

The system SHALL validate query payloads against bean validation constraints and SHALL reject invalid queries with a
400 Bad Request problem detail whose `fieldErrors` property maps each offending property path to its violation messages.

#### Scenario: Invalid list query is rejected with property errors

- **WHEN** a `FetchShowcaseListQuery` is dispatched whose payload violates its constraints (for example an `afterId`
  that is not a valid KSUID or a `size` outside 1 to 1000 inclusive)
- **THEN** the system rejects the request with a 400 Bad Request, detail "Given query is not valid", and a
  `fieldErrors` map of each offending property path to its validation messages

#### Scenario: Invalid by-ID query is rejected with property errors

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched with a `showcaseId` that is not a valid KSUID
- **THEN** the system rejects the request with a 400 Bad Request, detail "Given query is not valid", and a
  `fieldErrors` map of the `showcaseId` property to its validation messages

### Requirement: Error translation for query failures

The system SHALL map query failures to structured problem details: data access failures to 503 Service Unavailable,
timeouts to 504 Gateway Timeout, aborted inbound connections to 408 Request Timeout, and unknown errors to 503 Service
Unavailable.

#### Scenario: Data access failure produces 503

- **WHEN** searching the projection fails with a data access error
- **THEN** the system responds with a 503 Service Unavailable problem detail

#### Scenario: Timeout produces 504

- **WHEN** query handling times out
- **THEN** the system responds with a 504 Gateway Timeout problem detail and message "Operation timeout exceeded"

#### Scenario: Aborted inbound connection produces 408

- **WHEN** the inbound connection aborts during query handling
- **THEN** the system responds with a 408 Request Timeout

#### Scenario: Unknown error produces 503

- **WHEN** an unhandled error occurs during query handling
- **THEN** the system responds with a 503 Service Unavailable problem detail
