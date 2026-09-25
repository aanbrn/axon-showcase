# Tasks

## 1. Migration

- [x] 1.1 Bump the eight frontend majors in `showcase-web-ui/package.json` and reinstall: `react`/`react-dom` →
      `^19.3.0`, `@types/react`/`@types/react-dom` → `^19.3.0`, `@vitejs/plugin-react` → `^6.1.1`, `vite` → `^8.3.1`,
      `vitest` → `^5.0.1`, `jsdom` → `^30.1.1`; separately, `typescript` `^5.7.2` → `^6.0.3`
      (`cd showcase-web-ui && npm install`). Verify the resolved versions and that no peer warning names a version we
      control (such a warning means a member of a coupled pair is missing). — Done: a clean reinstall resolved
      `react`/`react-dom`/`@types/react(-dom)` `19.3.0`, `@vitejs/plugin-react` `6.1.1`, `vite` `8.3.1`, `vitest`
      `5.0.1`, `jsdom` `30.1.1`, `typescript` `6.0.3`, with no peer warning.
- [x] 1.2 Resolve the type-check fallout from TypeScript 6 and `@types/react` 19 (`./gradlew :showcase-web-ui:build`,
      which runs `tsc && vite build`), keeping the fixes mechanical and behavior-preserving. — Done: TypeScript 6
      deprecates `baseUrl`, so `tsconfig.json` drops it and makes the `paths` target relative (`./src/*`);
      `:showcase-web-ui:build` passes.
- [x] 1.3 Resolve any lint, Vite 8, or Vitest 5 fallout (`./gradlew :showcase-web-ui:check`), keeping the fixes
      mechanical and behavior-preserving — the web UI's behavior must not change. — Done: Vitest 5's `new` on a mock
      needed a constructable `EventSource` stub (arrow → function) and the submit test's timer advance had to be async;
      `:showcase-web-ui:check` passes (79 tests).

## 2. Docs

- [x] 2.1 Update the `docs/ideas.md` entry parked on 2026-09-25 ("Suppress or schedule the web UI's npm major updates"):
      it enumerated the nine frontend majors, so reword it once to the outcome — the eight majors migrated and
      `typescript` bumped to `^6.0.3`, leaving the single row `typescript` 6 → 7 deferred — while the suppression idea
      stays open. Verify the entry no longer lists the eight migrated majors and names `typescript` 6 → 7. — Done: the
      entry now records the outcome and names only `typescript` 6 → 7.

## 3. Verification

- [x] 3.1 `./gradlew :showcase-web-ui:check` (lint, format-check, Vitest) and `./gradlew :showcase-web-ui:build`
      (`tsc && vite build`) both pass — these own the lint/type/Vitest fallout. — Done: both BUILD SUCCESSFUL.
- [x] 3.2 Run the web UI end-to-end suite — `./gradlew :showcase-web-ui:e2eTest` (Playwright against the compose
      pipeline; needs `docker` and `pack`) — as the real-behavior check that Vite 8 / React 19 did not change the UI's
      rendered behavior; read the result. (It exercises the built bundle, not the `tsc`/lint/Vitest changes 3.1 owns.) —
      Done: BUILD SUCCESSFUL (5m29s); the compose pipeline booted, Playwright passed, the stack was torn down.
- [x] 3.3 Read `showcase-web-ui/build/npm-outdated.txt` (via `./gradlew :showcase-web-ui:npmOutdated`): only
      `typescript` (7.x) should remain. Read the report's content, not the task's exit status — `npm outdated` exits `1`
      whenever any row exists, and the deferred `typescript` row keeps the report non-empty (the tracker keeps listing
      it until TS 7 is taken). — Done: the report lists only `typescript 6.0.3 → 7.0.2`.
- [x] 3.4 Run `openspec validate --changes` and `./gradlew spotlessApply` followed by `./gradlew spotlessCheck`, and the
      Docker-free `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; all pass. — Done: all pass.
