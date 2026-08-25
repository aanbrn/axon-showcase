# Design: Expand command-client resilience component tests

## Context

See proposal.md — Why. The existing `ShowcaseCommandClientCT` fakes the command bus with `@MockitoBean CommandBus` and
covers Retry only. The query-client IT demonstrates the three-profile pattern (default disables Resilience4j; each
behavior enabled under its own profile with a small deterministic config).

## Goals / Non-Goals

**Goals:**
- Reach 100% branch coverage on `ShowcaseCommandClient` and `ShowcaseCommandRetryFilter` via the existing CT.
- Add TimeLimiter and CircuitBreaker behavior coverage for the command client, mirroring the query-client IT's nested
  `TimeLimiterBehavior`/`CircuitBreakerBehavior` structure.
- Exercise the auto-config's circuit-breaker `ignoreExceptions(ShowcaseCommandException.class)` semantics behaviorally.
- Stay within `componentTest`: the mocked `CommandBus` is the fake command service; no real service, no Testcontainers.

**Non-Goals:**
- Changing production code (the client, filter, or auto-config).
- Adding new test suites or dependencies (resilience4j, reactor-test, and BlockHound are already on the componentTest
  classpath).
- Touching the query-client IT or the e2e suites.

## Decisions

- **The mocked `CommandBus` is sufficient for all three behaviors.** TimeLimiter needs a dispatch that never invokes the
  result callback (the limiter fires); CircuitBreaker needs repeated failure callbacks then a fast-fail assertion.
  None require real dispatch semantics, so no real in-process bus or stub aggregate is warranted.
- **Follow the established profile pattern.** `application.yml` keeps `resilience4j.enabled: false`; add
  `application-timelimiter.yml` (time limiter on, `timeout-duration: PT0.01S`, circuit breaker/retry off) and
  `application-circuitbreaker.yml` (`sliding-window-size: 2`, `minimum-number-of-calls: 2`,
  `permitted-number-of-calls-in-half-open-state: 1`, `wait-duration-in-open-state: PT1S`, `failure-rate-threshold: 50`,
  retry/time limiter off) — copied from the query-client IT profiles with the instance name changed to
  `showcase-command-service`.
- **Nested classes named `<Feature>Behavior`** to match the repo convention. The existing `Retry` class is renamed to
  `RetryBehavior`, joining the new `TimeLimiterBehavior` and `CircuitBreakerBehavior`; each is `@ActiveProfiles` on its
  own profile.
- **Non-retryable filter coverage lives under the `retry` profile.** The filter is only evaluated when retry is active;
  each scenario asserts `times(1)` dispatch. Concrete `AxonNonTransientException` subclasses must be selected from the
  Axon API at implementation (e.g., a direct non-transient exception and one used as a `CommandExecutionException`
  cause), mirroring how the existing `retryableErrors` sources construct exceptions.
- **Circuit-breaker tests are order-independent via a `@BeforeEach` reset.** Both scenarios share one
  `CircuitBreakerRegistry` instance, so the fail-fast test leaves the circuit OPEN inside its
  `wait-duration-in-open-state` window and the business-error test would inherit that state; each test resets the
  circuit first.

## Risks / Trade-offs

- [Time limiter test flakiness on timing] → Mitigation: use a 10ms timeout and the same `StepVerifier.verifyTimeout`
  pattern as the query-client IT; a business-error dispatch completes immediately so it must not time out.
- [Circuit breaker test depends on counting dispatches to open the window] → Mitigation: small deterministic config
  (`minimum-number-of-calls: 2`) and the query-client IT's loop-then-assert approach.
- [Picking a concrete `AxonNonTransientException` subclass] → Resolved at implementation:
  `RemoteNonTransientHandlingException` (the non-transient twin of the `RemoteHandlingException` already used in the
  retryable-error sources) serves both the direct and the `CommandExecutionException`-cause cases.

## Migration Plan

1. Add `application-timelimiter.yml` and `application-circuitbreaker.yml` to the componentTest resources.
2. Extend `ShowcaseCommandClientCT`: one default-profile `onErrorMap` else-branch test; three retry-profile
   non-retryable tests.
3. Add `TimeLimiterBehavior` and `CircuitBreakerBehavior` nested classes.
4. Run `./gradlew :showcase-command-client:componentTest` and confirm the JaCoCo report shows 100% branches on
   `ShowcaseCommandClient` and `ShowcaseCommandRetryFilter`.
5. Rollback: revert the test/resources changes (production code untouched).

## Open Questions

None.