## 1. Update Gradle plugins

- [x] 1.1 In `gradle/libs.versions.toml`, bump `spotbugs-plugin` from `6.5.5` to `6.5.10`.
- [x] 1.2 In `gradle/libs.versions.toml`, bump `dependencyVersions-plugin` from `0.54.0` to
      `0.61.0` and change its group from `com.github.ben-manes` to `io.github.ben-manes`.
- [x] 1.3 In `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts`, change the
      applied plugin ID from `com.github.ben-manes.versions` to `io.github.ben-manes.versions`.

## 2. Verification

- [x] 2.1 Refresh the build model and run `./gradlew :build-logic:build` (or `./gradlew help`) to
      confirm the plugin resolution succeeds after the ID migration.
- [x] 2.2 Run `./gradlew dependencyUpdates --no-parallel` and confirm the task runs successfully
      under the new plugin version.
- [x] 2.3 Confirm `git diff` touches only `gradle/libs.versions.toml` and
      `dependency-versions-conventions.gradle.kts`.
