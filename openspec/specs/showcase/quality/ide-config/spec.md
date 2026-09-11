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
test-tier naming inspection are in effect. Running the setup SHALL reconcile a drifted configuration: it SHALL merge the
repository's settings (the `config/idea/*.xml` components and the test-tier naming inspection) into IntelliJ's files,
preserving other IDE- or plugin-managed content, so a configuration that has drifted — never applied cleanly, or
overwritten or dropped by IntelliJ — is restored rather than left stale or overwritten. Merging the configuration SHALL
NOT depend on the IDE being closed — only the plugin installation does; when the IDE is running, the setup SHALL still
merge the configuration and skip the plugin installation with a warning. IntelliJ reads these settings at startup, so
the merged configuration takes effect on its next reload (File → Reload All from Disk) or restart.

#### Scenario: Running setup configures the IDE

- **WHEN** a contributor runs the setup script on a fresh clone
- **THEN** the palantir and ktfmt plugins are enabled for the project, the import layout is in effect, and the test-tier
  naming inspection is present in the inspection profile

#### Scenario: Running setup is idempotent

- **WHEN** a contributor runs the setup script a second time
- **THEN** the resulting IDE configuration is unchanged (no duplicated settings or churn)

#### Scenario: Existing inspection settings are preserved

- **WHEN** the setup script runs against an inspection profile that already contains other inspection settings
- **THEN** the test-tier naming inspection is added or updated, and the other inspection settings are left untouched

#### Scenario: Setup reconciles a drifted configuration

- **WHEN** a managed setting has drifted (it was never applied cleanly, or IntelliJ overwrote it) and a contributor runs
  the setup again
- **THEN** the managed setting is restored to the repository's value, and any other content in the affected files is
  preserved

#### Scenario: The configuration is applied while the IDE is running

- **WHEN** the setup runs while IntelliJ is running
- **THEN** the repository's configuration is merged into the project's IDE files, and the plugin installation is skipped
  with a warning rather than aborting the whole setup; the merged configuration takes effect when IntelliJ next reloads
  (File → Reload All from Disk) or restarts

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
