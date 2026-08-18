# ADR-0005: Testable exit-on-startup via an application exit handler

Date: 2026-08-18

Status: Accepted

## Context

The query service can exit the JVM after index initialization (`showcase.query.exit-after-index-initialization`), and
the command service analogously after Flyway migration (`EXIT_AFTER_FLYWAY_MIGRATION`). Both call
`System.exit(SpringApplication.exit(context, () -> 0))` from an `InitializingBean` during context startup. `System.exit`
terminates the JVM, and `SpringApplication.exit` closes the context — so an integration test that enables the property
either kills the test worker or fails the context boot, leaving the exit-on-startup path untested.

## Decision

Route the exit through a small `@FunctionalInterface` bean instead of calling `System.exit` inline. The default bean
encapsulates the whole exit (closing the context via `SpringApplication.exit` and terminating via `System.exit`), while
an integration test replaces it with a `@MockitoBean` and asserts the handler is invoked after the startup step. The
seam covers both the context close and the JVM termination, because either step alone breaks a test context.

```java
@FunctionalInterface
interface ApplicationExitHandler {
    void exit(ApplicationContext applicationContext);
}

@Bean
ApplicationExitHandler applicationExitHandler() {
    return context -> System.exit(SpringApplication.exit(context, () -> 0));
}
```

## Consequences

- The exit-on-startup path is integration-testable without terminating the test JVM.
- The seam is a thin addition to the application class; the default behavior is unchanged.
- The command service's `exitAfterFlywayMigration` path can adopt the same pattern when tested.
