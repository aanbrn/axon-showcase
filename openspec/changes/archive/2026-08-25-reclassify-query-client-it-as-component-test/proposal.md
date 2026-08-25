# Proposal: Reclassify the query-client WireMock integration test as a component test

## Why

`ShowcaseQueryClientIT` is a full `@SpringBootTest` + `@EnableWireMock` suite (real serializers, resilience profiles,
BlockHound) with no Testcontainers — which the repo's own tier taxonomy defines as a **component** test ("a Spring
context with WireMock — but external infrastructure is never started"). It sits in `src/integrationTest` with suffix
`IT`, the lone outlier: the command-client's parallel WireMock-free-but-mocked-bus test is already a CT
(`ShowcaseCommandClientCT`), and the taxonomy would otherwise be contradicted by its own flagship example.

## What Changes

- Move `ShowcaseQueryClientIT` from `src/integrationTest` to `src/componentTest`, renaming the class and its
  `@DisplayName` to the component form (`ShowcaseQueryClientCT`, "Showcase query client component tests").
- Move the four profile resources (`application.yml`, `application-retry.yml`, `application-timelimiter.yml`,
  `application-circuitbreaker.yml`) from `src/integrationTest/resources` to `src/componentTest/resources`.
- In `showcase-query-client/build.gradle.kts`: add `implementation(project(":showcase-query-proto"))` and the BlockHound
  jvmArgs to the `componentTest` suite; remove the now-empty `integrationTest` suite and point the `e2eTest` suite's
  `shouldRunAfter(integrationTest)` at `componentTest`.
- Docs: update the AGENTS.md BlockHound-jvmArgs gotcha to name the query-client `componentTest` (not `integrationTest`)
  as the suite that needs the flags; optionally cite `ShowcaseQueryClientCT` in the component-tier examples.
- No production code changes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. This is a test-tier reclassification and build-config reorganization with no externally
observable behavior change.

## Impact

- **Code (tests/build only)**: `showcase-query-client/src/{integrationTest → componentTest}/` file moves and the class
  rename; `showcase-query-client/build.gradle.kts` suite rewiring.
- **Docs**: `AGENTS.md` (BlockHound gotcha; component-tier example). README's tier table becomes accurate without edits.
- **Build**: the query-client `check` now runs `test` + `componentTest` (no `integrationTest`); `-PskipITs` no longer
  affects it. Other modules' suites are untouched.
- **Tests**: verified via `./gradlew :showcase-query-client:componentTest` and the module `check`.