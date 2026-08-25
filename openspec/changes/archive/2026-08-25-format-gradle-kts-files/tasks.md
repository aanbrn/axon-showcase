## 1. Add the ktfmt formatting step

- [x] 1.1 Add a Spotless `kotlinGradle` step using `ktfmt()` (kotlinlang style, `max_line_length = 120`) targeting
      `**/*.gradle.kts`, wired in `code-check-conventions` (modules) and the root/build-logic projects

## 2. Apply the one-time reformat

- [x] 2.1 Run `./gradlew spotlessApply` to format all `.gradle.kts` files and confirm `spotlessCheck` passes

## 3. Set up IDE parity

- [x] 3.1 Enable the ktfmt IntelliJ plugin (marketplace id 14912) in the IDE, commit its project-level config
      (`.idea/ktfmt.xml` — Custom style reproducing kotlinlang at 120, since Kotlinlang mode hard-codes 100 columns),
      and extend `scripts/setup-idea.sh` to install the ktfmt plugin alongside `palantir-java-format`

## 4. Refresh the docs

- [x] 4.1 Update `AGENTS.md` Formatting convention to cover `.gradle.kts` and `README.md` as needed

## 5. Verify the change artifacts

- [x] 5.1 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors