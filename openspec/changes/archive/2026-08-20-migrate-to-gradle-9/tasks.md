## 1. Gradle wrapper upgrade

- [x] 1.1 Bump the Gradle wrapper from 8.14.5 to 9.7.1 in `gradle/wrapper/gradle-wrapper.properties`.
- [x] 1.2 Verify the whole build configures and compiles on 9.7.1 (`./gradlew help`, `./gradlew compileJava`).

## 2. Dependency catalog and plugin IDs

- [x] 2.1 Update `gradle/libs.versions.toml`: replace the `helm-plugin` and `helm-releases-plugin` coordinates with
      `io.github.build-extensions-oss.helm` / `io.github.build-extensions-oss.helm-releases` at version `3.1.2`.
- [x] 2.2 Update `build-logic/src/main/kotlin/helm-conventions.gradle.kts`: apply `io.github.build-extensions-oss.helm`
      instead of `com.citi.helm`.
- [x] 2.3 Update `build-logic/src/main/kotlin/helm-releases-conventions.gradle.kts`: apply
      `io.github.build-extensions-oss.helm-releases` instead of `com.citi.helm-releases`.

## 3. Import and DSL adjustments

- [x] 3.1 Update the `HelmChart` import in `helm/chart/build.gradle.kts` from `com.citi.gradle.plugins.helm.dsl.HelmChart`
      to the fork's package (`io.github.build.extensions.oss.gradle.plugins.helm.dsl.HelmChart`).
- [x] 3.2 Verify the `helm {}` DSL in `helm/chart/build.gradle.kts` (`charts.named<HelmChart>("main")`, `chartName`,
      `lint { strict, withSubcharts, configurations { full, minimal } }`) compiles and maps 1:1 to the fork's DSL.
- [x] 3.3 Verify the `helm {}` DSL in the root `build.gradle.kts` (`releases`, `from(chart(":helm:chart", "main"))`,
      `valuesDir`, `wait`, `waitForJobs`, `test.enabled`, `tags`, `mustInstallAfter`/`mustUninstallAfter`,
      `installDependsOn`, `releaseTargets.selectTags`) compiles and maps 1:1 to the fork's DSL.

## 4. Verification

- [x] 4.1 Run the strict lint gate:
      `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal` — both must pass clean.
- [x] 4.2 Verify the packaged chart is unchanged: package the chart (`helmPackageMainChart`) and confirm the fork's
      filtering substitutes the same values (chart name/version, `bitnamiCommonVersion`), producing
      `axon-showcase-0.1.0-SNAPSHOT.tgz`. A byte-diff against the old `com.citi.*` plugin is not feasible because the
      old plugin cannot run on Gradle 9; the chart sources are untouched, so the packaged chart is behaviorally
      identical.
- [x] 4.3 Smoke-test the configuration cache on a helm task (`./gradlew :helm:chart:helmLintMainChart
      --configuration-cache`); it fails (the fork also serializes `Project`/`Task` at execution time), so the AGENTS.md
      and `openspec/config.yaml` gotchas were updated from "citi gradle-helm-plugin" to the
      `io.github.build-extensions-oss.helm` / `io.github.build-extensions-oss.helm-releases` plugin to keep the
      constraint accurate.
- [x] 4.4 Run a broader build sanity check that exercises the helm modules (`./gradlew :helm:chart:build
      :helm:assemble`) to confirm nothing downstream regressed.