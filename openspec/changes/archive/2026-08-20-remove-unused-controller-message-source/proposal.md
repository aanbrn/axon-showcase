## Why

`ShowcaseApiController` declares a `messageSource` field that is never read. Its only consumer,
`handleWebExchangeBindException` (`return e.updateAndGetBody(messageSource, locale);`), was removed in the
`add-gateway-client-error-handling-tests` change, orphaning the field. The unused field triggers a pre-existing
ErrorProne `UnusedVariable` warning on every gateway compile and is dead code.

## What Changes

- Remove the `messageSource` field (and its Javadoc) from `ShowcaseApiController`, together with the now-unused
  `org.springframework.context.MessageSource` import.
- No behavioral change: localized error-message resolution for validation exceptions is handled by
  `ShowcaseApiErrorResolver` (which holds its own `MessageSource`), and the controller's
  `handleHandlerMethodValidationException` path is unaffected.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. This is a pure dead-code cleanup with no spec-level behavior change; the change sets `skip_specs: true`.

## Impact

- `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiController.java` — remove the dead `messageSource` field
  and its `MessageSource` import.
- Removes the `UnusedVariable` compiler warning emitted for this field.
