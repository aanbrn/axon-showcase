## Why

The `showcase-mapstruct-extension` module — the custom MapStruct `AccessorNamingStrategy` for Lombok fluent
accessors — has **zero test coverage**. The module's single class, `FluentAccessorNamingStrategy`, drives compile-time
mapper generation across services (e.g., `showcase-query-service`), and its behavior exists in a spec with no tests
backing it. This change adds deterministic unit tests for the class plus a verification of the SPI registration
contract.

## What Changes

- Add `FluentAccessorNamingStrategyTests` in `showcase-mapstruct-extension/src/test/java/showcase/mapstruct/`
  covering (via mocked `javax.lang.model` elements):
  - Param-less method matching a field by name and return type → recognized as getter
  - Param-less method with no matching field → NOT recognized
  - Method with parameters → NOT recognized
  - Method name matches a field but return type differs → NOT recognized
  - Fluent getter property name resolves to the method simple name
  - Standard getter property name falls back to the default strategy (`getShowcaseId()` → `showcaseId`)
  - Coexistence of fluent and standard getters
- Add a test asserting the `META-INF/services/org.mapstruct.ap.spi.AccessorNamingStrategy` file lists
  `showcase.mapstruct.FluentAccessorNamingStrategy` (SPI registration contract).
- Add `testImplementation(libs.mockito.core)` to the module's `build.gradle.kts`.

## Capabilities

### New Capabilities

_(none — pure test addition, no behavioral change)_

### Modified Capabilities

_(none)_

## Impact

- **Code**: new test source file under `showcase-mapstruct-extension/src/test/java/showcase/mapstruct/`; add
  `testImplementation(libs.mockito.core)` to `build.gradle.kts`.
- **Dependencies**: Mockito 5.23.0 (already in the version catalog and agent-enabled by `java-conventions`).
- **Behavior**: unchanged.