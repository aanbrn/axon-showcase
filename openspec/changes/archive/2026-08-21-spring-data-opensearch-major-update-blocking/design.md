# Design: Suppress spring-data-opensearch major updates until the Spring Boot 4 migration

## Context

See proposal.md — Why. The mechanism (catalog-ownership filter + opt-in major-disabled list) already exists and is
captured in `showcase/quality/dependency-management`. This change adds three exact coordinates to the shipped list,
with a rationale, because spring-data-opensearch 3.x is built on the Spring Data 2025.1 train (spring-data-elasticsearch
6.x, spring-data-commons 4.x) and Spring Framework 7 — the deferred Spring Boot 4 stack per ADR-0004.

## Goals / Non-Goals

**Goals:**
- Ship the spring-data-opensearch exact coordinates in `config/dependency-updates/major-disabled.properties` with a
  pointer to the recorded rationale.
- Suppress the spring-data-opensearch 2.x → 3.x major jump from `./gradlew dependencyUpdates` while keeping 2.x
  minor/patch updates visible.
- Keep `opensearch-java` and `opensearch-rest-client` majors reported (they are independent transport clients).

**Non-Goals:**
- Migrating to spring-data-opensearch 3.x or Spring Boot 4 — tracked upstream (ADR-0004), out of scope.
- Changing the `rejectVersionIf` mechanism or the ownership filter.

## Decisions

**D1 — Use exact `group:module` coordinates, not the `org.opensearch.client` group prefix.**
`matchesDisabled` supports both forms: a bare group matches the group and its sub-groups (dot-boundary aware); an
entry containing `:` matches exactly one coordinate. The spring-data-opensearch family shares `2.0.6` via a single
catalog `version.ref` and is the only part of `org.opensearch.client` locked to the SB4 train. `opensearch-java`
(3.9.0) and `opensearch-rest-client` (3.8.0) are already on major 3 and are transport clients that advance
independently — a group prefix would wrongly hide their future majors. Rejected alternative: group prefix
`org.opensearch.client` — over-blocks the independent transport clients.
This diverges from the JGroups precedent on purpose: there both artifacts shared the ecosystem lock, so one prefix was
precise; here the lock is confined to the spring-data family, so three exact entries are the precise form.

**D2 — Record the rationale in the spec, with only a short pointer comment in the properties file.**
Same convention as the JGroups/Flyway entries (see design D3 of `jgroups-major-update-blocking`): the substantive
"why" lives in the dependency-management spec; the file gains a one-line pointer per coordinate. Rejected alternative:
embedding the full rationale as multi-line comments in the properties file — duplicates content that will drift.

## Risks / Trade-offs

- [A future spring-data-opensearch 3.x release supports Spring Boot 3.5, and the entry silently hides an now-actionable
  upgrade.] → Mitigation: the spec states the suppression is tied to the SB4 migration; re-evaluate when SB4 lands or
  the library's compatibility matrix changes.
- [Suppressing majors could mask a spring-data-opensearch 2.x security fix.] → Mitigation: minor/patch updates remain
  reported, so 2.x security fixes still surface; a 3.x-only fix would be flagged by the dependency security scan
  (`snyk`), which is independent of the version report.
- [New spring-data-opensearch sub-modules added later won't be covered by these exact entries.] → Acceptable: the
  starter and testcontainers are the consumed artifacts today; a future module can be added to the list when it is
  adopted.