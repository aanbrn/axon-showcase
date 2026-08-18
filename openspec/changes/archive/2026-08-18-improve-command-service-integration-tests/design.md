## Context

The command service's `exitAfterFlywayMigration` path calls `System.exit` inline, which cannot be integration-tested
(terminates the test JVM). ADR-0005 already established a testable `ApplicationExitHandler` seam for the query service
and anticipated the command service adopting it. See proposal.md - Why for motivation.

## Goals / Non-Goals

**Goals:**

- Make the command service's exit-after-flyway-migration path integration-testable.
- Verify the exit handler is invoked after Flyway migration when the property is enabled.

**Non-Goals:**

- No runtime behavior changes.

## Decisions

- **Adopt the ADR-0005 `ApplicationExitHandler` seam** in `ShowcaseCommandApplication`: add the `@FunctionalInterface`
  and a default bean that closes the context via `SpringApplication.exit` and terminates via `System.exit`; have
  `flywayMigrationStrategy` call `exitHandler.exit(applicationContext)` instead of `System.exit` inline. *Alternative
  considered:* intercepting `System.exit` with a security manager — rejected, brittle and removed in modern JDKs.
- **Test via a separate top-level IT class** (`ShowcaseCommandApplicationExitAfterFlywayMigrationIT`) rather than a
  `@Nested` class (as the query service does). `@Nested` is incompatible here: the command service's JCache context
  collides across the outer and nested contexts ("Cache showcase-cache already exists"), unlike the query service's
  OpenSearch. A separate class with its own JGroups port isolates the context.
- **Keep `@DirtiesContext` only where it is needed.** It is required on the full-context ITs that boot JCache and
  JGroups (they leak global state across contexts), but not on `@JdbcTest` slice tests, which have no such state.
  Remove it from `ShowcaseTitleReservationIT` (verified to pass without it).

## Risks / Trade-offs

- [The `@Nested` approach works for the query service but not the command service] → use a separate top-level IT class;
  the test intent (exit handler invoked after the startup step) is preserved.
