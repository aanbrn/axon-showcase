# ADR-0014: Web UI dependency checks use npm's own tooling

Date: 2026-09-24

Status: Accepted

## Context

The JVM modules' dependencies are monitored twice: `dependencyUpdates` reports catalog-owned Gradle coordinates, and the
Snyk scan (`dependencySecurityCheck`) fails on known-vulnerable transitives. The web UI's npm dependencies
(`showcase-web-ui/package.json` / `package-lock.json`) sat outside both — `dependencyUpdates` reads only the version
catalog, the Snyk scan runs `snyk test --all-sub-projects` (Gradle only), and `.snyk` carries only `SNYK-JAVA-*` ignores
— so an outdated or vulnerable npm package produced neither an update issue nor a security finding. ADR-0006 recorded
the Snyk scan's mechanism (a root-level `Exec` task); it did not address the frontend.

## Decision

Cover the web UI's npm dependencies with npm's own tooling, as two Gradle tasks that use the pinned Node from the
node-gradle plugin and stay out of `check`:

- `npmOutdated` writes `npm outdated`'s report (and npm's exit code) for the `dependency-updates` workflow to fold into
  the "Dependency updates" issue.
- `npmAudit` runs `npm audit --json` over the full dependency tree and lets `NpmAuditRules` decide the verdict: it fails
  on any high-severity finding except one that traces to an advisory listed in `showcase-web-ui/npm-audit-ignores.json`
  with an unexpired date. It is run by a `web-ui-audit` job in the dependency-security workflow (renamed so its name
  states its role rather than one of its two scanners).

Alternatives rejected: extending Snyk to `showcase-web-ui/package-lock.json` (routes the UI through Snyk's npm support
for a second ecosystem, rather than the module's own npm); a dedicated web-UI update workflow (an extra tracker and
schedule for a report the existing "Dependency updates" issue can carry); `npm audit` with npm's default `--audit-level`
(of `low` as observed in npm's `--help`) — it would fail on low-and-above — noisy; `NpmAuditRules` keeps the
high-and-above floor as an explicit filter instead); and `--omit=dev` (hides devDependency advisories, which are part of
the supply chain).

## Consequences

- The two ecosystems are monitored symmetrically; the checks are locally runnable
  (`./gradlew :showcase-web-ui:npmOutdated` and `:npmAudit`) and reuse the one pinned Node version.
- npm's suppression mechanism now mirrors `.snyk`: `showcase-web-ui/npm-audit-ignores.json` lists advisories with no
  available fix, each pinned to its advisory id with a stated reason and an expiry, and `NpmAuditRules` suppresses the
  finding (and its transitive dependents) until that date — the owed follow-on recorded here before it landed.
- Both checks query a registry-served endpoint and so need network access, unlike `dependencyUpdates`' lock-file-free
  scans; the Snyk-free npm audit needs no token.
- Renaming the workflow moves its dispatch reference to `gh workflow run dependency-security.yml`.
- `braces <= 3.0.3` (GHSA-vfj7-8cjw-p6xm, CVE-2026-93687) has no patched **release** upstream, so its suppression entry
  is a dated acceptance, not a fix. The fix exists as the open `micromatch/braces#72` (the advisory thread `#70` is
  locked); the entry retires when that PR merges and a `braces` release carrying it resolves (the expiry re-surfaces the
  finding for re-assessment until then).
