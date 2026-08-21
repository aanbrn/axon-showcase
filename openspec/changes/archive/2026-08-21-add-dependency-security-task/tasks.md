## 1. Create the convention plugin

- [x] 1.1 Add `build-logic/src/main/kotlin/dependency-security-conventions.gradle.kts` registering a
      `dependencySecurityCheck` `Exec` task that runs `snyk test --all-sub-projects` from the root project directory
- [x] 1.2 In the same plugin, guard the missing-CLI case: when the `snyk` binary is not resolvable on `PATH`, the task
      fails with a clear message that the Snyk CLI is required

## 2. Apply the plugin to the root project

- [x] 2.1 Add `id("dependency-security-conventions")` to the `plugins {}` block in `build.gradle.kts` (alongside
      `id("dependency-versions-conventions")`)

## 3. Verify the task

- [x] 3.1 Run `./gradlew dependencySecurityCheck` and confirm it invokes the Snyk scan over all sub-projects and
      succeeds with the current (clean) dependency state
- [x] 3.2 Run `./gradlew check --dry-run` and confirm `dependencySecurityCheck` is NOT part of the `check` task graph
- [x] 3.3 Temporarily move `snyk` off `PATH` (or simulate) and confirm the task fails with the clear "Snyk CLI is
      required" message, then restore `PATH`