# Proposal: Enforce the web UI's naming conventions

## Why

The web UI's naming conventions — `PascalCase` components and types, `use*` hooks, `camelCase` helpers and parameters,
`UPPER_CASE` module-scope constants, and `*.test.ts(x)` test files — live only in the code's example and in review.
`enforce-web-ui-import-boundaries` closed the import half of the parked idea; this is the naming half. Two things make
it worth gating: `@typescript-eslint/naming-convention` is already available (the plugin is installed), and a unit test
under `src` named `*.spec.ts` **silently never runs**, because the Vitest include is `src/**/*.test.{ts,tsx}` — a dead
test is worse than a missing one. The rules codify what the code already exhibits (including the idiomatic `_` unused
parameter, which the rule set allows), with a positive control and a spec that states each convention.

## What Changes

- **Identifier rules** in the web module's flat `eslint.config.js`, using `@typescript-eslint/naming-convention`:
  - functions — `StrictPascalCase` where the name is uppercase-leading, `strictCamelCase` otherwise;
  - variables — `UPPER_CASE` or `StrictPascalCase` where uppercase-leading (a constant value, or a component/type
    declared as a `const`), `strictCamelCase` otherwise;
  - parameters — `strictCamelCase`, plus a second entry allowing a single leading underscore on an **unused** parameter;
  - `typeAlias`, `interface`, and `typeParameter` — `StrictPascalCase`.
- **An aligned unused-variable rule** — `@typescript-eslint/no-unused-vars` gains `argsIgnorePattern: '^_'`, so the `_`
  idiom the naming rules allow is not reported by the rule that flags unused parameters (the two rules agree on it).
- **A test-file rule** rejecting a `src` unit test named `*.spec.{ts,tsx}` — a file the Vitest include will not collect
  — with a message naming `*.test.ts(x)`; the Playwright `e2e/` suite's `.spec.ts` convention is untouched.
- **A positive control** — generalize the module's lint-rule control (rename `boundaries.test.ts` to
  `eslint-rules.test.ts`), make its snippets `no-unused-vars`-clean, and add a naming describe: a violating identifier,
  a hook called from a function not named `use*`, and a `*.spec.ts` path must each error; conforming names, an unused
  `_` parameter, and a `*.test.ts` path must pass.
- **Specification** — a new `showcase/quality/code-quality` requirement for the web module's naming enforcement.
- **Docs** — state the enforced conventions in `AGENTS.md`'s frontend bullet, remove the implemented idea from
  `docs/ideas.md`, and refresh ADR-0013's Consequences (its "the entry's naming half remains parked" clause describes
  work this change lands).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/code-quality` — a new requirement that the web module's naming conventions are enforced by the
  build.

## Impact

- **Web UI**: the ESLint config and one test file. **No shipped source changes** — the code conforms, including the one
  `_` (an unused `Array.from` callback parameter, which the rule set permits; verified by probing the rule).
- **Build / CI**: no new gate — the rules run in the existing `npmLint` step of `check`.
- **Risk**: `naming-convention` is easy to over- or under-configure → the rule set is run against the whole tree and
  pinned by the positive control (a rule that only ever passes is not evidence).
