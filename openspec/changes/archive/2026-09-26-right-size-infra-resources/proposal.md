# Proposal: Right-size the infrastructure and observability resources

## Why

The infrastructure and observability releases are sized by hand in `helm/values/*/values-local.yaml`, and the load-test
baseline that would validate them sampled only the `axon-showcase` namespace — so Postgres, Kafka, and OpenSearch are
measured but the monitoring stack (`kps`, `tempo`) is not. The app chart's defaults were just re-sized, while these
requests still overstate the measured envelope (roughly 2–3× the measured CPU and up to ~2.5× the memory — only the
OpenSearch master's `1.0Gi` request already sits at its measured working set), with `3.0`-CPU limits never approached,
and the request sums land near the 16 GiB colima VM's capacity. Extend the sampler to the whole cluster, re-measure, and
size the infrastructure and observability from that single result.

## What Changes

- `scripts/load-test-baseline.sh`: sample `kubectl top pods -A` (all namespaces) instead of only `axon-showcase`, so the
  report covers the infrastructure and observability services.
- A re-run of the baseline, recorded as a whole-stack measurement under `docs/load-tests/`.
- `helm/values/axon-showcase-db-events/values-local.yaml`, `helm/values/axon-showcase-kafka/values-local.yaml`,
  `helm/values/axon-showcase-os-views/values-local.yaml`, `helm/values/kps/values-local.yaml`, and
  `helm/values/tempo/values-local.yaml`: size the CPU requests, memory requests, and memory limits from the measurement,
  and drop the CPU limits for consistency with the app chart's policy.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none) — the `load-tests` spec's report requirement already says "per-service resource usage", which the broadened
sampling satisfies; no capability spec describes the local values' resource numbers. `skip_specs: true`.

## Impact

- **Build**: the load-test wrapper (a shell script, not gated) and the local Helm values; `helmLintMainChart*` cover
  only the app chart, so each changed chart is verified by rendering it with its local values (plus the wrapper's own
  run).
- **Tests**: none pin the local values; the `check` gates are unaffected.
- **Deployment**: the local target's pods request less and carry no CPU limits; memory limits remain. The recorded
  baseline becomes the input for any future sizing, and this change is itself a local-development concern (the app
  chart's reusable defaults were handled separately).
