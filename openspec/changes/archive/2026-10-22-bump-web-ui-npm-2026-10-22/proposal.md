# Proposal

## Why

The web UI's npm update report (`npm outdated`, re-run rather than read off the weekly tracker issue) names in-range
releases for eight of the module's declared dependencies: `@hookform/resolvers` 5.9.1 → 5.9.2, `@tanstack/react-query`
5.104.3 → 5.104.5, `eslint` 10.12.0 → 10.13.0, `eslint-plugin-boundaries` 7.2.0 → 7.2.1, the `@typescript-eslint/*` pair
and `typescript-eslint` at 8.71.2 → 8.72.0, and `vite` 8.3.4 → 8.3.5. `npm update` reconciles the whole graph rather
than those eight, so the lockfile also moves their transitives and drops one stale nested duplicate.

The report lists two majors as well, and this change stops at the in-range version of each. `typescript` 7.0.2 is
suppressed by policy, because `typescript-eslint`'s peer range caps TypeScript below the next major;
`@hookform/resolvers` 6.0.0 is a migration with its own decision, not an update-check row.

## What Changes

- **`showcase-web-ui/package-lock.json`** — the resolved graph, 51 packages moved and one stale nested entry
  (`resolve/node_modules/is-core-module`) removed. Two entries also flip from `devOptional` to `dev` (`fast-deep-equal`,
  `require-from-string`) — the diff's only non-version changes — and the declared ranges are otherwise unchanged: every
  one of these releases already satisfies the range it sits under.
- **`showcase-web-ui/package.json`** — the `overrides` block is deleted. `handlebars` still resolves to `4.7.10` from
  the two plugins' own exact pins, so the override is a no-op; keeping it would leave an unexpiring lock whose recorded
  trigger has fired.
- **`docs/ideas.md`** — the entry that parked the `handlebars` re-check is removed: the condition it named is met, so it
  is implemented rather than still open.
- The direct moves are the eight named above, of which `vite` is the only bundle-path one; the rest is transitive and
  dev-tooling churn (`rolldown` bindings, browserslist data, `postcss`, `undici`, `immer`).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — a dependency bump no capability spec covers (`skip_specs`). The module's gates, its bundle and its npm audit
check are the verification, not a behavior change.

## Impact

- **`showcase-web-ui` and `docs/ideas.md`.** The module's frontend `check` (lint, format-check, the TypeScript
  type-check, Vitest), the production bundle build, and `npmAudit` are the verification; `vite` moved, so the bundle is
  built rather than assumed. The `docs/ideas.md` edit removes the parked entry this change implements.
- **No API or runtime contract change:** every moved runtime package is a patch or minor inside its declared range, and
  the override's removal resolves to the same `handlebars`.
