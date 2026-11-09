# Tasks

The unit's record is `docs/units/stabilize-deployment-smoke-startup/unit.md`.

## 1. Runner startup tolerance

- [x] 1.1 `helm/values/axon-showcase/values-ci.yaml` — give `commandService`, `queryService`, `projectionService`, and
      `apiGateway` a `startupProbe` override of `timeoutSeconds: 3` and `failureThreshold: 20`. Verify by packaging the
      chart (`./gradlew :helm:chart:helmPackageMainChart`), rendering it with
      `helm template axon-showcase helm/chart/build/helm/charts/axon-showcase -f helm/values/axon-showcase/values-ci.yaml`,
      and confirming each of the four Deployments renders `failureThreshold: 20` and `timeoutSeconds: 3`; then render
      without `-f values-ci.yaml` and confirm the same fields read the chart default, so the override is what produces
      the value. — verified: with the values file, four Deployments carry `failureThreshold: 20` and `timeoutSeconds: 3`
      (the four management-port 8888 startup probes: three `/actuator/health`, one `/actuator/health/liveness`); without
      it, four carry `failureThreshold: 10` and twelve `timeoutSeconds: 1`.
- [x] 1.2 `.opencode/skills/deployment-operations/SKILL.md` — record that the `ci` values raise the startup-probe budget
      for the four JVM services and why (the runner's concurrent cold start), beside the existing runner-fit guidance.
      Verify by reading the edited paragraph and grepping the pack for the budget it names. — verified: the paragraph
      "The `ci` values raise the JVM startup-probe budget" names `timeoutSeconds: 3`, `failureThreshold: 20`, the
      63-second budget, and the concurrent-boot reason.

## 2. Live-event readiness gate

- [x] 2.1 `.github/workflows/deployment-smoke.yml` — in the `Smoke the deployed pipeline` step, after the existing
      `/showcases` readiness loop and before `:load-tests:gatlingRun`, publish a showcase through the ingress, keep its
      showcase ID from the response, poll `GET /events` with a bounded per-attempt timeout until a `data:` frame arrives
      (republishing each attempt and keeping every attempt's ID), fail with a named live-event-stage message when the
      attempt bound is exhausted or a publish returns no ID, and pass the collected IDs to the load as a comma-separated
      `-PwarmUpShowcaseIds`. Verify with `./gradlew workflowLint` and by reading the step's rendered shell. — verified:
      the step is written; `./gradlew check` (which runs `workflowLint`) is green.
- [x] 2.2 Prove the gate's loop fails when it should: serve a stream that sends only SSE keep-alive comment lines (no
      `data:` frame) and run the loop's shell against it, confirming it exits non-zero with the named message inside the
      bound — a gate keyed on any byte would pass this fixture, so it exercises the exact false positive the design
      rejects. Record the command and its output. — verified: the harness (the step's loop body, run with the step's
      `bash -e` flags) against a keep-alive-only stub exits 1 with "The gateway's live-event stream delivered no event
      through the ingress hostname axon-showcase-api", against a data-frame stub exits 0 with "live-event gate passed
      with run-1", and against a `202`-with-`Idempotency-Key` stub exits 0 with the header's ID. The first draft's
      `curl | grep -q` failed only under the harness's own `set -o pipefail` (the step runs `bash -e`, with no
      pipefail), so the temp-file read is kept because it behaves identically under either shell.
- [x] 2.3 `.opencode/skills/deployment-operations/SKILL.md` — record that the smoke waits for the gateway's live-event
      stream before driving the load, and why (a fresh consumer with no committed offset starts at the end, so events
      published earlier are skipped). Verify by reading the edited paragraph and grepping the pack for the gate. —
      verified: the paragraph "The deployment smoke waits for the live-event stream, and the load excludes what it
      published" names the join/reset behaviour, the warm-up publish, `warmUpShowcaseIds`, and the replay buffer.

## 3. SSE check excludes the warm-up showcase

- [x] 3.1 `load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java` — read `warmUpShowcaseIds` (default
      empty, comma-separated) and, for each ID, add `jsonPath("$.data.showcaseId").not(id)` to the match conditions of
      both SSE message checks — the initial check and the mid-hold check a sustained profile builds; with the property
      unset the checks keep today's conditions. Verify with `./gradlew :load-tests:compileGatlingJava` and by reading
      the built check sequence for both profiles. — verified: `WARM_UP_SHOWCASE_IDS` and the `sseMessageCheck` helper
      apply one `.not(id)` per ID, and both scenarios call it; `:load-tests:compileGatlingJava` is green.
- [x] 3.2 `load-tests/build.gradle.kts` — forward the `warmUpShowcaseIds` Gradle property into the simulation's system
      properties like the other load-test properties. Verify by reading the resolved property back from the task with a
      scratch init script (the same pattern the `remoteTimeout` task used), showing the value passes through and the
      unset case is absent rather than empty-string wired into a check name. — verified: the init-script probe printed
      `gatling.systemProperties=[:]` unset and `[warmUpShowcaseIds:abc,def]` with the property.
