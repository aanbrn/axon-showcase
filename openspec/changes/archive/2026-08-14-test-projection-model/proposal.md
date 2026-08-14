## Why

`showcase-projection-model` — the read-side projection contract — ships `ShowcaseEntity` and `ShowcaseStatus` with no
tests at all. The entity carries the full OpenSearch mapping (index name, sort, per-field types and date formats) and is
serialized to and from JSON with a `@Jacksonized` builder, so drift in either surface silently corrupts documents or
breaks queries. Both surfaces are currently unprotected.

## What Changes

- Add two component tests (`src/componentTest/java`) to `showcase-projection-model`:
    - `ShowcaseEntityJacksonCT` — round-trip serialization/deserialization of `ShowcaseEntity` through a real Jackson
      `ObjectMapper` with JSR310 support, covering `@Jacksonized` builder deserialization, `Duration`/`Instant` (nanos)
      handling, and null-field behavior.
    - `ShowcaseEntityMappingCT` — derives the real OpenSearch mapping from `ShowcaseEntity` via Spring Data
      Elasticsearch's
      `ElasticsearchMappingContext` and asserts the contract: `indexName` = `showcases`, the `showcaseId` sort setting,
      and per-field types (`Keyword`, `Text`, `Date_Nanos`) with the `strict_date_optional_time_nanos` formats.
- Register a `componentTest` test suite in `showcase-projection-model/build.gradle.kts` (mirroring the wiring in
  `showcase-query-proto/build.gradle.kts`), with `jackson-databind` and `jackson-datatype-jsr310` as component-test
  dependencies.
- No production code, no other module, and no testFixtures change.

## Capabilities

### New Capabilities

None — this change adds tests only and does not alter any behavior.

### Modified Capabilities

None.

## Impact

- **Code touched**: `showcase-projection-model/build.gradle.kts` plus two new component-test classes under
  `showcase-projection-model/src/componentTest/java/showcase/projection/`.
- **Dependencies**: component-test scope gains `jackson-databind` and `jackson-datatype-jsr310`. The Spring Data
  Elasticsearch mapping-context types are already available transitively through the module's existing
  `api(libs.spring.data.opensearch)`.
- **Systems**: none — component tests run in-process, no containers, no infrastructure.
- **Deliberately out of scope**: `RandomShowcaseEntityTestUtils` in testFixtures (postponed until a consumer exists),
  and any refactor of `showcase-projection-service` or `showcase-query-client`.
