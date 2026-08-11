## Purpose

Documents the current behavior of the write side of the CQRS showcase application: command handling, aggregate state
transitions, event emission, validation, saga deadlines, and event store persistence.

## ADDED Requirements

### Requirement: Command handling contract

The system SHALL receive the commands `ScheduleShowcaseCommand`, `StartShowcaseCommand`, `FinishShowcaseCommand`, and
`RemoveShowcaseCommand` over the Axon distributed command bus, each carrying a `showcaseId` that identifies the target
aggregate.

#### Scenario: ScheduleShowcaseCommand dispatched

- **WHEN** a `ScheduleShowcaseCommand` for a new showcase ID is dispatched with title, startTime, and duration
- **THEN** the command is handled and a `ShowcaseScheduledEvent` is emitted with showcaseId, title, startTime, duration,
  and scheduledAt

#### Scenario: StartShowcaseCommand dispatched

- **WHEN** a `StartShowcaseCommand` is dispatched for an existing scheduled showcase
- **THEN** the command is handled and a `ShowcaseStartedEvent` is emitted with showcaseId, duration, and startedAt

#### Scenario: FinishShowcaseCommand dispatched

- **WHEN** a `FinishShowcaseCommand` is dispatched for an existing started showcase
- **THEN** the command is handled and a `ShowcaseFinishedEvent` is emitted with showcaseId and finishedAt

#### Scenario: RemoveShowcaseCommand dispatched

- **WHEN** a `RemoveShowcaseCommand` is dispatched for an existing showcase
- **THEN** the title reservation is released, and a `ShowcaseRemovedEvent` is emitted with showcaseId and removedAt;
  if the showcase was started, a `ShowcaseFinishedEvent` is emitted before `ShowcaseRemovedEvent`

### Requirement: Showcase lifecycle state machine

Each showcase SHALL transition through the statuses SCHEDULED, STARTED, and FINISHED in order, and removal SHALL be
terminal. The final status of a removed showcase is REMOVED.

#### Scenario: Showcase finishes after being started

- **WHEN** a started showcase receives `FinishShowcaseCommand`
- **THEN** the showcase status is FINISHED

#### Scenario: Finished showcase cannot be restarted

- **WHEN** a `StartShowcaseCommand` is dispatched for a FINISHED showcase
- **THEN** the system rejects the command with error code ILLEGAL_STATE and message "Showcase is finished already"

#### Scenario: Scheduled showcase is started automatically by its deadline

- **WHEN** a showcase's scheduled start time is reached and the showcase is still SCHEDULED
- **THEN** a `StartShowcaseCommand` is dispatched for that showcase

#### Scenario: Started showcase is finished automatically by its deadline

- **WHEN** a started showcase's duration elapses and the showcase is still STARTED
- **THEN** a `FinishShowcaseCommand` is dispatched for that showcase

### Requirement: Idempotent command handling

Commands that describe a state the showcase is already in SHALL complete without emitting an event or error. Commands
that would put a showcase in an invalid lifecycle state SHALL be rejected with error code ILLEGAL_STATE.

#### Scenario: Duplicate schedule command is a no-op

- **WHEN** a `ScheduleShowcaseCommand` with an ID, title, startTime, and duration identical to the existing scheduled
  showcase is dispatched
- **THEN** the command completes without emitting an event and without error

#### Scenario: Rescheduling an existing showcase is rejected

- **WHEN** a `ScheduleShowcaseCommand` with an existing ID but different parameters is dispatched
- **THEN** the system rejects the command with error code ILLEGAL_STATE and message "Showcase cannot be rescheduled"

#### Scenario: Starting an already-started showcase is a no-op

- **WHEN** a `StartShowcaseCommand` is dispatched for an already STARTED showcase
- **THEN** the command completes without emitting an event and without error

#### Scenario: Finishing a finished showcase is a no-op

- **WHEN** a `FinishShowcaseCommand` is dispatched for an already FINISHED showcase
- **THEN** the command completes without emitting an event and without error

#### Scenario: Finishing a scheduled showcase is rejected

- **WHEN** a `FinishShowcaseCommand` is dispatched for a SCHEDULED showcase
- **THEN** the system rejects the command with error code ILLEGAL_STATE and message "Showcase must be started first"

### Requirement: Showcase title uniqueness

The system SHALL reserve a showcase's title before scheduling and SHALL reject a duplicate title.

#### Scenario: Duplicate title is rejected

- **WHEN** a `ScheduleShowcaseCommand` is dispatched with a title already in use by another showcase
- **THEN** the system rejects the command with error code TITLE_IN_USE and message "Given title is in use already", and
  no `ShowcaseScheduledEvent` is emitted

#### Scenario: Title reservation is released on removal

- **WHEN** a showcase is removed
- **THEN** its title is released and can be used by a newly scheduled showcase

### Requirement: Command validation

The system SHALL validate command payloads against bean validation constraints and SHALL reject invalid commands with
error code INVALID_COMMAND, including a map of property path to violation messages.

#### Scenario: Invalid command is rejected with property errors

- **WHEN** a command is dispatched whose payload violates its validation constraints (for example blank title, startTime
  not in the future, duration outside 1 to 10 minutes inclusive, or a `showcaseId` that is not a valid KSUID)
- **THEN** the system rejects the command with error code INVALID_COMMAND, message "Given command is not valid", and
  metadata mapping each offending property path to its validation message

### Requirement: Error translation for unknown aggregates

The system SHALL reject commands for unknown or removed aggregates with a normalized error code.

#### Scenario: Unknown showcase produces NOT_FOUND

- **WHEN** a `StartShowcaseCommand`, `FinishShowcaseCommand`, or `ScheduleShowcaseCommand` is dispatched for an ID with
  no aggregate
- **THEN** the system rejects the command with error code NOT_FOUND and message "No showcase with given ID"

#### Scenario: Command on a removed showcase produces ILLEGAL_STATE

- **WHEN** a command is dispatched for an ID of an already removed showcase
- **THEN** the system rejects the command with error code ILLEGAL_STATE and message "Showcase is removed already"

#### Scenario: Removing a removed or unknown showcase succeeds

- **WHEN** a `RemoveShowcaseCommand` is dispatched for a removed or non-existent showcase
- **THEN** the command completes as a success without error

### Requirement: Event persistence to the event store

Every emitted event SHALL be persisted to the PostgreSQL event store so it can be replayed to reconstruct aggregate
state.

#### Scenario: Scheduled event is persisted

- **WHEN** a `ShowcaseScheduledEvent` is emitted
- **THEN** the event is stored in the event store as a domain event entry

### Requirement: Saga deadlines and termination

The saga SHALL schedule a start deadline at the showcase's start time and a finish deadline at start time plus duration,
and SHALL end when the showcase is finished or removed.

#### Scenario: Saga schedules a finish deadline after start

- **WHEN** a `ShowcaseStartedEvent` is emitted
- **THEN** the saga schedules a deadline at startedAt plus duration that dispatches `FinishShowcaseCommand`

#### Scenario: Saga ends on finish or removal

- **WHEN** a `ShowcaseFinishedEvent` or `ShowcaseRemovedEvent` is emitted
- **THEN** the saga for that showcase ends and no further deadlines are processed

### Requirement: Kafka event publishing

Each emitted event SHALL be published to the Kafka topic `axon-showcase-events` with the aggregate identifier as the
record key and the Axon event message as the record value.

#### Scenario: Scheduled event is published to Kafka

- **WHEN** a `ShowcaseScheduledEvent` is emitted and persisted
- **THEN** the event is published to the Kafka topic `axon-showcase-events` keyed by the showcase ID