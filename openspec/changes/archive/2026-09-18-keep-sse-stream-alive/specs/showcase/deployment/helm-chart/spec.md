## MODIFIED Requirements

### Requirement: API gateway runtime tuning

The api-gateway Deployment SHALL wire its runtime tuning through environment: the query-service internal URL for read
routing, two Caffeine query caches (the showcase list and showcase-by-id queries) with size and expiry settings, the
live event stream's keep-alive interval, and the resilience4j environment for the time limiter, circuit breaker, and
retry, each with defaults and per-service command/query overrides.

#### Scenario: Gateway routes reads to the query service

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the internal query-service URL for forwarding read requests

#### Scenario: Query caches are tunable

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the showcase list and showcase-by-id query cache settings (maximum size and expiry after access
  and write)

#### Scenario: The live stream keep-alive interval is tunable

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the live event stream's keep-alive interval

#### Scenario: Resilience4j is configured with defaults and per-service overrides

- **WHEN** an api-gateway container is rendered
- **THEN** it receives the time limiter, circuit breaker, and retry environment, with default settings and
  command-service and query-service overrides for each
