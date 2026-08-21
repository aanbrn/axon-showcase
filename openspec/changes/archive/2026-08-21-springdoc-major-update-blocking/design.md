# Design: Suppress springdoc major updates until the Spring Boot 4 migration

## Context

See proposal.md — Why. The mechanism (catalog-ownership filter + opt-in major-disabled list) already exists and is
captured in `showcase/quality/dependency-management`. This change adds one exact coordinate to the shipped list, with
a rationale, because springdoc 3.x (3.0.3 and 3.1.0) is built against `spring-boot-starter-parent` 4.x — the deferred
Spring Boot 4 stack per ADR-0004.

## Goals / Non-Goals

**Goals:**
- Ship `org.springdoc:springdoc-openapi-starter-webflux-ui` in `config/dependency-updates/major-disabled.properties`
  with a pointer to the recorded rationale.
- Suppress the springdoc 2.x → 3.x major jump from `./gradlew dependencyUpdates` while keeping 2.x minor/patch updates
  visible.

**Non-Goals:**
- Migrating to springdoc 3.x or Spring Boot 4 — tracked upstream (ADR-0004), out of scope.
- Changing the `rejectVersionIf` mechanism or the ownership filter.

## Decisions

**D1 — Use the exact coordinate, not a group prefix.**
`matchesDisabled` supports both forms; the catalog owns only `org.springdoc:springdoc-openapi-starter-webflux-ui` (the
webmvc variants are not consumed by any module), so one exact entry is the precise form. A bare `org.springdoc` prefix
would additionally cover webmvc-ui/webmvc-api starters the project does not use — over-blocking. This follows the
exact-coordinate precedent from the spring-data-opensearch change, where the lock was confined to specific artifacts.

**D2 — Record the rationale in the spec, with only a short pointer comment in the properties file.**
Same convention as the JGroups/Flyway/spring-data-opensearch entries: the substantive "why" lives in the
dependency-management spec; the file gains a one-line pointer. Rejected alternative: embedding the full rationale as a
multi-line comment in the properties file — duplicates content that will drift.

## Risks / Trade-offs

- [A future springdoc 2.x release becomes the last SB3-compatible line and stops being patched, and the suppression
  hides an important 2.x update.] → Mitigation: minor/patch updates remain reported, so 2.x fixes still surface; the
  suppression only hides the 3.x jump, which cannot run on SB3.
- [springdoc 3.x later adds an SB3-compatible variant, and the entry silently hides an now-actionable upgrade.] →
  Mitigation: the spec states the suppression is tied to the SB4 migration; re-evaluate when SB4 lands or the
  library's compatibility matrix changes.
- [A security fix exists only in springdoc 3.x.] → Mitigation: flagged by the dependency security scan (`snyk`), which
  is independent of the version report.