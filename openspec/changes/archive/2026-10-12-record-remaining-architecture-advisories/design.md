# Design

## Context

See `proposal.md` — Why. Three of the audit's architecture section's advisories concern the completeness of the decision
record; of that section's other two, the ADR-0011 item shipped in `#553` and the ADR-0003 gate stays parked. Each item
below was investigated from the dependency's own sources or the build graph before being recorded, rather than from the
advisory's question:

- **LZ4.** `at.yawk.lz4:lz4-java`'s README states the fork is "the community-maintained continuation of lz4/lz4-java,
  which is no longer maintained … originally created to fix CVE-2025-12183", that it "is a drop-in replacement: the Java
  package names (`net.jpountz.*`) and API are unchanged", and that a consumer pulling `org.lz4:lz4-java` should "exclude
  it (or use Gradle's dependency substitution) and add this artifact instead". The original line's last real release is
  `1.8.0` (`1.8.1` on that coordinate is a relocation POM to the fork); the fork's is `1.12.0`. The CVE is
  GHSA-vqf4-7m7x-wgfc, an out-of-bounds memory access. The substitution shipped in `e28440c8` (2025-12-08).
- **Elasticsearch client.** `co.elastic.clients:elasticsearch-java` is declared in `showcase-projection-service`,
  `showcase-query-service`, and `showcase-query-client`'s `componentTest`, and no project source imports
  `co.elastic.clients`. It is **not** supplied transitively: `spring-data-opensearch`'s POM excludes
  `co.elastic.clients:*` (and `org.elasticsearch.client:*`) from the `spring-data-elasticsearch` dependency it pulls,
  while Spring Data Elasticsearch's `client.elc` classes do reference Elastic's client, and Spring Boot's
  `ElasticsearchDataAutoConfiguration` conditions on `client.elc.ElasticsearchTemplate`, so the context loads them at
  startup. A probe removed all three declarations and ran the three suites: every case failed during application-context
  startup with `NoClassDefFoundError: co/elastic/clients/ApiClient`; the declarations were then reverted. The catalog
  pins the coordinate at `9.5.4`, where Spring Boot's BOM manages `8.18.8`.
- **ADR-0013.** `showcase-web-ui/src/shared/` now holds `tracing.ts` and `telemetry.ts` in addition to `api`, `format`,
  and `retry`; the layer's defining property (no domain knowledge, imports nothing above) is unchanged.

## Goals / Non-Goals

**Goals:**

- Record in a durable home the _why_ behind each unrecorded structural choice, at the granularity a future reader
  removing or reviewing it needs.
- Retire the two parked `docs/ideas.md` entries the change implements.

**Non-Goals:**

- Changing the build: the LZ4 substitution and the Elasticsearch client declaration both stay. The probe proved the
  latter load-bearing; the former is a security fix.
- Removing the `lz4-java` catalog entry or touching `.snyk`: the substitution keeps the vulnerable coordinate from
  resolving, so no suppression is owed.
- Re-opening the deprecated low-level `RestClient` concern (ADR-0012's Consequences park it separately).

## Decisions

**Record the LZ4 substitution in a new retrospective ADR rather than `AGENTS.md`.** It is a structural, supply-chain
decision that applies repo-wide; `AGENTS.md` records only the mechanism today, and the ADR bullet's own doctrine is that
ADRs carry the _why_ behind structural choices. The ADR is dated to the shipping commit (`e28440c8`, 2025-12-08) with a
Context line stating it was recorded retrospectively, per the ADR convention.

**Record the Elasticsearch client's role in ADR-0012, not a new ADR.** ADR-0012 owns the read side's client graph and
its version-alignment half, and the advisory's own framing is that ADR-0012 "never mentions the coordinate". A clause
there keeps the read side's client story in one place, and its Consequences carry the probe's evidence — a warning that
a future cleanup of the "unused" coordinate breaks the read side.

**Refresh ADR-0013's parenthetical as the advisory recommends** rather than treating the stale list as a defect: the
layer's defining property is unchanged, so only the illustrative list needs the two new slices.

## Risks / Trade-offs

- The LZ4 ADR's rationale rests on a third party's README → the ADR cites the fork and the CVE by name, so a reader
  re-verifies at the source.
- A future `spring-data-opensearch` (or Spring Data Elasticsearch) bump may drop the ELC reference, making the
  Elasticsearch client declaration genuinely removable → ADR-0012's clause records the probe and its failure, so the
  re-check has a starting point.

## Migration Plan

None: a record correction, taking effect on the next read.
