## 1. Unit tests (src/test)

- [x] 1.1 Add `@DisplayName` (class + method level, static sentences) to the 13 test classes under
      `showcase-command-api/src/test/java/showcase/command/`
- [x] 1.2 Add `@DisplayName` (class + method level, static sentences) to the 5 test classes under
      `showcase-query-api/src/test/java/showcase/query/`
- [x] 1.3 Add `@DisplayName` (class + method level, static sentences) to the 4 test classes under
      `showcase-identifier-extension/src/test/`, `showcase-mapstruct-extension/src/test/`, and
      `showcase-resilience4j-extension/src/test/`

## 2. Component tests (src/componentTest)

- [x] 2.1 Add `@DisplayName` to `ShowcaseCommandClientCT` including its `@Nested Retry` group
      (`showcase-command-client/src/componentTest/java/showcase/command/ShowcaseCommandClientCT.java`)
- [x] 2.2 Add `@DisplayName` to `ShowcaseQueryClientCT` including its `@Nested TimeLimiter` and `@Nested Retry` groups
      (`showcase-query-client/src/componentTest/java/showcase/query/ShowcaseQueryClientCT.java`)
- [x] 2.3 Add `@DisplayName` to `ShowcaseAggregateCT` and `ShowcaseSagaCT`
      (`showcase-command-service/src/componentTest/java/showcase/command/`)
- [x] 2.4 Add `@DisplayName` to `ShowcaseApiControllerCT`
      (`showcase-api-gateway/src/componentTest/java/showcase/api/ShowcaseApiControllerCT.java`)
- [x] 2.5 Add `@DisplayName` to `QueryMessageRequestMapperCT`
      (`showcase-query-proto/src/componentTest/java/showcase/query/QueryMessageRequestMapperCT.java`)

## 3. Integration tests (src/integrationTest)

- [x] 3.1 Add `@DisplayName` to `ShowcaseCommandGatewayIT` and `ShowcaseTitleReservationIT`
      (`showcase-command-service/src/integrationTest/java/showcase/command/`)
- [x] 3.2 Add `@DisplayName` to `ShowcaseCommandClientIT` (`showcase-command-client/src/integrationTest/`) and
      `ShowcaseQueryClientIT` (`showcase-query-client/src/integrationTest/`)
- [x] 3.3 Add `@DisplayName` to `ShowcaseProjectorIT` (`showcase-projection-service/src/integrationTest/`) and
      `ShowcaseQueryControllerIT` (`showcase-query-service/src/integrationTest/`)

## 4. Conventions and verification

- [x] 4.1 Document the `@DisplayName` convention (class, `@Nested`, and method level; static sentences; keep named
      `argumentSet`s) in the Conventions section of `AGENTS.md`
- [x] 4.2 Run the unit tests of every affected module
      (`./gradlew :showcase-command-api:test :showcase-query-api:test :showcase-identifier-extension:test
      :showcase-mapstruct-extension:test :showcase-resilience4j-extension:test`)
- [x] 4.3 Run the component tests of every affected module
      (`./gradlew :showcase-command-client:componentTest :showcase-query-client:componentTest
      :showcase-command-service:componentTest :showcase-api-gateway:componentTest :showcase-query-proto:componentTest`)
- [x] 4.4 Confirm every `@Test`/`@ParameterizedTest` method and test class in the four test tiers carries a
      `@DisplayName`, by grepping for any remaining unannotated test files
- [x] 4.5 Run the integration tests of every affected module (requires Docker)
      (`./gradlew :showcase-command-service:integrationTest :showcase-command-client:integrationTest
      :showcase-query-client:integrationTest :showcase-projection-service:integrationTest
      :showcase-query-service:integrationTest`)
