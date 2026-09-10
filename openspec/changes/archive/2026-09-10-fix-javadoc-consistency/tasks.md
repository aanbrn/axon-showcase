## 1. Javadoc fixes

- [x] 1.1 Correct the `ShowcaseAggregate.start` Javadoc copy-paste and verify it says "starting an already finished
      showcase is rejected"
- [x] 1.2 Reword the `ShowcaseApiConstants` Javadocs to describe the constants as configuration keys (not cache names)
      and verify the class-level comment matches
- [x] 1.3 Replace `@throws` with prose documenting the emitted error on `ShowcaseQueryHandler.handle` and
      `ShowcaseRestApi.fetchById`, and verify the unused `ShowcaseQueryException` import is removed from
      `ShowcaseRestApi`
- [x] 1.4 Remove the redundant `throws` clauses on `ShowcaseTitleReservation.save` and `ShowcaseQueryHandler.handle` and
      verify both still compile
- [x] 1.5 Add the missing blank line before the `scheduledToDto` Javadoc in `ShowcaseEventMapper`
- [x] 1.6 Add Javadoc for the `ShowcaseProjector` constructor and the `ShowcaseSimulation` class

## 2. Test-fixture fix

- [x] 2.1 Fix `ShowcaseCommandMatchers.aCommandErrorDetailsWithErrorMessage` to use `errorMessage` as the
      `FeatureMatcher` feature description and verify the matcher still passes its test fixtures

## 3. Verification

- [x] 3.1 Run `spotlessApply`/`spotlessCheck` and the affected modules' `compileJava` (query-service, api-gateway,
      command-service, projection-service, load-tests gatling, command-api testFixtures) and verify they pass
- [x] 3.2 Run `openspec validate --changes` and verify the change is valid (skip_specs accepted)
