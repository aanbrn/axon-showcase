# Proposal: Expand command-client resilience component tests

## Why

`ShowcaseCommandClientCT` leaves measured coverage holes and resilience behavior gaps. JaCoCo shows
`ShowcaseCommandClient` at 75% branches (the `onErrorMap` else-branch is never taken) and `ShowcaseCommandRetryFilter`
at 50% branches (none of the three non-retryable `return false` paths are exercised). Beyond branch coverage, the
command client tests only Retry — unlike the query-client IT, it has no TimeLimiter or CircuitBreaker behavior tests,
so the auto-config's circuit-breaker `ignoreExceptions(ShowcaseCommandException.class)` semantics are unverified.

## What Changes

- Add one default-profile CT test for `ShowcaseCommandClient` where a `CommandExecutionException` carries details that
  are **not** a `ShowcaseCommandErrorDetails`, asserting the raw exception propagates (covers the `onErrorMap` else
  branch).
- Add three retry-profile CT tests asserting non-retryable exceptions are **not** retried (1 dispatch each):
  `ShowcaseCommandException`, a direct `AxonNonTransientException`, and a `CommandExecutionException` whose cause is an
  `AxonNonTransientException` (covers the three `ShowcaseCommandRetryFilter` branches).
- Add an `@Nested TimeLimiter` behavior class with an `application-timelimiter.yml` profile: a command whose dispatch
  never completes fails with a timeout; a business error (`ShowcaseCommandException`) completes without timing out.
- Add an `@Nested CircuitBreaker` behavior class with an `application-circuitbreaker.yml` profile: repeated dispatch
  failures open the circuit and subsequent calls fail fast with `CallNotPermittedException`; repeated
  `ShowcaseCommandException`s (business errors) leave the circuit closed.
- All scenarios reuse the existing `@MockitoBean CommandBus` fake — no real command service or external
  infrastructure.
- No production code changes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. This is a test-only change; component-test coverage does not alter externally observable
behavior.

## Impact

- **Code (tests only)**: `showcase-command-client/src/componentTest/java/showcase/command/ShowcaseCommandClientCT.java`
  (new outer test, new Retry tests, new `TimeLimiter`/`CircuitBreaker` nested classes) and new profile resources
  `showcase-command-client/src/componentTest/resources/application-timelimiter.yml` and
  `application-circuitbreaker.yml` (modeled on the query-client IT profiles).
- **Docs**: none expected.
- **Build**: `componentTest` still covered by `check`; no new suites or dependencies needed (resilience4j, reactor-test,
  BlockHound already in the componentTest classpath).
- **Tests**: verified via `./gradlew :showcase-command-client:componentTest` and the module's JaCoCo report (targets:
  `ShowcaseCommandClient` and `ShowcaseCommandRetryFilter` at 100% branches).