# showcase/quality/ide-config

## MODIFIED Requirements

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
