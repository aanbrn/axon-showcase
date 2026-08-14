## Context

See proposal.md - Why. The change adds JUnit 5 `@DisplayName` annotations (class, `@Nested`, and method level) to all
test classes across the four test tiers that currently rely on raw `subject_condition_expectedResult` method names. The
existing end-to-end suite (`ShowcaseApiGatewayE2E`) already establishes the pattern: static, human-readable sentences
without placeholders, with `@ParameterizedTest` methods keeping their named `argumentSet(...)` providers for per-case
detail. This design extends that same pattern to the remaining ~34 test classes and 2 `@Nested` groups in 12 modules,
with no production code changes.

## Goals / Non-Goals

**Goals:**

- Uniform rendering of test intent in IDE, Gradle console, and CI/JUnit XML across all four test tiers.
- Consistency with the established E2E `@DisplayName` style (static sentences, no placeholders, named `argumentSet`s).
- A documented convention in `AGENTS.md` that future tests follow via review.

**Non-Goals:**

- Changing any test logic, method names, or assertions — annotation-only refactor.
- Introducing a build-level enforcer (e.g., ArchUnit or a checkstyle rule) for `@DisplayName` presence.
- Renaming test methods or the `subject_condition_expectedResult` naming style itself.

## Decisions

- **Use JUnit 5 `org.junit.jupiter.api.DisplayName`** (already on the classpath) rather than a custom naming strategy or
  report post-processing. Rationale: zero new dependencies, native JUnit support, and it is the exact mechanism the E2E
  suite already uses. Alternatives considered and rejected: a `DisplayNameGenerator` (applies project-wide heuristics
  rather than explicit sentences, and does not render in the IDE's test tree as cleanly) and a report-rewrite plugin
  (adds build complexity for purely cosmetic output).
- **Static sentences derived from method names, no `{0}` placeholders.** Rationale: the proposal already resolved that
  named `argumentSet(...)` invocations render their own detail, so a static sentence keeps reports readable without
  duplicating parameter data. Alternative considered and rejected: templated `{0}`/`{1}` placeholders — they conflict
  with the E2E pattern and produce lower-quality default rendering.
- **Class-level sentence phrased as a short human-readable phrase, method-level as sentences.** Rationale: matches the
  existing E2E wording and keeps the IDE's grouped tree readable. `@Nested` groups receive the same treatment where they
  lack a display name (`TimeLimiter`, `Retry` in the client component tests).
- **Do not annotate helper/fixture classes** (`RandomCommandTestUtils`, `RandomQueryTestUtils`, `TestApp`, etc.).
  Rationale: they contain no tests; annotating them would be noise.
- **Document the convention in `AGENTS.md` rather than enforcing it in the build.** Rationale: annotation presence is a
  review-time style concern; the project convention is already that style rules live in `AGENTS.md` and are enforced via
  review.

## Risks / Trade-offs

- [Annotation-only edit could accidentally touch method bodies] → Mitigation: changes are confined to adding annotation
  lines above class/`@Nested`/method declarations; the test suites' green status is verified by the tasks (unit,
  component, and integration runs) before archiving.
- [A `@ParameterizedTest` display name could hide per-parameter detail if named arguments are lost] → Mitigation: named
  `argumentSet(...)` providers are preserved verbatim; only the static sentence is added, exactly as the E2E does.
- [Convention is enforced by review only, so a future test may skip `@DisplayName`] → Mitigation: accepted trade-off;
  task 4.4 greps for remaining unannotated files to confirm coverage at implementation time, and the `AGENTS.md` entry
  keeps the convention visible for reviewers.
