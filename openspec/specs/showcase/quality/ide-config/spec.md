# Ide Config Specification

## Purpose

Keeps the repository's IntelliJ configuration out of version control: the settings the IDE needs to match the build
formatters — palantir/ktfmt for the JVM and Prettier for the web module — plus the import layout and the test-tier
naming inspection, are sourced from `.editorconfig` and a committed setup script, so the IDE reads config rather than
owning versioned files.

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

### Requirement: The setup configures a formatter-matched IDE for the web module

The repository SHALL provide, through the same setup, the configuration that makes IntelliJ format the web module
(`showcase-web-ui`) to match its build gate, `prettier --check`: the project's Prettier settings (so IntelliJ formats
those files with the repository's Prettier and `showcase-web-ui/.prettierrc`, scoped to the file types the module's gate
covers — Prettier's default scope omits CSS and HTML) and the JS/TS indentation IntelliJ reads from `.editorconfig` (a
file it does not rewrite). The setup SHALL merge the settings it owns into IntelliJ's files, preserving other
IDE-managed content, so that re-running it reconciles drift, and SHALL NOT require a formatter plugin to be installed.

#### Scenario: The web module's formatter settings are configured

- **WHEN** a contributor runs the setup on a fresh clone
- **THEN** IntelliJ is configured to format the web module's files — including the CSS and HTML that Prettier's default
  scope omits — with the project's Prettier (matching `showcase-web-ui/.prettierrc`)

#### Scenario: The web module's JS/TS style is sourced from a read-only file

- **WHEN** a maintainer inspects the repository's web-module code-style configuration
- **THEN** the JS/TS indentation is expressed in `.editorconfig`, which IntelliJ reads without rewriting

#### Scenario: Web-module formatter settings are reconciled without clobbering

- **WHEN** the setup runs against an existing IntelliJ configuration that holds other settings
- **THEN** the web-module formatter settings are added or updated, and the other settings are preserved
