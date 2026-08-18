# Proposal: Introduce Architecture Decision Records (ADRs)

## Why

The repo captures behavior in OpenSpec specs and change plans, but cross-cutting architectural decisions — and the
reasons behind them — have no home. Decisions such as "retain Jackson 2 over Jackson 3", "Spring Boot 4 migration is
deferred", and "Java `@ConfigurationProperties` defaults are authoritative" are currently scattered across change
designs and commit history. ADRs give them a permanent, discoverable record so future contributors understand *why* the
system is shaped the way it is, not just *what* it does.

## What Changes

- Add a `docs/adr/` directory holding lightweight, numbered Architecture Decision Records using the Nygard format
  (Status / Context / Decision / Consequences).
- Add a `docs/adr/README.md` explaining the format and acting as a template for new ADRs.
- Record an initial set of ADRs capturing decisions already made:
  - `0001-record-architecture-decisions.md` — the meta ADR: this repo records architecture decisions as ADRs.
  - `0002-configuration-property-defaults-owned-by-java.md` — Java `@ConfigurationProperties` defaults are the
    authoritative contract; `application.yml` and the Helm chart mirror them (the gateway cache-default alignment).
  - `0003-retain-jackson-2-defer-jackson-3.md` — keep Jackson 2 (Axon 4 / OpenSearch compatibility); Jackson 3 is a
    separate, deferred migration.
  - `0004-defer-spring-boot-4-migration.md` — the SB4 migration is planned but deferred (deep API changes: Flyway /
    JCache / Jackson-2 bridge / Actuator relocation); reopening is a dedicated change.
- Add a convention note to `AGENTS.md`: record cross-cutting architecture decisions as ADRs under `docs/adr/`.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — documentation-only; no spec-level behavior change, so `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- New `docs/adr/` directory: `README.md` plus four ADR files.
- `AGENTS.md` — a short convention bullet under **Conventions**.
- No application code, build, dependency, or runtime behavior changes.
