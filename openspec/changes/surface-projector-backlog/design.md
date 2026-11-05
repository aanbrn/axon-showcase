# Design — surface the projector's consumer backlog

## Context

The projector's observability was decided by `projector-operability` and bounded by ADR-0023. It exposes the lifecycle
state (`STOPPED`/`RUNNING`/`RETRYING`), the epoch second of the last resolved batch, and the in-flight batch count, and
the indicator reports `UP` for `RUNNING` only, in readiness and never in liveness. ADR-0023 states that `RUNNING` means
"receiver assignment has occurred since startup or the latest stream failure", that it "is not a current-connectivity or
progress guarantee", and that "broker-connectivity and stall detection are outside this decision". Its Consequences park
consumer-lag-based stall detection in `docs/ideas.md` and name the `UP`-while-stalled boundary as the clause to revisit
if it lands.

The dashboard already renders the projector's progress, but only as time:

| Panel                               | What it measures                                                                                  | Why it cannot answer the backlog question                                                                                                                   |
| ----------------------------------- | ------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Event Age` (id 19)                 | the age of an event between its domain timestamp and its ingest, per payload type, as percentiles | it is sampled only when an event is ingested, so a consumer that stopped ingesting produces no sample at all, and it measures time, not outstanding records |
| `Since Last Resolved Batch` (id 86) | seconds since the last resolved batch, idle time included                                         | its own description says a growing age "cannot distinguish a stall from a healthy idle system (ADR-0023)"; it is not a verdict                              |

The parked idea asked for stall detection. An owner-authorized consultation with the technical advisor re-derived the
boundary from the evidence and narrowed it: the unit makes the projector's outstanding work **visible**, and does not
classify a stall, change the readiness policy, or alert. The advisor's reasons, in order:

- A "lag not decreasing" predicate reports healthy processing as stalled: a producer and a consumer that both advance
  leave the distance unchanged.
- The receiver's own consumer metrics are sampled on the poll loop, so the observation stops exactly when the consumer
  does; they also measure the **fetched** position, which excludes records already fetched and not yet committed — the
  hung-writer case the signal is for.
- No evidence in this repository justifies a stall threshold, so shipping one would be an unmeasured guess.

The repository has no other source of committed-offset distance: the Kafka release enables only the Bitnami JMX
exporter, whose broker MBeans carry no consumer-group offsets, and no `kafka-exporter` is deployed.

## Goals / Non-Goals

**Goals**

- Expose, per partition, how far the consumer group's committed offsets are behind the topic's end offsets.
- Expose the age of the last successful sample and the number of failed samples, so a reader can tell a fresh zero from
  a stale number.
- Report a partition the sampler cannot resolve as unresolved, never as zero backlog.
- Show the backlog in the bundled dashboard and the summary in the actuator's projector details.

**Non-Goals**

- Classifying a backlog as a stall, or choosing a stall window or threshold.
- Changing the projector's health status, its readiness membership, or liveness.
- Alerting, or a runbook that says when a backlog is "too large".
- Guaranteeing projection completeness: an acknowledged record whose projection was logged and skipped leaves lag at
  zero, which this signal cannot see.
- Chart templates, ServiceMonitor settings, and values: the projection service's ServiceMonitor already scrapes
  `/actuator/prometheus`, so only the bundled dashboard JSON gains panels.

## Decisions

1. **A separate `AdminClient` samples the group's committed offsets, not the receiver's consumer metrics.** The
   observation must outlive the state it observes: a consumer that stops polling cannot publish a fresh lag, and its
   metric measures the fetched position, which advances past records whose writes have not resolved. The admin call
   measures end-offset minus committed offset, so a batch that was fetched and never acknowledged still counts as
   outstanding. Alternative rejected: reactor-kafka's `ReceiverOptions.consumerListener` with `Consumer.metrics()` —
   available in the pinned 1.3.25, but its freshness and its semantics are both wrong for this signal.
2. **The sampler owns its lifecycle and its own scheduler, off every request and scrape path.** It implements
   `SmartLifecycle`, starts one virtual thread that samples at a fixed delay, and closes the client on stop. The health
   indicator and the Prometheus scrape read a volatile snapshot, so neither waits on Kafka. The configured timeout
   bounds **each** admin call (the topic description, the end-offset read, and the group-offset read), so a sample can
   take up to three times it, and its floor is 100 milliseconds rather than a whole second so those three calls stay
   inside one 15-second interval even when a deployment shortens it; a sample still in flight when the sampler stops is
   abandoned with the client closed under it, and the thread is not awaited beyond the client's own close. Alternative
   rejected: a scrape-time computation (a scrape would then block on a broker, and a stalled scrape would hide the very
   fact it reports).
3. **The admin client is built from the configured consumer properties, filtered to the keys `AdminClientConfig`
   knows.** The service has no admin configuration of its own, and the connection material (bootstrap servers, security
   protocol, and any SASL settings a deployment adds) arrives through the Kafka consumer properties. Filtering by
   `AdminClientConfig.configNames()` keeps the connection keys and drops consumer-only ones (`group.id`,
   `partition.assignment.strategy`), which the client would otherwise warn about. Alternative rejected: a hand-written
   list of connection keys, which a deployment's SASL addition would silently miss.
4. **A sample visits the union of the topic's partitions and the group's committed partitions, and resolves each to a
   distance or to unresolved.** The union matters at both ends: a partition present in the committed offsets but absent
   from the topic is still visited and publishes `NaN`, so it neither keeps a stale distance nor disappears, and a
   partition the group never committed appears at all. A partition resolves only when the end offset and a committed
   offset both exist and the committed offset is not past the end; otherwise it publishes `NaN` (so the series shows a
   gap rather than a healthy zero) and is counted by an `unresolved` gauge. An end-offset read that fails is not one
   partition's problem — the admin call resolves all of them together — so it fails the whole sample and is counted by
   the failure counter instead.
5. **A failed sample is visible as one.** It leaves the last per-partition values and the observation time in place and
   increments a failure counter, so a reader sees a stale distance plus the number of failed attempts rather than a
   value with no provenance. The counter reports attempts that failed, never that the sampler is running: a `0` beside a
   stale observation time means the attempts have not started.
6. **The health indicator gains details, not a status.** Three details join the existing ones: `consumerLag`, named as
   the **maximum resolved partition distance**; `consumerLagUnresolvedPartitions`, so a fresh sample that resolved one
   partition and left three unresolved cannot read as a complete zero; and `consumerLagObservedEpochSecond`. The
   distance is omitted when no partition resolved — the same rule as the metric, since a `0` there would be the healthy
   zero the change exists to prevent. The `UP`/`DOWN` computation, the readiness group, and liveness are untouched.
   ADR-0023's amendment records that the observation now exists and why the `UP`-while-stalled boundary is retained: a
   diagnostic number is not a binary health policy, and no measured threshold would justify one. The backlog's own
   outcomes — distance, freshness, failed samples, unresolved partitions — live in a requirement of their own, so the
   lifecycle requirement keeps stating what the state means and the backlog requirement states what is measured.
7. **One metric family, tagged per partition, with a pinned exposition name.** `showcaseProjector.consumerLag` carries
   `topic` and `partition` and a base unit of `records`, so the Prometheus name is
   `showcaseProjector_consumerLag_records` — a unit suffix the Micrometer name does not show, which is why a test pins
   the names the panels query, as `ShowcaseProjectorStateMonitorPrometheusTests` already does for the three existing
   gauges. The dashboard aggregates with `max by (partition, topic, pod)` rather than `sum`: every replica samples the
   same group, so a sum would multiply one distance by the replica count, and dropping the pod would let one replica's
   stale value stand for a fresh one from another. The sample age and the unresolved count carry the same pod identity,
   so a distance is always read beside the freshness of the sampler that produced it.
8. **Verification uses unit tests for the arithmetic and the publication, and one Kafka-backed integration test for the
   observation.** The arithmetic and the publication have exact expectations over a `PrometheusMeterRegistry`; the
   observation needs a broker and a controllable consumer, so it runs at the integration tier. Alternative rejected: a
   component test with a mocked admin client, which would verify the wiring rather than that the numbers match a broker.

## Risks / Trade-offs

- **One admin read per interval per replica.** At the default 15 seconds and three partitions, the cost is one
  `ListOffsets` and one `ListConsumerGroupOffsets` call per sample; each replica issues its own, since the sampler has
  no leadership election. No measurement of that cost was taken; the calls are small and bounded by the configured
  timeout.
- **Committed offsets lag acknowledgement by the client's commit interval.** The distance therefore includes recently
  acknowledged work, so a steady zero under load is expected rather than proof that nothing is in flight; the in-flight
  gauge covers that half.
- **Replicas sample independently.** A replica whose samples keep failing keeps its last distances while another replica
  reports fresh ones, so the panel can show a mixed-age view; the pod tag on every series is what lets a reader see
  which sampler a number came from, and no freshness threshold is introduced to collapse them.
- **The metric exists only after the first successful sample.** Before it, the panel shows no series and the details
  omit the summary; the `observed` gauge stays `0` while the failure counter reports how many attempts failed, so a
  sampler that has not attempted one and one whose attempts keep failing are told apart by the counter's value.
- **A topic whose partitions are unknown to the client resolves nothing.** The sampler describes the topic each sample,
  so a newly created partition appears on the next interval; a describe failure counts as a failed sample.

## Verification

```bash
# The arithmetic, the publication, and the exposition names
./gradlew :showcase-projection-service:test

