## Context

See proposal.md - Why. `ShowcaseApiErrorResolverCT` has 9 copy-paste test methods, 9 magic-number
index constants, and an index-based switch in `exceptionAt` that duplicates knowledge implicit in
the parameter types. The test also misses the name-fallback path — the common production path for
bare `@RequestParam`/`@PathVariable` — and multi-error accumulation.

## Goals / Non-Goals

**Goals:**
- Rewrite the test as a single `@ParameterizedTest` with a scenario table.
- Replace index-based dispatch with type-based dispatch (no magic numbers).
- Add coverage for the name-fallback and multi-error paths.

**Non-Goals:**
- Not touching `ShowcaseApiErrorResolver` (production code stays as-is).
- Not testing the `@Nullable` `ModelAttribute`/`RequestParam` visitor paths: Spring passes null
  annotations only via custom dispatch predicates (verified in
  `HandlerMethodValidationException.visitResults` — the default predicates use direct-annotation
  lookup, `MethodParameter.getParameterAnnotation`), which this application does not use. The
  resolver's `Optional.ofNullable` handling remains untested defensive code.
- Not introducing new dependencies or build configuration.

## Decisions

- **Replace the single 9-parameter `Controller.handle` with 9 single-parameter methods.**
  - Each method name is the parameter kind — no index constants needed. The resolver visits
    individual parameter results, so the method shape as a whole is irrelevant to behavior.
  - Alternative: keep one method and use an enum instead of int constants. Rejected — still
    requires keeping the enum in sync with parameter order and still needs the index switch.

- **Type-based dispatch in `exceptionFor(String methodName)`.**
  - `method.getParameterTypes()[0] == Payload.class` → `ParameterErrors`, else
    `ParameterValidationResult` — the same criterion Spring's `asErrors` uses.
  - Alternative: explicit per-method dispatch. Rejected — duplicates type knowledge already in the
    fixture method signatures.

- **By-name `controllerMethod(String name)` lookup.**
  - Fixture method names are unique, so filtering `getDeclaredMethods()` by name avoids passing a
    redundant `paramType` argument that the caller cannot know before resolving the method.

- **Parameterized test with `argumentSet(...)`.**
  - Matches the established pattern in ~20 other test classes in this repo.
  - Alternative: keep 9 `@Test` methods. Rejected — identical structure, more noise.

- **Add name-fallback rows with a bare `@RequestParam` and a bare `@PathVariable`.**
  - The blank annotation name exercises the name-resolution chain — the resolver has two variants:
    `Optional.ofNullable(annotation).map(name)` for `@RequestParam`/`@ModelAttribute` and
    `Optional.of(annotation.name())` for the remaining annotations; each variant gets one fallback
    row. The fallback is the most common production path (`@RequestParam(required = false) String
    title`, `@PathVariable String showcaseId`).
  - Each fallback fixture names its parameter distinctly (e.g. `value`) rather than using the
    production name (`showcaseId`), so the expected key is visibly the parameter name and does not
    coincide with the annotation-name path row (which asserts `id` for the path variable).
  - Alternative: keep only explicit-name rows. Rejected — they dodge the common path.

- **Add a multi-error accumulation row.**
  - Two resolvable errors on one parameter must both appear in the message list.
  - Alternative: skip. Rejected — the proposal's Why lists it as a gap; without a row the claim
    would be dishonest.

## Risks / Trade-offs

- [Single-parameter fixture methods are less realistic than a multi-parameter method] → Mitigation:
  the resolver visits individual parameter results, so realism buys nothing testable.
- [Type-based dispatch breaks if a future bean-kind parameter uses `String` or vice versa] →
  Mitigation: the dispatch criterion is visible in each fixture method signature next to its
  `argumentSet` row.
- [By-name lookup could resolve an unexpected method if names collide] → Mitigation: fixture
  method names are unique by construction; a missing method fails loudly with
  `IllegalStateException`.

## Migration Plan

1. Rewrite `ShowcaseApiErrorResolverCT` (fixtures, dispatch, parameterized test, new rows).
2. Run `./gradlew :showcase-api-gateway:componentTest` to verify all rows pass.
3. Confirm `git diff` touches only the CT file.

## Open Questions

- None.
