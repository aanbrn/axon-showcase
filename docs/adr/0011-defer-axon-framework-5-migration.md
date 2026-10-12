# ADR-0011: Defer the Axon Framework 5 migration

Date: 2026-09-21

Status: Accepted

Revisit when: the **Kafka** extension (`org.axonframework.extensions.kafka:axon-kafka`) publishes a 5.x release **and**
a replacement for AF4's JGroups distributed command bus is chosen — Axon Framework 5 ships no open-source distributed
command bus (it moved to AxonIQ's commercial offering, `io.axoniq.framework:axoniq-distributed-messaging`), so command
routing needs a decision, not a wait. The framework axis is already met: `org.axonframework:axon-framework-bom` and the
`org.axonframework.extensions.*` Spring Boot, Reactor, Micrometer-metrics, and OpenTelemetry-tracing extensions ship
stable 5.x. The Kafka wait is an external coordinate gate, checked by resolving the pinned coordinates; the routing
clause is an internal trigger, surfaced to the owner rather than detected by the tooling.

## Context

Axon Framework 5 is the current major, and the repository vendors the `axon4to5-*` migration skills, so moving off 4.x
is a contemplated direction. The project pins the framework at 4.x, and `config/dependency-updates/major-disabled.txt`
suppresses the `org.axonframework` major while pointing at `showcase/quality/dependency-management` for the rationale —
a pointer that resolved to nothing, so the reason was recorded nowhere. This ADR records it.

The pin is not a matter of preference but of upstream availability. The project runs Axon **without Axon Server** (see
ADR-0009), and two surfaces block the move: the **Kafka** extension (`org.axonframework.extensions.kafka:axon-kafka`,
latest `4.12.1`) has no 5.x release — AxonIQ lists it among the extensions whose `axon4-to-axon5-extension-*.yml`
migration recipes are no-ops today — and the **JGroups distributed command bus**, which AF4's `axon-jgroups` extension
provided and which Axon Framework 5 does not offer in the open-source framework at all: it moved into AxonIQ's
commercial offering (`io.axoniq.framework:axoniq-distributed-messaging`), and AxonIQ's own migration guide warns that
the non-commercial recipe leaves an application using that bus non-compiling.

The rest of the surface this ADR first named as blocking has since shipped, under **renamed** coordinates: the BOM is
`org.axonframework:axon-framework-bom` (the pinned `axon-bom` was renamed and publishes no 5.x), and the Spring Boot,
Reactor, Micrometer-metrics, and OpenTelemetry-tracing extensions now live at
`org.axonframework.extensions.{spring,reactor,metrics,tracing}` with stable 5.x releases — the old framework coordinates
(`axon-spring-boot-starter`, `axon-micrometer`, `axon-tracing-opentelemetry`) top out at `5.0.0-preview` or `4.x`.

Re-checked 2026-10-12 (the 2026-10-11 architecture audit reported this gate as "appears met on the framework axis")
against Maven Central metadata and AxonIQ's own migration sources — `AxonIQ/AxonFramework`'s `migration/README.md` and
its `axon4-to-axon5-extension-jgroups.yml` placeholder. The Kafka extension is still unmigrated and on the roadmap, and
no open-source JGroups extension is coming, so the gate above is **restated**, not met.

## Decision

Defer the Axon Framework 5 migration; remain on Axon Framework 4.x as the baseline. The distributed command bus
(JGroups, ADR-0009) and the Kafka event propagation both rest on surfaces that have not migrated, so the migration is a
coordinated move rather than a framework bump.

The migration reopens when the Kafka extension ships a 5.x release — and, because Axon Framework 5 has no open-source
distributed command bus, when command routing is decided: adopt AxonIQ's commercial distributed messaging, or
re-architect routing onto an open-source mechanism. Reopening does not require revisiting the direction, only the effort
and that routing decision, sequenced as: catalog/BOM bump → the extension swaps → the routing decision → per-service API
and configuration fixes → full build, integration, and e2e verification.

## Consequences

- The reference stack stays on Axon Framework 4.13.x, a supported line, with no split across two framework majors.
- The Axon major is suppressed in the dependency-update report (`config/dependency-updates/major-disabled.txt`), so its
  availability is re-checked deliberately rather than surfacing as a routine update.
- The vendored `axon4to5-*` migration skills remain a forward-looking aid: they exist to drive the migration when it
  lands, and the repository still runs the 4.x they migrate from. Their non-commercial recipe cannot carry this
  application across on its own, because it has no mapping for the distributed command bus.
- The `org.axonframework` suppression comment and the `showcase/quality/dependency-management` requirement that records
  it both point at this ADR, so the rationale has one home.

## Related decisions

This decision is a **framework-major deferral**, the same class as ADR-0003 (retain Jackson 2 until Axon and the
OpenSearch client support Jackson 3) and ADR-0004 (defer Spring Boot 4 until capacity allows the coordinated move):
ADR-0003 is blocked on an external condition and ADR-0004 waits on capacity, while this one is blocked on an external
condition **and** an open routing decision. Each records a reopen trigger rather than a date.

It is a **sibling of ADR-0009**, and no longer independent of it. ADR-0009 records the decision to run Axon without Axon
Server (avoiding its commercial licensing, with JGroups for command routing, PostgreSQL for the event store, and
db-scheduler for deadlines); this ADR records that the framework's next major is deferred. In Axon Framework 4 the two
are separable — the extensions' 4.x availability does not depend on whether Axon Server is used — but Axon Framework 5
removes that separation by moving the distributed command bus into AxonIQ's commercial offering, so the routing question
this ADR reopens is the same commercial-licensing question ADR-0009 decided for AF4.
