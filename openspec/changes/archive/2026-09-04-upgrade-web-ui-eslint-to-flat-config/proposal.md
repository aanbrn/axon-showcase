# Proposal: Upgrade the web-UI ESLint to a flat-config, non-deprecated line

## Why

The `showcase-web-ui` module runs ESLint 8.57.1 — deprecated ("This version is no longer supported") — pinned to the
legacy `.eslintrc.cjs` format that ESLint 9/10 dropped. ESLint 8's deprecated transitive dependencies
(`@humanwhocodes/config-array`, `@humanwhocodes/object-schema`, `glob@7`, `inflight`, `rimraf@3`) surface six of the
seven `npm warn deprecated` lines in every CI `npm ci` step. Upgrading to the current ESLint line (9.x, already flat
config; 10.x) and migrating the config to the flat `eslint.config.js` format removes the deprecated toolchain with no
change to the enforced lint rules or the `npm run lint` gate.

## What Changes

- Upgrade `eslint` from `^8.57.1` to the current 10.x line in `showcase-web-ui/package.json`, keeping the lint script
  and gate semantics identical.
- Migrate `.eslintrc.cjs` to `eslint.config.js` (flat config): `@eslint/js` recommended, `typescript-eslint`
  flat config, `eslint-plugin-react-hooks` flat config, `eslint-config-prettier`, and the `header/header` SPDX
  rule in flat-plugin form. The original `eslint-plugin-header` is unmaintained (2021) and declares no rule schema,
  which ESLint 9/10's config validation rejects — replace it with the maintained drop-in fork
  `@tony.ganchev/eslint-plugin-header` (same rule semantics, flat-config-native).
- Keep the same enforced rules and the `npm run lint`/`npm run format:check` check wiring — the lint gate behavior
  does not change.
- Optionally suppress the remaining upstream `whatwg-encoding` deprecation only if it is resolvable; it is an
  upstream-transitive of `jsdom` at its latest version, so it is expected to remain as an accepted external notice.

## Capabilities

### New Capabilities

_(none — pure tooling upgrade, no behavioral change)_

### Modified Capabilities

_(none — `npm run lint`, the rules enforced, and the `check` gate are unchanged)_

## Impact

- **Dependencies**: `showcase-web-ui/package.json` / `package-lock.json` — bump `eslint` to 10.x, add
  `@eslint/js`, bump `eslint-plugin-react-hooks` to 7.x, and swap `eslint-plugin-header` for the maintained fork
  `@tony.ganchev/eslint-plugin-header`.
- **Code**: `showcase-web-ui/.eslintrc.cjs` → `showcase-web-ui/eslint.config.js` (flat config rewrite).
- **Docs**: note the flat-config setup in `AGENTS.md`'s frontend conventions bullet if wording changes.
- **Behavior**: none — lint rules, gate, and scripts are unchanged. The deprecation warnings for the ESLint 8
  cluster disappear from CI logs; the upstream `whatwg-encoding` notice remains.