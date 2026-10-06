## MODIFIED Requirements

### Requirement: Query operations and endpoints

The system SHALL expose two operations: `fetchList`, which fetches matching showcases, and `fetchById`, which fetches a
single showcase by ID, both dispatching the query over the query service's generic gRPC query RPC, sending the query
serialized as a protobuf `QueryRequest` and decoding each `QueryResponse`.

#### Scenario: Fetching the list succeeds

- **WHEN** a `fetchList` operation is invoked with a `FetchShowcaseListQuery`
- **THEN** the system calls the query service's gRPC query RPC with the query as a protobuf `QueryRequest` and returns
  the decoded response stream as the matching showcases

#### Scenario: Fetching by ID succeeds

- **WHEN** a `fetchById` operation is invoked with a `FetchShowcaseByIdQuery` carrying a showcase ID
- **THEN** the system calls the query RPC with the query and returns the first decoded response as the matching showcase

### Requirement: Retry of retryable failures

The system SHALL retry a failed operation only when the failure is retryable: a gRPC status of `UNAVAILABLE`,
`DEADLINE_EXCEEDED`, `RESOURCE_EXHAUSTED`, or `ABORTED`, or an operation timeout. Each retryable failure causes a
re-request up to the configured attempt limit, after which the operation fails with the last error.

#### Scenario: Retryable status code is retried

- **WHEN** the query service fails the RPC with a retryable status such as `UNAVAILABLE` or `RESOURCE_EXHAUSTED`
- **THEN** the operation re-requests up to the configured attempt limit and then fails with that status

#### Scenario: Timeout is retried

- **WHEN** the operation does not complete within the configured timeout
- **THEN** the operation re-requests up to the configured attempt limit and then fails with the timeout error

#### Scenario: Request-level failure is retried

- **WHEN** the RPC fails before receiving a response
- **THEN** the operation re-requests up to the configured attempt limit and then fails with the request failure

#### Scenario: Non-retryable status code is not retried

- **WHEN** the query service fails the RPC with a non-retryable status such as `INVALID_ARGUMENT` or `NOT_FOUND`
- **THEN** the operation fails immediately without retrying

### Requirement: Time limiter

The system SHALL enforce a configured timeout on query operations: an operation that does not complete within the
configured timeout SHALL fail with a timeout error.

#### Scenario: Slow response times out

- **WHEN** the query service does not respond within the configured timeout
- **THEN** the operation fails with a timeout error

## REMOVED Requirements

### Requirement: Business error translation

**Reason**: The HTTP problem-detail translation is replaced by gRPC status translation, which no longer maps problem
details; restated in `Query error translation`.

**Migration**: Business errors arrive as gRPC statuses (`INVALID_ARGUMENT` / `NOT_FOUND`) and are mapped to
`ShowcaseQueryException` with the same error codes.

### Requirement: Client configuration

**Reason**: The property configures an HTTP URL, which no longer fits a gRPC channel; restated in
`Client target configuration`.

**Migration**: `showcase.query.api-url` becomes `showcase.query.target`, a non-empty gRPC target.

## ADDED Requirements

### Requirement: Query error translation

The system SHALL translate a gRPC error status into a `ShowcaseQueryException` with the matching error code:
`INVALID_QUERY` for an `INVALID_ARGUMENT` status whose trailers carry field errors, and `NOT_FOUND` for a `NOT_FOUND`
status carrying a message. A status that is not a field-error `INVALID_ARGUMENT` or a `NOT_FOUND` — including an
`INVALID_ARGUMENT` with no field errors — SHALL fail with the default client exception.

#### Scenario: Invalid query produces invalid-query error

- **WHEN** the query service fails the RPC with an `INVALID_ARGUMENT` status carrying a `field-errors-bin` trailer
- **THEN** the operation fails with a `ShowcaseQueryException` of error code `INVALID_QUERY` whose metadata carries the
  field errors and whose message is the status description

#### Scenario: Missing showcase produces not-found error

- **WHEN** the query service fails the RPC with a `NOT_FOUND` status carrying a message
- **THEN** the operation fails with a `ShowcaseQueryException` of error code `NOT_FOUND` whose message is the status
  description

#### Scenario: Non-business status propagates unchanged

- **WHEN** the query service fails the RPC with a status that is not a field-error `INVALID_ARGUMENT` or a `NOT_FOUND`
- **THEN** the operation fails with the default client exception for that status

### Requirement: Client target configuration

The system SHALL configure the query service gRPC target through a `showcase.query.target` property that must be
non-empty, and SHALL fail when the property is missing or empty.

#### Scenario: Valid target is accepted

- **WHEN** `showcase.query.target` is set to a non-empty gRPC target
- **THEN** the client connects to that target

#### Scenario: Missing target is rejected

- **WHEN** `showcase.query.target` is missing or empty
- **THEN** the client configuration fails validation
