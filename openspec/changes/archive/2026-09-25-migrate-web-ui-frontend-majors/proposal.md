# Proposal: Migrate the web UI's frontend majors (React 19, Vite 8, Vitest 5, jsdom 30)

## Why

The web UI's core toolchain sits behind its current majors — React 18, Vite 6, Vitest 3, jsdom 25 (TypeScript's manifest
pin is `^5.7.2`, resolving `5.9.3`) — so the weekly `dependency-updates` report lists them every run. Three pairs are
peer-coupled and must move together: `@vitejs/plugin-react` 6 requires Vite `^8`, `react-dom` 19 peers `react ^19.3.0`,
and `@types/react-dom` 19 peers `@types/react ^19.3.0`. Vitest 5 and jsdom 30 are independent of those (Vitest 5 accepts
Vite `^6.4 || ^7 || ^8`, and its `jsdom` peer is optional), but taking all eight together clears the report in one
change. TypeScript 7 is unreachable today: it falls outside typescript-eslint's supported range
(`typescript >=4.8.4 <6.1.0`) and no longer ships the classic JS API that stack consumes, so the migration takes
TypeScript `6.0.3` — the newest release inside the range — and defers 7 until the lint stack supports it.

## What Changes

- `showcase-web-ui/package.json` (+ `package-lock.json`) — `react`/`react-dom` → `^19.3.0`,
  `@types/react`/`@types/react-dom` → `^19.3.0`, `@vitejs/plugin-react` → `^6.1.1`, `vite` → `^8.3.1`, `vitest` →
  `^5.0.1`, `jsdom` → `^30.1.1`, and `typescript` `^5.7.2` → `^6.0.3`.
- Source/config fixes the migration requires (any `tsc`/type, lint, Vite, or Vitest fallout), with no behavior change.

**Deferred:** TypeScript 7 (the native compiler) — typescript-eslint does not support it yet; it becomes its own change
when the lint stack widens its range.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — `skip_specs: true` is set in `.openspec.yaml`; this is a toolchain migration whose behavior is unchanged, and no
capability spec pins a frontend version).

## Impact

- **Build**: the web UI's npm dependencies and lockfile; no JVM or deployment change.
- **Tests**: the frontend `check` (lint, format-check, Vitest) and `build` (`tsc && vite build`), plus the web UI
  Playwright e2e as the real-behavior check.
- **Deployment / CI**: the built UI bundle and image are rebuilt with the new toolchain; the deployed behavior is
  unchanged.
