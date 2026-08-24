# Proposal: Rework the check task and test-suite topology

## Why

`./gradlew check` (and therefore `./gradlew build`) currently runs **every** test suite in every module — including
`e2eTest`, which boots real service containers and triggers four `bootBuildImage` builds on every full build. Best
practice is the opposite: `check` is the fast, frequently-run verification gate, and infra-heavy suites are opt-in.

This change reworks the topology: `integrationTest` stays in `check` (a CQRS reference app verifies against real
PostgreSQL/Kafka/OpenSearch), but `-PskipITs` drops it for Docker-free iteration; `e2eTest` is separated completely out
of `check` as a standalone opt-in task.

## What Changes

- `java-conventions.gradle.kts`: replace the blunt `check dependsOn(testing.suites)` with an explicit membership
  filter — `e2eTest` is never in `check`; `integrationTest` is excluded when `-PskipITs` is present; `test` and
  `componentTest` always run.
- `code-coverage-conventions.gradle.kts`: `allSuiteTestTasks` (used by `jacocoTestReport` /
  `jacocoTestCoverageVerification`)
  respects the same filter — exclude `e2eTest` always and `integrationTest` when `-PskipITs`, so the coverage gate
  does not silently re-pull `integrationTest` into a skipped check.
- Docs: `AGENTS.md` (check pipeline, `-PskipITs`, e2e as a separate task) and `README.md`.

The suite tasks themselves are unchanged: `./gradlew integrationTest` and `./gradlew e2eTest` remain directly
runnable, and the `test → componentTest → integrationTest → e2eTest` ordering still applies when suites run
together.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. Test-tier membership of `check` is a build/convention concern documented in AGENTS.md/README, not a spec-level
behavior. `skip_specs: true`.

## Impact

- **Code**: `build-logic/src/main/kotlin/java-conventions.gradle.kts`,
  `build-logic/src/main/kotlin/code-coverage-conventions.gradle.kts`.
- **Docs**: `AGENTS.md`, `README.md`.
- **Build**: `check` no longer runs `e2eTest` or builds service images by default; `-PskipITs` removes
  `integrationTest`.
- **Tests**: suite contents unchanged; only `check` membership and coverage inputs are re-scoped.