## 1. Provider modules

- [x] 1.1 Remove the `testFixturesApi(testFixtures(project(":showcase-command-api")))` and
  `testFixturesImplementation(project(":showcase-test"))` blocks from `showcase-projection-model/build.gradle.kts`
- [x] 1.2 Change `showcase-query-api/build.gradle.kts` `testFixturesApi(testFixtures(project(":showcase-command-api")))`
  to `testFixturesImplementation(testFixtures(project(":showcase-command-api")))`, and add
  `implementation(testFixtures(project(":showcase-command-api")))` to its `test` suite (the suite imports
  `RandomCommandTestUtils` and previously got it via the `testFixturesApi` re-export)

## 2. Consumer modules

- [x] 2.1 In `showcase-query-service/build.gradle.kts`: add
  `implementation(testFixtures(project(":showcase-command-api")))`; remove
  `implementation(testFixtures(project(":showcase-projection-model")))` and
  `implementation(testFixtures(project(":showcase-query-proto")))`. Also add
  `implementation(project(":showcase-projection-model"))` and
  `implementation(project(":showcase-query-proto"))` to its `integrationTest` suite, which imports `ShowcaseEntity`
  (projection-model main) and `QueryMessageRequestMapper` (query-proto main) that the removed testFixtures edges used
  to provide
- [x] 2.2 In `showcase-query-client/build.gradle.kts`: add
  `implementation(testFixtures(project(":showcase-command-api")))`; remove
  `implementation(testFixtures(project(":showcase-projection-model")))` and
  `implementation(testFixtures(project(":showcase-query-proto")))`. Also add
  `implementation(project(":showcase-projection-model"))` to its `integrationTest` suite, which imports `ShowcaseEntity`
  (projection-model main)
- [x] 2.3 In `showcase-projection-service/build.gradle.kts`: remove
  `implementation(testFixtures(project(":showcase-projection-model")))`. Also add
  `implementation(project(":showcase-projection-model"))` to its `integrationTest` suite, which imports `ShowcaseEntity`
  (projection-model main)
- [x] 2.4 In `showcase-query-proto/build.gradle.kts`: change
  `implementation(testFixtures(project(":showcase-test")))` to `implementation(project(":showcase-test"))`

## 3. Verification

- [x] 3.1 Run `./gradlew :showcase-command-api:test :showcase-query-api:test :showcase-query-proto:componentTest` to
  confirm fixture-compilation and unit/component tests still pass
- [x] 3.2 Run `./gradlew compileTestJava compileComponentTestJava compileIntegrationTestJava` for every touched module
  (`showcase-query-service`, `showcase-query-client`, `showcase-projection-service`, `showcase-api-gateway`) to confirm
  all test imports resolve via direct dependencies
- [x] 3.3 Confirm `showcase-projection-model` no longer produces a test-fixtures jar (the `testFixturesJar` task has no
  outputs beyond the manifest) and that no module depends on it
- [x] 3.4 Run `./gradlew build` for the whole project to confirm nothing regressed
