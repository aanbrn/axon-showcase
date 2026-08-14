## 1. Rename the Gradle suite

- [ ] 1.1 In `showcase-api-gateway/build.gradle.kts`, rename the test suite from `register<JvmTestSuite>("integrationTest")`
      to `register<JvmTestSuite>("e2eTest")`, preserving its dependencies block, `shouldRunAfter(componentTest)`,
      `mustRunAfter(":showcase-command-client:integrationTest")`, `mustRunAfter(":showcase-query-client:integrationTest")`,
      the `dependsOn` of the four `bootBuildImage` tasks, and the `disable-axoniq-console-message` system property

## 2. Relocate and rename the test class

- [ ] 2.1 Move `showcase-api-gateway/src/integrationTest/java/showcase/api/ShowcaseApiGatewayIT.java` to
      `showcase-api-gateway/src/e2eTest/java/showcase/api/ShowcaseApiGatewayE2E.java`, renaming the class to
      `ShowcaseApiGatewayE2E` (no other code changes)

## 3. Update AGENTS.md test-tier documentation

- [ ] 3.1 Update the "Check runs" line to include `e2eTest` after `integrationTest`
- [ ] 3.2 Update the "Test suite order matters" paragraph to include `e2eTest` in the chain
- [ ] 3.3 Add an E2E tier entry to the Test Tiers block (`src/e2eTest/java`, suffix `E2E`, whole-system verification)
- [ ] 3.4 Add the suffix bullet "E2E test classes use the suffix `E2E`"
- [ ] 3.5 Reword the gotcha so `showcase-api-gateway` `e2eTest` must run after the client integration tests

## 4. Validate

- [ ] 4.1 Run `openspec validate --changes "rename-showcase-api-gateway-it-to-e2e"` and confirm the change passes
- [ ] 4.2 Run `./gradlew :showcase-api-gateway:tasks` (or `check`) and confirm the `e2eTest` suite is registered and
      wired into `check`

## 5. Archive

- [ ] 5.1 Run `openspec archive "rename-showcase-api-gateway-it-to-e2e"` to archive the change