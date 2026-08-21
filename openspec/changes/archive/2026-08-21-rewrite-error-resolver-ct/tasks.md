## 1. Rewrite ShowcaseApiErrorResolverCT

- [x] 1.1 In `showcase-api-gateway/src/componentTest/java/showcase/api/ShowcaseApiErrorResolverCT.java`,
      replace the single 9-parameter `Controller.handle` method with 9 single-parameter methods
      (`cookie`, `matrix`, `model`, `path`, `body`, `header`, `param`, `part`, `other`), each
      named after its parameter kind. Keep the class-level `@SuppressWarnings("unused")`.
- [x] 1.2 Delete the `COOKIE=0` through `OTHER=8` index constants.
- [x] 1.3 Replace `exceptionAt(int)` with `exceptionFor(String methodName)` using type-based
      dispatch (`method.getParameterTypes()[0] == Payload.class` → `ParameterErrors`, else
      `ParameterValidationResult`). Replace `controllerMethod()` with `controllerMethod(String name)`
      by-name lookup over `getDeclaredMethods()`.
- [x] 1.4 Replace the 9 `@Test` methods with a single `@ParameterizedTest` + `@MethodSource` using
      `argumentSet(...)`, preserving the existing 9 scenarios (cookie, matrix, model, path, body,
      header, param, part, other) with the same expected maps.
- [x] 1.5 Add `argumentSet` rows for the name-fallback path: fixture methods with a bare
      `@RequestParam` and a bare `@PathVariable` (no name attribute) whose annotation name is
      blank — the expected error key is the parameter name.
- [x] 1.6 Add an `argumentSet` row for multi-error accumulation: extend the `validationResult`
      helper to accept multiple resolvable errors, and assert both messages appear in the expected
      list.

## 2. Verification

- [x] 2.1 Run `./gradlew :showcase-api-gateway:componentTest` and confirm all rows pass, including
      the new name-fallback and multi-error rows.
- [x] 2.2 Confirm `git diff` touches only `ShowcaseApiErrorResolverCT.java` —
      `ShowcaseApiErrorResolver.java` must be unchanged.
