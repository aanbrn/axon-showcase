# Spec Delta

## MODIFIED Requirements

### Requirement: The setup reproduces the Python analysis module per working tree

Running the repository's setup SHALL provide IntelliJ Python analysis for `scripts/*.py` in the current working tree by
generating a Python module that names `scripts/` as its content root and references one registered Python interpreter,
and SHALL reconcile it in place on later runs, preserving the tree's other modules and any IDE- or plugin-managed
content.

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

- **WHEN** IntelliJ imports or syncs the project from its build model after the setup wrote the module
- **THEN** the module and its module-list entry remain, so the analysis is not lost by the sync

### Requirement: The Python analysis interpreter is the one the build runs

The setup SHALL resolve its interpreter with the build's own selection rule — the first `python3` on `PATH` — run from
the working tree's root so a per-working-tree selection on `PATH` applies, SHALL resolve that command to the real
interpreter it executes rather than to a path-resolving wrapper, and SHALL refuse an interpreter older than the build's
declared Python minimum. It SHALL match a registered SDK entry to that interpreter only when the entry's interpreter
resolves to that same binary and the entry is installed in the install that binary belongs to — the directory of the
path the setup reported or, when that interpreter runs outside a virtual environment, the directory the reported path
resolves to, since a path naming that target is then the same install. Everything else is a different SDK: a virtual
environment, whose interpreter resolves to the base interpreter it was created from but sits in the environment's own
directory, and a different interpreter installed in the same directory as the reported one.

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
