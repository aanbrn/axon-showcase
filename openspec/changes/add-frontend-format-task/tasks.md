## 1. Implementation

- [x] 1.1 Register `npmFormat` in `build-logic/src/main/kotlin/frontend-conventions.gradle.kts`: an `NpmTask` with
      `args = listOf("run", "format")`, `group = "verification"` (mirroring `npmFormatCheck`/`spotlessApply`),
      `dependsOn(npmCi)`, `inputs.files(fileTree("src"))` (as the sibling frontend tasks declare), a description, and no
      `outputs` (a format task must always run)
- [x] 1.2 Confirm the task formats the module and that verification is unchanged: `:showcase-web-ui:npmFormat` rewrites
      sources to Prettier's canonical form, `:showcase-web-ui:npmFormatCheck` still passes, and `check` does not depend
      on `npmFormat`

## 2. Documentation

- [x] 2.1 `AGENTS.md`: name `./gradlew :showcase-web-ui:npmFormat` in the Frontend/Formatting conventions as the
      frontend analog of `spotlessApply`
- [x] 2.2 `README.md`: mention the frontend format task where the web module's formatting is described

## 3. Verification

- [x] 3.1 `./gradlew spotlessApply` and `./gradlew spotlessCheck` pass; `openspec validate --all` passes
- [x] 3.2 `./gradlew :showcase-web-ui:npmFormat --dry-run` shows the task in the graph and `check`'s graph is unchanged
