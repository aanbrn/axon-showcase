# Design

## Context

See `proposal.md` — Why. Current state, derived from the module and the deployed local cluster rather than recalled:

- `ShowcaseSimulation` is one probabilistic REST scenario with users/sec injection profiles and no report; the load
  tests run in no pipeline. The documented command is also stale: the Gatling Gradle plugin registers **`gatlingRun`**,
  not `test`, so `./gradlew :load-tests:test` (as `AGENTS.md` and `README.md` state) runs no simulation at all. The
  plugin forwards only whitelisted JVM `-D` properties (`java.`, `gatling.`, …) to the forked simulation, so a bespoke
  `System.getProperty("baseUrl")` cannot be set with `-DbaseUrl` — configuration must be mapped from Gradle project
  properties into the task's `systemProperties`.
- The target is a **10 vCPU / 16 GiB** colima VM carrying the whole stack — the app tier _and_ Kafka, PostgreSQL,
  OpenSearch, and the kps/tempo observability stack. The app tier alone requests **5 cores** (gateway 1 + query 1 +
  projection 1 + command 2 replicas), so discretionary headroom is well under 10.
- Only the gateway and web UI have ingress; the query service is cluster-internal. The gateway's ingress hostname
  `axon-showcase-api` resolves via `./setup-hosts.sh`, and `kubectl top pods` works (metrics-server is present) while
  kps Prometheus is reachable.
- **Gatling 3.15.1 ships the SSE DSL** (`io.gatling.javaapi.http.Sse`), so `/events` is first-class rather than a hack.
- The repo's own definition of "normal" load is the `average` profile's `constantUsersPerSec(200)`.

## Goals / Non-Goals

**Goals:**

- Run the gateway's read and write surfaces **concurrently** with a configurable ratio, and add the SSE stream.
- Provide a **measurement** capability — a `calibrate` ramp and a `baseline` plateau — and a report a later change can
  consume as a resource-sizing baseline.
- Retain the existing extreme-load profiles so the rework strengthens coverage rather than trading it away.

**Non-Goals:**

- **No chart value change** here (B1): the resource-sizing change consumes the recorded baseline.
- **No query-service Protobuf surface**: it is cluster-internal and the query bus is already exercised by gateway reads.
- **No web-UI driving**: Playwright owns the browser `e2e`; the load test models browser-tab SSE connections, not
  clicks.
- **Not a CI gate**: the run stays opt-in.

## Decisions

- **D1 — Three concurrent scenarios, not one probabilistic scenario.** Split the single `randomSwitch` scenario into a
  read stream, a write-lifecycle stream, and an SSE stream, injected together. The alternative — keeping the single
  scenario — yields one aggregate statistic and no independent rate control, which defeats both the coverage and the
  measurement goal.
- **D2 — Gateway-only surface.** The query-service `/query` endpoint has no ingress; loading it directly needs a
  port-forward or in-cluster generation for little marginal value, since a gateway `GET /showcases` already dispatches
  the query bus. SSE, being a client-facing gateway surface the web UI consumes, is included.
- **D3 — SSE is its own stream and is excluded from the request assertions.** An SSE connection is long-lived, so its
  duration would swamp the request-response percentiles; Gatling groups/stats keep the SSE metrics separate and the
  assertions scoped to requests.
- **D4 — Ratio: 75 % reads / 25 % write-lifecycle completions, set by a `ratio` property.** The read and write streams
  are injected at rates derived from `rate` and `ratio` — the read stream at `ratio × rate` and the write-lifecycle
  stream at `(1 − ratio) × rate`, where one workload unit is a read iteration or a write-lifecycle completion — so the
  ratio is the injection split and per-stream statistics stay attributable. The SSE connection count is a separate
  property, not folded into the ratio.
