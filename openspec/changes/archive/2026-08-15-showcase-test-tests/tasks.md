## 1. Add the mockito dependency

- [x] 1.1 Add `testImplementation(libs.mockito.core)` to `showcase-test/build.gradle.kts`.

## 2. Write RandomTestUtilsTests

- [x] 2.1 Create `showcase-test/src/test/java/showcase/test/RandomTestUtilsTests.java` with unit tests for
  `anAlphabeticString` (exact length, alphabetic content), `anElementOf` for array and list (membership, empty-input
  `IllegalArgumentException`, single-element determinism), and `anEnum` (returned constant is one of the enum's values;
  enum with no constants throws `IllegalArgumentException`). Follow the repo conventions: class suffix `Tests`,
  `@DisplayName` on the class and every test method.

## 3. Write KafkaTestPublisherTests

- [x] 3.1 Create `showcase-test/src/test/java/showcase/test/KafkaTestPublisherTests.java` with a mocked
  `KafkaPublisher<?, ?>` and unit tests covering: domain-message construction (aggregate type, extracted identifier,
  sequence number 0, payload), per-aggregate sequence increments (same aggregate 0 then 1; two aggregates each start at
  0), `publishEventTwice` (two sends), `publishEvents` (one send per event), and `NullPointerException` when the
  identifier extractor returns null. Follow the repo conventions: class suffix `Tests`, `@DisplayName` on the class and
  every test method.

## 4. Verify

- [x] 4.1 Run `./gradlew :showcase-test:test` and confirm all tests pass.
- [x] 4.2 Run `./gradlew :showcase-test:check` and confirm the full check suite (compile → spotbugs → errorprone → test)
  passes.