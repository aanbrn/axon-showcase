# Design: Upgrade db-scheduler to 16.x

## Context

See proposal.md — Why. Current state relevant to the approach:

- `gradle/libs.versions.toml` pins `db-scheduler = 13.0.0`; Axon 4.13.2's BOM declares db-scheduler 16.6.0 and its
  integration (`DbSchedulerEventScheduler` / `DbSchedulerDeadlineManager` in `axon-messaging`, wired by
  `AxonDbSchedulerAutoConfiguration`) compiles against the 16.x builder API (`TaskDescriptor.instance(id).data(data)`
  `.build()`), which does not exist in 13.0.0. Verified: the command-service IT throws `NoSuchMethodError` on the
  `EventProcessor[showcase-saga]` thread whenever a deadline is scheduled.
- The saga (`ShowcaseSaga`) schedules "startShowcase"/"finishShowcase" deadlines via `DeadlineManager.schedule(...)`;
  these currently never fire.
- `ShowcaseSagaCT` uses `SagaTestFixture` (mocks `DeadlineManager`); IT/E2E tests start and finish showcases explicitly.
- The `db-scheduler.*` keys in the command service's `application.yml` (delay-startup-until-context-ready, threads,
  polling-interval PT0.5S, polling-strategy lock_and_fetch) are all still supported in 16.x.

## Goals / Non-Goals

**Goals:**
- Restore the spec'd saga deadline behavior: auto-start at the scheduled time, auto-finish after the duration.
- Prove it with integration tests that exercise the real scheduler and fail loudly on regression.
- Keep the command service on the Spring Boot 3 starter (`db-scheduler-spring-boot-starter`), unchanged artifact, with
  only the catalog version bumped.

**Non-Goals:**
- No changes to saga logic, deadline names, or lifecycle semantics.
- No new db-scheduler configuration surface; existing `application.yml` keys stay as-is.

## Decisions

### D1: Target db-scheduler 16.12.0

Choose the highest 16.x available for the Spring Boot 3 starter (`db-scheduler-spring-boot-starter`), which tops out
at 16.12.0. Axon 4.13.2 requires ≥16.6.0, satisfied by 16.12.0.

