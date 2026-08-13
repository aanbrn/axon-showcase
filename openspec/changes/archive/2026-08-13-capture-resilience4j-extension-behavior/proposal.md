## Why

The `showcase-resilience4j-extension` module provides a Spring Boot `AutoConfigurationImportFilter` that enables
hierarchical, property-driven control over which Resilience4j feature auto-configurations are imported — a master
`resilience4j.enabled` switch plus per-feature flags. Its behavior is undocumented in the spec catalog, making it
difficult to reason about changes that touch resilience configuration, feature toggling, or auto-configuration
filtering.

## What Changes

- Adds a new spec capturing the `showcase-resilience4j-extension` module's behavioral contract: the
  `AutoConfigurationImportFilter` SPI integration, the master and per-feature enablement flags with their defaults,
  the regex-based auto-configuration class matching, the bulkhead dual-flag requirement, and the module's runtime
  consumption model as an `implementation` dependency in client modules.

## Capabilities

### New Capabilities

- `showcase/resilience4j-extension`: Resilience4j auto-configuration import filtering — covers the
  `AutoConfigurationImportFilter` SPI registration, hierarchical property-based enablement (master switch plus
  per-feature flags), regex matching of Resilience4j auto-configuration classes, the bulkhead dual-flag coupling,
  Spring configuration metadata for IDE support, and the module's role as a runtime `implementation` dependency.

### Modified Capabilities

_(none — this is a pure documentation capture with no behavioral changes)_

## Impact

- **Specs only**: no code, build, or deployment changes.
- Adds `openspec/specs/showcase/resilience4j-extension/spec.md` to the spec catalog.
- Future changes touching Resilience4j feature toggling or auto-configuration filtering can reference this spec as a
  baseline.
