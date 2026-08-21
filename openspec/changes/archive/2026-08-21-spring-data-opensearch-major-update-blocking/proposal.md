# Proposal: Suppress spring-data-opensearch major updates until the Spring Boot 4 migration

## Why

The `dependencyUpdates` report shows a major jump for the `org.opensearch.client:spring-data-opensearch` family
(`-starter`, `-testcontainers`) from `2.0.6` to `3.1.1`. spring-data-opensearch 3.x is built on the Spring Data
2025.1 train (spring-data-elasticsearch 6.x, spring-data-commons 4.x) and Spring Framework 7, which is the deferred
Spring Boot 4 stack (per ADR-0004). The jump therefore belongs with the SB4 migration, not as a standalone update.

## What Changes

- `config/dependency-updates/major-disabled.properties`: add three exact coordinates — `org.opensearch.client:
  spring-data-opensearch`, `org.opensearch.client:spring-data-opensearch-starter`, and `org.opensearch.client:
  spring-data-opensearch-testcontainers` — to the major-blocking list. These suppress major-version updates for the
  spring-data-opensearch family; minor and patch updates within 2.x remain reported.
- The exact-coordinate form (not a group prefix) is used deliberately: `opensearch-java` and `opensearch-rest-client`
  share the `org.opensearch.client` group but are independent transport clients already on major 3, so their majors
  must keep being reported.
- The major-blocking mechanism in `build-logic` is unchanged; this is an addition to the shipped opt-in list plus a
  spec-delta rationale.

## Capabilities

### New Capabilities

None. The major-blocking mechanism already exists (see `showcase/quality/dependency-management`); this change only
adds coordinates to the opt-in suppression list.

### Modified Capabilities

- `showcase/quality/dependency-management`: the shipped major-disabled list gains the spring-data-opensearch exact
  coordinates; the requirement gains the rationale for why their major updates are suppressed (they belong with the
  deferred Spring Boot 4 migration, per ADR-0004).

## Impact

- **Code**: `config/dependency-updates/major-disabled.properties` (three added lines).
- **Specs**: `openspec/specs/showcase/quality/dependency-management/spec.md` (modified via delta, synced on archive).
- **Build**: `./gradlew dependencyUpdates` output stops listing the spring-data-opensearch major jump; same-major
  (2.x) updates stay reported, and `opensearch-java`/`opensearch-rest-client` majors remain visible.
- **Tests**: no test changes; verification runs `dependencyUpdates` and confirms the spring-data-opensearch major row
  is gone while `opensearch-java` majors are still reported.