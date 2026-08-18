## Context

`showcase-command-service` boots a heavy Axon context (event sourcing + PostgreSQL event store, JGroups distributed
command bus, saga persistence, DB Scheduler, metrics). It has component/ITs for individual pieces but no full-context
test of the `ShowcaseCommandApplication` bean wiring. See proposal.md - Why for motivation.

## Goals / Non-Goals

**Goals:**

- Verify the command-service's application bean wiring via a real context boot at the integration tier (mirroring
  `ShowcaseApiApplicationIT` and `ShowcaseQueryApplicationIT`).

**Non-Goals:**

- No production-code changes.
- No new coverage infra.

## Decisions

- **Full-context `@SpringBootTest` at the integration tier**, with Testcontainers PostgreSQL (event store, Flyway
  migration) and Kafka, since the command-service requires them to boot. *Alternative considered:* slice tests —
  rejected because they don't verify the full `ShowcaseCommandApplication` wiring.
- **Mirror `ShowcaseApiApplicationIT`**: assert the application beans — the JGroups connector, the primary
  `DistributedCommandBus`, the saga store, the Caffeine caches, the snapshot trigger, the DB scheduler, and the
  metrics beans.
- **Isolate JGroups** on a dedicated bind port and cluster name (as the gateway IT does) to avoid conflicts with a
  locally running instance.

## Risks / Trade-offs

- [Full-context boot with Testcontainers is slower than slice tests] → accepted; it is the integration tier's purpose.
- [JGroups connector boot in tests can be flaky] → isolated to a dedicated port/cluster.
