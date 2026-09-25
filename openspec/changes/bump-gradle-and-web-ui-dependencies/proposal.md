# Proposal: Bump the Gradle wrapper and the web UI's in-range npm dependencies

## Why

The `dependency-updates` tracker (issue #13) reports a Gradle wrapper update (`9.7.1` → `9.8.0`) and ten in-range web UI
npm updates (patches/minors where `wanted == latest`). Its catalog section is empty, and not because nothing moved: the
tracker's extraction targets a `have newer versions:` section header, while the build's report lists its catalog
candidates under `have later milestone versions:` — so no catalog update reaches the issue (a pre-existing tracker gap,
separate from this bump). Taking the in-range updates keeps the pinned toolchain current and clears the tracker's
actionable in-range rows, without crossing a major line.

## What Changes

- `gradle/wrapper/` — Gradle `9.7.1` → `9.8.0`, keeping the `-all` distribution: the `wrapper` task is run twice
  (`--gradle-version 9.8.0 --distribution-type all`), because the first run only rewrites `distributionUrl` while the
  second, running under `9.8.0`, regenerates the wrapper jar and `gradlew`/`gradlew.bat` at the new version.
- `showcase-web-ui/package-lock.json` — the ten in-range npm packages: `@playwright/test`, `@tanstack/react-query`,
  `@types/node`, `@typescript-eslint/eslint-plugin`, `@typescript-eslint/parser`, `typescript-eslint`, `eslint`,
  `prettier`, `react-hook-form`, and `zod` (`package.json`'s declared ranges already allow them, so only the lockfile
  changes).
- `.opencode/commands/gradle-update.md` — pass `--distribution-type all` and run the `wrapper` task twice, so the
  documented wrapper-update path preserves the repo's `-all` distribution and refreshes the wrapper jar/scripts.
- Docs — the pinned Gradle version in `AGENTS.md`, `README.md`, and `openspec/config.yaml`'s `context:` block.

**Deferred (not in this change):** the nine npm major updates the report also lists — `react`/`react-dom` 18 → 19,
`@types/react`/`@types/react-dom` 18 → 19, `@vitejs/plugin-react` 4 → 6, `vite` 6 → 8, `vitest` 3.2.7 → 5.0.1, `jsdom`
25 → 30, and `typescript` 5.9.3 → 7.0.2 — each crossing a major line and needing a migration. With no web UI
major-suppression mechanism they keep re-appearing in the weekly tracker until taken or suppressed.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — `skip_specs: true` is set in `.openspec.yaml`; a version bump changes no behavior, so no capability spec
changes).

## Impact

- **Build**: the Gradle wrapper and the web UI's lockfile. No build logic, application code, or service change.
- **Tests**: the frontend `check` (lint, format, Vitest) and the JVM gates run against the bumped toolchain.
- **Deployment / CI**: none beyond the toolchain; the nine deferred majors remain listed in the tracker.
