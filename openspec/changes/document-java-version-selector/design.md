# Design

## Context

`.java-version` has been tracked since `5ff1a0c5` and holds `21`, the same major the Java toolchain declares. Nothing in
the repository reads it, and no markdown document mentioned it before this change.

Three mechanisms decide which Java runs, and only one of them involves the file:

| Mechanism                                   | What it decides                                                                                 | Where it is declared                                                                              |
| ------------------------------------------- | ----------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| A version manager honouring `.java-version` | the local selection, and — for jenv's `export` plugin — the `JAVA_HOME` its prompt hook exports | the manager's own configuration                                                                   |
| The Gradle wrapper                          | the launcher JVM: `$JAVA_HOME/bin/java` when set, otherwise `java` from `PATH`                  | `gradlew`                                                                                         |
| The Java toolchain                          | which JDK compiles and tests                                                                    | `gradle/libs.versions.toml`, applied by `build-logic/src/main/kotlin/java-conventions.gradle.kts` |

CI has a fourth, independent selection (`actions/setup-java`), so the file never reaches a pipeline.

## Decisions

### D1 — Keep the file; document it instead of removing it

It is a working convenience today: jenv reads it, and its `export` plugin refreshes `JAVA_HOME` before each prompt, so
`./gradlew` in a shell that started there launches on the selected JDK. Removing it would delete a working selector to
answer a documentation gap.

### D2 — Describe the mechanism, not the readers

Support is conditional and not exclusive: jenv reads the name outright, and mise's `core:java` backend declares
`.java-version` as an idiomatic version file that it reads only when that file class is enabled. A list of readers in
the README would be the managers' documentation restated, and it would rot on their next release. The block therefore
says what the file is and what it does _not_ do, and leaves the reader list where it is maintained.

### D3 — Place it in Getting Started → Build the Project

It is a shell setting that affects the first build a visitor runs, so it belongs next to `./gradlew build` rather than
in the IDE-formatting section, which is about editors.

### D4 — No statement about the owner's machine

The measured details (Homebrew's `java` winning before the PATH fix, `JAVA_HOME` differing from `PATH`) generalise only
as the closing sentence that the two can disagree; the rest is host state, and a dotfile change is not part of this
unit.

## Alternatives considered

| Alternative                           | Why not                                                                                                              |
| ------------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| Delete `.java-version`                | Discards a working selector; the binding it might be mistaken for lives in the catalog and CI, which stay either way |
| Document it as “for jenv users”       | Reads as exclusionary, though jenv is only the best-known reader; and it still enumerates a reader                   |
| Enumerate the managers that honour it | A reference list in a showcase document that the managers' own docs own, and one that ages                           |
| Put the block in the IDE section      | The setting affects the build command, not the editor; the IDE section is about formatting                           |

## Claims table

| #   | Claim                                                                   | Source / command                                                                                                                                                                                                                     | Observed                                                                                                |
| --- | ----------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------- |
| 1   | `.java-version` is tracked, holds `21`, and predates every other change | `git ls-files -s .java-version`; `cat .java-version`; `git log --oneline -- .java-version \| tail -1`                                                                                                                                | `100644` blob, `21`, `5ff1a0c5 initial commit`                                                          |
| 2   | No markdown documented it before this change                            | `git grep -n "java-version" 32c21f5d -- '*.md' ':!openspec/changes/archive'`                                                                                                                                                         | one hit, `AGENTS.md`'s workflow bullet about `java-version: '21'`, not this file                        |
| 3   | jenv reads that file name                                               | `grep -n "java-version" "$(brew --prefix jenv)/libexec/libexec/jenv-version-file"`                                                                                                                                                   | lines 9–10 test and echo `${root}/.java-version`                                                        |
| 4   | jenv refreshes `JAVA_HOME` per prompt                                   | `cat ~/.jenv/plugins/export/etc/jenv.d/init/export_jenv_hook.zsh`; `jenv plugins`                                                                                                                                                    | `precmd_functions+=_jenv_export_hook`, which exports `$(jenv javahome)`; `export` is in the plugin list |
| 5   | mise can read the same name, only when idiomatic files are enabled      | `strings "$(command -v mise)" \| grep -o 'core:java[^ ]*'`; `mise settings get idiomatic_version_file_enable_tools`; `MISE_IDIOMATIC_VERSION_FILE_ENABLE_TOOLS=java mise current java` in a directory holding `.java-version` = `21` | `core:java.java-version.sdkmanrc`; `[]`; `21.0.13+9.0.LTS`                                              |
| 6   | The wrapper prefers `JAVA_HOME`, then `PATH`                            | `sed -n '119,135p' gradlew`                                                                                                                                                                                                          | `if [ -n "$JAVA_HOME" ] ; then … JAVACMD=$JAVA_HOME/bin/java … else JAVACMD=java`                       |
| 7   | The toolchain is declared apart from both                               | `sed -n '15,18p' build-logic/src/main/kotlin/java-conventions.gradle.kts`; `grep -n '^java' gradle/libs.versions.toml`                                                                                                               | `JavaLanguageVersion.of(libs.versions.java.get())`; `java = "21"`                                       |
| 8   | CI selects its JDK independently                                        | `sed -n '19,23p' .github/workflows/ci.yml`                                                                                                                                                                                           | `actions/setup-java` with `java-version: '21'`                                                          |
| 9   | The README states a Java floor, not a pin                               | `grep -n "Java 21+" README.md`                                                                                                                                                                                                       | the Prerequisites row                                                                                   |
| 10  | The Python selector is documented as personal and ignored               | `grep -n "python-version" .gitignore`; `grep -n "Pyenv users" README.md`                                                                                                                                                             | `.gitignore:12`; the README bullet that follows the Python-analysis one                                 |
| 11  | Nothing in the repository reads `.java-version`                         | `git grep -n '\.java-version' 32c21f5d`; `git grep -n 'java-version-file' 32c21f5d -- .github/workflows`                                                                                                                             | no hits; no workflow uses `actions/setup-java`'s `java-version-file` input                              |

### Claim-scan dismissals

`python3 scripts/claim-scan.py --base origin/main` over this unit's diff yields `count`, `path`, `absolute` and
`cross-ref` candidates, each dismissed by rule rather than by instance, since a hand-written sample of a set that
regenerates on every round drifts by construction: a bare numeral is never a cited claim, so the `count` candidates are
versions, task identifiers and row numbers; a backticked path is a file this change reads or edits, so the `path`
candidates are its sources and its own artifacts; an `absolute` candidate is a prose qualifier or requirement text
quoted while explaining a mechanism; and a `cross-ref` candidate is a compound phrase or this change's own workspace
path, never an unresolved pointer to another artifact.

## Non-Goals

- Changing `.java-version`, the catalog's `java` version, the toolchain convention, or CI.
- Revisiting the merged Python policy.
- Recording the owner's shell configuration, PATH order, or the measured host state.
- Claiming which managers honour the file beyond the two verified here.
