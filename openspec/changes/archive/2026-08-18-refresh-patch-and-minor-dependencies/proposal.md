# Proposal: Refresh patch and minor dependencies

## Why

A `dependencyUpdates` report shows the reference stack is drifting from current versions. The meaningful updates fall
into two tiers: safe patch/minor bumps that can land now without changing the Spring Boot 3.5 baseline, and major
bumps (Spring Boot 4, Axon 5, jgroups 5, flyway 13, junit 6, springdoc 3, spring-data-opensearch 3) that are coupled
major migrations — Spring Boot 4 is already deferred in ADR-0004. This change lands the safe tier to keep the stack
current and close the patch gap.

## What Changes

- Bump the following versions in `gradle/libs.versions.toml`:
  - `logback` `1.6.2` → `1.6.3`
  - `jackson2-bom` `2.22.1` → `2.22.2`
  - `guava` `33.6.0-jre` → `33.7.0-jre`
  - `swagger-ui` `5.32.11` → `5.32.13`
  - `elasticsearch-client-java` `9.1.12` → `9.5.1`
- Verify the full build and test tiers still pass after the bumps.
- Deliberately NOT bumped here (major, deferred): Spring Boot/Framework/Data/Security (SB4 — see ADR-0004), Axon 5,
  jgroups 5, flyway 13, junit 6, springdoc 3, spring-data-opensearch 3, Gradle 9.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — version-only updates with no spec-level behavior change; `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- `gradle/libs.versions.toml` — five version entries bumped.
- Consuming modules resolve the updated versions; a full `./gradlew build` (plus integration/e2e tiers) verifies no
  behavioral or compile regression.
- No API, schema, or runtime behavior changes; the major migrations remain separate, tracked changes.
