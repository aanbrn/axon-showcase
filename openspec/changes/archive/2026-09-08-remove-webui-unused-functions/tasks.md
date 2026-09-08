## 1. Remove the dead reconciliation helpers

- [x] 1.1 Remove `waitForShowcasePresence`, `waitForShowcaseStatus`, and `waitForShowcaseRemoval` (and their Javadoc)
      from `entities/showcase/query-hooks.ts`, keeping `waitForReadModel`, `reconcileShowcase`, `predicateForEvent`, and
      `waitForEvent`.
- [x] 1.2 In `useCreateShowcase.test.tsx`, drop the `waitForShowcasePresence` import, the
      `vi.mock('@/entities/showcase/query-hooks', ...)` block (which only mocked it), and its `not.toHaveBeenCalled()`
      assertions.
- [x] 1.3 In `useShowcaseActions.test.tsx`, drop the `waitForShowcaseStatus` import, the
      `vi.mock('@/entities/showcase/query-hooks', ...)` block (which only mocked it), and its `not.toHaveBeenCalled()`
      assertions.
- [x] 1.4 Rework `query-hooks.test.ts`'s dedup test to use `waitForEvent` for both legs instead of
      `waitForShowcaseStatus`.

## 2. Verify

- [x] 2.1 Run `tsc` (`build`) and `lint` for `showcase-web-ui`; confirm no type/lint errors.
- [x] 2.2 Run the web-UI tests (`npm test`/`./gradlew :showcase-web-ui:test` equivalent); confirm all pass.
- [x] 2.3 Run `./gradlew spotlessApply` and confirm formatting.
- [x] 2.4 Run `openspec validate --all` and confirm the change passes.
