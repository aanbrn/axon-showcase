# Run end-to-end tests in CI on a schedule

## Why

The `merge-governance` change added the PR fast gate and the `main` full gate, but explicitly deferred the heavy
gates: `e2eTest` (builds all four service images and boots the full pipeline) and `dependencySecurityCheck` (Snyk)
are never run automatically. Nothing catches cross-service wiring drift — dependency bumps, OpenSearch/Kafka image
version catalog changes, or inter-service timing regressions — between human-triggered runs. A scheduled e2e run
closes that gap with a system-level check over the real built images.

## What Changes

- Add a new `.github/workflows/e2e.yml` workflow (separate from `ci.yml`, which stays the merge gate):
  - **Triggers**: `workflow_dispatch` (manual) and a nightly `schedule` (cron) — not `pull_request` and not
    `push` to `main`, so the heavy run never blocks or couples to the merge gate.
  - **Job**: single `e2e` job on `ubuntu-latest` with Temurin JDK 21; runs `./gradlew
    :showcase-api-gateway:e2eTest` (which builds all four service images via `bootBuildImage` dependencies, then
    boots the full pipeline in Testcontainers).
  - **No secrets**: images are built locally and referenced by the fixed `project.version` (`0.1.0-SNAPSHOT`), and
    the test JVM receives `project.version` via the existing `java-conventions.gradle.kts` test system property —
    no image push, no registry credentials.
  - **Cache**: `gradle/actions/setup-gradle` restores the Gradle User Home (dependencies, wrapper, build cache),
    never the workspace `build/` directories (same rule as `ci.yml`).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/merge-governance`: adds the requirement that the end-to-end test suite runs on a schedule and on
  demand, and that it is not part of the merge gate.

## Impact

- **New files**: `.github/workflows/e2e.yml`.
- **GitHub config**: no ruleset changes — the e2e run is observational (schedule/manual), never a required check.
- **Build/test**: no Gradle task changes — the workflow invokes the existing `:showcase-api-gateway:e2eTest` task and
  its existing `bootBuildImage` dependencies.
- **Cost**: nightly run builds four service images (~10-15 min on the runner); acceptable for a scheduled check and
  only on demand thereafter.
- **Secrets**: none required.