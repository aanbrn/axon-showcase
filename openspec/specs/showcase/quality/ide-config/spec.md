# showcase/quality/ide-config Specification

## Purpose

Keeps the repository's IntelliJ configuration out of version control: the settings the IDE needs to match the build
formatters — palantir/ktfmt for the JVM and Prettier for the web module — the import layout and the test-tier naming
inspection, and the Python analysis module for `scripts/*.py`, are sourced from `.editorconfig` and a committed setup
script that reads the IDE's own SDK registration, so the IDE reads config rather than owning versioned files and every
working tree reproduces the same setup from one run.

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

### Requirement: The setup reproduces the Python analysis module per working tree

Running the repository's setup SHALL provide IntelliJ Python analysis for `scripts/*.py` in the current working tree by
generating a Python module that names `scripts/` as its content root and references one registered Python interpreter,
and SHALL reconcile it in place on later runs, preserving the tree's other modules and any IDE- or plugin-managed
content. It SHALL report the module's activation procedure: the module is read when the project is opened after a run
made while the project was closed, and a run made while the project was open is completed by closing the project,
running the setup again, and opening it. It SHALL NOT present reloading an open project as the way the module is added.

#### Scenario: A fresh working tree gains the module

- **WHEN** the setup runs in a working tree that has no IntelliJ configuration for `scripts/`
- **THEN** a Python module covering `scripts/` is written to the project's IDE configuration and registered in its
  module list, carrying one Python SDK reference

#### Scenario: Re-running is idempotent

- **WHEN** the setup runs a second time with the same inputs
- **THEN** the module file and the module list are unchanged

#### Scenario: Other modules and plugin-managed content are preserved

- **WHEN** the setup runs against an IDE configuration that lists other modules, and against a module file that carries
  another plugin's settings component
- **THEN** the other modules and that component are left untouched

#### Scenario: A drifted module is restored

- **WHEN** the managed module has drifted (its content root, its interpreter reference, or its module-list entry was
  changed or removed) and the setup runs again
- **THEN** the managed values are restored to the setup's own

#### Scenario: Importing the build model keeps the module

- **WHEN** IntelliJ imports or syncs the project from its build model after the setup wrote the module, and the project
  has opened the module
- **THEN** the module and its module-list entry remain, so the analysis is not lost by the sync

#### Scenario: The outcome names the module's activation procedure

- **WHEN** the setup reports its outcome after writing or confirming the module
- **THEN** the outcome names opening the project after a run made while it was closed as the activation step, names
  closing the project, running the setup again, and opening it for a run made while it was open, and does not present
  reloading an open project as adding the module

### Requirement: The Python analysis interpreter is the one the build runs

The setup SHALL resolve its interpreter with the build's own selection rule — the first `python3` on `PATH` — run from
the working tree's root so a per-working-tree selection on `PATH` applies, SHALL resolve that command to the real
interpreter it executes rather than to a path-resolving wrapper, and SHALL refuse an interpreter older than the build's
declared Python minimum. It SHALL match a registered SDK entry to that interpreter only when the entry's interpreter
resolves to that same binary and the entry is installed in the install that binary belongs to — the directory of the
path the setup reported or, when that interpreter runs outside a virtual environment, the directory the reported path
resolves to, since a path naming that target is then the same install. Everything else is a different SDK: a virtual
environment, whose interpreter resolves to the base interpreter it was created from but sits in the environment's own
directory, and a different interpreter installed in the same directory as the reported one. A working tree the
repository's workspace procedure creates, when that repository's primary checkout has been set up for IntelliJ, SHALL
carry the primary's interpreter selection — the `.python-version` that names it — when the primary carries one and the
tree does not, before the setup resolves the interpreter; the setup then resolves the interpreter the tree's `PATH`
reaches through that selection's selector, and writes the module for it when the SDK table registers an entry for it.
Such a tree, when the primary is set up for IntelliJ, SHALL be created and prepared in one operation before the unit's
first content edit in it, and SHALL be treated as ready only when that preparation succeeded and this stage reported
`CONFIGURED`; a preparation that does not reach that outcome SHALL keep the tree and report its path, the stage's line,
and the remedy, rather than deleting or recreating it.

#### Scenario: The interpreter follows the build's selection rule

- **WHEN** the setup runs from a working tree whose `PATH` resolves `python3` to a specific interpreter
- **THEN** the module references the SDK installed for that interpreter, matched on the interpreter and on the install
  the reported path belongs to rather than on either alone, and the setup reports the path and version it used

#### Scenario: An interpreter below the declared minimum stops the stage

- **WHEN** the resolved interpreter is older than Python 3.12
- **THEN** the Python stage fails with an error, the run exits non-zero, and no module is written

#### Scenario: A virtual environment's SDK is not the interpreter it wraps

- **WHEN** the SDK table registers a virtual environment whose interpreter is a symbolic link to the base interpreter
  the setup resolved
- **THEN** that entry does not match: the setup selects the entry installed for the interpreter's own directory, or
  reports the contributor's action when the table holds none

#### Scenario: An SDK registered by the target of a link is the same install

- **WHEN** the reported interpreter runs outside a virtual environment and the SDK table's entry names the binary that
  that interpreter's path resolves to
