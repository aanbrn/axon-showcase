# Spec Delta

## ADDED Requirements

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

### Requirement: The Python analysis interpreter is the one the build runs

The setup SHALL resolve its interpreter with the build's own selection rule — the first `python3` on `PATH` — run from
the working tree's root so a per-working-tree selection on `PATH` applies, SHALL resolve that command to the real
interpreter it executes rather than to a path-resolving wrapper, and SHALL refuse an interpreter older than the build's
declared Python minimum.

#### Scenario: The interpreter follows the build's selection rule

- **WHEN** the setup runs from a working tree whose `PATH` resolves `python3` to a specific interpreter
- **THEN** the module references the SDK registered for that interpreter, matched by the interpreter's real binary path,
  and the setup reports the path and version it used

#### Scenario: An interpreter below the declared minimum stops the stage

- **WHEN** the resolved interpreter is older than Python 3.12
- **THEN** the Python stage fails with an error, the run exits non-zero, and no module is written

### Requirement: An SDK the IDE cannot resolve is reported, not written

The setup SHALL take the interpreter's SDK name from the SDK table of an IntelliJ configuration it has identified —
explicitly selected, or uniquely matched — and SHALL NOT write a module reference it cannot resolve, reporting instead
the action the contributor must take and where to take it.

#### Scenario: The SDK name comes from the identified configuration

- **WHEN** the identified configuration's SDK table holds a Python SDK whose home directory is the resolved interpreter
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
