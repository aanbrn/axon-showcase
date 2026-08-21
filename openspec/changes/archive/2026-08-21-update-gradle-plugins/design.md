## Context

See proposal.md - Why. `com.github.spotbugs` and `com.github.ben-manes.versions` are out of date,
and the versions-plugin ID is deprecated (`com.github.ben-manes.versions` → `io.github.ben-manes.versions`).
The old versions-plugin also fails `dependencyUpdates` under Gradle 9.7.1 without `--no-parallel`.

## Goals / Non-Goals

**Goals:**
- Bump `spotbugs-plugin` to `6.5.10` and `dependencyVersions-plugin` to `0.61.0`.
- Migrate the versions-plugin ID to `io.github.ben-manes.versions`.

**Non-Goals:**
- Not bumping Spring Boot to 4.x (separate major migration).
- Not changing any dependency versions in the BOM (runtime dependencies are out of scope).

## Decisions

- **Bump `spotbugs-plugin` to `6.5.10`.**
  - Patch-level within the 6.5.x line; low risk.
  - Alternative: stay on `6.5.5`. Rejected — the proposal's Why calls out the lag.

- **Bump `dependencyVersions-plugin` to `0.61.0` and migrate the ID.**
  - `0.61.0` is the latest; the `com.github.ben-manes.versions` ID is deprecated in favor of
    `io.github.ben-manes.versions` (same author/codebase, new plugin marker). The migration is a
    one-line change in the convention file.
  - Alternative: bump to `0.61.0` but keep the old `com.github.ben-manes.versions` ID. Rejected —
    the old ID is deprecated; migrating now avoids a later forced migration.

## Risks / Trade-offs

- [The plugin ID migration could break plugin resolution in build-logic] → Mitigation: the
  underlying artifact (`com.github.ben-manes:gradle-versions-plugin`) is unchanged; only the
  applied ID string changes. Verification runs `dependencyUpdates` and `./gradlew build`.
- [Newer versions-plugin may change `dependencyUpdates` output/behavior] → Mitigation: the task
  config (`DependencyUpdatesTask`, `rejectVersionIf`, release channel) is unchanged; verification
  confirms it still runs under Gradle 9.7.1.

## Migration Plan

1. Bump the two versions in `gradle/libs.versions.toml`.
2. Migrate the plugin ID in `dependency-versions-conventions.gradle.kts`.
3. Refresh the build model, then run `./gradlew dependencyUpdates --no-parallel` (or the project's
   normal invocation) to confirm the plugin resolves and the task runs.

## Open Questions

- None.
