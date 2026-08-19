# Proposal: Add gateway client-error handling tests and remove dead server-side handlers

## Why

`ShowcaseApiController` has error handlers for exceptions the command/query clients can produce, but their coverage was
low: the `handleException` fallback's switch cases (for known exceptions *wrapped* in unknown ones) and the `findCause`
unwrap path were uncovered. The controller CT mocks the client interfaces, so these can be exercised by making the
mocked clients throw the corresponding exceptions. Investigation also confirmed that two handlers
(`handleWebExchangeBindException`, `handleErrorResponseException`) are dead defensive code for server-side errors the
gateway can never produce.

## What Changes

- Add `ShowcaseApiControllerCT` tests where the mocked clients throw **wrapped known exceptions** (command/query
  exception, Axon, WebClient, circuit breaker, timeout inside a `RuntimeException`) — the generic `handleException` +
  `findCause` unwraps them and routes to the mapped handler, covering the switch cases and the unwrap path.
- Remove the dead server-side handlers from `ShowcaseApiController`: `handleWebExchangeBindException` (only produced by
  `@ModelAttribute`/`BindingResult` binding, which the gateway never does) and `handleErrorResponseException` (nothing
  throws it), plus their unreachable `handleException` switch cases.
- Test-only additions; one behavior-preserving production cleanup (removing unreachable handlers).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- New component tests under `showcase-api-gateway/src/componentTest/`; dead handlers removed from `ShowcaseApiController`.
  `ShowcaseApiController` coverage ~84% → ~98.6%; gateway ~82% → ~88%. No runtime behavior changes.
