# showcase/quality/ide-config Specification

## MODIFIED Requirements

### Requirement: An SDK the IDE cannot resolve is reported, not written

The setup SHALL take the interpreter's SDK name from the SDK table of an IntelliJ configuration it has identified —
explicitly selected, matched to an installed IntelliJ IDEA bundle's product and version, or uniquely matched — and SHALL
NOT write a module reference it cannot resolve, reporting instead the action the contributor must take and where to take
it. On macOS it SHALL resolve the installed bundles' `CFBundleShortVersionString` and product names, prefer the
configuration at the default configuration location whose prefix and `major.minor` suffix match one of them — the
launcher the setup resolved first, then the newest — and report the configuration and the pair it matched. A matched
configuration that is absent or registers no SDK for the resolved interpreter SHALL stop the stage with the registration
owed in that configuration and `--idea-config-dir` named for a custom configuration path, never fall back to another
configuration's SDK name, and bundles are tried in that order accordingly. Automatic selection assumes the default
configuration location; a custom `idea.config.path` is not detected. Two products at one version that both register the
interpreter SHALL be reported as a choice rather than resolved unless a launcher resolves one of them. On a platform
where the version cannot be resolved, the setup SHALL keep the refusal and name that limitation.

#### Scenario: The SDK name comes from the identified configuration

- **WHEN** the identified configuration's SDK table holds a Python SDK installed for the resolved interpreter
- **THEN** the module references that entry's name verbatim

#### Scenario: The installed IDE's version selects its configuration

- **WHEN** an upgrade leaves two configurations that both register the resolved interpreter, and the installed bundle's
  product and version match one of them
- **THEN** the setup selects that configuration, writes the module from its SDK table, and reports the configuration and
  the product and version it matched instead of refusing

#### Scenario: The launcher the setup resolved selects its configuration

- **WHEN** the setup resolved a launcher for a bundle that is not the newest installed, and that bundle's configuration
  registers the resolved interpreter
- **THEN** the setup selects that configuration, installs the plugins into the same IDE, and reports it — the two stages
  configure one target

#### Scenario: The installed IDE has no configuration yet

- **WHEN** the installed bundle's product and version match no configuration directory at the default location, whether
  or not another configuration registers the resolved interpreter
- **THEN** the setup stops, reports that no configuration exists at the default location yet, names `--idea-config-dir`
  for a custom `idea.config.path`, and writes no module from another configuration's SDK table

#### Scenario: The version's configuration lacks the SDK

- **WHEN** the installed bundle's product and version match a configuration that registers no SDK for the resolved
  interpreter, whether or not another configuration does
- **THEN** the setup stops and reports the SDK registration owed in the matched configuration, naming it and
  `--idea-config-dir` for a custom `idea.config.path`, and writes no module — an SDK name from another configuration
  would not resolve in the installed IDE

#### Scenario: Two products at one version are a choice

- **WHEN** no launcher resolved, the installed bundles name both IntelliJ IDEA editions at the same version, and both
  configurations register the resolved interpreter
- **THEN** the setup reports the contributor's action, names both configurations, and writes no module

#### Scenario: The identified configuration registers the interpreter twice

- **WHEN** the identified configuration's SDK table holds two Python SDK entries for the resolved interpreter
- **THEN** the setup reports the contributor's action, names the interpreter, that configuration, and the keep-one
  remedy, and writes no module

#### Scenario: A missing or ambiguous match needs the contributor's action

- **WHEN** no bundle pair applies and no SDK entry matches the resolved interpreter, or several entries match it, or
  several configurations qualify and none was selected explicitly and no installed bundle resolves exactly one of them
- **THEN** the Python stage reports that the contributor's action is required, the run exits non-zero, and no module is
  written; the report names the interpreter path, the candidate configurations or entries, and the IntelliJ settings
  page on which the interpreter is registered as an SDK

#### Scenario: No IntelliJ configuration is found

- **WHEN** no IntelliJ configuration directory can be found and none was selected explicitly
- **THEN** the Python stage reports that it was skipped, the run exits zero, and the formatter settings are still merged

#### Scenario: An explicitly selected configuration is honoured and validated

- **WHEN** the contributor names a configuration directory
- **THEN** only that directory is consulted, and an unusable value fails the stage without falling back to a search

#### Scenario: A platform without a bundle resolution keeps the refusal

- **WHEN** several configurations register the interpreter on a platform where the installed IDE's version cannot be
  resolved
- **THEN** the setup reports the contributor's action and names that limitation, and writes no module
