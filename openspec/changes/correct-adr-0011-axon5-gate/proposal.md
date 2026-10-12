# Proposal

## Why

ADR-0011 defers the Axon Framework 5 migration behind what it recorded as an external coordinate gate, but the gate is
written against coordinates Axon has since renamed, so it can never fire as written — and the 2026-10-11 architecture
audit read it as "appears met". A re-check against the pinned coordinates and AxonIQ's own migration sources shows
neither reading is right: the framework, Spring Boot, Reactor, Micrometer-metrics, and OpenTelemetry-tracing extensions
now have stable 5.x under renamed coordinates, the Kafka extension is still a placeholder, and the AF4 JGroups
distributed command bus has moved into AxonIQ's **commercial** offering — so command routing is a decision, not an
upstream wait. The deferral stands; the record of why and when it reopens is stale on all three surfaces that state it.

## What Changes

- `docs/adr/0011-defer-axon-framework-5-migration.md`: `Revisit when:` reworded to the current coordinates and the
  routing decision; the `Context` corrected to the measured landscape (with the re-check recorded); the "sibling of
  ADR-0009, not a consequence of it" paragraph corrected, since AF5's commercial routing move couples them; and the
  `## Decision` reopen paragraph plus the `## Related decisions` classification corrected, both of which repeat the same
  gate.
- `showcase/quality/dependency-management` (delta): the Axon requirement's rationale body rewritten — the suppression
  outcome and every scenario unchanged.
- `config/dependency-updates/major-disabled.txt`: the `org.axonframework` comment's "the extensions/modules used have no
  5.x" wording corrected; it still names ADR-0011.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-management`: the Axon-suppression requirement's rationale body (the rule and its
  scenarios are unchanged).

## Impact

- Files: `docs/adr/0011-defer-axon-framework-5-migration.md`, `config/dependency-updates/major-disabled.txt`, and the
  delta spec for `showcase/quality/dependency-management`.
- No code, build, or deployment impact. The routing decision itself (commercial distributed messaging versus a
  re-architected command routing) is recorded as open, not taken here — it would amend ADR-0009 and is the owner's call.
