## Why

`ShowcaseApiErrorResolverCT` is hard to read and maintain:

- 9 copy-paste `@Test` methods with identical structure — each creates a `ProblemDetail`, calls
  `resolver.resolve(exceptionAt(INDEX), ...)`, and asserts one property entry.
- 9 magic-number index constants (`COOKIE=0` through `OTHER=8`) that must stay in sync with the
  parameter order of the single 9-parameter `Controller.handle` method; reordering a parameter
  silently breaks them.
- `exceptionAt(int)` uses an index `switch` to decide between `ParameterErrors` (bean params) and
  `ParameterValidationResult` (scalar params) — knowledge already implicit in the parameter types.
- Coverage gaps: the name-fallback path (blank annotation name → parameter name) is the common
  production path (`@RequestParam(required = false) String title`, `@PathVariable String
  showcaseId` in `ShowcaseApiController` carry no name attribute), yet the test always uses
  explicit annotation names and never exercises it; multi-error accumulation is also untested.

## What Changes

### `ShowcaseApiErrorResolverCT.java` (test only)

- Replace the single 9-parameter `Controller.handle` method with 9 single-parameter methods
  (`cookie`, `matrix`, `model`, `path`, `body`, `header`, `param`, `part`, `other`), each named
  after its parameter kind.
- Delete the `COOKIE=0` through `OTHER=8` index constants.
- Replace `exceptionAt(int)` with `exceptionFor(String methodName)` — type-driven dispatch
  (`method.getParameterTypes()[0] == Payload.class` → `ParameterErrors`, else
  `ParameterValidationResult`).
- Replace `controllerMethod()` with `controllerMethod(String name)` — by-name lookup over the
  unique fixture method names.
- Replace the 9 `@Test` methods with a single `@ParameterizedTest` + `@MethodSource` using
  `argumentSet(...)` — each row specifies the fixture method name, expected property name, and
  expected error map.
- Add `argumentSet` rows for:
  - the name-fallback path — bare `@RequestParam` and `@PathVariable` (no name attribute) whose
    annotation name is blank; the error key must be the parameter name;
  - multi-error accumulation — a parameter with two resolvable errors produces both messages.

## New Capabilities

- None.

## Modified Capabilities

- None (test-only rewrite; `ShowcaseApiErrorResolver` is untouched).

## Impact

- **Build**: no new dependencies, no build config changes.
- **Tests**: `ShowcaseApiErrorResolverCT` rewritten; the existing 9 scenarios are preserved as
  `argumentSet` rows, plus new name-fallback and multi-error rows. `ShowcaseApiControllerCT` and
  `ShowcaseApiGatewayE2E` unaffected.
- **Deployment**: no impact.
