## Why

The `resilience4j.thread-pool-bulkhead.enabled` property is redundant — there is no separate
`ThreadPoolBulkheadAutoConfiguration` class. Both `resilience4j.bulkhead.enabled` and
`resilience4j.thread-pool-bulkhead.enabled` gate the same single `BulkheadAutoConfiguration` import, creating
unnecessary ambiguity. Collapsing to a single `resilience4j.bulkhead.enabled` flag simplifies configuration without
losing any control.

## What Changes

- Remove the `resilience4j.thread-pool-bulkhead.enabled` property from the filter logic, configuration metadata,
  and spec.
- The bulkhead auto-configuration import is gated by `resilience4j.enabled` && `resilience4j.bulkhead.enabled` only.
- **BREAKING**: `resilience4j.thread-pool-bulkhead.enabled` is no longer recognized. Any existing configuration
  setting this property will have no effect.

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `showcase/resilience4j-extension`: Bulkhead auto-configuration is gated by a single `resilience4j.bulkhead.enabled`
  flag instead of two flags. The `resilience4j.thread-pool-bulkhead.enabled` property is removed. The per-feature
  flags list and configuration metadata are updated accordingly.

## Impact

- **Code**: `Resilience4jAutoConfigurationImportFilter.java` — remove `threadPoolBulkheadEnabled` variable, simplify
  bulkhead case to `resilienceEnabled && bulkheadEnabled`.
- **Config metadata**: `additional-spring-configuration-metadata.json` — remove the
  `resilience4j.thread-pool-bulkhead.enabled` entry.
- **Breaking**: any deployment setting `resilience4j.thread-pool-bulkhead.enabled=false` will silently lose that
  effect; bulkhead will now be controlled solely by `resilience4j.bulkhead.enabled`.
