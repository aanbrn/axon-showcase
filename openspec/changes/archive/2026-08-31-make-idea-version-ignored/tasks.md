## 1. Version-control changes

- [x] 1.1 Update `.gitignore` to ignore the entire `.idea/` directory (drop the `!` exceptions for the five files) and
      verify `git status` no longer shows any `.idea/` file
- [x] 1.2 `git rm -r --cached .idea/` to untrack the five files while leaving them on disk, and verify the files still
      exist locally and `git ls-files .idea/` is empty

## 2. .editorconfig spike and import layout

- [x] 2.1 Spike: determine the `ij_java_layout_imports` encoding that reproduces the palantir layout (static group,
      blank line, all non-static) and confirm IntelliJ honors the `ij_java_*` keys with no `Project.xml` scheme present;
      record the result in `design.md` (fallback: a minimal committed scheme pair if the keys need one to layer onto)
- [x] 2.2 Add `.editorconfig` with the palantir import layout (`ij_java_layout_imports`, single-class imports, on-demand
      counts), and verify IntelliJ's Optimize Imports on a sample file produces the palantir order

## 3. Templates and setup script

- [x] 3.1 Commit the plugin-toggle templates under `config/idea/` (`palantir-java-format.xml`, `ktfmt.xml`,
      `codeStyleConfig.xml`) matching the current committed `.idea/` content, and verify they are byte-identical to what
      the IDE needs
- [x] 3.2 Extend `setup-idea.sh` to write the three templates into `.idea/` when absent (idempotent, no overwrite of
      existing files), and verify running it twice changes nothing
- [x] 3.3 Add the stdlib-only Python inspection-profile upsert to `setup-idea.sh` (targeted block replace of the
      `NewClassNamingConvention` inspection, preserving other inspections), and verify it adds the block when absent,
      updates it when present, and leaves unrelated inspections untouched
- [x] 3.4 Document **Python 3** as a new developer prerequisite in `AGENTS.md` (required by `setup-idea.sh`'s inspection
      upsert) and verify the prerequisites section stays consistent

## 4. Docs

- [x] 4.1 Update `AGENTS.md` and `README.md` to state that `.idea/` is generated/ignored, the canonical config lives in
      `.editorconfig` + `config/idea/` + `setup-idea.sh`, and a fresh clone needs one `setup-idea.sh` run for IDE
      parity, verifying all added lines stay within the 120-character limit
- [x] 4.2 Verify the full change on a clean clone simulation: fresh checkout, run `setup-idea.sh`, confirm the IDE
      config matches the formatter (plugins enabled, import layout in effect, naming inspection present) and that
      `./gradlew check` (including `spotlessCheck`) passes without an IDE