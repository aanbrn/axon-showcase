# Tasks

## 1. Implementation

- [x] 1.1 Bump `jackson2-bom` from `2.22.2` to `2.22.3` and `jackson3-bom` from `3.2.2` to `3.2.3` in
      `gradle/libs.versions.toml`. Verify the resolved versions
      (`./gradlew :showcase-command-service:dependencies --configuration runtimeClasspath` shows
      `com.fasterxml.jackson.core:jackson-databind:2.22.3`, and a Jackson-3 consuming module — `:showcase-query-service`
      — shows `tools.jackson.core:jackson-databind:3.2.3`), and that `./gradlew dependencyUpdates` no longer lists
      either Jackson BOM as an available update in `build/dependencyUpdates/report.txt`. — Done: Jackson 2 resolves to
      `2.22.3` and Jackson 3 to `3.2.3`; both BOMs now appear only under "using the latest milestone version", with no
      `[current -> available]` update row.

## 2. Verification

- [x] 2.1 Run the dependency security scan — `./gradlew dependencySecurityCheck` (needs the Snyk CLI; the credentialed
      full scan needs `SNYK_TOKEN`) — and confirm the seven advisories (four Jackson 2, three Jackson 3) are gone and
      the scan reports no vulnerable paths. Read the scan output. If no local Snyk/token is available, name the deferred
      post-merge `dependency-security.yml` dispatch in the change's report; do not tick on an assumed result. — Done:
      `./gradlew dependencySecurityCheck` reports "Tested 20 projects, no vulnerable paths were found."
- [x] 2.2 Run the full Docker-free test suite — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` — as the
      classpath-bump regression check; it passes. — Done: BUILD SUCCESSFUL (352 tasks).
- [x] 2.3 Run `openspec validate --changes` (the delta validates and preserves every existing scenario) and
      `./gradlew spotlessApply` followed by `./gradlew spotlessCheck`; both pass. — Done: both pass.
- [x] 2.4 Record the `showcase/quality/dependency-security` `## Purpose` refresh owed at archive (a delta cannot carry a
      Purpose): its Purpose enumerates the constrained transitives and now omits Jackson 2. Apply it in the archive
      commit and name the deferral in the report. — Done: the Purpose now names Jackson 2.
