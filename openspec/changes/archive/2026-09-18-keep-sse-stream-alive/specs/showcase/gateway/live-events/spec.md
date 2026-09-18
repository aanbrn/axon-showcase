## MODIFIED Requirements

### Requirement: Stream lifecycle and failures

The SSE stream SHALL stay connected and deliver events continuously while the gateway runs, and SHALL NOT remain silent
for an interval long enough for an intermediary to close the connection: while no domain event occurs, the stream SHALL
emit a periodic keep-alive that carries no event, so an open connection always carries bytes. The keep-alive interval
SHALL be configurable, so a deployment can align it with its ingress's read timeout. Transient Kafka failures SHALL be
handled so the stream reconnects without losing the UI's already-rendered timeline.

#### Scenario: The stream reconnects after a transient failure

- **WHEN** the Kafka subscription fails transiently
- **THEN** the stream reconnects and resumes delivering new events

#### Scenario: An idle stream stays alive

- **WHEN** no domain event occurs for longer than the keep-alive interval
- **THEN** the stream emits a keep-alive frame that carries no event, so an intermediary observing the connection sees
  continuous traffic

#### Scenario: A keep-alive is not delivered as an event

- **WHEN** a client receives a keep-alive frame
- **THEN** it is not delivered as a domain event, so the client's event handling is unaffected

#### Scenario: The endpoint requires no authentication

- **WHEN** a client connects to the SSE endpoint
- **THEN** the connection is accepted without credentials, consistent with the rest of the gateway
