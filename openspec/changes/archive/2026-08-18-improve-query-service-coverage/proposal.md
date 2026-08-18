# Proposal: Improve query-service coverage

## Why

`showcase-query-service` reported 71% coverage, below the 0.80 gate baseline, so its coverage gate was disabled. The
uncovered code was the query-service's runtime wiring: the entity-to-DTO mapper, the query handler, the
`ShowcaseQueryApplication` beans (index initializer, health indicator, query-bus customizer), and the exit-after-index-
initialization path.

## What Changes

- Add tests covering the query-service's previously uncovered logic:
  - `ShowcaseMapperTests` (unit) — `entityToDto` / `dtoToEntity`.
  - `ShowcaseQueryHandlerCT` (component) — the list query and by-ID query found/not-found paths, composing the real
    `ShowcaseMapper` with the OpenSearch template faked.
  - `ShowcaseQueryApplicationIT` (integration) — the OpenSearch health indicator (via `/actuator/health`), the index
    initializer creating the showcase index on startup, the disabled index-initialization case, and the
    exit-after-index-initialization path. The controller needs no new tests — the existing `ShowcaseQueryControllerIT`
    already covers it over HTTP.
- Add a testable `ApplicationExitHandler` seam to `ShowcaseQueryApplication` (default: close context + `System.exit`)
  so the exit-on-startup path can be tested without terminating the test JVM; recorded as ADR-0005.
- Extend the `componentTest` suite dependencies (webflux, actuator, spring-data-opensearch starter, opensearch client,
  spring-tx, `showcase-projection-model`).
- Re-enable the module's coverage gate (remove `extra["coverage.gate.enabled"] = false`).
- Verify `./gradlew :showcase-query-service:jacocoTestReport` shows >= 80% (87%) and the gate passes.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — test-only plus a testability seam; no spec-level behavior change, so `skip_specs: true` is set in
`.openspec.yaml`)

## Impact

- New tests under `showcase-query-service/src/{test,componentTest,integrationTest}`; the gate opt-out removed from
  `showcase-query-service/build.gradle.kts`; component-test dependencies added.
- `showcase-query-service/src/main/java/showcase/query/ShowcaseQueryApplication.java` — the `ApplicationExitHandler`
  seam (default behavior unchanged).
- New ADR `docs/adr/0005-testable-exit-on-startup.md`.
