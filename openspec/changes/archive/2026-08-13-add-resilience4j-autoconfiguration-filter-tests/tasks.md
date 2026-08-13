## 1. Tests

- [ ] 1.1 Add `testImplementation(libs.mockito.core)` to `showcase-resilience4j-extension/build.gradle.kts`
- [ ] 1.2 Create `Resilience4jAutoConfigurationImportFilterTests` in
      `showcase-resilience4j-extension/src/test/java/showcase/resilience4j/` with a mocked `Environment` helper that
      stubs the six `getProperty(name, Boolean.TYPE, Boolean.TRUE)` calls from a `Map<String, Boolean>`
- [ ] 1.3 Add a test asserting `match` throws `IllegalStateException` with message `"environment" is required` when
      no environment is injected
- [ ] 1.4 Add a test asserting `resilience4j.enabled=false` excludes all five feature auto-configurations regardless
      of per-feature flags
- [ ] 1.5 Add a test asserting all feature auto-configurations are eligible when no flags are set (defaults to `true`)
- [ ] 1.6 Add a test asserting `resilience4j.circuitbreaker.enabled=false` excludes only the circuit breaker
      auto-configuration
- [ ] 1.7 Add a test asserting `resilience4j.bulkhead.enabled=false` excludes only the bulkhead auto-configuration
- [ ] 1.8 Add a test asserting a non-Resilience4j auto-configuration class name passes through unfiltered
- [ ] 1.9 Add a test asserting the circuit breaker FQCN from the spec matches the feature pattern and is gated by
      the circuit breaker flag
- [ ] 1.10 Add a test asserting null and empty class-name entries are excluded (result slot stays `false`)
- [ ] 1.11 Add a test asserting `META-INF/spring.factories` lists
       `showcase.resilience4j.Resilience4jAutoConfigurationImportFilter`
- [ ] 1.12 Add a test asserting `META-INF/additional-spring-configuration-metadata.json` declares all six
       `resilience4j.*.enabled` properties as `java.lang.Boolean` with `defaultValue` `true`

## 2. Validate

- [ ] 2.1 Run `openspec validate --changes "add-resilience4j-autoconfiguration-filter-tests"` and confirm the change
      passes
- [ ] 2.2 Run `./gradlew :showcase-resilience4j-extension:check` and confirm tests and checks pass

## 3. Archive

- [ ] 3.1 Run `openspec archive "add-resilience4j-autoconfiguration-filter-tests"` to archive the change