# Proposal: Stabilize the deployment smoke's cold start

## Why

The nightly Deployment Smoke failed on 2026-11-08 (`gh run view 42427617536`): the gateway and the projection service
were killed by their startup probes during the install, and the restarted gateway's Kafka consumer joined
`showcase-live-events` only after the load test had published its events — the consumer reset to `latest`, the SSE check
timed out 10 of 10, and the run failed on an assertion the deployed system never had a chance to satisfy. Waiting for
the stream needs an event, and the gateway replays buffered events to every new subscriber, so the wait must not become
the evidence the load's own check consumes.

## What Changes

- `helm/values/axon-showcase/values-ci.yaml` — the `ci` target raises the startup-probe tolerance (`timeoutSeconds`,
  `failureThreshold`) for the four JVM services, so the runner's concurrent cold start no longer restarts a service that
  is still booting.
- `.github/workflows/deployment-smoke.yml` — the smoke publishes a showcase per attempt, confirms the gateway's
  live-event stream delivers one (bounded and retried), collects every published ID, and fails the step when a publish
  yields no ID, before it drives the load.
- `load-tests/src/gatling/java/showcase/loadtests/ShowcaseSimulation.java` — both SSE message checks' match conditions
  exclude the showcases the smoke's wait published, from a comma-separated `warmUpShowcaseIds` property, so those
  buffered events cannot satisfy a check; with the property unset the checks keep today's conditions.
- `load-tests/build.gradle.kts` — the `gatlingRun` task forwards the new `warmUpShowcaseIds` Gradle property.
- `openspec/changes/stabilize-deployment-smoke-startup/specs/showcase/quality/merge-governance/spec.md` — a `MODIFIED`
  delta for the deployment-smoke requirement: the smoke waits for the live-event stream as well as for readiness.
- `openspec/changes/stabilize-deployment-smoke-startup/specs/showcase/quality/load-tests/spec.md` — a `MODIFIED` delta
  for the SSE requirement and the property enumeration: the checks exclude the warm-up showcases the smoke published.
- `.opencode/skills/deployment-operations/SKILL.md` — records the `ci` values' probe tolerance and the smoke's
  live-event gate beside the existing "fits the runner" guidance.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/merge-governance`: the deployment-smoke requirement gains the live-event readiness wait and the
  outcome that a slow cold start is not a smoke failure.
- `showcase/quality/load-tests`: the SSE requirement gains the warm-up exclusion, and the configurable-properties
  requirement gains `warmUpShowcaseIds`.

## Impact

- **Code**: one simulation class and one Gradle forwarding property in the `load-tests` module; no service, library, or
  chart-template change, and the chart's probe defaults stay as they are for local and other targets.
- **Deployment**: the `ci` target's values only (the throwaway kind cluster). The four JVM services get a 63-second
  startup budget instead of 33 seconds, with a 3-second probe timeout instead of 1 second. Requests and limits are
  untouched.
- **CI**: the smoke's pipeline step gains a bounded publish-and-confirm loop and passes the published IDs to the load.
  The `Diagnose a failed smoke` step still prints the deployed state before teardown.
- **Tests**: the simulation's SSE checks gain controls that need no cluster — a stub gateway replaying only warm-up
  events must fail them, and one also emitting an event for a run-created showcase must pass them, on both the smoke
  profile's single check and a sustained profile's mid-hold check. The end-to-end verification is the smoke's own
  dispatch (`gh workflow run deployment-smoke.yml --ref <branch>`), since the runner is the environment that exhibited
  the contention.
- **Load tests**: the assertion set, the thresholds, and the profiles are unchanged; only the SSE checks' match
  conditions and the property list move, and the property's absence restores today's behaviour.
