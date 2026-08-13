## Why

`KsuidIdentifierFactory` — the Axon SPI implementation that generates KSUID identifiers — has zero test coverage. The
only existing tests in the module (`KSUIDTests`) cover the `@KSUID` Bean Validation constraint, leaving the 
identifier-generation contract from the `showcase/identifier-extension` spec untested. This change adds deterministic
unit tests for the factory.

## What Changes

- Add a `KsuidIdentifierFactoryTest` covering:
  - `generateIdentifier()` returns a non-blank, 27-character Base62 string that round-trips through `Ksuid.fromString`
  - Generated identifiers are unique across many invocations
  - Axon SPI discovery: `IdentifierFactory.getInstance()` resolves to a `KsuidIdentifierFactory` (ServiceLoader
    registration)
  - Generated KSUIDs expose monotonically non-decreasing timestamps (the deterministic invariant behind chronological
    sortability)
- Document the same-second caveat as a test comment: string-level sortability is not asserted because two KSUIDs
  generated within the same second sort by random payload, not generation order.

## Capabilities

### New Capabilities

_(none — pure test addition, no behavioral change)_

### Modified Capabilities

_(none)_

## Impact

- **Code**: new test source file under `showcase-identifier-extension/src/test/java/showcase/identifier/`.
- **Dependencies**: none — reuses existing JUnit 5, AssertJ, `showcase-test`, and the `ksuid` library already present.
- **Behavior**: unchanged.