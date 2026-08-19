## Context

`ShowcaseApiController`'s error handlers translate exceptions the command/query clients can produce. The existing
`commandAvailabilityFailures`/`queryAvailabilityFailures` tests throw exceptions directly, so Spring routes them to the
specific `@ExceptionHandler`s — leaving the `handleException` fallback's switch cases (for *wrapped* known exceptions)
and `findCause` uncovered. The controller CT mocks the clients, so these can be exercised by throwing wrapped
exceptions. Spring internals confirm `WebExchangeBindException` (only from `@ModelAttribute`/`BindingResult` binding)
and `ErrorResponseException` (never thrown by Spring; only by app code) cannot occur in the gateway — their handlers are
dead. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Cover the `handleException` switch cases and `findCause` by mocking clients to throw wrapped known exceptions.
- Remove the dead `handleWebExchangeBindException`/`handleErrorResponseException` handlers and their unreachable switch
  cases.

**Non-Goals:**

- No change to the reachable client/transport handlers or the `AbortedException` switch case (its `Mono<Void>` handler
  conflicts when invoked through the generic handler).

## Decisions

- **Mock clients to throw wrapped known exceptions** (e.g. `Mono.error(new RuntimeException(knownException))`) so the
  generic `handleException` + `findCause` unwraps and routes to the right handler — mirroring a client transport
  wrapping a known exception. A parameterized source over the known types (command/query/Axon/WebClient/circuit
  breaker/timeout) asserts the mapped status. `AbortedException` is excluded (its `Mono<Void>` handler conflicts via the
  generic handler); `WebExchangeBindException`/`ErrorResponseException` are excluded (not in `findCause`'s predicate).
- **Remove the dead handlers** `handleWebExchangeBindException` and `handleErrorResponseException`, and the
  `HandlerMethodValidationException`/`WebExchangeBindException`/`ErrorResponseException` `handleException` switch cases
  (all unreachable), plus the direct tests that forced them.

## Risks / Trade-offs

- [Removing the dead handlers shrinks the safety net for hypothetical future paths] → the exceptions they handled
  cannot occur in the gateway (grounded in Spring internals), so the handlers are pure dead code.
