## REMOVED Requirements

### Requirement: Stream domain events over SSE

**Reason**: retitled to a declarative noun phrase, the corpus convention.

**Migration**: the requirement is unchanged in substance; the new header is `Domain event streaming over SSE`.

## ADDED Requirements

### Requirement: Domain event streaming over SSE

The gateway SHALL expose a Server-Sent-Events endpoint that streams showcase domain events as they occur. Events are
consumed from the Kafka topic the command service publishes to, using a consumer group distinct from the projection
service's, and are decoded with the Axon event serializer. The event stream is the sole event source — the gateway SHALL
NOT read the Axon event store and no projected events index SHALL be introduced.

#### Scenario: A domain event is streamed

- **WHEN** a showcase domain event is published to the Kafka topic
- **THEN** the SSE stream delivers the event, carrying at least the event type, the showcase ID, and the event timestamp

#### Scenario: Events carry their type and identity

- **WHEN** the gateway delivers an event
- **THEN** the event identifies its type (e.g. scheduled, started, finished, removed) and the showcase it concerns, so
  the UI can route it to the correct timeline
