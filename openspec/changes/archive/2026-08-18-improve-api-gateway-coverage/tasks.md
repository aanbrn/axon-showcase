## 1. Application bean-wiring integration test

- [x] 1.1 Register the `integrationTest` suite in `showcase-api-gateway/build.gradle.kts` (deps for the full context)
- [x] 1.2 `ShowcaseApiApplicationIT`: full-context boot verifies the JGroups connector, primary distributed command bus,
      cache-manager custom caches, security chain, and the two caches

## 2. Error resolver

- [x] 2.1 Extract `ShowcaseApiErrorResolver` (the `HandlerMethodValidationException` visitor + `WebExchangeBindException`
      mapping) from the controller, with javadocs
- [x] 2.2 `ShowcaseApiErrorResolverCT`: cover all visitor methods and the `WebExchangeBindException` branches

## 3. Controller component test

- [x] 3.1 Replace the mocked async caches in `ShowcaseApiControllerCT` with real in-process Caffeine caches (`@BeforeEach`
      clearing, cache-content assertions)

## 4. Gate and verification

- [x] 4.1 Re-enable the coverage gate (remove `extra["coverage.gate.enabled"] = false`)
- [x] 4.2 `jacocoTestReport` shows >= 80% (~82%) and the gate passes
