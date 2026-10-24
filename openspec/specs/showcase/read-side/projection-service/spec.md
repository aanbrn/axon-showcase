# showcase/read-side/projection-service Specification

## Purpose

Documents the current behavior of the read-model side of the CQRS showcase application: consuming showcase events from
Kafka and maintaining the `showcases` projection in OpenSearch.

## Requirements

### Requirement: Kafka event consumption

The system SHALL consume events from the Kafka topic configured as the default topic (default `axon-showcase-events`) as
an Axon `EventMessage` stream, using consumer group `showcase-projector`.

#### Scenario: Subscribes to the configured topic

- **WHEN** the projection service starts
- **THEN** it subscribes to the default Kafka topic (`axon-showcase-events` unless overridden by `KAFKA_TOPIC_EVENTS`)
  with consumer group `showcase-projector` (unless overridden by `KAFKA_CONSUMER_GROUP_ID`)

#### Scenario: Deserializes Axon event messages

- **WHEN** a Kafka record with the Axon message headers (`axon-message-id`, `axon-message-type`) and a
  Jackson-serialized payload is received
- **THEN** the record is converted to an `EventMessage` whose payload is a `ShowcaseEvent`

#### Scenario: Non-showcase payloads are ignored

- **WHEN** a record is received whose payload is not a `ShowcaseEvent`
- **THEN** the record is logged as a warning, counted as ignored, and not projected

#### Scenario: Showcase event without aggregate headers is counted as a failure

- **WHEN** a record carries a `ShowcaseEvent` payload but not the aggregate headers the converter needs
- **THEN** the record is counted as a failure and not projected, because it carries no aggregate sequence to order it by

### Requirement: Projection to the showcases index

The system SHALL maintain the `showcases` document for each showcase, using the showcase ID as the document ID, applying
one write operation per event and storing the aggregate sequence of the last applied event on the document.

#### Scenario: Scheduled event creates the document

- **WHEN** a `ShowcaseScheduledEvent` is consumed
- **THEN** the system creates a document with ID equal to the showcase ID if it does not already exist, containing
  `showcaseId`, `title`, `startTime`, `duration`, `status` = SCHEDULED, `scheduledAt`, and its aggregate sequence

#### Scenario: Started event updates the document

- **WHEN** a `ShowcaseStartedEvent` is consumed
- **THEN** the system updates the existing document with `duration`, `status` = STARTED, `startedAt`, and its aggregate
  sequence

#### Scenario: Finished event updates the document

- **WHEN** a `ShowcaseFinishedEvent` is consumed
- **THEN** the system updates the existing document with `status` = FINISHED, `finishedAt`, and its aggregate sequence

#### Scenario: Removed event deletes the document

- **WHEN** a `ShowcaseRemovedEvent` is consumed
- **THEN** the system deletes the document with ID equal to the showcase ID

#### Scenario: Update of a missing document is surfaced

- **WHEN** a `ShowcaseStartedEvent` or `ShowcaseFinishedEvent` is consumed for a showcase whose document does not exist
- **THEN** the item failure is logged (document missing) and does not prevent further records from being processed

#### Scenario: Duplicate create is surfaced

- **WHEN** a `ShowcaseScheduledEvent` is consumed for a showcase whose document already exists
- **THEN** the conflict is recognized as a redelivery of an already-applied event, the existing document is left
  unchanged, and processing continues

#### Scenario: Delete of a missing document is surfaced

- **WHEN** a `ShowcaseRemovedEvent` is consumed for a showcase whose document does not exist
- **THEN** the not-found outcome is recognized as the delete's intended end state and does not prevent further records
  from being processed

#### Scenario: Older replayed event does not regress the document

- **WHEN** a `ShowcaseScheduledEvent`, `ShowcaseStartedEvent`, or `ShowcaseFinishedEvent` is consumed whose aggregate
  sequence is lower than the sequence stored on the document
- **THEN** the write is a no-op, the newer document state is preserved, and processing continues

### Requirement: Batch processing and ordering

The system SHALL process consumed events in batches per partition, preserving per-aggregate order — independent
aggregates may interleave across a batch's dispatch rounds — and SHALL acknowledge record offsets only after every item
of the batch has a resolved outcome.

#### Scenario: Events for the same showcase are processed in order

- **WHEN** multiple events for the same showcase are consumed
- **THEN** they are applied in arrival order, so the document reflects the sequence of lifecycle events

#### Scenario: Offsets are acknowledged after the batch writes

- **WHEN** every item of a batch has a resolved outcome — success, convergence, or a logged skip — across all of the
  batch's dispatch rounds
- **THEN** the records in the batch are acknowledged so their offsets are committed

#### Scenario: A later event is not dispatched while its predecessor is unresolved

- **WHEN** a bulk item for an aggregate fails with a retryable outcome
- **THEN** that aggregate's next event is not written until the failed item is applied or its retry budget is exhausted

#### Scenario: Failed writes are retried with backoff

- **WHEN** a bulk item fails with a retryable outcome, such as a rejection under load
- **THEN** the unresolved items of that round are re-sent with backoff up to the configured maximum attempt count, and
  when that budget is exhausted the item failure is logged

#### Scenario: Stream failures restart the consumer

- **WHEN** the consumption stream fails or a bulk call fails with a transient error after its retries
- **THEN** the stream is restarted after a fixed delay, re-subscribing to the topic and re-delivering unacknowledged
  records

### Requirement: At-least-once delivery

The system SHALL deliver at-least-once: a record may be redelivered if it was written but not yet acknowledged, and the
system SHALL handle such duplicates without failing the stream.

#### Scenario: Redelivered duplicate event does not halt processing

- **WHEN** an already-applied event is redelivered after a restart
- **THEN** the redelivered write converges on the document's current state and the stream continues processing
  subsequent records
