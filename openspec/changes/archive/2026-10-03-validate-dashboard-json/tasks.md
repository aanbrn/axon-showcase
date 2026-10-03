# Tasks

## 1. The verification task

- [x] 1.1 Add `build-logic/src/main/kotlin/DashboardJsonRules.kt` holding the rule (given a set of files, return the
      list of `file → parse-error` failures; treat a missing/empty set as a failure), and
      `build-logic/src/main/kotlin/VerifyDashboardJsonTask.kt` (a cacheable `DefaultTask` with an `@InputFiles`
      `ConfigurableFileCollection` for the dashboards and a `@OutputFile`, following `VerifyInfraImageVersionsTask`).
      Verify by compiling `build-logic`.
- [x] 1.2 Add `build-logic/src/test/kotlin/DashboardJsonRulesTests.kt` covering: valid JSON passes; a truncated file
      fails naming that file; two malformed files are both reported; a missing/empty directory fails. Verify by running
      `./gradlew :build-logic:test` (and confirm the build-logic test formatter/checkstyle gates pass).
- [x] 1.3 Register `verifyDashboardJson` in the root `build.gradle.kts` (pointing at
      `helm/chart/src/main/helm/files/grafana-dashboards`, result file under `build/verification/`) and add
      `dependsOn("verifyDashboardJson")` to the root `check` task. Verify by compiling the root build and running
      `./gradlew verifyDashboardJson`.

## 2. Documentation

- [x] 2.1 Refresh the docs the change falsifies: `README.md`'s verification-command block (`README.md:776-778`, which
      lists `verifyInfraImageVersions`/`verifyInstallCommands`/`verifyModuleDependencies`) and `AGENTS.md`'s
      `check`-note enumeration (`AGENTS.md:500`), adding `verifyDashboardJson` (those are the only two places the
      `verify*` members are listed; the CI sections name none). Remove the implemented idea from `docs/ideas.md` (the
      2026-10-03 "Validate the bundled Grafana dashboard JSON in the build" entry — the section's only content, so
      remove the now-empty `## 2026-10-03` heading too). Verify by reading the edited passages and
      `grep -n "verifyDashboardJson" README.md AGENTS.md` hitting both, and
      `grep -n "Validate the bundled Grafana\|^## 2026-10-03" docs/ideas.md` returning nothing.
- [x] 2.2 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then `./gradlew spotlessCheck` and
      `openspec validate --changes`, and confirm all pass.

## 3. Verification

- [x] 3.1 Prove the check with a known-bad and a known-good input: truncate
      `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json` and confirm `./gradlew verifyDashboardJson`
      (and `check`) fails naming the file; restore it and confirm the task passes. Record both runs' output.
- [x] 3.2 Confirm the check runs inside `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (its `dependsOn` is
      wired) and that the standard `check` is green.
- [x] 3.3 Run the per-unit `lesson-capture` subagent over the change and apply its durable proposals; record the applied
      net `AGENTS.md` delta on this task.
