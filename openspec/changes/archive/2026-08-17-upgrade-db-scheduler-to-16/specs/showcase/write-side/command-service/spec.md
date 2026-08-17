## MODIFIED Requirements

### Requirement: Showcase lifecycle state machine

Each showcase SHALL transition through the statuses SCHEDULED, STARTED, and FINISHED in order, and removal SHALL be
terminal. A removed aggregate is marked as deleted and can no longer process lifecycle commands. The transitions from
SCHEDULED to STARTED and from STARTED to FINISHED SHALL occur automatically at the scheduled start time and after the
configured duration, without any manual command dispatch.

#### Scenario: Showcase finishes after being started

- **WHEN** a started showcase receives `FinishShowcaseCommand`
- **THEN** the showcase status is FINISHED

#### Scenario: Finished showcase cannot be restarted

- **WHEN** a `StartShowcaseCommand` is dispatched for a FINISHED showcase
- **THEN** the system rejects the command with error code ILLEGAL_STATE and message "Showcase is finished already"

#### Scenario: Scheduled showcase is started automatically by its deadline

- **WHEN** a scheduled showcase's start time is reached, no `StartShowcaseCommand` is dispatched by a caller, and the
  showcase is still SCHEDULED
- **THEN** a `StartShowcaseCommand` is dispatched for that showcase by the saga and the showcase becomes STARTED

#### Scenario: Started showcase is finished automatically by its deadline

- **WHEN** a started showcase's duration elapses, no `FinishShowcaseCommand` is dispatched by a caller, and the showcase
  is still STARTED
- **THEN** a `FinishShowcaseCommand` is dispatched for that showcase by the saga and the showcase becomes FINISHED

### Requirement: Saga deadlines and termination

The saga SHALL schedule a start deadline at the showcase's start time and a finish deadline at start time plus duration,
SHALL dispatch the corresponding commands when those deadlines fire, and SHALL end when the showcase is finished or
removed. The deadlines SHALL fire through the persistent scheduler: scheduling a deadline persists it, and the scheduled
command is dispatched even if no further commands are received.

#### Scenario: Saga schedules a start deadline on scheduling

- **WHEN** a `ShowcaseScheduledEvent` is emitted
- **THEN** the saga schedules a deadline at the showcase's start time that dispatches `StartShowcaseCommand`

#### Scenario: Saga schedules a finish deadline after start

- **WHEN** a `ShowcaseStartedEvent` is emitted
- **THEN** the saga schedules a deadline at startedAt plus duration that dispatches `FinishShowcaseCommand`

#### Scenario: Start deadline fires through the real scheduler

- **WHEN** a showcase is scheduled with a start time in the near future and no start command is dispatched by a caller
- **THEN** the start deadline fires through the persistent scheduler, the saga dispatches `StartShowcaseCommand`, and
  the showcase becomes STARTED

#### Scenario: Finish deadline fires through the real scheduler

- **WHEN** a showcase is started and its duration elapses without a finish command dispatched by a caller
- **THEN** the finish deadline fires through the persistent scheduler, the saga dispatches `FinishShowcaseCommand`, and
  the showcase becomes FINISHED

#### Scenario: Saga ends on finish or removal

- **WHEN** a `ShowcaseFinishedEvent` or `ShowcaseRemovedEvent` is emitted
- **THEN** the saga for that showcase ends and no further deadlines are processed