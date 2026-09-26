# Design

## Context

See `proposal.md` — Why. Current local values (requests/limits) and what the app-namespace baseline measured:

| Release               | Component                             | Request CPU/Mem  | Limit CPU/Mem   | Measured CPU/Mem |
| --------------------- | ------------------------------------- | ---------------- | --------------- | ---------------- |
| db-events (postgres)  | primary                               | `0.5` / `2.0Gi`  | `3.0` / `3.0Gi` | ~0.25 / ~0.8Gi   |
| kafka                 | controller                            | `0.5` / `2.0Gi`  | `3.0` / `3.0Gi` | ~0.15 / ~1.3Gi   |
| os-views (opensearch) | master                                | `0.5` / `1.0Gi`  | `3.0` / `1.5Gi` | ~0.19 / ~1.05Gi  |
| os-views              | data                                  | `0.5` / `2.0Gi`  | `3.0` / `3.0Gi` | ~0.16 / ~1.57Gi  |
| kps                   | prometheus / grafana / operator / ksm | mixed            | mixed           | **not measured** |
| tempo                 | tempo                                 | `0.25` / `2.0Gi` | `1.0` / `3.0Gi` | **not measured** |

The app-tier baseline sampled `kubectl top pods -n axon-showcase`, so the monitoring namespace was excluded. The
requests sum (app ~2.6 GiB + infra ~7.6 GiB + monitoring ~3.8 GiB) to roughly 14 GiB of the ~16 GiB VM.

## Goals / Non-Goals

**Goals:**

- Measure the whole cluster during a baseline plateau, monitoring included.
- Size the infrastructure and observability local values from that single measurement.
- Apply the app chart's CPU policy (CPU requests, no CPU limits) for consistency.

**Non-Goals:**

- No change to the app chart's defaults (done separately) or to the pinned infra chart versions.
- No production capacity claim — the measurement is host-relative and single-point.
- No autoscaling or persistence change.

## Decisions

- **D1 — Sample every namespace.** The wrapper switches to `kubectl top pods -A`, so one run covers the app, the
  infrastructure, and the observability stack (and the kube-system baseline overhead). The `load-tests` spec's
  "per-service resource usage" already covers this, so no delta.
- **D2 — The app chart's CPU policy applies to every block the values author.** Drop the `limits.cpu` from each
  `resources` block the local values name — the service containers and their exporters/sidecars; the charts' own init
  containers that the values do not name keep their shipped defaults. Keep the memory limits.
- **D3 — Requests from the measurement with headroom; memory limits stay above the measured peak.** CPU requests round
  up from the measured steady state; memory requests sit at the measured working set and limits keep headroom,
  especially for OpenSearch's JVM heap and Kafka's page cache, where a tight limit risks OOM rather than just
  throttling.
- **D4 — Helper containers keep small, aligned values.** Exporters, sidecars, the operator, and kube-state-metrics keep
  small requests rather than being sized from the coarse `kubectl top` sample, and the config reloader's local `64Mi`
  memory limit is raised to `128Mi` to align with the operator's.
- **D5 — `skip_specs`.** No capability spec describes the local values' resource numbers.

## Risks / Trade-offs

- **Host-relative, single-point measurement.** → Values are local-target overrides, deliberately conservative for the
  memory-constrained services; the recorded baseline is the evidence.
- **Lowering a memory limit can OOM a stateful service.** → Memory limits stay above the measured peak with margin, and
  only the requests (not the limits) are trimmed hard.
- **A monitoring service now measured under load.** → The re-run is the measurement; values are set from it, not
  guessed.
- **Re-applying the infra values restarts the ephemeral state stores.** Persistence is disabled locally, so upgrading
  PostgreSQL, Kafka, or OpenSearch wipes their data and the app release's `pre-upgrade` hooks (database migration, index
  initialization) leave no schema/index until re-run. → The app release is upgraded after the resized releases (task
  3.1), and the post-resize plateau (task 3.2) is the end-to-end check.
