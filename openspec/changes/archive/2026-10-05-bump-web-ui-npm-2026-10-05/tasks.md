# Tasks

## 1. Apply the bumps

- [x] 1.1 Run `npm update @tanstack/react-query @types/node eslint jsdom vite` in `showcase-web-ui/` and confirm
      `npm outdated` no longer lists them (only the suppressed `typescript` 7 remains). Done: `npm outdated` now lists
      only `typescript` 6.0.3 (wanted) → 7.0.2 (latest, suppressed).

## 2. Verification

- [x] 2.1 Run `./gradlew :showcase-web-ui:check` (lint, format-check, TypeScript type-check, Vitest) and confirm it
      passes. Done: `BUILD SUCCESSFUL`; Vitest 22 files / 103 tests passed under the new jsdom, lint/format/type-check
      green.
- [x] 2.2 Run `./gradlew :showcase-web-ui:build` and confirm the production bundle builds — the frontend `check` never
      builds it, and `vite` is the bundler. Done: `BUILD SUCCESSFUL`; `vite v8.3.2` transformed 198 modules into
      `build/dist`.
- [x] 2.3 Read the lockfile diff and describe it by what moved (the five direct bumps plus any transitive
      reconciliation); confirm no direct dependency moved a major. Done: 15 moved / 1 removed / 0 added. Direct:
      `@tanstack/react-query` (and `@tanstack/query-core`) 5.104.1, `@types/node` 26.6.4, `eslint` 10.12.0, `jsdom`
      30.1.2, `vite` 8.3.2. Transitive: six `@asamuzakjp/*`/`@csstools/*` patches, `whatwg-url` 17.1.2 → 17.2.0, and
      jsdom 30.1.2's own graph majors `data-urls` 7.0.0 → 8.0.0 and `tr46` 6.0.0 → 7.0.0 (the nested
      `data-urls/node_modules/whatwg-url` 16.0.1 was removed by dedupe). No _direct_ dependency moved a major; the two
      transitive majors come from jsdom's declared `data-urls ^8.0.0` / `whatwg-url ^17.2.0`, not an accidental
      resolution.
- [x] 2.4 Run the implementation `review-quick` loop over the diff; fix its findings and re-run until it reports nothing
      new. Round 1 found the missing bundle-build verification (now task 2.2) and the "no major moved" wording (now "no
      direct dependency moved a major"); both fixed, round 2 clean.
- [x] 2.5 Run the per-unit `lesson-capture` over this change and apply its durable proposals; record the applied net
      `AGENTS.md` delta. Done: one merge into the existing npm-checks bullet (a bundle-path bump must also run
      `:showcase-web-ui:build`), net +4 lines; no retirements.
- [x] 2.6 Run `./gradlew spotlessApply` after the last edit and confirm `spotlessCheck` passes; run the manual
      120-character check over the lines this change introduces. Done (final pass after the task ticks).
- [x] 2.7 Request the user's manual review pass — the step before committing. Done: the user approved the
      implementation.
