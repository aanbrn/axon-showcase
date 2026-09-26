# Tasks

## 1. Rules

- [x] 1.1 Add the `@typescript-eslint/naming-convention` block to `showcase-web-ui/eslint.config.js` (D1, D2): functions
      `StrictPascalCase` where the name is uppercase-leading and `strictCamelCase` otherwise; variables `UPPER_CASE` or
      `StrictPascalCase` where uppercase-leading and `strictCamelCase` otherwise; parameters `strictCamelCase` plus a
      second entry for `modifiers: ['unused']` with `leadingUnderscore: 'allow'`; `typeAlias`, `interface`, and
      `typeParameter` `StrictPascalCase`.
- [x] 1.2 Add the file-scoped block rejecting the `src` unit tests the Vitest include will not collect (D3): for
      `src/**/*.spec.{ts,tsx}`, a `no-restricted-syntax` report whose message names `*.test.ts(x)` (the Playwright
      `e2e/` suite is outside `eslint src`, so its `.spec.ts` convention is untouched).
- [x] 1.3 Align `@typescript-eslint/no-unused-vars` with `argsIgnorePattern: '^_'` (D2), so the `_` idiom the naming
      rules allow is not reported by the unused-variable rule; verify both rules agree on an unused `_` parameter.

## 2. Control

- [x] 2.1 Rename `showcase-web-ui/src/boundaries.test.ts` to `src/eslint-rules.test.ts`; generalize its helper to return
      **every** reported rule id (it currently keeps only `boundaries/*`), and make every acceptance snippet export or
      consume its imports (widening the helper exposes the two existing ones as `no-unused-vars`). Then add a naming
      describe (D4), with **one rejection case per rule entry**: `bad_function`/`Bad_function`, `bad_variable`/
      `Bad_Variable`, `bad_param` and a used `_x`, `bad_type`, `interface bad_interface`, a `<bad_t>` type parameter, a
      `src/**/*.spec.ts` path (asserted with `toContain`), and a hook in a function not named `use*` must each report
      their rule; conforming names, an unused `_` parameter, and a `*.test.ts` path must report nothing. Every snippet's
      virtual path sits in a layer (e.g. `src/pages/showcases/…`), and each acceptance snippet exports or consumes its
      own declarations as well as its imports (an unexported one reports `no-unused-vars`).

## 3. Docs and spec

- [x] 3.1 Update `AGENTS.md`'s frontend convention to state the enforced naming conventions, naming the strict formats
      the rules use — components and types `StrictPascalCase`, hooks `use*`, helpers and parameters `strictCamelCase`,
      module constants `UPPER_CASE`, tests `*.test.ts(x)`.
- [x] 3.2 Add the `showcase/quality/code-quality` delta (the web module's naming enforcement).
- [x] 3.3 Remove the implemented "Enforce the web UI's naming conventions with tooling" entry from `docs/ideas.md`.
- [x] 3.4 Refresh `docs/adr/0013-feature-sliced-design-layering.md`: its Consequences clause "the entry's naming half
      remains parked" now describes work this change lands — drop it (or replace it with the naming gate).

## 4. Verification

- [x] 4.1 `./gradlew :showcase-web-ui:check` passes with the rules active and **no shipped source change** — the code
      already follows the conventions, so the rule set must pass unmodified.
- [x] 4.2 The control proves each rejection (naming, the `use*` prefix through the React-hooks check, and the test-file
      pattern) and each acceptance; `npmLint` is run directly to confirm the rules can fail.
- [x] 4.3 `openspec validate --changes` passes; `./gradlew spotlessApply` then `spotlessCheck` pass.
