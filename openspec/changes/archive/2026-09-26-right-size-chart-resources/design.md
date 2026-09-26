# Design

## Context

See `proposal.md` — Why. The input is the measured baseline in `docs/load-tests/2026-09-26.md` (a 10-minute plateau at
119 workload units/s, ~385 rps, on a 10 vCPU / 16 GiB colima cluster). It is **host-relative and single-point** — the
load generator shares the host's CPU and there is one operating point — so it bounds sensible values rather than giving
production-true ones. The chart's default `resources` are rendered verbatim from `values.yaml` by
`common.tplvalues.render`, so omitting `limits.cpu` simply renders no CPU limit (the web UI already does this). No
capability spec describes the default resource values; the only resource-adjacent requirement is HPA/VPA rendering.

## Goals / Non-Goals

**Goals:**

- Bring the four JVM services' and the web UI's default requests within reach of measured usage.
- Drop CPU limits (avoid throttling), keep memory limits, and align the services' shape.
- Keep the local target tracking the chart defaults (no override).

**Non-Goals:**

- No production capacity plan — the baseline is one host-relative point; these are chart defaults a deployment overrides
  (`--set`, values files) or a VPA adjusts.
- No autoscaling change (HPA/VPA stay opt-in and untouched).
- No change to the local values file or to any test value file.

## Decisions

- **D1 — CPU requests from the measured usage with headroom.** `apiGateway` `1`, `queryService` `500m`, `commandService`
  `500m`, `projectionService` `100m`, `webUi` `50m`. The baseline (gateway ~0.75, query ~0.4, command ~0.2 per pod,
  projection ~0.07, web UI ~0) rounds up; the gateway keeps its current `1` because its measured use already approaches
  it. The alternative — one uniform request across the JVM services — is rejected because the gateway carries an order
  of magnitude more than the projection; alignment is in the shape, not a single number.
- **D2 — No CPU limits.** CPU limits cause throttling under burst without preventing noisy-neighbour CPU contention
  (requests do that); dropping them keeps the requests as the scheduling guarantee and the limits for memory only. The
  alternative — keep a lower limit (e.g. `1`) — was rejected as it would cap the gateway near its measured peak.
- **D3 — Memory request `0.5Gi`, limit `1Gi` for the four JVM services.** Measured working sets are ~0.36–0.52 GiB, so
  the request stays at `0.5Gi` and the limit at `1Gi`; `projectionService` rises from `0.75Gi` to `1Gi` to align. The
  web UI keeps `64Mi`/`128Mi` (measured ~44 Mi).
- **D4 — Chart defaults only; no local override.** The colima cluster has 10 cores and is not scheduling-bound, so an
  override would demonstrate nothing; keeping `values-local.yaml` resource-free lets it track the defaults.
- **D5 — `skip_specs`.** No capability spec describes the default resource values, so no delta is owed;
  `openspec validate` accepts a zero-delta change with `skip_specs: true`.

## Risks / Trade-offs

- **The baseline is host-relative and single-point.** A real deployment may need more. → These are defaults, overridable
  per environment, and the headroom is deliberate; the values are not derived as production truth.
- **No CPU limit means a runaway service can consume a node's CPU.** → CPU requests still drive scheduling and QoS, and
  memory limits remain; this is the standard latency-over-isolation trade.
- **Lower requests could mislead a scheduler.** → The CPU requests sit at or above measured steady-state usage; the
  memory requests sit at the measured working set (command-service's occasional ~519 Mi high is inside the `1Gi` limit),
  so a node sized to the requests still fits observed load.
