## Context

See proposal.md - Why. `showcase-projection-model` has no tests. `ShowcaseEntity` is the read-side OpenSearch document:
its Jackson serialization (via `@Jacksonized` + `@AllArgsConstructor(PRIVATE)` + `@Builder`) and its Spring Data
Elasticsearch mapping annotations are the only contract between the projector (write) and the query services (read). The
module's `build.gradle.kts` currently declares no `testing` block; per the test-tier convention in AGENTS.md, tests that
exercise real serializers live in `src/componentTest/java` with the `CT` suffix, and the `componentTest` suite must be
registered per-module (as done in `showcase-query-proto/build.gradle.kts`).

## Goals / Non-Goals

**Goals:**

- Lock the two unprotected surfaces of `ShowcaseEntity` with fast, in-process component tests: the Jackson round-trip
  and the derived OpenSearch mapping.
- Follow the project's existing test conventions: `componentTest` suite, `CT` suffix, `@DisplayName` on class and every
  test method, no comments, 120-char wrap.
- Keep the tests deterministic (fixed values, no randomness) and dependency-light.

**Non-Goals:**

- Adding `RandomShowcaseEntityTestUtils` or any testFixtures source set — postponed until a consumer exists.
- Refactoring `showcase-projection-service` or `showcase-query-client` to consume the entity fixture.
- Verifying behavior against a live OpenSearch instance (that belongs to the existing ITs in
  `showcase-projection-service`); these are pure in-process unit-style checks at the component tier.

## Decisions

- **Tier: component test, not unit test.** AGENTS.md defines a test's tier by its collaborators: using the *real*
  Jackson `ObjectMapper` and the *real* Spring Data `ElasticsearchMappingContext` makes these component tests, even
  though they run in-process with no containers. Hence `src/componentTest/java`, suffix `CT`, and an explicit
  `componentTest` suite registration. Alternative considered and rejected: calling them unit tests (`src/test`) — that
  would mislabel the tier and leave them out of the suite ordering convention (`test` → `componentTest` →
  `integrationTest`).

- **`ShowcaseEntityJacksonCT`: use a plain `new ObjectMapper()` plus `JavaTimeModule`.** The entity has no Spring
  context, so no `ObjectMapper` bean exists to reuse; a hand-built mapper with the JSR310 module is the smallest real
  serializer for the round-trip. `@Jacksonized` drives deserialization through the generated builder without extra
  config. Alternative considered and rejected: a `Jackson2ObjectMapperBuilder` or Spring `BeanFactory` — it pulls a
  Spring context in for no benefit at the component tier.

- **`ShowcaseEntityMappingCT`: derive the mapping with `SimpleElasticsearchMappingContext` and assert on
  `ElasticsearchPersistentEntity`/`ElasticsearchPersistentProperty`.** This exercises the same annotation-reading path
  the framework uses to create the index, so a change to `@Document`, `@Setting`, `@Field`, or `@DateFormat` that drifts
  from the intended mapping fails the test. Assertions: index name (`showcases`), the `showcaseId` sort setting, field
  types (`Keyword` for `showcaseId`/`status`, `Text` for `title`, `Date_Nanos` for the timestamps), and the
  `strict_date_optional_time_nanos` formats. Alternative considered and rejected: reading the annotations with plain
  reflection — it would verify that annotations exist, not that the framework interprets them the way the entity author
  intends (e.g. default `Date` type vs `Date_Nanos`, missing `format`).

- **Deterministic fixed fixtures in the tests.** Unlike the `Random*TestUtils` family, these tests use fixed values
  (e.g. a known showcase ID, an `Instant` with nanosecond precision, a `Duration`), so failures are reproducible and the
  assertions can pin exact values. No dependency on `showcase-test` is needed.

- **`componentTest` suite wiring mirrors `showcase-query-proto`.** Add the `testing { suites {
  register<JvmTestSuite>("componentTest") } }` block with `jackson2.databind` + `jackson2.jsr310` test dependencies and
  `shouldRunAfter(test)`. The `ElasticsearchMappingContext` classes are already on the compile classpath via the
  module's existing `api(libs.spring.data.opensearch)`, so no production dependency changes.

## Risks / Trade-offs

- [The mapping assertions are coupled to Spring Data Elasticsearch internals (`ElasticsearchPersistentProperty`
  accessors), which could change across framework upgrades] → Mitigation: assertions use the stable public accessors
  (`getFieldType()`, `getDateFormat()`); the component test is fast and local, so an upgrade break is caught the moment
  it lands and is a one-line fix.
- [A hand-built `ObjectMapper` may serialize differently than the one configured at runtime in the services] →
  Mitigation: the purpose here is to lock the *entity-level* contract (builder deserialization, nulls, nanos); the
  services' serializer configuration is already exercised by their integration tests.
- [The `componentTest` suite adds a Gradle task to the module] → Mitigation: it is wired identically to the existing
  per-module pattern and runs `shouldRunAfter(test)`, consistent with the other modules.
