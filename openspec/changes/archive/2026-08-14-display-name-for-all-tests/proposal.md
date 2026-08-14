## Why

Test methods use descriptive `subject_condition_expectedResult` names, but JUnit renders the raw method name in IDE,
Gradle console, and CI/JUnit XML output. Only the end-to-end tests (`ShowcaseApiGatewayE2E`) already override this with
human-readable `@DisplayName` sentences; the other ~34 test classes across all four test tiers do not. This change
makes every test's intent read cleanly in reports by adding `@DisplayName` — at class, `@Nested`, and method level —
following the existing E2E pattern.

## What Changes

- Add a class-level `@DisplayName` to all 34 test classes currently missing one (unit, component, integration, and the
  remaining e2e classes), as a short human-readable phrase.
- Add `@DisplayName` to the 2 `@Nested` groups that lack it (`TimeLimiter` and `Retry` in the client component tests),
  matching the E2E's grouped hierarchy.
- Add a method-level `@DisplayName` to all ~262 `@Test`/`@ParameterizedTest` methods as uniform static sentences (no
  placeholders), derived from the existing `subject_condition_expectedResult` method names.
- `@ParameterizedTest` methods keep their existing named `argumentSet(...)` providers — per-case detail still renders
  via JUnit's default `[{index}] {argumentSetNameOrArgumentsWithNames}` pattern; the `@DisplayName` stays a static
  sentence, exactly as the E2E does.
- Test helper/fixture classes (`RandomCommandTestUtils`, `RandomQueryTestUtils`, `TestApp`, etc.) are not annotated —
  they contain no tests.
- Document the `@DisplayName` convention in `AGENTS.md` so future tests follow it via review (no build-level enforcer).

## Capabilities

### New Capabilities

_(none — pure test-style refactor, no behavioral change)_

### Modified Capabilities

_(none — no existing spec covers test naming conventions)_

## Impact

- **Code**: annotations only across 34 test files in 12 modules, all four test tiers
  (`src/test`, `src/componentTest`, `src/integrationTest`, `src/e2eTest`). No production code touched.
- **Dependencies**: none — reuses JUnit 5's `org.junit.jupiter.api.DisplayName`, already on the classpath.
- **Behavior**: unchanged. Test method names and logic are untouched; suites compile and run identically.
- **Docs**: `AGENTS.md` conventions section extended with the `@DisplayName` convention.