Alternatives considered:
- 16.6.0 (Axon's exact BOM pin) — rejected: older than the newest available patch in the same 16.x line.
- 16.7.0 — rejected: unnecessary divergence from the newest available patch for no gain.

### D2: Keep the SB3 starter artifact unchanged

The command service stays on `db-scheduler-spring-boot-starter`; only the catalog version changes. The
`DbSchedulerCustomizer` / `DbSchedulerProperties` classes used by `ShowcaseCommandApplication` moved from the starter
jar to the `db-scheduler-spring-common` artifact in 16.x, which the starter pulls transitively; their API
(`executorService()`, `getThreads()`) is unchanged, so the customizer compiles as-is.

### D3: Verify deadline firing with a real-scheduler integration test

Add `ShowcaseSagaDeadlinesIT` (Testcontainers PostgreSQL + Kafka, mirroring `ShowcaseCommandGatewayIT`) that drives the
full saga path:

- **Auto-start**: schedule a showcase with `startTime = now + ~2s` (validation requires startTime in the future) and the
  minimum valid duration, then await the aggregate reaching STARTED — observing `ShowcaseStartedEvent` on the local
  event bus or querying the aggregate from the repository. Assert it happens without any caller-dispatched start
  command. Expected latency ≈ polling interval (PT0.5S) plus processing, so a ~10s await suffices.
- **Auto-finish**: in the same flow (started by the auto-start deadline), await FINISHED after the duration elapses.
  Duration is validated to 1–10 minutes inclusive, so the finish wait is ~60s for the minimum duration. This is the
  slow part; keep it as one test that covers the whole lifecycle so the ~70s total is paid once.

The assertions prove the deadlines fire through the persistent scheduler (not a mock): the saga thread's
`DeadlineManager` bean is the real `DbSchedulerDeadlineManager` created by `AxonDbSchedulerAutoConfiguration`.

Alternatives considered:
- Awaiting only that the deadline row appears in the db-scheduler table — rejected: proves scheduling, not firing; the
  auto-start test already proves firing cheaply.
- Component test asserting a real scheduler — rejected: `SagaTestFixture` deliberately fakes the `DeadlineManager`;
  a real scheduler needs a database, which is the integration tier.

### D4: Keep `ShowcaseSagaCT` unchanged

The component test remains on `SagaTestFixture` — it verifies saga decision logic (which deadlines are scheduled for
which events), while the new IT verifies the real scheduler actually executes them. Both tiers stay in place.

### D5: No DB migration — the existing schema already matches 16.x

Verified (details in the Migration Plan below): the project's own Flyway migration
`V0_1_0_3__db-scheduler.sql` already contains the `priority` column and `priority_execution_time_idx` index that
db-scheduler 16.x requires, and 16.x touches no column the migration lacks. No new Flyway migration is needed.

## Risks / Trade-offs

- [db-scheduler 16.x changed internal scheduling semantics not exercised by these tests] → Mitigation: the Axon
  integration only calls `schedule`/`cancel`/`start`/`stop`/`getSchedulerState` plus the builder API, all verified
  present in 16.6.0 and 16.12.0; the new IT covers the exact call path. Note `Scheduler.reschedule` returns `boolean`
  in 16.x vs `void` in 13.0.0 — not called by the Axon integration, so no impact.
- [New IT adds ~70s to the command-service integration suite] → Mitigation: acceptable one-time cost for restoring and
  locking in broken runtime behavior; suite still runs in parallel containers.
- [Reverting to 13.0.0 after this change restores the broken runtime] → Mitigation: rollback is a one-line catalog
  revert; the broken state is the current baseline, so no behavior regresses beyond today.

## Migration Plan

1. Bump `db-scheduler` to 16.12.0 in `gradle/libs.versions.toml`.
2. Compile the command service (verifies `DbSchedulerCustomizer`/`DbSchedulerProperties` against 16.x).
3. Add `ShowcaseSagaDeadlinesIT` and run the command-service `integrationTest` suite.
4. Run the full `check` for the command service (test → componentTest → integrationTest).

Rollback: revert the catalog version to 13.0.0. No data migration required (see D5).

### Schema verification

The `scheduled_tasks` table is created by the project's own Flyway migration
`showcase-command-service/src/main/resources/db/migration/postgresql/V0_1_0_3__db-scheduler.sql`, not by db-scheduler
(the db-scheduler jars ship no DDL; the schema is defined in the project). Comparing the migration against db-scheduler
16.12.0's PostgreSQL reference schema (`db-scheduler/src/test/resources/postgresql_tables.sql` at `v16.12.0`):

- The 16.x schema adds two things relative to 13.0.0: the `priority SMALLINT` column and the
  `priority_execution_time_idx` index. Both are **already present** in `V0_1_0_3__db-scheduler.sql`, because the
  migration was written against the newer db-scheduler schema.
- The 16.x runtime (`JdbcTaskRepository`) references exactly the columns the migration defines — `task_name`,
  `task_instance`, `task_data`, `execution_time`, `picked`, `picked_by`, `last_heartbeat`, `consecutive_failures`,
  `version`, and `priority` (the latter only when `enablePriority()` is on, which is off by default and the column is
  nullable anyway). No `update_attempt` or `scheduler_name` columns are required.
- `task_data` is `BYTEA`, matching 16.x's serialization expectations; the schema is unchanged from 13.0.0 in every
  column the runtime touches.

No new Flyway migration is needed for the upgrade. The command-service IT booting against a fresh Testcontainers
PostgreSQL applies `V0_1_0_3` and starts the scheduler; after the version bump that same boot exercises the 16.x
runtime against the unchanged schema, which the new `ShowcaseSagaDeadlinesIT` asserts end-to-end.

## Open Questions

None — the version, artifact, and test strategy are decided above and do not affect the spec delta or task breakdown.