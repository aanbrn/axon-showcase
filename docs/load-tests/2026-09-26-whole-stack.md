# Whole-stack load-test baseline: 2026-09-26

A second measurement from the reworked load tests (`scripts/load-test-baseline.sh`), with the sampler extended to
`kubectl top pods -A` so the **infrastructure and observability** namespaces are measured alongside the app. It is the
input for sizing the local values of the infra and monitoring releases. **Host-relative**: the load generator shares the
host's CPU with the cluster, so these are relative guidance, not absolute capacity.

## Target

- Local Helm cluster (`./gradlew helmInstallToLocal`), reached through the gateway ingress hostname
  `http://axon-showcase-api`.
- Node: **10 vCPU / ~16 GiB** (colima), carrying the app tier, PostgreSQL, Kafka, OpenSearch, and the kps/tempo
  observability stack.
- App tier on the chart defaults (`right-size-chart-resources`): the four JVM services at `0.5Gi` memory requests; the
  infra and monitoring releases on their local values at the time.

## Method

1. **Calibrate** — `calibrate` ramp to 200 workload units/s over 5 minutes; the knee was **not measured** (no sustained
   departure at the ceiling), so the operating point is 60 % of the ceiling.
2. **Baseline** — a 10-minute constant plateau at **120 units/s**, read share 0.75, 10 SSE connections held for the
   plateau, sampling every namespace.

## Result

- Requests: **234,030** over the 10-minute plateau (**~389 rps**), 0 failed.
- Response time: mean **5 ms**, p95 **8 ms**, p99 **12 ms**, max 514 ms.

Per-pod CPU / memory during the steady-state plateau (median of the middle 80 %):

| Namespace / pod                                          | CPU                 | Memory         |
| -------------------------------------------------------- | ------------------- | -------------- |
| axon-showcase / api-gateway                              | ~0.83 cores         | ~0.52 GiB      |
| axon-showcase / query-service                            | ~0.44 cores         | ~0.37 GiB      |
| axon-showcase / command-service (2 pods)                 | ~0.14–0.29 cores    | ~0.47 GiB      |
| axon-showcase / projection-service                       | ~0.07 cores         | ~0.43 GiB      |
| axon-showcase / web-ui                                   | ~0.001 cores        | ~0.04 GiB      |
| axon-showcase / db-events (postgres)                     | ~0.24 cores         | ~0.85–0.97 GiB |
| axon-showcase / kafka-controller                         | ~0.16 cores         | ~1.42–1.45 GiB |
| axon-showcase / os-views master                          | ~0.21 cores         | ~1.08 GiB      |
| axon-showcase / os-views data                            | ~0.18 cores         | ~1.58–1.61 GiB |
| monitoring / prometheus-kps                              | ~0.04 cores (5–51m) | ~1.45–1.47 GiB |
| monitoring / kps-grafana (pod total)                     | ~0.01 cores         | ~0.94 GiB      |
| monitoring / tempo                                       | ~0.02 cores         | ~1.15–1.68 GiB |
| monitoring / kps-operator                                | ~0.002 cores        | ~0.07 GiB      |
| monitoring / kps-kube-state-metrics                      | ~0.001 cores        | ~0.06 GiB      |
| kube-system / traefik                                    | ~0.26 cores         | ~0.16 GiB      |
| kube-system / coredns, metrics-server, local-path, svclb | ≤0.005 cores each   | ≤0.08 GiB each |

Observations for sizing: the infra CPU is well below the `0.5`-core requests, the `3.0`-core limits are never
approached; memory requests mostly overstate the working set except **Prometheus** (~1.46 GiB against a `1.0Gi`
request), **Grafana** (~0.94 GiB pod — Grafana plus its two `128Mi` sidecars — against a `0.5Gi` request), and the
**OpenSearch master** (~1.08 GiB against a `1.0Gi` request), which the sizing raises. Grafana's own container is a
`0.75Gi` request, so the pod request (~1 GiB with the sidecars) matches the measured pod total.

## Caveats

- Point-in-time and host-relative; a different colima shape changes the numbers.
- `kubectl top` aggregates a pod's containers (Grafana's `0.94 GiB` includes its two sidecars, datasource and dashboard)
  and has ~15 s resolution.
- No app-chart defaults change here — this sizes only the infra and observability local values.
