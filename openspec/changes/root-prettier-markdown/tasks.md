## 1. Add the root Spotless markdown format

- [x] 1.1 Add a `markdown` format to the root `spotless` block in `build.gradle.kts`: `target` the in-scope markdown
      (`docs/**/*.md`, `AGENTS.md`, `README.md`, `openspec/specs/**/*.md`, active `openspec/changes/*/`),
      `targetExclude` the archive, and declare the Prettier options inline via `prettier("3.9.6").config(...)`
      (`proseWrap: "always"` / `printWidth: 120`, matching the web-UI's Prettier 3.9.6) — no root `.prettierrc` file.
- [x] 1.2 Confirm `spotlessCheck` is wired into the root `check` (it is, via the existing Spotless plugin).

## 2. Reflow and verify

- [x] 2.1 Run `./gradlew spotlessApply` once to reflow the ~30 in-scope markdown files; review the diff (no semantic
      changes, `openspec validate` still passes).
- [x] 2.2 Run `./gradlew spotlessCheck` and confirm the markdown format passes.
- [x] 2.3 Update `AGENTS.md` / `README.md` to replace the "manual 120-char wrapping" convention with the automated
      Spotless/Prettier gate.
- [x] 2.4 Run `openspec validate --all` and confirm the change passes.
