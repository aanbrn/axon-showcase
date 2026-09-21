# ADR-0011: Defer the Axon Framework 5 migration

Date: 2026-09-21

Status: Accepted

## Context

Axon Framework 5 is the current major, and the repository vendors the `axon4to5-*` migration skills, so moving off 4.x
is a contemplated direction. The project pins the framework at 4.x, and
`config/dependency-updates/major-disabled.properties` suppresses the `org.axonframework` major while pointing at
`showcase/quality/dependency-management` for the rationale — a pointer that resolved to nothing, so the reason was
recorded nowhere. This ADR records it.

The pin is not a matter of preference but of upstream availability. The project runs Axon **without Axon Server** (see
ADR-0009), and depends on framework modules and extensions that have no 5.x release: the Kafka and JGroups extensions
(the routing and event-propagation surface, both still 4.x-only), and the Micrometer metrics and OpenTelemetry tracing
modules (also 4.x-only). The Spring Boot starter exists at a 5.x **preview** only, not a release. Migrating the
application code while those dependencies cannot move would leave the stack split across two incompatible majors.

## Decision

Defer the Axon Framework 5 migration; remain on Axon Framework 4.x as the baseline. The distributed command bus
(JGroups, ADR-0009) and the Kafka event propagation both rest on extensions that have not migrated, so the migration is
a coordinated move rather than a framework bump.

The migration reopens when the extensions and modules the project uses publish 5.x support — at minimum the Kafka and
JGroups extensions and the Micrometer and OpenTelemetry modules, with a released (not preview) Spring Boot starter. That
availability is external and changes without the repository moving, so the check is a query against the coordinates the
catalog pins, not a date. Reopening does not require revisiting the direction, only the effort, sequenced as:
catalog/BOM bump → the extension and module swaps → per-service API and configuration fixes → full build, integration,
and e2e verification.

## Consequences

- The reference stack stays on Axon Framework 4.13.x, a supported line, with no split across two framework majors.
- The Axon major is suppressed in the dependency-update report (`config/dependency-updates/major-disabled.properties`),
  so its availability is re-checked deliberately rather than surfacing as a routine update.
- The vendored `axon4to5-*` migration skills remain a forward-looking aid: they exist to drive the migration when it
  lands, and the repository still runs the 4.x they migrate from.
- The `org.axonframework` suppression comment and the `showcase/quality/dependency-management` requirement that records
  it both point at this ADR, so the rationale has one home.

## Related decisions

This decision is a **framework-major deferral**, the same class as ADR-0003 (retain Jackson 2 until Axon and the
OpenSearch client support Jackson 3) and ADR-0004 (defer Spring Boot 4 until capacity allows the coordinated move):
ADR-0003 and this one are blocked on an external condition; ADR-0004 waits on capacity. Each records a reopen trigger
rather than a date.

It is a **sibling of ADR-0009**, not a consequence of it. ADR-0009 records the decision to run Axon without Axon Server
(avoiding its commercial licensing, with JGroups for command routing, PostgreSQL for the event store, and db-scheduler
for deadlines); this ADR records that the framework's next major is deferred on upstream availability. Both sit under
the project's shared intention to exercise Axon Framework without Axon Server, but neither conditions the other — the
extensions' 5.x availability is independent of whether Axon Server is used.
