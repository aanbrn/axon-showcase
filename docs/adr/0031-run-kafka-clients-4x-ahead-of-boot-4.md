# ADR-0031: Run `kafka-clients` 4.x ahead of the Spring Boot 4 migration

Date: 2026-11-09

Status: Accepted

Revisit when either clause holds (a disjunction): the pinned Spring Boot line manages `kafka-clients` at `4.4.0` or
newer — external coordinate, `gradle/libs.versions.toml`'s `spring-boot` pin, read from that release's BOM
`kafka.version` — which makes the platform override redundant and retires it; or the overridden client line proves
incompatible with the pinned Spring Kafka or broker — internal trigger, shown by the integration or e2e suites
(`:showcase-command-service:integrationTest`, `:showcase-projection-service:integrationTest`,
`:showcase-api-gateway:integrationTest`, `:showcase-api-gateway:e2eTest`) failing on a `kafka-clients` version this
constraint selects — which reopens the decision rather than retiring it.

## Context

The scheduled `Dependency Security` run `42413633653` (2026-11-07) failed on two high-severity advisories in
`org.apache.kafka:kafka-clients@3.9.2`: `SNYK-JAVA-ORGAPACHEKAFKA-22150240` (CVE-2026-111073, a broker-side
decompression amplification in `ClientTelemetryUtils`) and `SNYK-JAVA-ORGAPACHEKAFKA-22150295` (CVE-2026-111115,
broker-side flexible-header parsing before authorization). Twelve of twenty sub-projects reported vulnerable paths, and
every finding was these two.

The coordinate arrives transitively — `axon-kafka 4.12.1` brings `cloudevents-kafka 4.0.1`, which declares
`kafka-clients` — and its version is owned by Spring Boot 3.5.16's BOM (`kafka.version=3.9.2`). The `3.9` line ends at
`3.9.2`; the advisories' fixed versions start at `4.2.2` (`4.3.2` on the `4.3` line). Spring Kafka 3.3.16, the line
Spring Boot 3.5 manages, compiles against `kafka-clients` 3.8.1, so `4.4.0` is ahead of the combination upstream tests.
The Spring Boot 4 migration, which would bring Spring Kafka 4 and a `kafka-clients` 4.x pairing, is deferred by
ADR-0004.

The repository's Snyk policy (`showcase/quality/dependency-security`) admits a suppression only for an advisory with no
available fix, and a fixed release exists here, so suppression is not the route.

## Decision

Constrain `org.apache.kafka:kafka-clients` to `4.4.0` through the `platform` project's `constraints` block, with the
version owned by the catalog, so every classpath the platform reaches resolves the patched client. Target `4.4.0` rather
than `4.2.2` because the `4.3` line re-opened the second advisory until `4.3.2`, so a later same-line bump would
re-surface it.

Alternatives considered:

- **Suppress both advisories in `.snyk` with an expiry.** Rejected: a fixed release exists and passes the suites here,
  and the policy admits only advisories with no available fix.
- **Migrate to Spring Boot 4 now.** Rejected: deferred by ADR-0004 and out of proportion to two advisories.
- **Declare the coordinate per module.** Rejected: the coordinate reaches several modules and the shared test fixture,
  and a per-module declaration would leave the next consumer unprotected.
- **Patch the broker image instead.** Rejected as the remedy for the scan, which reads the client, and unavailable
  today: the newest Bitnami `kafka` chart (`32.4.3`) packages Kafka `4.0.0`, still inside the affected ranges.

## Consequences

- The services run a client line ahead of Spring Kafka 3.3.16's supported pairing, so every `kafka-clients` bump the
  constraint governs is re-verified against a real broker rather than trusted.
- Verified on 2026-11-09 against `apache/kafka:3.9.0`: the build compiles, the three services' unit and component suites
  pass, their integration suites pass (36 command-service, 20 projection-service, 11 gateway), and the gateway e2e suite
  passes over the full command → Kafka → projection → query pipeline (29 tests).
- The client artifacts no longer carry either advisory. The deployed broker image remains inside both affected ranges;
  that exposure is a separate item, parked in `docs/ideas.md`, and this decision does not assess it.
- The override is temporary by design: the `Revisit when:` gate retires it once the pinned Spring Boot line manages a
  patched client.
