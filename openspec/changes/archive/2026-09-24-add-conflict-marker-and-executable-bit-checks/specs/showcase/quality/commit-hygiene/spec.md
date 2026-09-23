## MODIFIED Requirements

### Requirement: A pre-commit guard blocks a mechanically defective staged set

The repository SHALL provide a tracked git `pre-commit` hook that inspects the staged set before a commit is created and
refuses the commit when it detects a mechanically defective state: the project's formatter check fails; a staged path is
a generated artifact that must not be committed; a path is staged and then modified again so the index no longer matches
the working tree; a staged file carries a merge conflict marker; or a `captured:` marker is misplaced in a staged
`AGENTS.md`. The hook SHALL name each offending path and the reason and SHALL exit non-zero.

#### Scenario: A failing formatter check refuses the commit

- **WHEN** the project's formatter check fails while a formatter-owned file is staged
- **THEN** the guard refuses the commit and reports the formatter check's failure

#### Scenario: A force-staged generated artifact refuses the commit

- **WHEN** a generated artifact that the repository excludes (for example a Python bytecode file or a build output) is
  staged
- **THEN** the guard refuses the commit and names the offending path

#### Scenario: A path staged and then edited refuses the commit

- **WHEN** a path is staged and then modified again so the index and the working tree differ for that path
- **THEN** the guard refuses the commit and names the path

#### Scenario: A staged conflict marker refuses the commit

- **WHEN** a staged file carries a merge conflict branch marker
- **THEN** the guard refuses the commit and names the offending file and line

#### Scenario: A misplaced captured marker refuses the commit

- **WHEN** a staged `AGENTS.md` carries a `captured:` marker that is not inside a rule block (for example one sitting on
  a plain `- Text` bullet)
- **THEN** the guard refuses the commit and names the offending marker's location

#### Scenario: A clean staged set proceeds

- **WHEN** the staged set carries none of the detected defects
- **THEN** the guard passes and the commit proceeds

## ADDED Requirements

### Requirement: The tracked set carries no merge conflict marker

The standard `check` task SHALL verify that no tracked file carries a merge conflict marker, so a conflicted markdown or
docs file — whose branch marker the formatter rewrites into a blockquote rather than flagging — fails the CI `build`
gate. The verification SHALL name each offending file and line.

#### Scenario: The standard check verifies the tracked set

- **WHEN** the standard `check` task runs
- **THEN** it includes the conflict-marker verification

#### Scenario: A tracked conflict marker fails the build

- **WHEN** a tracked file carries a merge conflict branch marker
- **THEN** the conflict-marker verification fails and names the offending file and line

#### Scenario: A clean repository passes

- **WHEN** no tracked file carries a merge conflict branch marker
- **THEN** the conflict-marker verification passes

### Requirement: The tracked hook, shell scripts, and Gradle wrapper carry the executable bit

The standard `check` task SHALL verify that every tracked file git runs directly is mode `100755`: the `pre-commit` hook
(which git silently skips when non-executable), the shell scripts (`*.sh`, excluding the vendored `axon4to5-*` and
generated `openspec-*` skill subtrees), and the Gradle wrapper. A lost executable bit SHALL fail the CI `build` gate,
naming each offending path.

#### Scenario: The standard check verifies the executable bits

- **WHEN** the standard `check` task runs
- **THEN** it includes the executable-bit verification

#### Scenario: A non-executable hook fails the build

- **WHEN** a tracked file under `scripts/git-hooks/` is not mode `100755`
- **THEN** the executable-bit verification fails and names the offending path

#### Scenario: A non-executable shell script fails the build

- **WHEN** a tracked shell script (`*.sh`, outside the vendored `axon4to5-*` and generated `openspec-*` skill subtrees)
  is not mode `100755`
- **THEN** the executable-bit verification fails and names the offending path

#### Scenario: A non-executable Gradle wrapper fails the build

- **WHEN** the tracked `gradlew` is not mode `100755`
- **THEN** the executable-bit verification fails and names the offending path

#### Scenario: A clean repository passes

- **WHEN** every tracked hook, shell script, and the Gradle wrapper is mode `100755`
- **THEN** the executable-bit verification passes
