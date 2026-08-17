# Proposal: Configurable validation enablement

## Why

Integration tests in the command service are slowed by validation: `@ShowcaseDuration` enforces a minimum duration of 1
minute, so the saga deadlines IT waits roughly 60 seconds for the deadline to fire. Bean validation is currently hardwired
into the command and query services (each builds `new BeanValidationInterceptor<>()` inline), so there is no way to run a
fast test path that skips validation while still exercising the same handlers.

## What Changes

- Add a `showcase.command.validation-enabled` property (default `true`, env var `SHOWCASE_COMMAND_VALIDATION_ENABLED`) to
  the command service that controls whether command payloads are bean-validated before handling.
- Add a `showcase.query.validation-enabled` property (default `true`, env var `SHOWCASE_QUERY_VALIDATION_ENABLED`) to the
  query service that controls whether query payloads are bean-validated before handling.
- When validation is disabled, the existing interceptors skip only the validation step; error translation for unknown or
  removed aggregates (`NOT_FOUND`, `ILLEGAL_STATE`, idempotent remove) still applies.
- The saga deadlines IT sets `showcase.command.validation-enabled=false` and schedules the showcase to start immediately
  with a 1-second duration instead of the 1-minute minimum, cutting the test's wall-clock time from roughly 62 seconds to
  ~2-3 seconds.
- Add unit/component tests covering the interceptor behavior with validation both enabled and disabled, for the command
  and query services.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/write-side/command-service`: the `Command validation` requirement changes so that validation is enabled by
  default but can be disabled via configuration.
- `showcase/read-side/query-service`: the `Query validation` requirement changes so that validation is enabled by default
  but can be disabled via configuration.

## Impact

- `showcase-command-service` — `ShowcaseCommandMessageInterceptor`, `ShowcaseCommandProperties`, registration in
  `ShowcaseCommandApplication`; integration test `ShowcaseSagaDeadlinesIT`.
- `showcase-query-service` — `ShowcaseQueryMessageInterceptor`, `ShowcaseQueryProperties`, registration in
  `ShowcaseQueryApplication`; new/updated interceptor tests.
- Public behavior is unchanged with default configuration; the new properties are opt-out toggles primarily intended for
  tests and local development.
- No API, dependency, or schema changes; no Helm chart changes (defaults keep current behavior).
