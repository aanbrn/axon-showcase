## 1. Bump the CLI pins

- [x] 1.1 Bump `helm` from `4.2.4` to `4.3.0` in `gradle/libs.versions.toml` and verify `./gradlew helmUpdates` reports
      no remaining Helm CLI update
- [x] 1.2 Bump `snyk-version` from `v1.1307.0` to `v1.1307.2` in `.github/workflows/snyk.yml` and verify the workflow
      lints cleanly (`./gradlew workflowLint`)

## 2. Verify the bumped Helm client

- [x] 2.1 Verify the build's Helm tasks still work with the 4.3.0 client: run the chart lint
      (`./gradlew :helm:chart:helmLintMainChart`) and `verifyInfraImageVersions` (part of `check`), and confirm
      `openspec validate --changes` passes
