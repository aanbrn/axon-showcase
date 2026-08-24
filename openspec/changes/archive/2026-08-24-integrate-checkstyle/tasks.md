## 1. Add the Checkstyle tool version

- [x] 1.1 Add a `checkstyle` version to `gradle/libs.versions.toml` (a current 10.x release compatible with Java 21,
      catalog-owned so `dependencyUpdates` reports it)

## 2. Wire Checkstyle into the build

- [x] 2.1 Apply `id("checkstyle")` in `build-logic/src/main/kotlin/code-check-conventions.gradle.kts`, set
      `checkstyle.toolVersion` from the catalog, and point the config at the root `config/checkstyle` directory

## 3. Create the ruleset

- [x] 3.1 Create `config/checkstyle/checkstyle.xml` encoding the documented conventions: `LineLength` (120),
      naming rules (`TypeName`, `MethodName`, `ConstantName`) with the `Tests`/`CT`/`IT`/`E2E` test-tier suffixes
      allowed, `UnusedImports` and import ordering
- [x] 3.2 Create `config/checkstyle/suppressions.xml` for any legitimate legacy exclusions surfaced by the first run

## 4. Run and green the gate

- [x] 4.1 Run `./gradlew check` across all modules, fix style violations (or add targeted suppressions), and confirm
      `checkstyleMain`/`checkstyleTest` pass everywhere

## 5. Verify the change artifacts

- [x] 5.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors
- [x] 5.2 Update `AGENTS.md` to note Checkstyle among the build-enforced quality gates, and `README.md` to include
      checkstyle in the `check` pipeline description (per the docs-refresh convention)