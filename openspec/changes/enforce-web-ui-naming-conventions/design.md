# Design

## Context

See `proposal.md` — Why. Current state, derived from the module and the installed packages rather than recalled:

- **The conventions the code exhibits** (surveyed over `showcase-web-ui/src`): functions are `PascalCase` components
  (`App`, `ShowcaseList`, `CreateShowcaseForm`) or `camelCase` hooks/helpers (`useShowcases`, `formatTime`,
  `connectEventStream`); module-scope constants are `UPPER_CASE` (`BASE`, `SHOWCASES_QUERY_KEY`,
  `IDEMPOTENCY_KEY_HEADER`, `RETRY_ATTEMPTS`, `TRACE_ID`) or `camelCase` for objects/functions (`store`,
  `showcaseEventsReducer`); parameters are `camelCase`; type aliases, interfaces, and type parameters are `PascalCase`.
- **One deliberate exception**: `CreateShowcaseForm.tsx` uses `(_, i)` for an unused `Array.from` callback parameter —
  the idiomatic leading underscore, which `strictCamelCase` rejects unless the rule is told otherwise.
- **The test-file convention is load-bearing for the runner**: `vite.config.ts` sets
  `test.include = ['src/**/*.test.{ts,tsx}']`, so a `src` unit test named `*.spec.ts` is collected by nothing — it lints
  and compiles but never runs. No such file exists under `src`; the **Playwright suite under `e2e/`**
  (`e2e/showcase.spec.ts`) uses `.spec.ts` by Playwright's own convention, runs under `:showcase-web-ui:e2eTest`, and is
  outside the lint scope (`eslint src`), so it is the deliberate exception the rule must not catch.
- **The `use*` prefix is already enforced** by the module's existing `react-hooks/rules-of-hooks` (from
  `reactHooks.configs.recommended.rules`): a function that calls a hook must be named `use*` to be treated as one.
  Naming-convention rules cover the _shape_ of a hook name, not the requirement to use the prefix.
- **The mechanism**, verified against the installed `@typescript-eslint/eslint-plugin` (v8) rather than its docs:
  `naming-convention` takes a list of entries, each with `selector`, `modifiers`, `filter` (`{ regex, match }`),
  `format` (an **array** of allowed formats: `camelCase`, `strictCamelCase`, `PascalCase`, `StrictPascalCase`,
  `snake_case`, `UPPER_CASE`), `leadingUnderscore`/`trailingUnderscore`, and a `Modifiers` set that includes
  **`unused`** and **`destructured`**. Probed with both the module's config and a scratch one: an entry with
  `modifiers: ['unused'], leadingUnderscore: 'allow'` lets `(_, i)` pass while a _used_ `_x` and a `bad_name` still
  fail.
- **The gate exists**: `npmLint` (`eslint src --max-warnings 0`) runs in the web module's `check`, and the module's
  lint-rule control lints snippets against the real config through ESLint's Node API (`overrideConfigFile`, because the
  tsconfig has no `allowJs`).

## Goals / Non-Goals

**Goals:**

- Express the naming conventions the code exhibits as lint rules that fail on a violation.
- Make a test file the runner will not collect a build failure, not a silent no-op.
- Prove each rejection (a positive control), and state the conventions in the spec.

**Non-Goals:**

- **Not** a rename or refactor: no shipped source changes.
- **Not** React-component detection: the rule distinguishes an uppercase-leading name from a lowercase-leading one; it
  cannot know that a `PascalCase` function is a component, and it does not need to.
