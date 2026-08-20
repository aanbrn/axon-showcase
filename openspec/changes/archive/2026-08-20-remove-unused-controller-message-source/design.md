## Context

See proposal.md - Why. `ShowcaseApiController` injects a `MessageSource` but never reads it; the field's only consumer
(`handleWebExchangeBindException`) was removed in the `add-gateway-client-error-handling-tests` change. Localized
error-message resolution for the remaining validation path is owned by `ShowcaseApiErrorResolver`, which holds its own
`MessageSource`, so the controller field is purely vestigial.

## Goals / Non-Goals

**Goals:**
- Remove the dead `messageSource` field and its `MessageSource` import from `ShowcaseApiController`.
- Eliminate the ErrorProne `UnusedVariable` warning emitted for the field.

**Non-Goals:**
- Not moving message resolution into the controller or otherwise changing how validation errors are localized.
- Not touching `ShowcaseApiErrorResolver` or the controller's validation `@ExceptionHandler`.

## Decisions

- **Remove the field outright rather than rewire it.**
  - The field has no reader and the controller has no `@ModelAttribute`/`BindingResult` binding path that would need it
    (established by the `add-gateway-client-error-handling-tests` change). Its removal is a pure cleanup.
  - Alternative rejected: keep the field for future use — it is speculative and keeps an `UnusedVariable` warning in
    every gateway compile.
- **Remove the `org.springframework.context.MessageSource` import together with the field.**
  - The import is referenced nowhere else in the file; leaving it would preserve the unused-import warning in a
    different form.

## Risks / Trade-offs

- [Removing the field could break constructor injection if a bean were to require it] → Mitigation: `ShowcaseApiController`
  is built with `@RequiredArgsConstructor`; the field is never read, and its removal only drops the injected dependency,
  which no behavior depends on. Verified by a full-context test boot at the integration tier.

## Migration Plan

1. Remove the `messageSource` field (and its Javadoc) and the `MessageSource` import from `ShowcaseApiController`.
2. Run `./gradlew :showcase-api-gateway:componentTest` and `integrationTest` to confirm the context still boots and
   validation handling is unchanged.

## Open Questions

- None.
