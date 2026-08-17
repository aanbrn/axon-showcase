# Design: Configurable validation enablement

## Context

See proposal.md — Why. Both services hardwire bean validation: `ShowcaseCommandMessageInterceptor` and
`ShowcaseQueryMessageInterceptor` each instantiate `new BeanValidationInterceptor<>()` inline, and both are registered
unconditionally (command at `ShowcaseCommandApplication#commandBus`, query via the `queryBusCustomizer`
`InitializingBean`). Each service already has a `@ConfigurationProperties` class (`ShowcaseCommandProperties`,
`ShowcaseQueryProperties`) bound to its service-specific prefix.

## Goals / Non-Goals

**Goals:**

- A per-service configuration toggle (default enabled) that skips bean validation for commands and queries.
- Preserve error translation when validation is off: command `NOT_FOUND`/`ILLEGAL_STATE`/idempotent-remove behavior and
  query failure mapping must be unchanged.
- Make the saga deadlines IT fast (seconds, not ~62s) by running it with validation disabled and a short duration.

**Non-Goals:**

- Per-command or per-query validation control (the toggle is service-wide).
- Making validation itself faster or configurable in granularity (e.g., partial validation).
- Changing the error code/shape produced when validation runs and fails.
- Any change to the API gateway, projection service, clients, or Helm chart.

## Decisions

### 1. Gate validation inside the existing interceptors rather than conditionally registering them

Each interceptor gains a `final boolean validationEnabled` field and is annotated with Lombok's
`@RequiredArgsConstructor`
(which generates the single-arg `(boolean validationEnabled)` constructor; the inline-initialized
`BeanValidationInterceptor` field is excluded from it). When `false`, `handle` calls
`interceptorChain.proceed()` directly, bypassing `beanValidationInterceptor.handle(...)` but staying inside the same
try/catch so command error translation still applies.

Rationale: registering the interceptor only when enabled would drop the `AggregateNotFoundException` →
`NOT_FOUND`/`ILLEGAL_STATE` translation that other tests and behavior rely on. Keeping one interceptor with a skipped
validation step is the smallest change that preserves all non-validation behavior.

The command service's `distributedCommandBus` bean method was also made package-private (removing the `public`
modifier), matching the query service where all `@Bean` methods are package-private; this is a style-only change with no
behavioral impact.

Alternatives considered:

- Conditional registration via `@ConditionalOnProperty`: rejected — loses error translation when disabled.
- Separate "validation-only" interceptor toggled independently: rejected — more moving parts, no benefit.
- Injecting a no-op `ValidatorFactory`: rejected — `BeanValidationInterceptor` reads constraints from annotations, so a
  no-op factory does not remove enforcement; the toggle must skip the validation step itself.

### 2. Toggle lives on the existing `@ConfigurationProperties` classes

Add `private boolean validationEnabled = true;` to `ShowcaseCommandProperties` and `ShowcaseQueryProperties`, yielding
`showcase.command.validation-enabled` and `showcase.query.validation-enabled` (both default `true`). Following the
existing pattern in both services' `application.yml`, each property is bound to an environment variable with a default:
`showcase.command.validation-enabled: ${SHOWCASE_COMMAND_VALIDATION_ENABLED:true}` and
`showcase.query.validation-enabled: ${SHOWCASE_QUERY_VALIDATION_ENABLED:true}`. The registration sites pass
`properties.isValidationEnabled()` into the interceptor constructors.

Rationale: consistent with existing service-level configuration (e.g., `exit-after-flyway-migration`/
`EXIT_AFTER_FLYWAY_MIGRATION`, `index-initialization-enabled`/`INDEX_INITIALIZATION_ENABLED`), avoids a new
`@Value`-based pattern, and is trivially overridable in tests (via a Spring context) and deployment (via env var).

The env-var wiring is verified by a Spring context component test that registers the env-var-form key
(`SHOWCASE_COMMAND_VALIDATION_ENABLED=false` / `SHOWCASE_QUERY_VALIDATION_ENABLED=false`) as a
`SystemEnvironmentPropertySource` on an `ApplicationContextRunner` environment and asserts the properties bean reads it
as disabled. Relaxed binding normalizes the env-var-form key to the dotted property, and the `${...}` placeholder
resolves against the `Environment`'s property sources (including test property sources), so this exercises the same
binding path a real environment variable takes. A real OS env var cannot be set from within a JUnit process, so the
env-var-form key is the faithful in-process stand-in.

