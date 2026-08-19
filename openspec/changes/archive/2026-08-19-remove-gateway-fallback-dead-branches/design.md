## Context

`ShowcaseApiController.fetchList`/`fetchById` fall back to real in-process Caffeine `AsyncCache`s on a transient query
error. The fallback reads `getIfPresent(...)` and, when non-null, attaches `whenComplete((value, t2) -> ... else
sink.error(t))`. The `else` branch is unreachable: empirically, Caffeine's `getIfPresent` returns `null` for a
null-valued or failed future, so a non-null future always completed with a valid value. See proposal.md - Why.

## Goals / Non-Goals

**Goals:**

- Remove the unreachable fallback error branches, simplifying the controller.

**Non-Goals:**

- No change to the other unreachable defensive handlers (`WebExchangeBindException`, `ErrorResponseException`,
  `handleException` switch) — out of scope.

## Decisions

- **Replace `whenComplete` + dead `else` with `thenAccept`** in the three fallback sites, since a non-null cached future
  always holds a valid value. `thenAccept` fires only on successful completion with the value, which is exactly what a
  real Caffeine `getIfPresent` result is.
- **Revert the earlier ineffective tests** (they covered the miss path, duplicating existing `…CacheMiss` tests).

## Risks / Trade-offs

- [`thenAccept` would not fire on an exceptional future, leaving the flux hanging] → cannot happen: Caffeine's
  `getIfPresent` never returns an exceptional future (verified), only `null` or a valid future.