- **D5 — Calibration is a separate run; the baseline is a configured plateau.** Gatling builds
  `setUp(...).assertions(...)` before execution, so a threshold cannot be derived from the same run's calibration, and a
  `Simulation` cannot read its own statistics — Gatling writes them to `simulation.log` under `build/reports/gatling/`.
  The `calibrate` profile therefore ramps the mixed workload to `rate`, and the **wrapper** derives the knee — the rate
  where the response time departs the flat baseline — by reading that run's `simulation.log` with Gatling's
  `LogFileReader` (the 3.15 log is a binary format, so a small JVM helper `KneeFinder`, run by the `kneeFinder` Gradle
  task, does this), writing it to `load-tests/build/load-tests/knee.properties`. The operator then sets the operating
  point below it and runs `baseline`, which holds a constant plateau at `rate` for `duration` and asserts the configured
  percentiles. The starting point is **100 units/s** (secondary 50), chosen to sit inside the ~2.5-core app headroom on
  this host and far below where single-node Kafka/OpenSearch queue — a starting point the calibration confirms or
  lowers. The repo's `average` profile's 200 is a _users/sec_ curve (each virtual user issues several requests), so it
  is a coarse sanity anchor, not a unit equivalence.
- **D6 — Keep the six performance profiles and add `calibrate` and `baseline`.** The extreme-load shapes (`stress`,
  `spike`, `breakpoint`) still serve the coverage goal; the rewrite adds the measurement shapes rather than replacing
  them. The performance profiles keep users/sec (a fine unit for saturation tests); `calibrate` and `baseline` are
  workload-rate based, where a precise unit matters.
- **D7 — The report is assembled by a wrapper, and the first run is recorded.** The wrapper owns both measurement steps:
  after `calibrate` it parses `build/reports/gatling/<simulation>-<timestamp>/simulation.log` for the knee and writes
  `build/load-tests/knee.properties`; around `baseline` it samples `kubectl top pods -n axon-showcase` on an interval
  for the plateau's duration and merges the samples, the knee, and the Gatling summary into `build/load-tests/report.md`
  (gitignored). The change also commits a dated `docs/load-tests/<date>.md` recording the target shape, method, and
  numbers, like the `docs/audits/` reports. The committed record is a dated point-in-time artifact, not a durable
  constant — the resource-sizing change cites it, it does not key chart defaults off one machine.
- **D8 — `baseUrl` defaults to `http://axon-showcase-api` and the request `Host` derives from it.** The old default
  (`http://localhost`) reaches neither the compose `:8080` gateway nor the cluster ingress, and the hard-coded
  `Host: axon-showcase` header matches no ingress hostname; `setup-hosts.sh` is already the documented prerequisite.
- **D9 — Invoke `gatlingRun`, and forward configuration through `systemProperties`.** `load-testing-conventions` maps
  Gradle project properties (`-PbaseUrl`, `-Pprofile`, `-Prate`, `-Pratio`, `-Pduration`, `-PsseConnections`) into the
  Gatling task's `systemProperties`, since the plugin drops a bare `-DbaseUrl`; `rate` defaults to 100 units/s. The
  docs' `:load-tests:test` command is corrected to `:load-tests:gatlingRun` — the stale command is part of why the tests
  read as unexercised.

## Risks / Trade-offs

- **Host-relative numbers are not absolute.** The load generator shares the host's CPU with colima, and the VM's shape
  fixes the ceiling. → Record the target shape and method with the numbers; treat them as relative guidance, never as
  committed chart values.
- **A long mixed plateau grows the data set.** The write stream creates showcases as it runs, changing read cost over
  time. → Each write user runs a terminating lifecycle (finish then remove), and the mix and window are fixed.
- **SSE users are connections, not requests.** Folding them into "rps" would misstate load. → SSE is its own stream with
  its own stats and its own unit.
- **Scenario-model churn.** Requirement 1 and 2 change their scenario model, so the delta removes and re-adds them
  rather than modifying (a `MODIFIED` block cannot rename a scenario); the delta validator does not check scenario
  preservation across a removed/added pair, so the migration notes carry the behavior explicitly.

## Migration Plan

- Replace the simulation and its configuration, update the docs and the `load-tests` spec, then validate against the
  live local cluster and record the first baseline. Rollback is reverting the branch; the change touches no deployed
  artifact and no chart value.

## Open Questions

- Whether the performance profiles' users/sec curves should later be restated in a workload-rate unit. Deferred — it
  does not change the specs, the approach, or the task breakdown, and the `baseline` profile already provides the
  precise unit.
