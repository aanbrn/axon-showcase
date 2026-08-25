# Proposal: Consolidate the client e2e tests into the API gateway e2e

## Why

The gateway is the only consumer of the command and query clients, and `ShowcaseApiGatewayE2E` already exercises both
clients transitively through the full pipeline (HTTP → gateway → clients → services → Kafka → projection →
OpenSearch).
The separate client e2e tests are therefore largely redundant: `ShowcaseCommandClientE2E`'s command lifecycle and error
round-trips are subsumed by the gateway e2e, and `ShowcaseQueryClientE2E`'s filtering coverage already exists in the
gateway e2e's `FetchingListTests` (title, status, multi-status, size, afterId on real pipeline data). Consolidating to
one e2e suite cuts two container-booting suites and removes the `mustRunAfter` ordering. The isolated wire-contract
coverage the command-client e2e owned is preserved by a cheap serializer round-trip test.

## What Changes

- **Remove `ShowcaseCommandClientE2E`** and its `e2eTest` suite from `showcase-command-client` (files, resources, and
  `build.gradle.kts` wiring — including the `mustRunAfter(":showcase-command-service:integrationTest")` and the
  `bootBuildImage` dependency).
- **Remove `ShowcaseQueryClientE2E`** and its `e2eTest` suite from `showcase-query-client`.
- **Extend `ShowcaseApiGatewayE2E`** with **BlockHound** (install in `@BeforeAll` plus the `reactor-blockhound` suite
  dependency and the required jvmArgs) to keep the real-transport non-blocking check; its `FetchingListTests` already
  cover the `title`/`status`/multi-status/size/afterId filtering scenarios on real pipeline data.
- **Add a serializer round-trip unit test in `showcase-command-api`** covering the shared commands and
  `ShowcaseCommandErrorDetails` through a real Axon `JacksonSerializer` (the serializer `messages: jackson` produces),
  preserving the isolated wire-contract coverage the command-client e2e used to provide.
- **Docs**: update AGENTS.md (test-tier description, `mustRunAfter` ordering, BlockHound-jvmArgs gotcha) and README to
  reflect the single gateway e2e suite.
- No production code changes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. Test-tier consolidation and build-config reorganization with no externally observable
behavior change.

## Impact

- **Code (tests/build only)**: delete `showcase-command-client/src/e2eTest` and `showcase-query-client/src/e2eTest`;
  rewire `showcase-command-client`, `showcase-query-client`, and `showcase-api-gateway` `build.gradle.kts` (remove
  client e2e suites + gateway `mustRunAfter`; add BlockHound jvmArgs to the gateway e2e); extend
  `ShowcaseApiGatewayE2E`; add a serializer round-trip test in `showcase-command-api`.
- **Docs**: `AGENTS.md`, `README.md`.
- **Build**: one `e2eTest` suite remains (gateway); it still depends on all four service `bootBuildImage`s.
- **Tests**: verified via `./gradlew :showcase-command-api:test`, `:showcase-api-gateway:e2eTest`, and the full
  `./gradlew build -x e2eTest -PskipITs`.