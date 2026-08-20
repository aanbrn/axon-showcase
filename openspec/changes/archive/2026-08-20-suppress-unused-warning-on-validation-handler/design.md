## Context

See proposal.md - Why. `ShowcaseApiController.handleHandlerMethodValidationException` is a live Spring
`@ExceptionHandler`, invoked reflectively, but is not statically referenced from the catch-all `handleException` switch
(`HandlerMethodValidationException` is deliberately excluded there). ErrorProne's `UnusedMethod` therefore flags it. The
catch-all `handleException` already carries `@SuppressWarnings("unused")` for the same reflective-invocation reason.

## Goals / Non-Goals

**Goals:**
- Add `@SuppressWarnings("unused")` to `handleHandlerMethodValidationException` to silence the false-positive warning,
  consistent with the existing `handleException` suppression.
- Keep the handler fully live and its runtime behavior unchanged.

**Non-Goals:**
- Not re-adding `HandlerMethodValidationException` to the catch-all `handleException` switch.
- Not changing build configuration or ErrorProne settings.

## Decisions

- **Add `@SuppressWarnings("unused")` on the method, mirroring `handleException`.**
  - The codebase already uses this exact mechanism for the other reflective-only `@ExceptionHandler` (`handleException`),
    so it is the established, minimal convention. ErrorProne's `UnusedMethod` cannot see Spring's reflective
    `@ExceptionHandler` invocation, so the annotation is the correct targeted remedy.
  - Alternatives rejected:
    - Re-adding `HandlerMethodValidationException` to the `handleException` switch would make the method statically
      referenced but would change runtime dispatch (the switch's `findCause` predicate intentionally excludes it) and
      conflict with the earlier decision to keep method-validation handling as a dedicated handler.
    - A project-wide ErrorProne suppression or config change is broader than needed and would mask genuine unused-code
      findings elsewhere.

## Risks / Trade-offs

- [Suppressing `unused` could hide a future real removal of this handler] → Mitigation: the handler is a public-facing
  validation path covered by `ShowcaseApiControllerCT` (multiple `"Invalid request."` assertions) and the gateway E2E;
  removal would fail those tests, and the annotation is narrowly scoped to this one method.

## Migration Plan

1. Add `@SuppressWarnings("unused")` above `handleHandlerMethodValidationException` in `ShowcaseApiController`.
2. Run `./gradlew :showcase-api-gateway:compileJava` (or `componentTest`) and confirm the `UnusedMethod` warning is
   gone.

## Open Questions

- None.
