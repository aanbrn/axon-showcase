# Proposal: Suppress Flyway major updates until the Spring Boot 4 migration

## Why

The `dependencyUpdates` report shows a Flyway major jump (`org.flywaydb:flyway-core` and
`org.flywaydb:flyway-database-postgresql` from `11.20.3` to `13.3.0`). Spring Boot 3.5 (the current baseline, per
ADR-0004) manages Flyway at `11.7.2`; even Spring Boot 4.0 manages Flyway `11.14.1`, and Flyway major 12 only appears
with Spring Boot 4.1 (managing `12.4.0`). Flyway 13 is managed by no Spring Boot version. Bumping Flyway to a new major
therefore belongs with the deferred Spring Boot 4 migration, not as a standalone dependency update.

## What Changes

- `config/dependency-updates/major-disabled.properties`: add `org.flywaydb` (group prefix) to the major-blocking list.
  This suppresses major-version updates for both `flyway-core` and `flyway-database-postgresql`; minor and patch
  updates within Flyway 11 remain reported.
- The major-blocking mechanism in `build-logic` is unchanged; this is an addition to the shipped opt-in list plus a
  spec-delta rationale.

## Capabilities

### New Capabilities

None. The major-blocking mechanism already exists (see `showcase/quality/dependency-management`); this change only
adds a coordinate to the opt-in suppression list.

### Modified Capabilities

- `showcase/quality/dependency-management`: the shipped major-disabled list gains `org.flywaydb`; the requirement gains
  the rationale for why this coordinate's major updates are suppressed (Flyway majors belong with the deferred Spring
  Boot 4 migration, per ADR-0004).

## Impact

- **Code**: `config/dependency-updates/major-disabled.properties` (one added line).
- **Specs**: `openspec/specs/showcase/quality/dependency-management/spec.md` (modified via delta, synced on archive).
- **Build**: `./gradlew dependencyUpdates` output stops listing the Flyway major jump; same-major (11.x) updates stay
  reported.
- **Tests**: no test changes; verification runs `dependencyUpdates` and confirms the Flyway major row is gone while
  `org.axonframework`/`org.springframework` behavior is unaffected.