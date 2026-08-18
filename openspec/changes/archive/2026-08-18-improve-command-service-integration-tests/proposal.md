# Proposal: Add command-service exit-after-flyway-migration integration test

## Why

The command service can exit the JVM after Flyway migration (`EXIT_AFTER_FLYWAY_MIGRATION`), but this path is untested —
unlike the query service's `ExitAfterIndexInitialization` test (ADR-0005). The command service still calls
`System.exit` inline, which cannot be exercised by a test without terminating the test JVM.

## What Changes

- Apply the ADR-0005 seam to `ShowcaseCommandApplication`: route the exit through a testable `ApplicationExitHandler`
  `@FunctionalInterface` + default bean, instead of calling `System.exit` inline in `flywayMigrationStrategy`.
- Add `ShowcaseCommandApplicationExitAfterFlywayMigrationIT`, an integration test booting the full context with
  `showcase.command.exit-after-flyway-migration=true`, replacing the exit handler with a `@MockitoBean` and verifying it
  is invoked after the Flyway migration.
- Remove the now-unnecessary `@DirtiesContext` from `ShowcaseTitleReservationIT` (a `@JdbcTest` slice with no JCache or
  JGroups global state), verified to pass without it; keep it on the full-context ITs that boot JCache/JGroups.
- Behavior-preserving production change; no runtime behavior differences.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — test-only; `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- New integration test under `showcase-command-service/src/integrationTest/`; a thin behavior-preserving refactor of
  `ShowcaseCommandApplication` (the `ApplicationExitHandler` seam from ADR-0005).
