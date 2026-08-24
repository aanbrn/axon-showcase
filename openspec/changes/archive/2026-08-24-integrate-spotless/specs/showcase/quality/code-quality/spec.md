## ADDED Requirements

### Requirement: Source formatting is enforced by the build

The build SHALL format Java sources to a canonical style and verify formatting as part of the standard `check` task,
with no IDE required.

#### Scenario: Formatting check runs in the standard check

- **WHEN** the standard `check` task runs
- **THEN** it includes a formatting check for every module

#### Scenario: Unformatted source fails the build

- **WHEN** a source file does not conform to the canonical formatting
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