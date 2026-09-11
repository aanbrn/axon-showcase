# showcase/quality/ide-config

## ADDED Requirements

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
