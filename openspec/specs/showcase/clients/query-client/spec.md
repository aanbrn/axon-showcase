# showcase/clients/query-client Specification

## Purpose

Documents the behavior of the showcase query client: a reactive protobuf consumer fetching showcases from the query
service's streaming and single-query endpoints, translating problem-detail errors, and protecting the service with
Resilience4j time limiter, circuit breaker, and conditional retry over retryable HTTP status codes.

**Contract source:** the endpoints, query types, and error codes this client calls are owned by the
`read-side/query-service` spec — the endpoints `/streaming-query` and `/query`, the queries `FetchShowcaseListQuery` and
`FetchShowcaseByIdQuery`, and the error codes `INVALID_QUERY`, `NOT_FOUND`.

## Requirements

### Requirement: Query operations and endpoints

The system SHALL expose two operations: `fetchList`, which fetches matching showcases through the query service's
streaming query endpoint, and `fetchById`, which fetches a single showcase by ID through the single-query endpoint, both
sending the query serialized as protobuf.

#### Scenario: Fetching the list succeeds

- **WHEN** a `fetchList` operation is invoked with a `FetchShowcaseListQuery`
- **THEN** the system posts the query as a protobuf body to the streaming query endpoint and returns the matching
  showcases as a stream

#### Scenario: Fetching by ID succeeds

- **WHEN** a `fetchById` operation is invoked with a `FetchShowcaseByIdQuery` carrying a showcase ID
- **THEN** the system posts the query as a protobuf body to the single-query endpoint and returns the matching showcase

### Requirement: Business error translation

The system SHALL translate problem-detail error responses into a `ShowcaseQueryException` with the matching error code:
`INVALID_QUERY` for a 400 response carrying field errors, and `NOT_FOUND` for a 404 response carrying a detail message.
Responses without problem details or with unexpected statuses SHALL fail with the default client exception.

#### Scenario: Invalid query produces invalid-query error

- **WHEN** the query service responds with a 400 problem detail carrying a `fieldErrors` map
- **THEN** the operation fails with a `ShowcaseQueryException` of error code `INVALID_QUERY` whose metadata carries the
  field errors and whose message is the problem detail

#### Scenario: Missing showcase produces not-found error

- **WHEN** the query service responds with a 404 problem detail carrying a message
- **THEN** the operation fails with a `ShowcaseQueryException` of error code `NOT_FOUND` whose message is the problem
  detail

#### Scenario: Non-problem-detail error propagates unchanged

- **WHEN** the query service responds with an error that is not a problem detail or is an unexpected status
- **THEN** the operation fails with the default client exception for that response

### Requirement: Retry of retryable failures

The system SHALL retry a failed operation only when the failure is retryable: a response with a retryable HTTP status
code (408, 425, 429, 500, 502, 503, 504, 524), a timeout, or a request-level failure. Each retryable failure causes a
re-request up to the configured attempt limit, after which the operation fails with the last error.

#### Scenario: Retryable status code is retried

- **WHEN** the query service responds with a retryable HTTP status code such as 503 or 429
- **THEN** the operation re-requests up to the configured attempt limit and then fails with that status code

#### Scenario: Timeout is retried

- **WHEN** the query service does not respond before the request timeout
- **THEN** the operation re-requests up to the configured attempt limit and then fails with the timeout error

#### Scenario: Request-level failure is retried

- **WHEN** the request fails before receiving a response
- **THEN** the operation re-requests up to the configured attempt limit and then fails with the request failure

#### Scenario: Non-retryable status code is not retried

- **WHEN** the query service responds with a non-retryable HTTP status code such as 400 or 404
- **THEN** the operation fails immediately without retrying

### Requirement: Time limiter

The system SHALL enforce a timeout on query operations: an operation that does not complete within the configured
timeout SHALL fail with a timeout error.

#### Scenario: Slow response times out

- **WHEN** the query service responds slower than the configured timeout
- **THEN** the operation fails with a timeout error

### Requirement: Circuit breaker isolation of business errors

The system SHALL treat business errors as circuit breaker failures to ignore: a `ShowcaseQueryException` SHALL NOT count
toward opening the circuit breaker, while infrastructure failures SHALL.

#### Scenario: Business error does not open the circuit breaker

- **WHEN** a query fails with a `ShowcaseQueryException`
- **THEN** the circuit breaker is not affected by that failure

### Requirement: Automatic resilience configuration

The system SHALL register, on auto-configuration, the Resilience4j customizers that wire the retry filter and the
circuit breaker behavior above for the query service, so consumers get the protection without additional setup.

#### Scenario: Retry customizer is registered

- **WHEN** the query client is auto-configured
- **THEN** the retry configuration for the query service retries only failures accepted by the retry filter

#### Scenario: Circuit breaker customizer is registered

- **WHEN** the query client is auto-configured
- **THEN** the circuit breaker configuration for the query service ignores business errors

### Requirement: Client configuration

The system SHALL configure the query service base URL through a `showcase.query.api-url` property that must be a
non-empty HTTP URL, and SHALL fail when the property is missing or invalid.

#### Scenario: Valid API URL is accepted

- **WHEN** `showcase.query.api-url` is set to a non-empty HTTP URL
- **THEN** the client uses that URL as the query service base URL

#### Scenario: Missing API URL is rejected

- **WHEN** `showcase.query.api-url` is missing, empty, or not an HTTP URL
- **THEN** the client configuration fails validation
