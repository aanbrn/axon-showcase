# Proposal: Rework the load tests to match the deployed system

## Why

The load tests no longer describe the system they claim to. `ShowcaseSimulation` drives one probabilistic HTTP scenario
against the gateway's REST surface only: it never touches the SSE event stream, it mixes reads and writes inside a
single scenario so no per-surface cost is attributable, its injection is expressed in users/sec rather than a workload
rate, and its profiles are stress-testing shapes with no measurement window. It produces no report anyone consumes, runs
in no pipeline, and its `Host: axon-showcase` default does not match the local cluster's gateway hostname. So the
repository's "load tested" claim — and the parked idea to right-size the chart's `requests`/`limits` from load-test
baselines — both rest on an artifact that no longer reflects the deployed topology.

## What Changes

- **BREAKING** — `load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java`: replace the single
  probabilistic scenario with three concurrently injected scenarios — a **read** stream (`GET /showcases`,
  `GET /showcases/{id}`), a **write-lifecycle** stream (schedule → poll → start → poll → finish → poll → remove), and an
  **SSE** stream (`/events` connect / await / close) — with the read/write share a configured ratio (default: 75% reads,
  25% write-lifecycle completions).
- **New configuration surface** (`build-logic/src/main/kotlin/load-testing-conventions.gradle.kts` and the simulation):
  Gradle project properties `baseUrl` (defaulting to the local cluster's gateway ingress hostname), `profile`, `rate`,
  `ratio`, `duration`, and the SSE connection count, forwarded into the Gatling task's system properties (the plugin
  drops a bare `-DbaseUrl`); the profile selector supersedes the ambiguous `testType`.
- **Measurement profiles** (`load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java`): a `calibrate`
  profile that ramps the mixed workload, and a `baseline` profile that holds a constant plateau at a configured
  operating point and asserts the configured percentiles, so the recorded numbers describe an equilibrium rather than a
  ramp.
- **A baseline report** (`scripts/load-test-baseline.sh` and
  `load-tests/src/gatling/java/showcase/loadtests/KneeFinder.java`): the wrapper owns both measurement steps — it
  derives the knee from the `calibrate` run's `simulation.log` via Gatling's `LogFileReader` (the `kneeFinder` task)
  into `build/load-tests/knee.properties`, then samples `kubectl top pods` during the `baseline` plateau and assembles
  `build/load-tests/report.md` (gitignored); the first run is recorded in `docs/load-tests/<date>.md` (host shape,
  method, numbers) for the later resource-sizing change to consume.
- **A corrected command** (`AGENTS.md`, `README.md`): the run is `./gradlew :load-tests:gatlingRun`, not the documented
  `:load-tests:test`, which starts no simulation.
- `openspec/specs/showcase/quality/load-tests/spec.md` (delta): the parallel streams and the SSE surface, the
  workload-rate/ratio/duration configuration, the calibration and baseline profiles, and the baseline report.
- `docs/ideas.md`: remove the implemented "Rethink or rewrite the load tests" entry and refresh the resource-sizing
  entry's wording now that a baseline exists.
- `README.md` and `AGENTS.md`: refresh the load-test description and command for the local-Helm target and the new
  streams/ratio/report surface.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/load-tests`: the simulation's exercised surfaces (adds the SSE stream and the split read/write
  streams), its configuration (a workload rate, ratio, and duration alongside the users/sec performance profiles), its
  calibration and baseline profiles, and the baseline report it produces.

## Impact

- **Build**: `load-tests` and the `load-testing-conventions` convention plugin only. No new gate — the run stays opt-in
  (`./gradlew :load-tests:gatlingRun`), not part of `check` or `e2e`. No new dependency: Gatling 3.15 already ships the
  SSE DSL (`io.gatling.javaapi.http.Sse`).
- **Tests**: no unit/component/integration tier is affected; the rework is verified by running it against the live local
  Helm cluster.
- **Deployment**: no chart change. The run requires the local cluster to be installed (`./gradlew helmInstallToLocal`)
  and reaches the gateway through its ingress hostname (`setup-hosts.sh`). The recorded baseline is the input to the
  separate resource-sizing idea; it does not change chart values here.
