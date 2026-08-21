# Proposal: Update the Jackson 3 BOM on minor versions and amend ADR-0003

## Why

Jackson 3 is already present on the runtime classpaths of `showcase-query-service` and `showcase-projection-service`:
`co.elastic.clients:elasticsearch-java` (pulled by the OpenSearch client) brings in `tools.jackson.core:jackson-databind`
transitively, and the platform's `jackson3-bom` (currently `3.1.6`) constrains it. The `dependencyUpdates` report
surfaces a minor BOM update (`tools.jackson:jackson-bom [3.1.6 -> 3.2.2]`). ADR-0003 defers *adopting* Jackson 3 as the
serialization backend in application code, but the project already runs against Jackson 3 transitively — keeping the
BOM on current minors is dependency hygiene, not the deferred migration. The ADR does not currently acknowledge this
transitive presence, so the record is slightly stale.

## What Changes

- `gradle/libs.versions.toml`: bump `jackson3-bom` from `3.1.6` to `3.2.2`.
- `docs/adr/0003-retain-jackson-2-defer-jackson-3.md`: amend with a short note clarifying that Jackson 3 is already
  present transitively via `elasticsearch-java` (on the query and projection service classpaths) and that the
  `jackson3-bom` is kept current on minor versions, while backend adoption (replacing Jackson 2 in application code)
  remains deferred.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. No spec-level requirement changes; this is a dependency-version bump plus an ADR amendment. `skip_specs: true`.

## Impact

- **Code**: `gradle/libs.versions.toml` (one version value).
- **Docs**: `docs/adr/0003-retain-jackson-2-defer-jackson-3.md` (amended with a short clarification note).
- **Build**: Jackson 3 artifacts resolve to `3.2.2` on the query/projection runtime classpaths; `dependencyUpdates`
  stops listing the minor BOM update.
- **Tests**: no test changes; verification runs the affected services' build/tests and confirms resolution to `3.2.2`.