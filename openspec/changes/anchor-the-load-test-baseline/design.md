# Design

## Context

See `proposal.md` — Why. Current state: the below-knee profiles (`average`, `stress`, `soak`) assert fixed absolutes
(`ShowcaseSimulation`'s `assertions()`); `scripts/load-test-baseline.sh` runs `calibrate` → `kneeFinder` → `baseline`
and assembles `report.md` from the run's console summary (`SUMMARY_PATTERN` greps the aggregate `mean response time`,
`response time 95th`, and `response time 99th` rows); the committed `docs/load-tests/<date>.md` records are hand-written
prose around those numbers. `KneeFinder` — a Java class in the `gatling` source set, run by a `JavaExec` task that
`load-testing-conventions` registers for it, the wrong home for it since the convention then names a `load-tests` class
— already reads a run's Gatling log through the stats API (`responseTimePercentilesOverTime(OK, …)`,
`numberOfRequestsPerSecond(request, …)`) and writes properties. The console's aggregate percentiles are unusable as a
baseline: the SSE connections' long-lived response times land among them, and per-request latencies are not in the
console output at all.

## Goals / Non-Goals

**Goals:**

- Make the below-knee thresholds scale with the host's own measured baseline, so a fast host's regression is caught and
  a slow host is not judged against a fast host's numbers.
- Make the baseline machine-readable, committed, and reviewed.
- Emit the dated record from the run instead of a human transcribing numbers.

**Non-Goals:**

- No change to the calibration, the knee derivation, or the wrapper's knee/profiles loop.
- No change to the `baseline` profile's own assertions or to the assertion-free profiles.
- No scheduled run (a separate parked idea, reshaped as an availability smoke).

## Decisions

- **D1 — The reference is a committed properties file read from the classpath.** It lives at
  `load-tests/src/gatling/resources/baseline.properties`, written by a run and committed like the record. The classpath
  (rather than a path resolved against a working directory) lets a standalone `:load-tests:gatlingRun` see it too; a
  `baselineFile` property overrides it. The derivation policy travels in the same file, so there is one source of truth
  rather than a second copy of the floors in the build.
