## ADDED Requirements

### Requirement: Adopting process components use the declared bindings

An adopting process component SHALL resolve its process bindings through `docs/contracts/process-bindings.md`, which
owns the binding rules: the six roles, the read routes, the role-not-string rule, the missing-binding stop, and the
non-adopter trace. This requirement states the capability outcome and the scenarios below; the contract owns the rules.

#### Scenario: An adopting component resolves a binding

- **WHEN** an adopting component needs a declared role — the review ledger, the lifecycle, the review gate, the
  workspace record, the change artifacts, or the prior finding instances
- **THEN** it reads the declaration for the role's value, reads the referenced owner, and uses the role's own contract
  rather than restating it

#### Scenario: A renamed binding still resolves

- **WHEN** a target's declaration names a different path or heading for a role, and its own artifacts carry those names
- **THEN** the adopting component resolves the role through the declaration and preserves its obligations, without
  editing the component

#### Scenario: A missing binding stops the operation

- **WHEN** a required binding is missing, ambiguous, or semantically incompatible with the role
- **THEN** the component stops the affected operation and reports the specific dependency; it does not invent a value,
  delete the obligation, or weaken its policy

#### Scenario: A non-adopter is named

- **WHEN** a component does not adopt the bindings
- **THEN** the unit's consumer trace names it as a non-adopter, so the consumer set is explicit rather than inferred
