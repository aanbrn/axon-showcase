## 1. Mapper tests

- [x] 1.1 Add `ShowcaseMapperTests` (unit) covering `entityToDto` and `dtoToEntity`

## 2. Controller tests

- [x] 2.1 No new controller CT needed — the existing `ShowcaseQueryControllerIT` already covers dispatch and the
      exception handlers over real HTTP; it is included in the coverage measurement

## 3. Handler tests

- [x] 3.1 Add `ShowcaseQueryHandlerCT` (component) covering the list query and the by-ID query found/not-found paths,
      composing the real `ShowcaseMapper` with the OpenSearch template faked

## 4. Application tests

- [x] 4.1 No component test for `ShowcaseQueryApplication` — the app's bean wiring is covered at the integration tier by
      the `@SpringBootTest` context boot; add `ShowcaseQueryApplicationIT` covering the OpenSearch health indicator and
      the index initialization on startup

## 5. Gate and verification

- [x] 5.1 Re-enable `showcase-query-service`'s coverage gate (remove `extra["coverage.gate.enabled"] = false`)
- [x] 5.2 Verify `./gradlew :showcase-query-service:jacocoTestReport` shows >= 80% (86%) and the gate passes
