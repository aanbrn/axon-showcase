# ADR-0009: Route commands over Axon's JGroups distributed command bus, not Axon Server

Date: 2026-09-13

Status: Accepted

## Context

The gateway and the command service are separate processes: the gateway accepts commands over HTTP and must route them
to the aggregate in the command service, the only writer. Axon Framework 4 offers two ways to do that — a dedicated
**Axon Server** (a standalone routing and event-store server) or a **distributed command bus** over a JGroups cluster
whose members discover each other.

The repository takes the second route deliberately: an intention of the project is to exercise Axon Framework **without
Axon Server**, avoiding its commercial licensing — a cost the project does not take on. Every module that depends on
`axon-spring-boot-starter` — the four services and the two clients' componentTest suites — **excludes**
`axon-server-connector`, because the project runs no Axon Server anywhere; the gateway and the command service
additionally depend on `axon-extensions-jgroups-spring-boot-starter` and `jgroups-kubernetes`, joining a JGroups cluster
(`axon-showcase`) that discovers members by TCP-ping locally and KUBE_PING in Kubernetes. The decision predates
ADR-0001; this ADR records it and its consequences retrospectively (2026-09-13).

## Decision

Route commands over Axon's JGroups-based distributed command bus, and run no Axon Server: `axon-server-connector` is
excluded from **every module that depends on `axon-spring-boot-starter`** (the four services and the two clients'
componentTest suites), because the project runs no Axon Server anywhere, and events are stored in PostgreSQL rather than
in Axon Server's event store.

_Alternatives considered:_ Axon Server — not used. Its commercial licensing is a cost the project deliberately avoids,
and it would add a dedicated routing and event-store server to the stack — a further component to deploy, operate, and
secure; the repository covers routing with JGroups and the event store with PostgreSQL.

## Consequences

- No dedicated Axon component to deploy or operate — the deployed infrastructure stays Kafka, PostgreSQL, and
  OpenSearch. The deadline/scheduler role Axon Server would otherwise fill is covered by Axon's **db-scheduler**
  integration — the `DbSchedulerDeadlineManager` (the integration also provides an `EventScheduler`) backed by
  kagkarlsson's `db-scheduler` over the same PostgreSQL — configured through the `db-scheduler.*` properties (the chart
  exposes the scheduler settings as `commandService.dbScheduler` → `DB_SCHEDULER_*`).
- Command routing depends on JGroups cluster membership, so discovery must be provided per environment: TCP-ping for
  local runs, KUBE_PING (the Kubernetes API, via the namespace and the `jgroups-cluster=axon-showcase` labels) in the
  chart — which is why the NetworkPolicy must allow the Kubernetes lookup and the JGroups port (7800) must be reachable
  between the two pods.
- JGroups keeps JVM-global state (a fixed bind port and JVM-wide system properties), so the full-context integration
  suites that boot a JGroups-enabled service need `@DirtiesContext`.
- Major JGroups updates are held back by the dependency-update policy (see `showcase/quality/dependency-management`).
