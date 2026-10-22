## MODIFIED Requirements

### Requirement: The tracked hook, shell scripts, and Gradle wrapper carry the executable bit

The standard `check` task SHALL verify that every tracked file git runs directly is mode `100755`: the `pre-commit` hook
(which git silently skips when non-executable), the shell scripts (`*.sh`, excluding the vendored skill subtrees and the
generated `openspec-*` ones), and the Gradle wrapper. A lost executable bit SHALL fail the CI `build` gate, naming each
offending path.

#### Scenario: The standard check verifies the executable bits

- **WHEN** the standard `check` task runs
- **THEN** it includes the executable-bit verification

#### Scenario: A non-executable hook fails the build

- **WHEN** a tracked file under `scripts/git-hooks/` is not mode `100755`
- **THEN** the executable-bit verification fails and names the offending path

#### Scenario: A non-executable shell script fails the build

- **WHEN** a tracked shell script (`*.sh`, outside the vendored and generated `openspec-*` skill subtrees) is not mode
  `100755`
- **THEN** the executable-bit verification fails and names the offending path

#### Scenario: A non-executable Gradle wrapper fails the build

- **WHEN** the tracked `gradlew` is not mode `100755`
- **THEN** the executable-bit verification fails and names the offending path

#### Scenario: A clean repository passes

- **WHEN** every tracked hook, shell script, and the Gradle wrapper is mode `100755`
- **THEN** the executable-bit verification passes
