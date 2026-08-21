## 1. Add version and library entries

- [x] 1.1 In `gradle/libs.versions.toml`, add `jackson3-bom = "3.1.6"` and `httpclient5 = "5.6.3"` to the `[versions]` block
- [x] 1.2 In `gradle/libs.versions.toml`, register `jackson3-bom = { group = "tools.jackson", name = "jackson-bom", version.ref = "jackson3-bom" }` and `httpclient5 = { group = "org.apache.httpcomponents.client5", name = "httpclient5", version.ref = "httpclient5" }` in the `[libraries]` block

## 2. Update the platform BOM

- [x] 2.1 In `platform/build.gradle.kts`, add `api(platform(libs.jackson3.bom))` to the `dependencies` block alongside `api(platform(libs.jackson2.bom))`
- [x] 2.2 In `platform/build.gradle.kts`, add `api(libs.httpclient5)` to the `constraints` block alongside `api(libs.httpcore5.h2)`

## 3. Verify dependency resolution

- [x] 3.1 Run `./gradlew :showcase-projection-model:dependencies --configuration runtimeClasspath` and confirm `org.apache.httpcomponents.client5:httpclient5` resolves to `5.6.3` (previously `5.6.1`)
- [x] 3.2 Run `./gradlew :showcase-query-service:dependencies --configuration runtimeClasspath` (and `:showcase-projection-service`, `:showcase-query-client`) and confirm `tools.jackson.core:jackson-core` and `tools.jackson.core:jackson-databind` resolve to `3.1.6` and `httpclient5` resolves to `5.6.3`

## 4. Run the security scan

- [x] 4.1 Run `snyk test --all-sub-projects` and confirm `showcase-projection-model`, `showcase-projection-service`, `showcase-query-client`, and `showcase-query-service` report no vulnerable paths

## 5. Run the affected tests

- [x] 5.1 Run `./gradlew :showcase-query-service:test :showcase-projection-service:test :showcase-query-client:test :showcase-projection-model:test` and confirm green
- [x] 5.2 Run the query-service and projection-service integration tests (require Docker) and the query-client integration tests — these exercise the bumped Jackson 3 runtime against real OpenSearch — and confirm green