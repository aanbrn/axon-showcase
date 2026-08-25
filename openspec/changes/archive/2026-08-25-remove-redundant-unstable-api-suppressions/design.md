# Design: Remove redundant UnstableApiUsage suppressions from Gradle Kotlin DSL

## Context

See proposal.md — Why. Thirteen `.gradle.kts` files suppress Gradle's `UnstableApiUsage` compiler error. Gradle raises
unstable-API usage as a compilation error, so a suppression is required exactly when the file calls an
`@UnstableApi`-annotated Gradle API. In these build scripts that API is the JVM test-suites DSL
(`testing { suites { } }`, `register<JvmTestSuite>`, `suites.withType<JvmTestSuite>`).

## Goals / Non-Goals

**Goals:**
- Remove `@Suppress("UnstableApiUsage")` from every `.gradle.kts` file that compiles without it.
- Keep the suppression where the file genuinely uses the unstable test-suites DSL.
- Verify the whole build still passes after the removals.

**Non-Goals:**
- Replacing `@UnstableApi` usage (e.g. migrating off the test-suites DSL) — the suppression stays where the unstable
  API is used.
- Touching `@Suppress` on other annotations or in non-`.gradle.kts` sources.

## Decisions

- **The build is the source of truth for redundancy.** Gradle emits `UnstableApiUsage` as an error, so removing a
  needed suppression fails compilation. Instead of auditing each file against `@UnstableApi` metadata by hand, remove
  the suppressions and let `./gradlew build -x e2eTest -PskipITs` (which compiles every build script) report which
  files still need one. This is authoritative and avoids false positives from API-annotations drifting across Gradle
  versions.
- **Expected keepers are the test-suites-DSL files.** Files using `testing { suites { } }` / `register<JvmTestSuite>`
  (query-client, command-client, query-api, query-proto, projection-model, and likely the projection/query services'
  `e2eTest` suites) will retain the suppression; files with only `plugins`, `dependencies`, and `description`
  assignments (e.g. command-api) will lose it.
- **Reformat after edit.** After removing suppression lines, run `./gradlew spotlessApply` (the suppression is the
  first line, so removing it is a normal ktfmt-safe edit) and confirm `spotlessKotlinGradleCheck` passes.

## Risks / Trade-offs

- [A file we expect to lose the suppression still needs it (undetected unstable API)] → Mitigation: the build is
  authoritative; any such file keeps its suppression after verification.
- [A file that unexpectedly loses the suppression compiles only because of a compile-avoidance quirk] → Mitigation:
  run the full `./gradlew build -x e2eTest -PskipITs` so every build script is compiled, not just a subset.

## Migration Plan

1. Remove all `@Suppress("UnstableApiUsage")` lines from the 13 `.gradle.kts` files.
2. Run `./gradlew build -x e2eTest -PskipITs`; for each failing build script, restore its suppression.
3. Run `./gradlew spotlessApply` and confirm `spotlessKotlinGradleCheck` passes.
4. Rollback: `git checkout` the touched build scripts (or re-add the suppression lines).

## Open Questions

None.