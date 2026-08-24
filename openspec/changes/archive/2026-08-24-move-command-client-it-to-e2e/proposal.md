# Proposal: Move the command-client integration test to the e2e suite

## Why

`ShowcaseCommandClientIT` is structurally an end-to-end test, not an integration test. It boots the **real**
command-service container (`aanbrn/axon-showcase-command-service`) plus real PostgreSQL and Kafka, and dispatches
commands to it over the distributed command bus (JGroups) — every collaborator real. That is exactly the pattern of
`ShowcaseQueryClientE2E` (real query-service + OpenSearch), and the opposite of `ShowcaseQueryClientIT`, which fakes
the remote with WireMock.

The client test taxonomy is inconsistent: the two clients place the identical "client against the real deployed
service" test in different suites (command-client: `integrationTest`; query-client: `e2eTest`). This change commits to
the collaborator-realism taxonomy (integration = faked remote; e2e = real deployed system with all-real
collaborators, transport-independent — HTTP for the gateway/query-service, the distributed command bus for the
command-service), moves the
command-client test to the e2e suite, and clarifies the AGENTS.md e2e definition to match.

## What Changes

- Move `ShowcaseCommandClientIT` to `showcase-command-client/src/e2eTest/java/showcase/command/ShowcaseCommandClientE2E`
  (class and `@DisplayName` renamed to "end-to-end tests"), together with its Spring test config
  (`src/integrationTest/resources/application.yml` → `src/e2eTest/resources/application.yml`) — without it the suite
  runs with default Axon config and commands have no handler segment.
- Fix the fragile `awaitUntilClusterFormed` in the moved test: the command-service's "joined the cluster" log line is
  emitted at container startup, before `@ExtendWith(OutputCaptureExtension)` captures output, so the wait always
  timed out. Buffer the container log in a static `StringBuilder` via `withLogConsumer` and await that instead of
  `CapturedOutput`.
- `showcase-command-client/build.gradle.kts`: rename the `integrationTest` suite registration to `e2eTest`, carrying
  its configuration (BlockHound jvmArgs, `shouldRunAfter(componentTest)`,
  `mustRunAfter(":showcase-command-service:integrationTest")`, `dependsOn(":showcase-command-service:bootBuildImage")`).
- `showcase-api-gateway/build.gradle.kts`: `mustRunAfter(":showcase-command-client:integrationTest")` becomes
  `mustRunAfter(":showcase-command-client:e2eTest")`.
- `AGENTS.md`: clarify the e2e definition — a real deployed system exercised against all-real collaborators,
  transport-independent; the client e2e tests boot one real service plus its infra (over HTTP for the
  query-service, over the distributed command bus for the command-service), while the gateway e2e boots the full
  four-service pipeline.
  Update the ordering, `@DirtiesContext`, and BlockHound notes that reference the client suites.
- `README.md`: update the Testing tier table / ordering notes accordingly.

The command-client keeps its WireMock-style resilience coverage at the **component** tier
(`ShowcaseCommandClientCT`) — no integration-tier test is lost; the tier simply differs from the query-client's
(which places its WireMock scenarios at the integration tier).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. Test-organization change with no behavioral effect. `skip_specs: true`.

## Impact

- **Code**: `ShowcaseCommandClientIT.java` → `ShowcaseCommandClientE2E.java` (moved + renamed),
  `showcase-command-client/build.gradle.kts`, `showcase-api-gateway/build.gradle.kts`.
- **Docs**: `AGENTS.md` (e2e definition + suite references), `README.md` (testing notes).
- **Build**: the command-client `e2eTest` suite replaces the `integrationTest` suite; ordering guarantees preserved.
- **Tests**: the moved test class; existing suites unaffected.