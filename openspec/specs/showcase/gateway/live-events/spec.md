# showcase/gateway/live-events Specification

## Purpose

Documents the gateway's live event stream: a Server-Sent-Events endpoint that streams real domain events as they are
published to Kafka, so clients can observe the CQRS event flow live without reaching into the write side (the Axon
event store is never read by the gateway).

## Requirements

### Requirement: Stream domain events over SSE

The gateway SHALL expose a Server-Sent-Events endpoint that streams showcase domain events as they occur. Events are
consumed from the Kafka topic the command service publishes to, using a consumer group distinct from the projection
service's, and are decoded with the Axon event serializer. The event stream is the sole event source — the gateway
SHALL NOT read the Axon event store and no projected events index SHALL be introduced.

#### Scenario: A domain event is streamed

- **WHEN** a showcase domain event is published to the Kafka topic
- **THEN** the SSE stream delivers the event, carrying at least the event type, the showcase ID, and the event
  timestamp

#### Scenario: Events carry their type and identity

- **WHEN** the gateway delivers an event
- **THEN** the event identifies its type (e.g. scheduled, started, finished, removed) and the showcase it concerns, so
  the UI can route it to the correct timeline

### Requirement: The stream coexists with the projection consumer

The gateway consumer SHALL use a consumer group separate from the projection service's, so each event is delivered to
both the projection (for the read model) and the live stream without either stealing the other's messages.

#### Scenario: Projection and live stream both receive events

- **WHEN** a domain event is published to Kafka
- **THEN** the projection service and the gateway live stream each process it independently under their own consumer
  groups

### Requirement: Stream lifecycle and failures

The SSE stream SHALL stay connected and deliver events continuously while the gateway runs. Transient Kafka failures
SHALL be handled so the stream reconnects without losing the UI's already-rendered timeline.

#### Scenario: The stream reconnects after a transient failure

- **WHEN** the Kafka subscription fails transiently
- **THEN** the stream reconnects and resumes delivering new events

#### Scenario: The endpoint requires no authentication

- **WHEN** a client connects to the SSE endpoint
- **THEN** the connection is accepted without credentials, consistent with the rest of the gateway