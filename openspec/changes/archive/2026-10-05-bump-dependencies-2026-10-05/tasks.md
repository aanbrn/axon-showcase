# Tasks

## 1. Apply the bumps

- [x] 1.1 Bump the JVM catalog pins in `gradle/libs.versions.toml`: `logback` 1.6.5, `zstd-jni` 1.5.7-21, `guava`
      33.7.2-jre, `commons-lang3` 3.21.0, `jgroups` 4.2.32.Final, `opensearch-client-rest` 3.9.0, `swagger-ui` 5.33.1.
      Verify by reading the catalog. Done: seven `[versions]` edits; `git diff` shows exactly those lines.

## 2. Verification

- [x] 2.1 Run the full `./gradlew check` (with integration tests — Docker is required) and confirm it passes; a
      runtime-path bump can be binary-incompatible while the Docker-free check passes. Done: `BUILD SUCCESSFUL` (3m 14s,
      382 tasks); the four services' `integrationTest` tasks executed (projection, query, command, gateway).
- [x] 2.2 Re-run `./gradlew dependencyUpdates` and confirm the bumped coordinates no longer appear in the report's
      actionable section (only the excluded `log4j-core` row remains). Done: the report now lists only
      `org.apache.logging.log4j:log4j-core [2.17.1 -> 2.26.1]` (the ADR-0007 spurious row).
- [x] 2.3 Run the implementation `review-quick` loop over the diff; fix its findings and re-run until it reports nothing
      new. Done: clean on the first round (no findings).
- [x] 2.4 Run the per-unit `lesson-capture` over this change and apply its durable proposals; record the applied net
      `AGENTS.md` delta. Done: net 0 — the one candidate (formatting the change-dir markdown before the first `check`)
      is already covered by the `Formatting` convention and `spotlessMarkdownCheck`; no additions or retirements.
- [x] 2.5 Run `./gradlew spotlessApply` after the last edit and confirm `spotlessCheck` passes; run the manual
      120-character check over the lines this change introduces. Done (final pass after the task ticks).
- [x] 2.6 Request the user's manual review pass — the step before committing. Done: the user approved the
      implementation.
