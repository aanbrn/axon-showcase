# Proposal

## Why

The gateway controller component tests (`ShowcaseRestControllerCT`, `TelemetryControllerCT`) stall on CI: 42 of 124
cases time out at the `WebTestClient` `.exchange()` on a cold build (runs `41257923140`, `41260683762`, `40255154046`),
while warm reruns of the identical tree pass. Two diagnostics exist and neither answers where progress stops: the
failure-time dump arrives after the timeout, and the during-run `jcmd` snapshots contain no request-path thread. The
parked diagnosis in `docs/ideas.md` asks for the next step in as many words — a correlated per-request event sequence
rather than more snapshots.

## What Changes

- **`showcase-api-gateway/src/componentTest`** gains a request-timeline recorder (`WebFilter`, installed by both slices'
  test config): each request's admission, terminal, elapsed time, subscribing thread and `last=ADMITTED|TERMINAL`
  boundary, printed beside an aggregate line for the blocking scheduler the gateway routes controller methods to (tasks
  submitted, maximum queue wait, maximum run time) — aggregate because a test-scope instrument cannot carry a request
  identity into the scheduler.
- **A JFR summary** is recorded for the suite when `-Dshowcase.diagnostics.jfr=true` enables it, and printed on failure:
  the recording's health, the event and CPU-sample counts, the pinning-event count (`jdk.VirtualThreadPinned`), the
  parked threads with their top frames, and the suite's peak machine and process CPU load. The pinning count carries no
  frames: the JDK records a pinning event's stack trace only under `-Djdk.tracePinnedThreads`, which this suite does not
  set.
- **The during-run snapshot** gains a counted line stating how many virtual threads it holds, so an absence becomes a
  number rather than an inference.
- **`docs/ideas.md`** records what two local probes settled: `jcmd Thread.dump_to_file` does include virtual threads on
  this JDK with default tracking (so the earlier "no virtual thread in the snapshot" reading is real), and with
  `spring.threads.virtual.enabled` Reactor's `boundedElastic` is thread-per-task (so the parked "shrink the pool"
  experiment tested a mechanism that no longer exists).
- A **control** test drives a request through a slice and asserts the recorder and the JFR summary produce their output,
  so the instruments are proven to fire rather than assumed to.

## Capabilities

### New Capabilities

None — no capability spec covers test diagnostics for a stalled suite (`skip_specs: true`).

### Modified Capabilities

None.

## Scope

This change delivers **partial** stall diagnostics: admission, outcome, and the host/blocking-scheduler context around
them. Localization _inside_ an admitted, unfinished request — waiting for a blocking worker versus sitting in a
returned-but-incomplete publisher — is not delivered: a request identity does not reach the blocking scheduler from a
test-scope instrument, so those figures are aggregate. The parked objective stays open on that half.

## Impact

- **Test-only**: no production class, configuration, or dependency changes; nothing new ships in a service image.
- **No behaviour change to the suites**: the recorder and the recording are passive, and the extra output appears only
  on failure (the recorder's table is bounded).
