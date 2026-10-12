# Tasks

## 1. Catalog bumps

- [x] 1.1 Bump the ten stable catalog coordinates in `gradle/libs.versions.toml` (`jetty`, `netty-bom`, `grpc`,
      `elasticsearch-client-java`, `httpclient5`, `opentelemetry-bom`, `zstd-jni`, `kotlin`, `spotbugs`,
      `spotless-plugin`) to the versions the re-resolved `dependencyUpdates` report names.
- [x] 1.2 Run `./gradlew spotlessApply` to absorb the Spotless plugin bump's reflow, and account for the reflowed files
      in the change (not as "no source change").

## 2. Web UI npm

- [x] 2.1 Run the web UI's npm update (`./gradlew :showcase-web-ui:npm_update`) and describe the lockfile diff by what
      moved, not as "patches/minors".

## 3. Gradle wrapper

- [x] 3.1 Bump the wrapper to `9.8.1` with two `./gradlew wrapper --gradle-version 9.8.1 --distribution-type all` runs
      (the second executes under the new version and regenerates the jar), then diff all four wrapper files and confirm
      the distribution stays `-all`.

## 4. Verification

- [x] 4.1 Run the full `./gradlew check` (integration tests on Testcontainers) and `./gradlew :showcase-web-ui:build`;
      confirm green.
- [x] 4.2 Re-run the reports (`dependencyUpdates`, `:showcase-web-ui:npmOutdated`) and confirm the bumped coordinates no
      longer appear (the `log4j-core` row excepted).
- [x] 4.3 Run a `review-quick` pass over the proposal and the implementation, repeating until clean.

## 5. Capture

- [x] 5.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: **net +6 rendered lines** (the raw churn is larger — the Docs-refresh
      paragraph reflows) — (a) a clause on the Docs-refresh convention: a version bump makes stale every live doc that
      names the bumped version, including a coordinate an ADR or idea quotes, so grep the old string across the five doc
      surfaces; (b) a clause on the `opencode debug config` credential rule: `./gradlew --info`/`--debug` logs each
      exec'd process's environment, so never persist a verbose log into an artifact. It also parked the Gradle `--info`
      environment-dump note in `docs/ideas.md` for an upstream report. No retirements proposed.
- [x] 5.2 Ask the owner for the manual review pass, after 4.3 is clean and 5.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
- The Helm, buildpack, and Snyk-tooling findings are separate units.
