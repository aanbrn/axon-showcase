# Design

## Context

See `proposal.md` — Why. Current state that shapes the approach:

- `gradle/libs.versions.toml` pins `jackson2-bom = "2.22.2"` (`com.fasterxml.jackson:jackson-bom`) and
  `jackson3-bom = "3.2.2"` (`tools.jackson:jackson-bom`); `platform/build.gradle.kts` applies both
  (`api(platform(libs.jackson2.bom))`, `api(platform(libs.jackson3.bom))`), so each Jackson line's version is
  single-sourced in the catalog.
- `.snyk` carries no Jackson entry for either line, so the scan's Jackson findings are unmasked. Snyk names the fixes
  (`2.22.3`, `3.2.3`), and `dependencyUpdates` confirms both BOM updates are available.
- The `dependency-security` constraint requirement already enumerates `tools.jackson.core` (Jackson 3) — at `3.1.4` core
  / `3.1.5` databind — alongside `httpclient5`, `zstd-jni`, and `io.netty`, but not Jackson 2.
- `address-snyk-2026-09-21-findings` is the precedent: a fixable advisory is fixed by a patch bump, suppression is
  reserved for advisories with no patched release, and this requirement is extended to name the newly-constrained
  coordinate.

## Goals / Non-Goals

**Goals:**

- Clear all seven Jackson advisories with real fixes (no suppression).
- Keep the `dependency-security` constraint requirement accurate for both Jackson lines.

**Non-Goals:**

- The deferred Jackson 3 **backend migration** (ADR-0003): bumping the Jackson 3 patch is dependency hygiene, not that
  migration.
- Any `.snyk` suppression, or application code changes.

## Decisions

- **Bump both catalog BOM pins rather than suppress.** Patched releases exist for both lines, so each fix is a one-line
  version change; `.snyk` suppression is reserved for advisories with no patched release, per the precedent. Rejected: a
  `.snyk` ignore (fixes exist), and overriding only the flagged modules instead of the BOMs (splits the single source of
  truth).
- **Extend the `dependency-security` requirement for both lines.** The requirement enumerates the constrained
  transitives, so it needs the Jackson 3 floor raised to `3.2.3` and Jackson 2 added at `2.22.3` — the same move the
  precedent made for `io.netty`. Rejected: `skip_specs` (the requirement would under-describe the constraints the build
  applies and the versions that satisfy the scan).

## Risks / Trade-offs

- **Compatibility of `2.22.3` / `3.2.3` with Spring Boot 3.5.16 and Axon.** Both are patches within their existing
  lines, and the platform BOMs already override Spring Boot's managed versions → verify with the full test suite and the
  scan.
- **The scan needs the Snyk CLI (and a token for the credentialed run).** No CI job exercises it on a PR → verify the
  bump locally where possible and dispatch `dependency-security.yml` after merge, naming that in the change's report.

## Migration Plan

- Apply: the two catalog bumps; no build or code change to sequence.
- Rollback: revert the two lines (the previous patch versions remain valid pins).
