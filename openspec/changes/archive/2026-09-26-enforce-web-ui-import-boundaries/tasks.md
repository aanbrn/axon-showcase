# Tasks

## 1. Comply with ADR-0013

- [x] 1.1 Relocate the app-level UI state (D3): move the received-events state into `entities/showcase-event` (its slice
      plus a read hook and a dispatch-bound append hook) and the selection state into `entities/showcase` (its slice
      plus hooks); have `app/store.ts` compose the store from their reducers; remove the now-unused generic typed hooks
      and the `RootState`/`AppDispatch` exports they fed, and correct the store's JSDoc; switch `pages/showcases` to the
      entity hooks; move the state assertions of `app/store.test.ts` to the entity tests, leaving the app test to assert
      composition.
- [x] 1.2 Express the one sibling edge through FSD's `@x` (D4): the `entities/showcase` need for `ShowcaseEvent` goes
      through `entities/showcase-event`'s declared cross-import API; confirm no upward and no other sibling import
      remains (`grep` the graph).

## 2. Public APIs and the import rewrite

- [x] 2.1 Add an `index.ts` public API to each imported slice (`entities/showcase`, `entities/showcase-event`,
      `features/create-showcase`, `features/showcase-actions`, `widgets/showcase-detail`, `widgets/showcase-list`,
      `pages/showcases`) and `shared/index.ts`, each with the `// SPDX-License-Identifier: MIT` header; re-export each
      slice's surface, not a blanket `export *` of internals (D1). `app` gets no barrel — nothing imports it (D1).
- [x] 2.2 Rewrite the **source** imports to the public APIs, keeping intra-slice imports relative and leaving test files
      untouched (D7). Verify no source import still reaches a slice's internals.

## 3. Enforcement

- [x] 3.1 Add `eslint-plugin-boundaries` and `eslint-import-resolver-typescript` as web-UI devDependencies
      (`cd showcase-web-ui && npm install --save-dev …`), keeping the lockfile consistent.
- [x] 3.2 Configure the flat `eslint.config.js`: `boundaries/elements` for the layers/slices, `boundaries/dependencies`
      policies for the one-way direction, the public-API rule and the `@x` entry point, the TypeScript resolver for the
      `@/` alias, `boundaries/ignore` for the three out-of-slice files (`src/main.tsx`, `src/test-setup.ts`,
      `src/vite-env.d.ts`) and the tests, and `boundaries/no-unknown-files` so an unclassified source file is reported
      (D5, D7). Verify the mechanism against the plugin: the alias resolves, a deep, an upward, an undeclared sibling
      and an unclassified-source import/file are rejected, and a public-API and a declared `@x` import pass.
- [x] 3.3 Add the positive-control test (ESLint's Node API over the violating and conforming snippets, against the real
      config) asserting a reported `boundaries/*` error and a clean result respectively — the snippets cover the deep,
      upward, undeclared-sibling and unclassified-path rejections and the public-API and `@x` acceptances; assert by
      rule id, since the config's SPDX `header` rule would also flag a bare snippet (D6); load the config at runtime via
      `overrideConfigFile` — a static import would fail `npmTypeCheck` (the tsconfig covers `src` with no `allowJs`).

## 4. Docs and spec

- [x] 4.1 Update `AGENTS.md`'s frontend convention: the "importing only downward" sentence gains the `@x` sibling
      exception and states that the boundaries are enforced in the lint gate, and the "client state via a Redux Toolkit
      slice" sentence reflects the state living in the entity slices with the store composed in `app`.
- [x] 4.2 Refresh ADR-0013 line by line (D8): its Decision gains the `@x` clause and the two added defect classes (a
      public-API bypass, a file outside the layer graph), the `entities` population line gains the declared sibling
      edge, the `app` population line becomes the store's composition, and the Consequences' "convention, not a gate"
      becomes the enforced gate.
- [x] 4.3 Reword `docs/ideas.md`'s "Enforce web UI conventions with tooling" entry to the naming half that remains
      parked (the FSD boundaries are this change), adding the `— parked; no change yet.` status tag the entry currently
      lacks.
- [x] 4.4 Add the `showcase/quality/code-quality` delta (the web module's import boundaries are enforced by the build).

## 5. Verification

- [x] 5.1 `./gradlew :showcase-web-ui:check` passes (lint, format-check, type-check, tests) with the rules active and
      the refactor in place.
- [x] 5.2 `./gradlew :showcase-web-ui:build` passes — the barrels and the state move do not break the bundle.
- [x] 5.3 `openspec validate --changes` passes; `./gradlew spotlessApply` then `spotlessCheck` pass.
- [x] 5.4 Declare the config files `npmTest` reads (`eslint.config.js`, `vite.config.ts`, `tsconfig.json`) as its Gradle
      inputs, so a config-only regression cannot pass on a cached task (the boundaries test loads the config at
      runtime).
- [x] 5.5 Complete the cacheable npm tasks' input sets (the class 5.4 is an instance of): `npmTest` also reads
      `package.json` and the `package-lock.json` that owns the installed `node_modules`; `npmBuild` reads `index.html`,
      `package.json`, and `package-lock.json` — all now declared, so a build/test-affecting edit to any of them
      re-executes the task.
