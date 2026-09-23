## ADDED Requirements

### Requirement: Tracked-file hygiene is verified by the build

The standard `check` task SHALL verify that no tracked file is excluded by the repository's ignore rules — the CI-gated
counterpart of the pre-commit guard's force-staged-artifact check — so a generated artifact force-added at any point
fails the CI `build` gate even when no local hook is installed. The verification SHALL name each offending path.

#### Scenario: The standard check verifies tracked-file hygiene

- **WHEN** the standard `check` task runs
- **THEN** it includes the tracked-file hygiene verification

#### Scenario: A tracked ignored file fails the build

- **WHEN** a file matching the repository's ignore rules is tracked (for example a bytecode or build-output file
  force-added to the index)
- **THEN** the tracked-file hygiene verification fails and names the offending path

#### Scenario: A clean repository passes

- **WHEN** no tracked file matches the repository's ignore rules
- **THEN** the tracked-file hygiene verification passes
