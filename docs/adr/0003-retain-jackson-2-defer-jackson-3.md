# ADR-0003: Retain Jackson 2, defer Jackson 3

Date: 2026-08-18

Status: Accepted

## Context

Spring Boot 4 defaults to Jackson 3 (the `tools.jackson` package / `jackson-core` 3.x). The project's serialization
backend — Axon Framework 4.13 and the OpenSearch Java client — still target Jackson 2, and the project pins
`jackson2-bom` with the Blackbird module for efficient serialization. Migrating to Jackson 3 would couple two risky
upgrades (a new Spring Boot major and a new serialization backend) at once, with uncertain support in Axon and
OpenSearch.

## Decision

Retain Jackson 2 as the serialization backend. When moving to Spring Boot 4, use the framework's Jackson 2 bridge to
keep the existing `jackson2-bom`, `jackson2-databind`, `jackson2-jsr310`, and `jackson2-module-blackbird`
configuration. A Jackson 3 migration is a separate, later change, undertaken only once Axon and the OpenSearch client
support it.

## Consequences

- The serialized event format stays stable across the Spring Boot upgrade, protecting event-store replay and
  cross-service wire compatibility.
- The project carries the Jackson 2 bridge on Spring Boot 4, which may emit deprecation warnings until a future
  Jackson 3 migration.
- A dedicated effort (new change + ADR update) is required before adopting Jackson 3.
