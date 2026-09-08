## 1. Add the root Spotless kotlin format

- [x] 1.1 Add a `kotlin` format to the root `spotless` block in `build.gradle.kts`: `target("build-logic/src/**/*.kt")`
      and `ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }` (matching the existing `kotlinGradle` step).
- [x] 1.2 Confirm `spotlessCheck` is wired into the root `check` (it is, via the existing Spotless plugin).

## 2. Reflow and verify

- [x] 2.1 Run `./gradlew spotlessApply` once to reflow the four build-logic task classes; review the diff (no semantic
      changes, `openspec validate` still passes).
- [x] 2.2 Run `./gradlew spotlessCheck` and confirm the `kotlin` format passes.
- [x] 2.3 Update `AGENTS.md` / `README.md` to include build-logic Kotlin in the Spotless coverage description.
- [x] 2.4 Run `openspec validate --all` and confirm the change passes.
