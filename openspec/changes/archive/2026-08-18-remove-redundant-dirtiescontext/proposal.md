# Proposal: Remove redundant @DirtiesContext usages

## Why

`@DirtiesContext` tears down the Spring context after a test class (releasing global JVM state such as JGroups ports,
JCache caches, and `System.exit`-capable handlers). It is only needed where a full-context boot leaks such global state
— not on contexts that are safely cacheable. Several ITs/CTs use it defensively where it buys nothing.

## What Changes

- Remove `@DirtiesContext` from contexts with no JGroups/JCache global state (verified to pass without it):
  - `ShowcaseProjectorIT` (projection service — no JGroups/JCache, single IT).
  - `ShowcaseQueryClientIT` (top-level class and its three `@ActiveProfiles` nested classes — distinct profiles already
    get separate cached contexts).
  - `ShowcaseCommandClientCT` `Retry` nested class (distinct `@ActiveProfiles`).
- Keep `@DirtiesContext` on the full-context ITs that boot JGroups/JCache and require teardown between classes:
  `ShowcaseApiApplicationIT`, `ShowcaseCommandApplicationIT`, `ShowcaseCommandApplicationExitAfterFlywayMigrationIT`,
  `ShowcaseCommandGatewayIT`, `ShowcaseSagaDeadlinesIT`, and `ShowcaseCommandClientIT`.
- Test-only; no application code changes.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — test-only; `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- Removed `@DirtiesContext` annotations (and now-unused imports) from the listed integration/component tests; no
  production code changes.
