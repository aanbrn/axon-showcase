# ADR-0023: Projector health gates readiness, never liveness

Date: 2026-10-24

Status: Accepted

## Context

`ShowcaseProjector` consumes Kafka through a stream that is retried indefinitely after a failure, and its
`SmartLifecycle.isRunning()` reports only that a subscription object exists. Before this change the actuator therefore
reported a projector that was repeatedly recovering exactly as it reported a consuming one, and the parked operability
unit (issue #673) recorded that gap.

The service is deployed by the Helm chart with three probe paths and by Docker Compose with its own health check. The
chart's startup probe and the compose health check both target the root `/actuator/health`, which aggregates every
`HealthIndicator` bean; the readiness and liveness groups exist as separate endpoints. The Prometheus ServiceMonitor
scrapes the Service, whose endpoints normally exclude pods that are not ready.

The alternatives were: expose the state only as a metric and leave health alone (the service still reports healthy while
not consuming); expose a dedicated endpoint outside the probe groups (visible only to someone who knows the endpoint);
or let the state participate in the probes.

## Decision

A projector that is **retrying** or **stopped** is reported not healthy; a running one reports its state and the time of
its last resolved batch. The indicator joins the **readiness** group only, and the readiness group lists it explicitly
(`readinessState,projector`). Liveness keeps its default, and the chart's startup probe targets
`/actuator/health/liveness` rather than the root health, so a projector that has not yet subscribed can fail a rollout
but never kill the container. The compose stack keeps its health check on the root endpoint: compose never restarts an
unhealthy container, so that endpoint is the local stack's surfacing of the state.

`RUNNING` records that receiver assignment has occurred since startup or the latest stream failure. It is not a
current-connectivity or progress guarantee; errors emitted by the consumption pipeline set `RETRYING`, while retries
internal to the Kafka client do not necessarily do so.

The scrape path was measured: Prometheus retained the target and scraped a fresh state sample while readiness was DOWN,
so the Service publishes no not-ready addresses.

Broker-connectivity and stall detection are outside this decision: a receiver that has reached assignment can remain
reported `UP` while Kafka is unreachable or processing is stalled, and an unchanged progress timestamp alone cannot
distinguish that from healthy idleness.

## Consequences

- Readiness reports `DOWN` before initial assignment and during stream-restart waits; whether Kubernetes marks the pod
  not ready or delays a rollout depends on probe timing and thresholds.
- The liveness path is unchanged in meaning, so a pod is never restarted for the projector's state alone.
- No not-ready-address publication is needed: the ServiceMonitor keeps the target while readiness is down.
- Consumer-lag-based stall detection is parked in `docs/ideas.md` (2026-10-24); if it lands, this ADR's
  `UP`-while-stalled boundary is the clause to revisit. **Superseded by the 2026-11-05 amendment below: the parked entry
  was implemented as backlog visibility, and this boundary is retained.**

## Amendment — 2026-11-05: the backlog is observable, the health boundary is retained

Context: the change `surface-projector-backlog` implements **the observation** the parked entry "Detect a stalled
projector from consumer lag" deferred — it reads the consumer group's committed offsets against the topic's end offsets
through its own `AdminClient`, so the distance no longer depends on the consumption pipeline reporting anything. The
detection the entry's title named is superseded out of scope rather than shipped; what lands is the visible distance,
and this amendment supersedes the entry rather than the Decision:

- the projector's health indicator carries `consumerLag` (the maximum resolved partition distance),
  `consumerLagUnresolvedPartitions`, and `consumerLagObservedEpochSecond`;
- the dashboard renders the per-partition distance beside the sample's age and its unresolved-partition count;
- a partition the sample cannot resolve — such as one with no committed offset, a committed offset past the end, or a
  partition the topic no longer reports — publishes no distance and is counted as unresolved, so an unavailable answer
  is never a healthy zero; the requirement "The projector's consumer backlog is observable" in
  `read-side/projection-service` owns that list.

The Decision above stands unchanged, including its `UP`-while-stalled boundary. A diagnostic distance is not a binary
health policy: a stalled projector still reports `UP`, because no measured threshold distinguishes an unacceptable delay
from a large but healthy batch, and the readiness group's meaning — "the consumer holds partitions" — is what a rollout
should turn on. `docs/ideas.md` therefore no longer carries that entry; the surviving content is this amendment.
