## 1. Remove dead WebExchangeBindException overload

- [x] 1.1 In `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiErrorResolver.java`, remove the
      `resolve(WebExchangeBindException, Locale, ProblemDetail)` overload and its Javadoc, and remove the now-unused
      `org.springframework.web.bind.support.WebExchangeBindException` import.
- [x] 1.2 In `showcase-api-gateway/src/componentTest/java/showcase/api/ShowcaseApiErrorResolverCT.java`, remove the four
      `resolve_webExchangeBindException_*` tests, and remove the now-unused
      `org.springframework.web.bind.support.WebExchangeBindException` import.

## 2. Verification

- [x] 2.1 Run `./gradlew :showcase-api-gateway:componentTest` and confirm the remaining `ShowcaseApiErrorResolverCT`
      tests pass.
- [x] 2.2 Run `./gradlew :showcase-api-gateway:jacocoTestReport` and confirm the coverage gate is unaffected by the
      removal.
