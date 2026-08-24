## Purpose

Enforces the project's code style and static quality conventions through the build, so style checks are uniform and
independent of any developer IDE.

## ADDED Requirements

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