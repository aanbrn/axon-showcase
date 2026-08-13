## Purpose

Documents the behavior of the custom MapStruct `AccessorNamingStrategy` that enables MapStruct to recognize Lombok
fluent accessors (methods matching a field by name and return type, without `get`/`set` prefixes) during compile-time
mapper generation.

## ADDED Requirements

### Requirement: Fluent getter detection via field match

The system SHALL recognize a parameterless method as a fluent getter when the method's enclosing class contains a field
with the same simple name and the same type as the method's return type. Methods with parameters or methods whose
enclosing class has no matching field SHALL NOT be recognized as fluent getters.

#### Scenario: Parameterless method matching a field by name and type

- **WHEN** a class has a field `String showcaseId` and a parameterless method `String showcaseId()`
- **THEN** the method is recognized as a fluent getter for the `showcaseId` property

#### Scenario: Parameterless method with no matching field

- **WHEN** a class has a parameterless method `computedValue()` with no backing field of the same name and type
- **THEN** the method is NOT recognized as a fluent getter

#### Scenario: Method with parameters is not a fluent getter

- **WHEN** a class has a method `showcaseId(String newValue)` with a matching field but the method takes a parameter
- **THEN** the method is NOT recognized as a fluent getter

#### Scenario: Method name matches field but return type differs

- **WHEN** a class has a field `String showcaseId` and a parameterless method `int showcaseId()`
- **THEN** the method is NOT recognized as a fluent getter because the return type does not match the field type

### Requirement: Fluent getter property name resolution

The system SHALL resolve the property name of a fluent getter as the method's simple name itself (e.g., method
`showcaseId()` maps to property `showcaseId`). For non-fluent methods, the system SHALL fall back to the default
`DefaultAccessorNamingStrategy` property name resolution (e.g., `getShowcaseId()` maps to `showcaseId`).

#### Scenario: Fluent getter property name

- **WHEN** a fluent getter method `showcaseId()` is detected on a class
- **THEN** the property name is resolved as `showcaseId`

#### Scenario: Standard getter property name

- **WHEN** a standard getter method `getShowcaseId()` is detected on a class
- **THEN** the property name is resolved as `showcaseId` via the default naming strategy

### Requirement: Coexistence of fluent and standard accessors

The system SHALL recognize both fluent getters and standard JavaBean getters simultaneously. A method SHALL be
considered a getter if it is either a fluent getter or a standard getter as determined by the default
`DefaultAccessorNamingStrategy`.

#### Scenario: Class with both fluent and standard getters

- **WHEN** a class has a fluent getter `title()` and a standard getter `getDescription()`
- **THEN** both methods are recognized as getters, and MapStruct maps both the `title` and `description` properties

### Requirement: SPI registration and compile-time discovery

The system SHALL register the fluent accessor naming strategy via the Java ServiceLoader mechanism under the
`org.mapstruct.ap.spi.AccessorNamingStrategy` service interface. MapStruct's annotation processor SHALL discover and
apply the strategy automatically at compile time when the module is present on the annotation processor classpath.

#### Scenario: MapStruct discovers the naming strategy via SPI

- **WHEN** the module is on the annotation processor classpath during compilation
- **THEN** MapStruct's annotation processor discovers and applies the fluent accessor naming strategy automatically,
  without requiring explicit configuration in mapper annotations

### Requirement: Compile-time-only annotation processor dependency

The system SHALL be consumed as an annotation processor dependency, not as a regular compile or runtime dependency.
The module SHALL NOT appear on the runtime classpath of consuming services. The module's sole dependency SHALL be the
MapStruct annotation processor library.

#### Scenario: Consumer declares annotationProcessor dependency

- **WHEN** a service module declares `annotationProcessor(project(":showcase-mapstruct-extension"))` (e.g.,
  `showcase-query-service`)
- **THEN** the naming strategy is available to MapStruct at compile time only and does not appear in the service's
  runtime classpath or Docker image

#### Scenario: Module absent from annotation processor classpath

- **WHEN** a service module uses MapStruct with fluent accessor types but does not declare the module as an annotation
  processor
- **THEN** MapStruct fails to recognize fluent accessors, resulting in unmapped properties or compilation errors
