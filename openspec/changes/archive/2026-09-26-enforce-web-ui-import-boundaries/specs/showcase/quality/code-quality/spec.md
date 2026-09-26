## ADDED Requirements

### Requirement: The web module's import boundaries are enforced by the build

The web module's Feature-Sliced structure SHALL be enforced by the standard check: a layer SHALL import only from layers
below it (`app` > `pages` > `widgets` > `features` > `entities` > `shared`), a slice SHALL NOT import a sibling slice
except through an explicitly declared `@x` cross-import API, every cross-slice import SHALL target the target slice's
public API rather than its internals, and a source file that matches no layer or slice SHALL be rejected. The ignored
out-of-slice files — the composition entry, the test setup, the type declarations, and test files — are outside the
layer graph and excluded.

#### Scenario: A deep cross-slice import is rejected

- **WHEN** a module imports a file inside another slice rather than that slice's public API
- **THEN** the lint check fails on that import

#### Scenario: An upward-layer import is rejected

- **WHEN** a module imports from a layer above its own
- **THEN** the lint check fails on that import

#### Scenario: An undeclared sibling-slice import is rejected

- **WHEN** a module imports a sibling slice without a declared `@x` cross-import API
- **THEN** the lint check fails on that import

#### Scenario: A source file outside the layer graph is rejected

- **WHEN** a source file matches no layer or slice and is not one of the ignored out-of-slice files
- **THEN** the lint check fails on that file

#### Scenario: A public-API downward import is accepted

- **WHEN** a module imports a lower slice's public API
- **THEN** the lint check accepts it

#### Scenario: A declared cross-import is accepted

- **WHEN** a module imports a sibling through the target's declared `@x` cross-import API
- **THEN** the lint check accepts it

#### Scenario: The ignored out-of-slice files are accepted

- **WHEN** the composition entry, the test setup, a type declaration, or a test file imports across boundaries
- **THEN** the lint check accepts it
