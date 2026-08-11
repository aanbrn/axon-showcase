# showcase/projection-service Specification

## Purpose
Documents the current behavior of the read-model side of the CQRS showcase application: consuming showcase events from
Kafka and maintaining the `showcases` projection in OpenSearch.

## Requirements
### Requirement: Kafka event consumption

The system SHALL consume events from the Kafka topic configured as the default topic (default `axon-showcase-events`)
as an Axon `EventMessage` stream, using consumer group `showcase-projector`.

#### Scenario: Subscribes to the configured topic

- **WHEN** the projection service starts
- **THEN** it subscribes to the default Kafka topic (`axon-showcase-events` unless overridden by `KAFKA_TOPIC_EVENTS`)
  with consumer group `showcase-projector` (unless overridden by `KAFKA_CONSUMER_GROUP_ID`)

#### Scenario: Deserializes Axon event messages

- **WHEN** a Kafka record with the Axon message headers (`axon-message-id`, `axon-message-type`) and a Jackson-serialized
  payload is received
- **THEN** the record is converted to an `EventMessage` whose payload is a `ShowcaseEvent`

#### Scenario: Non-showcase payloads are ignored

- **WHEN** a record is received whose payload is not a `ShowcaseEvent`
- **THEN** the record is logged as a warning, counted as ignored, and not projected

### Requirement: Projection to the showcases index

The system SHALL maintain the `showcases` document for each showcase, using the showcase ID as the document ID, applying
one write operation per event.

#### Scenario: Scheduled event creates the document

- **WHEN** a `ShowcaseScheduledEvent` is consumed
- **THEN** the system creates a document with ID equal to the showcase ID if it does not already exist, containing
  `showcaseId`, `title`, `startTime`, `duration`, `status` = SCHEDULED, and `scheduledAt`

#### Scenario: Started event updates the document

- **WHEN** a `ShowcaseStartedEvent` is consumed
- **THEN** the system updates the existing document with `duration`, `status` = STARTED, and `startedAt`

#### Scenario: Finished event updates the document

- **WHEN** a `ShowcaseFinishedEvent` is consumed
- **THEN** the system updates the existing document with `status` = FINISHED and `finishedAt`

#### Scenario: Removed event deletes the document

- **WHEN** a `ShowcaseRemovedEvent` is consumed
- **THEN** the system deletes the document with ID equal to the showcase ID

#### Scenario: Update of a missing document is surfaced

- **WHEN** a `ShowcaseStartedEvent` or `ShowcaseFinishedEvent` is consumed for a showcase whose document does not exist
- **THEN** the item failure is logged (document missing) and does not prevent further records from being processed

#### Scenario: Duplicate create is surfaced

- **WHEN** a `ShowcaseScheduledEvent` is consumed for a showcase whose document already exists
- **THEN** the item failure is logged (version conflict) and does not prevent further records from being processed

#### Scenario: Delete of a missing document is surfaced

- **WHEN** a `ShowcaseRemovedEvent` is consumed for a showcase whose document does not exist
- **THEN** the not-found outcome is logged as a warning and does not prevent further records from being processed

### Requirement: Batch processing and ordering

The system SHALL process consumed events in batches per partition, preserving per-partition order, and SHALL acknowledge
record offsets only after the batch's OpenSearch writes complete.

#### Scenario: Events for the same showcase are processed in order

- **WHEN** multiple events for the same showcase are consumed
- **THEN** they are applied in arrival order, so the document reflects the sequence of lifecycle events

#### Scenario: Offsets are acknowledged after the batch writes

- **WHEN** a batch's OpenSearch bulk write completes successfully
- **THEN** the records in the batch are acknowledged so their offsets are committed

#### Scenario: Failed writes are retried with backoff

- **WHEN** an OpenSearch write fails with a transient error
- **THEN** the write is retried with exponential backoff up to a maximum attempt count before the stream is restarted

#### Scenario: Stream failures restart the consumer

- **WHEN** the consumption stream fails or the OpenSearch write exhausts its retries
- **THEN** the stream is restarted after a fixed delay, re-subscribing to the topic and re-delivering unacknowledged
  records

### Requirement: At-least-once delivery

The system SHALL deliver at-least-once: a record may be redelivered if it was written but not yet acknowledged, and
the system SHALL handle such duplicates without failing the stream.

#### Scenario: Redelivered duplicate event does not halt processing

- **WHEN** an already-applied event is redelivered after a restart
- **THEN** the resulting duplicate write is logged as an error but the stream continues processing subsequent records