Alternative considered: a shared `showcase.validation-enabled` property. Rejected — the two services are independent
deployables; per-service toggles match the existing `showcase.command`/`showcase.query` prefix convention.

### 3. Tests exercise both toggle states

- Command interceptor component tests: valid/invalid commands with validation on (existing behavior), and a
  validation-violating command that succeeds when the toggle is off. These compose the interceptor with real Axon
  collaborators (`DefaultUnitOfWork`, `DefaultInterceptorChain`) and live in `src/componentTest/java` (suffix `CT`).
  Tests use diamond inference for the interceptor (`new ShowcaseCommandMessageInterceptor<>(...)`) and bare `List.of()`
  for the (empty) interceptor list; `throws Exception` is declared only where `handle` is invoked directly rather than
  inside `assertThatThrownBy`.
- Command interceptor unit tests: the same scenarios with the `UnitOfWork` and `InterceptorChain` collaborators mocked
  via Mockito (live in `src/test/java`, suffix `Tests`), including the `AggregateNotFoundException` → NOT_FOUND,
  `AggregateDeletedException` → ILLEGAL_STATE, and idempotent-remove paths with validation disabled.
- Query interceptor component tests: invalid query rejected when on, query proceeds when off. Same approach as the
  command interceptor tests.
- Query interceptor unit tests: the same scenarios with the collaborators mocked via Mockito (`src/test/java`, suffix
  `Tests`).
- `ShowcaseSagaDeadlinesIT`: set `showcase.command.validation-enabled=false`, use a 1-second duration, and start the
  showcase at `Instant.now()` (no future offset). The start deadline fires on the next 0.5-second db-scheduler poll, and
  the finish deadline fires `startedAt + 1s` later, cutting the test from ~62 seconds to ~2-3 seconds. A deadline can
  only fire after the saga registers it, so a non-future start time is safe.
- The query service registers a new `componentTest` suite in `build.gradle.kts`, and its `integrationTest` suite now
  runs after it (`shouldRunAfter(componentTest)`), enforcing the `test → componentTest → integrationTest` tier order.
  The command service's existing component suite keeps the same deps (plus `spring-boot-starter-test` for the properties
  binding tests).

Rationale: without a disabled-path test the toggle could silently break handling; the enabled path is already covered by
existing suites, but adding explicit interceptor-level tests keeps the contract pinned in both states.

### 4. db-scheduler executor threads use the standard thread-name prefix

The command service's `dbSchedulerCustomizer` names its virtual-thread executor's threads with
`Scheduler.THREAD_PREFIX + "-"`, producing `db-scheduler-0`, `db-scheduler-1`, ... A prefix without the trailing dash
would produce `db-scheduler0`, diverging from db-scheduler's own default factories, which use `THREAD_PREFIX + "-"`
(`db-scheduler-`, `db-scheduler-execute-due-`, `db-scheduler-housekeeper-`). The trailing dash keeps log thread names
uniform across the scheduler. Purely cosmetic; no behavioral impact. (Initially suspected to fix the test-JVM linger seen
in the saga deadlines IT, but a controlled A/B test proved it non-causal — the linger came from stale Gradle worker
processes — so it is retained for naming consistency only.)

## Risks / Trade-offs

- [Production misconfiguration disables validation] → property defaults to `true`; documentation and proposal mark it as
  opt-out for tests/local dev only.
- [Test coverage gap: disabled path skips real constraints] → the enabled path remains the default and is covered by
  existing integration/component suites; the new fast test runs the same handlers with validation skipped, which is its
  intent.
- [Saga IT no longer exercises the 1-minute floor] → the floor is covered elsewhere (validation-on tests assert duration
  constraints); the IT's purpose is deadline scheduling, not duration bounds.

## Migration Plan

- No data or schema migration. New properties default to current behavior, so existing deployments are unaffected.
- Rollback: remove/ignore the properties; behavior reverts to always-validate.

## Open Questions

None.
