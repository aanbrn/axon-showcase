## 1. Wrapped known-exception handling

- [x] 1.1 Add a parameterized `ShowcaseApiControllerCT` test where the mocked client throws each known command/query
      exception wrapped in a `RuntimeException`, asserting the mapped status (covers the `handleException` switch cases
      and the `findCause` unwrap path). Excludes `AbortedException` (its `Mono<Void>` handler conflicts via the generic
      handler) and `WebExchangeBindException`/`ErrorResponseException` (not in `findCause`'s predicate)

## 2. Dead server-side handler removal

- [x] 2.1 Remove `handleWebExchangeBindException` and `handleErrorResponseException` from `ShowcaseApiController`
      (server-side errors the gateway cannot produce), plus their unreachable `handleException` switch cases and the
      now-unused imports

## 3. Documentation

- [x] 3.1 Add the missing `findCause` javadoc in `ShowcaseApiController`
