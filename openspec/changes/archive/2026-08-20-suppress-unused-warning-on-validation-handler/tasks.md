## 1. Suppress the unused-method warning

- [x] 1.1 In `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiController.java`, add
      `@SuppressWarnings("unused")` above `handleHandlerMethodValidationException`, matching the existing pattern on
      `handleException`.

## 2. Verification

- [x] 2.1 Run `./gradlew :showcase-api-gateway:compileJava` and confirm the `UnusedMethod` warning for
      `handleHandlerMethodValidationException` is gone.
- [x] 2.2 Run `./gradlew :showcase-api-gateway:componentTest` and confirm the validation handling tests still pass.
