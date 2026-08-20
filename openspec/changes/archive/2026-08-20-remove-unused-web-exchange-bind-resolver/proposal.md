## Why

The `resolve(WebExchangeBindException, Locale, ProblemDetail)` overload on `ShowcaseApiErrorResolver` is dead code: the
only production `@ExceptionHandler` that used it (`handleWebExchangeBindException`) was removed in the
`add-gateway-client-error-handling-tests` change, which established that this WebFlux gateway never produces a
`WebExchangeBindException` (it is only emitted by `@ModelAttribute`/`BindingResult` binding). The overload is now
referenced exclusively by its own component tests. This change removes the dead overload and its tests, aligning with
the repo's convention of removing unreachable defensive error-handling code.

## What Changes

- Remove the `resolve(WebExchangeBindException, Locale, ProblemDetail)` overload (and its Javadoc) from
  `ShowcaseApiErrorResolver`, together with its now-unused `WebExchangeBindException` import.
- Remove the four `resolve_webExchangeBindException_*` component tests from `ShowcaseApiErrorResolverCT`, together with
  their now-unused `WebExchangeBindException` import.
- No behavioral change: the remaining `resolve(HandlerMethodValidationException, ...)` overload and its tests are
  untouched, and the controller's `handleHandlerMethodValidationException` path is unaffected.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. This is a pure dead-code cleanup with no spec-level behavior change; the change sets `skip_specs: true`.

## Impact

- `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiErrorResolver.java` — remove the dead overload and its
  `WebExchangeBindException` import.
- `showcase-api-gateway/src/componentTest/java/showcase/api/ShowcaseApiErrorResolverCT.java` — remove the four
  `resolve_webExchangeBindException_*` tests and the `WebExchangeBindException` import.
- Coverage: the jacoco gate's denominator shrinks by the removed dead overload; remaining component tests are
  unaffected.
