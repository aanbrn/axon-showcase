## 1. Upgrade the ESLint toolchain

- [ ] 1.1 In `showcase-web-ui/package.json`, bump `eslint` from `^8.57.1` to the current 10.x line, add
      `@eslint/js` (10.x) as a devDependency, and bump `eslint-plugin-react-hooks` to a version that supports ESLint 10
      (7.x). Keep `typescript-eslint` on the 8.x line (it supports ESLint 8/9/10) and keep `eslint-config-prettier`
      and `eslint-plugin-header`. Verify with `npm install` that the lockfile updates and `npm ls eslint` resolves a
      single 10.x `eslint`.
- [ ] 1.2 Replace `.eslintrc.cjs` with a flat `eslint.config.js`: use `@eslint/js` recommended, the `typescript-eslint`
      flat config (`tseslint.configs.recommended`), `eslint-plugin-react-hooks` flat config
      (`reactHooks.configs.flat.recommended`), `eslint-config-prettier`, and the `header/header` SPDX rule in
      flat-plugin form (`plugins: { header }`, `'header/header': ['error', 'line', [' SPDX-License-Identifier: MIT']]`).
      Carry over `languageOptions` (browser globals, parser), `ignores` (dist, node_modules, build, .gradle), and the
      `root: true` semantics from the legacy config. Delete `.eslintrc.cjs`.
- [ ] 1.3 Run `npm run lint` and confirm it passes with zero warnings and the same violations rejected as before
      (the SPDX header rule still fires on a missing header). Confirm `npm run format:check` is unaffected.

## 2. Verify the CI warnings are gone

- [ ] 2.1 Run a fresh `npm ci` (e.g. `rm -rf node_modules && npm ci`) and confirm the ESLint 8 deprecation cluster
      (`eslint@8.57.1`, `@humanwhocodes/config-array`, `@humanwhocodes/object-schema`, `glob@7`, `inflight`,
      `rimraf@3`) no longer appears. The upstream `whatwg-encoding@3.1.1` notice (jsdom transitive, latest version)
      is expected to remain and is accepted.
- [ ] 2.2 Run `./gradlew :showcase-web-ui:check` and confirm lint, format, and tests all pass with the new ESLint.

## 3. Validate and document

- [ ] 3.1 Run `openspec validate --all` and confirm the change passes.
- [ ] 3.2 Update `AGENTS.md`'s frontend conventions bullet if it names ESLint 8 or the `.eslintrc` format, to reflect
      the flat `eslint.config.js` setup.