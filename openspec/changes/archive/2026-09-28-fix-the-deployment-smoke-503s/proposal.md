# Proposal: Fix the deployment smoke's 503 window

## Why

The smoke's load step failed on 37 failed polls, every one a gateway `503 Service Unavailable` (verified from run
36354957471), in a nine-second window at the start of the `baseline` plateau — after the `smoke` profile had passed and
the releases had become ready, so the pipeline was warm and the disruption happened _during_ the load. The chart's
default memory limits (1Gi per service) bound each JVM's heap to roughly three-quarters of that, and the trimmed CI
target lowered only the _requests_ — so a service can exhaust its heap under the plateau's burst, be restarted, and
answer 503 while it returns. The run's cluster is deleted by the time anyone looks, so the failure left no evidence.

## What Changes

- Raise the app release's memory **limits** in the CI target's values. The requests stay, so the scheduling budget that
  fits the runner is untouched, and each service keeps its share of the runner's 16 GB.
- Run only the `smoke` profile: it asserts zero failed requests and carries no latency thresholds, where the `baseline`
  plateau's fixed ones (p95 500 ms, p99 1000 ms) failed at 732 ms and 2812 ms on a shared 4-vCPU runner — a measurement
  that belongs to a host, not to CI.
- Capture a failed load step's evidence while the cluster still exists — the pods with their restart counts, and the
  gateway, query, and projection logs — so the next failure is diagnosed from the run rather than guessed at.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none) — the smoke's specified behavior is unchanged: it still installs what fits the runner and asserts that no request
fails. The values fix conforms to it; the diagnostic step is internal control flow that no scenario constrains. Neither
changes a specified outcome, so no delta is owed — `skip_specs: true`.

## Impact

- **Build**: `helm/values/axon-showcase/values-ci.yaml` and the deployment-smoke workflow.
- **Tests**: verified by dispatching the smoke after it lands; the new evidence step either confirms the limits were the
  cause or names the real one.
- **Deployment**: none.
