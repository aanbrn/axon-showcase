## Context

See proposal.md - Why. The build is migrating to Gradle 9. The `com.citi.helm` / `com.citi.helm-releases` plugins
(2.2.0) were archived upstream on 2026-01-16; their maintained continuation, the `build-extensions-oss` fork, is
compiled with Kotlin 2.3.0 metadata that the previous Gradle 8.14.5 (embedded Kotlin 2.0.21) cannot read. Migrating the
helm plugin to the fork therefore forces a Gradle bump, and vice versa: the two are coupled. The chart's templates,
values, and rendered behavior must not change; only the Gradle version and the Gradle plugin implementing the chart's
build tasks change.

## Goals / Non-Goals

- **Goals**: Migrate the build to Gradle 9.7.1; replace the archived helm plugins with the maintained
  `build-extensions-oss` fork at 3.1.2; preserve the exact task contract (`helmLintMainChart` + `Full`/`Minimal`
  configurations, `helmPackageMainChart`, `helmInstallToLocal`, release ordering via `mustInstallAfter`); keep the
  whole build compiling and configuring on 9.7.1.
- **Non-Goals**: Not adopting the configuration cache build-wide; not changing any chart template, value, or rendered
  resource; not altering the release set or its ordering; not addressing the Gradle-10 deprecation warnings (the
  `by getting` / `by register` test-suite DSL) surfaced by the upgrade.

## Decisions

- **Adopt Gradle 9.7.1 (the latest stable).**
  - Alternatives: stay on 8.14.5 (cannot run the fork's Kotlin-2.3.0 helm plugins); bump to Gradle 9.0–9.3 (embedded
    Kotlin 2.2.x still cannot read 2.3.0 metadata); bump to 9.4+ (minimum that works). Picking the newest stable 9.7.1
    (embedded Kotlin 2.4.0) gets the latest ecosystem fixes and also resolves the build-logic's own `kotlin-gradle-plugin`
    2.4.10 metadata (readable by Kotlin 2.4.0).
- **Adopt `io.github.build-extensions-oss.helm` / `io.github.build-extensions-oss.helm-releases` 3.1.2.**
  - Alternatives: stay on `com.citi.*` 2.2.0 (upstream archived, depends on APIs Gradle 9 removes); use the fork's
    2.2.0 re-release (byte-identical code, no benefit); maintain a private fork (ongoing maintenance burden). The
    fork's 3.1.x is the only actively maintained, Gradle-9-compatible option.
- **Keep the migration confined to the build tooling.**
  - The fork preserves the `helm {}` DSL surface (`charts`, `lint` + `configurations`, `repositories`,
    `filtering.values`, `releases` with `mustInstallAfter`, `releaseTargets`, `selectTags`) and the
    `chart(project, chart)` function. The code impact is limited to the wrapper URL, the catalog coordinates, the two
    convention plugins, and the `HelmChart` import package in `helm/chart/build.gradle.kts`.
- **Do not enable the configuration cache.**
  - The fork, like citi before it, is not configuration-cache compatible (it serializes `Project`/`Task` at execution
    time). The AGENTS.md / `openspec/config.yaml` gotcha is retained, updated to name the new plugin.

## Risks / Trade-offs

- [The fork's 3.1.x moved packages; the `HelmChart` type and any DSL drift must be verified] → Mitigation: keep the
  DSL usage identical, run the strict lint tasks and package the chart to confirm output.
- [The fork is not configuration-cache compatible (verified by smoke test)] → Mitigation: keep the AGENTS.md / config
  constraint; Gradle 9 does not enable configuration cache by default, so this only matters for a future Gradle-10 /
  CC-adoption change.
- [Gradle 9.7.1 deprecates the `by getting` / `by register` Kotlin DSL test-suite delegates] → Mitigation: out of scope
  here; tracked as a Gradle-10-readiness follow-up (warnings only, build still succeeds).
- [The ben-manes `dependencyUpdates` task fails on Gradle 9+ under `org.gradle.parallel=true`] → Mitigation: unrelated
  to this change's tasks; run it with `--no-parallel` when used.

## Migration Plan

1. Bump the wrapper to 9.7.1; swap the catalog coordinates and the two convention plugins; update the `HelmChart`
   import.
2. Verify the whole build configures (`./gradlew help`) and compiles (`./gradlew compileJava`) on 9.7.1.
3. Run the strict lint gate (`helmLintMainChartFull`, `helmLintMainChartMinimal`), package the chart, and confirm the
   fork's filtering produces the same chart.
4. Smoke-test the configuration cache; update the AGENTS.md / config gotcha to the new plugin's status.
5. Rollback: revert the wrapper URL and the catalog coordinates / plugin IDs (a pure dependency swap).

## Open Questions

- None. The fork's release-DSL calls (`chart(project, chart)`, `waitForJobs`, `tags`, `valuesDir`,
  `mustInstallAfter`) were verified to map 1:1 during implementation.