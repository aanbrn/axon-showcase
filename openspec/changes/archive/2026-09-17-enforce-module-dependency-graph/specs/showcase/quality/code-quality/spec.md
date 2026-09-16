## ADDED Requirements

### Requirement: The module dependency graph is enforced by the build

The build SHALL verify the modules' declared project dependencies against the structure's sanctioned shape and fail
`check` when a module acquires a forbidden edge, naming the offending edge. The forbidden edges SHALL be: a module
depending on a service application (`showcase-command-service`, `showcase-query-service`, `showcase-projection-service`,
`showcase-api-gateway`); an `-extension` module depending on anything other than `platform` and the `showcase-test`
dependency; and a contract module (`showcase-command-api`, `showcase-query-api`, `showcase-projection-model`,
`showcase-query-proto`) depending on a `-client` module or a service application. The verification SHALL inspect each
module's production source sets — `main`, which ships, and `testFixtures`, which other modules consume as an artifact;
test suites are deliberately out of scope, since a suite's edges reach no artifact and a suite depending on a service
application is a legitimate way to exercise it.

#### Scenario: A forbidden edge fails the build

- **WHEN** a module declares a dependency on a service application, an `-extension` gains a non-platform dependency, or
  a contract module depends on a `-client` or a service application
- **THEN** the verification fails and reports the offending edge (its source and target modules), so the drift is caught
  where it is introduced rather than by a later audit

#### Scenario: The current graph passes

- **WHEN** the verification runs against the repository's modules as they stand
- **THEN** it passes, because every rule asserts a property the graph already has — the check is containment, not new
  capability

#### Scenario: A dependency's kind and scope are respected

- **WHEN** the verification walks a module's dependencies
- **THEN** it inspects every declaration configuration of that module's production source sets (`main` and
  `testFixtures`), excluding the `platform` BOM by its declared target, and ignores any edge whose source and target are
  the same module when deciding violations — neither is a structural dependency between modules
