## 1. Modernize extra-property access

- [x] 1.1 In `build-logic/src/main/kotlin/code-coverage-conventions.gradle.kts`, replace
      `project.extra.properties["coverage.generatedClassExcludes"]` with
      `project.findProperty("coverage.generatedClassExcludes")` in `generatedClassExcludes()`.
- [x] 1.2 In the same file, replace `project.extra.properties["coverage.gate.enabled"]` with
      `project.findProperty("coverage.gate.enabled")` in the `coverageGateEnabled` provider.

## 2. Verification

- [x] 2.1 Run a full configuration with warnings visible (`./gradlew help --warning-mode all`) and confirm no *new*
      warnings are introduced by these edits (the pre-existing `Project.getProperties()` warning attributed to the
      `io.github.build-extensions-oss.helm` plugin may remain; it is not from this repo).
- [x] 2.2 Run the coverage tasks for a module that applies `code-coverage-conventions` (e.g.
      `./gradlew :showcase-command-service:jacocoTestReport`) to confirm the gate and generated-class excludes behave
      identically.
- [x] 2.3 Track the upstream helm-plugin `getProperties()` deprecation: file a GitHub issue against
      `build-extensions-oss/gradle-helm-plugin`. Filed on 2026-08-20 as
      https://github.com/build-extensions-oss/gradle-helm-plugin/issues/145 (after granting the `public_repo` scope to
      the classic PAT). Bump the plugin when a fixed release lands.