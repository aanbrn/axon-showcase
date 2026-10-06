# showcase/read-side/query-service Specification

## Purpose

Documents the current behavior of the read side of the CQRS showcase application: serving the generic gRPC query
transport, dispatching Axon streaming queries, and searching the `showcases` projection in OpenSearch.

## Requirements

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

### Requirement: Query transport and RPC

The system SHALL expose a generic gRPC `Dispatch(QueryRequest) returns (stream QueryResponse)` RPC that reconstructs the
Axon streaming query message from the request, dispatches it on the query bus, and streams each response as a
`QueryResponse` carrying the response type, its revision, the serialized payload, and the serialized metadata.

#### Scenario: Streaming query returns the full response stream

- **WHEN** a `Dispatch` call carrying a valid `QueryRequest` is received
- **THEN** the system streams every query response produced for that request as a `QueryResponse`

#### Scenario: Each response carries its serialized payload and type

- **WHEN** a query response is streamed
- **THEN** its `QueryResponse` carries the response payload type, its revision when set, the serialized payload, and the
  serialized metadata

#### Scenario: Unknown expected response type is rejected

- **WHEN** a `QueryRequest` references a response type that cannot be resolved
- **THEN** the system fails the call with an `INVALID_ARGUMENT` status and detail "Unknown expected response type"

#### Scenario: Tracing context is propagated to the dispatched query

- **WHEN** a `QueryRequest` is dispatched to the query bus
- **THEN** the tracing context is propagated with the query message

### Requirement: Fetch showcase by ID handling

The system SHALL handle `FetchShowcaseByIdQuery`, streaming the matching showcase as a `QueryResponse`, or failing the
call with a `NOT_FOUND` status when the showcase is absent.

#### Scenario: Existing showcase is returned

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched for a showcase ID that exists in the projection
- **THEN** the system streams the showcase for that ID

#### Scenario: Missing showcase produces NOT_FOUND

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched for a showcase ID that does not exist in the projection
- **THEN** the system fails the call with a `NOT_FOUND` status and message "No showcase with given ID"

### Requirement: Query payload validation

The system SHALL validate query payloads against bean validation constraints, enabled by default and configurable via
the `showcase.query.validation-enabled` property, and SHALL reject invalid queries by failing the call with an
`INVALID_ARGUMENT` status whose `field-errors-bin` trailer maps each offending property path to its violation messages.

#### Scenario: Invalid list query is rejected with property errors

- **WHEN** a `FetchShowcaseListQuery` is dispatched whose payload violates its constraints (for example an `afterId`
  that is not a valid KSUID or a `size` outside 1 to 1000 inclusive) and validation is enabled (the default)
- **THEN** the system fails the call with an `INVALID_ARGUMENT` status, detail "Given query is not valid", and a
  `field-errors-bin` trailer of each offending property path to its validation messages

#### Scenario: Invalid by-ID query is rejected with property errors

- **WHEN** a `FetchShowcaseByIdQuery` is dispatched with a `showcaseId` that is not a valid KSUID and validation is
  enabled (the default)
- **THEN** the system fails the call with an `INVALID_ARGUMENT` status, detail "Given query is not valid", and a
  `field-errors-bin` trailer of the `showcaseId` property to its validation messages

#### Scenario: Query violating constraints succeeds when validation is disabled

- **WHEN** a query is dispatched whose payload violates its constraints while `showcase.query.validation-enabled` is set
  to `false`
- **THEN** the query proceeds to handling without validation, and no `INVALID_ARGUMENT` status is produced

### Requirement: Query failure translation

The system SHALL map query failures to gRPC statuses: data access failures to `UNAVAILABLE`, timeouts to
`DEADLINE_EXCEEDED`, cancelled calls to the default client status, and unknown errors to `INTERNAL`.

#### Scenario: Data access failure produces UNAVAILABLE

- **WHEN** searching the projection fails with a data access error
- **THEN** the system fails the call with an `UNAVAILABLE` status

#### Scenario: Timeout produces DEADLINE_EXCEEDED

- **WHEN** query handling times out
- **THEN** the system fails the call with a `DEADLINE_EXCEEDED` status and message "Operation timeout exceeded"

#### Scenario: Cancelled call produces the default status

- **WHEN** the caller cancels the call during query handling
- **THEN** the call ends cancelled with the default client status

#### Scenario: Unknown error produces INTERNAL

- **WHEN** an unhandled error occurs during query handling
- **THEN** the system fails the call with an `INTERNAL` status
