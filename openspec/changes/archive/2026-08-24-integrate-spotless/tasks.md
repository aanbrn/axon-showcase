## 1. Add the Spotless plugin

- [x] 1.1 Add a `spotless` plugin version to `gradle/libs.versions.toml` (current stable compatible with Gradle 9.7.1,
      catalog-owned) and `implementation(libs.spotless.plugin)` to `build-logic/build.gradle.kts`

## 2. Wire Spotless into the build

- [x] 2.1 Apply `id("com.diffplug.spotless")` in `build-logic/src/main/kotlin/code-check-conventions.gradle.kts` and
      configure the `java` target: `palantirJavaFormat()`, `licenseHeader("// SPDX-License-Identifier: MIT")`, and an
      explicit `target("src/**/*.java")` so generated sources under `build/` are never formatted (mirroring the
      checkstyle generated-source exclusion)

## 3. Retire the checkstyle import-order rule

- [x] 3.1 Remove the `CustomImportOrder` module from `config/checkstyle/checkstyle.xml` (the formatter now owns import
      order; `UnusedImports` and the naming rules stay)

## 4. Apply and green the gate

- [x] 4.1 Run `./gradlew spotlessApply` once to reformat all Java sources and add the license headers
- [x] 4.2 Run `./gradlew check` (or `spotlessCheck` + the checkstyle tasks) across all modules and confirm the gates
      pass with the formatted sources

## 5. Verify the change artifacts

- [x] 5.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors
- [x] 5.2 Update `AGENTS.md` (formatting convention flips from the IntelliJ formatter to `./gradlew spotlessApply` as
      the canonical format step; note import order is formatter-owned; add an IntelliJ auto-reformat gotcha) and
      `README.md` (check pipeline note gains spotless; add an IntelliJ IDEA Setup subsection for contributors) per the
      docs-refresh convention

## 6. Version the IntelliJ code style

- [x] 6.1 Add `.idea/codeStyles/Project.xml` with the palantir import layout (static imports first, blank line, then
      all other imports) and set `.idea/codeStyles/codeStyleConfig.xml` to `USE_PER_PROJECT_SETTINGS=true` +
      `PREFERRED_PROJECT_CODE_STYLE=Project` (per-project settings are required for IntelliJ to honor `Project.xml`)
- [x] 6.2 Un-ignore `.idea/codeStyles/` in `.gitignore` so the code style is repo-versioned and contributors inherit it
- [x] 6.3 Add `scripts/setup-idea.sh` (runs `installPlugins palantir-java-format` against a discovered IntelliJ,
      independent of the Steroid MCP) and commit `.idea/palantir-java-format.xml` (`enabled=true`) so the plugin
      install is scriptable and auto-enabled for the project