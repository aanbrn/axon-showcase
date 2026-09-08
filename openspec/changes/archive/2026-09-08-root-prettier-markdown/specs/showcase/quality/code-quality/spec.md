## MODIFIED Requirements

### Requirement: Source formatting is enforced by the build

The build SHALL format Java and Kotlin DSL (`.gradle.kts`) sources to a canonical style — including removal of unused
imports — and verify formatting as part of the standard `check` task, with no IDE required. The build SHALL also format
root markdown (`docs/`, `AGENTS.md`, `README.md`, `openspec/specs/`, and active `openspec/changes/*/`) with Prettier at
`printWidth: 120` with `proseWrap: "always"`, and verify it in `check` — ending the manual 120-char wrapping convention.
The `openspec/changes/archive/` historical record is not reformatted.

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

#### Scenario: Header present is accepted

- **WHEN** a source file carries the SPDX license header
- **THEN** the build accepts it

#### Scenario: Missing header fails the build

- **WHEN** a source file does not carry the SPDX license header
- **THEN** the formatting check fails on that file

#### Scenario: Standard check verifies every gate without an IDE

- **WHEN** the standard `check` task runs on a machine with no IDE installed
- **THEN** it runs all code-quality gates and reports success or violations

#### Scenario: Change is verified without opening an IDE

- **WHEN** a contributor verifies a change
- **THEN** `./gradlew check` is sufficient — no IDE step is required

#### Scenario: Unformatted markdown fails the build

- **WHEN** a root markdown file (`docs/`, `AGENTS.md`, `README.md`, `openspec/specs/`, an active `openspec/changes/*/`)
  is not Prettier-formatted
- **THEN** the root markdown formatting check fails and reports the offending file

#### Scenario: Archived change markdown is not reformatted

- **WHEN** the root markdown formatting check runs
- **THEN** it does not check `openspec/changes/archive/` (the historical record is left as recorded)
