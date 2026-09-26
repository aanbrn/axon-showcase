# Tasks

## 1. Invocation and configuration

- [x] 1.1 Map the Gradle project properties (`baseUrl`, `profile`, `rate`, `ratio`, `duration`, `sseConnections`) into
      the Gatling task's `systemProperties` in `build-logic/src/main/kotlin/load-testing-conventions.gradle.kts` — the
      plugin drops a bare `-DbaseUrl` — and read them in the simulation with defaults (`baseUrl` →
      `http://axon-showcase-api`, `profile` → `smoke`, `ratio` → `0.75`, `rate` → `100`, `duration` → `10m`). Verify by
      running `./gradlew :load-tests:gatlingRun -Pprofile=smoke -PbaseUrl=http://axon-showcase-api` and confirming the
      run targets that base URL.
- [x] 1.2 Correct the documented command from `:load-tests:test` to `:load-tests:gatlingRun` in `AGENTS.md` and
      `README.md`. Verify with `./gradlew :load-tests:tasks --all` that the task exists and that `:load-tests:test` has
      no Gatling dependency.

## 2. Simulation restructure

- [x] 2.1 Split `load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java` into a **read** stream
      (`GET /showcases`, `GET /showcases/{id}`) and a **write-lifecycle** stream (schedule → poll → start → poll →
      finish → poll → remove), injected concurrently, carrying over the 500 ms polling retry with its 5-minute window;
      verify `./gradlew :load-tests:check` compiles and a `smoke` run reports separate per-stream statistics.
- [x] 2.2 Add the **SSE** stream with `io.gatling.javaapi.http.Sse` (connect → await events → close), keeping its
      long-lived connection time out of the request assertions; verify a `smoke` run connects, observes events for the
      write lifecycle, and records the SSE statistics separately.
- [x] 2.3 Verify the SSE keep-alive: run the SSE stream with the write stream disabled for longer than the gateway's
      keep-alive interval (`apiGateway.events.keepAliveInterval`, default `PT15S`) and confirm the connection stays open
      and receives the keep-alive.
- [x] 2.4 Derive the request `Host` from `baseUrl` (replace the hard-coded `axon-showcase` header); verify the `smoke`
      run reaches the gateway through its ingress hostname.

## 3. Measurement and baseline report

- [x] 3.1 Add the `calibrate` profile (ramp the mixed workload to `rate`) with no pass assertions; verify a short run
      produces the `simulation.log` the wrapper parses.
- [x] 3.2 Add the `baseline` profile (hold a constant plateau at the configured `rate` for the configured `duration`)
      asserting the configured percentiles; verify a short baseline run at a low `rate` passes.
- [x] 3.3 Add the report wrapper: derive the knee from the `calibrate` run's `simulation.log` into
      `build/load-tests/knee.properties`, sample `kubectl top pods -n axon-showcase` during the `baseline` plateau, and
      assemble `build/load-tests/report.md` (target shape, calibration knee, operating point, plateau duration, plateau
      response times, per-service resource usage); verify the file is produced and names each of those.
- [x] 3.4 Prove the assertions can fail: run `baseline` against a known-bad target (or an impossible percentile
      threshold) and confirm the task exits non-zero on a failed assertion; then run the known-good `smoke` profile and
      confirm it passes.
- [x] 3.5 Run `calibrate` against the local Helm cluster, set the operating point below the knee, run `baseline`
      (`./gradlew :load-tests:gatlingRun -Pprofile=baseline -Prate=<point>`), and record the result in
      `docs/load-tests/<date>.md` (target shape, method, numbers). Verify the recorded numbers match
      `build/load-tests/report.md`.

## 4. Docs and spec follow-through

- [x] 4.1 Update `AGENTS.md`'s load-test section and `README.md`'s load-test description for the read/write/SSE streams,
      the ratio, the `calibrate` and `baseline` profiles, the report, and the local-Helm target. Verify every documented
      command runs as written.
- [x] 4.2 Remove the implemented "Rethink or rewrite the load tests" entry from `docs/ideas.md` and refresh the
      resource-sizing entry's wording now that a baseline exists. Verify no entry still calls the load tests stale.
- [ ] 4.3 Refresh the `showcase/quality/load-tests` `## Purpose` in the archive commit (a delta cannot carry a Purpose)
      so it names the three concurrent streams and the baseline report rather than the old scenario flow; verify the
      refreshed Purpose against the spec's requirements.

## 5. Verification

- [x] 5.1 `./gradlew spotlessApply` then `./gradlew spotlessCheck` pass, and `openspec validate --changes` passes.
- [x] 5.2 `./gradlew :load-tests:check` passes (compile, Checkstyle, SpotBugs on the Gatling source set).
- [x] 5.3 Read `build/load-tests/report.md` and `docs/load-tests/<date>.md` together and confirm the method, the
      calibration knee, the operating point, and the numbers agree, with the host shape stated.
