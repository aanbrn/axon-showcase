## 1. Component test suite setup

- [x] 1.1 Register a `componentTest` `JvmTestSuite` in `showcase-projection-model/build.gradle.kts` mirroring
  `showcase-query-proto`'s wiring (`shouldRunAfter(test)`)
- [x] 1.2 Add `jackson2.databind` and `jackson2.jsr310` to the `componentTest` suite dependencies (the
  `ElasticsearchMappingContext` classes come transitively via `api(libs.spring.data.opensearch)`)

## 2. Jackson round-trip test

- [x] 2.1 Create `showcase-projection-model/src/componentTest/java/showcase/projection/ShowcaseEntityJacksonCT.java`
  with a plain `ObjectMapper` + `JavaTimeModule`
- [x] 2.2 Cover `@Jacksonized` builder deserialization and exact-value round-trip of every field, including an
  `Instant` with nanosecond precision and a `Duration`
- [x] 2.3 Cover null-field behavior (entity with only some fields set round-trips with the same nulls)

## 3. OpenSearch mapping contract test

- [x] 3.1 Create `showcase-projection-model/src/componentTest/java/showcase/projection/ShowcaseEntityMappingCT.java`
  deriving the mapping via `SimpleElasticsearchMappingContext`
- [x] 3.2 Assert the index name (`showcases`) and the `showcaseId` sort setting
- [x] 3.3 Assert field types (`Keyword` for `showcaseId`/`status`, `Text` for `title`, `Date_Nanos` for timestamps) and
  the `strict_date_optional_time_nanos` date formats

## 4. Verification

- [x] 4.1 Run `./gradlew :showcase-projection-model:componentTest` and confirm all tests pass
- [x] 4.2 Run `./gradlew :showcase-projection-model:check` (compile → spotbugs → errorprone → test → componentTest) to
  confirm nothing regressed
