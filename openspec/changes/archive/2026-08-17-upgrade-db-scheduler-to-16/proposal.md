# Proposal: Upgrade db-scheduler to 16.x

## Why

Axon 4.13.2's db-scheduler integration (bumped in `d46a33e`) requires the db-scheduler 16.x API —
`TaskDescriptor.instance(id).data(data).build()` — but the project still resolves `db-scheduler` 13.0.0. At runtime the
saga's `DeadlineManager.schedule(...)` calls throw `NoSuchMethodError` on the `EventProcessor[showcase-saga]` thread,
so **scheduled showcases are never auto-started and started showcases are never auto-finished**. Nothing catches this:
the component test mocks the `DeadlineManager`, and the integration/E2E tests start and finish showcases explicitly.
The write-side spec already asserts the deadline behavior, so spec and runtime are out of sync. This change restores
the spec'd behavior.

## What Changes

- Upgrade the `db-scheduler` catalog entry from `13.0.0` to `16.12.0` (the highest 16.x available for the Spring Boot 3
  starter, keeping the artifact name unchanged).
- Verify the command service's `DbSchedulerCustomizer` / `DbSchedulerProperties` usage still compiles: those classes
  moved from the starter jar to the new `db-scheduler-spring-common` artifact, which the starter pulls transitively;
  their API (`executorService()`, `getThreads()`) is unchanged.
- Add integration tests that prove the saga deadlines actually fire through the real scheduler (auto-start at the
  scheduled time, auto-finish after the duration), so the broken path is covered by a test that fails loudly if the
  runtime ever regresses.
- Keep the `db-scheduler.*` configuration keys in `application.yml` unchanged (they are all still supported by 16.x).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/write-side/command-service`: strengthen the "Saga deadlines and termination" requirement and the
  "Scheduled showcase is started automatically by its deadline" / "Started showcase is finished automatically by its
  deadline" scenarios so they assert the deadlines fire through the real scheduler and the lifecycle transitions
  actually happen without manual intervention.

## Impact

- `gradle/libs.versions.toml` — `db-scheduler` version `13.0.0` → `16.12.0`.
- `showcase-command-service/build.gradle.kts` — no change (stays `db-scheduler-spring-boot-starter`).
- `showcase-command-service/src/main/java/showcase/command/ShowcaseCommandApplication.java` — `DbSchedulerCustomizer`
  bean unchanged (API preserved), only compile-verified against 16.x.
- `showcase-command-service` integration tests — new tests exercising the real saga deadline path via Testcontainers
  (PostgreSQL + Kafka).
- Runtime behavior: scheduled showcases resume being auto-started/auto-finished (currently broken).