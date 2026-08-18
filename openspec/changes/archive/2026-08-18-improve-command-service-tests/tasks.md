## 1. Application bean-wiring integration test

- [x] 1.1 Add `ShowcaseCommandApplicationIT` booting the full command-service context with Testcontainers PostgreSQL
      (event store + Flyway)
- [x] 1.2 Assert the `ShowcaseCommandApplication` bean wiring: JGroups connector, primary distributed command bus,
      saga store, caches, snapshot trigger, DB scheduler, and metrics beans
- [x] 1.3 Isolate JGroups on a dedicated bind port/cluster name in the test

## 2. Testcontainers deprecation fix

- [x] 2.1 Replace `org.testcontainers.containers.PostgreSQLContainer` (deprecated in Testcontainers 2.0.5) with the
      non-generic `org.testcontainers.postgresql.PostgreSQLContainer` in the command-service, command-client, and
      api-gateway tests

## 3. Scheduler metrics component test

- [x] 3.1 Add `ShowcaseDbSchedulerMetricsCT` verifying scheduler/candidate/execution event counters and the
      execution counter/timers (with task/deadline/event/result/error tags) against a real Micrometer
      `SimpleMeterRegistry`
- [x] 3.2 Verify the reflection-based execution-lag recording, including its error-handling path
