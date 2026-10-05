## MODIFIED Requirements

### Requirement: Read-model reconciliation of the showcase list

The UI SHALL reconcile the showcase list with the read model after a write, because the command and query sides are
eventually consistent: the gateway confirms a command before the projection has updated the read model. Reconciliation
SHALL be driven by the live event stream: a burst of domain events SHALL debounce into a single list refetch, and the
refetch SHALL repeat until every pending event's effect is visible, so the list does not briefly show stale state.
Events replayed from history on a new SSE connection SHALL NOT trigger refetching (the list is already fresh after an
initial load). The showcase entity SHALL own reconciliation and expose one event-stream-driven entry point; the UI SHALL
NOT expose reconciliation helpers that bypass the event stream.

#### Scenario: A created showcase appears only once projected

- **WHEN** the gateway streams the SCHEDULED event for a new showcase over SSE
- **THEN** the UI refetches the list until the new showcase is returned by the read model, then shows it

#### Scenario: A lifecycle action is reflected once projected

- **WHEN** the user starts, finishes, or removes a showcase and the gateway streams the matching event over SSE
- **THEN** the UI refetches the list until the read model reflects the new status or removal

#### Scenario: A saga-triggered transition is reflected

- **WHEN** the saga automatically starts or finishes a scheduled showcase and the gateway streams the event over SSE
- **THEN** the UI reconciles the list against the read model so the new status appears without a reload

#### Scenario: Concurrent transitions share one poll

- **WHEN** several events for the same showcase arrive in quick succession (e.g. a saga starting a just-scheduled
  showcase)
- **THEN** the UI coalesces them into a single reconciliation tracking the latest expected state, rather than one
  refetch per event

#### Scenario: A burst across showcases triggers one refetch

- **WHEN** several domain events arrive in quick succession, including events for different showcases
- **THEN** the UI debounces them into a single list refetch rather than one refetch per event

#### Scenario: Replayed history does not trigger reconciliation

- **WHEN** the UI opens the event stream and the gateway replays recent history
- **THEN** the UI does not refetch for events that occurred before the UI opened the stream

#### Scenario: Reconciliation is driven by the event stream

- **WHEN** a producer of the showcase list wants to wait for the read model to reflect a state change
- **THEN** the only supported way is the event-stream reconciliation, and event-free helpers are not used
