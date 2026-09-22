## ADDED Requirements

### Requirement: Trace-context propagation on API calls

The UI SHALL propagate W3C Trace Context on every fetch-based gateway request, so a page load's requests join a single
trace the gateway continues through the command/query pipeline. (The SSE stream is a browser `EventSource`, which cannot
set request headers, so it stays outside the trace.) Each page load SHALL use one trace id, each request a distinct
parent id, and the trace SHALL be marked sampled, so the pipeline records it rather than dropping it.

#### Scenario: A request carries a W3C traceparent

- **WHEN** the UI sends a fetch-based request to the gateway
- **THEN** the request carries a `traceparent` header of the form `00-<32 hex>-<16 hex>-01`

#### Scenario: One page load shares a trace id

- **WHEN** a page load issues several gateway requests
- **THEN** each request's `traceparent` carries the same trace id and a distinct parent id

#### Scenario: The trace is marked sampled

- **WHEN** the UI builds a `traceparent`
- **THEN** its version is `00` and its flags are `01`, so the gateway continues and records the trace
