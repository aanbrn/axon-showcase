## 1. Remove the redundant suppressions

- [x] 1.1 Remove `@file:Suppress("UnstableApiUsage")` from all `.gradle.kts` files carrying it and the two inline
      `@Suppress("UnstableApiUsage")` in `settings.gradle.kts` / `build-logic/settings.gradle.kts`, and verify
      `spotlessKotlinGradleCheck` passes after `spotlessApply`

## 2. Let the build decide which suppressions are required

- [x] 2.1 Run `./gradlew build -x e2eTest -PskipITs` and restore the suppression on every build script that fails to
      compile, and verify the full build then passes

## 3. Verify the change artifacts

- [x] 3.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors