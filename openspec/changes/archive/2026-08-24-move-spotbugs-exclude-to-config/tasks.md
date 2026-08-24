## 1. Relocate the SpotBugs filter

- [x] 1.1 Move `spotbugs-exclude.xml` to `config/spotbugs/spotbugs-exclude.xml`
- [x] 1.2 Update `code-check-conventions.gradle.kts` so the exclude and include filter lookups point at
      `config/spotbugs/` (preserving the "if present" existence check)

## 2. Refresh the docs

- [x] 2.1 Update the `AGENTS.md` SpotBugs bullet to reference `config/spotbugs/` for the filters

## 3. Verify the change artifacts

- [x] 3.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors