# showcase/quality/code-quality Specification

## Purpose
Enforces the project's code style and static quality conventions through the build, so style checks are uniform and
independent of any developer IDE.

## Requirements
### Requirement: Code style is enforced by the build

The build SHALL run a code-style check as part of the standard `check` task across all modules.

#### Scenario: Standard check runs the style check

- **WHEN** the standard `check` task runs
- **THEN** it includes a code-style check for every module

#### Scenario: Style violation fails the build

- **WHEN** a source file violates the configured style rules
- **THEN** the style check fails and reports the offending file and rule

#### Scenario: Style check runs without an IDE

- **WHEN** the style check runs on a machine with no IDE installed
- **THEN** it executes entirely within the Gradle build

### Requirement: Line length limit matches the project convention

Source lines MUST NOT exceed 120 characters, matching the project's documented wrapping convention.

#### Scenario: Over-long line is rejected

- **WHEN** a source line exceeds 120 characters
- **THEN** the style check fails on that line

#### Scenario: Boundary line is accepted

- **WHEN** a source line is exactly 120 characters
- **THEN** the style check accepts it

### Requirement: Naming and import conventions are enforced

Type, method, and constant naming and import hygiene MUST follow the project's conventions, including the test-tier
suffixes (`Tests`, `CT`, `IT`, `E2E`).

#### Scenario: Non-conforming type name is rejected

- **WHEN** a type name does not conform to the configured naming rules
- **THEN** the style check fails on that type

#### Scenario: Test-tier suffix is accepted

- **WHEN** a test class uses one of the project's test-tier suffixes (`Tests`, `CT`, `IT`, `E2E`)
- **THEN** the style check accepts the type name

### Requirement: Source formatting is enforced by the build

The build SHALL format Java and Kotlin DSL (`.gradle.kts`) sources to a canonical style — including removal of unused
imports — and verify formatting as part of the standard `check` task, with no IDE required.

#### Scenario: Formatting check runs in the standard check

- **WHEN** the standard `check` task runs
- **THEN** it includes a formatting check for every module

#### Scenario: Unformatted source fails the build

- **WHEN** a source file does not conform to the canonical formatting
- **THEN** the formatting check fails and reports the offending file

#### Scenario: Unused import fails the build

- **WHEN** a Kotlin DSL (`.gradle.kts`) source file contains an unused import
- **THEN** the formatting check fails and reports the offending file

#### Scenario: Formatting runs without an IDE

- **WHEN** the formatting check runs on a machine with no IDE installed
- **THEN** it executes entirely within the Gradle build

### Requirement: License headers are enforced

Each Java source file SHALL carry the repository's license header, matching the MIT license declared in the LICENSE
file.

#### Scenario: Header present is accepted

- **WHEN** a source file carries the SPDX license header
- **THEN** the build accepts it

#### Scenario: Missing header fails the build

- **WHEN** a source file does not carry the SPDX license header
- **THEN** the formatting check fails on that file
### Requirement: Quality verification does not require an IDE

The standard `check` task SHALL verify all code-quality gates entirely within the Gradle build, so a contributor never
needs an IDE to validate a change.

#### Scenario: Standard check verifies every gate without an IDE

- **WHEN** the standard `check` task runs on a machine with no IDE installed
- **THEN** it runs all code-quality gates and reports success or violations

#### Scenario: Change is verified without opening an IDE

- **WHEN** a contributor verifies a change
- **THEN** `./gradlew check` is sufficient — no IDE step is required
