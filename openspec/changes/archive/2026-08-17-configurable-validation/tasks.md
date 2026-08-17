# Tasks: Configurable validation enablement

## 1. Command service — configuration and interceptor

- [x] 1.1 Add `private boolean validationEnabled = true;` to `ShowcaseCommandProperties` (prefix `showcase.command`),
  producing `showcase.command.validation-enabled` with default `true`
- [x] 1.2 Wire the property to an environment variable in `application.yml` following the existing pattern:
  `validation-enabled: ${SHOWCASE_COMMAND_VALIDATION_ENABLED:true}`
- [x] 1.3 Add a `final boolean validationEnabled` field to `ShowcaseCommandMessageInterceptor` annotated with Lombok's
  `@RequiredArgsConstructor` (generating the single-arg `(boolean validationEnabled)` constructor); when `false`,
  `handle` calls `interceptorChain.proceed()` directly instead of `beanValidationInterceptor.handle(...)`, keeping the
  existing `JSR303ViolationException`/`AggregateNotFoundException`/`AggregateDeletedException` translation intact
- [x] 1.4 Update the `distributedCommandBus` bean in `ShowcaseCommandApplication` (making the bean method
  package-private like the query service's `@Bean` methods) to construct
  `new ShowcaseCommandMessageInterceptor<>(showcaseCommandProperties.isValidationEnabled())`, wiring the new property
- [x] 1.5 Update the `dbSchedulerCustomizer` in `ShowcaseCommandApplication` to name the executor's virtual threads with
  `Scheduler.THREAD_PREFIX + "-"`, matching db-scheduler's own default thread-name prefix (`db-scheduler-`); confirmed via
  A/B test that it has no effect on test-JVM linger, retained for naming consistency only

## 2. Query service — configuration and interceptor

- [x] 2.1 Add `private boolean validationEnabled = true;` to `ShowcaseQueryProperties` (prefix `showcase.query`),
  producing `showcase.query.validation-enabled` with default `true`
- [x] 2.2 Wire the property to an environment variable in `application.yml` following the existing pattern:
  `validation-enabled: ${SHOWCASE_QUERY_VALIDATION_ENABLED:true}`
- [x] 2.3 Add a `final boolean validationEnabled` field to `ShowcaseQueryMessageInterceptor` annotated with Lombok's
  `@RequiredArgsConstructor` (generating the single-arg `(boolean validationEnabled)` constructor); when `false`,
  `handle` calls `interceptorChain.proceed()` directly, keeping the `JSR303ViolationException` →
  `INVALID_QUERY` translation intact
- [x] 2.4 Update the `queryBusCustomizer` `InitializingBean` in `ShowcaseQueryApplication` to construct
  `new ShowcaseQueryMessageInterceptor<>(showcaseQueryProperties.isValidationEnabled())`, wiring the new property

## 3. Tests

- [x] 3.1 Add command interceptor component tests (new `src/componentTest/java` class in `showcase.command` package,
  suffix `CT`) covering: invalid command rejected with INVALID_COMMAND when validation is enabled; a
  validation-violating command proceeds successfully when validation is disabled; `AggregateNotFoundException` still
  translates to NOT_FOUND with validation disabled. The tests compose the interceptor with real Axon collaborators
  (`DefaultUnitOfWork`, `DefaultInterceptorChain`)
- [x] 3.1b Add command interceptor unit tests (new `src/test/java` class `ShowcaseCommandMessageInterceptorTests`)
  mocking the collaborators (`UnitOfWork`, `InterceptorChain` via Mockito) covering: invalid command rejected with
  INVALID_COMMAND when validation is enabled (real validator throws `JSR303ViolationException`); a violating command
  proceeds when validation is disabled; `AggregateNotFoundException` → NOT_FOUND, `AggregateDeletedException` →
  ILLEGAL_STATE, and idempotent remove (missing aggregate for a remove command is ignored) all with validation disabled.
  `libs.mockito.junit.jupiter` added to the `test` suite. The interceptor is constructed with diamond inference
  (`new ShowcaseCommandMessageInterceptor<>(...)`) and `throws Exception` is declared only on methods that invoke
  `handle`/`proceed` directly (not those wrapping the call in `assertThatThrownBy`)
- [x] 3.2 Add query interceptor component tests (new `src/componentTest/java` class in `showcase.query` package, suffix
  `CT`) covering: invalid query rejected when validation is enabled; a validation-violating query proceeds when
  validation is disabled. Same real-collaborator approach as 3.1
- [x] 3.2b Add query interceptor unit tests (new `src/test/java` class `ShowcaseQueryMessageInterceptorTests`) mocking
  the collaborators (`UnitOfWork`, `InterceptorChain` via Mockito) covering: invalid query rejected with INVALID_QUERY
  when validation is enabled; a violating query proceeds when validation is disabled. `libs.mockito.junit.jupiter`
  added to the `test` suite. The interceptor is constructed with diamond inference and `throws Exception` is declared
  only where `handle`/`proceed` are invoked directly. Register the new `componentTest` suite in
  `showcase-query-service/build.gradle.kts` and make the `integrationTest` suite run after it
  (`shouldRunAfter(componentTest)`), enforcing the `test → componentTest → integrationTest` tier order
- [x] 3.3 Update `ShowcaseSagaDeadlinesIT`: add `showcase.command.validation-enabled=false` to `@TestPropertySource`,
  replace `Duration.ofMinutes(ShowcaseDuration.MIN_MINUTES)` with a 1-second duration, and drop the 2-second
  `startTime` offset (use `Instant.now()`); keep all assertions unchanged
- [x] 3.4 Verify the saga deadlines IT completes in ~2-3 seconds (not ~62 seconds) and still passes, relying on the
  0.5-second db-scheduler polling interval rather than long validation-enforced durations

## 4. Verification

- [x] 4.1 Run `./gradlew :showcase-command-service:check` and `./gradlew :showcase-query-service:check` — all test tiers
  green
- [x] 4.2 Confirm default behavior is unchanged: with no `validation-enabled` property set,
  INVALID_COMMAND/INVALID_QUERY rejection tests still pass
- [x] 4.3 Add a Spring context component test confirming env-var-form binding (new `src/componentTest/java` classes
  `ShowcaseCommandPropertiesCT` / `ShowcaseQueryPropertiesCT`): supply `SHOWCASE_COMMAND_VALIDATION_ENABLED=false` /
  `SHOWCASE_QUERY_VALIDATION_ENABLED=false` (env-var-style key) via a `SystemEnvironmentPropertySource` registered on
  the `ApplicationContextRunner` environment, and assert `isValidationEnabled()` is `false`. Spring's relaxed binding
  normalizes the env-var-form key to the dotted property, and the `${...}` placeholder resolves against the test
  property source, so this exercises the same binding path a real environment variable would
