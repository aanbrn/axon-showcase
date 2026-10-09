# showcase/clients/web-ui Specification

## Purpose

Documents the behavior of the standalone web UI: a browser application that browses and drives showcase lifecycle
actions through the gateway REST API, renders a live event timeline fed by the gateway's SSE stream, propagates W3C
Trace Context on its API calls so a page load's requests join one trace through the pipeline, and measures and reports
its own client-side experience (Core Web Vitals and JavaScript errors) — all so the CQRS/Event-Sourcing pipeline can be
demonstrated visually and followed end to end.

## Requirements

### Requirement: Showcase browsing

The UI SHALL render the list of showcases returned by the gateway's showcase listing endpoint, displaying for each
showcase its title, status (`SCHEDULED`, `STARTED`, `FINISHED`), duration, and a status-aware timestamp: the scheduled
start time, the expected finish time once started, or the actual finish time once done.

#### Scenario: The showcase list is displayed

- **WHEN** the UI loads the showcase list
- **THEN** each showcase is shown with its title, status, duration, and the status-aware timestamp

#### Scenario: The list reflects status changes

- **WHEN** a showcase's status changes
- **THEN** the UI reflects the new status on the next list refresh, without a full page reload

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

### Requirement: Lifecycle action dispatch

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

### Requirement: Create-form validation

The UI SHALL validate the create-showcase form client-side before invoking the gateway: a non-empty title of at most 255
characters, a start time in the future, and a duration from the supported set. The start-time picker SHALL pre-fill with
a future time and roll forward to the next minute until the user edits it, so the value is always in the future.

#### Scenario: An invalid form is not submitted

- **WHEN** the user submits a form with an empty title, an over-long title, a past start time, or an unsupported
  duration
- **THEN** the UI shows the validation message and does not call the gateway

#### Scenario: The start time is always future

- **WHEN** the user leaves the start-time picker untouched
- **THEN** the picker shows a future time, advancing to the next minute at each minute boundary

### Requirement: Per-showcase history timeline

The UI SHALL render a timeline for a selected showcase built from the read model's timestamps: scheduling, start, and
finish. This history is derived from the read model only — the UI SHALL NOT read events from the event store or a
projected events index.

#### Scenario: The history timeline shows the read-model timestamps

- **WHEN** the user selects a showcase
- **THEN** the timeline shows the showcase's scheduling, start, and finish markers from its read-model timestamps

### Requirement: Live event display over SSE

The UI SHALL subscribe to the gateway's live event stream (Server-Sent Events) and append each received domain event to
the relevant showcase's timeline as it arrives. The live stream is the only event source; history and live events are
combined in the timeline without duplication.

#### Scenario: Live events append to the timeline

- **WHEN** the gateway pushes a domain event over SSE
- **THEN** the UI appends the event to the matching showcase's timeline

#### Scenario: The UI tolerates a disconnected stream

- **WHEN** the SSE connection drops
- **THEN** the UI keeps the already-rendered history and live events and reconnects to the stream

### Requirement: Trace-context propagation on API calls

The UI SHALL propagate W3C Trace Context on every fetch-based gateway request, so a page load's requests join a single
trace the gateway continues through the command/query pipeline. (The SSE stream is a browser `EventSource`, which cannot
set request headers, so it stays outside the trace.) Each page load SHALL use one trace id, each request a distinct
parent id, and the trace SHALL be marked sampled, so the pipeline records it rather than dropping it.

#### Scenario: A request carries a W3C traceparent

- **WHEN** the UI sends a fetch-based request to the gateway
- **THEN** the request carries a `traceparent` header of the form `00-<32 hex>-<16 hex>-01`

#### Scenario: One page load shares a trace id

- **WHEN** a page load issues several gateway requests
- **THEN** each request's `traceparent` carries the same trace id and a distinct parent id

#### Scenario: The trace is marked sampled

- **WHEN** the UI builds a `traceparent`
- **THEN** its version is `00` and its flags are `01`, so the gateway continues and records the trace

### Requirement: Client-side experience measurement and reporting

The UI SHALL measure the Core Web Vitals (LCP, INP, CLS, FCP, TTFB) and capture uncaught JavaScript errors and unhandled
promise rejections, and SHALL report them to the gateway's telemetry endpoint tagged with the current route. Reporting
SHALL be fire-and-forget and SHALL NOT degrade or block the page. (The report's trace context is owned by the
"Trace-context propagation on API calls" requirement, which covers every fetch-based gateway request.)

#### Scenario: A measured vital is reported

- **WHEN** the browser reports a Core Web Vital for the page
- **THEN** the UI sends the vital to the gateway telemetry endpoint

#### Scenario: A client-side error is reported

- **WHEN** an uncaught JavaScript error or an unhandled promise rejection occurs
- **THEN** the UI reports it to the gateway telemetry endpoint

#### Scenario: A failed report leaves the page working

- **WHEN** the telemetry request fails or is unavailable
- **THEN** the page continues to work without a user-visible error
