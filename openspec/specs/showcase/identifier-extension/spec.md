# showcase/identifier-extension Specification

## Purpose
Documents the behavior of the KSUID identifier extension: automatic KSUID generation via the Axon `IdentifierFactory`
SPI and Bean Validation of KSUID-formatted strings across commands, queries, and REST endpoints.
## Requirements
### Requirement: KSUID identifier generation via Axon SPI

The system SHALL generate KSUID (K-Sortable Unique IDentifier) identifiers for all Axon aggregates by registering a
custom `IdentifierFactory` implementation via the Java ServiceLoader mechanism. When Axon creates a new aggregate
without an explicitly supplied identifier, the factory SHALL produce a string representation of a newly generated KSUID.

#### Scenario: Aggregate created without explicit identifier

- **WHEN** Axon creates a new aggregate and no identifier is explicitly supplied
- **THEN** the system generates a KSUID via the registered `IdentifierFactory` and assigns it as the aggregate
  identifier

#### Scenario: KSUIDs are chronologically sortable

- **WHEN** two KSUID identifiers are generated at different points in time
- **THEN** the identifier generated later sorts lexicographically after the identifier generated earlier, enabling
  time-ordered iteration and cursor-based pagination

### Requirement: KSUID Bean Validation constraint

The system SHALL provide a `@KSUID` Bean Validation constraint annotation that validates whether a string value is a
well-formed KSUID. The constraint SHALL be applicable to methods, fields, annotation types, constructors, parameters,
and type-use positions. The constraint SHALL be repeatable.

#### Scenario: Valid KSUID passes validation

- **WHEN** a field or parameter annotated with `@KSUID` contains a string produced by `Ksuid.newKsuid().toString()`
- **THEN** validation passes with no constraint violation

#### Scenario: Non-KSUID string fails validation

- **WHEN** a field or parameter annotated with `@KSUID` contains a string that is not a valid KSUID (e.g., a UUID)
- **THEN** validation fails with the message "must be a valid KSUID (K-Sortable Unique IDentifier)."

### Requirement: Null values are valid under @KSUID

The `@KSUID` constraint SHALL treat `null` values as valid. Presence validation SHALL be delegated to complementary
constraints such as `@NonNull` or `@NotBlank`, enabling composition where `@KSUID` validates format and a separate
constraint validates presence.

#### Scenario: Null value passes @KSUID validation

- **WHEN** a field annotated with `@KSUID` (without `@NonNull` or `@NotBlank`) contains `null`
- **THEN** validation passes with no constraint violation

#### Scenario: Null value fails when combined with @NonNull

- **WHEN** a field annotated with both `@KSUID` and `@NonNull` contains `null`
- **THEN** validation fails due to the `@NonNull` constraint, not the `@KSUID` constraint

### Requirement: KSUID validation message is externalized

The system SHALL resolve the `@KSUID` constraint violation message from a `ValidationMessages.properties` resource
bundle using the key `showcase.identifier.KSUID.message`. The resolved message SHALL be
"must be a valid KSUID (K-Sortable Unique IDentifier).".

#### Scenario: Constraint violation message resolution

- **WHEN** a `@KSUID` constraint violation occurs
- **THEN** the violation message is "must be a valid KSUID (K-Sortable Unique IDentifier)."

### Requirement: Module dependency exposure

The `showcase-identifier-extension` module SHALL expose the `@KSUID` annotation, the `KsuidValidator` class, and the
Axon `IdentifierFactory` SPI type as API dependencies. The underlying KSUID library SHALL be an implementation
dependency, not transitively exposed to consumers.

#### Scenario: Consumer module depends on identifier-extension via api configuration

- **WHEN** a module declares `api(project(":showcase-identifier-extension"))` (e.g., `showcase-command-api`)
- **THEN** the `@KSUID` annotation and `KsuidValidator` are available on the consumer's compile classpath, but the
  underlying `com.github.ksuid` library is not transitively exposed

#### Scenario: Consumer module depends on identifier-extension via implementation configuration

- **WHEN** a module declares `implementation(project(":showcase-identifier-extension"))` (e.g.,
  `showcase-query-proto`)
- **THEN** the identifier-extension types are available at compile time but are not exposed on the consumer's API
  classpath

