## Why

ErrorProne's `UnusedMethod` inspection flags `handleHandlerMethodValidationException` in `ShowcaseApiController` as
unused, but it is a live Spring `@ExceptionHandler` invoked only reflectively. Unlike the sibling handlers it is not
statically referenced from the catch-all `handleException` switch (`HandlerMethodValidationException` is deliberately
excluded from that switch), so the warning is a false positive. The catch-all `handleException` already carries
`@SuppressWarnings("unused")` for the same reason; the validation handler is the only reflective-only handler missing
it, leaving a warning on every gateway compile.

## What Changes

- Add `@SuppressWarnings("unused")` to `handleHandlerMethodValidationException` in
  `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiController.java`, matching the existing pattern on
  `handleException`.
- No behavioral change: the handler remains a live `@ExceptionHandler` for `HandlerMethodValidationException`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. This is a build-warning cleanup with no spec-level behavior change; the change sets `skip_specs: true`.

## Impact

- `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiController.java` — one `@SuppressWarnings("unused")`
  annotation added to `handleHandlerMethodValidationException`.
- Removes the `UnusedMethod` compiler warning emitted for this method.
