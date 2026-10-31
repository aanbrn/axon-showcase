## MODIFIED Requirements

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
