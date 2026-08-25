## 1. Restore the suppressions

- [x] 1.1 Add `@file:Suppress("UnstableApiUsage")` to the nine test-suites-DSL build scripts, and verify via the
      IntelliJ inspection that each reports zero `UnstableApiUsage` warnings
- [x] 1.2 Re-add the inline `@Suppress("UnstableApiUsage")` inside `dependencyResolutionManagement { }` in
      `settings.gradle.kts` and `build-logic/settings.gradle.kts`, and verify via the IntelliJ inspection that both
      report zero `UnstableApiUsage` warnings

## 2. Verify the build is unaffected

- [x] 2.1 Run `./gradlew spotlessApply` and `./gradlew build -x e2eTest -PskipITs`, and confirm the full build stays
      green

## 3. Verify the change artifacts

- [x] 3.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors