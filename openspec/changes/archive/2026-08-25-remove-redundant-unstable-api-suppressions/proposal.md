# Proposal: Remove redundant UnstableApiUsage suppressions from Gradle Kotlin DSL

## Why

Thirteen `.gradle.kts` files carry `@Suppress("UnstableApiUsage")`; several are redundant because the file uses no
`@UnstableApi`-annotated Gradle API. Redundant suppressions defeat their own purpose — they keep Gradle from flagging
future accidental unstable-API usage in those files.

## What Changes

- Remove `@file:Suppress("UnstableApiUsage")` (and the two inline `@Suppress("UnstableApiUsage")` in
  `settings.gradle.kts` and `build-logic/settings.gradle.kts`) from `.gradle.kts` files that use no unstable API.
- Keep the suppression only where the file actually uses an `@UnstableApi`-annotated API — in these build scripts the
  JVM test-suites DSL (`testing { suites { } }`, `register<JvmTestSuite>`, `suites.withType<JvmTestSuite>`) — so the
  build still compiles.
- Redundancy is determined empirically: a file is redundant when its build scripts compile without the suppression
  (Gradle raises unstable-API usage as a compile error, so removing a needed suppression fails the build).
- No behavioral or functional change; this only removes compiler-diagnostic suppressions.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. Removing redundant compiler-diagnostic suppressions from build scripts is a pure refactor
with no externally observable behavior change.

## Impact

- **Code**: up to 13 `.gradle.kts` files — module build files (`showcase-*`), the `build-logic` convention
  `java-conventions.gradle.kts`, and the root/build-logic `settings.gradle.kts`.
- **Docs**: none expected (`AGENTS.md`/`README.md` untouched unless a convention note becomes stale).
- **Build**: re-verified via `spotlessKotlinGradleCheck` and build-script compilation (`./gradlew build -x e2eTest
  -PskipITs`).
- **Tests**: no test changes.