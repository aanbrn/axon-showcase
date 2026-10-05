# showcase/quality/dependency-security Specification

## Purpose

Ensures the build does not ship known-vulnerable dependencies: the platform constrains vulnerable transitive
dependencies — Jackson 3 (`tools.jackson.core`), Jackson 2 (`com.fasterxml.jackson.core`), Apache HttpClient 5,
`zstd-jni`, and `io.netty` — to patched versions so the Snyk scan reports clean, and the web UI's npm dependencies are
audited by `npmAudit`.

## Requirements

### Requirement: Vulnerable transitive dependencies are constrained to patched versions

The platform SHALL constrain the transitive dependencies that dependency scans flag as vulnerable to their patched
versions: `tools.jackson.core` modules SHALL resolve through the `tools.jackson:jackson-bom` at a version that fixes the
reported issues (at least `3.2.3`), `com.fasterxml.jackson.core` modules (Jackson 2) SHALL resolve through the
`com.fasterxml.jackson:jackson-bom` at a version that fixes the reported issues (at least `2.22.3`),
`org.apache.httpcomponents.client5:httpclient5` SHALL resolve to at least `5.6.4`, `com.github.luben:zstd-jni` SHALL
resolve to at least `1.5.7-14`, and `io.netty` modules SHALL resolve through the `io.netty:netty-bom` at a version that
fixes the reported issue (at least `4.2.18.Final`).

#### Scenario: Jackson 3 modules resolve to the aligned BOM version

- **WHEN** a module that depends on `elasticsearch-java` resolves its runtime classpath
- **THEN** `tools.jackson.core:jackson-core` and `tools.jackson.core:jackson-databind` resolve to the
  `tools.jackson:jackson-bom` version, which is at least `3.2.3`

#### Scenario: Jackson 2 modules resolve to the patched BOM version

- **WHEN** a module that depends on `jackson-databind` resolves its runtime classpath
- **THEN** `com.fasterxml.jackson.core:jackson-core` and `com.fasterxml.jackson.core:jackson-databind` resolve to the
  `com.fasterxml.jackson:jackson-bom` version, which is at least `2.22.3`

#### Scenario: httpclient5 resolves to a patched version in every consuming module

- **WHEN** a module that depends on `opensearch-rest-client` resolves its runtime classpath
- **THEN** `org.apache.httpcomponents.client5:httpclient5` resolves to version `5.6.4` or newer

#### Scenario: zstd-jni resolves to a patched version wherever kafka-clients is present

- **WHEN** a module that depends on `kafka-clients` resolves its runtime classpath
- **THEN** `com.github.luben:zstd-jni` resolves to version `1.5.7-14` or newer

#### Scenario: Netty modules resolve to a patched version wherever reactor-netty is present

- **WHEN** a module that depends on `reactor-netty-http` resolves its runtime classpath
- **THEN** `io.netty:netty-codec-http` resolves to version `4.2.18.Final` or newer

#### Scenario: Dependency scan reports no vulnerable paths

- **WHEN** `snyk test --all-sub-projects --policy-path=.snyk` runs against the build
- **THEN** none of `showcase-projection-model`, `showcase-projection-service`, `showcase-query-client`, and
  `showcase-query-service` report a vulnerable path for Jackson 3 or `httpclient5`, no sub-project reports one for
  Jackson 2, `zstd-jni`, or `io.netty`, and the only suppressed findings are those pinned in `.snyk` with a stated
  reason and expiry

### Requirement: Local dependency security scan task

The build SHALL provide a `dependencySecurityCheck` Gradle task that runs the Snyk dependency scan
(`snyk test --all-sub-projects --policy-path=.snyk`) across all sub-projects and reports the result to the developer.
The `--policy-path=.snyk` flag SHALL point the scan at the repository's `.snyk` policy so its suppressed findings take
effect. The task SHALL NOT be part of the `check` lifecycle.

#### Scenario: Developer runs the dependency security scan

- **WHEN** a developer runs `./gradlew dependencySecurityCheck` with the Snyk CLI installed
- **THEN** the task invokes `snyk test --all-sub-projects --policy-path=.snyk` against the build and reports the scan
  result, failing when vulnerable paths are found

#### Scenario: Normal build does not run the dependency scan

- **WHEN** a developer runs `./gradlew check` or any build task other than `dependencySecurityCheck`
- **THEN** the dependency scan does not run

#### Scenario: Snyk CLI is not installed

- **WHEN** a developer runs `./gradlew dependencySecurityCheck` without the Snyk CLI on `PATH`
- **THEN** the task fails with a clear message that the Snyk CLI is required

### Requirement: Web UI dependency vulnerability scan

The build SHALL provide an `npmAudit` task that scans the web UI's npm dependencies with `npm audit`, covering both
production and development dependencies, and failing when a high-severity (or greater) vulnerability is present. The
task SHALL use the project's pinned Node and SHALL NOT be part of the `check` lifecycle. The task SHALL consult a
suppression list (`showcase-web-ui/npm-audit-ignores.json`) that names npm advisories with no available fix, each pinned
to its advisory id with a stated reason and an expiry date, so that an unfixable advisory does not leave the check
permanently red; a suppressed advisory's transitive dependents SHALL also be treated as suppressed. The task SHALL fail
when any unsuppressed high-severity (or greater) vulnerability is present, SHALL fail on a malformed suppression entry
or one whose advisory id matches no finding in the report (so a stale or mistyped entry cannot sit silently), and SHALL
report each suppressed advisory and its expiry.

#### Scenario: Developer runs the web UI vulnerability scan

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmAudit` and a high-severity vulnerability is present
- **THEN** the task fails and reports the vulnerable npm package and its advisory

#### Scenario: A development-dependency advisory is reported

- **WHEN** a high-severity vulnerability is present in a development dependency
- **THEN** the task fails, because the scan covers development as well as production dependencies

#### Scenario: A suppressed advisory passes with its expiry reported

- **WHEN** the only high-severity findings trace to an advisory listed in the suppression list with an unexpired
  `expires` date
- **THEN** the task succeeds and reports the suppressed advisory and its expiry

#### Scenario: An expired suppression fails again

- **WHEN** the only high-severity findings trace to a suppressed advisory whose `expires` date is in the past
- **THEN** the task fails, so the suppression re-surfaces for re-assessment rather than hiding the finding indefinitely

#### Scenario: A stale or mistyped suppression entry fails

- **WHEN** the suppression list holds an entry whose advisory id matches no finding in the `npm audit` report
- **THEN** the task fails, naming that entry, so a suppression for an already-fixed advisory (or a typo) cannot sit
  silently in the list

#### Scenario: A malformed suppression entry fails

- **WHEN** a suppression entry is missing its `id`, `reason`, or `expires` field
- **THEN** the task fails, naming the entry

#### Scenario: A clean web UI audit passes

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmAudit` and no high-severity vulnerability is present
- **THEN** the task completes successfully

#### Scenario: Normal build does not run the web UI vulnerability scan

- **WHEN** a developer runs `./gradlew check` or any build task other than `npmAudit`
- **THEN** the web UI vulnerability scan does not run
