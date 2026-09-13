# ADR-0009: Route commands over Axon's JGroups distributed command bus, not Axon Server

Date: 2026-09-13

Status: Accepted

## Context

The gateway and the command service are separate processes: the gateway accepts commands over HTTP and must route them
to the aggregate in the command service, the only writer. Axon Framework 4 offers two ways to do that — a dedicated
**Axon Server** (a standalone routing and event-store server) or a **distributed command bus** over a JGroups cluster
whose members discover each other.

The repository takes the second route. Both the gateway and the command service **exclude** `axon-server-connector` and
depend on `axon-extensions-jgroups-spring-boot-starter` and `jgroups-kubernetes`, joining a JGroups cluster
(`axon-showcase`) that discovers members by TCP-ping locally and KUBE_PING in Kubernetes. The decision predates ADR-0001
and its original rationale is not recorded anywhere in the repository; this ADR records the decision and its
consequences retroactively.

## Decision

Route commands over Axon's JGroups-based distributed command bus, and run no Axon Server: `axon-server-connector` is
excluded from both the gateway and the command service, and events are stored in PostgreSQL rather than in Axon Server's
event store.

_Alternatives considered:_ Axon Server — not used. It would add a dedicated routing and event-store server to the stack,
a further component to deploy, operate, and secure; the repository covers routing with JGroups and the event store with
PostgreSQL.

## Consequences

- No dedicated Axon component to deploy or operate — the deployed infrastructure stays Kafka, PostgreSQL, and
  OpenSearch.
- Command routing depends on JGroups cluster membership, so discovery must be provided per environment: TCP-ping for
  local runs, KUBE_PING (the Kubernetes API, via the namespace and the `jgroups-cluster=axon-showcase` labels) in the
  chart — which is why the NetworkPolicy must allow the Kubernetes lookup and the JGroups port (7800) must be reachable
  between the two pods.
- JGroups keeps JVM-global state (a fixed bind port and JVM-wide system properties), so the full-context integration
  suites that boot a JGroups-enabled service need `@DirtiesContext`.
- Major JGroups updates are held back by the dependency-update policy (see `showcase/quality/dependency-management`).
