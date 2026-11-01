# Tasks

Workspace: unit=instrument-the-gateway-ct-stall; path=/Users/afanasyev/Development/axon-showcase;
branch=instrument-the-gateway-ct-stall; writer=ses_e6bb3d98effeNURMqTdoGwAu9f

## 1. The request-phase recorder

- [x] 1.1 Add `showcase.api.diagnostics.RequestPhases`: a `WebFilter` recording each exchange's arrival and terminal
      (completed, failed, cancelled) with a monotonic timestamp, the elapsed milliseconds, the outcome status, and the
      subscribing thread's name and virtual-thread identity, kept in a bounded ring plus the set of still-open requests.
- [x] 1.2 Render it as a failure-time table: the open requests, then the most recent completed ones, so a stalled
      request reads as an arrival with no terminal.
- [x] 1.3 Install the filter in both slices through one shared `@TestConfiguration` (`StallDiagnosticsConfig`, imported
      by `ShowcaseRestControllerCT` and `TelemetryControllerCT`) at the outermost precedence, passing every exchange
      through unchanged. The registration control asserts both imports by reflection, so a slice that drops the import
      fails rather than silently losing the recorder.

## 2. The JFR summary

- [x] 2.1 Add `showcase.api.diagnostics.JfrSummary`: a lazily started `jdk.jfr.consumer.RecordingStream` enabling
      `jdk.VirtualThreadPinned` and `jdk.ThreadPark` (20 ms threshold each) and `jdk.CPULoad`, delivered to in-process
      counters. The two per-virtual-thread lifecycle events are deliberately absent: they fire once per task, no
      question this design asks needs them, and a sampled count cannot attribute a thread to a request, and a first
      version that enabled them stalled locally once with the CI signature before two later runs passed.
- [x] 2.2 Render a bounded summary — a pinning count, park counts by top frame, and the suite's peak CPU — and close the
      recording at JVM shutdown. The pinning count carries no frames: the JDK records them only under
      `-Djdk.tracePinnedThreads`, measured as `getStackTrace()` null on the event this JDK emits.
- [x] 2.3 Add `JfrSummaryExtension` (`BeforeAllCallback`) and register it in the extension service file, so the
      recording is running before the suite's first test rather than starting at the first failure.

## 3. The during-run snapshot

- [x] 3.1 Print, in the during-run snapshot's header, a counted line for the virtual threads the `jcmd` dump lists, so
      their absence is a number rather than an inference; the parse is asserted over a real dump sample, and a platform
      thread merely named `virtual` is not counted.
- [x] 3.2 Print the request table and the JFR summary from `ThreadDumpOnFailure` ahead of the thread dump, so a
      failure's output opens with the two answerable questions.
- [x] 3.3 Record the blocking scheduler's figures: decorate the `boundedElastic` executor
      (`Schedulers.addExecutorServiceDecorator`) so each task's submission, start and return are stamped, and print the
      aggregate line (submitted, max queue, max run). Per-request attribution was attempted first and measured to
      attribute nothing — the submitting thread never matched the admitted row, `unattributed` equalling `submitted` —
      so the figures are aggregate and labelled as such.

## 4. The control

- [x] 4.1 Add `DiagnosticsControlCT`: drive one request through a controller of the slice and assert the recorder
      rendered it with a completion and an elapsed time, and that a request left open is rendered as open. The class
      carries nine tests: the completed row, the failed row, the open row, a subscribed request that never completes,
      the two scheduler-figure branches, a blocking request's aggregates, a failed recording's reason, the
      property-gated pinning/park control, and the property-gated disabled-render case; the suite is 135 declared (134
      executed, one skipped by property) — re-derived after the final control was added.
- [x] 4.2 Assert the JFR summary's pinning and park counters rise above the run's baseline after the control's own
      virtual thread blocks inside a monitor, so the recording is proven to capture events rather than merely start.
- [x] 4.3 Prove the controls fail when the instrument is absent. Measured on the four-test version of the class: with
      the `RequestPhases` bean removed from the control's config, `4 tests completed, 2 failed` (the completed-request
      and failed-request assertions); with `jdk.VirtualThreadPinned` left un-enabled, `4 tests completed, 1 failed` (the
      JFR assertion). Both restored. Later controls were proven red as they were added — the parse control against the
      trailing-space pattern, the run figure against its submission gate.
- [x] 4.4 Assert the dump parse over both real line shapes — this JDK's `#<id> "<name>" virtual` and a newer JDK's
      state-and-timestamp form — and that a platform thread merely named `virtual` is not counted; the control is red
      against the pattern that only accepted the newer shape.
