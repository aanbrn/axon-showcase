# Design

## Context

See `proposal.md` — Why. The re-check (2026-10-12) resolved ADR-0011's gate against the coordinates the catalog pins,
querying Maven Central metadata and reading AxonIQ's own migration sources. The measured landscape:

| Axis (as pinned)                                                    | 5.x state                                                                                                                                                              |
| ------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BOM `org.axonframework:axon-bom`                                    | no 5.x — **renamed** to `org.axonframework:axon-framework-bom` (stable `5.3.3`)                                                                                        |
| Framework modules (`axon-messaging`, `axon-modelling`, `axon-test`) | stable `5.3.3`                                                                                                                                                         |
| Spring Boot starter                                                 | moved to `org.axonframework.extensions.spring:axon-spring-boot-starter` (stable `5.3.3`); the old `org.axonframework:axon-spring-boot-starter` is `5.0.0-preview` only |
| Micrometer metrics                                                  | moved to `org.axonframework.extensions.metrics:axon-metrics-micrometer` (stable `5.3.3`)                                                                               |
| OpenTelemetry tracing                                               | moved to `org.axonframework.extensions.tracing:axon-tracing-opentelemetry` (stable `5.1.2`)                                                                            |
| Reactor                                                             | moved to `org.axonframework.extensions.reactor:axon-reactor` (stable `5.3.3`)                                                                                          |
| **Kafka** (`org.axonframework.extensions.kafka:axon-kafka`)         | **no 5.x** (`4.12.1` latest)                                                                                                                                           |
| **JGroups** (`org.axonframework.extensions.jgroups:axon-jgroups`)   | **no 5.x** (`4.12.0` latest) — and none is coming                                                                                                                      |

Two findings beyond the coordinates, both from AxonIQ's own sources:

- **Kafka is a genuine wait.** `AxonIQ/AxonFramework`'s `migration/README.md` lists Kafka among the "extensions without
  finalized AF5 mappings", whose `axon4-to-axon5-extension-*.yml` recipes "are no-ops today"; the Kafka placeholder's
  own description says the extension "does not yet have a finalized Axon Framework 5 equivalent".
- **JGroups is not.** The JGroups placeholder's description states the AF4 distributed command bus "has moved into the
  AxonIQ commercial offering (`io.axoniq.framework:axoniq-distributed-messaging`)", and the same README warns that the
  non-commercial `UpgradeAxon4ToAxon5` recipe "leaves your codebase non-compiling" for an app using the distributed
  command bus. So no open-source 5.x JGroups extension will appear, and the clause is unsatisfiable as an "upstream
  ships it" wait.

The stale text lives on three surfaces: ADR-0011 — the `Revisit when:`, the `Context`'s "no 5.x release"/"4.x-only"/
"preview only" claims and the "sibling … independent of whether Axon Server is used" paragraph, the `## Decision`
paragraph that repeats the reopen gate ("reopens when the extensions … publish 5.x support"), and the
`## Related decisions` sentence classifying this ADR as "blocked on an external condition" — the `dependency-management`
requirement's body (which restates the same rationale), and the `major-disabled.txt` comment.

## Goals / Non-Goals

**Goals:**

- Make ADR-0011's record true: the trigger states the current coordinates and names the routing decision, and the
  `Context` carries the measured landscape with the re-check dated — so the next architecture audit reads it correctly.
- Correct the two restatements of the same rationale (the spec body, the config comment) in the same sweep.

**Non-Goals:**

- Taking the routing decision (commercial distributed messaging versus a re-architected open-source routing). It amends
  ADR-0009's premise and is the owner's call; this change records it as the open question instead.
- Re-planning the migration: the Kafka wait alone keeps it deferred.
- Amending ADR-0009 itself: it is an AF4-scoped, settled decision whose text stays accurate for the pinned major.

## Decisions

**Reword the trigger to the current coordinates and split it into a wait plus a decision.** ADR-0011's `Revisit when:`
stays required (it is a deferral), but it must name coordinates that can actually publish a 5.x — the renamed BOM and
the `extensions.*` groups — and state the routing decision rather than a JGroups release, since none is coming.

**Correct the spec's rationale body, not its scenarios.** The suppression outcome and all three scenarios are unchanged;
only the body that explains why restates a now-false fact. A `MODIFIED` delta carries the full requirement (description
plus every scenario) with the body rewritten.

**Record the re-check in the ADR's `Context`, dated and sourced.** The audit re-raises a deferral whose condition
appears met; naming the 2026-10-12 re-check, its result, and the two sources that settle the JGroups question stops the
loop without adding a mechanism.

## Risks / Trade-offs

- Axon's roadmap can move (the Kafka extension "on the roadmap") → the Kafka half is a coordinate check a future check
  re-reads, and the ADR names that signal rather than predicting a date; the routing half is named as an owner-surfaced
  trigger, so a coordinate-only check is not mistaken for the whole gate.
- The commercial-routing fact is external and can change → the ADR cites AxonIQ's migration sources by name, so a reader
  re-verifies at the source rather than trusting the paraphrase.

## Migration Plan

None: a record correction, taking effect on the next read.
