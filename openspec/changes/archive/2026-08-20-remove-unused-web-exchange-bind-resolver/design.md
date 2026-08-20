## Context

See proposal.md - Why. `ShowcaseApiErrorResolver` is a `@Component` invoked only from
`ShowcaseApiController.handleHandlerMethodValidationException` (via its `resolve(HandlerMethodValidationException, ...)`
overload). The sibling `resolve(WebExchangeBindException, Locale, ProblemDetail)` overload has no production caller since
the `handleWebExchangeBindException` handler was removed; it survives only in `ShowcaseApiErrorResolverCT`.

## Goals / Non-Goals

**Goals:**
- Remove the dead `resolve(WebExchangeBindException, ...)` overload and the four component tests that exercise it.
- Leave the live `resolve(HandlerMethodValidationException, ...)` overload, the controller handler, and their tests
  untouched.

**Non-Goals:**
- Not refactoring the visitor logic or error-property naming.
- Not re-adding any `WebExchangeBindException` handling in the controller.

## Decisions

- **Remove the dead overload rather than keep it as defensive completeness.**
  - The overload was orphaned by the `add-gateway-client-error-handling-tests` change, which established the gateway
    never produces a `WebExchangeBindException` (it is emitted only by `@ModelAttribute`/`BindingResult` binding, which
    this WebFlux controller does not use). Keeping it would contradict the repo's convention of removing unreachable
    defensive error-handling code.
  - Alternative rejected: keep the overload for future-proofing — it would remain untestable-in-production and add
    maintenance surface for a path the gateway cannot reach.
- **Remove the four `resolve_webExchangeBindException_*` tests together with the overload.**
  - The tests exist solely to cover a now-unreachable branch; retaining them would test dead code and keep a
    misleading import in the component test.
  - Shared helpers (`bindingWithError()`, `parameter()`, `Payload`, `Controller`, and the `MODEL`/`BODY`/`PART`/`OTHER`
    constants) are also used by the remaining `HandlerMethodValidationException` tests and are kept.

## Risks / Trade-offs

- [Removing the overload could lower measured coverage below the jacoco gate] → Mitigation: the dead overload is removed
  from the coverage denominator while live component tests remain, so the ratio does not decrease; verify with
  `./gradlew :showcase-api-gateway:componentTest` and `jacocoTestReport`.
- [A future `@ModelAttribute`-based endpoint could reintroduce a need for `WebExchangeBindException` handling] →
  Mitigation: re-add handling deliberately if such an endpoint is ever introduced, rather than keeping speculative dead
  code.

## Migration Plan

1. Remove the overload and its `WebExchangeBindException` import from `ShowcaseApiErrorResolver`.
2. Remove the four tests and the `WebExchangeBindException` import from `ShowcaseApiErrorResolverCT`.
3. Run `./gradlew :showcase-api-gateway:componentTest` and `jacocoTestReport` to confirm the remaining tests pass and
   coverage is unaffected.

## Open Questions

- None.
