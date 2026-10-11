# Proposal

## Why

The scheduled `Dependency Security` run fails on `main` — both `snyk` and `web-ui-audit` — so the repository's
dependency security is red. Snyk reports two new findings in `org.postgresql:postgresql@42.7.13` (a High "Not Failing
Securely" and a Medium), and the web UI's `npm audit` reports six (five high, one critical): `handlebars` (critical),
`source-map-js`, and the `@boundaries/elements` / `eslint-plugin-boundaries` chain — all dev-only transitives.

## What Changes

- `gradle/libs.versions.toml`: `postgresql = "42.7.13"` → `"42.7.14"` (fixes the driver's two findings; the version
  exists on Maven Central).
- `showcase-web-ui/package.json` (+ lockfile): force `handlebars` → `4.7.10` via `overrides` (the critical;
  `eslint-plugin-boundaries@latest` pins `handlebars: "4.7.9"` exactly, so npm's offered fix is a breaking downgrade of
  the plugin); the lockfile refresh also moves `source-map-js` → `1.2.2`, which needs no override (its dependents'
  `^1.2.1` resolves it).
- `showcase-web-ui/npm-audit-ignores.json`: suppress any finding that survives with no fix (advisory id + rationale +
  expiry), as the existing `braces` entry does.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. A dependency bump changes coordinates, not behavior, and no capability spec owns them.

## Impact

- Files: `gradle/libs.versions.toml`, `showcase-web-ui/package.json`, `showcase-web-ui/package-lock.json`,
  `showcase-web-ui/npm-audit-ignores.json` if a suppression is needed, and `docs/ideas.md` (parking the override's
  re-check).
- The `postgresql` driver is runtime-path, so the bump is verified with the integration tests.
- The web-UI bumps are dev-only (lint/build), never in the built bundle.