- [x] 3.3 Control A — with a stub gateway that serves `/showcases` and replays **only** a warm-up showcase's event on
      `/events`, run `./gradlew :load-tests:gatlingRun -Pprofile=smoke -PbaseUrl=<stub> -PwarmUpShowcaseIds=<id>` and
      confirm it fails on `showcaseEvent`, naming that check — the hollowed-check false positive gone. Record the
      output. — verified: `showcaseEvent | 10 | 0 | 10` with `Check showcaseEvent timeout`, `Global` 10 failures, BUILD
      FAILED.
- [x] 3.4 Control B — with the same stub also emitting an event for a showcase created during the run, before that
      create's HTTP response completes, confirm the same invocation passes the SSE check, so the exclusion adds no
      dependency on the order between the HTTP response and the stream. Record the output. — verified: `Global` 35/35
      OK, `showcaseEvent` 10/10 OK, `showcaseEvent: count of failed events … true (actual : 0.0)`, BUILD SUCCESSFUL.
- [x] 3.5 Control C — isolate the mid-hold check under a profile that builds it
      (`-Pprofile=baseline -Pduration=PT30S -Pratio=0.75`, which makes `SSE_SUSTAINED_WRITES` true): the stub delivers a
      run-created showcase's event when the connection opens, so the initial check passes, and afterwards replays only
      warm-up events. With the exclusion on the mid-hold check the run must fail on the timed-out mid-hold await; remove
      that one `.not(...)` condition and the same run must pass, since the warm-up replay then satisfies it. Record both
      outputs — the second is what proves the control perturbs the check it claims to. — verified: with the exclusion
      the run failed (`showcaseEvent | 20 | 10 | 10`, BUILD FAILED); with the mid-hold `.not(...)` removed the same run
      passed (`showcaseEvent | 20 | 20 | 0`, BUILD SUCCESSFUL); the file was restored from its snapshot and `diff` is
      empty. (The first stub revision failed all ten initial checks too, because it sent the fresh event to one
      connection only — the control was rebuilt to send one per connection.)
- [x] 3.6 `openspec/changes/stabilize-deployment-smoke-startup/specs/showcase/quality/load-tests/spec.md` — confirm the
      two `MODIFIED` requirement blocks copy the main spec's text with only the warm-up clauses added and every existing
      scenario retained in order. Verify with `openspec validate --all`. — verified: `openspec validate --all` reports
      27 passed, 0 failed, with both deltas present.

## 4. Close-out

- [x] 4.1 Re-derive `design.md`'s claims table after the unit's last edit to its own record: run each row's command and
      confirm it reproduces the row's result, and keep the dismissal list at one reason per kind. Verify by the
      commands' output matching the table. — verified: every row's command was re-run; two Results were re-anchored to
      the current source (the readiness-loop/gatlingRun line numbers and the `sseMessageCheck` helper), and the rest
      reproduce as stated.
- [x] 4.2 `./gradlew spotlessApply`, then `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; confirm green. —
      verified: BUILD SUCCESSFUL (415 tasks), including `workflowLint`, the chart packaging and lint, `verify*`, and the
      load-test module's compile.
- [x] 4.3 `openspec validate --all` passes and both deltas sit at their mirrored paths under
      `openspec/changes/stabilize-deployment-smoke-startup/specs/showcase/quality/`. — verified: 27 passed, 0 failed;
      `merge-governance/spec.md` and `load-tests/spec.md` are both present.
- [x] 4.4 Write `docs/units/stabilize-deployment-smoke-startup/process-defects.md` stating what the unit observed (or
      that it observed none), and confirm the record names no cause it did not verify. — verified: one record (findings
      files outside the instance shape) with its evidence and knowledge boundary; no cause diagnosis.
- [x] 4.5 Re-derive the deep-review routing at the implementation boundary, once the `helm/values/` and `.github/` edits
      exist. Run
      `python3 scripts/review-routing.py --unit stabilize-deployment-smoke-startup --change stabilize-deployment-smoke-startup`
      and record the output it prints, with the signals it matches, in
      `docs/units/stabilize-deployment-smoke-startup/deep-review.md`. The proposal-boundary record is provisional — the
      edits' paths are expected to raise `infrastructure-or-deployment` and with it `REQUIRED` — so this task replaces
      that outcome rather than confirming it, and a `REQUIRED` outcome runs the deep review before completion. —
      verified: the tool printed `ROUTING: REQUIRED` with `infrastructure-or-deployment` on the workflow and the values
      file; the outcome and its signals are recorded in `deep-review.md`, and the deep review's pass 1 ran.

## 5. At the PR (after the owner's approval, as part of the merge)

- [ ] 5.1 Push the branch and dispatch the smoke against it
      (`gh workflow run deployment-smoke.yml --ref stabilize-deployment-smoke-startup`); confirm the run completes green
      and record its URL and conclusion. This is the only environment that reproduces the runner's cold-start
      contention, so it is the end-to-end check of the change, and it completes inside the change's PR before the
      archive commit.
