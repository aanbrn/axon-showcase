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
- `npmAudit` runs `npm audit --audit-level=high` over the full dependency tree, failing on high-severity findings, and
  run by a `web-ui-audit` job in the dependency-security workflow (renamed so its name states its role rather than one
  of its two scanners).

Alternatives rejected: extending Snyk to `showcase-web-ui/package-lock.json` (routes the UI through Snyk's npm support
for a second ecosystem, rather than the module's own npm); a dedicated web-UI update workflow (an extra tracker and
schedule for a report the existing "Dependency updates" issue can carry); `npm audit` without `--audit-level` (npm
resolves the default to `low`, so it fails on low-and-above — noisy); and `--omit=dev` (hides devDependency advisories,
which are part of the supply chain).

## Consequences

- The two ecosystems are monitored symmetrically; the checks are locally runnable
  (`./gradlew :showcase-web-ui:npmOutdated` and `:npmAudit`) and reuse the one pinned Node version.
- npm has no `.snyk` analogue: there is no in-repo suppression for a genuine unfixable npm advisory. If one appears, an
  ignore mechanism (or a deliberately version-pinned dev dependency) becomes its own change.
- Both checks query a registry-served endpoint and so need network access, unlike `dependencyUpdates`' lock-file-free
  scans; the Snyk-free npm audit needs no token.
- Renaming the workflow moves its dispatch reference to `gh workflow run dependency-security.yml`.