- **D2 — The module owns its build specifics: the reference writer, the tasks, and the property whitelist.**
  `baselineStats` is a `JavaExec` in `load-tests/build.gradle.kts` over the module's `gatling` source set runtime
  classpath — never in `load-testing-conventions`, which must not name a module's own interface. `kneeFinder`'s
  registration (a class name) and the `loadTestProperties` whitelist (the simulation's system-property names) both move
  here in the same change, the task's path, classpath, and arguments unchanged; the convention keeps applying
  `java-conventions` and the Gatling plugin — the module's kind, as this repo's other single-consumer conventions
  (`frontend-conventions`, `helm-conventions`, `protobuf-conventions`) mark theirs. The task reads the baseline run's
  Gatling log per request name over the read/write names the assertions cover (`LoadTestRequests.READ_WRITE`, the one
  list the simulation and the task both read, so they cannot drift) and writes the mean, 95th, and 99th percentile per
  name plus the policy, the recording instant, and the target. A name the run never exercised (a short run's finish
  share, say) is simply absent, which is why an uncovered request falls back per name (D4). The console's aggregate
  percentiles are not usable (the SSE connections' long-lived response times land among them) and per-name latencies are
  not in the console output.
- **D3 — The derivation is `max(floor, factor × baseline)` per request and percentile.** The defaults — `factor = 5`,
  floors of 50 ms (mean), 100 ms (p95), and 200 ms (p99) — are recorded in the reference. The floors sit an order of
  magnitude above the committed plateau's response times (mean 5 ms, p95 8 ms, p99 11 ms in
  `docs/load-tests/2026-09-26.md`) so a quiet baseline's noise cannot fail a run, while the relative term binds once a
  host's baseline exceeds the floor over the factor (a 100 ms baseline p95 yields a 500 ms threshold; a 20 ms one is
  floored at 100 ms). The alternatives — absolute thresholds taken from the baseline (the first slower-but-fine run
  fails) and a pure multiple with no floor (a quiet baseline's noise fails) — were rejected for those failure modes.
- **D4 — An unusable reference falls back to the absolute thresholds, per request.** An unreadable or absent reference,
  one that does not cover a request, or one whose recorded target is not the target under test keeps the previous
  behaviour (mean ≤ 100 ms, p95 ≤ 500 ms, p99 ≤ 1000 ms) for that request, rather than running unasserted or asserting
  another environment's numbers; the simulation logs which set it used, which requests fell back, and when it ignored a
  mismatched reference. Rejected: failing the run on a missing reference (a first run after a checkout would be
  unrunnable) and requiring the reference to cover every name (a short run need not exercise them all).
- **D5 — The record is written to a tracked path, and the operator formats it.** `docs/load-tests/<date>.md` for the
  wrapper's default (local) target — D7 gives another target its slug — with the run's method and numbers and a header
  marking it as generated; being Spotless-gated markdown, it is committed after `./gradlew spotlessApply` (the repo's
  post-edit step) and the operator's annotations. A same-day re-run overwrites that day's generated record — it is the
  day's latest measurement — and the operator renames it to the repo's `<date>-<suffix>.md` form when both are worth
  keeping. Rejected: writing only under `build/` (a copy step is what the idea set out to remove) and a
  `<date>-<time>.md` name (noise per run).
- **D6 — Three requirements are modified, none added.** Each behaviour is described by an existing requirement
  (`Pass assertions`, `Configurable target, profile, rate, ratio, and duration`,
  `A baseline run reports its measurements`), so each takes a `MODIFIED` delta carrying its existing scenarios in the
  main spec's order. It also owes a `## Purpose` refresh at archive time, since the capability's scope now includes the
  reference and the record (a delta cannot carry a Purpose).
- **D7 — A non-local target gets its own artifacts, and the report states the target measured.** The wrapper's reference
  path defaults to the module's committed reference for its own default (local) target and to
  `baseline-<slug>.properties` for any other — a sibling classpath resource, `<slug>` being the target host with every
  non-alphanumeric character replaced by `-` — its record to `docs/load-tests/<date>-<slug>.md`, and `-PbaselineFile`
  points a run at an environment's own reference; the report's target line states the URL measured instead of asserting
  "local Helm cluster". With D4's guard a mis-pointed reference is inert rather than silently wrong. Rejected: one
  shared reference for every environment (a staging run overwrites the local numbers, and local runs then assert against
  them) and refusing to run off-local (the simulation is environment-agnostic; only the wrapper's sampling and artifacts
  are local-shaped). Its resource sampling still follows the ambient kube context (`kubectl top pods -A`, the first
  node's allocatable), so a non-local run's sample data may describe another cluster — documented, deliberately not
  fixed here.
- **D8 — The write stream's poll conditions become session predicates, and take no delta.** The three conditions were
  Gatling EL strings (`"#{queryStatus} != 200"` and the two status ones), and a sustained baseline run crashed them —
  `Can't parse '200 != 200' into boolean … exiting loop` — KO-ing 194 of 22,463 polls, which trips the `baseline`
  profile's zero-failure assertion (and, for `average`/`stress`/`soak`, the success-rate one), blocking a clean baseline
  altogether. They now compare the session attribute textually in Java (`doWhileDuring(Function<Session, Boolean>, …)`).
  The existing `Polling retries until the expected state` scenario already states the retry contract, so the fix makes
  the code conform to the spec rather than changing what it specifies — no delta is owed.

## Risks / Trade-offs

- **A reference measured on one host anchors another host's thresholds.** → It is the environment's last measurement,
  refreshed by re-running the baseline; the record states the host shape (as the existing records do), and the policy is
  reviewable in the reference itself.
- **A tighter threshold can flake.** → The floors absorb noise, the factor is generous, and the reference is refreshed
  rather than frozen.
- **A run writes tracked files (the reference and the record).** → Deliberate: that is the ready-to-commit contract, and
  both are reviewed diffs rather than silent writes.
- **The generated record is formatter-gated.** → The operator runs `spotlessApply` before committing; the record's
  header says so.

## Verification

- A short `baseline` run writes the reference; spot-check its per-name values against the run's Gatling report
  (`load-tests/build/reports/gatling/<simulation>/`, the source `baselineStats` itself reads), since `report.md` carries
  only aggregate rows.
- A short below-knee run passes with the derived thresholds, and its log states the values it derived.
- Positive control: temporarily lower the reference (a tiny baseline and floors) so a passing run fails its derived
  assertion — confirm the failure names the derived threshold — then restore the reference.
- The record appears at `docs/load-tests/<date>.md` (the default target) with the run's numbers, and `spotlessCheck`
  passes after the operator's `spotlessApply`.
