# ADR-0001: Record architecture decisions with ADRs

Date: 2026-08-18

Status: Accepted

## Context

The repository captures behavior and change plans through OpenSpec (specs, proposals, designs, tasks), but
cross-cutting architecture decisions and their rationale have no permanent home. Decisions such as "retain Jackson 2
over Jackson 3", "defer the Spring Boot 4 migration", and "Java `@ConfigurationProperties` defaults are authoritative"
were scattered across change designs and commit history, invisible to future contributors.

## Decision

Record architecture decisions as lightweight, numbered Architecture Decision Records in `docs/adr/` using the Nygard
format (Status / Context / Decision / Consequences). Use OpenSpec for behavior and change planning; use ADRs for the
reasons behind structural and cross-cutting choices. Cross-cutting decisions made during a change are captured as an
ADR when the change lands.

## Consequences

- Decisions become discoverable and auditable in one place.
- A small documentation overhead: an ADR is written when a decision is made, and its Consequences may need updating if
  trade-offs change.
- Some decisions may be recorded in both an ADR (rationale) and an OpenSpec design (plan) — the ADR is the durable
  record, the design is the plan.
