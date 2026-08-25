## 1. Remove the client e2e suites

- [x] 1.1 Delete `showcase-command-client/src/e2eTest` and remove the `e2eTest` suite (and its e2e-only dependencies,
      `mustRunAfter`, and `bootBuildImage` reference) from `showcase-command-client/build.gradle.kts`, remove the
      now-empty `src/e2eTest` directory tree, and verify the module builds and `componentTest` still passes
- [x] 1.2 Delete `showcase-query-client/src/e2eTest` and remove the `e2eTest` suite (and its e2e-only dependencies)
      from `showcase-query-client/build.gradle.kts`, remove the now-empty `src/e2eTest` directory tree, and verify the
      module builds and `componentTest` still passes

## 2. Extend the gateway e2e

- [x] 2.1 Drop the `mustRunAfter(":showcase-command-client:e2eTest")` / `mustRunAfter(":showcase-query-client:e2eTest")`
      references and add the BlockHound jvmArgs to the gateway `e2eTest` task in `showcase-api-gateway/build.gradle.kts`
- [x] 2.2 The gateway e2e's `FetchingListTests` already covered the title/status/multi-status filtering scenarios on
      real pipeline data (only the query service's sort-by-showcaseId-desc order applies, not start-time); tightened
      the status-filtered tests to assert the exact result size (matching the one-per-status seed) and added an
      explicit showcaseId-descending order assertion to the no-filtering list test, preserving the query-client e2e's
      exact-set and ordering coverage; added `BlockHound.install()` to the gateway e2e `@BeforeAll` (with the
      `reactor-blockhound` suite dependency), and verified the e2e runs green
- [x] 2.3 Remove the redundant `@SuppressWarnings("resource")` from the `dbEvents` `PostgreSQLContainer` field in
      `ShowcaseApiGatewayE2E` (the non-generic container triggers no `AutoCloseableResource` warning), leaving the six
      required suppressions on the other `@Container` fields untouched, and verify the IDE reports no resource
      warnings

## 3. Preserve the isolated wire-contract check

- [x] 3.1 Add a serializer round-trip test in `showcase-command-api/src/test/java` round-tripping the four commands and
      `ShowcaseCommandErrorDetails` through an Axon `JacksonSerializer`, and verify
      `./gradlew :showcase-command-api:test` passes

## 4. Refresh the docs

- [x] 4.1 Update AGENTS.md (test-tier description, `mustRunAfter` ordering, BlockHound-jvmArgs gotcha) and README to
      reflect the single gateway e2e suite

## 5. Verify the change

- [x] 5.1 Run `./gradlew build -x e2eTest -PskipITs` and confirm the only remaining `e2eTest` suite is the gateway's,
      with no `:showcase-command-client:e2eTest` / `:showcase-query-client:e2eTest` tasks
- [x] 5.2 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors