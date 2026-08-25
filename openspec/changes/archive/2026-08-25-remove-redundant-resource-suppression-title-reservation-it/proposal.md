# Proposal: Remove a redundant resource suppression from the title reservation integration test

## Why

`ShowcaseTitleReservationIT` suppresses `"resource"` on its non-generic `PostgreSQLContainer` field, but the
`AutoCloseableResource` inspection emits no warning there — the suppression is a no-op. The other `"unused"` half
of the annotation is load-bearing (the `@Container` field is never referenced directly), so it stays.

## What Changes

- Change `@SuppressWarnings({"resource", "unused"})` to `@SuppressWarnings("unused")` on the `dbEvents` container field
  in `ShowcaseTitleReservationIT`.
- No production code, behavior, or other test changes.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. A one-line test-source IDE-hygiene fix with no externally observable behavior change.

## Impact

- **Code (tests only)**:
  `showcase-command-service/src/integrationTest/java/showcase/command/ShowcaseTitleReservationIT.java`.
- **Docs**: none.
- **Build**: unchanged; verified via `compileIntegrationTestJava`.
- **Tests**: no test changes; verified by the IDE reporting no `AutoCloseableResource` warning without the `"resource"`
  half.