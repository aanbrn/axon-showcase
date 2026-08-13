## 1. Code changes

- [ ] 1.1 In `Resilience4jAutoConfigurationImportFilter.java`, replace the `Preconditions.checkState` import with
      `org.springframework.util.Assert` and change the call site to `Assert.state(environment != null, ...)`
- [ ] 1.2 In `build.gradle.kts`, remove `implementation(libs.guava)`

## 2. Validate

- [ ] 2.1 Run `openspec validate --changes "remove-guava-from-resilience4j-extension"` and confirm the change passes
- [ ] 2.2 Run `./gradlew :showcase-resilience4j-extension:build` and confirm compilation and checks pass

## 3. Archive

- [ ] 3.1 Run `openspec archive "remove-guava-from-resilience4j-extension"` to archive the change