- [x] 4.5 Give the recorder a removal seam (`clear()`) and use it in the control's `@BeforeEach` and after the open-row
      assertion, so a synthetic exchange cannot outlive its test or evict a real one.

- [x] 4.6 Subscribe a request whose handler never completes, with a short client timeout, and assert the row renders its
      last boundary — the shape a real stall leaves behind. Observed: `OPEN … last=ADMITTED`, so a client timeout does
      not cancel the exchange in the filter; the stalled source is released in cleanup. A second control runs a blocking
      handler and asserts the aggregate figures move and the row closes at `last=TERMINAL`.
- [x] 4.7 Assert that the JFR summary states `recording: disabled` when the property is absent, so an unrun recording is
      never read as zero counters; report the counters as operational and suite-wide (never reset by a test, so a
      control's own events are part of the figures it reads); and report a closed or failed stream as such rather than
      as `running`.

- [x] 4.8 Give the diagnostics package a `package-info.java` with `@NullMarked`, as the repository's NullAway-compiled
      source sets do (`git ls-files '*package-info.java'` tracks 23), so non-null is the default and only the genuinely
      absent fields (a row's terminal, status and failure, the recording and its reasons) carry `@Nullable`. The owner
      caught the first version annotating each member instead.

## 5. Verification

- [x] 5.1 Run `./gradlew :showcase-api-gateway:componentTest` in both configurations and confirm each is green with no
      diagnostic output: 135 declared tests, 134 executed and one skipped by property, 0 failures — the JFR control runs
      with `-Dshowcase.diagnostics.jfr=true`, the disabled-render control without it.
- [x] 5.2 Run `./gradlew :showcase-api-gateway:check -PskipITs -Pcoverage.gate.enabled=false` for the module's gates.
- [x] 5.3 Run `./gradlew spotlessApply` after the last edit and `spotlessCheck`, plus `openspec validate --all` and the
      manual 120-character check over the whole changed set.
- [x] 5.4 Run the review gate's `review-quick` loop, repeating until clean, and record the deep-review routing decision.
      Six rounds: 9, 5, 4, 3, 1, clean. Routing `REQUIRED` (`[security-declaration]`), so the deep review ran: pass 1
      raised 4 findings (1 blocking) and pass 2 confirmed them fixed and raised 1 more, fixed in turn — section 7.
- [x] 5.5 Ask the owner for the manual review pass, and for **acceptance of the narrowed scope**: this unit delivers
      admission/outcome rows plus aggregate blocking-scheduler and host context, and deliberately does not deliver
      localization inside an admitted, unfinished request (no request identity reaches the scheduler from test scope).
      The parked objective stays open on that half. Asked after the deep review and the advisor consultation closed; the
      deep review's step to ask before editing was not followed for pass 1's findings — disclosed, with an offer to
      revert any of them.

## 6. The change's own records

- [x] 6.1 Record `docs/ideas.md`'s two corrected facts: a text `jcmd` dump does include virtual threads with default
      tracking, and `boundedElastic` is thread-per-task under virtual threads, so the parked pool-shrink experiment
      tested a mechanism that no longer exists.
- [x] 6.2 Record the pre-write derivation pass as a task (every unit owes it). It ran: the claims table is in
      `design.md`, and its rows come from the run logs, the scratch dump probe, the application configuration, and the
      spec-corpus grep.
- [x] 6.3 Write `docs/process-defects/instrument-the-gateway-ct-stall.md`, or record that the unit observed none. Three
      records: a probe read from an up-to-date task, a probe that ran under the shell's JDK 25 rather than the suite's
      21, and the review command's stop step being skipped.

## 7. The deep review

- [x] 7.1 Routing:
      `scripts/review-routing.py --unit instrument-the-gateway-ct-stall --change     instrument-the-gateway-ct-stall`
      returned `REQUIRED` on the signal `[security-declaration]` — `DiagnosticsControlCT` declares a
      `SecurityWebFilterChain`. The pass judged it appropriate (test-only, `permitAll`, identical to both sibling
      slices, and production security lives in `main`).
- [x] 7.2 Pass 1 (initial) findings and their statuses: - **Blocking** — "the pinning half cannot fire on this JDK, and
      its control passes vacuously". Status: `FIXED_AND_VERIFIED`, with one half `REJECTED`: measured on 21.0.13,
      `jdk.VirtualThreadPinned` **does** fire (`PinMatrix`: an event at a 20 ms threshold and a 150 ms monitor-blocked
      sleep), so "cannot fire" is refuted; the valid halves were the vacuous control and the promised frames. The
      control now asserts a **delta** over the run's baseline instead of a global count, and the summary states the
      pinning count without frames — measured: `getStackTrace()` is null on the event this JDK emits unless
      `-Djdk.tracePinnedThreads` is set, which the suite does not. - **Should-fix** — the CPU figure is a suite peak,
      not the failing test's window. Status: `FIXED_AND_VERIFIED` — the summary and the design's mechanism table now say
      so, rather than widening the instrument. - **Should-fix** — the request table's thread column records the
      _subscribing_ thread, not the one where the request blocks. Status: `FIXED_AND_VERIFIED` — the column is
      `subscribed-on=` and the artifacts say "subscribing thread". - **Nitpick** — a dead per-event frame limit and a
      duplicated `=== request phases ===` header. Status: `FIXED_AND_VERIFIED` — the limit is gone and the header prints
      once.
- [x] 7.3 Pass 2 (verification) confirmed all four pass-1 findings (`FIXED_AND_VERIFIED`, with pass 1's "cannot fire"
      half `REJECTED` on evidence) and raised one new should-fix: `proposal.md` still said the pinning events arrive
      "with their top frames" after the retraction appended beside it, and the design's cost risk repeated the stale
      wording. `FIXED_AND_VERIFIED`: the proposal names the pinning count and the parked threads' frames separately, and
      the risk says only the park event's frames are read. The verification pass independently reproduced the pinning
      event on 21.0.13 and the delta control.
- [x] 7.4 Pass 3 (verification of the post-advisor state) confirmed no earlier finding regressed, judged the routing
      signal's `SecurityWebFilterChain` still appropriate, and raised two nitpicks — the test tally (135/134,
      re-derived) and two prose imprecisions (the stalled handler is a releasable `Sinks.Empty`; an unobserved maximum
      renders `unavailable`). Both fixed; pass 3 closed with the unit cleared to ship subject to the owner accepting the
      narrowed scope.
- [x] 7.5 No further deep pass: pass 3 verified the implementation and its two findings were documentation only; every
      change after it — the 135/134 tally, the `Sinks.Empty` wording, the unavailable-maximum wording, and the restored
      task 4.2 — touched `tasks.md`/`design.md` and no source file, and quick rounds 9–11 re-screened the artifacts. The
      routing signal (`[security-declaration]`) is unchanged and was judged appropriate in pass 1 and re-confirmed in
      pass 3. A fourth pass would re-read code that has not moved, which is the residual risk this decision accepts.
- [x] 7.6 No fourth deep pass after the owner's IDE-warning fixes: the only code changes since pass 3 are nullability
      annotations plus the `package-info.java` the repository's convention calls for, and a watchdog restructure that
      round 12 verified path by path (timeout, interrupt, non-zero exit, blank dump, temp-directory cleanup — the same
      results as before). The module gates and the IDE's nullability and constant-value inspections are clean, so the
      residual risk this decision accepts is that a fourth pass would re-read code whose behaviour did not move.

## 8. The advisor consultation

- [x] 8.1 First consultation returned "do not ship": the timeline only bracketed the gap, the mechanism table treated
      contextual signals as discriminating, always-on JFR lacked containment, and the controls did not establish the
      distinctions. All four were addressed: a blocking-scheduler instrument, a rewritten table of distinguishable
      outcomes, JFR behind `-Dshowcase.diagnostics.jfr=true` with health reporting, and controls that assert exact
      boundaries.
- [x] 8.2 Second consultation accepted the intent but showed the attribution was a guess: attaching a task by submitting
      thread attributed nothing (measured `submitted=2 unattributed=2`). Per-request queue/run claims were **removed**;
      the scheduler figures are an aggregate line labelled as such, and rows carry `last=ADMITTED|TERMINAL`.
- [x] 8.3 Third and fourth consultations: queue wait is measured from each task's eligible time, periodic tasks are
      counted rather than timed, maxima carry their observation counts, a maximum with no observation renders
      `unavailable` rather than `0`, the stalled-request control asserts exactly `OPEN … last=ADMITTED`, and the
      surviving thread-existence inference was removed from every artifact.
- [x] 8.4 The advisor **agreed** (sixth state), subject to the owner accepting the narrowed scope — recorded in
      `proposal.md` `## Scope` and the design's scope boundary: admission, outcome and aggregate host/scheduler context
      are delivered; localization inside an admitted, unfinished request is not, and the parked objective stays open on
      that half.
- [x] 8.5 Seventh consultation, after the owner's IDE-warning fixes: **agree — technically shippable**, with no
      remaining problem within the consultation's scope. The advisor confirmed the `package-info` mechanism, the
      `@Nullable` set, and the watchdog restructure's separation of directory creation, definite process assignment,
      timeout/interrupt handling and single-`finally` cleanup.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The parked parallelism A/B (`docs/ideas.md`) stays parked until a stall arrives under these instruments.
