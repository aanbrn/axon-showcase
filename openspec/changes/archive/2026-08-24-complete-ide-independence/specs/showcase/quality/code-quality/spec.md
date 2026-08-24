## ADDED Requirements

### Requirement: Quality verification does not require an IDE

The standard `check` task SHALL verify all code-quality gates entirely within the Gradle build, so a contributor never
needs an IDE to validate a change.

#### Scenario: Standard check verifies every gate without an IDE

- **WHEN** the standard `check` task runs on a machine with no IDE installed
- **THEN** it runs all code-quality gates and reports success or violations

#### Scenario: Change is verified without opening an IDE

- **WHEN** a contributor verifies a change
- **THEN** `./gradlew check` is sufficient — no IDE step is required