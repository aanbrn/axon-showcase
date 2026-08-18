## 1. Testable exit seam

- [x] 1.1 Add the `ApplicationExitHandler` `@FunctionalInterface` + default bean to `ShowcaseCommandApplication`, and
      route `flywayMigrationStrategy` through it instead of calling `System.exit` inline (ADR-0005)

## 2. Exit-after-flyway integration test

- [x] 2.1 Add `ShowcaseCommandApplicationExitAfterFlywayMigrationIT`: boot the full context with
      `showcase.command.exit-after-flyway-migration=true`, replace the exit handler with a `@MockitoBean`, and verify it
      is invoked after the Flyway migration
- [x] 2.2 Use a separate top-level IT class (not `@Nested`) with a dedicated JGroups port, since `@Nested` collides with
      the command service's JCache context

## 3. DirtiesContext cleanup

- [x] 3.1 Remove the unnecessary `@DirtiesContext` from `ShowcaseTitleReservationIT` (`@JdbcTest` slice, no JCache/JGroups
      global state), verified to pass; keep it on the full-context ITs
