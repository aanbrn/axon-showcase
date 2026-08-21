# Proposal: Suppress springdoc major updates until the Spring Boot 4 migration

## Why

The `dependencyUpdates` report shows a major jump for `org.springdoc:springdoc-openapi-starter-webflux-ui` from
`2.8.17` to `3.1.0`. springdoc 3.x (both 3.0.3 and 3.1.0) is built against `spring-boot-starter-parent` 4.x and pulls
the SB4-modularized auto-configuration artifacts (`spring-boot-webflux`, `spring-boot-web-server`, `spring-boot-health`),
which is the deferred Spring Boot 4 stack (per ADR-0004). The jump therefore belongs with the SB4 migration, not as a
standalone update.

## What Changes

- `config/dependency-updates/major-disabled.properties`: add the exact coordinate
  `org.springdoc:springdoc-openapi-starter-webflux-ui` to the major-blocking list. This suppresses major-version
  updates for the springdoc webflux-ui starter; minor and patch updates within 2.x remain reported.
- The exact-coordinate form is used because the catalog owns only the webflux-ui starter; the webmvc variants are not
  consumed by any module.
- The major-blocking mechanism in `build-logic` is unchanged; this is an addition to the shipped opt-in list plus a
  spec-delta rationale.

## Capabilities

### New Capabilities

None. The major-blocking mechanism already exists (see `showcase/quality/dependency-management`); this change only
adds a coordinate to the opt-in suppression list.

### Modified Capabilities

- `showcase/quality/dependency-management`: the shipped major-disabled list gains the springdoc webflux-ui starter
  exact coordinate; the requirement gains the rationale for why its major updates are suppressed (it belongs with the
  deferred Spring Boot 4 migration, per ADR-0004).

## Impact

- **Code**: `config/dependency-updates/major-disabled.properties` (one added line).
- **Specs**: `openspec/specs/showcase/quality/dependency-management/spec.md` (modified via delta, synced on archive).
- **Build**: `./gradlew dependencyUpdates` output stops listing the springdoc major jump; same-major (2.x) updates stay
  reported.
- **Tests**: no test changes; verification runs `dependencyUpdates` and confirms the springdoc major row is gone while
  same-major behavior holds.