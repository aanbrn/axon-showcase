## 1. Code changes

- [ ] 1.1 In `Resilience4jAutoConfigurationImportFilter.java`, remove the `threadPoolBulkheadEnabled` variable and
      simplify the bulkhead case to `resilienceEnabled && bulkheadEnabled`
- [ ] 1.2 In `additional-spring-configuration-metadata.json`, remove the `resilience4j.thread-pool-bulkhead.enabled`
      property entry

## 2. Validate

- [ ] 2.1 Run `openspec validate --changes "simplify-bulkhead-enablement"` and confirm the change and spec structure
      pass
- [ ] 2.2 Run `./gradlew :showcase-resilience4j-extension:build` and confirm compilation and checks pass

## 3. Review

- [ ] 3.1 Review the delta spec against the code changes and confirm the MODIFIED requirements match the new behavior

## 4. Archive

- [ ] 4.1 Run `openspec archive "simplify-bulkhead-enablement"` to fold the delta into
      `openspec/specs/showcase/resilience4j-extension/spec.md`
- [ ] 4.2 Confirm `openspec/specs/showcase/resilience4j-extension/spec.md` reflects the single-flag bulkhead gating
