## 1. Move and rename the test class

- [x] 1.1 Move `ShowcaseCommandClientIT.java` to `src/e2eTest/java/showcase/command/ShowcaseCommandClientE2E.java`,
      renaming the class and its `@DisplayName` to the end-to-end naming
- [x] 1.2 Move the test's Spring config `src/integrationTest/resources/application.yml` to
      `src/e2eTest/resources/application.yml` (Jackson message serializer + JGroups distributed-bus settings), renaming
      the application name — without it the e2e suite ran with default Axon config and commands had no handler segment
- [x] 1.3 Fix the fragile `awaitUntilClusterFormed`: the cluster-formation log line is emitted at container startup,
      before `@ExtendWith(OutputCaptureExtension)` starts capturing, so the wait always timed out. Buffer the
      command-service log in a static `StringBuilder` (via `withLogConsumer`) and await that instead of `CapturedOutput`

## 2. Re-wire the command-client build

- [x] 2.1 Rename the `integrationTest` suite to `e2eTest` in `showcase-command-client/build.gradle.kts`, carrying the
      BlockHound jvmArgs, `shouldRunAfter(componentTest)`, `mustRunAfter(":showcase-command-service:integrationTest")`,
      and `dependsOn(":showcase-command-service:bootBuildImage")` configuration

## 3. Update the gateway ordering

- [x] 3.1 Change `mustRunAfter(":showcase-command-client:integrationTest")` to
      `mustRunAfter(":showcase-command-client:e2eTest")` in `showcase-api-gateway/build.gradle.kts`

## 4. Refresh the docs

- [x] 4.1 Update `AGENTS.md`: clarify the e2e definition (real deployed system against all-real collaborators,
      transport-independent — HTTP for gateway/query, distributed command bus for command; client e2e = one real
      service + infra, gateway e2e = full four-service pipeline) and update the ordering, `@DirtiesContext`, and
      BlockHound notes that reference the client suites
- [x] 4.2 Update `README.md` Testing notes accordingly

## 5. Verify the change artifacts

- [x] 5.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors
- [x] 5.2 Run the command-client `e2eTest` suite and confirm it passes with the new wiring