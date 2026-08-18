## 1. Command-service configuration and tests

- [x] 1.1 Add `saga-cache`, `saga-associations-cache`, and `showcase-snapshot-trigger` blocks to
      `showcase-command-service/src/main/resources/application.yml` with env-var defaults mirroring the Java field
      defaults (`SAGA_CACHE_MAX_SIZE=1000`, `SAGA_CACHE_EXPIRES_AFTER_ACCESS=PT10M`, `SAGA_CACHE_EXPIRES_AFTER_WRITE=PT5M`;
      `SAGA_ASSOCIATIONS_CACHE_*` same defaults; `SHOWCASE_SNAPSHOT_TRIGGER_LOAD_TIME_THRESHOLD=PT0.5S`)
- [x] 1.2 Rework `ShowcaseCommandPropertiesCT`: defaults test asserting every field (booleans, 3 caches at
      1000/PT10M/PT5M, snapshot trigger at 500ms), env-var-form binding tests for `EXIT_AFTER_FLYWAY_MIGRATION` and the
      `SHOWCASE_CACHE_*`, `SAGA_CACHE_*`, `SAGA_ASSOCIATIONS_CACHE_*`, and `SHOWCASE_SNAPSHOT_TRIGGER_LOAD_TIME_THRESHOLD`
      vars, constraint tests asserting context failure for `maximum-size=-1` on each of the three caches, and a
      yml-wiring test loading `src/main/resources/application.yml` via `ConfigDataApplicationContextInitializer`
      asserting the yml defaults bind and a simulated `SAGA_CACHE_MAX_SIZE` env var overrides the yml default through
      the placeholder (design D6)

## 2. Query-service tests

- [x] 2.1 Rework `ShowcaseQueryPropertiesCT`: defaults test asserting all three booleans (`indexInitializationEnabled`
      true, `exitAfterIndexInitialization` false, `validationEnabled` true), env-var-form binding tests for
      `INDEX_INITIALIZATION_ENABLED` and `EXIT_AFTER_INDEX_INITIALIZATION`, keeping the existing
      `SHOWCASE_QUERY_VALIDATION_ENABLED` coverage, and a yml-wiring test loading the service `application.yml` via
      `ConfigDataApplicationContextInitializer` asserting the `showcase.query.*` placeholders bind their yml defaults
      and a simulated `SHOWCASE_QUERY_VALIDATION_ENABLED` env var overrides the yml default (design D6)

## 3. API-gateway tests

- [x] 3.1 Add `ShowcaseApiPropertiesCT` in `showcase-api-gateway/src/componentTest/java/showcase/api/`: defaults test
      asserting both cache map entries (`FetchShowcaseListQuery`, `FetchShowcaseByIdQuery`) at 1000/PT10M/PT5M,
      env-var-form binding tests for the six `FETCH_SHOWCASE_LIST_QUERY_CACHE_*` and `FETCH_SHOWCASE_BY_ID_QUERY_CACHE_*`
      vars, constraint tests asserting context failure for `maximum-size=-1` and `expires-after-access=PT-1S` on a
      cache entry, and a yml-wiring test loading the gateway `application.yml` via
      `ConfigDataApplicationContextInitializer` asserting the `showcase.api.caches.*` and `showcase.query.api-url`
      placeholders bind their yml defaults and a simulated `SHOWCASE_QUERY_SERVICE_URL` env var overrides the yml
      default (design D6; also covers the query-client wiring since its placeholder lives in the gateway yml)

## 4. Projection-service suite and tests

- [x] 4.1 Add a `componentTest` JVM test suite to `showcase-projection-service/build.gradle.kts` mirroring the
      query-service suite (shared dependencies, `shouldRunAfter(test)`, the `AllowRedefinitionToAddDeleteMethods` and
      `EnableDynamicAgentLoading` jvmArgs). The existing `integrationTest` ordering was corrected from
      `shouldRunAfter(test)` to `shouldRunAfter(componentTest)` so the suite order stays test → componentTest →
      integrationTest
- [x] 4.2 Add `ShowcaseProjectorPropertiesCT` in `showcase-projection-service/src/componentTest/java/showcase/projection/`:
      defaults test asserting all 11 fields (`minConcurrency=1`, `maxConcurrency=256`, batch 100/PT0.1S/10000, retry
      3/PT0.1S, restart PT10S), env-var-form binding tests for the eight `PROJECTOR_*` vars, constraint tests
      asserting context failure for the 15 out-of-range values (below/above bounds for `min/maxConcurrency`,
      `batch.maxSize`/`maxTime`/`bufferMaxSize`, `retry.maxAttempts`/`minBackoff`, `restart.delay`), and a yml-wiring
      test loading the service `application.yml` via `ConfigDataApplicationContextInitializer` asserting the
      `showcase.projector.*` placeholders bind their yml defaults and a simulated `PROJECTOR_MAX_CONCURRENCY` env var
      overrides the yml default (design D6)

## 5. Query-client tests

- [x] 5.1 Add `ShowcaseQueryClientPropertiesCT` in `showcase-query-client/src/componentTest/java/showcase/query/`:
      env-var-form binding test for `SHOWCASE_QUERY_SERVICE_URL`, and constraint tests asserting context failure for
      `apiUrl=""` (`@NotEmpty`) and `apiUrl=not-a-url` (`@URL`). No yml-wiring test: the query-client module has no
      `application.yml` — its `SHOWCASE_QUERY_SERVICE_URL` placeholder lives in the api-gateway yml and is covered by
      the gateway yml test in 3.1 (design D6)

## 6. Helm chart

- [x] 6.1 Add `sagaCache`, `sagaAssociationsCache`, and `showcaseSnapshotTrigger` sections with `@param` docs to
      `helm/chart/src/main/helm/values.yaml`, mirroring the existing `showcaseCache` block
- [x] 6.2 Add the seven env vars (`SAGA_CACHE_*` x3, `SAGA_ASSOCIATIONS_CACHE_*` x3,
      `SHOWCASE_SNAPSHOT_TRIGGER_LOAD_TIME_THRESHOLD`) to the command-service deployment env in
      `helm/chart/src/main/helm/templates/command-service/deployment.yaml`, following the `SHOWCASE_CACHE_*` pattern.
      The new env vars render unconditionally from `values.yaml` defaults (no conditional branches), so the lint value
      files (`helm-lint-full.yaml` / `helm-lint-minimal.yaml`) need no changes; task 7.2 lints both branches

## 7. Verification

- [x] 7.1 Run `./gradlew :showcase-command-service:componentTest :showcase-query-service:componentTest
      :showcase-api-gateway:componentTest :showcase-projection-service:componentTest :showcase-query-client:componentTest`
      and confirm all pass
- [x] 7.2 Run `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal` and confirm the chart
      lints clean with the new values