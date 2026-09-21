# Record the blocking-execution rationale and retire the band-aid idea

## Why

The 2026-09-21 architecture audit raised the query-service's blocking-execution configurer as an unrecorded band-aid
whose necessity was "not self-evidently" established, and the parked `docs/ideas.md` entry called the gateway's
equivalent a coarse workaround whose removal was the goal. Both premises are refuted by measurement, and the
measurements are worth recording so the next audit does not re-raise it.

## What Changes

- **`AGENTS.md`** — a Gotcha records that both configurers are load-bearing, with the measured effect of removal in each
  service: the gateway's `ShowcaseRestControllerCT` drops 9 of 76 (`400` → `500` on request-validation cases) and the
  query-service's BlockHound-guarded `ShowcaseQueryControllerIT` drops 3 of 10 (`400`/`404` → `503`), with zero
  BlockHound hits in both. The bullet also bounds the mechanism: the gateway's not-found test passes without the
  configurer (its `fetchById` is stubbed, so `@ExceptionHandler` maps the error directly), while the query-service's
  fails because its error comes off the dispatched query bus.
- **`docs/ideas.md`** — the parked "remove the gateway's blocking-execution routing" entry leaves: explored and decided
  against, its premise disproved.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs` (no requirement describes this implementation mechanism).

## Impact

- **Specs**: none — `skip_specs`. The behaviour is an implementation mechanism no requirement describes, and the suites'
  assertions pin the observable contract already.
- **Code**: none. Both experiments were reverted; the tree carries no code change.
- **Reports**: `docs/audits/2026-09-21.md` is the run's record and is left unedited; the answer to its question lives in
  the `AGENTS.md` bullet.
