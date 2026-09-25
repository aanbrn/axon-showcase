# Tasks

## 1. Implementation

- [x] 1.1 Add a `typecheck` script to `showcase-web-ui/package.json` (`"typecheck": "tsc --noEmit"`). Verify
      `cd showcase-web-ui && npm run typecheck` passes on the clean tree. — Done: `npm run typecheck` passes.
- [x] 1.2 Register `npmTypeCheck` in `build-logic/src/main/kotlin/frontend-conventions.gradle.kts` (group
      `verification`, `dependsOn(npmCi)`, `args("run", "typecheck")`, `inputs.files(fileTree("src"))` and
      `inputs.file("tsconfig.json")`) and add it to the frontend `check` wiring
      (`dependsOn(npmLint, npmFormatCheck, npmTypeCheck, npmTest)`). Verify `./gradlew :showcase-web-ui:check --dry-run`
      lists `npmTypeCheck` and `:showcase-web-ui:build` still runs `npmBuild`. — Done: the `check` dry-run lists
      `npmTypeCheck`; the `build` dry-run still lists `npmBuild`.

## 2. Docs

- [x] 2.1 Remove the "Type-check the web UI in the merge gate" idea from `docs/ideas.md` (the change's own idea removal
      rides the branch). Verify `grep -rn "Type-check the web UI in the merge gate" docs/ideas.md` returns nothing. —
      Done: the idea is removed.
- [x] 2.2 Refresh `AGENTS.md`'s two frontend-`check` descriptions this change falsifies: the Build & Test comment
      (`./gradlew :showcase-web-ui:check` — add the type-check) and the Continuous Integration e2e sentence (the
      frontend `check` now type-checks; only `vite build` stays on `assemble`). Verify both read correctly and that
      `grep -n "composes lint, format-check, and Vitest" AGENTS.md` returns nothing. — Done: both refreshed; the stale
      sentence is gone.

## 3. Verification

- [x] 3.1 Prove the check fails on a known-bad input and passes on a known-good one: introduce a deliberate type error
      (e.g. a mistyped value in a `src` file), run `./gradlew :showcase-web-ui:npmTypeCheck` and confirm it fails with
      the TypeScript diagnostic (and that `check` fails); revert the error and confirm both pass. Read the output. —
      Done: a probe (`const showcaseTypeCheckProbe: number = 'not-a-number';`) failed `npmTypeCheck` with `error TS2322`
      (and `TS6133`) and failed `check`; after reverting, both pass.
- [x] 3.2 Run `./gradlew :showcase-web-ui:check` and the Docker-free
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; both pass, and the added type-check's cost is the
      ~1-second `tsc --noEmit`. — Done: both pass; `npmTypeCheck` is in the root `check` graph.
- [x] 3.3 Run `openspec validate --changes` (the delta validates) and `./gradlew spotlessApply` followed by
      `./gradlew spotlessCheck`; both pass. — Done: both pass.