# The new properties' binding and rejection cases
./gradlew :showcase-projection-service:componentTest

# Zero backlog, backlog while consumption is stopped, drain after restart, a group that never
# committed, and an unreachable broker — needs Docker
./gradlew :showcase-projection-service:integrationTest

# The module's gates, Docker-free
./gradlew :showcase-projection-service:check -PskipITs -Pcoverage.gate.enabled=false

# Whole-repository gates that read the dashboard JSON and the specs
./gradlew check -PskipITs -Pcoverage.gate.enabled=false
openspec validate --all
```

The panels are deployment artifacts, so the implementation also reads them back from the local stack: the four series
from Prometheus, and the provisioned panel from Grafana, each against the control that produced it — a positive backlog,
a fresh zero, an unresolved partition, and a stale observation. `verifyDashboardJson` proves the JSON is provisionable,
and the exposition test proves the names exist; neither proves a query returns what the panel claims, and a screenshot
proves neither.

## Claims table

| Claim                                                                                                                         | Source                                                                                                                                                           | Result                                                                                                                                                                |
| ----------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| No production `AdminClient` exists today                                                                                      | `grep -rn "AdminClient" showcase-projection-service/src`                                                                                                         | the only file is `ShowcaseProjectorIT`, the integration test's own client                                                                                             |
| The requirement exposes exactly three signals                                                                                 | `sed -n '139,182p' openspec/specs/showcase/read-side/projection-service/spec.md`                                                                                 | "observed lifecycle state, last resolved batch time, and in-flight batch count", with six scenarios                                                                   |
| ADR-0023 parks lag detection and names the boundary to revisit                                                                | `grep -n "Consumer-lag-based stall detection" docs/adr/0023-projector-health-readiness.md`                                                                       | Consequences: "parked in `docs/ideas.md` (2026-10-24); if it lands, this ADR's `UP`-while-stalled boundary is the clause to revisit"                                  |
| The dashboard renders progress only as time                                                                                   | the dashboard JSON's panels 19 and 86                                                                                                                            | `Event Age` measures the ingest delay per payload type; `Since Last Resolved Batch` says a growing age "cannot distinguish a stall from a healthy idle system"        |
| The Kubernetes deployment has no consumer-group lag source                                                                    | `grep -rn "kafka-exporter\|consumer_group" helm/`                                                                                                                | no hits; the Kafka release enables only `metrics.jmx`                                                                                                                 |
| The new metrics need no chart wiring change                                                                                   | `helm/chart/src/main/helm/templates/projection-service/servicemonitor.yaml`                                                                                      | the ServiceMonitor scrapes `/actuator/prometheus` on the `http-management` port                                                                                       |
| The dashboard had 44 non-row panels in 8 sections before this change, and `README.md` stated that count                       | a `json` load of the dashboard at `d8cf14ba` counting rows and non-rows, and `git show d8cf14ba:README.md \| grep -n "44 panels"`                                | 44 non-row panels over 8 rows, and the README carried both figures; this change adds two panels and the README now states 46                                          |
| A gauge's base unit becomes an exposition suffix                                                                              | `ShowcaseProjectorStateMonitorPrometheusTests` and its `last.progress` → `last_progress_seconds` mapping                                                         | the existing test pins the suffix, which is why the new names are pinned the same way                                                                                 |
| The receiver exposes the live consumer, but only on its poll loop                                                             | `javap` over `reactor-kafka-1.3.25.jar`                                                                                                                          | `ReceiverOptions.consumerListener(ConsumerListener)` with `consumerAdded`/`consumerRemoved` exist, so the rejection is about freshness, not availability              |
| Filtering the consumer properties by `AdminClientConfig.configNames()` keeps the connection keys and drops consumer-only ones | a single-file `java` probe over the pinned `kafka-clients-3.9.2.jar` and `slf4j-api` printing `AdminClientConfig.configNames()`, and the assertion task 1.2 adds | the run printed 74 names, holding `bootstrap.servers`, `security.protocol`, `ssl.*`, and `sasl.*`, and holding neither `group.id` nor `partition.assignment.strategy` |
| `KafkaProperties` builds consumer and producer properties only                                                                | `javap` over `axon-kafka-spring-boot-autoconfigure-4.12.1.jar`                                                                                                   | `buildConsumerProperties()` and `buildProducerProperties()` exist; no admin variant                                                                                   |

Dismissals, one reason per kind: `path` — every named path is a repository file this change reads or edits, a path the
change's own verification names, or a media type; `absolute` — the flagged words sit inside quoted requirement or ADR
text, inside the change's own Non-Goals and prohibitions, or in a unit-record field whose value is a standing factual
statement rather than a claim about behavior; `count` — the flagged figures are the dashboard's panel and section
counts, the configured sample interval and bounds, the environment's partition figures the design cites, the change's
task identifiers, and the claims table's own rows; `cross-ref` — the flagged commands and paths are this change's own
verification instructions.

## Consequences

- An operator can see how many records the projector is behind, per partition, and how old that number is, without
  reading a log.
- The health endpoint stays a report of the lifecycle state; a stalled projector remains reported healthy, and the ADR
  says so with its reason rather than silently.
- The projector's "is it alive", "how long since it did work", and "how much work is outstanding" questions become three
  signals, so none is asked of a number that cannot answer it.
