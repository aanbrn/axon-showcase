# Ide Config Specification

## Purpose

Keeps the repository's IntelliJ configuration out of version control: the settings the IDE needs to match the build
formatter (palantir/ktfmt enablement, the import layout, and the test-tier naming inspection) are sourced from
`.editorconfig` and a committed setup script, so the IDE reads config rather than owning versioned files.

## Requirements

### Requirement: IntelliJ configuration is not versioned

The repository SHALL NOT track files under `.idea/`. The version-control ignore rules SHALL ignore the entire `.idea/`
directory, and no project-local IDE configuration file SHALL be committed.

#### Scenario: The whole .idea directory is ignored

- **WHEN** a contributor inspects the repository's ignore rules
- **THEN** `.idea/` is fully ignored, including any files previously whitelisted

#### Scenario: IDE configuration is not committed

- **WHEN** a contributor checks what files are under version control
- **THEN** no `.idea/` files appear in the tracked set

### Requirement: The import layout is sourced from a read-only file

The palantir import layout and the single-class-import settings that keep IntelliJ's `Optimize Imports` consistent with
the build formatter SHALL be defined in `.editorconfig` — a file IntelliJ reads but does not rewrite.

#### Scenario: Import layout is defined in .editorconfig

- **WHEN** a maintainer inspects the repository's code-style configuration
- **THEN** the import layout (static imports first, then a blank line, then non-static imports) and the
  single-class-import behavior are expressed in `.editorconfig`

#### Scenario: The IDE reads the import layout without rewriting it

- **WHEN** IntelliJ optimizes imports in a Java source file
- **THEN** it applies the `.editorconfig` import layout, and the `.editorconfig` file is not modified by the IDE

### Requirement: The setup script configures a formatter-matched IDE

The repository SHALL provide a setup script that, when run on a fresh clone, configures IntelliJ to match the build
formatter: it installs the palantir and ktfmt plugins, enables them for the project, and ensures the import layout and
test-tier naming inspection are in effect.

#### Scenario: Running setup configures the IDE

- **WHEN** a contributor runs the setup script on a fresh clone
- **THEN** the palantir and ktfmt plugins are enabled for the project, the import layout is in effect, and the
  test-tier naming inspection is present in the inspection profile

#### Scenario: Running setup is idempotent

- **WHEN** a contributor runs the setup script a second time
- **THEN** the resulting IDE configuration is unchanged (no duplicated settings or churn)

#### Scenario: Existing inspection settings are preserved

- **WHEN** the setup script runs against an inspection profile that already contains other inspection settings
- **THEN** the test-tier naming inspection is added or updated, and the other inspection settings are left untouched

### Requirement: Test-tier naming is enforced by an inspection

The repository SHALL ship the test-tier naming convention (`Tests`, `CT`, `IT`, `E2E` suffixes) as an IntelliJ
inspection in the project's inspection profile, so non-conforming test class names are flagged in the IDE.

#### Scenario: The naming inspection is present

- **WHEN** a contributor opens the project's inspection profile
- **THEN** it contains the test-tier naming convention inspection (e.g. `NewClassNamingConvention` with the
  `JUnitTestClassNamingConvention` extension)

#### Scenario: Non-conforming test class name is flagged

- **WHEN** a test class name does not end in one of the test-tier suffixes (`Tests`, `CT`, `IT`, `E2E`)
- **THEN** the IDE flags the name under the test-tier naming inspection