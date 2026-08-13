## Why

The `showcase-resilience4j-extension` module pulls in Guava (~2.8 MB) solely for a single
`Preconditions.checkState` call. Spring's `Assert.state` — already available transitively via `spring-core` —
provides the same semantics (throws `IllegalStateException` with a message) with zero additional dependencies.

## What Changes

- Replace `com.google.common.base.Preconditions.checkState` with `org.springframework.util.Assert.state` in
  `Resilience4jAutoConfigurationImportFilter`.
- Remove the `guava` dependency from `build.gradle.kts`.

## Capabilities

### New Capabilities

_(none — pure dependency swap, no behavioral change)_

### Modified Capabilities

_(none — the filter's observable behavior is unchanged)_

## Impact

- **Code**: `Resilience4jAutoConfigurationImportFilter.java` — one import swap, one call site.
- **Dependencies**: `build.gradle.kts` — remove `implementation(libs.guava)`.
- **No behavioral change**: same exception type (`IllegalStateException`), same message, same runtime behavior.