- **THEN** the entry matches, and the module references its name verbatim

#### Scenario: A virtual environment's interpreter does not select the base installation's SDK

- **WHEN** the reported interpreter runs from a virtual environment and the SDK table registers only the SDK of the
  binary that environment's interpreter resolves to
- **THEN** that entry does not match, and the setup reports the contributor's action naming the environment's own
  interpreter

#### Scenario: A different interpreter in the same directory is not the one the build runs

- **WHEN** the SDK table registers a Python SDK whose interpreter is a different binary installed in the same directory
  as the resolved interpreter
- **THEN** that entry does not match, and the setup reports the contributor's action when the table holds no entry for
  the resolved interpreter

#### Scenario: A new working tree inherits the primary's interpreter

- **WHEN** the workspace procedure creates a working tree whose repository's primary checkout has been set up for
  IntelliJ and carries an interpreter selection, and the tree carries none
- **THEN** the tree carries the primary's selection, and the setup resolves the interpreter the tree's `PATH` reaches
  through it, writing the module for that interpreter when the SDK table registers an entry for it and reporting the
  action otherwise

#### Scenario: A created tree is prepared before the unit's first write

- **WHEN** the workspace procedure creates a working tree while the primary checkout is set up for IntelliJ
- **THEN** the tree is created and prepared in one operation, its interpreter selection inherited before the setup
  resolves it, and the tree is ready for the unit's first content edit only when that preparation succeeded and this
  stage reported `CONFIGURED`

#### Scenario: An unsuccessful preparation keeps the tree and reports it

- **WHEN** the preparation of a created tree fails, or its Python stage reports `NEEDS_ACTION`, `ERROR`, or `SKIPPED`
- **THEN** the tree is kept, and the report names its path, the stage line, and the remedy, instead of the tree being
  deleted or recreated

#### Scenario: A primary with no IntelliJ setup owes no preparation

- **WHEN** a created tree's primary checkout carries no IntelliJ configuration
- **THEN** no preparation is owed, the tree is still created, and the report says so rather than treating the absence as
  a failure

### Requirement: An SDK the IDE cannot resolve is reported, not written

The setup SHALL take the interpreter's SDK name from the SDK table of an IntelliJ configuration it has identified —
explicitly selected, or uniquely matched — and SHALL NOT write a module reference it cannot resolve, reporting instead
the action the contributor must take and where to take it.

#### Scenario: The SDK name comes from the identified configuration

- **WHEN** the identified configuration's SDK table holds a Python SDK installed for the resolved interpreter
- **THEN** the module references that entry's name verbatim

#### Scenario: A missing or ambiguous match needs the contributor's action

- **WHEN** no SDK entry matches the resolved interpreter, or several entries match it, or several configurations qualify
  and none was selected explicitly
- **THEN** the Python stage reports that the contributor's action is required, the run exits non-zero, and no module is
  written; the report names the interpreter path, the candidate configurations or entries, and the IntelliJ settings
  page on which the interpreter is registered as an SDK

#### Scenario: No IntelliJ configuration is found

- **WHEN** no IntelliJ configuration directory can be found and none was selected explicitly
- **THEN** the Python stage reports that it was skipped, the run exits zero, and the formatter settings are still merged

#### Scenario: An explicitly selected configuration is honoured and validated

- **WHEN** the contributor names a configuration directory
- **THEN** only that directory is consulted, and an unusable value fails the stage without falling back to a search

### Requirement: The setup reports each stage's outcome and continues its other work

The setup SHALL print one outcome for each of its reporting stages — the settings merge and the Python analysis —
`CONFIGURED` when the stage wrote or confirmed its configuration, `SKIPPED` when it had nothing to configure,
`NEEDS_ACTION` when the contributor must act, and `ERROR` when it could not run — and SHALL exit zero only when no stage
reported `NEEDS_ACTION` or `ERROR`. A failing stage SHALL NOT stop the stages the setup can still run.

#### Scenario: A successful run exits zero and reports every stage

- **WHEN** every reporting stage reports `CONFIGURED` or `SKIPPED`, and the plugin installation succeeds or is
  legitimately skipped
- **THEN** the run exits zero and each reporting stage's line carries its outcome

#### Scenario: A failed plugin installation makes the run exit non-zero

- **WHEN** the plugin installation fails while both reporting stages report `CONFIGURED` or `SKIPPED`
- **THEN** the run exits non-zero and reports the installation failure, without claiming that either reporting stage
  failed

#### Scenario: A contributor action makes the run exit non-zero without stopping the other stages

- **WHEN** a stage reports `NEEDS_ACTION`
- **THEN** the run exits non-zero, the stages that could run did run and report their own outcomes, and the plugin
  installation was attempted

#### Scenario: A stage error makes the run exit non-zero without stopping the other stages

- **WHEN** a stage reports `ERROR`
- **THEN** the run exits non-zero after the other stages have run and reported their own outcomes, and the plugin
  installation was attempted

#### Scenario: A skipped plugin installation is a legitimate outcome

- **WHEN** the IDE is running or no launcher is found, so the plugin installation cannot run
- **THEN** the installation reports that it was skipped, the run's exit status still reflects any other stage's
  `NEEDS_ACTION` or `ERROR`, and no stage treats the skip as a failure of its own
