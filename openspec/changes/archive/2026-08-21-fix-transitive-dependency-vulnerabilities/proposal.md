# Proposal: Fix transitive dependency vulnerabilities

## Why

`./gradlew` builds ship three transitive dependencies that Snyk flags as vulnerable across four projects
(`showcase-projection-model`, `showcase-projection-service`, `showcase-query-client`, `showcase-query-service`):
`tools.jackson.core:jackson-core:3.1.0` and `tools.jackson.core:jackson-databind:3.1.0` (pulled in by
`elasticsearch-java:9.5.1`) and `org.apache.httpcomponents.client5:httpclient5:5.6.1` (pulled in by
`opensearch-rest-client:3.8.0`). All introducing clients are already at their latest versions, so the only fix is to
constrain the vulnerable transitives at the `platform` BOM level.

## What Changes

- `gradle/libs.versions.toml`: add a `jackson3-bom` version (`3.1.6`) and an `httpclient5` version (`5.6.3`);
  register the `tools.jackson:jackson-bom` platform and `httpclient5` library.
- `platform/build.gradle.kts`: import `api(platform(libs.jackson3.bom))` alongside `jackson2-bom`, and add
  `api(libs.httpclient5)` to the `constraints` block.
- Resolved effect: Jackson 3 modules align to `3.1.6`, `httpclient5` aligns to `5.6.3`, clearing the Snyk findings in
  the four affected projects.

## Capabilities

### New Capabilities

- `showcase/quality/dependency-security`: the platform must not resolve known-vulnerable transitive dependencies —
  vulnerable transitives are constrained to patched versions so dependency scans report clean.

### Modified Capabilities

None. No functional behavior of the services changes; only resolved dependency versions.

## Impact

- **Code**: `gradle/libs.versions.toml`, `platform/build.gradle.kts` only.
- **Modules**: resolved classpaths of `showcase-projection-model`, `showcase-projection-service`,
  `showcase-query-client`, `showcase-query-service` (all consume `:platform` via `java-conventions`).
- **Dependencies**: `tools.jackson.core` bumps `3.1.0` → `3.1.6` (patch-level); `httpclient5` bumps `5.6.1` → `5.6.3`
  (patch-level) and `5.6` → `5.6.3` in the service classpaths.
- **Build/tests**: dependency report should resolve the patched versions; query/projection integration tests (real
  OpenSearch via Testcontainers) and client integration tests exercise the bumped Jackson 3 runtime. `snyk test
  --all-sub-projects` should report no vulnerable paths in the four projects.