# Proposal: Restore UnstableApiUsage suppressions for the test-suites DSL and settings scripts

## Why

The `remove-redundant-unstable-api-suppressions` change removed `@Suppress("UnstableApiUsage")` from every `.gradle.kts`
based on a build-centric test: the Gradle compiler only errors on `@UnstableApi`-annotated APIs, and the JVM test-suites
DSL (`testing { suites { } }`) plus `dependencyResolutionManagement` are `@Incubating` — so the build compiled without
the suppressions. But the IntelliJ `UnstableApiUsage` inspection flags `@Incubating` too, so those scripts still carry
IDE warnings. The suppressions were not redundant from the IDE's perspective; this change restores them where they are
genuinely needed.

## What Changes

- Re-add `@file:Suppress("UnstableApiUsage")` to the nine build scripts that use the test-suites DSL:
  `build-logic/src/main/kotlin/java-conventions.gradle.kts`, `showcase-api-gateway`, `showcase-command-client`,
  `showcase-command-service`, `showcase-projection-model`, `showcase-projection-service`, `showcase-query-api`,
  `showcase-query-proto`, `showcase-query-service`.
- Re-add the inline `@Suppress("UnstableApiUsage")` inside `dependencyResolutionManagement { }` in `settings.gradle.kts`
  and `build-logic/settings.gradle.kts`.
- Do NOT touch `showcase-command-api/build.gradle.kts` (no test-suites DSL — its suppression was genuinely redundant,
  with zero IDE warnings) or `showcase-query-client/build.gradle.kts` (already restored in the preceding change).
- No build behavior changes — these are compiler/IDE directives only; the Gradle build compiles with or without them.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. Build-script IDE-hygiene directives with no externally observable behavior change.

## Impact

- **Code (build scripts only)**: 11 `.gradle.kts` files restored to their pre-`remove-redundant-...` suppression state.
- **Docs**: none (`AGENTS.md`/`README.md` do not discuss these suppressions).
- **Build**: unchanged; verified via `spotlessKotlinGradleCheck` and `./gradlew build -x e2eTest -PskipITs`.
- **Tests**: no test changes; verified by clearing the IDE `UnstableApiUsage` inspections on all 11 files.