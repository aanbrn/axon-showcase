## 1. Remove unused messageSource field

- [x] 1.1 In `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiController.java`, remove the `messageSource`
      field and its Javadoc, and remove the now-unused `org.springframework.context.MessageSource` import.

## 2. Verification

- [x] 2.1 Run `./gradlew :showcase-api-gateway:componentTest` and confirm it passes with no `UnusedVariable` warning
      for `ShowcaseApiController`.
- [x] 2.2 Run `./gradlew :showcase-api-gateway:integrationTest` to confirm the full application context still boots
      with the dependency removed.
