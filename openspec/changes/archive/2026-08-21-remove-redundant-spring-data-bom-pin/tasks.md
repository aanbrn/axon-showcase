## 1. Remove the redundant spring-data-bom pin

- [x] 1.1 Remove the `spring-data-bom` version entry and library coordinate from `gradle/libs.versions.toml`, and the
      `api(platform(libs.spring.data.bom))` line from `platform/build.gradle.kts`; verify `spring-data-commons` and
      `spring-data-jdbc` catalog entries remain

## 2. Verify the change

- [x] 2.1 Run `./gradlew compileJava` (or the affected modules) and confirm the build succeeds with Spring Data still
      resolving to `2025.0.13` from the SB-imported BOM
- [x] 2.2 Run `./gradlew dependencyUpdates` and confirm `spring-data-bom [2025.0.13 -> 2025.1.7]` no longer appears,
      while `spring-data-commons`/`spring-data-jdbc` still resolve as before

## 3. Verify the change artifacts

- [x] 3.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors