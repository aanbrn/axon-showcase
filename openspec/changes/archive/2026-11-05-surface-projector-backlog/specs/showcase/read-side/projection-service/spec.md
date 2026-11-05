## MODIFIED Requirements

### Requirement: Projector operating state is observable

The system SHALL expose the projector's observed lifecycle state, last resolved batch time, and in-flight batch count
through its actuator and metrics surface, together with the consumer backlog the next requirement owns. The indicator
SHALL report `UP` only for `RUNNING` and `DOWN` for `RETRYING` or `STOPPED`, SHALL participate in readiness, and SHALL
NOT participate in liveness.

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

## ADDED Requirements

### Requirement: The projector's consumer backlog is observable

The system SHALL expose the consumer group's outstanding backlog per partition — the distance between the topic's end
offset and the group's committed offset — with the freshness of the last successful sample, the number of samples that
failed, and the number of partitions the sample could not resolve. It SHALL report a partition it cannot resolve as
unresolved rather than as zero, and SHALL NOT change the projector's health status.

#### Scenario: The projector's outstanding backlog is observable

- **WHEN** the projector is running and the topic's end offsets are ahead of the group's committed offsets
- **THEN** the metrics surface exports the distance per partition and the time of the last successful sample, and the
  indicator's details carry the maximum resolved partition distance and the number of partitions the sample could not
  resolve, so a reader sees how many records are outstanding, how much of that answer is complete, and how old the
  observation is
- **AND** the backlog does not change the indicator's status, the readiness endpoint, or liveness

#### Scenario: A backlog the sampler cannot resolve is not reported as zero

- **WHEN** a sample finds a partition with no committed offset for the group, a committed offset past the end, or a
  partition the topic no longer reports
- **THEN** that partition reports no backlog value and is counted as unresolved — in the metrics and in the indicator's
  unresolved-partition count — rather than publishing a zero

#### Scenario: A failed sample publishes no value and is counted

- **WHEN** the sampler's own admin calls fail — the broker is unreachable or the topic cannot be described
- **THEN** the last published distances and the observation time stay unchanged, the failed-sample count increments, and
  no zero is published in place of the missing observation

#### Scenario: A sample that resolves no partition reports no summary

- **WHEN** the sampler has not completed a successful sample yet
- **THEN** the observation time is `0`, no distance is published, the failed-sample count reports how many attempts have
  failed, and the indicator carries no maximum-distance detail

#### Scenario: A group that never committed reports no distance

- **WHEN** a sample finds the topic's partitions and no committed offsets for the group
- **THEN** every partition is counted by the unresolved gauge, no distance is published, and the observation time is
  fresh while the indicator carries no maximum-distance detail
