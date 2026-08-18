# Proposal: Fix testcontainers dependency names

## Why

The version catalog pins `testcontainers-bom` to 2.0.5 but references the testcontainers modules under their old 1.x
artifact names (`org.testcontainers:junit-jupiter`, `org.testcontainers:postgresql`, `org.testcontainers:kafka`). Those
artifacts no longer exist at 2.x, so the modules resolve to **1.21.4** (via Spring Boot's managed testcontainers) while
`testcontainers-core` resolves to **2.0.5** — a mixed-version classpath that happens to work today but is fragile and
breaks when the Spring Boot-managed testcontainers version changes (as seen when trying Spring Boot 4).

## What Changes

- In `gradle/libs.versions.toml`, rename the three testcontainers library aliases to the 2.x artifact names, keeping the
  same aliases and the `testcontainers-bom` 2.0.5 pin:
  - `testcontainers-junit-jupiter`: `org.testcontainers:junit-jupiter` → `org.testcontainers:testcontainers-junit-jupiter`
  - `testcontainers-postgresql`: `org.testcontainers:postgresql` → `org.testcontainers:testcontainers-postgresql`
  - `testcontainers-kafka`: `org.testcontainers:kafka` → `org.testcontainers:testcontainers-kafka`
- All consuming modules keep their existing aliases, so no module build file changes.
- No functional or API change to application code; the integration/e2e test infrastructure now resolves a single,
  consistent testcontainers 2.0.5 version set.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — this is a test-infrastructure dependency-coordinate fix with no spec-level behavior change; `skip_specs: true`
is set in `.openspec.yaml`)

## Impact

- `gradle/libs.versions.toml` — three testcontainers library `name` fields.
- Consuming modules (`showcase-command-service`, `showcase-query-service`, `showcase-projection-service`,
  `showcase-query-client`, `showcase-api-gateway` e2e) — now resolve the testcontainers modules at 2.0.5 instead of a
  mixed 1.21.4/2.0.5 set; any 2.x API differences surface in the affected integration/e2e tests.
- No dependency version bump (BOM stays 2.0.5), no application code change.
