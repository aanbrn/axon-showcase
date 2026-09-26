# Proposal: Right-size the chart's default resources

## Why

The chart ships speculative default resources: the four JVM services each request `1.0` CPU / `0.5Gi` with a `3.0`-CPU
limit, the web UI requests `100m` / `64Mi` with no CPU limit, and the local target overrides none. The
`rework-load-tests` baseline (`docs/load-tests/2026-09-26.md`) now measures steady-state usage at ~385 rps — the gateway
~0.75 CPU, query ~0.4, command ~0.2, projection ~0.07 — so the requests overstate every service (the gateway's measured
~0.75 already approaches its `1.0` request), and the 3-CPU limits cap services far above anything observed. Size the
defaults from that measurement, drop the CPU limits, and align the services' shape.

## What Changes

- `helm/chart/src/main/helm/values.yaml`: replace the five `resources` blocks with the measured-with-headroom set, and
  remove every `limits.cpu`:

  | Service           | CPU request | Memory request | Memory limit |
  | ----------------- | ----------- | -------------- | ------------ |
  | apiGateway        | `1`         | `0.5Gi`        | `1Gi`        |
  | queryService      | `500m`      | `0.5Gi`        | `1Gi`        |
  | commandService    | `500m`      | `0.5Gi`        | `1Gi`        |
  | projectionService | `100m`      | `0.5Gi`        | `1Gi`        |
  | webUi             | `50m`       | `64Mi`         | `128Mi`      |

- `docs/ideas.md`: remove the implemented "Rethink resource requests/limits …" entry.
- `docs/load-tests/2026-09-26.md`: update its Target bullet to describe the chart defaults the run used in the past
  tense, correcting the inaccurate request/limit parenthetical (the run predates this change).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none) — no capability spec describes the chart's default resource values (the `helm-chart` spec's resource-adjacent
requirement is the HPA/VPA one, not the defaults), so this change sets `skip_specs: true`.

## Impact

- **Build**: chart values only; `helmLintMainChartFull`/`Minimal` and `helmPackageMainChart` still pass. No gate asserts
  the default values.
- **Tests**: none pin the resources (the lint value files set VPA bounds, not the service defaults).
- **Docs**: the baseline record's Target bullet is corrected to the defaults it ran against.
- **Deployment**: the deployed pods render lower CPU requests with no CPU limit; memory limits stay (projection rises
  from `0.75Gi` to `1Gi` for consistency). The local target overrides nothing, so it keeps tracking the chart defaults.
