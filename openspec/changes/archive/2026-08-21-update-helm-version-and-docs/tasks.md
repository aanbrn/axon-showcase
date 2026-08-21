## 1. Update Helm version and docs

- [x] 1.1 In `gradle/libs.versions.toml`, bump `helm = "4.2.3"` to `helm = "4.2.4"`.
- [x] 1.2 In `README.md`, update the "Helm 3.x" prerequisite to "Helm 4.x".
- [x] 1.3 In `AGENTS.md`, update the "Helm 3.x" prerequisite to "Helm 4.x".

## 2. Verification

- [x] 2.1 Refresh the build model and run `./gradlew :helm:chart:helmLintMainChart` to confirm the
      bumped Helm CLI (4.2.4) lints the chart cleanly.
- [x] 2.2 Confirm `git diff` touches only `gradle/libs.versions.toml`, `README.md`, and `AGENTS.md`.
