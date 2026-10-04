# Proposal

## Why

The `Dependency Security` workflow's scheduled `web-ui-audit` job fails on `braces <= 3.0.3` (GHSA-vfj7-8cjw-p6xm, a
stack-exhaustion DoS), a dev-only transitive of `eslint-plugin-boundaries` via `@boundaries/elements`/`micromatch`. The
advisory's patched-versions field is **empty** — the fix is not upstream — so `npm audit --audit-level=high` can never
pass, and `npmAudit` has no suppression mechanism, unlike the Snyk scan's `.snyk` policy. A genuine unfixable advisory
therefore leaves the check permanently red, which is the condition ADR-0014 already recorded as owed its own change
("npm has no `.snyk` analogue… an ignore mechanism … becomes its own change").

## What Changes

- Add `showcase-web-ui/npm-audit-ignores.json` — a suppression list of npm advisories, each pinned to an advisory id
  with a `reason` and an `expires` date, mirroring `.snyk`'s shape.
- Rework the `npmAudit` task to run `npm audit --json`, filter the suppressed advisories, and fail on the unsuppressed
  high-severity remainder — reporting which findings are suppressed and when each expires.
- Suppress GHSA-vfj7-8cjw-p6xm with a dated rationale (dev-only, no patched version upstream).
- Update ADR-0014 — its Consequences (it named this mechanism as a follow-on) and its Decision, whose `npmAudit`
  invocation (`npm audit --audit-level=high`) and "`npm audit` without `--audit-level` … rejected" clause the reworked
  task supersedes — and the `dependency-security` spec.
- Report the upstream gap: `micromatch/braces` has no release fixing CVE-2026-93687.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-security`: extends the web UI dependency vulnerability scan requirement with a
  documented, expiring suppression mechanism (an advisory with no available fix is suppressed by id until its expiry,
  and the unsuppressed remainder still fails the task).

## Impact

`showcase-web-ui/npm-audit-ignores.json` (new), the `npmAudit` task in
`build-logic/src/main/kotlin/frontend-conventions.gradle.kts`, ADR-0014, and the `dependency-security` spec. No runtime
dependency, image, or service change — the suppressed advisory is dev-only lint tooling that never reaches the built
bundle. The `snyk` job and the JVM side are unaffected.
