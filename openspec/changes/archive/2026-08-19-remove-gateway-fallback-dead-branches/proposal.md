# Proposal: Remove dead cache-fallback error branches from the gateway controller

## Why

`ShowcaseApiController`'s cache-fallback `whenComplete` error branches (the `else { sink.error(t) }` taken when a
cached future completes with a null value or an error) are unreachable: with real Caffeine `AsyncCache`s,
`getIfPresent` never returns a null/error-completing future — it returns `null` or a future holding a valid value.
They are dead code.

## What Changes

- Simplify the `fetchList`/`fetchById` cache fallback in `ShowcaseApiController`: replace the `whenComplete` plus dead
  `else` with `thenAccept`, since a non-null cached future always holds a valid value.
- Revert the earlier fallback error-branch tests, which only exercised the (already-covered) cache-miss path and added
  no coverage.
- Behavior-preserving production change; the removed branches were unreachable.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- `ShowcaseApiController` fallback simplified (dead code removed); coverage slightly improved by shrinking the
  reachable surface.
