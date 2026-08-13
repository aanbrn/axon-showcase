## 1. Tests

- [ ] 1.1 Add `testImplementation(libs.mockito.core)` to `showcase-mapstruct-extension/build.gradle.kts`
- [ ] 1.2 Create `FluentAccessorNamingStrategyTests` in `showcase-mapstruct-extension/src/test/java/showcase/mapstruct/`
      with a test helper that builds mocked `javax.lang.model` elements (a class with a field of a given name/type and
      a method of a given name/parameters/return type)
- [ ] 1.3 Add a test asserting a param-less method matching a field by name and return type is recognized as a getter
- [ ] 1.4 Add a test asserting a param-less method with no matching field is NOT recognized as a getter
- [ ] 1.5 Add a test asserting a method with parameters is NOT recognized as a getter
- [ ] 1.6 Add a test asserting a method whose name matches a field but whose return type differs is NOT recognized as a
      getter
- [ ] 1.7 Add a test asserting a fluent getter's property name resolves to the method simple name
- [ ] 1.8 Add a test asserting a standard getter (`getShowcaseId()`) resolves its property name via the default strategy
- [ ] 1.9 Add a test asserting fluent and standard getters coexist on the same class
- [ ] 1.10 Add a test asserting the `META-INF/services/org.mapstruct.ap.spi.AccessorNamingStrategy` resource file
       lists `showcase.mapstruct.FluentAccessorNamingStrategy`

## 2. Validate

- [ ] 2.1 Run `openspec validate --changes "add-fluent-accessor-naming-strategy-tests"` and confirm the change passes
- [ ] 2.2 Run `./gradlew :showcase-mapstruct-extension:test` and confirm all tests pass

## 3. Archive

- [ ] 3.1 Run `openspec archive "add-fluent-accessor-naming-strategy-tests"` to archive the change