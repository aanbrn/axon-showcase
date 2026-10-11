# Design

## Context

See `proposal.md` — Why. The failing scans are `snyk` (two findings in `org.postgresql:postgresql@42.7.13`, fixed by
`42.7.14`) and `web-ui-audit` (six npm findings). The version catalog owns `postgresql` (`gradle/libs.versions.toml`).
The web UI's findings are dev-only transitives — `eslint-plugin-boundaries` → `@boundaries/elements` →
`handlebars`/`micromatch`/`braces`, plus `source-map-js` — and `showcase-web-ui/npm-audit-ignores.json` already
suppresses one (`braces`, expiring 2026-11-16).

## Goals / Non-Goals

**Goals:**

- The two scans pass on `main`.
- No unnecessary breaking change.

**Non-Goals:**

- Applying the dependency updates (issue #13): a separate unit that shares `libs.versions.toml`, so it is sequenced.
- A Spring Boot bump: the driver is catalog-owned, so a one-line bump suffices.

## Decisions

**Bump `postgresql` in the catalog.** The driver is catalog-owned (`libs.versions.toml`), so the fix is one line;
`42.7.14` was verified to exist (Maven Central's metadata lists it — its search API was stale and reported none).

**Force `handlebars` via `overrides`, not npm's suggested fix.** npm offers `eslint-plugin-boundaries@5.4.0` — a
downgrade from the installed `7.2.0`, which would break the FSD boundary lint — because the plugin pins
`handlebars: "4.7.9"` exactly. An `overrides` entry forces the fixed `4.7.10` under it; `source-map-js` needs no
override — its dependents' `^1.2.1` resolves `1.2.2` on a lockfile refresh. _Alternative rejected:_ taking npm's
`--force` fix (a breaking plugin downgrade).

**Suppress only what has no fix.** `@boundaries/elements` (3.1.1) and `eslint-plugin-boundaries` (7.2.0) are already at
their latest, and their remaining vulnerable dependency is the already-suppressed `braces`; any finding that still has
no fix gets an advisory-id suppression with a rationale and an expiry, matching the existing entry.

## Risks / Trade-offs

- The driver bump is runtime-path → verified with the integration tests (Docker), not just a compile.
- The `overrides` entry diverges the resolved tree from the plugin's declared pin → re-check when no package in the tree
  still requests `handlebars 4.7.9` (both `eslint-plugin-boundaries` and `@boundaries/elements` pin it exactly, so both
  must bump before the override is dropped).
- A suppression carries an expiry, so it re-surfaces if upstream ships no fix.

## Migration Plan

None: dependency bumps taking effect on the next build/install; the scans re-run on the next schedule or dispatch.
