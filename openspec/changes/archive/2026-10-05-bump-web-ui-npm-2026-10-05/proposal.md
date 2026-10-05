# Proposal

## Why

The weekly update check (`dependency-updates`, the 2026-10-04 run) flagged newer in-range npm releases for the web UI.
Keeping the pins current is the routine hygiene the check exists to surface.

## What Changes

- `showcase-web-ui/package-lock.json` (lockfile only; the declared caret ranges in `package.json` already admit these) —
  `@tanstack/react-query` 5.104.0 → 5.104.1, `@types/node` 26.6.3 → 26.6.4, `eslint` 10.11.0 → 10.12.0, `jsdom` 30.1.1 →
  30.1.2, `vite` 8.3.1 → 8.3.2.
- Deliberately excluded, per a recorded decision: `typescript` 7 (the deferred major — `typescript-eslint`'s peer range
  caps TypeScript below the next major; `config/web-ui-updates/major-disabled.txt`).

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — a pure dependency bump (`.openspec.yaml` sets `skip_specs: true`); no requirement changes.

## Impact

- `showcase-web-ui/package-lock.json` only. `package.json`'s declared ranges are unchanged.
- The frontend gates (`lint`, `format:check`, the TypeScript type-check, Vitest) are the verification; `vite` is the
  build tool, so the production bundle is exercised by `build`/`assemble`.
