# Design

## Context

`idempotent-projection-writes` gave the projector a defined failure policy (ADR-0022) and deferred its operability as
its own unit (`docs/ideas.md`, 2026-10-23; issue #673). The service consumes Kafka through a reactor stream that is
retried indefinitely, and `SmartLifecycle.isRunning()` reports only that the subscription object exists, so a projector
in a retry loop is indistinguishable from a consuming one and the actuator reports the service healthy. The backpressure
buffer is documented in events but applied after the batch window, so its configured value does not bound what its unit
names. The service's `application.yml` also carries settings no classpath reads.

The local cluster is left on the pre-change release from the shipped change's rehearsal; its documents carry no
`sequence`, which ADR-0022 excludes from the guarantee. Restoring merged `main` there is this change's first execution
step, through the ADR's upgrade procedure.

## Goals / Non-Goals

**Goals:**

- An operator can tell, from the deployed service, whether the projector is consuming, retrying, or stopped, and when it
  last made progress.
- The buffer's configured value bounds the unit the property documents, and the effective bound is stated in that unit.
- Configuration that no classpath reads is removed; configuration that is load-bearing or deliberately defaulted is kept
  with its reason recorded.

**Non-Goals:**

- No dead-letter queue, gap-aware repair, rebuild facility, or fencing of an outstanding write — those are separate
  parked units and ADR-0022's policy is unchanged.
- No broker-connectivity or stall detection. After assignment, broker unreachability, a hung write, or a revoked
  partition can leave the reported state `RUNNING` without further progress, and the progress timestamp cannot
  distinguish healthy idleness from a stall; that is the parked consumer-lag unit. The local 75-second broker outage
  demonstrated the boundary.
- No Grafana panel: the gauges are exposed for one; the dashboard change is its own unit.

## Decisions

### `RUNNING` means partitions assigned, not `start()` called

`ShowcaseProjectorStateMonitor` holds a `ProjectorState`, the epoch second of the last resolved batch, and the in-flight
batch count. The state model is three values, defined by what is observable rather than by intent:

| State      | Meaning                                                                 | Health |
| ---------- | ----------------------------------------------------------------------- | ------ |
| `STOPPED`  | not consuming — never started, starting (no assignment yet), or stopped | DOWN   |
| `RUNNING`  | assignment since startup or the latest stream failure                   | UP     |
| `RETRYING` | the stream failed and waits for its restart delay                       | DOWN   |

The transitions are one rule each:

- `start()` leaves the state `STOPPED`; the receiver's partition-assignment listener
  (`ReceiverOptions.addAssignListener`) moves it to `RUNNING`, because assigning partitions is the milestone that proves
  the receiver reached Kafka and it also holds with an empty topic.
- An error emitted by the consumption pipeline invokes `doBeforeRetry` and moves it to `RETRYING`; a later receiver
  assignment returns it to `RUNNING`. Broker-client retries that emit no pipeline error do not cause this transition.
- `stop()` moves it to `STOPPED`.
- A batch's resolved write stamps the progress timestamp; an error or a cancellation clears its in-flight count without
  stamping it.

Gauges: `showcaseProjector.state` (0 stopped, 1 running, 2 retrying), `showcaseProjector.processing` (in-flight
batches), `showcaseProjector.last.progress` (epoch seconds, 0 before the first batch).

### The indicator gates readiness and never liveness

`ShowcaseProjectorHealthIndicator` reports `UP` for `RUNNING`, `DOWN` for `RETRYING` and `STOPPED` (details: the state,
the `lastProgressEpochSecond`, and, while retrying, the recorded failure description). `application.yml` adds it to the
readiness group explicitly (`management.endpoint.health.group.readiness.include: readinessState,projector`); the
liveness group keeps its default. Readiness reports `DOWN` before the initial assignment and during a stream-restart
wait, so Kubernetes rollout behavior depends on the probe's timing and thresholds; it is not a broker-availability
guarantee.

The consequence must hold on every probe path:

- The projection Deployment's `startupProbe` hits the root `/actuator/health`, which aggregates the new indicator; it
  moves to `/actuator/health/liveness`, so a projector that has not yet subscribed cannot kill the container.
- The projection Service needs no not-ready-address publication: the measurement with readiness DOWN and a live
  Prometheus kept the target (`up{job="axon-showcase-projection-service"} = 1`) and scraped a fresh state sample, so the
  ServiceMonitor does not lose a not-ready pod.
- The compose stack keeps `THC_PATH=/actuator/health`: compose never restarts an unhealthy container and nothing gates
  on this service's health, so the root endpoint there is the local stack's only surfacing of the projector's state.

### The buffer is bounded in events, measured by the shipped control

The buffer originally sat after `bufferTimeout`, so it buffered batches although its property documented events. The
shipped arrangement places it before the window with `limitRate(maxSize)`, which bounds the window's demand to one
batch: the measurement through `projectPartition` (a source that ignores demand, a blocked writer, `max-size: 2`,
`buffer-max-size: 4`) shows the overflow at `buffer + 2 × maxSize + 1` — nine records with `max-size: 2` and
`buffer-max-size: 4`, the buffer plus the window and the prefetch — where the post-window placement would have accepted
whole batches, and a second control feeds a demand-respecting source below the capacity and completes without
overflowing. The write prefetch is bounded to one batch (`concatMap(this::writeBatch, 1)`). The bound is **per
partition**: the configured value plus the window and the prefetch, one batch each, and the total scales with
`max-concurrency`; the property's Javadoc and the delta clause state that scope, and the delta's scenario asserts the
overflow matches the documented unit.

### The dead-configuration sweep removes only what is proven unread

A key is removed only when its own evidence proves no classpath reads it; the table is the reviewed list, and a new
candidate is added to it (and justified) before it is removed.

| Key                                                     | Disposition | Evidence                                                                                                                                                                                    |
| ------------------------------------------------------- | ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `resilience4j.*`                                        | removed     | No Resilience4j jar on the module's runtime classpath; the platform contributes only its BOM                                                                                                |
| `spring.kafka.*`                                        | removed     | No spring-kafka on the runtime classpath; the consumer reads Axon's `axon.kafka.*` properties                                                                                               |
| `spring.webflux.problemdetails.enabled`                 | kept        | The module declares `spring-boot-starter-webflux`, and the key switches `ProblemDetailsErrorHandlingConfiguration` on; its default is `false`, so the explicit `true` is live configuration |
| `spring.data.elasticsearch.repositories.enabled: false` | kept        | Spring Data Elasticsearch is on the classpath through the OpenSearch starter; the flag disables its repositories auto-configuration                                                         |
| `management.*.export.enabled: false`                    | kept        | The chart enables the Prometheus exporter through `MANAGEMENT_PROMETHEUS_METRICS_EXPORT_ENABLED`                                                                                            |

## Risks

- A stream failure makes readiness report `DOWN` until an assignment on a later subscription; a broker outage that emits
  no stream error can leave it `UP`, as the local 75-second outage showed.
- The monitor's state writes are unqualified: `stop()` runs only at context shutdown, there is no in-process stop/start
  and no actuator lifecycle endpoint, so a late callback cannot leave an observed state behind. The generation guard is
  deferred to the code path that adds an in-process restart.
- A subscribed-but-stalled projector still reports `UP`; its stale `last.progress` is the signal (see Non-Goals).
- A sustained buffer overflow restarts the stream: each re-subscription re-assigns partitions (`RUNNING`) before the
  burst overflows again (`RETRYING`), so readiness oscillates rather than holding `DOWN`. Accepted: the overflow is the
  configured signal that the writer cannot keep up, and the oscillation is visible in the state gauge and the restart
  logs.
- Moving the startup probe to liveness means a container whose projector never subscribes is not restarted by that
  probe; the readiness signal is what surfaces it.
- The scrape path was measured: Prometheus retained the target and a fresh state sample while readiness was DOWN, so the
  Service is unchanged and no not-ready-address publication is needed.
- The buffer mechanism is decided by the probe; a wrong choice shows up as either an event bound that does not hold or a
  batch-unit property that surprises an operator, and task 3.1's measurements are the acceptance evidence.

## Amendments

The change edits four delta specs:

- `…/specs/showcase/read-side/projection-service/spec.md`
  - ADDS “Projector operating state is observable” — the actuator reports the state, a retrying or stopped projector is
    not reported healthy, and the metrics surface exports the state and the last progress.
  - MODIFIES “Batch processing and ordering” — the requirement's backpressure clause is stated in the unit the probe
    establishes, with its scenario; every existing scenario is carried unchanged.
- `…/specs/showcase/quality/commit-hygiene/spec.md`
  - MODIFIES “Review-round rows record the reviewer session's continuity” — the `caller-restarted` violation record and
    the field-vs-cause clause.
  - MODIFIES “Review-round ledger rows are unique” — the misfiled-deep-pass acknowledgement.
- `…/specs/showcase/quality/agent-skills/spec.md`
  - MODIFIES “The quick review loop runs on every unit” — a replacement needs an evidenced recovery case.
- `…/specs/showcase/deployment/helm-chart/spec.md`
  - MODIFIES “Health and readiness probes” — the projection service's startup probe targets the liveness endpoint.
  - “Per-service services” is unchanged: task 2.5 found that no not-ready-address publication was needed.

ADR-0022's Consequences are refreshed (its Decision is unchanged): the parked-list sentence drops the operability state
and buffer-unit correction because this change lands them. The readiness/liveness posture is recorded in ADR-0023.

## Claims

| Claim                                                                                               | Source                                                                                                                         |
| --------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `isRunning()` returns only whether a subscription object exists                                     | `ShowcaseProjector.java:247-250`                                                                                               |
| A stream failure is retried indefinitely with a fixed delay                                         | `ShowcaseProjector.java:270-285` (`Retry.fixedDelay(Long.MAX_VALUE, …)`)                                                       |
| Before this change the backpressure buffer was applied after the batch window                       | `git show HEAD:showcase-projection-service/src/main/java/showcase/projection/ShowcaseProjector.java` (the pre-change position) |
| The buffer property is documented in events                                                         | `ShowcaseProjectorProperties.java:44-50`                                                                                       |
| The batch window requests `requested × batchSize` from upstream, not unbounded                      | `reactor-core` 3.8.7 (`FluxBufferTimeout.BufferTimeoutSubscriber.requestMore`)                                                 |
| Reactor Kafka's `ReceiverOptions` exposes a partition-assignment listener                           | `reactor-kafka` (the pinned dependency; `ReceiverOptions.addAssignListener`)                                                   |
| The projection service has no MVC/WebFlux controller                                                | `grep` over `showcase-projection-service/src/main/java` finds none                                                             |
| No Resilience4j jar is on the module's runtime classpath                                            | `:showcase-projection-service:dependencies --configuration runtimeClasspath`                                                   |
| No spring-kafka is on the module's runtime classpath                                                | the same dependency report                                                                                                     |
| The consumer's properties come from Axon's `KafkaProperties`                                        | `ShowcaseProjector.java:202-205`                                                                                               |
| Spring Data Elasticsearch is on the classpath through the OpenSearch starter                        | the same dependency report                                                                                                     |
| The chart enables the Prometheus exporter through an environment variable                           | `templates/projection-service/deployment.yaml` and `_helpers.tpl` (`MANAGEMENT_PROMETHEUS_METRICS_EXPORT_ENABLED`)             |
| The actuator exposes health, prometheus, and info                                                   | `application.yml` (`management.endpoints.web.exposure.include`)                                                                |
| The chart's projection `startupProbe` targets the root health endpoint today                        | `git show HEAD:helm/chart/src/main/helm/templates/projection-service/deployment.yaml` (its `startupProbe` path)                |
| The projection Service is what the ServiceMonitor selects                                           | `templates/projection-service/service.yaml` and `servicemonitor.yaml`                                                          |
| `spring.webflux.problemdetails.enabled` defaults to false and is read by `WebFluxAutoConfiguration` | `spring-boot-autoconfigure` 3.5.16 (`WebFluxProperties$Problemdetails`, `ProblemDetailsErrorHandlingConfiguration`)            |

## Open Questions

None.
