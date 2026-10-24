## ADDED Requirements

### Requirement: Projector operating state is observable

The system SHALL expose the projector's observed lifecycle state, last resolved batch time, and in-flight batch count
through its actuator and metrics surface. The indicator SHALL report `UP` only for `RUNNING` and `DOWN` for `RETRYING`
or `STOPPED`, SHALL participate in readiness, and SHALL NOT participate in liveness.

#### Scenario: A retrying projector is not reported healthy

- **WHEN** the consumption pipeline emits an error and the projector waits to restart
- **THEN** the actuator reports the projector's state as retrying
- **AND** the projector indicator and the readiness endpoint report it not healthy, with the recorded failure in the
  indicator's details
- **AND** the projector's state alone does not change liveness

#### Scenario: A stopped projector is not reported healthy

- **WHEN** the projector has not been started, is starting without an assigned partition, or has been stopped
- **THEN** the actuator reports the projector's state as stopped, and the health endpoint reports the projector not
  healthy

#### Scenario: A consuming projector with no events yet reports no progress

- **WHEN** the consumer has assigned partitions and no event has arrived yet
- **THEN** the actuator reports the state as running, the last progress as none (`0`), and the health endpoint reports
  it healthy

#### Scenario: A consuming projector reports its progress

- **WHEN** the projector is consuming and a batch's writes have resolved
- **THEN** the actuator reports the projector's state as running, exposes the time of the last resolved batch, and
  reports it healthy

#### Scenario: The state, progress, and in-flight count are exported as metrics

- **WHEN** the projector changes state, starts a batch, or resolves one
- **THEN** the metrics surface exports the state, the time of the last resolved batch, and the number of batches in
  flight, so a scrape shows all three without the actuator

#### Scenario: Broker unreachability without a stream failure does not change the reported state

- **WHEN** the receiver has reached partition assignment and Kafka becomes unreachable without the consumption pipeline
  emitting an error
- **THEN** the projector remains reported as running, its indicator remains healthy, and the last-progress timestamp
  remains the time of the last resolved batch (or `0` if none resolved)

## MODIFIED Requirements

### Requirement: Batch processing and ordering

The system SHALL process consumed events in batches per partition, preserving per-aggregate order — independent
aggregates may interleave across a batch's dispatch rounds — SHALL acknowledge record offsets only after every item of
the batch has a resolved outcome, and SHALL bound the consumer's buffering in events per partition, with the effective
bound documented as the configured buffer capacity plus up to two batches: one batch window and one write prefetch.

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

#### Scenario: The buffer's overflow point matches its documented unit

- **WHEN** a burst fills the configured buffer while the batch writer is slower than the consumer
- **THEN** the stream fails with an overflow error when the documented unit's bound is reached, so the operator's
  configured value and the observed bound agree
