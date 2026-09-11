# showcase/quality/code-quality

## ADDED Requirements

### Requirement: The web module's formatting is applied by the build

The build SHALL provide a Gradle task that applies the web module's Prettier formatting (`prettier --write`), so a
contributor or agent formats the frontend with `./gradlew :showcase-web-ui:npmFormat` instead of invoking npm directly.
The task SHALL apply the same Prettier invocation the formatting check verifies (so the two agree), SHALL run entirely
within the Gradle build with no IDE required, and SHALL NOT be part of the standard `check` task — which verifies
formatting through the existing `npmFormatCheck` task instead.

#### Scenario: The format task applies Prettier

- **WHEN** a contributor runs the web module's format task
- **THEN** the module's sources are rewritten to Prettier's canonical form (matching the formatting check)

#### Scenario: Verification does not run the writing task

- **WHEN** the standard `check` task runs
- **THEN** it verifies formatting with the format-check task (`npmFormatCheck`) and does not invoke the writing format
  task

#### Scenario: Formatting runs without an IDE

- **WHEN** the format task runs on a machine with no IDE installed
- **THEN** it executes entirely within the Gradle build