- **Not** the `use*` prefix as a new rule — the existing React-hooks check supplies it (above).
- **Not** property, method, or import naming (JSON payload fields, external API names, and React/Redux member names are
  not ours to rename), filename case (a component module is `PascalCase`, a helper module `camelCase` — a per-file rule
  cannot express a convention that depends on the file's kind), or a test's _directory_ layout (a
  `__tests__/foo.test.ts` is collected by the include; only the suffix is gated).
- **Not** the constant-versus-component _value kind_: an uppercase-leading `const` is accepted as `UPPER_CASE` or
  `PascalCase`, because the rule sees a name and not the value — `UPPER_CASE` for a constant stays a review matter, not
  a gate.

## Decisions

- **D1 — One `naming-convention` block, distinguished by the leading character.** Functions get two entries:
  `filter: { regex: '^[A-Z]', match: true }` → `StrictPascalCase`, and an unfiltered entry → `strictCamelCase`.
  Variables likewise: uppercase-leading → `UPPER_CASE` **or** `StrictPascalCase` (the rule sees a name, not a value, so
  it cannot tell a constant from a component declared as a `const`), unfiltered → `strictCamelCase`. `typeAlias`,
  `interface`, and `typeParameter` → `StrictPascalCase`. Parameters → `strictCamelCase`, plus the unused-parameter entry
  below.
- **D2 — Strict formats, with the idiom's leading underscore allowed on unused parameters.** The code has no leading or
  trailing underscores except `(_, i)`, so `strictCamelCase`/`StrictPascalCase` (not the strict-less forms) is accurate;
  a second `parameter` entry — `modifiers: ['unused']`, `format: ['strictCamelCase']`, `leadingUnderscore: 'allow'` —
  keeps the idiom while still rejecting a _used_ `_x` (probed, above). The allowance must **agree with
  `@typescript-eslint/no-unused-vars`**, which the module's `recommended` rules enable: an unused `_` parameter is
  otherwise reported by that rule, so the change also sets its `argsIgnorePattern: '^_'` — without it the control's
  acceptance case cannot hold and the convention contradicts itself (probed: the naming rule alone leaves the parameter
  clean but the general assertion red).
- **D3 — The test-file rule is a file-scoped `no-restricted-syntax`.** ESLint cannot enumerate files that do not exist,
  but it does lint the ones that do: a block matching `src/**/*.spec.{ts,tsx}` — the Vitest surface, so the Playwright
  `e2e/` suite is untouched — that reports every match (`selector: 'Program'`) turns "a test the runner will not
  collect" into a lint error naming `*.test.ts(x)`. The alternative — a separate script or Gradle task — adds a gate for
  a property the lint gate already sees.
- **D4 — The control lives with the existing one, its helper widens, and its snippets carry their imports into use.**
  `src/boundaries.test.ts` already lints snippets against the real config; the change renames it
  `src/eslint-rules.test.ts`, **generalizes its helper to return every reported rule id** (it currently filters to
  `boundaries/*`), and adds a naming describe sharing that helper. Widening the helper exposes the two existing
  acceptance snippets' unused imports (`no-unused-vars`), so every acceptance case — old and new — must export or
  consume what it imports. **One rejection case per rule entry**, so no entry is exercised only on the accept side:
  `bad_function` and `Bad_function` (the two `function` entries), `bad_variable` and `Bad_Variable` (the two `variable`
  entries), `bad_param` and a used `_x` (the `parameter` entry and the narrowness of the unused allowance), `bad_type`,
  an `interface bad_interface`, and a `<bad_t>` type parameter (the three type entries), a `src/**/*.spec.ts` path (the
  file-scoped rule), and a hook called from a function not named `use*` (the React-hooks rule). Accepted: conforming
  names, an unused `_` parameter, and a `*.test.ts` path. Three apply-time traps: the spec-path assertion must use
  `toContain` (such a path also reports `boundaries/no-unknown-files`), every naming snippet's virtual path must sit in
  a layer (e.g. `src/pages/showcases/…`) so that rule does not fire, and each acceptance snippet's **own declarations**
  must be exported or consumed — an unexported conforming snippet reports three `no-unused-vars`.
- **D5 — The conventions are specified in `code-quality`**, as a sibling of "The web module's import boundaries are
  enforced by the build", with the mechanism (selectors, filters, modifiers) staying here per the corpus's outcome-only
  rule.

## Risks / Trade-offs

- **`naming-convention` over/under-matching**: the rule reports identifiers whose declaration matches a selector, so a
  mis-scoped entry either floods the tree or silently passes. Mitigation: the rule set is run against the whole tree
  (the conventions are the code's, so it must pass unchanged) and each rejection case is pinned by the control.
- **Destructuring**: a plain `variable` entry checks destructured _bindings_ (`const { data: showcases } = …` checks
  `showcases`), while object-pattern _keys_ are not checked — so payload shapes are unaffected but local renames are.
  The tree must pass with the rule on (verify at apply); no `destructured` modifier is requested.
- **`Program` as an always-match selector** for D3 is a deliberate use of `no-restricted-syntax` to forbid a file
  pattern; the risk is that a future reader reads it as a syntax rule — the message states what it is for.
- **The `_` allowance is narrow by construction**: it applies only to an unused parameter; a future used `_x` is
  rejected (probed), so the convention does not quietly widen.
