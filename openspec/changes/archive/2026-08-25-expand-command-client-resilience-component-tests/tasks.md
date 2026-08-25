## 1. Add the resilience profile resources

- [x] 1.1 Add `application-timelimiter.yml` and `application-circuitbreaker.yml` to
      `showcase-command-client/src/componentTest/resources/` (modeled on the query-client IT profiles, instance name
      `showcase-command-service`), and verify `./gradlew :showcase-command-client:componentTest` still passes

## 2. Close the measured branch holes

- [x] 2.1 Add a default-profile test asserting a `CommandExecutionException` whose details are not a
      `ShowcaseCommandErrorDetails` propagates unchanged, and verify it exercises the `onErrorMap` else branch
- [x] 2.2 Add retry-profile tests asserting `ShowcaseCommandException`, a direct `AxonNonTransientException`, and a
      `CommandExecutionException` with an `AxonNonTransientException` cause are each dispatched exactly once (not
      retried), and verify `ShowcaseCommandRetryFilter` reaches 100% branches

## 3. Add TimeLimiter and CircuitBreaker behavior tests

- [x] 3.1 Add a `TimeLimiterBehavior` nested class (`@ActiveProfiles("timelimiter")`) covering a hanging dispatch
      timing out and a business error completing without a timeout, and verify the time-limiter profile config applies
- [x] 3.2 Add a `CircuitBreakerBehavior` nested class (`@ActiveProfiles("circuitbreaker")`) covering repeated dispatch
      failures opening the circuit with fail-fast afterward and repeated `ShowcaseCommandException`s leaving the
      circuit closed, and verify the circuit-breaker profile config applies
- [x] 3.3 Rename the existing `Retry` nested class to `RetryBehavior` for consistency with the `<Feature>Behavior`
      convention, and verify `componentTest` still passes

## 4. Verify coverage and change artifacts

- [x] 4.1 Run `./gradlew :showcase-command-client:componentTest` and confirm the JaCoCo report shows
      `ShowcaseCommandClient` and `ShowcaseCommandRetryFilter` at 100% branches
- [x] 4.2 Run `openspec validate` on the change and confirm all planning artifacts are complete with no errors