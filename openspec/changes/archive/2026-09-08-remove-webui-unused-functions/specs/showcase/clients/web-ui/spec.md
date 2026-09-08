## MODIFIED Requirements

### Requirement: Reconcile the list with the eventually-consistent read model

The UI SHALL reconcile the showcase list with the read model after a write, because the command and query sides are
eventually consistent: the gateway confirms a command before the projection has updated the read model. Reconciliation
SHALL be driven by the live event stream: when the UI receives a domain event, it SHALL poll the showcase list until the
event's effect is visible, so the list does not briefly show stale state. Concurrent reconciliations for the same
showcase SHALL coalesce into a single poll, and events replayed from history on a new SSE connection SHALL NOT trigger
reconciliation (the list is already fresh after an initial load). The event-stream reconciliation (`waitForEvent`) is
the only supported entry point; the UI SHALL NOT expose reconciliation helpers that bypass the event stream.

#### Scenario: A created showcase appears only once projected

- **WHEN** the gateway streams the SCHEDULED event for a new showcase over SSE
- **THEN** the UI polls the list until the new showcase is returned by the read model, then shows it

#### Scenario: A lifecycle action is reflected once projected

- **WHEN** the user starts, finishes, or removes a showcase and the gateway streams the matching event over SSE
- **THEN** the UI polls the list until the read model reflects the new status or removal

#### Scenario: A saga-triggered transition is reflected

- **WHEN** the saga automatically starts or finishes a scheduled showcase and the gateway streams the event over SSE
- **THEN** the UI reconciles the list against the read model so the new status appears without a reload

#### Scenario: Concurrent transitions share one poll

- **WHEN** several events for the same showcase arrive in quick succession (e.g. a saga starting a just-scheduled
  showcase)
- **THEN** the UI runs a single poll loop tracking the latest expected state, rather than one poll per event

#### Scenario: Reconciliation is driven by the event stream

- **WHEN** a producer of the showcase list wants to wait for the read model to reflect a state change
- **THEN** the only supported way is the event-stream reconciliation, and event-free helpers are not used
