## 1. Move the test and resources to the component tier

- [x] 1.1 Move `ShowcaseQueryClientIT.java` to `src/componentTest/java/showcase/query/ShowcaseQueryClientCT.java`,
      rename the class and `@DisplayName` to the component form, and verify it compiles in the componentTest suite
- [x] 1.2 Move the four `application*.yml` profile resources from `src/integrationTest/resources` to
      `src/componentTest/resources`, and verify the moved CT's `@ActiveProfiles` scenarios still resolve them
- [x] 1.3 Remove the now-empty `src/integrationTest` directory tree, and verify it is gone from the module

## 2. Rewire the test suites

- [x] 2.1 Add `implementation(project(":showcase-query-proto"))` and the BlockHound jvmArgs to the `componentTest`
      suite, remove the now-empty `integrationTest` suite, and point the `e2eTest` suite's `shouldRunAfter` at
      `componentTest`, and verify `./gradlew :showcase-query-client:componentTest` passes
- [x] 2.2 Re-add `@file:Suppress("UnstableApiUsage")` to the query-client build script (its `testing { }` DSL is
      `@Incubating` and triggers the IDE inspection even though the Gradle compiler does not error), and verify the IDE
      warnings clear

## 3. Refresh the docs

- [x] 3.1 Update the AGENTS.md BlockHound-jvmArgs gotcha to name the query-client `componentTest` (not
      `integrationTest`) and cite `ShowcaseQueryClientCT` in the component-tier examples

## 4. Verify the change

- [x] 4.1 Run `./gradlew :showcase-query-client:check` and the full `./gradlew build -x e2eTest -PskipITs` and confirm
      the query-client has no `integrationTest` suite left
- [x] 4.2 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors