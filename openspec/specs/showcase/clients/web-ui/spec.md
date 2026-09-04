# showcase/clients/web-ui Specification

## Purpose

Documents the behavior of the standalone web UI: a browser application that browses and drives showcase lifecycle
actions through the gateway REST API and renders a live event timeline fed by the gateway's SSE stream, so the
CQRS/Event-Sourcing pipeline can be demonstrated visually.

## Requirements

### Requirement: Browse showcases

The UI SHALL render the list of showcases returned by the gateway's showcase listing endpoint, displaying for each
showcase its title, status (`SCHEDULED`, `STARTED`, `FINISHED`), duration, and a status-aware timestamp: the scheduled
start time, the expected finish time once started, or the actual finish time once done.

#### Scenario: The showcase list is displayed

- **WHEN** the UI loads the showcase list
- **THEN** each showcase is shown with its title, status, duration, and the status-aware timestamp

#### Scenario: The list reflects status changes

- **WHEN** a showcase's status changes
- **THEN** the UI reflects the new status on the next list refresh, without a full page reload

### Requirement: Reconcile the list with the eventually-consistent read model

The UI SHALL reconcile the showcase list with the read model after a write, because the command and query sides are
eventually consistent: the gateway confirms a command before the projection has updated the read model. Reconciliation
SHALL be driven by the live event stream: when the UI receives a domain event, it SHALL poll the showcase list until
the event's effect is visible, so the list does not briefly show stale state. Concurrent reconciliations for the same
showcase SHALL coalesce into a single poll, and events replayed from history on a new SSE connection SHALL NOT trigger
reconciliation (the list is already fresh after an initial load).

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

### Requirement: Drive lifecycle actions

The UI SHALL let a user create a showcase (title, start time, duration) and start, finish, or remove an existing
showcase, by invoking the corresponding gateway REST endpoints. Validation errors returned by the gateway SHALL be
surfaced to the user.

#### Scenario: A showcase is created

- **WHEN** the user submits a valid new-showcase form
- **THEN** the UI calls the gateway schedule endpoint and the new showcase appears in the list

#### Scenario: A showcase is started, finished, or removed

- **WHEN** the user triggers start, finish, or remove for a showcase
- **THEN** the UI calls the corresponding gateway endpoint and reflects the result

#### Scenario: Validation errors are surfaced

- **WHEN** the gateway rejects an action with a validation error
- **THEN** the UI displays the error so the user can correct the input

### Requirement: Validate the form before submission

The UI SHALL validate the create-showcase form client-side before invoking the gateway: a non-empty title of at most
255 characters, a start time in the future, and a duration from the supported set. The start-time picker SHALL
pre-fill with a future time and roll forward to the next minute until the user edits it, so the value is always in
the future.

#### Scenario: An invalid form is not submitted

- **WHEN** the user submits a form with an empty title, an over-long title, a past start time, or an unsupported
  duration
- **THEN** the UI shows the validation message and does not call the gateway

#### Scenario: The start time is always future

- **WHEN** the user leaves the start-time picker untouched
- **THEN** the picker shows a future time, advancing to the next minute at each minute boundary

### Requirement: Render a per-showcase history timeline

The UI SHALL render a timeline for a selected showcase built from the read model's timestamps: scheduling, start, and
finish. This history is derived from the read model only — the UI SHALL NOT read events from the event store or a
projected events index.

#### Scenario: The history timeline shows the read-model timestamps

- **WHEN** the user selects a showcase
- **THEN** the timeline shows the showcase's scheduling, start, and finish markers from its read-model timestamps

### Requirement: Display live events over SSE

The UI SHALL subscribe to the gateway's live event stream (Server-Sent Events) and append each received domain event to
the relevant showcase's timeline as it arrives. The live stream is the only event source; history and live events are
combined in the timeline without duplication.

#### Scenario: Live events append to the timeline

- **WHEN** the gateway pushes a domain event over SSE
- **THEN** the UI appends the event to the matching showcase's timeline

#### Scenario: The UI tolerates a disconnected stream

- **WHEN** the SSE connection drops
- **THEN** the UI keeps the already-rendered history and live events and reconnects to the stream