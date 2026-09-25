# Proposal: Type-check the web UI in the standard check

## Why

The frontend `check` composes `npmLint`, `npmFormatCheck`, and `npmTest`; `tsc` runs only inside `npmBuild`
(`tsc && vite build`), which hangs off `assemble` — and the PR gate runs `check`, not `assemble`. So a `tsconfig.json`
or source type error merges green. `migrate-web-ui-frontend-majors` proved it: TypeScript 6's `baseUrl` deprecation
failed only `:showcase-web-ui:build` (`npmBuild`), which the merge gate never reaches; only the migration's deliberate
`build` run caught it.

## What Changes

- `showcase-web-ui/package.json` — a `typecheck` script (`tsc --noEmit`).
- `build-logic/src/main/kotlin/frontend-conventions.gradle.kts` — a `npmTypeCheck` `NpmTask` (the `typecheck` script, no
  IDE) wired into the frontend `check` alongside `npmLint`, `npmFormatCheck`, and `npmTest`.
- `openspec/specs/showcase/quality/code-quality` (delta) — an added requirement that the web UI is type-checked by the
  build, with scenarios for a type error failing `check` and the bundle build staying on `assemble`.
- `AGENTS.md` — refresh the two places that describe the frontend `check`'s members (the Build & Test comment and the
  Continuous Integration e2e sentence), which this change falsifies.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/code-quality`: adds a requirement that the standard `check` type-checks the web UI (`tsc --noEmit`),
  so a type error fails `check` rather than only the bundle build.

## Impact

- **Build**: a `package.json` script and a `frontend-conventions` task; `check` gains a ~1-second `tsc --noEmit` step
  per run.
- **Tests**: the frontend `check` now also type-checks; existing tests are unaffected.
- **Deployment / CI**: the PR gate's `check` gains the type-check, so type errors are caught before merge.
- **Docs**: `AGENTS.md`'s Build & Test comment and its Continuous Integration e2e sentence are refreshed to describe the
  frontend `check`'s new member.
