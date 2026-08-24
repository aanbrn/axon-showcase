# Proposal: Add unit tests for the API gateway data contracts

## Why

The gateway's unit-test suite (`src/test`) is empty — a scaffold leftover. All gateway testing happens at the
component/integration/e2e tiers, but the gateway's own data contracts — `ScheduleShowcaseRequest` and
`ScheduleShowcaseResponse` — carry real validation logic (`@NotBlank`/`@NotNull` plus the `@ShowcaseTitle`,
`@ShowcaseStartTime`, `@ShowcaseDuration`, and `@KSUID` constraints) that is only exercised indirectly through the
controller component tests. These are exactly the kind of value objects the command/query API modules unit-test
(`ScheduleShowcaseCommandTests` and friends). Filling the empty suite with the same style of tests gives the data
contracts isolated, fast coverage and removes the redundant empty source set.

## What Changes

- `showcase-api-gateway/src/test/java/showcase/api/ScheduleShowcaseRequestTests.java` — construction and validation
  tests mirroring `ScheduleShowcaseCommandTests`: all-fields construction, plus `Validator`-based checks for a valid
  request, blank/too-long title, missing start time, and missing/too-short/too-long duration.
- `showcase-api-gateway/src/test/java/showcase/api/ScheduleShowcaseResponseTests.java` — construction (including the
  `@NonNull`-driven null-pointer check on a missing showcase ID) and `@KSUID` validation of the showcase ID.

No production code changes; the new tests reuse the shared `RandomCommandTestUtils` fixtures from
`showcase-command-api` testFixtures (already on the test suite classpath).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. Test-only change with no behavioral effect. `skip_specs: true`.

## Impact

- **Code**: two new test classes in `showcase-api-gateway/src/test/java/showcase/api/`.
- **Docs**: none.
- **Build**: the gateway `test` task now has real sources; `checkstyleTest`/`spotbugsTest` are no longer empty no-ops.
- **Tests**: the new unit tests; existing suites unaffected.