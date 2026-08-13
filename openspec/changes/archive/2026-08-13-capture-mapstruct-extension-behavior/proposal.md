## Why

The `showcase-mapstruct-extension` module provides a custom MapStruct `AccessorNamingStrategy` that bridges Lombok's
`@Accessors(fluent = true)` with MapStruct's property detection at compile time. Its behavior is undocumented in the
spec catalog, making it difficult to reason about changes that touch mapper generation, fluent accessor patterns, or
the query-service's entity-to-DTO mapping pipeline.

## What Changes

- Adds a new spec capturing the `showcase-mapstruct-extension` module's behavioral contract: fluent getter detection
  rules, property name resolution, coexistence with standard JavaBean accessors, SPI registration, and the module's
  compile-time-only consumption model as an annotation processor.

## Capabilities

### New Capabilities

- `showcase/mapstruct-extension`: Fluent accessor naming strategy for MapStruct — covers the `AccessorNamingStrategy`
  SPI integration, fluent getter detection logic (parameterless method matching a field by name and return type),
  property name resolution for fluent vs standard accessors, and the module's role as a compile-time annotation
  processor dependency.

### Modified Capabilities

_(none — this is a pure documentation capture with no behavioral changes)_

## Impact

- **Specs only**: no code, build, or deployment changes.
- Adds `openspec/specs/showcase/mapstruct-extension/spec.md` to the spec catalog.
- Future changes touching MapStruct mapper generation or fluent accessor handling can reference this spec as a baseline.
