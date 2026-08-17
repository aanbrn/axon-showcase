# Tasks: Upgrade db-scheduler to 16.x

## 1. Dependency upgrade

- [x] 1.1 Bump `db-scheduler` from `13.0.0` to `16.12.0` in `gradle/libs.versions.toml` (keep the
      `dbScheduler-springBootStarter` artifact as `db-scheduler-spring-boot-starter`)
- [x] 1.2 Verify the command service compiles against 16.x, confirming `DbSchedulerCustomizer` /
      `DbSchedulerProperties` (now from the transitively pulled `db-scheduler-spring-common`) still resolve
- [x] 1.3 Confirm the existing `V0_1_0_3__db-scheduler.sql` migration needs no changes (it already defines the
      `priority` column and `priority_execution_time_idx` index db-scheduler 16.x expects); verified by booting the
      command service against a fresh Testcontainers PostgreSQL (the IT boots the full context and the scheduler runs)
- [x] 1.4 Run `./gradlew :showcase-command-service:test :showcase-command-service:componentTest` to confirm no
      regression in unit/component tiers

## 2. Saga deadline integration tests

- [x] 2.1 Add `ShowcaseSagaDeadlinesIT` in `showcase-command-service/src/integrationTest/java/showcase/command/`
      mirroring `ShowcaseCommandGatewayIT` (Testcontainers PostgreSQL + Kafka, `@DirtiesContext`)
- [x] 2.2 Implement the auto-start scenario: schedule a showcase with `startTime = now + ~2s` and the minimum valid
      duration, await the aggregate reaching STARTED (via `ShowcaseStartedEvent` on the local event bus or repository
      query) without dispatching any start command manually
- [x] 2.3 Implement the auto-finish scenario: in the same flow, await the aggregate reaching FINISHED after the
      duration elapses (minimum valid duration, ~60s wait) without dispatching any finish command manually
- [x] 2.4 Confirm the assertions exercise the real `DbSchedulerDeadlineManager` (deadlines persisted and fired through
      the scheduler, not a mock)
- [x] 2.5 Run `./gradlew :showcase-command-service:integrationTest` and verify the new IT passes on 16.x

## 3. Verification

- [x] 3.1 Run the full command-service `check` (test → componentTest → integrationTest) and confirm green