## Why

The Javadocs haven't been reviewed in a while; a thorough review found one factually wrong copy-paste, a few misleading
docs (reactive `@throws`, cache "names" that are actually config keys), redundant `throws` clauses, and small
consistency gaps. One real code bug surfaced too: a test-fixture matcher reports the wrong feature name in failure
messages.

## What Changes

- Correct the `ShowcaseAggregate.start` Javadoc copy-paste ("finishing an already finished showcase" → "starting an
  already finished showcase").
- Fix `ShowcaseCommandMatchers.aCommandErrorDetailsWithErrorMessage` to report `errorMessage` instead of `errorCode` as
  the matched feature (misleading test-failure messages).
- Reword `ShowcaseApiConstants` Javadocs: the constants are configuration keys, not runtime cache names (the registered
  caches are kebab-case literals).
- Replace `@throws` on reactive-return methods (`ShowcaseQueryHandler.handle`, `ShowcaseRestApi.fetchById`) with prose
  documenting the emitted error; drop the now-unused import.
- Remove redundant `throws` clauses on unchecked exceptions (`ShowcaseTitleReservation.save`,
  `ShowcaseQueryHandler.handle`).
- Add a missing blank line before a method Javadoc (`ShowcaseEventMapper`), Javadoc for the `ShowcaseProjector`
  constructor and the `ShowcaseSimulation` class.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none (Javadoc/cleanup only; no behavior change — `skip_specs: true`)

## Impact

- 9 files across `showcase-*` modules and `load-tests`: Javadoc wording/correctness, one test-fixture matcher fix, two
  redundant `throws` removals, one unused import removal.
- No runtime behavior, API, or dependency changes.
