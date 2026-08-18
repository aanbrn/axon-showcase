## Context

`@DirtiesContext` forces Spring to tear down the context after a class. It is necessary when a full-context boot leaks
global JVM state (JGroups ports + system properties, JCache's JVM-global cache manager, `System.exit`-capable handlers);
without it, the next full-context class collides (e.g. "Cache showcase-cache already exists", JGroups port conflicts).
See proposal.md - Why for motivation.

## Goals / Non-Goals

**Goals:**

- Remove `@DirtiesContext` where the context is safely cacheable, keeping only the cases that genuinely need teardown.

**Non-Goals:**

- No runtime behavior changes; no production code changes.

## Decisions

- **Keep `@DirtiesContext` where full-context JGroups/JCache state is booted** (the six gateway/command/client ITs).
- **Remove it where no such global state exists**, verified empirically by removing and running each suite:
  - `ShowcaseProjectorIT` — no JGroups/JCache; a single IT with no competing context.
  - `ShowcaseQueryClientIT` top-level and its three `@ActiveProfiles` nested classes — distinct profiles already get
    separate cached contexts, so no teardown is required.
  - `ShowcaseCommandClientCT` `Retry` nested — same reasoning (distinct `@ActiveProfiles`).

## Risks / Trade-offs

- [Removing `@DirtiesContext` could hide a subtle cross-context leak] → mitigated by verifying each removal by running
  the suite; only profiles/slices without JGroups/JCache were touched.
