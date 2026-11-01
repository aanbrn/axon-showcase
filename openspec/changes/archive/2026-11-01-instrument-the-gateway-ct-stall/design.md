# Design

## Context

`docs/ideas.md` `## 2026-10-06` holds the parked diagnosis: the gateway controller suites fail with a `TimeoutException`
at `WebTestClient.exchange()` on a loaded CI runner, the mechanism is unproven, and the recorded next step is "a
correlated per-request event sequence (subscribe, scheduling, execution, terminal or cancel) rather than more
snapshots". Three failures carry the signature — `40255154046` (2026-10-26), and this unit's `41257923140` and
`41260683762`, each `124 tests, 42 failed` on a cold build of roughly 24 minutes, each followed by a green warm rerun of
the same head in under 10 minutes.

Two local probes this unit ran narrow the field before any code is written:

- **`jcmd Thread.dump_to_file` does see virtual threads on 21.0.13, the JDK the suites run.** A scratch JVM on that JDK,
  holding one virtual thread from `newVirtualThreadPerTaskExecutor` and one from `Thread.ofVirtual()`, dumped both in
  **text** format with default settings (2 stacks, 3 `virtual` markers); with `-Djdk.trackAllThreads=false` only the
  mounted one remained. So the text dumps are adequate, the JSON switch buys nothing, and the parked entry's "no virtual
  thread appears in any snapshot" is a fact about the stall rather than an artefact of the instrument. The JDK matters
  for the shape of the line: 21 writes `#<id> "<name>" virtual`, while a newer JDK appends the state and a timestamp — a
  first probe here ran under the shell's JDK 25 outside the repository and validated the wrong shape; the watchdog's
  parse now accepts both.
- **`boundedElastic` is not a pool here.** The gateway runs `spring.threads.virtual.enabled: true`, and reactor-core
  3.6+ swaps `boundedElastic` for a thread-per-task scheduler (`BoundedElasticThreadPerTaskScheduler`, Reactor's own
  announcement of the 3.6 line). The parked experiment that shrank the pool to four therefore tested a mechanism that
  cannot exist in this configuration — a local pass on it was never evidence of anything.

Each instrument answers one question, and the ones that can only describe the host are labelled as such:

| Question a stall raises                                       | What answers it                                                    |
| ------------------------------------------------------------- | ------------------------------------------------------------------ |
| did the request reach the application at all?                 | the request table: a row exists, and its `last=` boundary          |
| did it close, and with what outcome?                          | the row's terminal, status and elapsed time                        |
| was a blocking task running, and how long did it wait or run? | the scheduler aggregate line: submitted, max queue, max run        |
| was the host starved, and were virtual threads pinned/parked? | the JFR summary: supplementary JVM-wide context, never per request |

