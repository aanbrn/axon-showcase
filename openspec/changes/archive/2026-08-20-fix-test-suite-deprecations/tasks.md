## 1. Refactor the `test` suite binding

Replace `val test by getting(JvmTestSuite::class)` with `val test = suites.getByName<JvmTestSuite>("test")` in each
module that declares it.

- [x] 1.1 `showcase-command-service/build.gradle.kts`
- [x] 1.2 `showcase-command-client/build.gradle.kts`
- [x] 1.3 `showcase-query-service/build.gradle.kts`
- [x] 1.4 `showcase-query-client/build.gradle.kts`
- [x] 1.5 `showcase-query-proto/build.gradle.kts`
- [x] 1.6 `showcase-projection-service/build.gradle.kts`
- [x] 1.7 `showcase-projection-model/build.gradle.kts`
- [x] 1.8 `showcase-api-gateway/build.gradle.kts`

## 2. Refactor the registered suite bindings

Replace `val suite by register<JvmTestSuite>("...") { ... }` with `val suite = suites.register<JvmTestSuite>("...")
{ ... }` in each module that declares one, keeping the suites bound as `val`s and passing the provider to
`shouldRunAfter` / `mustRunAfter` where referenced.

- [x] 2.1 `showcase-command-service/build.gradle.kts` — `componentTest`
- [x] 2.2 `showcase-query-service/build.gradle.kts` — `componentTest`
- [x] 2.3 `showcase-query-client/build.gradle.kts` — `componentTest`, `integrationTest`
- [x] 2.4 `showcase-projection-service/build.gradle.kts` — `componentTest`
- [x] 2.5 `showcase-api-gateway/build.gradle.kts` — `componentTest`, `integrationTest`

## 3. Documentation

- [x] 3.1 Update the AGENTS.md convention note "A suite can be referenced in `shouldRunAfter(...)` only when bound as
      a val (e.g. `val integrationTest by register<JvmTestSuite>(\"integrationTest\")`)" to the non-deprecated binding
      form (`val integrationTest = suites.register<JvmTestSuite>("integrationTest")`).

## 4. Verification

- [x] 4.1 Run a full configuration with warnings visible
      (`./gradlew help --warning-mode all`) and confirm no test-suite `by getting` / `by register` deprecation warnings
      remain and no new warnings appear.
- [x] 4.2 Run a build sanity check that compiles the test source sets (e.g. `./gradlew compileTestJava`) and confirm
      the suites still wire up and the ordering references resolve.