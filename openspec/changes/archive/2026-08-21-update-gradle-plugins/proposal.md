## Why

Two external Gradle plugins lag their latest releases, and one of them has a deprecated plugin ID:

- `com.github.spotbugs` (spotbugs-gradle-plugin) is pinned at `6.5.5`; latest is `6.5.10`.
- `com.github.ben-manes.versions` (gradle-versions-plugin) is pinned at `0.54.0`; latest is
  `0.61.0`. The `com.github.ben-manes.versions` plugin ID is deprecated in favor of
  `io.github.ben-manes.versions` (same author/codebase, new marker). The old `0.54.0` also fails
  `dependencyUpdates` under the project's Gradle 9.7.1 with "Parallel project execution is not
  supported"; a newer version resolves this.

## What Changes

### `gradle/libs.versions.toml`

- Bump `spotbugs-plugin` from `6.5.5` to `6.5.10`.
- Bump `dependencyVersions-plugin` from `0.54.0` to `0.61.0` and change its group from
  `com.github.ben-manes` to `io.github.ben-manes` (the new coordinate; the old one receives no
  further releases).

### `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts`

- Change the applied plugin ID from `com.github.ben-manes.versions` to
  `io.github.ben-manes.versions`.

## New Capabilities

- None.

## Modified Capabilities

- None (plugin version bumps + plugin ID migration; no behavioral change).

## Impact

- **Build**: SpotBugs and the versions plugin resolve to newer versions; the `dependencyUpdates`
  task now runs under the new plugin ID and version.
- **Tests**: no test changes.
- **Deployment**: no impact.