Two of those are aggregate and are labelled so: a test-scope instrument cannot carry a request identity into the
blocking scheduler, and a first version that attached a task to an exchange by its submitting thread was measured to
attribute nothing (`unattributed` equalled `submitted` on every task the probe's controller ran). The design therefore
reports the scheduler figures beside the request table and does not claim a per-request queue or run time.

## Goals / Non-Goals

**Goals:**

- Make the next CI stall answer what this scope can answer from its own output: whether a request was admitted, how each
  admission ended, and what the host and the blocking scheduler were doing around it.
- Prove locally that each instrument fires, so "no output" cannot be confused with "instrument never worked".

**Scope boundary, stated rather than implied:** localization _inside_ an admitted, unfinished request — whether it
waited for a blocking worker or sat in a returned-but-incomplete publisher — is **not** delivered. The scheduler figures
are aggregate because a request identity does not reach the scheduler from a test-scope instrument, and a first
attribution attempt measured nothing. The unit therefore narrows the parked objective rather than completing it, and its
report says so; the owner accepts this narrowing before the change ships.

**Non-Goals:**

- A parallelism cap, a timeout raise, or any change to what the tests verify: the owner chose the diagnostic first, and
  a cap stays an experiment for a later unit (a bounded A/B on cold builds).
- Changing the JVM flags, the suites' timeouts, or the `jcmd` snapshot format — the probe above shows the snapshot is
  already adequate.
- Reproducing the stall on demand: the shipped configuration did not stall locally, and one run of a heavier draft did
  (the Decision below records it), so a local green run is not evidence against the stall and a local reproduction is
  not a property this change claims to have.

## Decisions

**Instrument the request's own path first, because that is what the parked diagnosis asks for.** A `WebFilter` in the
slice records arrival and terminal (completed/failed/cancelled) with `System.nanoTime()` and the subscribing thread's
identity, so a stalled request is a row with an arrival and no terminal — the one fact none of the existing dumps
provide. The filter is installed through one shared `@TestConfiguration` (`StallDiagnosticsConfig`) that both slices
import, rather than in each slice's own `TestConfig`: the wiring then lives in one place, and the registration control
asserts both imports, so a slice that drops it fails instead of silently losing its failure output.

**Record the blocking scheduler's figures as an aggregate, and say that they are one.** A filter alone brackets an
exchange without locating the gap, and the parked diagnosis wants to tell "waiting for a worker" from "inside the
controller". The gateway routes every controller method to the blocking scheduler
(`ShowcaseBlockingExecutionConfigurer`), so the recorder decorates that scheduler's executor —
`Schedulers.addExecutorServiceDecorator` — and stamps each task's submission, start and return. Queue wait is measured
from each task's <em>eligible</em> time (submission plus a scheduled delay) rather than from submission, so an
intentional delay is not reported as queueing; periodic tasks are counted rather than timed, since every invocation
would reuse one submission stamp; and each maximum carries its observation count, so an unavailable maximum reads as
`unavailable` rather than `0`. Attaching a task to the exchange that caused it needs a request identity the scheduler
does not carry: measured, the submitting thread never matched the admitted row (`unattributed` equalled `submitted`), so
the figures are reported as an aggregate beside the table and no row claims a queue or run time. _Alternative rejected:_
inventing an identity by time window, which would attribute another request's task whenever two overlap.

**Record the JFR figures only when a property asks for them, and report the recording's health.** The recording is
supplementary context, its cost on a loaded runner is unmeasured, and an always-on recorder would change the load the
stall depends on. `-Dshowcase.diagnostics.jfr=true` enables it; otherwise the summary states that it is disabled, and
when it is enabled it states the event and CPU-sample counts, or the reason it is unavailable — so an absent observation
is never read as a zero. Aggregation state is bounded and a start or stream failure disables the summary instead of
failing a test.

**Use a live `jdk.jfr.consumer.RecordingStream` rather than a file-based recording.** The stream starts with the suite
and delivers each event to an in-process handler, so the summary is a set of counters the failure printer renders, with
no file, no `jfr` CLI, and no artifact upload to CI. _Alternatives rejected:_ `Recording` plus `RecordingFile` — the JDK
21 `Recording` has no live event accessor, only `dump(Path)` and `getStream(...)` — and `-XX:StartFlightRecording` plus
a workflow artifact upload, which touches `ci.yml` and its spec to produce a binary nobody reads during a failure.

**Record pinning as a count, parked threads with their frames, and the CPU load as a suite peak.** The JDK records a
pinning event's stack trace only under `-Djdk.tracePinnedThreads` — measured here: the event fires with
`getStackTrace()` null — so the summary states the pinning count and does not promise frames it cannot get; park events
carry their frames, so those are rendered. The CPU peak is the highest sample anywhere in the run, not a window around
the failing test, and the summary says so.

**Record neither the per-virtual-thread start and end events.** The two task-lifecycle events fire once per virtual
thread, and this application creates one per blocking task, so they are the highest-volume part of the recording; no
question this design asks needs them, and a sampled count could not attribute a thread to a request anyway. A first
version enabled all three event families, and one of its runs stalled locally with the CI signature (17 of 130 failing
at the 30 s `WebTestClient` timeout) before two later runs of the same configuration passed; that is one observation,
not a cause, but it is enough to keep the highest-volume events out of a diagnostic that must not change the load it
measures.

**Print a counted line for virtual threads in the existing dump.** The probes show a text dump can hold them, so the
question a reader has ("were there any?") becomes a number the printer states, instead of an absence they must
interpret. This is a small, additive change to `ThreadDumpWatchdog`, whose snapshot header prints it.

**Verify with a control that exercises each instrument's output path.** The instruments only produce output on a stall,
and the suite does not stall locally in its shipped configuration, so a green suite proves nothing about them: the
control drives a request through a slice, parks a virtual thread, and asserts the rendered request table and the JFR
counts. That is the `check-and-test-authoring` rule — a check is evidence only once it has been shown to fail — applied
to a diagnostic rather than to a gate.

## Risks / Trade-offs

- **The recorder's memory grows with the suite's request count** → the table keeps a bounded ring of the most recent
  requests plus the open ones, and each row is a small record of primitives.
- **The recording is off unless a run asks for it** → the diagnostics that must always run — the request table and the
  counted dump line — carry no recording; the JFR figures are enabled per run, so their cost is a decision rather than a
  default. The paired enabled/disabled cold-run measurement the advisor asked for stays parked in `docs/ideas.md`.
- **A JFR recording costs CPU when enabled** → it holds three events, two thresholded at 20 ms, and the park event's
  stack frames are what the summary reads (the pinning event carries none); the per-thread lifecycle events are
  deliberately absent (see Decisions), and the summary is printed only on failure.
- **A filter in the slice changes what the tests exercise** → the filter only observes; it sits at the outermost
  precedence and passes the exchange through unchanged, and the control asserts a request still completes normally.
- **The instruments may still not find the mechanism** → the follow-up is the parked parallelism A/B, and the design
  records what each instrument rules out so the next attempt does not repeat a refuted hypothesis.

## Migration Plan

None: test-only, taking effect on the next run of the suite.

## Claims table

| Claim                                                                                                          | Source                                                                                                                                                                                                                                                                                                     |
| -------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Three CI failures share the `124 tests, 42 failed` signature on cold builds                                    | the run logs of `40255154046`, `41257923140`, `41260683762`, each in its `:showcase-api-gateway:componentTest` task                                                                                                                                                                                        |
| Warm reruns of the same heads pass                                                                             | `gh pr checks 785` after `gh run rerun --failed`: 9m42s and 9m33s, `build pass`                                                                                                                                                                                                                            |
| A text `jcmd` dump includes unmounted virtual threads by default on this JDK                                   | the scratch probe `VtDumpProbe` on `21.0.13`: text `parkNanosFrames=2` with the default, `=1` with `-Djdk.trackAllThreads=false`                                                                                                                                                                           |
| `boundedElastic` is thread-per-task under `spring.threads.virtual.enabled`                                     | `showcase-api-gateway/src/main/resources/application.yml` (`threads.virtual.enabled: true`) and Reactor's 3.6 announcement of `BoundedElasticThreadPerTaskScheduler`                                                                                                                                       |
| The REST suite's client timeout is 30s and the telemetry suite's is the 5s default                             | `ShowcaseRestControllerCT`'s `@AutoConfigureWebTestClient(timeout = "30s")` beside `TelemetryControllerCT`'s absent attribute                                                                                                                                                                              |
| The JDK 21 `Recording` API has no live event accessor, so the implementation streams through `RecordingStream` | `javap jdk.jfr.Recording` on 21.0.13 shows `dump(Path)` and `getStream(Instant, Instant)` and no `getEvents()`; `javap jdk.jfr.consumer.RecordingStream` shows `enable`/`onEvent`                                                                                                                          |
| The watchdog's pattern matches both dump shapes the two JDKs emit                                              | the shipped `^#\d+ ".*" virtual(\s\|$)` against a JDK 21.0.13 dump (`#30 "direct-of-virtual" virtual`, no state) and a JDK 25.0.5 one (`#26 "" virtual TIMED_WAITING …`); the control asserts both shapes and the `"my virtual worker"` decoy, and was red against the earlier trailing-space-only pattern |
| The full event set stalled the suite locally once, with the CI signature                                       | one local `:showcase-api-gateway:componentTest --rerun`: 130 tests, 17 failed, each `Timeout on blocking read for 30000000000 NANOSECONDS`; the same configuration passed on two later runs, so this is an observation, not a cause                                                                        |
| A client timeout leaves the row open at its last boundary                                                      | the control `aStalledRequestIsRenderedWithItsLastBoundary`: a never-completing-until-released `Sinks.Empty` handler with a 400 ms client timeout renders `OPEN … last=ADMITTED`                                                                                                                            |
| The blocking scheduler is decoratable from test scope                                                          | `Schedulers.addExecutorServiceDecorator` in `RequestPhases.instrumentBlockingScheduler`, exercised by the header's `blocking tasks submitted=` figures                                                                                                                                                     |
| No capability spec covers this diagnostic                                                                      | `git grep -ic` over `openspec/specs/**/spec.md`: `thread dump` 0, `ThreadDump` 0, `JFR` 0, `VirtualThread` 0, `intermittent` 0                                                                                                                                                                             |

### Dismissals

`scripts/claim-scan.py`'s candidates are dismissed by kind, one reason each:

- `absolute` — the quantifier words sit in statements whose sources the table states.
- `count` — the scanned numerals are figures the table's own rows source, or version strings the scanner reads as
  counts.
- `path` — a scanned path or command names the repository surface the change edits or the source a row cites.
- `cross-ref` — the scanned identifiers are event names and JDK flags this design defines or quotes.
