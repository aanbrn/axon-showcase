# ide-config Specification

## MODIFIED Requirements

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
