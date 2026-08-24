# Design: Rework the check task and test-suite topology

## Context

See proposal.md — Why. `check` currently aggregates every suite (unit, component, integration, e2e) via
`dependsOn(testing.suites)`, so the default build runs e2e and builds all service images. This change re-scopes
`check` to the fast gate and makes the infra-heavy suites explicit.

## Goals / Non-Goals

**Goals**

- `check` runs the fast gates plus `integrationTest` by default; `-PskipITs` drops integration for Docker-free runs.
- `e2eTest` is fully out of `check` — no image builds and no full-system runs on a default `./gradlew build`.
- Coverage inputs match what `check` actually runs (no silent re-pull of skipped suites).
- The suite ordering guarantees survive when suites run together.

**Non-Goals**

- Renaming or restructuring the test tiers themselves (unit/component/integration/e2e) — the topology of *check*
  membership changes, not the suites.
- Adding new aggregate tasks — `./gradlew e2eTest` already runs all e2e suites across modules.

## Decisions

- **`integrationTest` stays in `check` by default; `-PskipITs` excludes it.** This is the pragmatic middle for a
  CQRS reference app: verifying against real PostgreSQL/Kafka/OpenSearch is the point (Docker is a documented
  prerequisite), while the property gives a fast, Docker-free local `check`. The property is **presence-based**
  (`-PskipITs` or `-PskipITs=true`), matching the spirit of Maven's `skipITs`.
- **`e2eTest` is completely separate from `check`.** The suite task remains the entry point
  (`./gradlew e2eTest`), and its `bootBuildImage`/ordering configuration is untouched — those dependencies simply
  no longer fire on a default build. No new aggregate task.
- **Share one "check-runnable suite" predicate between `check` and coverage.** `java-conventions` uses it to wire
  `check`; `code-coverage-conventions` uses it to scope `allSuiteTestTasks` (exclude `e2eTest` always, exclude
  `integrationTest` when `skipITs`). Rationale: prevents the coverage gate from re-pulling a skipped suite into
  `check`, which is the classic coupling this rework must break. Note: implemented as an identical `skipITs` provider
  expression in each convention rather than a shared symbol — a shared top-level extension property triggered a Kotlin
  compiler IR-lowering crash, so the predicate is duplicated (semantically identical) in the two files.
- **Ordering is preserved.** `shouldRunAfter(test)` / `mustRunAfter(...)` remain on the suite tasks, so when
  integration and e2e run (in `check` and standalone respectively), the documented order holds.

## Risks / Trade-offs

- [A default `check` no longer verifies the command → Kafka → projection → query pipeline] → Mitigation: e2e
  remains one explicit command (`./gradlew e2eTest`); the docs and AGENTS.md present it as a first-class, opt-in
  step
- [Coverage numbers differ between `check` and `check -PskipITs`] → Mitigation: coverage follows what `check` runs;
  the difference (integration coverage) is expected and documented.

## Migration Plan

1. Add the check-runnable-suite predicate to `java-conventions` and rewire `check`.
2. Apply the same predicate to `allSuiteTestTasks` in `code-coverage-conventions`.
3. Verify: `./gradlew check` includes `integrationTest`; `./gradlew check -PskipITs` excludes it (and the coverage
   gate does not re-pull it); `./gradlew e2eTest` still runs e2e with image builds; `./gradlew build` runs no e2e.
4. Update `AGENTS.md` (check pipeline, `-PskipITs`, e2e as a separate task) and `README.md`.
5. Rollback: restore `check dependsOn(testing.suites)` and the original `allSuiteTestTasks` filter.

## Open Questions

None — the `-PskipITs` naming, e2e-as-a-task separation, and coverage re-scoping are settled.