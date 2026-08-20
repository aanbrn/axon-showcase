## Why

The build runs Gradle 8.14.5, whose embedded Kotlin (2.0.21) can no longer keep pace with the plugin ecosystem:
the maintained continuation of the helm Gradle plugins, `build-extensions-oss` 3.x, is compiled with Kotlin 2.3.0
metadata that Gradle 8 cannot read. Because the old `com.citi.helm` / `com.citi.helm-releases` plugins (2.2.0) were
archived upstream on 2026-01-16 and depend on deprecated Gradle APIs slated for removal, migrating to the maintained
fork — and the Gradle version it requires — unblocks both the Gradle 9 upgrade and future plugin compatibility.

## What Changes

- Bump the Gradle wrapper from 8.14.5 to 9.7.1 (the latest stable; embedded Kotlin 2.4.0), required to run the fork's
  Kotlin-2.3.0-compiled helm plugins.
- Replace the helm plugin dependency in `gradle/libs.versions.toml`:
  - `com.citi.helm` 2.2.0 → `io.github.build-extensions-oss.helm` 3.1.2
  - `com.citi.helm-releases` 2.2.0 → `io.github.build-extensions-oss.helm-releases` 3.1.2
- Update the two convention plugins that apply the plugins:
  - `build-logic/src/main/kotlin/helm-conventions.gradle.kts` (applies `io.github.build-extensions-oss.helm`)
  - `build-logic/src/main/kotlin/helm-releases-conventions.gradle.kts` (applies
    `io.github.build-extensions-oss.helm-releases`)
- Update the `HelmChart` import in `helm/chart/build.gradle.kts` to the fork's package
  (`io.github.build.extensions.oss.gradle.plugins.helm.dsl.HelmChart`).
- Verify the `helm {}` DSL blocks in the root `build.gradle.kts` and `helm/chart/build.gradle.kts` (charts, lint
  configurations, repositories, filtering, releases, release targets) survive unchanged.
- No chart templates, values, or rendered chart behavior change; the linting/package/install task contract is
  preserved exactly (`helmLintMainChartFull`, `helmLintMainChartMinimal`, `helmPackageMainChart`,
  `helmInstallToLocal`).

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. This is a pure tooling change: the chart's behavior and its build/lint/install contract are preserved, only the
  Gradle version and the Gradle plugin implementing them are changed. The change therefore sets `skip_specs: true`.

## Impact

- `gradle/wrapper/gradle-wrapper.properties` — Gradle distribution URL 8.14.5 → 9.7.1.
- `gradle/libs.versions.toml` — helm plugin coordinates and versions.
- `build-logic/src/main/kotlin/helm-conventions.gradle.kts` and `helm-releases-conventions.gradle.kts` — plugin IDs.
- `helm/chart/build.gradle.kts` — `HelmChart` import package.
- `build.gradle.kts` — the `helm { releases { ... } }` DSL and the `chart(":helm:chart", "main")` reference must be
  verified against the fork's DSL (the `chart(project = ..., chart = ...)` function survives, per the fork docs).
- AGENTS.md and `openspec/config.yaml` — the configuration-cache gotcha updated to name the new plugin (the fork is
  also not configuration-cache compatible, so the constraint stands).
- Gradle 9.7.1 deprecation warnings: the Kotlin DSL `by getting` / `by register` test-suite delegate APIs are
  deprecated and will be removed in Gradle 10 (a separate follow-up, not addressed here).